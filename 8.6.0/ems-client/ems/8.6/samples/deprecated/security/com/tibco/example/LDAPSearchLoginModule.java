/*
 * Copyright (c) 2008-2016 TIBCO Software Inc.
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 *
 * $Id: LDAPSearchLoginModule.java 90180 2016-12-13 23:00:37Z $
 *
 */

package com.tibco.example;

import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/**
 * This class implements a vanilla LDAP login process. The module binds to the
 * LDAP server, searches for the user name supplied by the EMS client, and then
 * attempts to bind to the LDAP server again using the results of the search.
 * <p>
 * Most properties added in the JAAS configuration file will be passed directly
 * to JNDI when creating the LDAP context. This allows a lot of flexibility in
 * configuring the server connection. Only properties that begin with "ems_ldap."
 * are reserved for this module.
 * <p>
 * The module will hold one lookup context open, and use copies of that context
 * to do the actual searches.  This allows the LDAP implementation to reuse the
 * connection for subsequent searches.
 * <p>
 * When the option ems_ldap.group_attribute is specified, a search for the groups
 * the user is a member of will be attempted. If groups are found, they will be stored
 * in LDAPGroupInfo. A JACI plugin can then make use of that information.
 * <p>
 * To setup this example, specify the location of a JAAS Configuration file
 * through the EMS server configuration option "jaas_config_file".  Please
 * refer to the EMS user guide for further options concerning JAAS Login
 * Modules.
 * <p>
 * The JAAS configuration file entry for this login Module should have
 * a section similar to the following:
 * <pre>
 * EMSUserAuthentication {
 *  com.tibco.example.LDAPSearchLoginModule required
 *        ems_ldap.url="ldap://some_ldap_server:389"
 *        ems_ldap.binding_name="cn=Administrator,cn=users,dc=test,dc=some_company,dc=com"
 *        ems_ldap.binding_password="AdministratorPassword"
 *        ems_ldap.user_base_dn="cn=users,dc=test,dc=some_company,dc=com"
 *        ems_ldap.user_attribute="cn"
 *  };  
 * </pre>
 * <p>
 * The JAAS configuration file entry that also supports groups should have
 * a section similar to the following:
 * <pre>
 * EMSUserAuthentication {
 *   com.tibco.example.LDAPSearchLoginModule required
 *        ems_ldap.url="ldap://some_ldap_server:389"
 *        ems_ldap.binding_name="cn=Administrator,cn=users,dc=test,dc=some_company,dc=com"
 *        ems_ldap.binding_password="AdministratorPassword"
 *        ems_ldap.user_base_dn="cn=users,dc=test,dc=some_company,dc=com"
 *        ems_ldap.user_attribute="cn"
 *        ems_ldap.group_attribute="cn"
 *        ems_ldap.group_base_dn="cn=groups,dc=test,dc=some_company,dc=com"
 *  };
 * </pre>
 */
public class LDAPSearchLoginModule implements LoginModule {
    /**
     * The class name of the JNDI context factory. Defaults to
     * com.sun.jndi.ldap.LdapCtxFactory
     */
    public static final String               LDAP_PROVIDER         = "ems_ldap.provider";

    /**
     * The URL of the LDAP server.
     */
    public static final String               LDAP_URL              = "ems_ldap.url";

    /**
     * The name this module will use when binding to the LDAP server to perform
     * a search.
     */
    public static final String               LDAP_BINDING_NAME     = "ems_ldap.binding_name";

    /**
     * The password this module will use when binding to the LDAP server to
     * perform a search.
     */
    public static final String               LDAP_BINDING_PASSWORD = "ems_ldap.binding_password";

    /**
     * The base path for the LDAP search.
     */
    public static final String               LDAP_BASE_DN          = "ems_ldap.user_base_dn";

    /**
     * The attribute that will be compared to the user name for the search.
     */
    public static final String               LDAP_USER_ATTRIBUTE   = "ems_ldap.user_attribute";

