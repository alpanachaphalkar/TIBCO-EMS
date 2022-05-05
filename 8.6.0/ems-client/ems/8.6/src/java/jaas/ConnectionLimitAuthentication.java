/*
 * Copyright (c) 2015-2019 TIBCO Software Inc.
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 *
 * $Id: ConnectionLimitAuthentication.java 106732 2019-01-10 22:05:58Z $
 *
 */


package com.tibco.tibems.tibemsd.security.jaas;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.spi.LoginModule;

import com.tibco.tibems.tibemsd.security.ConnectionAttributes;
/**
 * This class limits the number of active connections per connection group
 * as identified by matching IP address, hostname, or LDAPID, 
 * further referred to as key.
 * <p>
 * Connection information is provided through the subject, as exactly one
 * Principal implementing the ConnectionAttributes interface.
 * <p>
 * The type of key is specified through a jaas_config_file parameter, see
 * {@link #CLAUTH_CONN_LIMIT}. The connection limit is similar, see
 * {@link #CLAUTH_TYPE}.
 * <p>
 * Any login requests for a key that is at the connection limit will be
 * refused until logout requests for that key have cleared space.
 * <p>
 * If the connection limit is not provided, the limit defaults to 1000
 * connections per key.
 * <p>
 * If the group type is not provided, the key defaults to hostname.
 * <p>
 * To enable this module, specify the location of a JAAS Configuration file
 * through the EMS server configuration option "jaas_config_file".  Please
 * refer to the EMS user guide for further options concerning JAAS Login
 * Modules.
 * <p>
 * NOTE: When stacking this module with other JAAS modules, it is imperative
 * to provide this module as the last one used, and to provide the 'requisite'
 * flag instead of the 'required' flag. If not, then certain groups could
 * be eventually refused all connections until the server is restarted.
 * <p>
 * The JAAS configuration file entry for this login module should have
 * a section similar to the following:
 * <pre>
 * EMSUserAuthentication {
 *  com.tibco.tibems.tibemsd.security.jaas.ConnectionLimitAuthentication required
 *  debug="true"
 *  tibems.connectionlimit.max_connections="5"
 *  tibems.connectionlimit.type="HOSTNAME" ;
 * };
 * </pre>
 * </p>
 * @see com.tibco.tibems.tibemsd.security.ConnectionAttributes
 * @see javax.security.auth.spi.LoginModule
 */
public class ConnectionLimitAuthentication implements LoginModule
{
    protected ConnectionMap        tracker              = null;
    protected int                  connLimit            = 0;
    
    protected enum LimitType 
    {
        IP,
        HOSTNAME,
        LDAPID,
        LDAPID_AT_HOSTNAME
    };

    protected LimitType            limiter;
    
    public static final String     CLAUTH_CONN_LIMIT    = 
                                    "tibems.connectionlimit.max_connections";
    
    public static final String     CLAUTH_TYPE          = 
                                    "tibems.connectionlimit.type";
    
    protected CallbackHandler      jaasCallbackHandler  = null;
    
    /*
     * ConnectionAttributes contain the connection information provided
     * by the EMS server.
     */
    protected ConnectionAttributes connectionAttributes = null;
    
    protected JAASConfiguration    jaasConfig           = null;
    protected static boolean       debug                = false;
   
    /**
     * This class provides a single object by which to track logins and
     * logouts in the ConnectionLimitAuthentication LoginModule.
     * <p>
     * All calls to this module use the same instance, and all methods are
     * synchronized to allow safe tracking of concurrent connections.
     * <p>
     * Each login and logout request comes with a key that groups connections.
     * <p>
     * Each time a login or logout occurs, this increments or decrements the
     * count of active connections for the given key
     * <p>
     * When instantiated, the module is given an integer limit on the number
     * of active connections per key. After this limit is reached for a given
     * key, any further login requests are refused until another logout occurs
     * for that key.
     * </p>
     */
    protected static class ConnectionMap
    {
        private static ConnectionMap                     instance = null;
        private static HashMap<String, Integer>          userConnectionMap;
        private static int                               maxConnectionsPerUser;

        protected ConnectionMap(int maxConnections)
        {
            userConnectionMap = new HashMap<String, Integer>();
            maxConnectionsPerUser = maxConnections;
        }

        public static synchronized ConnectionMap getInstance(int maxConnections)
        {
            if (instance == null)
            {
                instance = new ConnectionMap(maxConnections);
            }
            return instance;
        }

        public synchronized void addConnection(String key) throws FailedLoginException
        {
            int count = 0;

            if (userConnectionMap.containsKey(key))
            {
                count = userConnectionMap.get(key);
            }

            count++;

            if (count > maxConnectionsPerUser)
            {
                FailedLoginException fe = new FailedLoginException(
                        "Connection limit reached.");

                throw fe;
            }

            userConnectionMap.put(key,count);
        }

        public synchronized void removeConnection(String key) throws FailedLoginException
        {
            int count = 0;

            if (userConnectionMap.containsKey(key))
            {
                count = userConnectionMap.get(key);
            }

            count--;

            if (count < 0)
            {
                FailedLoginException fe = new FailedLoginException(
                        "No connections to remove.");

                throw fe;
            }
            else if (count == 0)
            {
                userConnectionMap.remove(key);
            }
            else
            {
                userConnectionMap.put(key,count);
            }
        }
    }


