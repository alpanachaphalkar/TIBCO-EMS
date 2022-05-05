#!/bin/sh

##
## Copyright (c) 2015-2015 by TIBCO Software Inc.
## ALL RIGHTS RESERVED
##
## $Id: setup.sh 80855 2015-08-06 16:20:24Z $
##
## This setup file can be used to setup the environment for 
## compiling the provided JAAS modules
## 

## run via '. setup.sh'
## 
## Set TIBEMS_ROOT to the root of your installation of
## TIBCO Enterprise Message Service software
## 

if [ -z "${TIBEMS_ROOT}" ]
then
    TIBEMS_ROOT=../../..
fi

## 
## You should not need to change the text below
## 

TIBEMS_JAVA=${TIBEMS_ROOT}/lib

if [ ! -f ${TIBEMS_JAVA}/jms-2.0.jar ]
then
    echo "${TIBEMS_JAVA}/jms-2.0.jar file not found."
    return
fi

if [ ! -f ${TIBEMS_JAVA}/tibjms.jar ]
then
    echo "${TIBEMS_JAVA}/tibjms.jar file not found."
    return
fi

if [ ! -f ${TIBEMS_JAVA}/tibemsd_sec.jar ]
then
    echo "${TIBEMS_JAVA}/tibemsd_sec.jar file not found."
    return
fi

if [ ! -f ${TIBEMS_JAVA}/tibjmsadmin.jar ]
then
    echo "${TIBEMS_JAVA}/tibjmsadmin.jar file not found."
    return
fi

CLASSPATH=$CLASSPATH:${TIBEMS_JAVA}/tibjmsadmin.jar
CLASSPATH=$CLASSPATH:${TIBEMS_JAVA}/tibjms.jar
CLASSPATH=$CLASSPATH:${TIBEMS_JAVA}/jms-2.0.jar
CLASSPATH=$CLASSPATH:${TIBEMS_JAVA}/tibemsd_sec.jar

export CLASSPATH
