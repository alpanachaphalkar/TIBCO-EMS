 =================================================================
 TIBCO Enterprise Message Service
 Copyright (c) 2001-2018 TIBCO Software Inc.
 ALL RIGHTS RESERVED
 =================================================================

This file describes the JAAS configuration to integrate with the JAAS
properties file module provided by Jetty. The included `emsca.jaas` file contains a
sample configuration for the `org.eclipse.jetty.jaas.spi.PropertyFileLoginModule`
JAAS module.

This module defines users that can access EMSCA in the file `users.txt`.
The sample users defined are `guest`, `admin`, `all`, and `nogroup`. With the respective
passwords `guest-pw`, `admin-pw`, `all-pw`, and `nogroup-pw`.

Users `admin` and `all` have administrative privileges in EMSCA. They can add or delete
emsd servers to emsca. They can also edit configurations and deploy them.

Users `guest`, `all` also have guest privileges in EMSCA, which allow them to
only view configurations. Guest users cannot modify EMSCA, nor can they edit
or deploy ems configurations.

Note that the `all` user is a trick user showing that JAAS can have users be in
both admin and guest roles. If logging in, the more permissive role is used, that of admin.

For completeness' sake, the `nogroup` user does not have emsca-admin or emsca-guest roles,
so it will not be allowed to login into EMSCA.

To use the sample, edit `emsca.jaas` and modify the `file="/path/to/jaas/users.txt";` line to
specify a correct location of the users.txt file in your file system. If running on Windows,
specify your windows path using forward slashes as in `c:/path/to/jaas/users.txt`.

After the file has been edited, start emsca with the option `--jaas` followed by the path to 
the `emsca.jaas` file. Again, on Windows, specify the path using forward slashes.


 
EMSCA 8.1+ Properties JAAS Authentication
=========================================


The security mechanism of EMSCA 8.1 and above relies on Java Authentication and 
Authorization Service JAAS LoginModules 
<http://docs.oracle.com/javase/1.5.0/docs/guide/security/jaas/JAASLMDevGuide.html>.
For information on how to enable authentication in EMSCA 8.x in general, 
please refer to SOL1-E4BALL, and your EMSCA documentation.


EMSCA JAAS Configuration
------------------------

In order to determine if a user should have access to the administration
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



org.eclipse.jetty.jaas.spi.PropertyFileLoginModule Configuration
---------------------------------------------------------------------

EMSCA 7.0 and 8.0 Properties file JAAS authentication uses the LoginModule class
`org.eclipse.jetty.jaas.spi.PropertyFileLoginModule`. The login module 
dictates the format of its configuration file which drives the configuration 
for the module.

To configure the properties file module, you'll need to specify a file that 
provides your configuration. In the following example, JAAS is configured 
to use the properties file module. It also specifies the path to a file 
containing the username, role and password that the module will use to authenticate.

```
tibemsca {
       org.eclipse.jetty.jaas.spi.PropertyFileLoginModule required
       debug="true"
       file="/path/to/credentials/file";
};
```


Credentials File
----------------

The credentials file provides all the logins, roles and password information 
to the module. The format of the file is:

```
username: password, roles\n
```

Here's an example:

```
guest:     guest-pw,                             emsca-guest
admin:     admin-pw,                             emsca-admin
test:      test1234,                             emsca-admin
obfus:     OBF:1xtn1vn01thb1vo21xtv,             emsca-admin
md5:       MD5:1bc29b36f623ba82aaf6724fd3b16718, emsca-admin
```

    Note that the password field appears in cleartext for the first 3 entries.
    The last 2 entries each obfuscate or hash the password so that it is not
    user readable.


Obfuscating and Hashing JAAS Passwords
--------------------------------------

The passwords in the credentials file can be stored in clear text, obfuscated or 
encrypted. To generate a non-clear text password you'll need to use a jetty utility.
The jetty utility command is:

```
java -cp /path/to/jetty-all.jar java org.eclipse.jetty.util.security.Password [<user>] <password>
```

The username is optional. The tool will output the cleartext, obfuscated (OFB),
 and hashed (MD5) passwords. Copy and paste the version you want and put it in 
 the file. Note that for non-clear text passwords, the `OBF` and `MD5` prefixes 
 let the module know how to deal with the password and must be present in the file.

```
java -cp /path/to/jetty-all.jar org.eclipse.jetty.util.security.Password admin adminpassword 
adminpassword 
OBF:1s3g1vg11wn11xf51xmi1y0s1ri71y0y1xms1xfx1wn51vgt1s3m
MD5:e3274be5c857fb42ab72d786e281b4b8
```
    Note that the mangle functionality cannot be used in the JAAS credentials file.

EMSCA Group Configuration
-------------------------

You JAAS credential file must reference the groups `emsca-admin` and/or 
`emsca-guest`. Users that you want to grant access to EMSCA must 
be members of the appropriate group.

The other important requirement is that `emsca-admin` members in your 
credentials file must be members of the `$admin` EMS group in the EMS server,
as EMS will require them to have `$admin` privileges in order to read 
or deploy configurations from the EMS server.

    Note that EMS authentication is separate from any other source of
    authentication that the EMS server may use.


Configuring EMSCA to use the JAAS configuration
-----------------------------------------------

You can store the LDAP JAAS configuration in the EMSCA configuration file
 by adding the following property:

```
… 
com.tibco.emsca.jaas=/path/to/your/properties/module/configuration
…
``` 

Alternatively, you can specify the JAAS configuration on the command
line while launching EMSCA:

```
tibemsca --jaas /path/to/your/properties/module/configuration
```

You can test your authentication configuration by using a browser and 
logging into EMSCA.
