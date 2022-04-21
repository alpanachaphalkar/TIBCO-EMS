/*
 * Copyright (c) 2013-2019 TIBCO Software Inc.
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 *
 * $Id: LDAPSimpleAuthentication.java 106731 2019-01-10 21:19:38Z $
 *
 */
package com.tibco.tibems.tibemsd.security.jaas;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.CommunicationException;
import javax.naming.LimitExceededException;
import javax.naming.InsufficientResourcesException;
import javax.naming.ServiceUnavailableException;
import javax.naming.Context;
import javax.naming.NamingException;
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
 * This class implements a very basic form of LDAP authentication for EMS. It
 * will validate all connections (be they users, routes, etc.) by
 * authenticating to the LDAP server as that user.  Regarding applications,
 * the name and password are those passed into EMS when an application
 * creates a connection.
 * <p>
 * The user name must be in the form of a distinguished name, unless a
 * user name pattern is supplied through the tibems.ldap.user_pattern.
 * parameter.  When a user pattern is supplied, the DN used for the lookup
 * will be this pattern string, with '%u' replaced with the name of the user.
 * e.g. DN=user=%u;ou=People
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
 * To enable this module, specify the location of a JAAS Configuration file
 * through the EMS server configuration option "jaas_config_file".  Please
 * refer to the EMS user guide for further options concerning JAAS Login
 * Modules.
 * <p>
 * The JAAS configuration file entry for this login module should have
 * a section similar to the following:
 * <pre>
 * EMSUserAuthentication {
 *      com.tibco.tibems.tibemsd.security.jaas.LDAPSimpleAuthentication required
 *      debug=false
 *      tibems.ldap.url="ldap://ldapserver:389"
 *      tibems.ldap.user_pattern="CN=%u"
 * }; 
 * </pre>
 * </p>
 * @see javax.security.auth.spi.LoginModule
 */
public class LDAPSimpleAuthentication implements LoginModule
{
    protected CallbackHandler   jaasCallbackHandler = null;

    protected LDAPConfiguration jaasConfig          = null;
    
    protected Pattern           userPattern         = null; 
    protected String            userNamePrefix      = null;
    protected String            userNamePostfix     = null;
    
    protected boolean           debug               = false;
    
    /**
     * The URL of the LDAP server.  The default is "ldap://localhost:389".
     */
    public static final String LDAP_URL = "tibems.ldap.url";
    
    /**
     * This is the keystore that will be used for SSL connections.
     */
    public static final String LDAP_CERTIFICATE = "tibems.ldap.truststore";

    /**
     * The user pattern to use with simple LDAP authentication, for example
     * "uid=%u;ou=People".  The default is "CN=%u".
     */
    public static final String LDAP_USER_PATTERN = "tibems.ldap.user_pattern";

    public static final String LDAP_OPERATION_TIMEOUT = "tibems.ldap.operation_timeout";

    /**
     * Writes to System.out when debug is enabled.
     * 
     * @param format a printf format string.
     * @param args the arguments referenced in the format string. 
     * 
     * @throws LoginException
     */
    protected void debugLog(String format, Object... args)
    {
        if (debug)
            System.out.printf(format, args);
    }
    
    /**
     * Convenience method that creates a LoginException.
     * 
     * @param baseException the cause of the login exception.
     * @param msg the detail message. 
     * 
     * @throws LoginException
     */
    protected static LoginException buildLoginException(Exception baseException, String msg)
    {
        LoginException ex = new LoginException(msg);
        
        if (baseException != null)
            ex.initCause(baseException);
    
        return ex;
    }

    /**
     * Convenience method that invokes a NameCallback and PasswordCallback,
     * converting exceptions into LoginExceptions.  The caller must
     * ensure thread safety.
     * 
     * @param nameCb the name callback of the JAAS LoginModule
     * @param passwordCb the password callback of the JAAS LoginModule
     * 
     * @throws LoginException
     */
    protected void invokeCallbacks(NameCallback nameCb, PasswordCallback passwordCb)
        throws LoginException
    {
        Callback[] callbacks = new Callback[2];
        callbacks[0]         = nameCb;
        callbacks[1]         = passwordCb;

        try
        {
            jaasCallbackHandler.handle(callbacks);
        }
        catch (java.io.IOException ioe)
        {
            throw buildLoginException(ioe, ioe.toString());
        }
        catch (UnsupportedCallbackException uce)
        {
            throw buildLoginException(uce, 
                    "Error: " + uce.getCallback().toString()
                    + " unable to garner authentication information from the user");
        }
    }
    
    /**
     * A convenience method to retrieve the password from the password callback.
     * 
     * @param passwordCb - the login module password callback
     * @return the password as a string, or null if not set
     */
    static String getAndClearPassword(PasswordCallback passwordCb)
    {
        if (passwordCb == null)
            return null;
        
        char[] password = passwordCb.getPassword();
        
        passwordCb.clearPassword();
        
        if (password != null)
            return new String(password);
        
        return null;
    }
    
