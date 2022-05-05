/* 
 * Copyright (c) 2001-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: sub.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibemstsub_h
#define _INCLUDED_tibemstsub_h

#include "types.h"
#include "status.h"

#if defined(__cplusplus)
extern "C" {
#endif

/* deprecated as of 5.0. please use tibemsMsgConsumer_GetDestination */
extern tibems_status
tibemsTopicSubscriber_GetTopic(
    tibemsTopicSubscriber       topicSubscriber,
    tibemsTopic*                topic);

/* deprecated as of 5.0. please use tibemsMsgConsumer_GetNoLocal */
extern tibems_status
tibemsTopicSubscriber_GetNoLocal(
    tibemsTopicSubscriber       topicSubscriber,
    tibems_bool*                isNoLocal);

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibemstsub_h */
