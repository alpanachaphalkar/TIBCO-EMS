/*
 * Copyright (c) 2012-2016 TIBCO Software Inc.
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 *
 * $Id: LDAPGroupInfo.java 90180 2016-12-13 23:00:37Z $
 *
 */

package com.tibco.example;

import java.util.Hashtable;
import java.util.Arrays;

/**
* This class functions is used to facilitate exchane of data between the 
* LDAPSearchLoginModule and the GroupFilePermissionsModule.
* 
* The exchange of group membership primarely works through shared access to the 
* static hashtable userMembership.
* 
* Be aware that the group membership of a user is only updated on logon. 
* Therefore you should NOT use GroupFilePermissionModule if you require 
* timely changes of group membership.
*/
public class LDAPGroupInfo 
{
    private static Hashtable<String,String[]> userMembership  = new Hashtable<String,String[]>(); 

    private LDAPGroupInfo()
    {
    }

    public static String[] getUserMember(String userName)
    {
        if (userMembership.containsKey(userName))
            return userMembership.get(userName);
        else
            return new String[0];
    }

    public static void setUserMember(String userName, String[] grouplist)
    {
        userMembership.put(userName, grouplist);
    }

    public static void debugGroupOutput()
    {
        if (userMembership.isEmpty())
        {
            System.out.println("membership hashtable contains no members");
        }
        else
        {
            for (String username : userMembership.keySet())
            {
                String[] groups = userMembership.get(username);
                System.out.println("the user: " + username + " belongs to: " + Arrays.toString(groups));
            }
        }
    }
}
