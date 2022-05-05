/*
 * Copyright (c) 2007-2016 TIBCO Software Inc.
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 *
 * $Id: FlatFileUserAuthLoginModule.java 90180 2016-12-13 23:00:37Z $
 *
 */
package com.tibco.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

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
 *
 * This sample demonstrates the use of JAAS within
 * TIBCO Enterprise Message Service.
 * 
 * To setup this example, specify the location of a JAAS Configuration file
 * through the EMS server configuration option "jaas_config_file".  Please
 * refer to the EMS user guide for further options concerning JAAS Login
 * Modules
 *
 * The JAAS configuration file entry for this login Module should have
 * a section similar to the following:
 *  
 * EMSUserAuthentication {
 *  com.tibco.example.FlatFileUserAuthLoginModule required 
 *     debug=true 
 *     filename="../samples/security/userpass.txt";
 *  };  
 *
 * Options
 * debug={true,false} An optional value that displays additional output.
 * filename=<string> the location of username password file
 * 
 * The username/password file is a simple ASCII file in which each line
 * contains a username and password.  A file might contain:
 * 
 * Colin:guitar!
 * Russ:photos00
 * Balbhim:9tennis9
 * Bob:runner01
 *
 * @see javax.security.auth.spi.LoginModule
 */
public class FlatFileUserAuthLoginModule implements LoginModule
{
    // initial state
    private CallbackHandler            callbackHandler;

    // configurable options
    private boolean                    debug           = false;
    private String                     filename        = null;

    // the authentication status
    private boolean                    succeeded       = false;
    
    // our username and password
    private String                     username        = null;
    private String                     password        = null;
       
    private void debugOutput(String s)
    {
        if (debug)
            System.out.println("EMS FlatFileUserAuthLoginModule - " + s);
    }
    
    // this method initializes any 
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                    Map<String, ?> sharedState, Map<String, ?> options)
    {
        this.callbackHandler = callbackHandler;

        // initialize any configured options
        debug = "true".equalsIgnoreCase((String)options.get("debug"));
        filename = (String)options.get("filename");
    }
    
    // each line is username:password, if the username
    // and password matches, we'll return a success (true).
    private boolean processLine(String line)
    {
        String workingLine = line.trim();
        String[] tokens = workingLine.split(":");

        // username is tokens[0];
        // password is tokens[1];
        if (username.compareTo(tokens[0]) == 0)
        {
            debugOutput("Found user " + tokens[0]);
            if (password.compareTo(tokens[1]) == 0)
            {
                debugOutput("Password is valid.");
                return true;
            }
            else
            {
                debugOutput("Password did not match");
            }
        }
        
        return false;
    }

    
    private boolean readUsersFile() throws IOException
    {
        boolean validUser = false;
        
        debugOutput("Parsing file: " + filename);
        
        BufferedReader in = new BufferedReader(new FileReader(filename));
        String str;
        while ((str = in.readLine()) != null && validUser == false) {
            validUser = processLine(str);
        }
        in.close();
        
        if (validUser == false)
            debugOutput("Unable to authenticate user " + username);

        return validUser;
    }
    

    /**
     * Authenticate a user based on a very simple flat file scheme.
     * 
     * <p>
     * 
     * @return true in all cases since this <code>LoginModule</code> should
     *         not be ignored.
     * 
     * @exception FailedLoginException
     *                if the authentication fails. <p>
     * 
     * @exception LoginException
     *                if this <code>LoginModule</code> is unable to perform
     *                the authentication.
     */
    public boolean login() throws LoginException
    {

        succeeded = false; // Indicate not yet successful

        if (filename == null)
            throw new LoginException("Error:  Required file name not specified for this login module. (e.g. filename=emsusers.txt");
        
        if (callbackHandler == null)
            throw new LoginException("Error: no CallbackHandler available " +
                        "to garner authentication information from the user");

        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback(" ");
        callbacks[1] = new PasswordCallback(" ", false);
        
        try {
            callbackHandler.handle(callbacks);
            
            username = ((NameCallback)callbacks[0]).getName();
            debugOutput("Authenticating user:  " + username);
            
            char[] tmpPassword = ((PasswordCallback)callbacks[1]).getPassword();
            if (tmpPassword == null) {
                // treat a NULL password as an empty password
                tmpPassword = new char[0];
            }
            password = new String(tmpPassword);
        } 
        catch (java.io.IOException ioe) {
            throw new LoginException(ioe.toString());
        } 
        catch (UnsupportedCallbackException uce) {
            throw new LoginException("Error: " + uce.getCallback().toString() +
                " not available to garner authentication information " +
                "from the user");
        }

        /*
         *  Parse our file and see if the proper username/password
         *  is in it.
         */
        try {
            succeeded = readUsersFile();
        }
        catch (Exception e)
        {
            debugOutput(e.toString());
        }
        
        if (!succeeded)
            throw new LoginException("Login failed");
        
        return true;
    }

    /**
     * <p> This method is called if the LoginContext's overall authentication
     * succeeded (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL
     * LoginModules succeeded).
     * 
     * @exception LoginException
     *                if the commit fails.
     * 
     * @return true if this LoginModule's own login and commit attempts
     *         succeeded, or false otherwise.
     */
    public boolean commit() throws LoginException
    {
        /* commit is not required for this example */
        return true;
    }

    /**
     * <p> This method is called if the LoginContext's overall authentication
     * failed. (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL
     * LoginModules did not succeed).
     * 
     * <p> If this LoginModule's own authentication attempt succeeded (checked
     * by retrieving the private state saved by the <code>login</code> and
     * <code>commit</code> methods), then this method cleans up any state
     * that was originally saved.
     * 
     * <p>
     * 
     * @exception LoginException
     *                if the abort fails.
     * 
     * @return false if this LoginModule's own login and/or commit attempts
     *         failed, and true otherwise.
     */
    public boolean abort() throws LoginException
    {
        debugOutput("aborted authentication attempt");

        if (succeeded == false)
        {
            return false;
        }
        else
        {
            // overall authentication succeeded and commit succeeded,
            // but someone else's commit failed
            logout();
            succeeded = false;
            return true;
        }
    }

    /**
     * Logout the user.
     * 
     * @exception LoginException
     *                if the logout fails.
     * 
     * @return true in all cases since this <code>LoginModule</code> should
     *         not be ignored.
     */
    public boolean logout() throws LoginException
    {
        succeeded = false;
        if (debug)
            System.out.println("EMS FlatFileUserAuthLoginModule - logging out user: " + username);
        return true;
    }
}
