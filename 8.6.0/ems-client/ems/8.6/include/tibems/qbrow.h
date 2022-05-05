/* 
 * Copyright (c) 2001-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: qbrow.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibemsqueuebrowser_h
#define _INCLUDED_tibemsqueuebrowser_h

#include "types.h"
#include "status.h"

#if defined(__cplusplus)
extern "C" {
#endif

extern tibems_status
tibemsQueueBrowser_GetQueue(
    tibemsQueueBrowser          queueBrowser,
    tibemsQueue*                queue);

extern tibems_status
tibemsQueueBrowser_GetMsgSelector(
    tibemsQueueBrowser          queueBrowser,
    const char**                selector);

extern tibems_status
tibemsQueueBrowser_GetNext(
    tibemsQueueBrowser          queueBrowser,
    tibemsMsg*                  msg);

extern tibems_status
tibemsQueueBrowser_Close(
    tibemsQueueBrowser          queueBrowser);

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibemsqueuebrowser_h */
