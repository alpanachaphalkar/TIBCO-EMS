/* 
 * Copyright (c) 2001-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: msgcons.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibemsmsgcons_h
#define _INCLUDED_tibemsmsgcons_h

#include "types.h"
#include "status.h"

#if defined(__cplusplus)
extern "C" {
#endif

typedef void
(*tibemsMsgCallback) (
    tibemsMsgConsumer           msgConsumer,
    tibemsMsg                   msg,
    void*                       closure);

extern tibems_status
tibemsMsgConsumer_Close(
    tibemsMsgConsumer           msgConsumer);

extern tibems_status
tibemsMsgConsumer_GetDestination(
    tibemsMsgConsumer           msgConsumer,
    tibemsDestination*          destination);

extern tibems_status
tibemsMsgConsumer_GetMsgSelector(
    tibemsMsgConsumer           msgConsumer,
    const char**                selectorPtr);

extern tibems_status
tibemsMsgConsumer_GetNoLocal(
    tibemsMsgConsumer           msgConsumer,
    tibems_bool*                noLocal);

extern tibems_status
tibemsMsgConsumer_Receive(
    tibemsMsgConsumer           msgConsumer,
    tibemsMsg*                  msg);

extern tibems_status
tibemsMsgConsumer_ReceiveTimeout(
    tibemsMsgConsumer           msgConsumer,
    tibemsMsg*                  msg,
    tibems_long                 timeout);

extern tibems_status
tibemsMsgConsumer_ReceiveNoWait(
    tibemsMsgConsumer           msgConsumer,
    tibemsMsg*                  msg);

extern tibems_status
tibemsMsgConsumer_SetMsgListener(
    tibemsMsgConsumer           msgConsumer,
    tibemsMsgCallback           callback,
    void*                       closure);

extern tibems_status
tibemsMsgConsumer_GetMsgListener(
    tibemsMsgConsumer           msgConsumer,
    tibemsMsgCallback*          callbackPtr,
    void**                      closure);

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibemsmsgcons_h */
