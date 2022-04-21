/* 
 * Copyright (c) 2001-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: mmsg.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibemsmapmsg_h
#define _INCLUDED_tibemsmapmsg_h

#include "types.h"
#include "status.h"

#if defined(__cplusplus)
extern "C" {
#endif

extern tibems_status
tibemsMapMsg_Create(
    tibemsMapMsg*       message);

extern tibems_status
tibemsMapMsg_GetBoolean(
    tibemsMapMsg        message,
    const char*         name,
    tibems_bool*        value);

extern tibems_status
tibemsMapMsg_GetByte(
    tibemsMapMsg        message,
    const char*         name,
    tibems_byte*        value);

extern tibems_status
tibemsMapMsg_GetBytes(
    tibemsMapMsg        message,
    const char*         name,
    const void**        bytes,
    tibems_uint*        bytesSize);

extern tibems_status
tibemsMapMsg_GetChar(
    tibemsMapMsg        message,
    const char*         name,
    tibems_wchar*       value);

extern tibems_status
tibemsMapMsg_GetDouble(
    tibemsMapMsg        message,
    const char*         name,
    tibems_double*      value);

extern tibems_status
tibemsMapMsg_GetFloat(
    tibemsMapMsg        message,
    const char*         name,
    tibems_float*       value);

extern tibems_status
tibemsMapMsg_GetInt(
    tibemsMapMsg        message,
    const char*         name,
    tibems_int*         value);

extern tibems_status
tibemsMapMsg_GetLong(
    tibemsMapMsg        message,
    const char*         name,
    tibems_long*        value);

extern tibems_status
tibemsMapMsg_GetShort(
    tibemsMapMsg        message,
    const char*         name,
    tibems_short*       value);

extern tibems_status
tibemsMapMsg_GetString(
    tibemsMapMsg        message,
    const char*         name,
    const char**        value);

extern tibems_status
tibemsMapMsg_GetMapMsg(
    tibemsMapMsg        msg,
    const char*         name,
    tibemsMapMsg*       value);

extern tibems_status
tibemsMapMsg_GetStreamMsg(
    tibemsMapMsg        msg,
    const char*         name,
    tibemsStreamMsg*    value);

extern tibems_status
tibemsMapMsg_SetBoolean(
    tibemsMapMsg        message,
    const char*         name,
    tibems_bool         value);

extern tibems_status
tibemsMapMsg_SetByte(
    tibemsMapMsg        message,
    const char*         name,
    tibems_byte         value);

extern tibems_status
tibemsMapMsg_SetBytes(
    tibemsMapMsg        message,
    const char*         name,
    void*               bytes,
    tibems_uint         bytesSize);

extern tibems_status
tibemsMapMsg_SetReferencedBytes(
    tibemsMapMsg        message,
    const char*         name,
    void*               bytes,
    tibems_uint         bytesSize);

extern tibems_status
tibemsMapMsg_SetChar(
    tibemsMapMsg        message,
    const char*         name,
    tibems_wchar        value);

extern tibems_status
tibemsMapMsg_SetDouble(
    tibemsMapMsg        message,
    const char*         name,
    tibems_double       value);

extern tibems_status
tibemsMapMsg_SetFloat(
    tibemsMapMsg        message,
    const char*         name,
    tibems_float        value);

extern tibems_status
tibemsMapMsg_SetInt(
    tibemsMapMsg        message,
    const char*         name,
    tibems_int          value);

extern tibems_status
tibemsMapMsg_SetLong(
    tibemsMapMsg        message,
    const char*         name,
    tibems_long         value);

extern tibems_status
tibemsMapMsg_SetShort(
    tibemsMapMsg        message,
    const char*         name,
    tibems_short        value);

extern tibems_status
tibemsMapMsg_SetString(
    tibemsMapMsg        message,
    const char*         name,
    const char*         value);

extern tibems_status
tibemsMapMsg_SetMapMsg(
    tibemsMsg           msg,
    const char*         name,
    tibemsMsg           mapMsg,
    tibems_bool         takeOwnership);

extern tibems_status
tibemsMapMsg_SetStreamMsg(
    tibemsMsg           msg,
    const char*         name,
    tibemsMsg           streamMsg,
    tibems_bool         takeOwnership);

extern tibems_status
tibemsMapMsg_SetShortArray(
    tibemsMsg           msg,
    const char*         name,
    const tibems_short* value,
    tibems_uint         count);

extern tibems_status
tibemsMapMsg_SetIntArray(
    tibemsMsg           msg,
    const char*         name,
    const tibems_int*   value,
    tibems_uint         count);

extern tibems_status
tibemsMapMsg_SetLongArray(
    tibemsMsg           msg,
    const char*         name,
    const tibems_long*  value,
    tibems_uint         count);

extern tibems_status
tibemsMapMsg_SetFloatArray(
    tibemsMsg           msg,
    const char*         name,
    const tibems_float* value,
    tibems_uint         count);

extern tibems_status
tibemsMapMsg_SetDoubleArray(
    tibemsMsg               msg,
    const char*             name,
    const tibems_double*    value,
    tibems_uint             count);

extern tibems_status
tibemsMapMsg_ItemExists(
    tibemsMapMsg        message,
    const char*         name,
    tibems_bool*        answer);

extern tibems_status
tibemsMapMsg_GetMapNames(
    tibemsMsg           message,
    tibemsMsgEnum*      enumeration);

extern tibems_status
tibemsMapMsg_GetField(
    tibemsMapMsg        message,
    const char*         name,
    tibemsMsgField*     data);



#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibemsmapmsg_h */
