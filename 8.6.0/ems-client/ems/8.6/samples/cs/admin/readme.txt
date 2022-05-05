 =================================================================
 Copyright (c) 2017-2019 TIBCO Software Inc. 
 ALL RIGHTS RESERVED

 $Id: readme.txt 108984 2019-04-03 23:17:44Z $
 =================================================================

 This directory contains .NET administration samples for TIBCO EMS that can
 be built and run either with .NET Framework or .NET Core.

 .NET Framework
 =================================================================

 In order to build and run the samples for .NET Framework, you need to install
 .NET Framework and either Microsoft Visual Studio with the .NET Framework
 Developer Pack or the .NET Core SDK and make sure the 'dotnet' tool has been
 placed in your PATH.

 Compiling and running the samples
 -----------------------------------------------------------------

 1. Go to the samples\cs\admin directory of your TIBCO EMS installation.

 2. Execute:
    dotnet build AdminSamples.sln -f <target framework>
    where <target framework> is a supported version of .NET Framework.

    For example:
    dotnet build AdminSamples.sln -f net472
    
    You can also build an individual program:
    dotnet build csServer.csproj -f <target framework>
   
 3. Make sure the TIBCO EMS server (tibemsd) is running.

 4. You can now run the samples from their location in the build output
    directory.

    For example:
    bin\x64\Release\net472\csServer.exe

    If the server is running on a different computer, use the -server parameter
    when running samples. For example, if the server is running on a computer
    named mainhost, then run the samples using the following command:

    <path to program>\csServer.exe -server tcp://mainhost:7222

 Shared Assemblies
 -----------------------------------------------------------------

 When the EMS assemblies are not in the Global Assembly Cache (GAC), they
 must be in the same directory as any executable that uses them.
 When these assemblies are installed in the GAC, they are available for all
 applications on the machine. This is only applicable to .NET Framework.

 The TIBCO EMS installer installs the EMS assemblies in the GAC by default.

 While the 'dotnet build' command copies all the necessary assemblies in the
 build output directory, you might want to rely on the GAC instead in
 particular situations.

 This section describes how to install and uninstall the EMS assemblies
 through a script or manually. Two scripts (register.bat and unregister.bat)
 have been provided in this sample directory to perform the
 installation/uninstallation of the EMS assemblies into/from the GAC.
 You should verify that "gacutil.exe" is installed on your computer and that
 the above batch files hold the correct path to gacutil.exe before running
 them. gacutil.exe is usually located in
 "C:\Program Files (x86)\Microsoft SDKs\Windows\<version>\bin\NETFX <version> Tools\".

 To install the assemblies in the GAC using the batch file provided:

 . Go to the samples\cs\admin directory of your TIBCO EMS installation
   and execute:
   
   register.bat

 To uninstall the assembly from GAC using the batch file provided:

 . Go to the samples\cs\admin directory of your TIBCO EMS installation
   and execute:
   
   unregister.bat

 To install the assemblies into the GAC manually:

 1. Verify that "gacutil.exe" is installed on your computer.

 2. Execute:

    <directory containing gacutils.exe>\gacutil.exe /i <path to bin directory>\TIBCO.EMS.dll
    <directory containing gacutils.exe>\gacutil.exe /i <path to bin directory>\TIBCO.EMS.UFO.dll
    <directory containing gacutils.exe>\gacutil.exe /i <path to bin directory>\TIBCO.EMS.ADMIN.dll

 To uninstall the assemblies from the GAC manually:

 1. Verify that "gacutil.exe" is installed on your computer.

 2. Execute:

    <directory containing gacutils.exe>\gacutil.exe /u TIBCO.EMS
    <directory containing gacutils.exe>\gacutil.exe /u TIBCO.EMS.UFO
    <directory containing gacutils.exe>\gacutil.exe /u TIBCO.EMS.ADMIN

 .NET Core
 =================================================================

 In order to build and run the samples for .NET Core, you need to install the
 .NET Core SDK and make sure the 'dotnet' tool has been placed in your PATH.

 Compiling and running the samples
 -----------------------------------------------------------------

 1. Go to the samples/cs/admin directory of your TIBCO EMS installation.

 2. Execute:
    dotnet build AdminSamples.sln -f <target framework>
    where <target framework> is a supported version of .NET Core.

    For example:
    dotnet build AdminSamples.sln -f netcoreapp2.1
    
    Omitting the -f option will build for both .NET Core and .NET Framework,
    only on Windows. This is not supported on Linux.
    
    You can also build an individual program:
    dotnet build csServer.csproj -f <target framework>
   
 3. Make sure the TIBCO EMS server (tibemsd) is running.

 4. You can now run the samples from their location in the build output
    directory.

    For example:
    dotnet bin/x64/Release/netcoreapp2.1/csServer.dll

    If the server is running on a different computer, use the -server parameter
    when running samples. For example, if the server is running on a computer
    named mainhost, then run the samples using the following command:

    dotnet <path to program>/csServer.dll -server tcp://mainhost:7222

 Sample Programs Description
 =================================================================

 The samples programs located in this directory are simple examples of basic
 EMS functionality.

 All samples accept the '-server' command line parameter, which specifies
 the URL of a running instance of the TIBCO EMS server (tibemsd).
 By default, all samples try to connect to the server running on the
 local computer with default port 7222. The server URL is usually
 specified in the form '-server "tcp://hostname:port"'.

 All samples accept the parameters -user and -password. You may need to
 use these parameters when running samples against a server with
 authorization enabled.
