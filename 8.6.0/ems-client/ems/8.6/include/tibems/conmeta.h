/* 
 * Copyright (c) 2001-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: conmeta.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibemsconmeta_h
#define _INCLUDED_tibemsconmeta_h

#include "types.h"
#include "status.h"

#if defined(__cplusplus)
extern "C" {
#endif

extern tibems_status
tibemsConnectionMetaData_GetEMSMajorVersion(
    tibemsConnectionMetaData    metaData,
    tibems_int*                 majorVersion);

extern tibems_status
tibemsConnectionMetaData_GetEMSMinorVersion(
    tibemsConnectionMetaData    metaData,
    tibems_int*                 minorVersion);

extern tibems_status
tibemsConnectionMetaData_GetEMSVersion(
    tibemsConnectionMetaData    metaData,
    const char**                version);

extern tibems_status
tibemsConnectionMetaData_GetEMSProviderName(
    tibemsConnectionMetaData    metaData,
    const char**                providerName);

extern tibems_status
tibemsConnectionMetaData_GetProviderMajorVersion(
    tibemsConnectionMetaData    metaData,
    tibems_int*                 majorVersion);

extern tibems_status
tibemsConnectionMetaData_GetProviderMinorVersion(
    tibemsConnectionMetaData    metaData,
    tibems_int*                 minorVersion);

extern tibems_status
tibemsConnectionMetaData_GetProviderVersion(
    tibemsConnectionMetaData    metaData,
    const char**                version);


#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibemsconmeta_h */
