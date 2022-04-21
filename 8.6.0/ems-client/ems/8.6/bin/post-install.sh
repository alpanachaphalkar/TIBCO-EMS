#!/bin/sh
## 
##
## Copyright (c) 2019-2020 by TIBCO Software Inc.
## ALL RIGHTS RESERVED
##
## $Id: post-install.sh_template 128656 2020-09-17 15:04:08Z $
##
## This script generates a TIBCO Universal Installer product info file so that
## other TIBCO products can check against the presence of the EMS client.
##

SCRIPT=$(basename $0)

# Get the full path to the present script.
SCRIPT_LOCATION="$(cd "$(dirname "$0")"; pwd)"

if [[ $SCRIPT_LOCATION != */ems/8.6/bin ]]
then
    echo "To run $SCRIPT, your EMS installation path needs to end with ems/8.6."
    exit 1
fi

TIBCO_HOME="$(cd "$SCRIPT_LOCATION/../../.."; pwd)"
echo "$SCRIPT using TIBCO_HOME=\"$TIBCO_HOME\""

INSTALL_LOCATION=$TIBCO_HOME/ems/8.6
PRODINFO_FILE_PATH=$TIBCO_HOME/_installInfo/ems-8.6.0.000_prodInfo.xml

mkdir -p "$TIBCO_HOME/_installInfo"
result=$?
[ $result -ne 0 ] && exit 1

# Generate a TIBCO Universal Installer product info file so that other TIBCO products can locate us.
cat > $PRODINFO_FILE_PATH << PRODINFO
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<TIBCOInstallerFeatures>
    <productDef alwaysReinstall="true" buildNumber="V4" buildType="release" docinfoFile="TIB_ems_8.6.0_docinfo.html" hostname="" hotfixDirToBackup="" id="ems" installDir="" licenseFile="TIB_ems_8.6.0_license.pdf" name="TIBCO EMS Enterprise Edition 8.6.0" productMMName="shared" productMMVersion="1.0.0" productMachineModel="true" productType="contributor" readmeFile="" relnotesFile="" secondaryId="" targetPlatform="" timeStamp="" totalRequiredSizeInMB="" version="8.6.0.000" nativeinstaller="true"/>
    <installerFeature installLocation="$INSTALL_LOCATION" name="EMS Server Baseline" parentID="ems" version="8.6.0">
        <assemblyList>
            <assembly displayName="product_tibco_ems_server" installLocation="$INSTALL_LOCATION" uid="product_tibco_ems_server" version="8.6.0.004"/>
            <assembly displayName="TIBCO EMS Runtime" installLocation="$INSTALL_LOCATION" uid="product_tibco_ems_runtime" version="8.6.0.004"/>
        </assemblyList>
        <dependency description="TIBCO EMS Feature" mustBeInstalled="false" type="feature" uid="EMS Client Jars_ems" version="8.6.0.0" vpduid=""/>
        <dependency description="TIBCO EMS Feature" mustBeInstalled="false" type="feature" uid="EMS conf2JSON Converter_ems" version="8.6.0.0" vpduid=""/>
        <dependency description="TIBCO EMS Feature" mustBeInstalled="false" type="feature" uid="EMS ThirdParty_ems" version="8.6.0.0" vpduid=""/>
        <dependency description="TIBCO EMS Feature" mustBeInstalled="false" type="feature" uid="EMS Central Administration_ems" version="8.6.0.0" vpduid=""/>
    </installerFeature>
    <installerFeature installLocation="$INSTALL_LOCATION" name="Required" parentID="ems" version="8.6.0">
        <assemblyList>
            <assembly displayName="TIBCO EMS Common" installLocation="$INSTALL_LOCATION" uid="product_tibco_ems_common" version="8.6.0.004"/>
        </assemblyList>
    </installerFeature>
    <installerFeature installLocation="$INSTALL_LOCATION" name="EMS Client Baseline" parentID="ems" version="8.6.0">
        <assemblyList>
            <assembly displayName="product_tibco_ems_64bit_client_runtime" installLocation="$INSTALL_LOCATION" uid="product_tibco_ems_64bit_client_runtime" version="8.6.0.004"/>
            <assembly displayName="product_tibco_ems_64bit_client_ufo_runtime" installLocation="$INSTALL_LOCATION" uid="product_tibco_ems_64bit_client_ufo_runtime" version="8.6.0.004"/>
        </assemblyList>
        <dependency description="TIBCO EMS Feature" mustBeInstalled="false" type="feature" uid="EMS ThirdParty_ems" version="8.6.0.0" vpduid=""/>
        <dependency description="TIBCO EMS Feature" mustBeInstalled="false" type="feature" uid="EMS Client Jars_ems" version="8.6.0.0" vpduid=""/>
    </installerFeature>
    <installerFeature installLocation="$INSTALL_LOCATION" name="EMS Client Jars" parentID="ems" version="8.6.0">
        <assemblyList>
            <assembly displayName="product_tibco_ems_runtime_ufo_common" installLocation="$INSTALL_LOCATION" uid="product_tibco_ems_runtime_ufo_common" version="8.6.0.004"/>
            <assembly displayName="TIBCO EMS Common Runtime" installLocation="$INSTALL_LOCATION" uid="product_tibco_ems_runtime_common" version="8.6.0.004"/>
        </assemblyList>
    </installerFeature>
    <installerFeature installLocation="$INSTALL_LOCATION" name="EMS ThirdParty" parentID="ems" version="8.6.0">
        <assemblyList>
            <assembly displayName="product_tibco_ems_64bit_thirdparty_runtime" installLocation="$INSTALL_LOCATION" uid="product_tibco_ems_64bit_thirdparty_runtime" version="8.6.0.004"/>
        </assemblyList>
        <dependency description="TIBCO EMS Feature" mustBeInstalled="false" type="feature" uid="Required_ems" version="8.6.0.0" vpduid=""/>
    </installerFeature>
    <installerFeature installLocation="$INSTALL_LOCATION" name="EMS Central Administration" parentID="ems" version="8.6.0">
        <assemblyList>
            <assembly displayName="product_tibco_ems_ca_samples" installLocation="$INSTALL_LOCATION" uid="product_tibco_ems_ca_samples" version="8.6.0.004"/>
            <assembly displayName="product_tibco_ems_ca_runtime_common" installLocation="$INSTALL_LOCATION" uid="product_tibco_ems_ca_runtime_common" version="8.6.0.004"/>
            <assembly displayName="product_tibco_ems_ca_runtime" installLocation="$INSTALL_LOCATION" uid="product_tibco_ems_ca_runtime" version="8.6.0.004"/>
        </assemblyList>
    </installerFeature>
    <installerFeature installLocation="$INSTALL_LOCATION" name="EMS conf2JSON Converter" parentID="ems" version="8.6.0">
        <assemblyList>
            <assembly displayName="product_tibco_ems_converter" installLocation="$INSTALL_LOCATION" uid="product_tibco_ems_converter" version="8.6.0.004"/>
        </assemblyList>
    </installerFeature>
    <customSettings enableTibcoHomeConfigDirectory="true"/>
    <installationInfo mode="console" system.java.version="1.8.0_192" uiversion="3.6.6.001"/>
    <customVariables>
        <variable name="ems.version" value="8.6.0"/>
        <variable name="ems.shortversion" value="8.6"/>
        <variable name="ems.home" value="$INSTALL_LOCATION"/>
    </customVariables>
</TIBCOInstallerFeatures>
PRODINFO
result=$?

[ $result -ne 0 ] && exit 1

echo "File \"$PRODINFO_FILE_PATH\" generated."

exit 0