    /**
     * The attribute of a Group that contains the group name.
     */
    public static final String               LDAP_GROUP_ATTRIBUTE   = "ems_ldap.group_attribute";

    /**
     * Base path for the LDAP group search.
     */
    public static final String               LDAP_GROUP_BASE_DN   = "ems_ldap.group_base_dn";

    /**
     * The attribute of an LDAP group object that specifies the distinguished 
     * names (DNs) of the members of the group.
     */
    public static final String               LDAP_GROUP_MEMBER_ATTRIBUTE = "ems_ldap.group_member_attribute";

    /**
     * The filter used in the search. By default, a filter is created using the
     * ems_ldap.group_member_attribute property. If a more complex filter is needed, use
     * this property to override the default. Any occurrence of '{1}' in the
     * search string will be replaced with the user name.
     */
    public static final String               LDAP_GROUP_FILTER    = "ems_ldap.group_filter";

    /**
     * The filter used in the search. By default, a filter is created using the
     * ems_ldap.group_member_attributee property. If a more complex filter is needed, 
     * use this property to override the default. Any occurrence of '{1}' in the
     * search string will be replaced with the user name.
     */
    public static final String               LDAP_FILTER           = "ems_ldap.filter";

    /**
     * The scope of the search. If set to "subtree" then the subtree LDAP scope
     * will be used. Any value other than "subtree" will cause a one level
     * search. Default is to use a one level search.
     */
    public static final String               LDAP_SCOPE            = "ems_ldap.scope";
    
    /**
     * If there is a communication failure with the LDAP server, the module will
     * retry the connection this many times.  The default value is 0, so to retries
     * will be attempted.
     */
    public static final String               LDAP_RETRIES          = "ems_ldap.retries";
    
    /**
     * The module will wait this number of milliseconds before retrying the connection
     * to the LDAP server. 
     */
    public static final String               LDAP_RETRY_DELAY      = "ems_ldap.retry_delay";
    
    /**
     * This is the keystore that will be used to establish trust for SSL connections.
     */
    public static final String               LDAP_CERTIFICATE      = "ems_ldap.certificate";

    /**
     * Enables caching of LDAP data.
     */
    public static final String               LDAP_CACHE_ENABLED    = "ems_ldap.cache_enabled";

    /**
     * Specifies the maximum time (in seconds) that cached LDAP data is retained before
     * it is refreshed.
     */
    public static final String               LDAP_CACHE_TTL        = "ems_ldap.cache_ttl";

    /**
     * Enables debugging output from the module, to aid in diagnosing configuration
     * problems.  WARNING: USE OF THE DEBUG FLAG MAY CREATE SECURITY VULNERABILITIES
     * by revealing information in the log file that would not otherwise be available.
     */
    public  static final String              LDAP_DEBUG            = "ems_ldap.debug";
    
    private static final String              LDAP_OPTION_PREFIX    = "ems_ldap.";

    private static final Object              initLock              = new Object();
    private static Hashtable<String, Object> ldapEnvironment       = null;

    private static final Object              globalCtxLock         = new Object();
    private static LdapContext               globalCtx             = null;

    private static String                    ldapBindingName;
    private static String                    ldapBindingPassword;

    private static String                    ldapUserBaseDn;
    private static String                    ldapUserFilter        = "{0}={1}";
    private static String                    ldapUserAttribute;

    private static String                    ldapGroupBaseDn;
    private static String                    ldapGroupFilter       = "{0}={1}";
    private static String                    ldapGroupAttribute;
    private static String                    ldapGroupMemberAttribute = "uniquemember";

    private static int                       ldapSearchScope       = SearchControls.ONELEVEL_SCOPE;

    private static int                       connectRetries        = 0;
    private static int                       connectDelay          = 1000;
    private static boolean                   cacheEnabled          = false;
    private static int                       cacheTTL              = 60;
    private static HashMap<String, Object>   usersCache            = null;
    private static cleanupCacheThread        cleanupCache          = null;

