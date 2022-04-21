#!/bin/sh

#
# Copyright (c) 2001-2020 by TIBCO Software Inc.
# ALL RIGHTS RESERVED
#
# $Id: pcthreads.sh 129199 2020-10-07 22:55:14Z $
#

pushd ..

# run a given dest, producer mode and consumer mode while changing thread count
java tibjmsPerfController -server ${TIBEMS_SERVER} -output ${TIBEMS_OUTPUT} ${TIBEMS_DEST} ${TIBEMS_MODE} ${TIBEMS_SIZE} -prodthrds   1 -consthrds   1 -time 60
java tibjmsPerfController -server ${TIBEMS_SERVER} -output ${TIBEMS_OUTPUT} ${TIBEMS_DEST} ${TIBEMS_MODE} ${TIBEMS_SIZE} -prodthrds  10 -consthrds  10 -time 60
java tibjmsPerfController -server ${TIBEMS_SERVER} -output ${TIBEMS_OUTPUT} ${TIBEMS_DEST} ${TIBEMS_MODE} ${TIBEMS_SIZE} -prodthrds  20 -consthrds  20 -time 60
java tibjmsPerfController -server ${TIBEMS_SERVER} -output ${TIBEMS_OUTPUT} ${TIBEMS_DEST} ${TIBEMS_MODE} ${TIBEMS_SIZE} -prodthrds  30 -consthrds  30 -time 60
java tibjmsPerfController -server ${TIBEMS_SERVER} -output ${TIBEMS_OUTPUT} ${TIBEMS_DEST} ${TIBEMS_MODE} ${TIBEMS_SIZE} -prodthrds  40 -consthrds  40 -time 60
java tibjmsPerfController -server ${TIBEMS_SERVER} -output ${TIBEMS_OUTPUT} ${TIBEMS_DEST} ${TIBEMS_MODE} ${TIBEMS_SIZE} -prodthrds  50 -consthrds  50 -time 60
java tibjmsPerfController -server ${TIBEMS_SERVER} -output ${TIBEMS_OUTPUT} ${TIBEMS_DEST} ${TIBEMS_MODE} ${TIBEMS_SIZE} -prodthrds  60 -consthrds  60 -time 60
java tibjmsPerfController -server ${TIBEMS_SERVER} -output ${TIBEMS_OUTPUT} ${TIBEMS_DEST} ${TIBEMS_MODE} ${TIBEMS_SIZE} -prodthrds  70 -consthrds  70 -time 60
java tibjmsPerfController -server ${TIBEMS_SERVER} -output ${TIBEMS_OUTPUT} ${TIBEMS_DEST} ${TIBEMS_MODE} ${TIBEMS_SIZE} -prodthrds  80 -consthrds  80 -time 60
java tibjmsPerfController -server ${TIBEMS_SERVER} -output ${TIBEMS_OUTPUT} ${TIBEMS_DEST} ${TIBEMS_MODE} ${TIBEMS_SIZE} -prodthrds  90 -consthrds  90 -time 60
java tibjmsPerfController -server ${TIBEMS_SERVER} -output ${TIBEMS_OUTPUT} ${TIBEMS_DEST} ${TIBEMS_MODE} ${TIBEMS_SIZE} -prodthrds 100 -consthrds 100 -time 60

popd
mv ../${TIBEMS_OUTPUT} .
