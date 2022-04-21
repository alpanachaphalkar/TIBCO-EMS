/* 
 * Copyright (c) 2001-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: tmsg.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibemstextmsg_h
#define _INCLUDED_tibemstextmsg_h

#include "types.h"
#include "status.h"

#if defined(__cplusplus)
extern "C" {
#endif

extern tibems_status
tibemsTextMsg_Create(
    tibemsTextMsg*      message);

extern tibems_status
tibemsTextMsg_GetText(
    tibemsTextMsg       message,
    const char**        text);

extern tibems_status
tibemsTextMsg_SetText(
    tibemsTextMsg       message,
    const char*         text);

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibemstextmsg_h */
