/* 
 * Copyright (c) 2007-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: consinfo.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibems_consinfo_h
#define _INCLUDED_tibems_consinfo_h

#include "types.h"
#include "status.h"
#if defined(__cplusplus)
extern "C" {
#endif

extern tibems_status 
tibemsConsumerInfo_GetCreateTime(
    tibemsConsumerInfo          consumerInfo,
    tibems_long*                created);

extern tibems_status 
tibemsConsumerInfo_GetDestinationName(
    tibemsConsumerInfo          consumerInfo,
    char*                       destName,
    tibems_int                  name_len);

extern tibems_status 
tibemsConsumerInfo_GetDurableName(
    tibemsConsumerInfo          consumerInfo,
    char*                       durableName,
    tibems_int                  name_len);

extern tibems_status 
tibemsConsumerInfo_GetDestinationType(
    tibemsConsumerInfo          consumerInfo,
    tibemsDestinationType*      destType);

extern tibems_status 
tibemsConsumerInfo_GetDetailedStatistics(
    tibemsConsumerInfo          consumerInfo,
    tibemsCollection*           detailStat);

extern tibems_status
tibemsConsumerInfo_GetID(
    tibemsConsumerInfo          consumerInfo,
    tibems_long*                id);

extern tibems_status
tibemsConsumerInfo_GetPendingMessageCount(
    tibemsConsumerInfo          consumerInfo,
    tibems_long*                count);

extern tibems_status
tibemsConsumerInfo_GetPendingMessageSize(
    tibemsConsumerInfo          consumerInfo,
    tibems_long*                size);

extern tibems_status
tibemsConsumerInfo_GetCurrentMsgCountSentByServer(
    tibemsConsumerInfo          consumerInfo,
    tibems_long*                count);

extern tibems_status
tibemsConsumerInfo_GetCurrentMsgSizeSentByServer(
    tibemsConsumerInfo          consumerInfo,
    tibems_long*                size);

extern tibems_status
tibemsConsumerInfo_GetElapsedSinceLastAcknowledged(
    tibemsConsumerInfo          consumerInfo,
    tibems_long*                time);

extern tibems_status
tibemsConsumerInfo_GetElapsedSinceLastSent(
    tibemsConsumerInfo          consumerInfo,
    tibems_long*                time);

extern tibems_status
tibemsConsumerInfo_GetTotalAcknowledgedCount(
    tibemsConsumerInfo          consumerInfo,
    tibems_long*                count);

extern tibems_status
tibemsConsumerInfo_GetTotalMsgCountSentByServer(
    tibemsConsumerInfo          consumerInfo,
    tibems_long*                count);

extern tibems_status
tibemsConsumerInfo_IsConnected(
    tibemsConsumerInfo          consumerInfo,
    tibems_bool*                connected);

extern tibems_status
tibemsConsumerInfo_IsConnectionConsumer(
    tibemsConsumerInfo          consumerInfo,
    tibems_bool*                connectionConsumer);

extern tibems_status
tibemsConsumerInfo_IsActive(
    tibemsConsumerInfo          consumerInfo,
    tibems_bool*                active);

extern tibems_status
tibemsConsumerInfo_IsShared(
    tibemsConsumerInfo          consumerInfo,
    tibems_bool*                shared);

extern tibems_status 
tibemsConsumerInfo_GetSharedSubscriptionName(
    tibemsConsumerInfo          consumerInfo,
    char*                       sharedName,
    tibems_int                  name_len);

extern tibems_status
tibemsConsumerInfo_GetStatistics(
    tibemsConsumerInfo          consumerInfo,
    tibemsStatData*             stat);

extern tibems_status 
tibemsConsumerInfo_Destroy(
    tibemsConsumerInfo          consumerInfo);

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibems_consinfo_h */
