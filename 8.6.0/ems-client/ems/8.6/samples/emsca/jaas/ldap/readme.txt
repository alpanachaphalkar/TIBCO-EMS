 =================================================================
 TIBCO Enterprise Message Service
 Copyright (c) 2001-2014 TIBCO Software Inc.
 ALL RIGHTS RESERVED
 =================================================================

 This file describes the Central Admin JAAS configuration to integrate
 with an LDAP server. The included `ldap.jaas` contains a sample configuration
 for the `org.eclipse.jetty.jaas.spi.LdapLoginModule` JAAS module.

 The `ldap.jaas` sample configuration file tells the module how to read your LDAP directory
 and extract valid users and roles for EMSCA. The sample is simply a boilerplate which 
 requires modification to match your LDAP server's schema.

 To use the sample, you must edit the `ldap.jaas` file to match your LDAP server's schema 
 (described below), then start emsca with the option `--jaas` followed by the path to the
 `emsca.jaas` file.
 On Windows, specify the path to the file using forward slashes.


EMSCA LDAP JAAS Authentication
==============================

The security mechanism of EMSCA 8.x relies on Jetty's implementation of Java
Authentication and Authorization Service JAAS LoginModules. See
<http://docs.oracle.com/javase/1.5.0/docs/guide/security/jaas/JAASLMDevGuide.html>.

For information on how to enable authentication in EMSCA 8.x in general, 
please refer to SOL1-E4BALL, and your EMSCA documentation.


EMSCA JAAS Configuration
------------------------

In order to determine if a user should have access to administrative
 functionality in EMSCA, EMSCA defines two group names `emsca-admin` and `emsca-guest`:

 * The `emsca-admin` group is able to add, view, edit, delete and deploy 
   configurations in EMSCA. 

 * The `emsca-guest` group is a read-only group that is able to view 
   configurations in EMSCA.

These group names are EMSCA-only constructs to enable the administration 
functionality in the EMSCA UI to add, delete, edit and deploy configurations 
from within EMSCA.

To read or deploy configurations from/to an EMS server, EMSCA authenticates 
to the EMS server using the credentials provided by the user. For this reason, 
the `emsca-admin` user also needs to exist in the EMS configuration and be a 
member of the `$admin` group.


 org.eclipse.jetty.jaas.spi.LdapLoginModule Configuration
 --------------------------------------------------------------

The LDAP JAAS authentication of EMSCA 8.1 and above uses the LoginModule class
`org.eclipse.jetty.jaas.spi.LdapLoginModule`. The login module dictates
the format of its configuration file which drives the LDAP configuration for 
the module.

To configure the LDAP module, you'll need to specify a file that provides your 
LDAP configuration. In the following example, the LDAP server is a Windows 
Active Directory server:

```
    tibemsca {
        org.eclipse.jetty.jaas.spi.LdapLoginModule required
        debug="true"
        contextFactory="com.sun.jndi.ldap.LdapCtxFactory"
        hostname="10.105.192.61"
        port="389"
        bindDn="CN=Administrator,CN=Users,DC=test,DC=na,DC=tibco,DC=com"
        bindPassword="Tibco123"
        authenticationMethod="simple"
        forceBindingLogin="true"
        userBaseDn="CN=Users,DC=test,DC=na,DC=tibco,DC=com"
        userRdnAttribute="cn"
        userIdAttribute="cn"
        userPasswordAttribute="userPassword"
        userObjectClass="person"
        roleBaseDn="CN=Users,DC=test,DC=na,DC=tibco,DC=com"
        roleNameAttribute="cn"
        roleMemberAttribute="member"
        roleObjectClass="group";
    };
```



LDAP User and Group Configuration
---------------------------------

You LDAP server must define the groups `emsca-admin` and `emsca-guest`. LDAP Users 
that you want to grant access to EMSCA must be members of the appropriate group.

The other important requirement is that `emsca-admin` members in your LDAP server 
must be members of the `$admin` EMS group in the EMS server, as EMS will require them to 
have `$admin` privileges in order to read or deploy configurations from the EMS server.


Note:
   If using LDAP to drive authentication on the EMS server, you'll need to 
   configure EMS separately. The support document `SOL1-DCUJ2G` contains 
   details about how to enable LDAP authentication for the EMS server. 

   The EMSCA JAAS currently only supports JETTY JAAS providers. The JAAS 
   plugin module shipped with EMS is not supported for use with EMSCA.

   For more information on Jetty and JAAS see  <http://wiki.eclipse.org/Jetty/Tutorial/JAAS>.


Configuring EMSCA to use the JAAS configuration
-----------------------------------------------

You can store the LDAP JAAS configuration in the EMSCA configuration file by 
adding the following property:

```
… 
com.tibco.emsca.jaas=/path/to/your/ldap/module/configuration
…
``` 

Alternatively, you can specify the JAAS configuration on the command line while
launching EMSCA:

```
tibemsca --jaas /path/to/your/ldap/module/configuration
```

You can test your authentication configuration by using a browser and logging in to EMSCA.
