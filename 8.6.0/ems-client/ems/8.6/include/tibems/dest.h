/* 
 * Copyright (c) 2001-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: dest.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibemsdest_h
#define _INCLUDED_tibemsdest_h

#include "types.h"
#include "status.h"

#if defined(__cplusplus)
extern "C" {
#endif

extern tibems_status
tibemsDestination_Create(
    tibemsDestination*          destination,
    tibemsDestinationType       type,
    const char*                 name);

extern tibems_status
tibemsDestination_Destroy(
    tibemsDestination           destination);

extern tibems_status
tibemsDestination_Copy(
    tibemsDestination           destination,
    tibemsDestination*          copy);

extern tibems_status
tibemsDestination_GetName(
    tibemsDestination           destination,
    char*                       name,
    tibems_int                  name_len);

extern tibems_status
tibemsDestination_GetType(
    tibemsDestination           destination,
    tibemsDestinationType*      type);

/* queues */
extern tibems_status
tibemsQueue_Create(
    tibemsQueue*                queue,
    const char*                 queueName);

extern tibems_status
tibemsQueue_GetQueueName(
    tibemsQueue                 queue,
    char*                       name,
    tibems_int                  name_len);

extern tibems_status
tibemsQueue_Destroy(
    tibemsQueue                 queue);

/* topics */
extern tibems_status
tibemsTopic_Create(
    tibemsTopic*                topic,
    const char*                 topicName);

extern tibems_status
tibemsTopic_Destroy(
    tibemsTopic                 topic);

extern tibems_status
tibemsTopic_GetTopicName(
    tibemsTopic                 topic,
    char*                       name,
    tibems_int                  name_len);

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibemsdest_h */
