/* 
 * Copyright (c) 2013-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: subinfo.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibems_subinfo_h
#define _INCLUDED_tibems_subinfo_h

#include "types.h"
#include "status.h"
#if defined(__cplusplus)
extern "C" {
#endif

extern tibems_status
tibemsSubscriptionInfo_GetID(
    tibemsSubscriptionInfo      subscriptionInfo,
    tibems_long*                id);

extern tibems_status
tibemsSubscriptionInfo_GetName(
    tibemsSubscriptionInfo      subscriptionInfo,
    char*                       name,
    tibems_int                  nameLength);

extern tibems_status
tibemsSubscriptionInfo_GetCreateTime(
    tibemsSubscriptionInfo      subscriptionInfo,
    tibems_long*                created);

extern tibems_status
tibemsSubscriptionInfo_GetTopicName(
    tibemsSubscriptionInfo      subscriptionInfo,
    char*                       topicName,
    tibems_int                  topicNameLength);

extern tibems_status
tibemsSubscriptionInfo_GetPendingMessageCount(
    tibemsSubscriptionInfo      subscriptionInfo,
    tibems_long*                msgCount);

extern tibems_status
tibemsSubscriptionInfo_GetPendingMessageSize(
    tibemsSubscriptionInfo      subscriptionInfo,
    tibems_long*                msgSize);

extern tibems_status
tibemsSubscriptionInfo_GetConsumerCount(
    tibemsSubscriptionInfo      subscriptionInfo,
    tibems_int*                 consumerCount);

extern tibems_status
tibemsSubscriptionInfo_HasSelector(
    tibemsSubscriptionInfo      subscriptionInfo,
    tibems_bool*                hasSelector);

extern tibems_status
tibemsSubscriptionInfo_GetSelector(
    tibemsSubscriptionInfo      subscriptionInfo,
    char*                       selector,
    tibems_int                  selectorLength);

extern tibems_status
tibemsSubscriptionInfo_IsShared(
    tibemsSubscriptionInfo      subscriptionInfo,
    tibems_bool*                shared);

extern tibems_status
tibemsSubscriptionInfo_IsDurable(
    tibemsSubscriptionInfo      subscriptionInfo,
    tibems_bool*                durable);

extern tibems_status
tibemsSubscriptionInfo_Destroy(
    tibemsSubscriptionInfo      subscriptionInfo);

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibems_subinfo_h */

