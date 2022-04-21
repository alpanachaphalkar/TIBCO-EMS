/*
 * Copyright (c) 2013-2019 TIBCO Software Inc.
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 *
 * $Id: LDAPConfiguration.java 106731 2019-01-10 21:19:38Z $
 *
 */
package com.tibco.tibems.tibemsd.security.jaas;

import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.directory.SearchControls;

/**
 * This class encapsulates the configuration for the EMS LDAP JAAS plug-ins
 * and provides convenience methods to retrieve configured parameters.
 * <p>
 * Any properties that do not begin with "tibems" are ignored and passed
 * directly into the LDAP related properties.  Note, some SSL/TLS properties
 * may be required to be set in the environment.  Those may be set through the
 * "jvm_option" server configuration parameter in the tibemsd.conf file.
 * <p>
 * Once created, the properties are read-only, and thus thread safe.  The only
 * threading issue is retrieving the LDAP properties, so a copy is made. 
 *
 */
public class LDAPConfiguration extends JAASConfiguration
{
    private static final long serialVersionUID = -8202655620710910724L;

    /**
     * These are JNDI specific properties, saved off for quickly passing into the
     * JNDI layer.
     */
    private Hashtable<String, Object> ldapProperties = new Hashtable<String, Object>();
    
    /**
     *  The LDAP server(s) configured.  These may be include backup servers.
     */
    static LdapServerSet ldapServers = null;
    
    /**
     * This class represents a set of LDAP servers, the initial server
     * and backups as defined by the configuration.
     * <p>
     * The server configuration can be defined as a single LDAP server URL,
     * or a series of LDAP URLs representing the primary and backups.
     * <p>
     * To configure a backup, provide a list of URLs separated by "," for example:
     * "ldap://localhost:389,ldap://localhost:489".  The servers will be
     * attempted in order should they fail.  Any number of backup
     * servers may be specified.
     */
    private static class LdapServerSet
    {
        int      index   = 0;
        String[] urlList = null;
        
        // do not allow an empty constructor
        @SuppressWarnings("unused")
        private LdapServerSet() {}
            
        /**
         * Constructor. Splits the LDAP URLs into an array.
         */
        LdapServerSet(String url)
        {
            urlList = url.split(",");
        }
            
        /**
         * Gets the current LDAP URL, suitable for use in a LdapContext.
         * @return the current LDAP URL.
         */
        synchronized String getCurrentUrl()
        {
            return urlList[index];
        }
            
        /**
         * Gets the next backup LDAP URL, suitable for use in a LdapContext.
         * @return the next backup LDAP URL.
         */
        synchronized String getNextUrl()
        {
            if (urlList.length == 1)
                return urlList[0];
                
            index++;
            
            if (index == urlList.length)
                index = 0;
                
            return getCurrentUrl();
        }
            
        /**
         * Gets the number of LDAP URLS.
         * 
         * @return a count of LDAP URLS.
         */
        synchronized int getUrlCount()
        {
            return urlList.length;
        }
    }
    
    /**
     * Prints the JAAS and LDAP/JNDI properties to the specified print stream.
     * 
     * @param ps the PrintStream to write to.
     */
    protected void printProperties(PrintStream ps)
    {
        super.printProperties(ps);
        printProperties("LDAP LoginModule: JNDI Properties", ldapProperties, ps);
    }
    
    /**
     * Initializes the LDAP configuration used by the EMS JAAS Plug-in.
     * 
     * @param defaults LoginModule default values.
     * @param options LoginModule options passed into the initialization
     * method of a LoginModule.
     */
    protected void addOptions(Map<String, ?>defaults, Map<String, ?> options)
    {
        super.addOptions(defaults, options);
        
        if (getString(LDAPSimpleAuthentication.LDAP_URL) != null)
        {
            String ldapUrl = new String(getString(LDAPSimpleAuthentication.LDAP_URL));
            if (ldapUrl.toLowerCase().startsWith("ldaps"))
            {
                String cert = getString(LDAPSimpleAuthentication.LDAP_CERTIFICATE);
                if (cert != null)
                    System.setProperty("javax.net.ssl.trustStore", cert);
            }

            ldapServers = new LdapServerSet(ldapUrl);
            ldapProperties.put(Context.PROVIDER_URL, ldapServers.getCurrentUrl());

            String operation_timeout = getString(LDAPSimpleAuthentication.LDAP_OPERATION_TIMEOUT);
            if ( operation_timeout != null ){
                ldapProperties.put("com.sun.jndi.ldap.read.timeout", operation_timeout);
                ldapProperties.put("com.sun.jndi.ldap.connect.timeout", operation_timeout);
            }
        }
        
        ldapProperties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");

        /*
         * Now add any properties that are not EMS specific into the LDAP specific
         * properties.  This provides a way to pass additional properties to
         * configure JNDI, and override any default properties EMS sets.  
         * 
         * Examples would include:
         * java.naming.security.authentication
         * java.naming.security.protocol
         * java.naming.language
         * 
         */
        Pattern   propsPattern = Pattern.compile("^tibems\\..*");
        for (Map.Entry<String, ?> e : this.entrySet())
        {
            if (!propsPattern.matcher((CharSequence) e.getKey()).matches())
                ldapProperties.put((String) e.getKey(), e.getValue());
        }
    }
    
    /**
     * Gets the value of a configuration option.
     * 
     * @param key the name of the option.
     * @returns value of the option as a Search Control scope value.
     */    
    synchronized int getScope(Object key)
    {
        if (!checkProperty(key))
            return 0;

        if ( ((String)super.get(key)).compareTo("subtree") == 0)
            return SearchControls.SUBTREE_SCOPE;
        if ( ((String)super.get(key)).compareTo("sub") == 0)
            return SearchControls.SUBTREE_SCOPE;
        if ( ((String)super.get(key)).compareTo("object") == 0)
            return SearchControls.OBJECT_SCOPE;
        
        return SearchControls.ONELEVEL_SCOPE;
    }
    
    /**
     * Gets the current URL that should be used to establish a connection
     * with the LDAP server.
     *  
     * @return the current LDAP URL.
     */    
    String getCurrentLdapUrl()
    {
        return (String)ldapProperties.get(Context.PROVIDER_URL);
    }

    /**
     * Updates the configuration after a communication failure.
     */
    void updateOnFailure()
    {
        ldapProperties.put(Context.PROVIDER_URL, ldapServers.getNextUrl());
    }
    
    /**
     * Gets the number of the times an LDAP server should retry a connection.
     *  
     * @return retry count.
     */
    int getInitialConnectRetryCount()
    {
        return ldapServers.getUrlCount();
    }
    
    /**
     * Returns a copy of the ldap properties.  A copy is returned
     * for thread safety purposes.
     * 
     * @returns the current LDAP properties.
     */    
    Hashtable<String, Object> getLdapProperties()
    {
        return new Hashtable<String, Object>(ldapProperties);
    }
}
