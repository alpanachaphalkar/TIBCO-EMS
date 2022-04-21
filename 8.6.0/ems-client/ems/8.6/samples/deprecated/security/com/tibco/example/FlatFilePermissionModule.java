/*
 * Copyright (c) 2007-2016 TIBCO Software Inc.
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 *
 * $Id: FlatFilePermissionModule.java 90180 2016-12-13 23:00:37Z $
 *
 */


package com.tibco.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.tibco.tibems.tibemsd.security.Action;
import com.tibco.tibems.tibemsd.security.AuthorizationResult;
import com.tibco.tibems.tibemsd.security.Authorizer;
import com.tibco.tibems.tibemsd.security.Util;

/**
 *
 * This sample demonstrates the use of JACI within
 * TIBCO Enterprise Message Service.
 * 
 * To setup this example, specify the name of a class which implements
 * JACI and its classpath through the EMS server configuration option 
 * "jaci_class" and "jaci_classpath". Please refer to the EMS user guide for 
 * further options concerning JACIModule.
 * 
 * The permission file used by this FlatFilePermissionModule is a simple ASCII 
 * file in which each line contains a username, destination, actions, and cache timeout. 
 * Cache timeout determines how long results from previous Permissions Module queries
 * are cached. A missing timeout means the result is not cached. A file might contain:
 * 
 * Colin > TOPIC_SUBSCRIBE 30
 * Russ > TOPIC_PUBLISH 10
 * Bob test.* TOPIC_SUBSCRIBE,TOPIC_PUBLISH,TOPIC_DURABLE,TOPIC_USE_DURABLE
 * 
 */
public class FlatFilePermissionModule implements Authorizer
{
    final static String FILE_NAME_PROPERTY  = "example.permission.file";
    final static String DEBUG_PROPERTY      = "example.permission.debug";

    File    file;
    boolean debug = false;

    private void debugOutput(String s)
    {
        if (debug)
            System.out.println("EMS FlatFilePermissionModule - " + s);
    }

    public FlatFilePermissionModule() throws Exception
    {
        // Find out what the file name is
        String fileName = System.getProperty(FILE_NAME_PROPERTY);
        if (fileName == null)
            throw new Exception("Need to set permission file in system property " + FILE_NAME_PROPERTY);
        
        file = new File(fileName);
        if (!file.canRead())
            throw new Exception("Cannot read permission file " + fileName);

        String debugOn = System.getProperty(DEBUG_PROPERTY);
        if (debugOn != null)
            debug = "true".equalsIgnoreCase(debugOn);
    }

    /* (non-Javadoc)
     * @see com.tibco.tibems.tibemsd.security.Authorizer#isAllowed(java.lang.String, com.tibco.tibems.tibemsd.security.Action)
     */
    public AuthorizationResult isAllowed(String userName, Action action)
    {
        debugOutput("Checking user " + userName + " " + action.getType() + " on " + action.getDestination());
        BufferedReader in = null;
        try
        {
            // Try to open the file.
            in = new BufferedReader(new FileReader(file));
            try 
            {
                // Parse file, looking for user + destination match
                String line;
                while ( (line = in.readLine()) != null)
                {
                    AuthorizationResult result = parseLine(userName, action, line);
                    if (result != null)
                        return result;
                }
            }
            finally
            {
                in.close();
            }
        } catch (IOException e)
        {
            // Something went wrong.  Some implementations would want to
            // return a deny value here.  For illustration purposes, we return
            // "no opinion"
            e.printStackTrace();
            return null;
        }
        
        // We didn't find any permission in the file, so deny
        return new AuthorizationResult(false, 1800, TimeUnit.SECONDS);
    }

    private AuthorizationResult parseLine(String userName, Action action, String line)
    {
        // Ignore comment lines
        if (line.startsWith("#"))
            return null;

        // Get four parts, separated by white space, the last one (cache time) is optional
        String[] parts = line.split("\\s+", 4);
        
        if (parts == null || parts.length < 3)
            return null;
        
        String lineUserName     = parts[0];
        String lineDestination  = parts[1];
        String lineAllows       = parts[2];
        long   cacheTime        = 0;

        if (parts.length == 4)
        {
            try
            {
                cacheTime = Long.parseLong(parts[3]);
            }
            catch (NumberFormatException e)
            {
                e.printStackTrace();
                return null;
            }
        }

        // Does the username match?
        if (userName.equals(lineUserName))
        {
            // Does the line's wildcard destination match this one?
            if (Util.isDestinationContainedBy(action.getDestination(), lineDestination))
            {
                // Is the action explicitly allowed?
                if (lineAllows.contains(action.getType().name()))
                    return new AuthorizationResult(true, cacheTime, TimeUnit.SECONDS, lineDestination);
                else
                    return new AuthorizationResult(false, cacheTime, TimeUnit.SECONDS, lineDestination);
            }
        }
        
        return null;
    }

}
