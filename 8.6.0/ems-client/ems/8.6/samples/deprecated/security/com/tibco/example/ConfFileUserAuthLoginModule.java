/*
 * Copyright (c) 2011-2016 TIBCO Software Inc.
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 *
 * $Id: ConfFileUserAuthLoginModule.java 90180 2016-12-13 23:00:37Z $
 *
 */
package com.tibco.example;

import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.mail.internet.MimeUtility;

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
 *  com.tibco.example.ConfFileUserAuthLoginModule required 
 *     debug=true 
 *     filename="../samples/security/users.conf";
 *  };  
 *
 * Options
 * debug={true,false} An optional value that displays additional output.
 * filename=<string> the location of users.conf or a file in that very same 
 * format.
 * 
 * @see javax.security.auth.spi.LoginModule
 */
public class ConfFileUserAuthLoginModule implements LoginModule
{

    public byte[] decodeBase64(String in) throws Exception 
    {
        byte[] b = in.getBytes("US-ASCII");
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        InputStream b64is = MimeUtility.decode(bais, "base64");
        byte[] tmp = new byte[b.length];
        int n = b64is.read(tmp);
        byte[] res = new byte[n];
        System.arraycopy(tmp, 0, res, 0, n);
        return res;
    }  

    private static class Credential
    {
        public final byte[] salt;
        public final byte[] hash;

        public Credential(byte[] salt, byte[] hash)
        {
            this.salt = salt;
            this.hash = hash;
        }
    }

    // initial state
    private CallbackHandler                      callbackHandler;

    /**
     * Use userTable as lock to bind reloading from the file.
     * */
    // user storage
    private static final Map<String, Credential> userTable    = new HashMap<String, Credential>();
    private static volatile long                 lastModified = -1;

    // configurable options
    private static boolean                       debug        = false;
    private static File                          file         = null;
    private static String                        filename     = null;

    // the authentication status
    private boolean                              succeeded    = false;
    private boolean                              userExists   = false;

    // our username and password
    private String                               username     = null;
    private byte[]                               password     = null;

    private void debugOutput(String s)
    {
        if (debug)
            out.printf("EMS ConfFileUserAuthLoginModule - %s\n", s);
    }

