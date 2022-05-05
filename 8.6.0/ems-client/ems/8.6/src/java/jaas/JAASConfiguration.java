/*
 * Copyright (c) 2013-2016 TIBCO Software Inc.
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 *
 * $Id: JAASConfiguration.java 90180 2016-12-13 23:00:37Z $
 *
 */
package com.tibco.tibems.tibemsd.security.jaas;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class JAASConfiguration extends Hashtable<String, Object>
{
    private static final long serialVersionUID = 5534065333083847148L;

    /**
     * Enables debugging output from the module, to aid in diagnosing configuration
     * problems.  WARNING: USE OF THE DEBUG FLAG MAY CREATE SECURITY VULNERABILITIES
     * by revealing information in the log file.
     */
    public static final String JAAS_DEBUG = "debug";
    
    /**
     * Enable debug output.
     */
    protected boolean debug = false;

    /**
     * A set used for discovering unused properties when debugging.  This
     * is very helpful in identifying mistyped parameter names.
     */
    HashSet<String> retrievedOptions = null;
    
    /**
     * Prints the properties found in a Hashtable.
     * 
     * @param hashName name of the property grouping.
     * @param p the properties
     * @param ps the output PrintStream.
     */
    protected static void printProperties(String hashName, Hashtable<String, Object> p, PrintStream ps)
    {
        Object key;
        Object value;
        
        ps.println(hashName + " {");
        
        Enumeration<String> en = p.keys();
        
        while (en.hasMoreElements())
        {
            key   = en.nextElement();
            value = p.get(key);
            
            ps.println("    " + key + ":" + value);
        }
        
        ps.println("}");
        
    }
    
    /**
     * Prints the JAAS and LDAP/JNDI properties to the specified print stream.
     * 
     * @param ps the PrintStream to write to.
     */
    protected void printProperties(PrintStream ps)
    {
        printProperties("JAAS LoginModule: EMS JAAS Properties", this, ps);
    }

    /**
     * Prints JAAS properties that have not been retrieved, to the
     * specified print stream. This method is useful for debugging
     * mistyped parameters.
     * 
     * @param ps the PrintStream to write to.
     */
    protected void printUnusedProperties(PrintStream ps)
    {
        if (retrievedOptions == null || ps == null)
            return;

        /*
         * Create a copy of the login module options and remove those
         * that have been retrieved, leaving options that are not
         * recognized.  These may be system properties passed into JNDI,
         * or mistyped properties/options.
         */
        Hashtable<String, Object> remainingOptions = new Hashtable<String, Object>(this);

        Iterator<String> iter = retrievedOptions.iterator();
        while (iter.hasNext())
            remainingOptions.remove(iter.next());

        iter = remainingOptions.keySet().iterator();
        while (iter.hasNext())
            ps.printf("EMS JAAS Configuration: Unrecognized parameter '%s'.\n", iter.next());
    }
    
    
    /**
     * Initializes the configuration used by the EMS JAAS Plug-in.
     * 
     * @param defaults LoginModule default values.
     * @param options LoginModule options passed into the initialization
     * method of a LoginModule.
     */
    protected void addOptions(Map<String, ?>defaults, Map<String, ?> options)
    {
        // add all of the properties passed into our hash.
        if (defaults != null)
            putAll(defaults);
        
        if (options != null)
            putAll(options);
        
        if (getBoolean(JAAS_DEBUG) == true)
        {
            debug = true;
            if (retrievedOptions == null)
            {
                retrievedOptions = new HashSet<String>();
                retrievedOptions.add(JAAS_DEBUG);
            }
        }
    }
    
    /**
     * Checks the validity of a property.  When debugging, the
     * retrieved option name is saved to later identify unknown 
     * configuration options/properties.
     * 
     * @param key the name of the configuration option.
     * @return true if the property is valid, false otherwise.
     */
    protected boolean checkProperty(Object key)
    {
        if (key == null)
            return false;
        
        if (!containsKey(key))
            return false;
        
        if (retrievedOptions != null)
            retrievedOptions.add((String) key);
        
        return true;
    }
    
    /**
     * Gets the value of a configuration option.
     * 
     * @param key the name of the option.
     * @returns value of the option as a String.
     */    
    protected synchronized String getString(Object key)
    {
        if (!checkProperty(key))
            return null;

        return (String)super.get(key);
    }

    /**
     * Gets the value of a configuration option.
     * 
     * @param key the name of the option.
     * @returns value of the option as a Boolean.
     */    
    protected synchronized boolean getBoolean(Object key)
    {
        if (!checkProperty(key))
            return false;

        if ( ((String)super.get(key)).compareToIgnoreCase("true") == 0)
            return true;
        
        return false;
    }
    
    /**
     * Gets the value of a configuration option.
     * 
     * @param key the name of the option.
     * @returns value of the option as an int.
     */    
    protected synchronized int getInt(Object key)
    {
        if (!checkProperty(key))
            return 0;

        return Integer.valueOf((String)super.get(key));
    }
}