    protected static LimitType getLimiter(String value)
    {
        if (value == null)
        {
            debugLog("ConnectionLimit LoginModule: No limit type specified. Defaulting to HOSTNAME.\n");
            value = "HOSTNAME";
        }

        switch (value.toUpperCase())
        {
            case "IP":
                return LimitType.IP;
            case "HOSTNAME":
                return LimitType.HOSTNAME;
            case "LDAPID":
                return LimitType.LDAPID;
            case "LDAPID@HOSTNAME":
                return LimitType.LDAPID_AT_HOSTNAME;
            default:
                debugLog("ConnectionLimit LoginModule: Limit type unrecognized. Defaulting to HOSTNAME.\n");
                return LimitType.HOSTNAME;
        }    
    }
   
    protected static int getConnLimit(String value)
    {
        int limit = 1000;

        try
        {
            limit = Integer.parseInt(value);
        }
        catch (NumberFormatException nfe)
        {
            debugLog("ConnectionLimit LoginModule: Maximum number of connections misconfigured.\n"
                    + "Defaulting to 1000.\n");
        }
        return limit;
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
     * Convenience method that invokes just NameCallback (for logout),
     * converting exceptions into LoginExceptions.  The caller must
     * ensure thread safety.
     * 
     * @param nameCb the name callback of the JAAS LoginModule
     * 
     * @throws LoginException
     */
    protected void invokeNameCallback(NameCallback nameCb)
        throws LoginException
    {
        Callback[] callbacks = new Callback[1];
        callbacks[0]         = nameCb;

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
     * Writes to System.out when debug is enabled.
     * 
     * @param format a printf format string.
     * @param args the arguments referenced in the format string. 
     * 
     * @throws LoginException
     */
    protected static void debugLog(String format, Object... args)
    {
        if (debug)
            System.out.printf(format, args);
    }
    
    /**
     * @see javax.security.auth.spi.LoginModule#initialize(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map<String, ?> sharedState, Map<String, ?> options)
    {
        jaasConfig = new JAASConfiguration();
        
        HashMap<String, Object> defaults = new HashMap<String, Object>();
        
        jaasConfig.addOptions(defaults, options);
        
        debug = jaasConfig.debug;

        /*
         * There will be exactly one connection information object in the subject.
         */
        Set<ConnectionAttributes> ciSet = subject.getPrincipals(
            ConnectionAttributes.class);
        
        connectionAttributes = ciSet.iterator().next();
       
        connLimit = getConnLimit(jaasConfig.getString(CLAUTH_CONN_LIMIT));
        
        tracker = ConnectionMap.getInstance(connLimit);
        
        limiter = getLimiter(jaasConfig.getString(CLAUTH_TYPE));
        
        jaasCallbackHandler = callbackHandler;

        if (jaasConfig.debug)
        {
            jaasConfig.printProperties(System.out);
            jaasConfig.printUnusedProperties(System.out);
        }
        
        debugLog("ConnectionLimit LoginModule: Initialized\n");
    }
    
    /**
     * This method authenticates an EMS connection with the configured
     * parameters.  If successful, true is returned, If not, a
     * LoginException or FailedLoginException is thrown.
     * 
     * @see javax.security.auth.spi.LoginModule#login()
     */
    @Override
    public boolean login() throws LoginException
    {
        String key = null;
        try
        {
            key = getConnectionKey(limiter);
            debugLog("ConnectionLimit LoginModule: Logging in %s\n", key);
            tracker.addConnection(key);
            debugLog("ConnectionLimit LoginModule: Login successful for %s\n", key);
        }
        catch (FailedLoginException fe)
        {
            debugLog("ConnectionLimit LoginModule: Connection Limit of %d has "
                    + "been reached for %s : Refusing connection\n",
                     connLimit,
                     key);
            throw fe;
        }
        return true;
    }
    
    /**
     * This method is not used by EMS.
     * @see javax.security.auth.spi.LoginModule#commit()
     */
    @Override
    public boolean commit() throws LoginException 
    {
        return true;
    }

    /**
     * This method is not used by EMS.
     * @see javax.security.auth.spi.LoginModule#abort()
     */
    @Override
    public boolean abort() throws LoginException 
    {
        return true;
    }
    
    public String getConnectionKey(LimitType lim) throws LoginException
    {
        NameCallback nameCb = null;
        InetAddress ipAddr = null;
        switch (lim)
        {
            case IP:
                ipAddr = connectionAttributes.getInetAddress();
                return ipAddr.getHostAddress();
            case HOSTNAME:
                ipAddr = connectionAttributes.getInetAddress();
                return ipAddr.getCanonicalHostName();
            case LDAPID:
                nameCb = new NameCallback(" ");
                invokeNameCallback(nameCb);
                return nameCb.getName();
            case LDAPID_AT_HOSTNAME:
                nameCb = new NameCallback(" ");
                invokeNameCallback(nameCb);
                String ldapId = nameCb.getName();
                ipAddr = connectionAttributes.getInetAddress();
                return ldapId+"@"+ipAddr.getCanonicalHostName();
            default:
                ipAddr = connectionAttributes.getInetAddress();
                return ipAddr.getCanonicalHostName();
        }
    }
    
    /**
     * @see javax.security.auth.spi.LoginModule#logout()
     */
    @Override
    public boolean logout() throws LoginException 
    {
        String key;
        try
        {
            key = getConnectionKey(limiter);
            debugLog("ConnectionLimit LoginModule: Logging out %s\n", key);
            tracker.removeConnection(key);
            debugLog("ConnectionLimit LoginModule: Logout successful for %s\n", key);
        }
        catch (FailedLoginException e)
        {
            debugLog("ConnectionLimit LoginModule: No connection found: Bad logout\n");
            throw e;
        }
        return true;
    }
}

