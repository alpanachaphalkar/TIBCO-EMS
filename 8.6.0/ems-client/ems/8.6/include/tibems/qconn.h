/* 
 * Copyright (c) 2001-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: qconn.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibemsqconn_h
#define _INCLUDED_tibemsqconn_h

#include "types.h"
#include "status.h"

#if defined(__cplusplus)
extern "C" {
#endif

/* deprecated as of 5.0. please use tibemsConnection_Create */
extern tibems_status
tibemsQueueConnection_Create(
    tibemsQueueConnection*              queueConnection,
    const char*                         brokerURL,
    const char*                         clientId,
    const char*                         username,
    const char*                         password);

/* deprecated as of 5.0. please use tibemsConnection_CreateSession */
extern tibems_status
tibemsQueueConnection_CreateQueueSession(
    tibemsQueueConnection               queueConnection,
    tibemsQueueSession*                 queueSession,
    tibems_bool                         transacted,
    tibemsAcknowledgeMode               acknowledgeMode);

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibemsqconn_h */