    private static boolean                   debug                 = false;

    private CallbackHandler                  callbackHandler;
    
    private class userData {
        private byte[] pwMessageDigest = null;
        private long   timestamp       = 0;
        
        private userData(byte[] msgDigest) {
            pwMessageDigest = msgDigest;
            timestamp       = System.currentTimeMillis();
        }
        
        private byte[] getPwMsgDigest() {
            return pwMessageDigest;
        }
        
        private long getTimestamp() {
            return timestamp;
        }
    }
    
    private class cleanupCacheThread extends Thread {        
        private static final long cacheCleanupInterval = 5 * 60 * 1000; // 5 minutes
        
        private cleanupCacheThread(String threadName) {
            super(threadName);
            start();            
        }
        
        public void run() {
            while(true)
            {
                try
                {
                    Thread.sleep(cacheCleanupInterval);
                    synchronized(usersCache)
                    {
                        for ( Iterator<Object> iter = usersCache.values().iterator(); iter.hasNext();)
                        {
                            userData user = (userData)iter.next();
                            if (user.getTimestamp() + (cacheTTL * 1000) < System.currentTimeMillis())
                            {
                                iter.remove();
                                if (debug)
                                    System.out.println("Removing expired entry.");
                            }
                        }
                    }                    
                }
                catch(InterruptedException e) {
                }
            }
        }        
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.security.auth.spi.LoginModule#abort()
     */
    public boolean abort() throws LoginException {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.security.auth.spi.LoginModule#commit()
     */
    public boolean commit() throws LoginException {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.security.auth.spi.LoginModule#initialize(javax.security.auth.Subject,
     *      javax.security.auth.callback.CallbackHandler, java.util.Map,
     *      java.util.Map)
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map<String, ?> sharedState, Map<String, ?> options) {
        this.callbackHandler = callbackHandler;

        // Set up ldapEnvironment just once
        synchronized (initLock) {
            if (ldapEnvironment == null) {
                Hashtable<String, Object> ldapEnvironmentTemp = new Hashtable<String, Object>();
                
                if(options.get(LDAP_DEBUG) != null)
                {
                    debug = "true".equalsIgnoreCase((String) options.get(LDAP_DEBUG)) 
                        || "enabled".equalsIgnoreCase((String) options.get(LDAP_DEBUG));
                    if (debug)
                        System.out.println("LDAPSearchLoginModule debug mode enabled.");
                }

                // Get basic lookup parameters set
                if (options.get(LDAP_PROVIDER) != null)
                    ldapEnvironmentTemp.put(Context.INITIAL_CONTEXT_FACTORY, options
                            .get(LDAP_PROVIDER));
                else
                    ldapEnvironmentTemp.put(Context.INITIAL_CONTEXT_FACTORY,
                            "com.sun.jndi.ldap.LdapCtxFactory");

                ldapEnvironmentTemp.put(Context.PROVIDER_URL, options.get(LDAP_URL));
                
                if (options.get(LDAP_CERTIFICATE) != null)
                {
                    if (debug)
                        System.out.println("LDAP_CERTIFICATE is set to: " + options.get(LDAP_CERTIFICATE));
                    String keystore = (String)options.get(LDAP_CERTIFICATE);
                    System.setProperty("javax.net.ssl.trustStore", keystore);
                }

                ldapBindingName = (String) options.get(LDAP_BINDING_NAME);
                ldapBindingPassword = (String) options.get(LDAP_BINDING_PASSWORD);

                ldapUserBaseDn = (String) options.get(LDAP_BASE_DN);
                ldapUserAttribute = (String) options.get(LDAP_USER_ATTRIBUTE);

                if (options.get(LDAP_FILTER) != null)
                    ldapUserFilter = (String) options.get(LDAP_FILTER);

                ldapGroupBaseDn = (String)options.get(LDAP_GROUP_BASE_DN);
                ldapGroupAttribute = (String)options.get(LDAP_GROUP_ATTRIBUTE);

                if (options.get(LDAP_GROUP_MEMBER_ATTRIBUTE) != null)
                    ldapGroupMemberAttribute = (String) options.get(LDAP_GROUP_MEMBER_ATTRIBUTE);

                if (options.get(LDAP_GROUP_FILTER) != null)
                    ldapGroupFilter = (String) options.get(LDAP_GROUP_FILTER);

                if ("subtree".equalsIgnoreCase((String) options.get(LDAP_SCOPE)))
                    ldapSearchScope = SearchControls.SUBTREE_SCOPE;

                if (options.get(LDAP_RETRIES) != null)
                    connectRetries = Integer.valueOf((String) options.get(LDAP_RETRIES));

                if (options.get(LDAP_RETRY_DELAY) != null)
                    connectDelay = Integer.valueOf((String) options.get(LDAP_RETRY_DELAY));

                if (options.get(LDAP_CACHE_TTL) != null)
                    cacheTTL = Integer.valueOf((String) options.get(LDAP_CACHE_TTL));

                if(options.get(LDAP_CACHE_ENABLED) != null)
                {
                    cacheEnabled = "true".equalsIgnoreCase((String) options.get(LDAP_CACHE_ENABLED)) 
                        || "enabled".equalsIgnoreCase((String) options.get(LDAP_CACHE_ENABLED));
                    if (debug)
                        if (cacheEnabled)
                            System.out.println("LDAPSearchLoginModule cache enabled.");
                }

                // Add any other JNDI properties that don't start with
                // "ems_ldap."
                for (Map.Entry<String, ?> e : options.entrySet()) {
                    if (!e.getKey().startsWith(LDAP_OPTION_PREFIX))
                        ldapEnvironmentTemp.put(e.getKey(), e.getValue());
                }

                ldapEnvironment = ldapEnvironmentTemp;
                
                if (usersCache == null)
                    usersCache = new HashMap<String, Object>();
    
                if (cleanupCache == null)
                    cleanupCache = new cleanupCacheThread("cleanupCache");
            }
        }
        // initialize() is always called at least once before any other methods
        // of the class are called. After this, ldapEnvironment is read-only, so
        // unsynchronized access is OK.
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.security.auth.spi.LoginModule#login()
     */
    public boolean login() throws LoginException {
        NameCallback namecallback = new NameCallback(" ");
        PasswordCallback passwordcallback = new PasswordCallback(" ", false);
        Callback[] callbacks = { namecallback, passwordcallback };

        // Get the user name and password from the server
        try {
            callbackHandler.handle(callbacks);
        } catch (java.io.IOException ioe) {
            LoginException le = new LoginException("IO error in callback");
            le.initCause(ioe);
            throw le;
        } catch (UnsupportedCallbackException uce) {
            LoginException le = new LoginException("Unsupported callback");
            le.initCause(uce);
            throw le;
        }

        // Check the user name and password
        int retriesLeft = connectRetries;

        String name = namecallback.getName();
        String password = new String(passwordcallback.getPassword());
        if (cacheEnabled)
        {
            if (authenticateFromCache(name, password))
                retriesLeft = -1;
        }

        while (retriesLeft >= 0)
        {
            LdapContext searchCtx = newLookupContext();
            try {
                    try {
                        name = namecallback.getName();
                        password = new String(passwordcallback.getPassword());

                        if (!searchAndBind(searchCtx, name, password))
                            throw new FailedLoginException("Login Failed");

                        retriesLeft = -1;
                        if (cacheEnabled)
                            updateUserCache(name, password);

                    } catch (CommunicationException e) {
                        LoginException logException = new LoginException("LDAP communication failure");
                        logException.initCause(e);
                        logException.printStackTrace(System.err);

                        retriesLeft--;
                        if (retriesLeft < 0)
                            throw logException;

                        try
                        {
                            if (connectDelay > 0)
                                Thread.sleep(connectDelay);
                        } catch (InterruptedException e1) {}
                        System.err.println("Reconnecting to LDAP server.");
                    }                

            } finally {
                try {
                    // log out search connection
                    searchCtx.close();
                } catch (NamingException e) {
                }
            }
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.security.auth.spi.LoginModule#logout()
     */
    public boolean logout() throws LoginException {
        return true;
    }

    private void searchGroups(LdapContext searchCtx, String name, String loginuserDN) 
    throws CommunicationException, NamingException
    {
        if (ldapGroupAttribute!= null && 
            loginuserDN != null && loginuserDN.length() > 0 &&
            name != null)
        {
            List<String> groups = new ArrayList<String>();

            // Search for groups the user belongs to in order to get their names 
            // Create the search controls   
            SearchControls groupsSearchCtls = new SearchControls();

            //Specify the search scope (reusing the user scope)
            groupsSearchCtls.setSearchScope(ldapSearchScope);

            groupsSearchCtls.setReturningAttributes( new String[]{ldapGroupAttribute});

            //Search for objects using the filter
            NamingEnumeration<SearchResult> groupsAnswer = searchCtx.search(
                    ldapGroupBaseDn, ldapGroupFilter,
                        new Object[] { ldapGroupMemberAttribute, loginuserDN } , groupsSearchCtls);

            if(debug)
                System.out.println("Set the membership for user: " + loginuserDN);

            
            //Loop through the search results
            while (groupsAnswer.hasMoreElements()) {
                SearchResult sr = groupsAnswer.next();
                String       groupName = sr.getAttributes().get(ldapGroupAttribute).get().toString();
                if (groupName != null && groupName.length() > 0)
                    groups.add(groupName);
            }

            LDAPGroupInfo.setUserMember(name,groups.toArray(new String[]{}));

            if(debug)
                LDAPGroupInfo.debugGroupOutput();
        }
    }

    /**
     * Search for the user, and bind to the LDAP server as that user.
     * 
     * @param searchCtx
     * @param name
     * @param password
     * @return true if search returned at least one result, and bind succeeded.
     * @throws CommunicationException
     */
    private boolean searchAndBind(LdapContext searchCtx, String name, String password)
            throws CommunicationException {
        try {
            SearchControls controls = new SearchControls();

            controls.setSearchScope(ldapSearchScope);

            // Do the search
            NamingEnumeration<SearchResult> result = searchCtx.search(ldapUserBaseDn, ldapUserFilter,
                    new Object[] { ldapUserAttribute, name }, controls);

            if (debug)
                System.out.println("Successfully searched for user.");

            // It should be just one record returned. Regardless, we'll take the
            // first one.
            if (result.hasMore()) {
                SearchResult sr = result.next();

                // Get the full name of the user
                String loginuserDN = sr.getNameInNamespace();

                // Now try to bind as the user
                Hashtable<String, Object> ldapProperties = cloneEnvironment();
                ldapProperties.put(Context.SECURITY_PRINCIPAL, loginuserDN);
                ldapProperties.put(Context.SECURITY_CREDENTIALS, password);

                LdapContext userLdapCtx = new InitialLdapContext(ldapProperties, null);
                userLdapCtx.close();

                searchGroups(searchCtx, name, loginuserDN);
                return true;
            } else {
                if (debug)
                    System.out.println("No matching user found.");
            }
        } catch (CommunicationException ce) {
            // CommunicationException is a subclass of NamingException, but we
            // still want to throw it from this method.
            discardGlobalContext();
            throw ce;
        } catch (NamingException e) {
            // Ignore other naming exceptions, and just return false.
            if (debug)
                e.printStackTrace(System.err);
        }

        return false;
    }

    /**
     * Creates a context appropriate for doing the search. Must return a valid
     * context or throw an exception. If the underlying LDAP implementation
     * supports connection pooling, this will usually return the same LDAP
     * connection that globaCtx represents, so it can be used over and over.
     * 
     * @return An LdapContext created by calling newInstance() on the global
     *         context.
     * @throws LoginException
     */
    private static LdapContext newLookupContext() throws LoginException {
        synchronized (globalCtxLock) {
            try {
                if (globalCtx == null)
                    globalCtx = newGlobalContext();

                return globalCtx.newInstance(null);

            } catch (NamingException e) {
                LoginException logException = new LoginException("Bind to LDAP server failed");
                logException.initCause(e);

                throw logException;
            }
        }
    }
    
    private static void discardGlobalContext() {
        synchronized (globalCtxLock) {
            if (debug)
                System.out.println("Disconnecting from LDAP server.");
            try
            {
                if (globalCtx != null)
                    globalCtx.close();
            } catch (NamingException e) {}
            globalCtx = null;
        }
    }

    /**
     * Creates a context appropriate for generating search contexts.  Must
     * return a valid context or throw an exception.
     * 
     * @return An LdapContext created with the bindings from the JAAS configuration file
     * @throws NamingException
     */
    private static LdapContext newGlobalContext() throws NamingException {
        Hashtable<String, Object> finalLDAPProperties = cloneEnvironment();

        if (ldapBindingName != null)
            finalLDAPProperties.put(Context.SECURITY_PRINCIPAL, ldapBindingName);
        if (ldapBindingPassword != null)
            finalLDAPProperties.put(Context.SECURITY_CREDENTIALS, ldapBindingPassword);

        if (debug)
            System.out.println("Connecting to LDAP server.");
        return new InitialLdapContext(finalLDAPProperties, null);
    }
    
    private static boolean authenticateFromCache(String name, String password) throws LoginException {
        boolean       match         = false;
        userData      user          = null;
        MessageDigest messageDigest = null;
        
        if (debug)
               System.out.println("Authenticating user " + name + " in cache. size = " + usersCache.size());

        synchronized(usersCache)
        {
            user = (userData) usersCache.get(name);
        }
        
        if (debug)
        {
            if (user != null)
                System.out.println("Found user " + name + " in cache.");
            else
                System.out.println("Did not find user " + name + " in cache.");
        }

        if (user != null)
        {
            if (user.getTimestamp() + (cacheTTL * 1000) < System.currentTimeMillis()) {
                if (debug)
                    System.out.println("Cache entry " + name + " has expired.");
                return false;
            }
            byte[] cachedPwDigest = user.getPwMsgDigest();
            byte[] pwBytes = password.getBytes();

            try {
                messageDigest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException nsae) {
                throw new LoginException("Failed to initialize messageDigest.");
            }

            messageDigest.update(pwBytes);
            byte[] pwDigest = messageDigest.digest();
                
            match = MessageDigest.isEqual(cachedPwDigest, pwDigest);
            if (debug)
            {
                if (match == true)
                    System.out.println("Cached password for user " + name + " matched.");
                else
                    System.out.println("Cached password for user " + name + " did not match.");
            }
        }
        return match;        
    }

    private void updateUserCache(String name, String password) throws LoginException {

        MessageDigest messageDigest = null;
 
        byte[] pwBytes = password.getBytes();
        
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException nsae) {
            throw new LoginException("Failed to initialize messageDigest.");
        }
        messageDigest.update(pwBytes);
        byte[] pwDigest = messageDigest.digest();

        userData user = new userData(pwDigest);
        synchronized(usersCache)
        {
            usersCache.put(name, user);
        }
        if (debug)
            System.out.println("Updated cache for user " + name + " cache size = " +usersCache.size());
    }

    @SuppressWarnings("unchecked")
    private static Hashtable<String, Object> cloneEnvironment() {
        return (Hashtable<String, Object>) ldapEnvironment.clone();
    }
}
