/* 
 * Copyright (c) 2001-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: emserr.h 90180 2016-12-13 23:00:37Z $
 * 
 */
#ifndef _INCLUDED_emserr_h
#define _INCLUDED_emserr_h

#include "types.h"
#include "status.h"

#if defined(__cplusplus)
extern "C" {
#endif

extern tibems_status
tibemsErrorContext_Create(
    tibemsErrorContext*         errorContext);

extern tibems_status
tibemsErrorContext_Close(
    tibemsErrorContext          errorContext);

extern tibems_status
tibemsErrorContext_GetLastErrorString(
    tibemsErrorContext          errorContext,
    const char**                string);

extern tibems_status
tibemsErrorContext_GetLastErrorStackTrace(
    tibemsErrorContext          errorContext,
    const char**                string);

#ifdef __cplusplus
}
#endif

#endif

