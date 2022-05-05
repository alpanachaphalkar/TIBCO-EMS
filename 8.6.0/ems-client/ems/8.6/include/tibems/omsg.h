/* 
 * Copyright (c) 2001-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: omsg.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibemsobjectmsg_h
#define _INCLUDED_tibemsobjectmsg_h

#include "types.h"
#include "status.h"

#if defined(__cplusplus)
extern "C" {
#endif

extern tibems_status
tibemsObjectMsg_Create(
    tibemsObjectMsg*    message);

extern tibems_status
tibemsObjectMsg_GetObjectBytes(
    tibemsObjectMsg     message,
    void**              bytes,
    tibems_uint*        byteSize);

extern tibems_status
tibemsObjectMsg_SetObjectBytes(
    tibemsObjectMsg     message,
    const void*         bytes,
    tibems_uint         byteSize);

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibemsobjectmsg_h */
