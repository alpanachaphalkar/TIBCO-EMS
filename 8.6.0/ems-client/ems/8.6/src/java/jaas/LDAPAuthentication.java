/*
 * Copyright (c) 2013-2016 TIBCO Software Inc.
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 *
 * $Id: LDAPAuthentication.java 90180 2016-12-13 23:00:37Z $
 *
 */
package com.tibco.tibems.tibemsd.security.jaas;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.CommunicationException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import com.tibco.tibjms.admin.*;


/**
 * This class implements an LDAP based JAAS module for EMS. It will validate 
 * all connections (be they users, routes, etc.) by authenticating to the
 * LDAP server using the supplied credentials.  
 * <p>
 * Most properties added in the JAAS configuration file will be passed
 * directly to JNDI when creating the LDAP context. This allows a lot
 * of flexibility in configuring the server connection. Only properties
 * that begin with "tibems" are reserved for this module.
 * <p>
 * Some properties may be required to be set in the environment, such as SSL
 * related properties.  Those are to be setup through the jvm_option parameter
 * in the EMS server configuration.  However, the SSL certificate is handled here
 * for convenience.
 * <p>
 * This module will keep one lookup context open using a manager context,
 * and then use copies of that context to search for users.  This allows the
 * LDAP implementation to reuse the connection for subsequent searches,
 * improving performance.
 * <p>
 * To enable this module, specify the location of a JAAS Configuration file
 * through the EMS server configuration option "jaas_config_file".  Please
 * refer to the EMS user guide for further options concerning JAAS Login
 * Modules.
 * <p>
 * The JAAS configuration file entry for this login module should have
 * a section similar to the following:
 * <pre>
 * EMSUserAuthentication {
 *      com.tibco.tibems.tibemsd.security.jaas.LDAPAuthentication required
 *      debug=false
 *      tibems.ldap.url="ldaps://ldapserver:391"
 *      tibems.ldap.trustStore="/certificates/cacerts"
 *      tibems.ldap.user_base_dn="ou=Marketing,dc=company,dc=com"
 *      tibems.ldap.user_attribute="uid"
 *      tibems.ldap.scope="subtree"
 *      tibems.cache.enabled=true
 *      tibems.cache.user_ttl=600
 *      tibems.ldap.manager="CN=Manager"
 *      tibems.ldap.manager_password="password" ;
 * }; 
 * </pre>
 * </p>
 * @see com.tibco.tibems.tibemsd.security.jaas.LDAPSimpleAuthentication
 * @see javax.security.auth.spi.LoginModule
 */