    // this method initializes any
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map<String, ?> sharedState, Map<String, ?> options)
    {
        this.callbackHandler = callbackHandler;

        if (file == null)
        {
            synchronized (userTable)
            {
                if (file == null)
                {
                    // initialize any configured options
                    debug = "true".equalsIgnoreCase((String) options.get("debug"));
                    filename = (String) options.get("filename");
                    file = new File((null == filename ? "users.conf" : filename));

                    debugOutput("initialize with: " + file.getAbsolutePath()
                            + " exists, " + file.exists());

                    if (!file.exists())
                        file = null;

                    try
                    {
                        reloadUsers();
                    }
                    catch (Exception e)
                    {
                        debugOutput("Error: initially loading users failed "
                                + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Reload the user table. This operation is only performed when necessary.
     * 
     * This file defines all users. The format of the file is:
     * username:password:"description"
     * 
     * Username The name of the user. The username cannot exceed 127 characters 
     * in length.
     * 
     * Password is mandatory and starts with "$2$" to indicate that 
     * Hmac-SHA256-144 was used to hash the password.
     * The password is stored as the salt and base 64 encoded, 
     * truncated SHA256 hash of the password. 
     * Salt and encoded password hash are separated by $
     * 
     * (further types are not supported by this plugin).
     */
    private void reloadUsers()
        throws LoginException
    {
        if (file == null)
            throw new LoginException("Error: Specified file for this login "
                    + "module does not exist: " + filename 
                    + " (e.g. use filename=users.conf");

        final long fileModificationTime = file.lastModified();
        if (lastModified != fileModificationTime)
        {
            synchronized (userTable)
            {
                if (lastModified == fileModificationTime)
                {
                    // someone else did it.
                    return;
                }

                userTable.clear();
                lastModified = fileModificationTime;
                debugOutput("Parsing file: " + file.getName());

                BufferedReader in = null;
                try
                {
                    in = new BufferedReader(new FileReader(file));
                    String workingLine;
                    String[] tokens;
                    String user;
                    String salt;
                    String pwdHash;
                    int indexDeviderSalt;
                    byte[] decodedPwdHash = null;
                    while ((workingLine = in.readLine()) != null)
                    {
                        workingLine = workingLine.trim();
                        // parse, and store
                        tokens = workingLine.split(":");

                        if (tokens.length > 2)
                        {
                            user = tokens[0];

                            if (user.length() > 0
                                    && tokens[1].startsWith("$2$"))
                            {
                                indexDeviderSalt = tokens[1].lastIndexOf('$');

                                salt = tokens[1].substring(3, indexDeviderSalt);
                                pwdHash = tokens[1].substring(1 + indexDeviderSalt);
                                decodedPwdHash = decodeBase64(pwdHash);
                                debugOutput("storing user: " + user);
                                userTable.put(user,
                                              new Credential(salt.getBytes("US-ASCII"),
                                                             decodedPwdHash));
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    debugOutput(e.toString());
                    LoginException le = new LoginException(
                                                           "IO error when reading users from "
                                                                   + file.getName());
                    le.initCause(e);
                    throw le;
                }
                finally
                {
                    if (in != null)
                    {
                        try
                        {
                            in.close();
                        }
                        catch (IOException e)
                        {
                            debugOutput(e.toString());
                            LoginException le = new LoginException(
                                                                   "IO error when reading users from "
                                                                           + file.getName());
                            le.initCause(e);
                            throw le;
                        }
                    }
                }
            }
        }
    }

    /**
     * Authenticate a user based on the contents of users.conf.
     * 
     * <p>
     * 
     * @return true if the user exists in the configuration file and could be authenticated.
     *         false iif the user does not exist in the configuration file (this
     *             <code>LoginModule</code> should be ignored).
     * 
     * @exception FailedLoginException
     *                if the authentication fails. <p>
     * 
     * @exception LoginException
     *                if this <code>LoginModule</code> is unable to perform
     *                the authentication.
     */
    public boolean login()
        throws LoginException
    {
        succeeded = false; // Indicate not yet successful

        if (callbackHandler == null)
            throw new LoginException("Error: no CallbackHandler available "
                    + "to garner authentication information from the user");

        // Get the user name and password from the server
        try
        {
            NameCallback nameCallback = new NameCallback(" ");
            PasswordCallback passwordCallback = new PasswordCallback(" ", false);
            Callback[] callbacks = { nameCallback, passwordCallback };
            callbackHandler.handle(callbacks);

            username = nameCallback.getName();
            debugOutput("Authenticating user: " + username);
            password = new String(passwordCallback.getPassword()).getBytes();

        }
        catch (IOException ioe)
        {
            LoginException le = new LoginException(
                                                   "Error: IO error in callback");
            le.initCause(ioe);
            throw le;
        }
        catch (UnsupportedCallbackException uce)
        {
            LoginException le = new LoginException(
                                                   "Error: Unsupported callback");
            le.initCause(uce);
            throw le;
        }
        reloadUsers();

        final Credential user = userTable.get(username);
        userExists = (user != null);
        if (userExists)
        {
            Mac mac;
            try
            {
                mac = Mac.getInstance("HmacSHA256");
                mac.init(new javax.crypto.spec.SecretKeySpec(user.salt,
                                                             "HmacSHA256"));
            }
            catch (NoSuchAlgorithmException nsae)
            {
                debugOutput("Error: NO HmacSHA1 " + nsae.getMessage());
                LoginException le = new LoginException(
                                                       "Error: HmacSHA1 not available");
                le.initCause(nsae);
                throw le;
            }
            catch (InvalidKeyException ike)
            {
                debugOutput("Error: invalid key " + ike.getMessage());
                LoginException le = new LoginException("Error: Invalid key");
                le.initCause(ike);
                throw le;
            }

            byte[] hashedPwd = mac.doFinal(password);
            /**
             * As per RFC 2104 section 5, Truncated output, it is OK to truncate
             * the output.
             *  
             * " We recommend that the output length t be not less than half the
             * length of the hash output (to match the birthday attack bound) 
             * and not less than 80 bits (a suitable lower bound on the number 
             * of bits that need to be predicted by an attacker)."
             * 
             * This has been done during storing and has to be done now as well. 
             * 144 bits makes a nice size for base-64 encoding 
             */
            byte[] truncatedHashedPwd = new byte[18];
            System.arraycopy(hashedPwd, 0, truncatedHashedPwd, 0,
                             truncatedHashedPwd.length);

            succeeded = Arrays.equals(truncatedHashedPwd, user.hash);
            if (!succeeded)
                throw new FailedLoginException("Login failed");
        }
        else
        {
            debugOutput("Unable to authenticate user " + username);
        }

        return userExists;
    }

    /**
     * <p> This method is called if the LoginContext's overall authentication
     * succeeded (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL
     * LoginModules succeeded).
     * 
     * @exception LoginException
     *                if the commit fails.
     * 
     * @return true if the user exists in the configuration file.
     *         false if not and this <code>LoginModule</code> should be ignored.
     */
    public boolean commit()
        throws LoginException
    {
        return userExists;
    }

    /**
     * Reset the plugin's state and return if this plugin was able to find
     * the user at all.
     * @return true if the user exists in the configuration file.
     *         false if not and this <code>LoginModule</code> should be ignored.
     */
    private boolean reset()
    {
        boolean tmpUserExists = userExists;
        username = null;
        password = null;
        succeeded = false;
        userExists = false;
        return tmpUserExists;
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
     * @return true if the user exists in the configuration file.
     *         false if not and this <code>LoginModule</code> should be ignored.
     */
    public boolean abort()
        throws LoginException
    {
        debugOutput("aborted authentication attempt");
        return reset();
    }

    /**
     * Logout the user.
     * 
     * @exception LoginException
     *                if the logout fails.
     * 
     * @return true if the user exists in the configuration file.
     *         false if not and this <code>LoginModule</code> should be ignored.
     */
    public boolean logout()
        throws LoginException
    {
        if (debug)
            out.println("EMS FlatFileUserAuthLoginModule - logging out user: "
                    + username);

        return reset();
    }
}
