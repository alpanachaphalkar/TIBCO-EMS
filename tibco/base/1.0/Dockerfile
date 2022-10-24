#
# Copyright (c) 2019-2020 TIBCO Software Inc.
# All Rights Reserved. Confidential & Proprietary.
# For more information, please contact:
# TIBCO Software Inc., Palo Alto, California, USA
#
#
# OM BASE DOCKERFILES PROJECT
# ---------------------------------------------------------
# This is the Dockerfile for OM components BASE Docker Image
# 
# HOW TO BUILD THIS IMAGE
# -----------------------
# Put all downloaded files in the same directory as this Dockerfile
# Run: 
#      $ docker build -t tibco/base:1.0 --rm=true .
#
# Pull base image
# ---------------
FROM registry.access.redhat.com/ubi8/ubi-minimal

# COPY jdk-11*_linux-x64*.tar.gz /

ENV PATH=/usr/java/jdk-11.*/bin:/usr/sbin:$PATH \
    TIBCO_HOME=/home/tibuser/tibco \
    OM_HOME=/home/tibuser/tibco/om/5.1

RUN microdnf clean all \
        && microdnf -y update \
        && microdnf -y install wget tar gzip findutils python38 python38-pip\
        && microdnf -y install rsync \
        && microdnf clean all \

RUN pip install gdown \
        && gdown --id 12LLSp3Ud7dwJiXbMPQTbvyQAYFwxbKB8 --output jdk-11*_linux-x64*.tar.gz

RUN mkdir /usr/java \
        && mkdir -p /home/tibuser/tibco/om/5.1 \
        && tar xvf jdk-11*_linux-x64*.tar.gz -C /usr/java \
        && rm jdk-11*_linux-x64*.tar.gz

ENV JAVA_HOME /usr/java/jdk-11.0.4

ENV PATH $JAVA_HOME/bin:$PATH

RUN export JAVA_HOME
	