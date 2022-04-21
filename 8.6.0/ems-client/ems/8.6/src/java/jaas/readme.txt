/*
 * Copyright (c) 2013-2017 TIBCO Software Inc.
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 *
 * $Id: readme.txt 93176 2017-04-27 23:34:07Z $
 *
 */
 
TIBCO EMS JAAS LoginModule Customization

TIBCO EMS Supports authentication and authorization through supplied
JAAS LoginModules.  Source files are provided, allowing customization
and extension of functionality.

Compiling the EMS JAAS modules
---------------------------------------------

At the minimum, to compile the EMS JAAS modules you need to first
set up your java classpath to include the following jar files:

<EMS install directory>/lib/tibjmsadmin.jar
<EMS install directory>/lib/tibjms.jar
<EMS install directory>/lib/jms-2.0.jar
<EMS install directory>/lib/tibemsd_sec.jar

The setup.sh or setup.bat script will set up this classpath for you
once the proper installation path has been provided as mentioned
in step 4 below.

Be sure to include any additional jar files that may be required
by modifications to the source.

To compile, follow the following steps:

1. Make sure your computer has Java 1.8 installed.

2. Open console window and change directory to 
   <EMS install directory>/src/java/jaas
   
3. Modify the source files as required, or use them as
   templates to create an entirely new JAAS login module.

4. Modify the setup.sh (or setup.bat for Windows).

5. execute:

   . setup.sh (or setup.bat on Windows)

6. execute:

   javac -d . *.java

   This command compiles the JAAS login module source files.

7. execute:

   jar -cvf modified_ems_jaas.jar com
   
   This command creates the java archive for use in the 
   EMS server.

8. Create a JAAS configuration file and modify the EMS
   server configuration as described in the EMS User's
   guide. Add the modified_ems_jaas.jar to the
   security_classpath.  While required to compile, note
   that the tibemsd_sec.jar file is not required here as
   it will be included by the EMS server at runtime.
   
   For example, replacing <EMS install directory> with the EMS 
   installation directory in the classpath below would
   create a valid security_classpath for the installed
   source files.

   security_classpath = <EMS install directory>/src/java/jaas/modified_ems_jaas.jar:<EMS install directory>/lib/tibjmsadmin.jar:<EMS install directory>/lib/tibjms.jar:<EMS install directory>/lib/jms-2.0.jar

   Be sure to include any additional jar files that modifications
   may require.

9. Launch the EMS server and test any modifications. It
   may be helpful to enable JAAS module debugging with the 
   debug="true" parameter in the JAAS configuration.  Always
   remember to disable JAAS Module debugging before placing an 
   EMS server into production.
