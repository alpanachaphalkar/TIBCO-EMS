/* 
 * Copyright (c) 2001-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: msgprod.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibemsmsgprod_h
#define _INCLUDED_tibemsmsgprod_h

#include "types.h"
#include "status.h"

#if defined(__cplusplus)
extern "C" {
#endif

typedef void
(*tibemsMsgCompletionCallback) (
    tibemsMsg                   msg,
    tibems_status               status,
    void*                       closure);

extern tibems_status
tibemsMsgProducer_Close(
    tibemsMsgProducer           msgProducer);

extern tibems_status
tibemsMsgProducer_GetDestination(
    tibemsMsgProducer           msgProducer,
    tibemsDestination*          destination);

extern tibems_status
tibemsMsgProducer_GetDeliveryDelay(
    tibemsMsgProducer           msgProducer,
    tibems_long*                deliveryDelay);

extern tibems_status
tibemsMsgProducer_GetDeliveryMode(
    tibemsMsgProducer           msgProducer,
    tibems_int*                 deliveryMode);

extern tibems_status
tibemsMsgProducer_GetDisableMessageID(
    tibemsMsgProducer           msgProducer,
    tibems_bool*                doDisableMessageID);

extern tibems_status
tibemsMsgProducer_GetDisableMessageTimestamp(
    tibemsMsgProducer           msgProducer,
    tibems_bool*                doDisableMessageTimeStamp);

extern tibems_status
tibemsMsgProducer_GetPriority(
    tibemsMsgProducer           msgProducer,
    tibems_int*                 priority);

extern tibems_status
tibemsMsgProducer_GetTimeToLive(
    tibemsMsgProducer           msgProducer,
    tibems_long*                timeToLive);

extern tibems_status
tibemsMsgProducer_SetDeliveryDelay(
    tibemsMsgProducer           msgProducer,
    tibems_long                 deliveryDelay);

extern tibems_status
tibemsMsgProducer_SetDeliveryMode(
    tibemsMsgProducer           msgProducer,
    tibems_int                  deliveryMode);

extern tibems_status
tibemsMsgProducer_SetDisableMessageID(
    tibemsMsgProducer           msgProducer,
    tibems_bool                 doDisableMessageID);

extern tibems_status
tibemsMsgProducer_SetDisableMessageTimestamp(
    tibemsMsgProducer           msgProducer,
    tibems_bool                 doDisableMessageTimeStamp);

extern tibems_status
tibemsMsgProducer_SetPriority(
    tibemsMsgProducer           msgProducer,
    tibems_int                  priority);

extern tibems_status
tibemsMsgProducer_SetTimeToLive(
    tibemsMsgProducer           msgProducer,
    tibems_long                 timeToLive);

extern tibems_status
tibemsMsgProducer_Send(
    tibemsMsgProducer           msgProducer,
    tibemsMsg                   msg);

extern tibems_status
tibemsMsgProducer_AsyncSend(
    tibemsMsgProducer           msgProducer,
    tibemsMsg                   msg,
    tibemsMsgCompletionCallback asyncSendCallback,
    void*                       asyncSendClosure);

extern tibems_status
tibemsMsgProducer_SendEx(
    tibemsMsgProducer           msgProducer,
    tibemsMsg                   msg,
    tibems_int                  deliveryMode,
    tibems_int                  priority,
    tibems_long                 timeToLive);

extern tibems_status
tibemsMsgProducer_AsyncSendEx(
    tibemsMsgProducer           msgProducer,
    tibemsMsg                   msg,
    tibems_int                  deliveryMode,
    tibems_int                  priority,
    tibems_long                 timeToLive,
    tibemsMsgCompletionCallback asyncSendCallback,
    void*                       asyncSendClosure);

extern tibems_status
tibemsMsgProducer_SendToDestination(
    tibemsMsgProducer           msgProducer,
    tibemsDestination           destination,
    tibemsMsg                   msg);

extern tibems_status
tibemsMsgProducer_AsyncSendToDestination(
    tibemsMsgProducer           msgProducer,
    tibemsDestination           destination,
    tibemsMsg                   msg,
    tibemsMsgCompletionCallback asyncSendCallback,
    void*                       asyncSendClosure);

extern tibems_status
tibemsMsgProducer_SendToDestinationEx(
    tibemsMsgProducer           msgProducer,
    tibemsDestination           destination,
    tibemsMsg                   msg,
    tibemsDeliveryMode          deliveryMode,
    tibems_int                  priority,
    tibems_long                 timeToLive);

extern tibems_status
tibemsMsgProducer_AsyncSendToDestinationEx(
    tibemsMsgProducer           msgProducer,
    tibemsDestination           destination,
    tibemsMsg                   msg,
    tibemsDeliveryMode          deliveryMode,
    tibems_int                  priority,
    tibems_long                 timeToLive,
    tibemsMsgCompletionCallback asyncSendCallback,
    void*                       asyncSendClosure);

extern tibems_status
tibemsMsgProducer_SetNPSendCheckMode(
    tibemsMsgProducer           producer, 
    tibemsNpCheckMode           mode);

extern tibems_status
tibemsMsgProducer_GetNPSendCheckMode(
    tibemsMsgProducer           producer, 
    tibemsNpCheckMode          *mode);

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibemsmsgprod_h */
