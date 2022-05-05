 =================================================================
 TIBCO Enterprise Message Service
 Copyright (c) 2008-2013 TIBCO Software Inc.
 =================================================================

 This directory and its subdirectories contain security plugin samples for 
 TIBCO Enterprise Message Service.

 Samples located in this directory are simple examples for demonstrating basic
 concepts of extensible security (JAAS and JACI module) in TIBCO Enterprise Message Service.

 Compiling and running samples.
 ---------------------------------------------

 In order to compile and run samples you need to execute
 setup.bat (Windows) or setup.sh (UNIX) script located in
 this directory. You may need to change the script to reflect
 your installation of TIBCO Enterprise Message Service software.
 Please read the comments inside the script file. Normally you
 don't need to change it if you installed TIBCO Enterprise Message Service
 into the default directory.

 To compile and run sample security modules, do the following steps:

 1. Verify the setting of TIBEMS_ROOT environment variable inside
    the setup.bat or setup.sh script file.

 2. Make sure JavaMail API is on your classpath. 
    (See example ConfFileUserAuthLoginModule for more information.)

 3. Open console window and change directory to the samples/security
    subdirectory of your TIBCO Enterprise Message Service installation.

 4. run "setup" script.

 5. execute:

    javac com/tibco/example/*.java
    
    jar cf SecurityPlugins.jar com/tibco/example/*.class

 6. Make sure security-related parameters are correctly setup in server configuration file 
    (tibemsd.conf). An example of server configuration file, tibemsd-jaas.conf, is given 
    in this directory. This sample configuration file makes use of the FlatFile authentication
    and authorization modules. For detailed information, please refer to users_guide document.
    The remainder of this step by step tutorial uses the FlatFile plugins. 

 7. Start TIBCO Enterprise Message Service server (tibemsd) with the jaas configuration file,
    ensuring that you remain in the samples/security directory.

 8. Now you can run some samples to test authentication and authorization provided by
    the sample FlatFileUserAuthLoginModule and FlatFilePermissionModule. Please notice that
    the secure property for destinations to be tested should be set for the test of JACI module.

    Suppose you have built java samples, execute:

    java tibjmsMsgProducer -topic test.1 -user Russ -password photos00 hello
    
    This sample will successfully complete. However, when you execute:
    
    java tibjmsMsgConsumer -topic test.1 -user Russ -password photos00
    
    It will fail. This is because the user Russ has only TOPIC_PUBLISH right for any destination,
    which is specified in permission.txt.
    
    You can observe the reverse result for user Colin.
    
    For user Bob, both samples can successfully complete. However, the destination must be 
    child of test.*.
    
    Both users, Colin and Russ, have cache timeout set in permission.txt. You can 
    observe the cache effect through the output message of tibemsd.

Samples description:
---------------------------------------------

FlatFileUserAuthLoginModule

      An example of Java Authentication and Authorization Service (JAAS) module to 
      provide authentication based on a userpass text file, which includes username and 
      password. See the sample file userpass.txt in this directory.
      Plugin class: com.tibco.example.FlatFileUserAuthLoginModule
      JAAS Config Options: 
          debug=<true/false>
          filename="<location of the flat file>"

FlatFilePermissionModule

      An example of Java Access Control Interface (JACI) module to provide authorization
      based on a permission text file, which includes username, destination, actions, 
      and optional cache timeout. See the sample file permission.txt in this directory.
      Plugin class: com.tibco.example.FlatFilePermissionModule
      JRE Options: 
          example.permission.file=<location of the permissions file>
          example.permission.debug=<true/false>

ConfFileUserAuthLoginModule

      An example of Java Authentication and Authorization Service (JAAS) module to 
      provide authentication based on a file that complies with users.conf.
      For detailed information, please refer to users_guide document.
      This example module reloads the content of the file once the file changes.
      Therefore, administrative changes to users.conf will be noticed.
      This sample depends on the package javax.mail (JavaMail API). 
      The JavaMail API is an optional package for Java SE and has to be downloaded 
      separately. Java EE includes this package by default.
      (http://www.oracle.com/technetwork/java/javamail/index.html)
      In case of problems make sure this API can be found on the classpath.
      (Configuration option jaas_classpath for runtime problems.)
      Plugin class: com.tibco.example.ConfFileUserAuthLoginModule
      JAAS Config Options: 
          debug=<true/false>
          filename="<location of an users.conf file>"

LDAPSearchLoginModule - DEPRECATED

      This module has been deprecated.  Use com.tibco.ems.com.tibco.tibems.tibemsd.security.jaas.LDAPSimpleAuthentication
      instead.

      The module binds to the LDAP server, searches for the user name supplied by the EMS client, 
      and then attempts to bind to the LDAP server again using the results of the search.
      Plugin class: com.tibco.example.LDAPSearchLoginModule
      JAAS Config Options:
          ems_ldap.provider="<Class name of the JNDI context factory.>"
          ems_ldap.url="<URL of the LDAP server.>"
          ems_ldap.binding_name="<Name to bind with initially.>"
          ems_ldap.binding_password="<Password for initial bind.>"
          ems_ldap.user_base_dn="<Base path for the LDAP search.>"
          ems_ldap.user_attribute="<Attribute that will be compared to the user name for the search.>"
          ems_ldap.filter="<The filter used in the search. 
                            By default, a filter is created using the 'ems_ldap.user_attribute' property.
                            If a more complex filter is needed, use this property to override the default. 
                            Any occurrence of '{1}' in the search string will be replaced with the user name.>"
          ems_ldap.scope="<The scope of the search. Possible values are 'subtree' or 'onelevel'. 
                           By default, 'onelevel' is used.>"
          ems_ldap.retries="<Number of retries on communication failure.>"
          ems_ldap.retry_delay="<Number of milliseconds to wait until retry.>"
          ems_ldap.certificate="<Keystore that will be used to establish trust for SSL connections.>"
          ems_ldap.cache_enabled="<Enables caching of LDAP data.>"
          ems_ldap.cache_ttl="<Maximum time (in seconds) that cached LDAP data is retained before refresh.>"
          ems_ldap.debug=<true/false>

LDAPDirectLoginModule - DEPRECATED.

      This module has been deprecated.  Use com.tibco.ems.com.tibco.tibems.tibemsd.security.jaas.LDAPSimpleAuthentication
      instead.

      This class implements a very basic form of LDAP login for EMS. It will validate all connections
      (of users, routes, etc.) by authenticating to the LDAP server as that user.
      The user names must be fully qualified DNs, unless a user name pattern is given. 
      The option 'example.user_pattern' may contain a pattern string. 
      The actual DN used for the lookup will be this pattern string, 
      with '%u' replaced with the user name provided by the interface.
      Plugin class: com.tibco.example.LDAPDirectLoginModule
      JAAS Config Options:
          example.ldap_url="<URL of the LDAP server.>"
          example.user_pattern="<DN containing the string '%u' (later replaced by username).>"
          <Any property not prefixed with 'example.' will be passed to JNDI.>="<Any value>"

LDAPSearchLoginModule cooperating with GroupFilePermissionModule - DEPRECATED

      This module has been deprecated.  Use com.tibco.ems.com.tibco.tibems.tibemsd.security.jaas.LDAPGroupUserAuthentication
      instead.

      LDAPSearchLoginModule:
      The module binds to the LDAP server, searches for the user name supplied by the EMS client, 
      and then attempts to bind to the LDAP server again using the results of the search.
      When the option ems_ldap.group_attribute is present, it will also search for the users's groups
      and make them available to GroupFilePermissionModule.
      PLEASE NOTE that a user's groups will only be updated upon successful login.
      
      GroupFilePermissionModule:
      The module correlates the user's groups provided by LDAPSearchLoginModule
      and the groups specified in the specified group permission file.
      The group permission file is a simple text file in which each line contains 
      a group name, destination, actions, and an optional cache timeout. 
      Cache timeout determines how long results from previous Permissions Module queries are cached.
      It has no impact on the groups a user is a member of. See the sample group permission file
      permission_group.txt in this directory.

      JAAS Plugin class: com.tibco.example.LDAPSearchLoginModule
      JAAS Config Options:
          ems_ldap.provider="<Class name of the JNDI context factory.>"
          ems_ldap.url="<URL of the LDAP server.>"
          ems_ldap.binding_name="<Name to bind with initially.>"
          ems_ldap.binding_password="<Password for initial bind.>"
          ems_ldap.user_base_dn="<Base path for the LDAP search.>"
          ems_ldap.user_attribute="<Attribute that will be compared to the user name for the search.>"
          ems_ldap.filter="<The filter used in the search. 
                            By default, a filter is created using the 'ems_ldap.user_attribute' property.
                            If a more complex filter is needed, use this property to override the default. 
                            Any occurrence of '{1}' in the search string will be replaced with the user name.>"
          ems_ldap.scope="<The scope of the search for users and their groups.
                           Possible values are 'subtree' or 'onelevel'. By default, 'onelevel' is used.>"
          ems_ldap.retries="<Number of retries on communication failure.>"
          ems_ldap.retry_delay="<Number of milliseconds to wait until retry.>"
          ems_ldap.certificate="<Keystore that will be used to establish trust for SSL connections.>"
          ems_ldap.cache_enabled="<Enables caching of LDAP data.>"
          ems_ldap.cache_ttl="<Maximum time (in seconds) that cached LDAP data is retained before refresh.>"
          ems_ldap.debug=<true/false>
          ems_ldap.group_attribute="<The attribute of a Group that contains the group name.>"
          ems_ldap.group_base_dn="<Base path for the LDAP group search.>"
          ems_ldap.group_member_attribute="<The attribute of an LDAP group object that specifies the 
                                            distinguished names (DNs) of the members of the group.
                                            By default, 'uniquemember' is used.>"
          ems_ldap.group_filter="<The filter used in the search. 
                                  By default, a filter is created using the ems_ldap.group_member_attribute
                                  property. If a more complex filter is needed, use this property to override
                                  the default.  Any occurrence of '{1}' in the search string will be replaced
                                  with the user name.>"
      JACI Plugin class: com.tibco.example.GroupFilePermissionModule
      JACI JRE Options: 
          example.group.permission.file=<location of the group permissions file>
          example.group.permission.debug=<true/false>
