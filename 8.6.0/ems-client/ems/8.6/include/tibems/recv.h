/* 
 * Copyright (c) 2001-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: recv.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibemsqrecv_h
#define _INCLUDED_tibemsqrecv_h

#include "types.h"
#include "status.h"

#if defined(__cplusplus)
extern "C" {
#endif

/* deprecated as of 5.0. please use tibemsMsgConsumer_GetDestination */
extern tibems_status
tibemsQueueReceiver_GetQueue(
    tibemsQueueReceiver         queueReceiver,
    tibemsQueue*                queue);

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibemsqrecv_h */
