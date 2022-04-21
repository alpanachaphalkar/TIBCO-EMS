/*
 * Copyright (c) 2012-2016 TIBCO Software Inc.
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 *
 * $Id: GroupFilePermissionModule.java 90180 2016-12-13 23:00:37Z $
 *
 */

package com.tibco.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import com.tibco.tibems.tibemsd.security.Action;
import com.tibco.tibems.tibemsd.security.AuthorizationResult;
import com.tibco.tibems.tibemsd.security.Authorizer;
import com.tibco.tibems.tibemsd.security.Util;

/**
 *
 * This sample demonstrates the use and cooperation of JAAS and JACI within
 * TIBCO Enterprise Message Service.
 * 
 * To setup this example, specify the name of a class which implements
 * JACI and its classpath through the EMS server configuration option 
 * "jaci_class" and "jaci_classpath". Please refer to the EMS user guide for 
 * further options concerning JACIModule.
 * 
 * The permission file used by this GroupFilePermissionModule is a simple text 
 * file in which each line contains a group name, destination, actions, and cache timeout.
 * Cache timeout determines how long results from previous Permissions Module queries
 * are cached. A missing timeout means the result is not cached. 
 * The timeout has no impact on the groups a user is a member of.
 * 
 * A file might contain:
 * group1 foo.> TOPIC_SUBSCRIBE 30
 * group2 foo.> TOPIC_PUBLISH 10
 * group3 test.* TOPIC_SUBSCRIBE,TOPIC_PUBLISH,TOPIC_DURABLE,TOPIC_USE_DURABLE
 * 
 */
public class GroupFilePermissionModule implements Authorizer
{
    public static final String FILE_NAME_PROPERTY  = "example.group.permission.file";
    public static final String DEBUG_PROPERTY      = "example.group.permission.debug";

    private File    file = null;
    private boolean debug = false;

    private Hashtable<String,String[]> membership = null;

    private String[] groups = null;

    private void debugOutput(String s)
    {
        if (debug)
            System.out.println("EMS GroupFilePermissionModule - " + s);
    }

    public GroupFilePermissionModule() throws Exception
    {
        // Find out what the file name is
        String fileName = System.getProperty(FILE_NAME_PROPERTY);
        if (fileName == null)
            throw new Exception("Need to set permission file in system property " + FILE_NAME_PROPERTY);

        file = new File(fileName);
        if (!file.canRead())
            throw new Exception("Cannot read permission file " + fileName);

        debug = "true".equalsIgnoreCase(System.getProperty(DEBUG_PROPERTY)); 
    }

    /* (non-Javadoc)
     * @see com.tibco.tibems.tibemsd.security.Authorizer#isAllowed(java.lang.String, com.tibco.tibems.tibemsd.security.Action)
     */
    public AuthorizationResult isAllowed(String userName, Action action)
    {
        debugOutput("Checking user " + userName + " " + action.getType() + " on " + action.getDestination());
        if (debug)
            LDAPGroupInfo.debugGroupOutput();

        String[] membership = LDAPGroupInfo.getUserMember(userName);

        if (membership.length>0)
        {
            try
            {
                // Try to open the file.
                BufferedReader in = new BufferedReader(new FileReader(file));
                try 
                {
                    // Parse file, looking for user + destination match
                    for( String line; (line = in.readLine()) != null;)
                    {
                        AuthorizationResult result = processLine(userName, membership, 
                                                                 action, line);
                        if (result != null)
                            return result;
                    }
                }
                finally
                {
                    in.close();
                }
            }
            catch (IOException e)
            {
                // Something went wrong.  Some implementations would want to
                // return a deny value here.  For illustration purposes, we return
                // "no opinion"
                e.printStackTrace();
            }
        }
        else
        {
            debugOutput("The user " + userName + " belongs to no group.");
        }

        // We didn't find any permission in the file, so deny 
        return new AuthorizationResult(false, 1800, TimeUnit.SECONDS);
    }

    private AuthorizationResult processLine(String userName, String[] groups, 
                                          Action action, String line)
    {
        // Ignore comment lines
        if (line.startsWith("#"))
            return null;

        // Get four parts, separated by white space, the last one (cache time) is optional
        String[] parts = line.split("\\s+", 4);

        if (parts == null || parts.length < 3)
        {
            debugOutput("Bad format: Not enough tokens in line '" + line + "'");
            return null;
        }

        String lineGroupName    = parts[0];
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
                debugOutput("Bad format: Couldn't parse cache time '" + parts[3] + "'.");
                return null;
            }
        }

        debugOutput("check if the user belongs to this group and if this group has permission: " + lineGroupName);

        for (String group : groups)
        {
            if (lineGroupName.equals(group))
            {
                debugOutput(" the group: " + group + " the user belongs to is defined within the permission file");

                // Does the line's wildcard destination match this one?
                if (Util.isDestinationContainedBy(action.getDestination(), lineDestination))
                {
                    // Is the action explicitly allowed?
                    if (lineAllows.contains(action.getType().name()))
                        return new AuthorizationResult(true, cacheTime, TimeUnit.SECONDS, lineDestination);
                    else
                        return null;
                }
            }
        }
        return null;
    }
}
