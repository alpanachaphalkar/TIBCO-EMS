/* 
 * Copyright (c) 2001-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: msgreq.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibemsmsgreq_h
#define _INCLUDED_tibemsmsgreq_h

#include "types.h"
#include "status.h"

#if defined(__cplusplus)
extern "C" {
#endif

/* deprecated as of 5.0. please use tibemsMsgRequestor_Create */
extern tibems_status
tibemsQueueRequestor_Create(
    tibemsQueueSession          session,
    tibemsMsgRequestor*         msgRequestor,
    tibemsQueue                 queue);

/* deprecated as of 5.0. please use tibemsMsgRequestor_Create */
extern tibems_status
tibemsTopicRequestor_Create(
    tibemsTopicSession          session,
    tibemsMsgRequestor*         msgRequestor,
    tibemsTopic                 topic);

extern tibems_status
tibemsMsgRequestor_Create(
    tibemsSession               session,
    tibemsMsgRequestor*         msgRequestor,
    tibemsDestination           destination);

extern tibems_status
tibemsMsgRequestor_Request(
    tibemsMsgRequestor          msgRequestor,
    tibemsMsg                   msgSent,
    tibemsMsg*                  msgReply);

extern tibems_status
tibemsMsgRequestor_Close(
    tibemsMsgRequestor          msgRequestor);

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibemsmsgreq_h */
