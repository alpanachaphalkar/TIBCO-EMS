 =================================================================
 TIBCO Enterprise Message Service
 Copyright (c) 2008-2019 TIBCO Software Inc.
 =================================================================

 This directory contains security plug-in JAAS configuration file samples
 demonstrating the use of TIBCO Enterprise Message Service provided JAAS 
 login modules.

 Using the Samples
 ---------------------------------------------

 To use the samples, modify them as necessary and then reference 
 a sample file with the "jaas_config_file" parameter in the EMS daemon
 main configuration file.

 The samples will need to be modified. The defaults values are setup for
 OpenLDAP; other LDAP implementations may require modifications to filters
 and various attribute parameters due to differing attribute names specific
 to a LDAP server implementation. At a minimum, the LDAP url and root DN
 parameters need to be modified.
 
 Note that use of the debug flag may create security vulnerabilies by 
 revealing information in the server log output. Remember to remove or
 disable the debug parameter after configuration is complete.

 Sample descriptions
 ---------------------------------------------

 ems_hostbased.txt

 An example configuration using the EMS host based login module to 
 authenticate a user based on their connection information using
 an IP, IP mask, or hostname.

 ems_ldap_simple.txt

 An example configuration for the EMS Simple LDAP login module.

 ems_ldap_simple_ft.txt

 An example configuration for the EMS Simple LDAP login module specifying
 a backup or fault tolerant LDAP server in the ldap URL.

 ems_ldap_ssl.txt

 An example configuration for the EMS Simple LDAP login module using SSL.
 The jssecacerts file referenced should be replaced with a keystore
 generated by the java keytool utility.

 ems_ldap_with_groups.txt

 A example of the full featured group user LDAP LoginModule using
 user caching and group membership.

 ems_hostbased_and_ldap.txt

 An example demonstrating stacking the EMS JAAS modules.  The user
 is first authenticated by connection information, succeeding if an
 IP address is the loopback address, or originating from the domain
 "tibco.com".  If the connection is successfully authenticated, then 
 the user is authenticated through LDAP.

 ems_ldap_activedirectory.txt

 An example configuration using the group backlink feature with 
 Active Directory Server configuration parameters.

 ems_ldap_cache_branches.txt

 An example configuration using stacked LDAP modules to authenticate
 users using two different branches of a LDAP server. User caching is
 enabled and the cache instance is shared between the stacked modules for
 performance.  Note that by modifying LDAP urls, this stacking scheme
 could be used to authenticate a user from one of several LDAP servers.

 ems_hostbased_conn_limit.txt

 An example configuration stacking modules to achieve hostbased
 authentication while limiting the number of connections allowed
 from any one hostname.

 ems_ldap_conn_limit.txt

 An example configuration stacking modules to achieve LDAP authentication 
 while limiting the number of connections allowed from any one LDAPID.

 ems_ldap_hostname_conn_limit.txt

 An example configuration stacking modules to achieve LDAP authentication
 while limiting the number of connections allowed from any one combination
 of LDAPID and hostname.