public class LDAPAuthentication extends LDAPSimpleAuthentication implements
        LoginModule 
{
    private String      managerName        = null;
    private String      managerPassword    = null;
    private LdapContext managerContext     = null;
    
    private String      userBaseDn         = null;
    private String      userFilter         = "{0}={1}";
    private String      userAttribute      = null;

    private int         searchScope        = SearchControls.ONELEVEL_SCOPE;

    private int         connectRetries     = 0;
    private int         connectDelay       = 1000;

    /*
     * For performance, the manager contexts are pooled.
     */
    private static HashMap<String, LdapContext> managerContextPool = new HashMap<String, LdapContext>();
    
    /*
     * To maintain information across authorization attempts and LoginModule
     * instances, the user cache(s) are kept in a static HashMap.  By default,
     * there will be one user cache per set of LoginModule search criteria,
     * but multiple LoginModule instances can be configured to use the same
     * cache. 
     */
    protected static HashMap<String, UserCache> userCaches = new HashMap<String, UserCache>();
    
    /*
     * The local instance of the user cache.
     */
    protected UserCache                         userCache     = null;
    protected int                               cacheTTL      = 0;
    protected String                            cacheInstance = null;
    
    /**
     * The distinguished name of the server that this module will use
     * when binding to the LDAP server to perform a search.  The
     * specified user must have permissions to search LDAP for users
     * under the entry specified by ldap.user_base_dn.   The default
     * is "CN=Manager".
     */
    public static final String LDAP_MANAGER_NAME = "tibems.ldap.manager";

    /**
     * The password used when binding to the LDAP server as the manager.
     * This password may be mangled using the EMS Administration tool.
     */
    public static final String LDAP_MANAGER_PASSWORD = "tibems.ldap.manager_password";

    /**
     * The base path for the LDAP search.
     */
    public static final String LDAP_BASE_DN = "tibems.ldap.user_base_dn";

    /**
     * The attribute that will be compared to the user name for the search.
     * The default is "uid".
     */
    public static final String LDAP_USER_ATTRIBUTE = "tibems.ldap.user_attribute";

    /**
     * The filter used when searching for a user.  If a more complex filter is
     * needed, use this property to override the default. Any occurrence of
     * '{0}' will be replaced with the user attribute, and '{1}' in the
     * search string will be replaced with the user name.  The default
     * is "{0}={1}".
     */
    public static final String LDAP_FILTER = "tibems.ldap.user_filter";

    /**
     * The scope of the search.  Valid values include "onelevel", "subtree",
     * and "object".  Default is to use a one level search.
     */
    public static final String LDAP_SCOPE = "tibems.ldap.scope";
    
    /**
     * If there is a communication failure with the LDAP server, the module will
     * retry the connection this many times.  The default value is 0, meaning no
     * retries will be attempted.
     */
    public static final String LDAP_RETRIES = "tibems.ldap.retries";
    
    /**
     * The module will wait this number of milliseconds before retrying the connection
     * to the LDAP server. The default is "1000".
     */
    public static final String LDAP_RETRY_DELAY = "tibems.ldap.retry_delay";
    
    /**
     * Enables caching of users for better performance.  The default is "false".
     */
    public static final String LDAP_CACHE_ENABLED = "tibems.cache.enabled";

    /**
     * Specifies the maximum time (in seconds) that cached LDAP data is retained before
     * it is refreshed.  The default is 60.
     */
    public static final String LDAP_CACHE_TTL = "tibems.cache.user_ttl";

    /**
     * A string that represents an instance of the user cache.  When stacked
     * LoginModules specify the same instance, they will share the same user
     * cache as a form of optimization.  The default is a unique cache based
     * on tibems.ldap.url, tibems.ldap.user_base_dn, and
     * tibems.ldap.user_attribute.
     */
    public static final String LDAP_CACHE_INSTANCE = "tibems.cache.instance";
    
    /**
     * This class implements a cache for users.  Once a user has been
     * successfully authenticated, the user is added to the cache.  Future
     * login requests will be authenticated against cached entries, providing
     * better performance.
     * <p>
     * When a cache is created, a cleanup thread is started that periodically
     * purges the cache of expired users.  
     */    
    class UserCache
    {
        private HashMap<String, UserData> cache         = null;
        private int                       cacheTTL      = 60;
        private MessageDigest             messageDigest = null;
        
        /**
         * This class represents a user entry in the cache.
         */
        private class UserData
        {
            private byte[] pwMessageDigest = null;
            private long   timestamp       = 0;
            private String name            = null;
            private List<String> groups    = null;
            
            /**
             * UserData constructor.
             * 
             * @param name the name of the user.
             * @param msgDigest the hashed password of the user.
             */
            private UserData(String name, byte[] msgDigest)
            {
                this.name = name;
                update(msgDigest);
            }
            
            /**
             * Get the user message digest.
             * @return message digest.
             */
            private byte[] getPwMsgDigest()
            {
                return pwMessageDigest;
            }
            
            /**
             * Indicates whether a user entry has expired, based on the cacheTTL
             * value.
             * 
             * @param ttlSeconds the duration in seconds that users remain valid.
             * 
             * @return true if this user has expired, false otherwise.
             */
            private boolean hasExpired(int ttlSeconds)
            {
                return (timestamp + (ttlSeconds * 1000) < System.currentTimeMillis());
            }
            
            /**
             * Gets the name of the user.
             * @return the name of the user.
             */
            private String getName()
            {
                return name;
            }
            
            /**
             * Updates the user's password and timestamp.
             * @param msgDigest the hashed password.
             */
            private void update(byte[] msgDigest)
            {
                pwMessageDigest = msgDigest;
                timestamp       = System.currentTimeMillis();
            }
            
            /**
             * Sets the groups this user belongs to.
             * @param newGroups a new or updated list of groups.
             */            
            private void setGroups(List<String> newGroups)
            {
                if (newGroups == null)
                {
                    groups = null;
                    return;
                }
                
                if (groups == null)
                {
                    groups = new ArrayList<String>(newGroups);
                }
                else
                {              
                    groups.clear();
                    groups.addAll(newGroups);
                }
            }
            
            /**
             * Retrieves the groups this user belongs to.
             * @returns the current list of groups.
             */
            private List<String> getGroups()
            {
                return groups;
            }
        }

        /**
         * This class implements a cleanup thread for the cache.  Every five
         * minutes a cleanup method is called on the cache to remove expired
         * entries.
         */    
        private class CacheCleanupThread extends Thread
        {
            private static final long cacheCleanupInterval = 5 * 60 * 1000;
            
            /**
             * CacheCleanupThread constructor.
             */
            private CacheCleanupThread()
            {
                super("User Cache Cleanup");
                setDaemon(true);
                start();  
            }
            
            public void run()
            {
                while(true)
                {
                    try
                    {
                        Thread.sleep(cacheCleanupInterval);
                        cleanup();
                    }
                    catch(InterruptedException e) {}
                }
            }
        }

        /**
         * The UserCache constructor will setup a messageDigest, and
         * create the cache cleanup thread.
         * 
         * @param ttl - the maximum time (in seconds) that cached data is
         * valid.
         */
        UserCache(int ttl)
        {
            if (ttl <= 0)
                throw new IllegalArgumentException("Invalid cache value.");

            cacheTTL = ttl;
            
            try
            {
                messageDigest = MessageDigest.getInstance("SHA-256");
            }
            catch (NoSuchAlgorithmException nsae)
            {
                System.err.println("Fatal Error:  Failed to initialize messageDigest.");
                RuntimeException re = new RuntimeException("Failed to initialize MessageDigest");
                re.initCause(nsae);
                throw re;
            }
            
            cache = new HashMap<String, UserData>();

            new CacheCleanupThread();
        }

        /**
         * Removes expired users from the cache.
         */
        void cleanup()
        {
            synchronized(cache)
            {
                Iterator<UserData> iter = cache.values().iterator();
                while (iter.hasNext())
                {
                    UserData ud = iter.next();
                    if (ud.hasExpired(cacheTTL))
                    {
                        iter.remove();
                        debugLog("LDAP LoginModule:  Removing expired user %s from cache\n",
                                ud.getName());
                    }
                }
            }                    
        }
        
        /**
         * Adds or updates a user in the cache.
         * 
         * @param name name of the user
         * @param password password of the user.  If different, it will
         * be updated.
         */        
        private void put(String name, String password) throws LoginException
        {
            UserData user = null;
            
            synchronized(cache)
            {
                messageDigest.update(password.getBytes());
                byte[] pwDigest = messageDigest.digest();

                user = cache.get(name);
                
                if (user != null)
                    user.update(pwDigest);
                else
                    user = new UserData(name, pwDigest);
                
                cache.put(name, user);
            }
            
            debugLog("LDAP LoginModule: Updated cache with user %s.\n", name);
        }
        
        /**
         * Sets the groups a user belongs to.
         * 
         * @param name name of the user
         * @param list of group names.
         */          
        public void setGroups(String userName, List<String> groups)
        {
            UserData user;
            
            synchronized(cache)
            {
                user = cache.get(userName);
                
                if (user != null)
                {
                    user.setGroups(groups);
                    debugLog("LDAP LoginModule: Updated cache for user %s with groups %s.\n",
                            userName, groups.toString());
                }
            }
        }
        
        /**
         * Retrieves the groups a user belongs to.
         * 
         * @param name name of the user
         * @param list of group names.
         */          
        public List<String> getGroups(String userName)
        {
            UserData user;
            
            synchronized(cache)
            {
                user = cache.get(userName);
                
                if (user != null)
                    return user.getGroups();
            }
            
            return null;
        }
        
        /**
         * Authenticates a user using the cache.
         * 
         * @param name name of the user
         * @param password password of the user.
         * 
         * @return true if the user exists and the password is valid.  False
         * if the user does not exist, or the password is invalid.
         * 
         * @throws LoginException on an internal error.
         */         
        boolean authenticateUser(String name, String password) throws LoginException
        {
            boolean       success       = false;
            UserData      user          = null;
            MessageDigest messageDigest = null;
            
            debugLog("LDAP LoginModule: Authenticating user %s from cache: ", name);

            user = (UserData) cache.get(name);
            if (user == null)
            {
                debugLog("Entry not found.\n");
                return false;
            }
            
            if (user.hasExpired(cacheTTL))
            {
                debugLog("Entry has expired.\n");
                return false;
            }
                
            try
            {
                messageDigest = MessageDigest.getInstance("SHA-256");
            }
            catch (NoSuchAlgorithmException nsae)
            {
                throw buildLoginException(nsae, "Failed to initialize messageDigest.");
            }

            messageDigest.update(password.getBytes());
            success = MessageDigest.isEqual(user.getPwMsgDigest(), messageDigest.digest());

            debugLog("%s\n", (success ? "Success." : "Failed (Invalid password)."));
                
            return success;        
        }
    }

    /**
     * Gets or creates a user cache based on configuration parameters.
     * 
     * @return the user cache for this LoginModule.
     */
    protected UserCache getUserCache()
    {
        UserCache uc = null;
        
        if (cacheInstance == null)
        {
            cacheInstance = jaasConfig.getCurrentLdapUrl() + ":" + 
                userBaseDn + ":" + userAttribute;
        }

        synchronized (userCaches)
        {
            uc = userCaches.get(cacheInstance);
            if (uc == null)
            {
                debugLog("LDAP LoginModule: Creating cache <%s>.\n",
                    cacheInstance);

                uc = new UserCache(cacheTTL);
                userCaches.put(cacheInstance,uc);
            }
            else
            {
                debugLog("LDAP LoginModule: Using cache <%s>.\n",
                    cacheInstance);
            }
         }
        
        return uc;
    }

    /**
     * Returns an instance of the manager context for this module's LDAP URL.
     * If a pooled manager context does not exist for that LDAP URL, it
     * creates one.
     * 
     * @return a LdapContext created by calling newInstance() on the manager
     *         context.
     * @throws LoginException
     */
    protected LdapContext newManagerLdapContextInstance()
        throws NamingException, LoginException
    {
        synchronized (managerContextPool)
        {
            String key = jaasConfig.getString(
                LDAPSimpleAuthentication.LDAP_URL) + managerName;

            if (managerContext == null)
                managerContext = managerContextPool.get(key);
            
            if (managerContext != null)
            {
                 debugLog("LDAP LoginModule: Using existing manager context.\n");
            }
            else
            {
                String password;
                try
                {
                    password = TibjmsAdmin.unmanglePassword(managerPassword);
                }
                catch (Exception e)
                {
                    debugLog("LDAP LoginModule: Error unmangling manager password.");
                    throw buildLoginException(e, "Unable to unmangle password");
                }
                
                managerContext = createLdapContext(managerName, password);
                managerContextPool.put(key, managerContext);

                debugLog("LDAP LoginModule: Created new manager context.\n");
            }
        }

        return managerContext.newInstance(null);
    }
    
    /**
     * Closes the manager context (and all instances).
     */
    protected void closeManagerLdapContext()
    {
        synchronized (managerContextPool)
        {
            if (managerContext == null)
                return;
        
            String key = jaasConfig.getString(
                LDAPSimpleAuthentication.LDAP_URL) + managerName;
        
            managerContextPool.remove(key);

            closeLdapContext(managerContext);
            managerContext = null;
        }
    }
     
    /**
     * Sleeps in a thread for the configured connect delay time.
     */    
    private void loginDelay()
    {
        if (connectDelay > 0)
        {
            try
            {
                Thread.sleep(connectDelay);
            }
            catch (InterruptedException ie) {}
        }
    }
    
    /**
     *  Invoked by this LoginModule after successful LDAP authentication.
     *  @param userName the name of the user authenticated.
     *  @param userResult the search result for the member.
     *  
     *  @see com.tibco.tibems.tibemsd.security.jaas.LDAPGroupUserAuthentication
     */
    protected void onLdapAuthenticationSuccess(String userName, SearchResult userResult)
            throws LoginException, CommunicationException {}
    
    /**
     *  Invoked by this LoginModule after successful Cache authentication.
     *  @param userName the name of the user authenticated.
     *  @see com.tibco.tibems.tibemsd.security.jaas.LDAPGroupUserAuthentication
     */
    protected void onCacheAuthenticationSuccess(String username) 
            throws LoginException {}    
    
    
    /**
     * @see com.tibco.tibems.tibemsd.security.jaas.LDAPAuthentication#initializeModule(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
     */    
    @Override
    protected void initializeModule(Subject subject, CallbackHandler callbackHandler,
            Map<String, ?> sharedState, Map<String, ?> options)
    {
        super.initializeModule(subject, callbackHandler, sharedState, options);
        
        /*
         *  Add the default values, then override them with the contents
         *  of the options parameter.
         */
        HashMap<String, Object> defaults = new HashMap<String, Object>();
        defaults.put(LDAP_MANAGER_NAME, "CN=Manager");
        defaults.put(LDAP_MANAGER_PASSWORD, "password");
        defaults.put(LDAP_BASE_DN, "");
        defaults.put(LDAP_USER_ATTRIBUTE, "uid");
        defaults.put(LDAP_FILTER, "{0}={1}");
        defaults.put(LDAP_SCOPE, "onelevel");
        defaults.put(LDAP_RETRIES, "0");
        defaults.put(LDAP_RETRY_DELAY, "1000");
        defaults.put(LDAP_CACHE_ENABLED, "false");
        defaults.put(LDAP_CACHE_TTL, "60");
        
        jaasConfig.addOptions(defaults, options);
        
        managerName     = jaasConfig.getString(LDAP_MANAGER_NAME);
        managerPassword = jaasConfig.getString(LDAP_MANAGER_PASSWORD);
            
        userBaseDn      = jaasConfig.getString(LDAP_BASE_DN);
        userAttribute   = jaasConfig.getString(LDAP_USER_ATTRIBUTE);
        userFilter      = jaasConfig.getString(LDAP_FILTER);
        searchScope     = jaasConfig.getScope(LDAP_SCOPE);

        connectRetries  = jaasConfig.getInt(LDAP_RETRIES);
        connectDelay    = jaasConfig.getInt(LDAP_RETRY_DELAY);
        
        cacheTTL        = jaasConfig.getInt(LDAP_CACHE_TTL);
        cacheInstance   = jaasConfig.getString(LDAP_CACHE_INSTANCE);

        if (jaasConfig.getBoolean(LDAP_CACHE_ENABLED) == true)
        {
            debugLog("LDAP LoginModule: Cache enabled.\n");
            userCache = getUserCache();
        }
    }

    /**
     * @see javax.security.auth.spi.LoginModule#initialize(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map<String, ?> sharedState, Map<String, ?> options)
    {
        initializeModule(subject, callbackHandler, sharedState, options);
        printProperties();
    }
    
    /**
      * This implementation queries LDAP (and optionally a user cache) to 
      * authenticate a user.  A context with LDAP manager credentials is used
      * to first look up a user and retrieve the complete distinguished name
      * of the user's entry.  If the user exists, a separate LDAP context is
      * created, authenticating that user.  For performance reasons, the
      * manager context, once created, exists for the lifetime of the
      * LoginModule.
      * <p>
      * Should connectivity with the LDAP server break, multiple reconnection
      * attempts may be made based on the LoginModule parameters.
      * <p>
      * To increase performance, user caching may be enabled.  When enabled,
      * once a user has been authenticated via LDAP, that user is added into
      * the user cache allowing for a quick lookup.  If the user cache entry
      * is found to be expired, the user is authenticated with LDAP again and
      * the cache is updated.
      * <p>
      * If the login attempt is unsuccessful, a LoginException or
      * FailedLoginException is thrown.
      * 
      * @see javax.security.auth.spi.LoginModule#login()
      */
    @Override
    public boolean login() throws LoginException
    {
        NameCallback     nameCb     = new NameCallback(" ");
        PasswordCallback passwordCb = new PasswordCallback(" ", false);
        
        invokeCallbacks(nameCb, passwordCb);

        String userName = new String(nameCb.getName());
        String password = getAndClearPassword(passwordCb);

        if (password == null || password.compareTo("") == 0)
            throw buildLoginException(new LoginException(), 
                    "Password cannot be empty");

        if (userCache != null)
        {
            if (userCache.authenticateUser(userName, password))
            {
                onCacheAuthenticationSuccess(userName);
                return true;
            }
        }
    
        for (int i = 0; i <= connectRetries; i++)
        {
            try
            {
                SearchResult sr = authenticateUserWithLdap(userName, password);

                if (userCache != null)
                    userCache.put(userName, password);
                
                onLdapAuthenticationSuccess(userName, sr);

                return true;
            }
            catch (CommunicationException ce)
            {
                // if this is an SSL authentication issue, something is wrong
                // with the SSL setup and we exit immediately.  Retries will
                // not help.
                if (ce.getCause() instanceof  javax.net.ssl.SSLException)
                {
                    debugLog("LDAP LoginModule: SSL Error.\n");
                    throw buildLoginException(ce, "LDAP SSL Error");
                }
                
                if (i < connectRetries)
                {
                    loginDelay();
                    debugLog("LDAP LoginModule: Unable to connect.  Retrying.\n");
                }
                else
                {
                    debugLog("LDAP LoginModule: Communication failure.\n");
                    throw buildLoginException(ce, "LDAP Communication Failure");
                }
            }
        }

        return true;
    }
    
    /**
     * Authenticates a user with the LDAP server.  
     * <p>
     * This method uses a manager context to search for a user's DN based
     * on configured filters. If found, the user is then authenticated by
     * creating an LDAP context using the supplied credentials.  A missing
     * user or invalid password results in a FailedLoginException.
     * 
     * @param name the user.
     * @param password password for the user.
     * @throws CommunicationException LoginException FailedLoginException
     */
    protected SearchResult authenticateUserWithLdap(String userName, String password)
            throws CommunicationException, FailedLoginException, LoginException
    {
        LdapContext    context      = null;
        SearchResult   searchResult = null;
        
        try
        {
            debugLog("LDAP LoginModule: Authenticating user %s with LDAP.\n", userName);

            context = newManagerLdapContextInstance();
            
            SearchControls controls = new SearchControls();
            controls.setSearchScope(searchScope);
            
            debugLog("LDAP LoginModule: Searching for user under %s:\n", userBaseDn);
            debugLog("LDAP LoginModule:     filter = %s\n", userFilter);
            debugLog("LDAP LoginModule:     {0} = %s\n", userAttribute);
            debugLog("LDAP LoginModule:     {1} = %s\n", userName);
            
            NamingEnumeration<SearchResult> result = managerContext.search(
                userBaseDn, userFilter,
                new Object[] { userAttribute, userName },
                controls);
            
            if (!result.hasMore())
            {
                debugLog("LDAP LoginModule: Failed (User not found).\n");
                throw new FailedLoginException("No matching user found:  " + userName);
            }
            
            // There should be one record returned, however if there are more
            // only use the first one for our purposes here.
            searchResult = result.next();

            if (!authenticateUserDN(searchResult.getNameInNamespace(), password))
            {
                debugLog("LDAP LoginModule: Failed (Invalid User Password).\n");
                throw new FailedLoginException("Unable to authenticate " + userName);
            }

            debugLog("LDAP LoginModule: User %s Authenticated.\n", userName);
        }
        catch (CommunicationException ce)
        {
            /*
             *  This means that we lost connection to the LDAP server.  Throw
             *  this exception so the caller knows to retry.
             */
            debugLog("LDAP LoginModule: Failed (Communication Error).\n");
            closeManagerLdapContext();

            throw ce;
        }
        catch (NamingException e)
        {
            debugLog("LDAP LoginModule: Failed (LDAP Error).\n");
            throw buildLoginException(e, "LDAP error");
        }
        finally
        {
            /*
             * Close this instance of the manager context.
             */
            closeLdapContext(context);
        }
        
        return searchResult;
    }

    /**
     * Authenticates a user with LDAP by creating a context with the 
     * supplied distinguished name and password.
     * 
     * @param userDN the distinguished name of the user.
     * @param password password for the user.
     * 
     * @returns true if the user authenticated, false otherwise.
     * @throws CommunicationException
     */    
    private boolean authenticateUserDN(String userDN, String password)
            throws CommunicationException
    {
        try 
        {
            closeLdapContext(createLdapContext(userDN, password));
        }
        catch (CommunicationException ce)
        {
            throw ce;
        }
        catch (NamingException e)
        {
            return false;
        }
        
        return true;
    }
}
