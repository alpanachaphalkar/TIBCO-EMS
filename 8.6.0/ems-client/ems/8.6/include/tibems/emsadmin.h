/* 
 * Copyright (c) 2007-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: emsadmin.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibems_emsadmin_h
#define _INCLUDED_tibems_emsadmin_h

#include "types.h"
#include "admtypes.h"
#include "status.h"
#include "conn.h"
#include "destinfo.h"
#include "qinfo.h"
#include "tinfo.h"
#include "statdata.h"
#include "prodinfo.h"
#include "consinfo.h"
#include "subinfo.h"
#include "deststat.h"
#include "collect.h"

#if defined(__cplusplus)
extern "C" {
#endif

extern tibems_status
tibemsAdmin_Create(
    tibemsAdmin*                admin,
    const char*                 url,
    const char*                 userName,
    const char*                 password, 
    tibemsSSLParams             sslParams);

extern tibems_status
tibemsAdmin_Close(
    tibemsAdmin                 admin);

extern tibems_status
tibemsAdmin_GetInfo(
    tibemsAdmin                 admin,
    tibemsServerInfo*           serverInfo);

extern tibems_status
tibemsAdmin_GetConsumer(
    tibemsAdmin                 admin,
    tibemsConsumerInfo*         consInfo,
    tibems_long                 consumerID);

extern tibems_status
tibemsAdmin_GetConsumers(
    tibemsAdmin                 admin,
    tibemsCollection*           consInfos,
    tibems_long                 connectionID,
    const char*                 username,
    tibemsDestinationInfo       destination,
    tibems_bool                 durable,
    tibems_int                  dataFlags);

extern tibems_status
tibemsAdmin_GetSubscriptions(
    tibemsAdmin                 admin,
    tibemsCollection*           subscriptionInfos,
    tibems_int                  filterFlags,
    const char*                 name,
    const char*                 topicName);

extern tibems_status
tibemsAdmin_GetDestination(
    tibemsAdmin                 admin,
    tibemsDestinationInfo*      destInfo,
    const char*                 destName,
    tibemsDestinationType       destType);

#define tibemsAdmin_GetQueue(admin, queueInfo, name) \
        tibemsAdmin_GetDestination((admin), (queueInfo), (name), (TIBEMS_QUEUE))

#define tibemsAdmin_GetTopic(admin, topicInfo, name) \
        tibemsAdmin_GetDestination((admin), (topicInfo), (name), (TIBEMS_TOPIC))

extern tibems_status
tibemsAdmin_GetDestinations(
    tibemsAdmin                 admin,
    tibemsCollection*           collection,
    const char*                 pattern,
    tibemsDestinationType       destType,
    tibems_int                  permType,
    tibems_bool                 statOnly);

#define tibemsAdmin_GetQueues(admin, collection, pattern, permType) \
        tibemsAdmin_GetDestinations((admin), (collection), (pattern), \
                                    (TIBEMS_QUEUE), (permType), (TIBEMS_FALSE))

#define tibemsAdmin_GetTopics(admin, collection, pattern, permType) \
        tibemsAdmin_GetDestinations((admin), (collection), (pattern), \
                                    (TIBEMS_TOPIC), (permType), (TIBEMS_FALSE))

extern tibems_status
tibemsAdmin_GetProducerStatistics(
    tibemsAdmin                 admin,
    tibemsCollection*           prodInfos,
    const char*                 username,     /* Reserved for future use. */
    tibems_long                 connectionID, /* Reserved for future use. */
    tibemsDestinationInfo       destination);

extern tibems_status
tibemsAdmin_GetCommandTimeout(
    tibemsAdmin                 admin,
    tibems_long*                timeout);

extern tibems_status
tibemsAdmin_SetCommandTimeout(
    tibemsAdmin                 admin,
    tibems_long                 timeout);

extern tibems_status
tibemsAdmin_SetExceptionListener(
    tibemsAdmin                 admin,
    tibemsExceptionCallback     listener,
    const void*                 closure);

extern tibems_status
tibemsServerInfo_Destroy(
    tibemsServerInfo            serverInfo);

extern tibems_status
tibemsServerInfo_GetQueueCount(
    tibemsServerInfo            serverInfo,
    tibems_int*                 count);

extern tibems_status
tibemsServerInfo_GetTopicCount(
    tibemsServerInfo            serverInfo,
    tibems_int*                 count);

extern tibems_status
tibemsServerInfo_GetProducerCount(
    tibemsServerInfo            serverInfo,
    tibems_int*                 count);

extern tibems_status
tibemsServerInfo_GetConsumerCount(
    tibemsServerInfo            serverInfo,
    tibems_int*                 count);

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibems_emsadmin_h */

