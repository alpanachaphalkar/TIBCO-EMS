/*
 * Copyright (c) 2013-2016 TIBCO Software Inc.
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 *
 * $Id: HostBasedAuthentication.java 90180 2016-12-13 23:00:37Z $
 *
 */
package com.tibco.tibems.tibemsd.security.jaas;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import com.tibco.tibems.tibemsd.security.ConnectionAttributes;

/**
 * This class checks the host name and/or IP address associated with the
 * client connection during authentication.
 * <p>
 * Connection information is provided through the subject, as exactly one
 * Principal implementing the ConnectionAttributes interface.
 * <p>
 * When enabled, the IP address of the incoming connection is evaluated
 * against a whitelist of IP addresses and/or IP masks.  If any of the
 * IP addresses or masks result in a match, IP authentication for the user is
 * considered successful.
 * <p>
 * When enabled, the host name of the incoming connection is compared
 * with the configured whitelist of patterns, which may be specific host names
 * or regular expressions.  See {@link #HBAUTH_ACCEPTED_HOSTNAMES} for more
 * information.  If the connection's host name evaluates true with any
 * of the patterns in the list, authentication is considered successful. 
 * Warning, comparing host names may result in a NIS or DNS lookup having a
 * performance impact.
 * <p>
 * The host name or IP mask must match for authentication success.
 * <p>
 * Particular types of connections can be identified for authentication.  If
 * a connection type is not specified, authentication is skipped.
 * See {@link #HBAUTH_CONNECTIONS} for more information.   
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
 *         com.tibco.tibems.tibemsd.security.jaas.HostBasedAuthentication required
 *         debug="true"
 *         tibems.hostbased.accepted_addresses="10.101.10.1, 10.100.0.0/16"
 *         tibems.hostbased.accepted_hostnames="'.tibco.com','production.*'" ; 
 * };
 * </pre>
 * </p>
 * @see com.tibco.tibems.tibemsd.security.ConnectionAttributes
 * @see javax.security.auth.spi.LoginModule
 */
public class HostBasedAuthentication implements LoginModule
{
    /*
     * ConnectionAttributes contain the connection information provided
     * by the EMS server.
     */
    protected ConnectionAttributes     connectionAttributes = null;
    
    protected JAASConfiguration        jaasConfig           = null;
    protected boolean                  debug                = false;
    
    /*
     * The whitelists of IP addresses and hostnames. 
     */
    private   String                   acceptedAddresses    = null;
    private   String                   acceptedHostnames    = null;

    /**
     * BigInteger values are used for creating masks, 128 bits for IPV6 and
     * 32 bits for IPV4.
     */
    static private final byte[] BITS_128 = {-1,-1,-1,-1,
                                            -1,-1,-1,-1,
                                            -1,-1,-1,-1,
                                            -1,-1,-1,-1};
    
    static private final byte[] BITS_32  = {-1,-1,-1,-1};  
    
    static private final BigInteger HIGH_128_INT = new BigInteger(BITS_128);
    static private final BigInteger HIGH_32_INT  = new BigInteger(BITS_32);
    
    /**
     * A comma delimited list of host names or patterns to compare with the 
     * incoming connection's host name, as known by the EMS server.
     * A match will result in successful authentication.  The host names
     * or patterns must be enclosed by quotes. 
     * <p>
     * Host names or domains can be explicitly specified, or any regular
     * expression working with {@link java.util.regex.Pattern} may be
     * used.  A domain may be used by beginning the string with a '.'.
     * Each host-name or pattern must be encapsulated by a single
     * quote and separated by a comma.  These will be compared with the 
     * hostname associated with the IP of the connecting EMS client.  This
     * could have a performance impact as a NIS or DNS lookup may be
     * performed. 
     * <p>
     * If this property is not set, host names are not checked during 
     * authentication.
     * <p>
     * e.g. tibems.hostbased.accepted_hostnames="'host1', '.tibco.com', '^.*_SERVER\\.tibco\\.com'
     */
    public static final String HBAUTH_ACCEPTED_HOSTNAMES = "tibems.hostbased.accepted_hostnames";
    
