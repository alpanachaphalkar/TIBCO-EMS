 =================================================================
 Copyright (c) 2001-2019 by TIBCO Software Inc.
 ALL RIGHTS RESERVED

 $Id: readme.txt 109536 2019-04-17 21:09:01Z $
 =================================================================

 This directory contains sample configuration files
 for the TIBCO EMS server.

 tibemsd.conf    
              This is the main server configuration file.
              The tibemsd.conf sample located in this directory
              refers to files users.conf, groups.conf, topics.conf,
              queues.conf, acl.conf, factories.conf, routes.conf,
              bridges.conf, transports.conf, tibrvcm.conf, durables.conf
              and stores.conf.
              While this sample file contains descriptions of the parameters,
              other configuration files (such as tibemsdldap.conf)
              contain only the values.


 users.conf   
              Sample users configuration file


 groups.conf   
              Sample groups configuration file


 topics.conf   
              Sample topics configuration file


 queues.conf   
              Sample queues configuration file


 acl.conf   
              Sample acl configuration file.
              The acl configuration defines permissions of users and groups
              on topics and queues as well as administrator permissions.


 factories.conf   
              Sample factories configuration file.


 routes.conf   
              Sample routes configuration file.


 bridges.conf   
              Sample bridges configuration file.


 transports.conf   
              Sample transports configuration file.


 tibrvcm.conf   
              Sample configuration file to pre-register
              TIBCO Rendezvous Certified Listeners.


 durables.conf
              Sample configuration file with the names and properties of
              statically defined durable topic subscribers.


 stores.conf
              Sample stores configuration file.



 The following are special-purpose configuration files:

 tibemsdssl.conf
              This configuration file demonstrates running tibemsd
              with SSL listen ports. It also shows examples for
              many of the SSL parameters.


 tibemsdft-1.conf
 tibemsdft-2.conf
              These configuration files demonstrate fault tolerance
              capabilities. Both tibemsd instances can be run on
              the same host.


 tibemsdroute-1.conf
 tibemsdroute-2.conf
 topicsroute.conf
 queuesroute1.conf
 queuesroute2.conf
 routes2.conf
              These configuration files demonstrate routing between
              multiple servers. Both tibemsd instances can be run on
              the same host.

              
 tibemsdldap.conf
 tibemsSchema.ldif
              These configuration files demonstrate running tibemsd
              with user authentication performed by LDAP server.


 tibemsd-db.conf
 stores-db.conf
              These configuration files demonstrate using a database
              for storage.


 tibemsd-jaas.conf
 jaas-config.txt
              These configuration files demonstrate using one of the
              pre-built JAAS modules.
