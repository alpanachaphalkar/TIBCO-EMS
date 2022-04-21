/* 
 * Copyright (c) 2007-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: destinfo.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibems_destinfo_h
#define _INCLUDED_tibems_destinfo_h

#include "types.h"
#include "status.h"

#if defined(__cplusplus)
extern "C" {
#endif

/*
 * Do not use the API's in this file.
 * Instead use the corresponding tibemsQueueInfo_* API
 * or tibemsTopicInfo_* API. See qinfo.h and tinfo.h
 */

extern tibems_status 
tibemsDestinationInfo_Create(
    tibemsDestinationInfo*      destInfo,
    const char*                 name,
    tibemsDestinationType       destType);

extern tibems_status 
tibemsDestinationInfo_GetConsumerCount(
    tibemsDestinationInfo       destInfo,
    tibems_int*                 count);

extern tibems_status 
tibemsDestinationInfo_GetPendingMessageCount(
    tibemsDestinationInfo       destInfo,
    tibems_long*                count);

extern tibems_status 
tibemsDestinationInfo_GetPendingMessageSize(
    tibemsDestinationInfo       destInfo,
    tibems_long*                size);

extern tibems_status 
tibemsDestinationInfo_GetPendingPersistentMessageCount(
    tibemsDestinationInfo       destInfo,
    tibems_long*                count);

extern tibems_status 
tibemsDestinationInfo_GetPendingPersistentMessageSize(
    tibemsDestinationInfo       destInfo,
    tibems_long*                size);

extern tibems_status 
tibemsDestinationInfo_GetInboundStatistics(
    tibemsDestinationInfo       destInfo,
    tibemsStatData*             statData);

extern tibems_status 
tibemsDestinationInfo_GetOutboundStatistics(
    tibemsDestinationInfo       destInfo,
    tibemsStatData*             statData);

extern tibems_status 
tibemsDestinationInfo_GetName(
    tibemsDestinationInfo       destInfo,
    char*                       name,
    tibems_int                  name_len);

extern tibems_status 
tibemsDestinationInfo_GetType(
    tibemsDestinationInfo       destInfo,
    tibemsDestinationType*      destType);

extern tibems_status
tibemsDestinationInfo_GetFlowControlMaxBytes(
    tibemsDestinationInfo       destInfo,
    tibems_long*                maxBytes);

extern tibems_status
tibemsDestinationInfo_GetMaxBytes(
    tibemsDestinationInfo       destInfo,
    tibems_long*                maxBytes);

extern tibems_status
tibemsDestinationInfo_GetMaxMsgs(
    tibemsDestinationInfo       destInfo,
    tibems_long*                maxMsgs);

extern tibems_status
tibemsDestinationInfo_GetOverflowPolicy(
    tibemsDestinationInfo       destInfo,
    tibems_int*                 overflowPolicy);

extern tibems_status 
tibemsDestinationInfo_Destroy(
    tibemsDestinationInfo       destInfo);

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibems_destinfo_h */
