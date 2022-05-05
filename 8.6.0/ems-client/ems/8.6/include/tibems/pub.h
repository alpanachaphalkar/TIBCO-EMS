/* 
 * Copyright (c) 2001-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: pub.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibemstpub_h
#define _INCLUDED_tibemstpub_h

#include "types.h"
#include "status.h"

#if defined(__cplusplus)
extern "C" {
#endif

/* deprecated as of 5.0. please use tibemsMsgProducer_GetDestination */
extern tibems_status
tibemsTopicPublisher_GetTopic(
    tibemsTopicPublisher        topicPublisher,
    tibemsTopic*                topic);

/* deprecated as of 5.0. please use tibemsMsgProducer_Send */
extern tibems_status
tibemsTopicPublisher_Publish(
    tibemsTopicPublisher        topicPublisher,
    tibemsMsg                   msg);

/* deprecated as of 5.0. please use tibemsMsgProducer_SendEx */
extern tibems_status
tibemsTopicPublisher_PublishEx(
    tibemsTopicPublisher        topicPublisher,
    tibemsMsg                   msg,
    tibems_int                  deliveryMode,
    tibems_int                  priority,
    tibems_long                 timeToLive);

/* deprecated as of 5.0. please use tibemsMsgProducer_SendToDestination */
extern tibems_status
tibemsTopicPublisher_PublishToTopic(
    tibemsTopicPublisher        topicPublisher,
    tibemsTopic                 topic,
    tibemsMsg                   msg);

/* deprecated as of 5.0. please use tibemsMsgProducer_SendToDestinationEx */
extern tibems_status
tibemsTopicPublisher_PublishToTopicEx(
    tibemsTopicPublisher        topicPublisher,
    tibemsTopic                 topic,
    tibemsMsg                   msg,
    tibemsDeliveryMode          deliveryMode,
    tibems_int                  priority,
    tibems_long                 timeToLive);

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibemstpub_h */
