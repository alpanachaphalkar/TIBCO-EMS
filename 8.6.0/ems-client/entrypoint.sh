#!/usr/bin/env bash
#set -x

if [[ $INIT_FLAG = 'true' ]]
then
   
    if [[ -z $env_ems_password ]]
    then

        for FILE in $TIBCO_HOME/ems_scripts/*.txt; do  
            echo $FILE 
            tibemsadmin -server tcp://${env_ems_svc}:${env_ems_svc_port} -user ${env_ems_user} -ignore -script $FILE 
	done
        exit_status=$?
               
    else
        for FILE in $TIBCO_HOME/ems_scripts/*.txt; do
            echo $FILE
            tibemsadmin -server tcp://${env_ems_svc}:${env_ems_svc_port} -user ${env_ems_user} -ignore -password ${env_ems_password} -script $FILE 
        done
        exit_status=$?
    fi
    if [[ $exit_status != 0 ]]
    then
    	echo "EMS init failed."
        exit $exit_status
    fi
    echo "EMS init completed."
else
    echo "EMS init not executed."
fi
exit 0