    /**
     * Creates an LdapContext with the supplied credentials and
     * LoginModule configuration.  If there is a communication failure,
     * attempts will be made with backup servers if configured.
     * 
     * @param userDN the full DN of the user name when creating the LDAP connection.
     * @param password the password of the user
     * 
     * @return An LdapContext created with the bindings from the JAAS configuration file
     * @throws NamingException
     */
    protected LdapContext createLdapContext(String userDN, String password) throws NamingException
    {
        int count;
        
        count = jaasConfig.getInitialConnectRetryCount();
        while(count > 0)
        {
            Hashtable<String, Object> props = jaasConfig.getLdapProperties();
            
            if (userDN != null)
            {
                props.put(Context.SECURITY_PRINCIPAL, userDN);
                if (password != null)
                    props.put(Context.SECURITY_CREDENTIALS, password);
            }

            try
            {
                debugLog("LDAP LoginModule: Connecting to ldap server at %s as %s.\n", 
                        jaasConfig.getCurrentLdapUrl(), userDN);
                
                return new InitialLdapContext(props, null);
            }
            catch (NamingException ne)
            {
                debugLog("LDAP LoginModule: Connect attempt to ldap server at %s "+ 
                         "as %s failed due to exception %s\n",
                         jaasConfig.getCurrentLdapUrl(),
                         userDN,
                         ne);

                jaasConfig.updateOnFailure();

                count--;

                if (count == 0)
                    throw ne;
                else
                    debugLog("Failed to connect.  Retrying.\n");
            }
        }
        
        return null;
    }

    /**
     * Convenience method that closes an LDAP context, ignoring errors.
     * 
     * @param context An LdapContext to be closed.
     */    
    protected void closeLdapContext(LdapContext context)
    {
        if (context == null)
            return;
        
        try
        {
            context.close();
        }
        catch (NamingException ne) {}
    }

    /**
     * Initializes this LoginModule.
     * 
     * @param subject the Subject to be authenticated. 
     * @param callbackHandler a CallbackHandler for communicating with the
     *        end user (prompting for usernames and passwords, for example). 
     * @param sharedState state shared with other configured LoginModules.
     * @param options options specified in the login Configuration for this
     *         particular LoginModule.
     */
    protected void initializeModule(Subject subject, CallbackHandler callbackHandler,
            Map<String, ?> sharedState, Map<String, ?> options)
    {
        jaasCallbackHandler = callbackHandler;
        
        jaasConfig = new LDAPConfiguration();
        
        HashMap<String, Object> defaults = new HashMap<String, Object>();
        defaults.put(LDAP_URL, "ldap://localhost:389");
        defaults.put(LDAP_USER_PATTERN, "CN=%u");
        
        jaasConfig.addOptions(defaults, options);
        
        setNameComponents();
        
        debug = jaasConfig.debug;
    }
    
    /**
     * Prints configuration properties/options and unused properties.
     */
    void printProperties()
    {
        if (jaasConfig.debug)
        {
            jaasConfig.printProperties(System.out);
            jaasConfig.printUnusedProperties(System.out);
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
     * A convenience method to set the user pattern components
     * used in generating a full DN of a user entry.
     */
    private void setNameComponents()
    {
        String pattern = jaasConfig.getString(LDAP_USER_PATTERN);
        if (pattern == null)
            return;
        
        if (userPattern == null)
            userPattern = Pattern.compile("(.*)%u(.*)");
        
        Matcher m = userPattern.matcher(pattern);
        if (m.matches())
        {
            if (m.groupCount() > 0)
                userNamePrefix  = m.group(1);
            
            if (m.groupCount() > 1)
                userNamePostfix = m.group(2);
        }
        else
        {
           System.err.printf("Invalid %s pattern: %s",
                   LDAP_USER_PATTERN, pattern);
        }
    }
    
    /**
     * This method generates a full distinguished name based on the user
     * pattern passed into the options.
     * 
     * @param name the plain user name of a user.
     * @return a full user DN based on the com.tibco.tibems.tibemsd.security.user_pattern
     */  
    String generateUserDN(String name)
    {
        if (name == null)
            return null;
        
         return (userNamePrefix != null ? userNamePrefix : "" ) + name +
               (userNamePostfix != null ? userNamePostfix : "" );
    }
    
   /**
     * This method simply creates a local LDAP context, binding to the
     * LDAP server as a particular user with credentials.  If successful,
     * true is returned. If not, a LoginException or FailedLoginException
     * is thrown.
     * 
     * @see javax.security.auth.spi.LoginModule#login()
     */
    @Override
    public boolean login() throws LoginException {
        
        LdapContext               ldapCtx        = null;
        NameCallback              nameCb         = new NameCallback(" ");
        PasswordCallback          passwordCb     = new PasswordCallback(" ", false);
        
        invokeCallbacks(nameCb, passwordCb);

        try
        {
            String password = getAndClearPassword(passwordCb);

            if (password == null || password.compareTo("") == 0)
                throw buildLoginException(new LoginException(),
                        "Password cannot be empty");

            ldapCtx = createLdapContext(generateUserDN(nameCb.getName()),
                        password);
        }
        catch (NamingException e)
        {
            debugLog("LDAP LoginModule: Failed to authenticate user: %s\n",
                nameCb.getName());
            
            if (e instanceof CommunicationException)
                throw buildLoginException(e, "LDAP Communication Error.");

            FailedLoginException fe = new FailedLoginException(
                "Authentication failed.");
            fe.initCause(e);

            throw fe;
        }
        finally
        {
            closeLdapContext(ldapCtx);
        }
        
        debugLog("LDAP LoginModule: User %s Authenticated.\n",
                nameCb.getName());

        return true;
    }

    /**
     * This method is not used by EMS.
     * @see javax.security.auth.spi.LoginModule#commit()
     */
    @Override
    public boolean commit() throws LoginException {
        return true;
    }

    /**
     * This method is not used by EMS.
     * @see javax.security.auth.spi.LoginModule#abort()
     */
    @Override
    public boolean abort() throws LoginException {
        return true;
    }

    /**
     * For this module, no actions are required in logout.
     * @see javax.security.auth.spi.LoginModule#logout()
     */
    @Override
    public boolean logout() throws LoginException {
        return true;
    }

}
