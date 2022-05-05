/*
 * Copyright (c) 2008-2016 TIBCO Software Inc.
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 *
 * $Id: LDAPDirectLoginModule.java 90180 2016-12-13 23:00:37Z $
 *
 */

package com.tibco.example;

import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.CommunicationException;
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
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/**
 * This class implements a very basic form of LDAP login for EMS. It will
 * validate all connections (be they users, routes, etc.) by authenticating to
 * the LDAP server as that user.
 * <p>
 * The user names must be fully qualified DNs, unless a user name pattern is
 * given. The option 'example.user_pattern' may contain a pattern string. The
 * actual DN used for the lookup will be this pattern string, with '%u' replaced
 * with the user name provided by the interface.
 * <p>
 * Most properties added in the JAAS configuration file will be passed directly
 * to JNDI when creating the LDAP context. This allows a lot of flexibility in
 * configuring the server connection. Only properties that begin with "example."
 * are reserved for this module. In the example as given, the only property used
 * is "example.ldap_url".
 */
public class LDAPDirectLoginModule implements LoginModule {
    private static final Pattern      EXAMPLE_PROP_PATTERN     = Pattern.compile("^example\\..*");
    private static final Pattern      USER_SUB_PATTERN         = Pattern.compile("(.*)%u(.*)");

    private static final String       LDAP_URL_OPTION          = "example.ldap_url";
    private static final String       LDAP_USER_PATTERN_OPTION = "example.user_pattern";

    private CallbackHandler           callbackHandler;
    private LdapContext               ldapCtx;
    private String                    usernameBefore           = "";
    private String                    usernameAfter            = "";

    private Hashtable<String, Object> ldapEnvironment          = new Hashtable<String, Object>();

    /*
     * (non-Javadoc)
     * 
     * @see javax.security.auth.spi.LoginModule#abort()
     */
    public boolean abort() throws LoginException {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.security.auth.spi.LoginModule#commit()
     */
    public boolean commit() throws LoginException {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.security.auth.spi.LoginModule#initialize(javax.security.auth.Subject,
     *      javax.security.auth.callback.CallbackHandler, java.util.Map,
     *      java.util.Map)
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map<String, ?> sharedState, Map<String, ?> options) {
        this.callbackHandler = callbackHandler;

        // Get basic lookup parameters set
        ldapEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        ldapEnvironment.put(Context.PROVIDER_URL, options.get(LDAP_URL_OPTION));

        Object patternTemp = options.get(LDAP_USER_PATTERN_OPTION);
        if (patternTemp != null && patternTemp instanceof String) {
            Matcher m = USER_SUB_PATTERN.matcher((String) patternTemp);
            if (m.matches()) {
                usernameBefore = m.group(1);
                usernameAfter = m.group(2);
            } else {
                System.err.println("Invalid user pattern: " + (String) patternTemp);
            }
        }

        // Add any other JNDI properties
        for (Map.Entry<String, ?> e : options.entrySet()) {
            if (!EXAMPLE_PROP_PATTERN.matcher(e.getKey()).matches())
                ldapEnvironment.put(e.getKey(), e.getValue());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.security.auth.spi.LoginModule#login()
     */
    public boolean login() throws LoginException {
        NameCallback namecallback = new NameCallback(" ");
        PasswordCallback passwordcallback = new PasswordCallback(" ", false);
        Callback[] callbacks = new Callback[2];
        callbacks[0] = namecallback;
        callbacks[1] = passwordcallback;

        try {
            callbackHandler.handle(callbacks);
        } catch (java.io.IOException ioe) {
            throw new LoginException(ioe.toString());
        } catch (UnsupportedCallbackException uce) {
            throw new LoginException("Error: " + uce.getCallback().toString()
                    + " not available to garner authentication information " + "from the user");
        }

        Hashtable<String, Object> finalLDAPProperties = new Hashtable<String, Object>();
        finalLDAPProperties.putAll(ldapEnvironment);

        finalLDAPProperties.put(Context.SECURITY_PRINCIPAL, usernameBefore + namecallback.getName()
                + usernameAfter);
        finalLDAPProperties.put(Context.SECURITY_CREDENTIALS, new String(passwordcallback
                .getPassword()));

        try {
            ldapCtx = new InitialLdapContext(finalLDAPProperties, null);
        } catch (NamingException e) {
            LoginException logException;
            if (e instanceof CommunicationException) {
                logException = new LoginException("LDAP lookup Failure");
                logException.initCause(e);
                logException.printStackTrace();
            } else {
                logException = new LoginException("Login failed");
                logException.initCause(e);
            }

            throw logException;
        }

        // Immediately log out
        try {
            ldapCtx.close();
            ldapCtx = null;
        } catch (NamingException e) {
            LoginException logException = new LoginException("Unable to close LDAP context");
            logException.initCause(e);
            throw logException;
        }

        return true;
    }

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#logout()
     */
    public boolean logout() throws LoginException {
        return true;
    }

}
