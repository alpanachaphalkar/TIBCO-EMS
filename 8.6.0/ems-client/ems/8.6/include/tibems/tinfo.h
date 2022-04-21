/* 
 * Copyright (c) 2007-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: tinfo.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibems_tinfo_h
#define _INCLUDED_tibems_tinfo_h

#include "types.h"
#include "status.h"

#if defined(__cplusplus)
extern "C" {
#endif

extern tibems_status 
tibemsTopicInfo_GetSubscriptionCount(
    tibemsTopicInfo             topicInfo,
    tibems_int*                 count);

extern tibems_status 
tibemsTopicInfo_GetDurableSubscriptionCount(
    tibemsTopicInfo             topicInfo,
    tibems_int*                 count);

extern tibems_status 
tibemsTopicInfo_GetActiveDurableCount(
    tibemsTopicInfo             topicInfo,
    tibems_int*                 count);

// Deprecated: Use tibemsTopicInfo_GetDurableSubscriptionCount() instead.
extern tibems_status 
tibemsTopicInfo_GetDurableCount(
    tibemsTopicInfo             topicInfo,
    tibems_int*                 count);

#define tibemsTopicInfo_Create(topicInfo, name) \
        tibemsDestinationInfo_Create((topicInfo), (name), TIBEMS_TOPIC)

#define tibemsTopicInfo_GetSubscriberCount(topicInfo, count) \
        tibemsDestinationInfo_GetConsumerCount((topicInfo), (count))

#define tibemsTopicInfo_GetPendingMessageCount(topicInfo, count) \
        tibemsDestinationInfo_GetPendingMessageCount((topicInfo), (count))

#define tibemsTopicInfo_GetPendingMessageSize(topicInfo, size) \
        tibemsDestinationInfo_GetPendingMessageSize((topicInfo), (size))

#define tibemsTopicInfo_GetPendingPersistentMessageCount(topicInfo, count) \
        tibemsDestinationInfo_GetPendingPersistentMessageCount((topicInfo), (count))

#define tibemsTopicInfo_GetPendingPersistentMessageSize(topicInfo, size) \
        tibemsDestinationInfo_GetPendingPersistentMessageSize((topicInfo), (size))

#define tibemsTopicInfo_GetInboundStatistics(topicInfo, statData) \
        tibemsDestinationInfo_GetInboundStatistics((topicInfo), (statData))

#define tibemsTopicInfo_GetOutboundStatistics(topicInfo, statData) \
        tibemsDestinationInfo_GetOutboundStatistics((topicInfo), (statData))

#define tibemsTopicInfo_GetName(topicInfo, name, name_len) \
        tibemsDestinationInfo_GetName((topicInfo), (name), (name_len))

#define tibemsTopicInfo_GetFlowControlMaxBytes(topicInfo, maxBytes) \
        tibemsDestinationInfo_GetFlowControlMaxBytes((topicInfo), (maxBytes))

#define tibemsTopicInfo_GetMaxBytes(topicInfo, maxBytes) \
        tibemsDestinationInfo_GetMaxBytes((topicInfo), (maxBytes))

#define tibemsTopicInfo_GetMaxMsgs(topicInfo, maxMsgs) \
        tibemsDestinationInfo_GetMaxMsgs((topicInfo), (maxMsgs))

#define tibemsTopicInfo_GetOverflowPolicy(topicInfo, overflowPolicy ) \
        tibemsDestinationInfo_GetOverflowPolicy((topicInfo), (overflowPolicy))

#define tibemsTopicInfo_Destroy(topicInfo) \
        tibemsDestinationInfo_Destroy((topicInfo))

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibems_tinfo_h */