    /**
     * A comma delimited list of IP addresses or net/prefix masks to compare with the 
     * incoming connection's IP address.  Both IPV4 and IPV6 are supported. 
     * Any match will result in successful authentication.
     * <p>
     * If this property is not set, IP address checking is disabled.
     * <p>
     * e.g. tibems.hostbased.accepted_addresses="10.1.2.23, 10.100.0.0/16, 0:0:0:0:0:0:0:1"
     */    
    public static final String HBAUTH_ACCEPTED_ADDRESSES = "tibems.hostbased.accepted_addresses";

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
     * Converts a ConnectionInfo.Type to a string representation.
     * 
     * @param t connection type to convert
     * @return String representation of a connection type.
     */
    private String typeToString(ConnectionAttributes.Type t)
    {
        switch (t)
        {
            case CLIENT:
                return "CLIENT";
            case ADMIN:
                return "ADMIN";
            case FT:
                return "FT";
            case ROUTE:
                return "ROUTE";
        }
        
        return "UNKNOWN";
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

        acceptedAddresses = jaasConfig.getString(HBAUTH_ACCEPTED_ADDRESSES);
        acceptedHostnames = jaasConfig.getString(HBAUTH_ACCEPTED_HOSTNAMES);
        

        if (jaasConfig.debug)
        {
            jaasConfig.printProperties(System.out);
            jaasConfig.printUnusedProperties(System.out);
        }
        
        /*
         * There will be exactly one connection information object in the subject.
         */
        Set<ConnectionAttributes> ciSet = subject.getPrincipals(
            ConnectionAttributes.class);
        
        connectionAttributes = ciSet.iterator().next();
    }
    
    /**
     * Checks if this module is configured to perform authentication.
     * 
     * @return true if configured, false otherwise.
     */
    private boolean isConfigured()
    {
        if (acceptedHostnames == null &&  acceptedAddresses == null)
            return false;
        
        return true;
    }

    /**
     * Checks to see if the incoming connection's host name can be accepted.
     * <p>
     * This splits values in a string setup as:
     * "'value1','value2','value3','value4'" and evaluates the patterns
     * with the host name, returning true if an comparison succeeds.
     * 
     * @return true if the connection's host name can be accepted,
     * false otherwise.
     */
    private boolean canAcceptHostName(InetAddress ipAddress)
    {
        String  regex       = null;
        String  host        = null;
        boolean success     = false;
        String  hostPattern = null;
        
        if (acceptedHostnames == null)
            return false;
        
        host = ipAddress.getCanonicalHostName();
        
        /*
         * First trim off the leading and trailing single quotes.
         */
        regex = acceptedHostnames.replaceAll("^\'|\'[\\s]*$", "");
        
        /*
         * Accounting for whitespace, split the string into the various
         * expressions using the end quote, comma delimiter, and
         * beginning quote of the next pattern.
         */ 
        String [] patterns = regex.split("\'[\\s]*,[\\s]*\'");
        
        /*
         * Check each pattern for a match.  One match is a success.
         */
        for (int i = 0; i < patterns.length; i++)
        {
            debugLog("HostBased LoginModule: Evaluating \"%s\" with \"%s\":\n",
                host, patterns[i]);
            
            hostPattern = patterns[i].trim();
            
            if (hostPattern.startsWith("."))
                success = host.endsWith(hostPattern);
            
            /*
             * Always attempt a pattern match in case this is a pattern
             * that begins with "."
             */
            if (success == false)
            {
                try
                {
                    success = Pattern.matches(patterns[i], host);
                }
                catch (Exception ex)
                {
                    success = false;
                    debugLog("HostBased LoginModule: Invalid pattern '%s' (%s)\n", 
                        patterns[i], ex.getMessage());
                }
            }
            
            if (success)
            {
                debugLog("HostBased LoginModule: Matches (SUCCESS).\n");                
                return true;
            }

            debugLog("HostBased LoginModule: Does not match.\n");                
        }
        
        debugLog("HostBased LoginModule: No hostname matches found.\n");
        
        return false;
    }

    /**
     * Tests whether an IP address matches the string representation
     * of an IP address.
     * 
     * @param ipAddr the IP address to test
     * @param compareIP the string representation of an IPV4 or IPV6 address.
     * @return true if the IP addresses are equal, false otherwise.
     */
    private boolean matchesIP(InetAddress ipAddr, String compareIP)
    {
        byte[] ipBytes  = ipAddr.getAddress();
        byte[] ip2Bytes = null;
        
        try 
        {
            ip2Bytes = InetAddress.getByName(compareIP).getAddress();
        }
        catch (UnknownHostException ue)
        {
            debugLog("HostBased LoginModule: Invalid IP address '%s'.\n", compareIP);
            debugLog("HostBased LoginModule:    %s.\n", ue.getMessage());
            return false;
        }

        BigInteger ip  = new BigInteger(ipBytes);
        BigInteger ip2 = new BigInteger(ip2Bytes);
        
        return (ip.equals(ip2));
    }
    
    /**
     * Converts a BigInteger holding an IP address to a string representation
     * of the dotted IP address.
     * 
     * @param ipInteger the BigInteger holding the IP.
     * @param ipV4 - set to true if this is IPv4, false if IPv6.
     * @return a string representing the IP address.
     * @throws UnknownHostException
     */
    private static String toAddressString(BigInteger ipInteger, boolean ipV4)
    {
        byte[] biByteArray = ipInteger.toByteArray();
        byte[] addrArray = new byte[ipV4 ? 4 : 16];
        
        for (int j = 0; j < addrArray.length; j++)
            addrArray[j] = 0;
        
        for (int i = 0; i < biByteArray.length; i++)
            addrArray[i] = biByteArray[i];

        try
        {
            return InetAddress.getByAddress(addrArray).getHostAddress();
        }
        catch (UnknownHostException ex)
        {
            return "Error getting host address:  " + ex.getMessage();
        }
    }

    /**
     * This method determines if an IP address falls within the range
     * specified by a net/prefix mask (CIDR notation).
     * 
     * @param ipAddress the IP address to test
     * @param ipNet the IP portion of a CIDR notation
     * @param prefix the prefix portion of the CIDR notation
     * @return true if the IP address falls within range, false otherwise.
     * @throws UnknownHostException
     */
    private boolean matchesNetPrefix(InetAddress ipAddress, String ipNet,
        int prefix) throws UnknownHostException
    {
        byte[]     netBytes = null;
        byte[]     ipBytes  = null;
        BigInteger mask     = null;
        boolean    ipV4     = false;
        
        netBytes = InetAddress.getByName(ipNet).getAddress();
        ipBytes  = ipAddress.getAddress();
        
        if ((prefix <= 0) || (prefix > (netBytes.length*8)))
        {
            debugLog("HostBased LoginModule: Invalid prefix length of %d.\n", prefix);
            return false;
        }

        if (netBytes.length != ipBytes.length)
            return false;
        
        ipV4 = (netBytes.length == 4);
        
        if (ipV4)
            mask = HIGH_32_INT.shiftLeft(32-prefix);
        else
            mask = HIGH_128_INT.shiftLeft(128-prefix);
        
        BigInteger ip   = new BigInteger(ipBytes);
        BigInteger net  = new BigInteger(netBytes);

        /*
         * When debugging, providing the range is useful.
         */
        if (jaasConfig.debug)
        {
            BigInteger low  = net.and(mask);
            BigInteger high = low.add(mask.not());

            debugLog("HostBased LoginModule: Valid Range is from %s to %s.\n", 
                toAddressString(low, ipV4), toAddressString(high, ipV4));
        }
        
        return net.and(mask).equals(ip.and(mask));
    }
    
    /**
     * Determines if an IP address in string form is a mask.
     * 
     * @param ipAddress
     * 
     * @return true if the IP is a mask, false otherwise.
     */
    static boolean isMask(String ipAddress)
    {
        if (ipAddress == null)
            return false;
        
        return ipAddress.contains("/");
    }
    
    /**
     * A convenience method that breaks a mask into parts, delimited
     * by a "/".
     * 
     * @param  mask a string IP mask in a net/prefix form.
     * @return a string array where the first element is an IP,
     *         and the second is a the prefix.
     *         Null on error.
     */
    private String[] getMaskParts(String mask)
    {
        String[] rv = null;
        
        if (mask.contains("/") == false)
        {
            rv = new String[1];
            rv[0] = mask;
        }
        else
        {
            rv = mask.split("/");
            if (rv.length != 2)
                rv = null;
        }
        
        return rv;
    }
    
    /**
     * Determines if an IP address matches a given mask.
     * 
     * @param ipAddress the address to check
     * @param mask a mask to compare the address, in mask, net/mask,
     *        or IP/prefix form.
     * @return true is the address falls in range of the mask, false
     *         otherwise.
     * @throws UnknownHostException
     */
    private boolean matchesMask(InetAddress ipAddress, String mask)
    {
        String[] parts = getMaskParts(mask);
        if (parts == null || parts.length == 1)
        {
            debugLog("HostBased LoginModule: Invalid Mask.");
            return false;
        }

        try
        {
            return matchesNetPrefix(ipAddress, parts[0],
                Integer.parseInt(parts[1]));
        }
        catch (NumberFormatException nfex)
        {
            debugLog("HostBased LoginModule: Invalid mask prefix %s.\n", mask);
            debugLog("HostBased LoginModule: Error:  %s\n", nfex.getMessage());
        }
        catch (UnknownHostException uhex)
        {
            debugLog("HostBased LoginModule: Invalid mask %s.\n", mask);
            debugLog("HostBased LoginModule: Error:  %s\n", uhex.getMessage());
        }
           
        return false;
    }
    
    /**
     * Determines if the connection's IP address can be accepted.
     * 
     * @return true if the IP address can be accepted.
     * @throws UnknownHostException 
     */
    private boolean canAcceptIP(InetAddress ipAddress)
    {
        boolean success = false;
        
        /*
         * No addresses are considered a failure here; parameters that are
         * not configured are checked by the caller.
         */
        if (acceptedAddresses == null)
            return false;
        
        String[] ipArray = acceptedAddresses.split("[\\s]*,[\\s]*");
        for (int i = 0; i < ipArray.length; i++)
        {
            debugLog("HostBased LoginModule: Comparing with IP \"%s\"\n",
                ipArray[i]);
            
            if (isMask(ipArray[i]))
                success = matchesMask(ipAddress, ipArray[i]);
            else
                success = matchesIP(ipAddress, ipArray[i]);
            
            debugLog("HostBased LoginModule: IP %s.\n",
                    success ? "matches" : "does not match");
            
            if (success)
                return true;
        }
        
        debugLog("HostBased LoginModule: No address matches found.\n");
        
        return false;
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
        debugLog("HostBased LoginModule: Authenticating connection.\n"); 
        debugLog("  IP:   %s\n", connectionAttributes.getInetAddress().getHostAddress());
        debugLog("  User: %s\n", connectionAttributes.getName());
        debugLog("  Type: %s\n", typeToString(connectionAttributes.getType()));
        debugLog("  ssl:  %s\n", connectionAttributes.isSSL() ? "true" : "false");
        debugLog("  x509: %s\n", (connectionAttributes.getX509Data() == null ?
                                             "null" : "present"));
        
        debugLog("HostBased LoginModule: IP comparison %s been configured.\n",
                (acceptedAddresses != null) ? "has" : "has NOT");
        debugLog("HostBased LoginModule: Hostname evaluation %s been configured.\n",
                (acceptedHostnames != null) ? "has" : "has NOT");
        
        /*
         * If we have an invalid configuration, do not allow the
         * login to continue - it is a user error that must be addressed.
         */
        if (isConfigured() == false)
        {
            debugLog("HostBased LoginModule: Invalid configuration; authorization failure.\n");
            debugLog("HostBased LoginModule: At least one 'accepted' parameter must be set.\n");
            return false;
        }
        
        InetAddress ipAddr = connectionAttributes.getInetAddress();

        /* 
         * The IP addresses are checked first.  A success there will prevent a 
         * host name check and avoid an unnecessary NIS/DNS Lookup.
         */
        if (canAcceptIP(ipAddr) || canAcceptHostName(ipAddr))
        {
            debugLog("HostBased LoginModule: Authentication success.\n");
            return true;
        }
        else
        {
            debugLog("HostBased LoginModule: Authentication failure.\n");
            throw new FailedLoginException("Connection authentication failure.");
        }
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
