/* 
 * Copyright (c) 2001-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: smsg.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibemsstreammsg_h
#define _INCLUDED_tibemsstreammsg_h

#include "types.h"
#include "status.h"

#if defined(__cplusplus)
extern "C" {
#endif

extern tibems_status
tibemsStreamMsg_Create(
    tibemsStreamMsg*    message);

extern tibems_status
tibemsStreamMsg_ReadField(
    tibemsStreamMsg     message,
    tibemsMsgField*     value);

extern tibems_status
tibemsStreamMsg_ReadBoolean(
    tibemsStreamMsg     message,
    tibems_bool*        value);

extern tibems_status
tibemsStreamMsg_ReadByte(
    tibemsStreamMsg     message,
    tibems_byte*        value);

extern tibems_status
tibemsStreamMsg_ReadBytes(
    tibemsStreamMsg     message,
    void**              value,
    tibems_uint*        size);

extern tibems_status
tibemsStreamMsg_ReadChar(
    tibemsStreamMsg     message,
    tibems_wchar*       value);

extern tibems_status
tibemsStreamMsg_ReadDouble(
    tibemsStreamMsg     message,
    tibems_double*      value);

extern tibems_status
tibemsStreamMsg_ReadFloat(
    tibemsStreamMsg     message,
    tibems_float*       value);

extern tibems_status
tibemsStreamMsg_ReadInt(
    tibemsStreamMsg     message,
    tibems_int*         value);

extern tibems_status
tibemsStreamMsg_ReadLong(
    tibemsStreamMsg     message,
    tibems_long*        value);

extern tibems_status
tibemsStreamMsg_ReadShort(
    tibemsStreamMsg     message,
    tibems_short*       value);

extern tibems_status
tibemsStreamMsg_ReadString(
    tibemsStreamMsg     message,
    char**              value);

extern tibems_status
tibemsStreamMsg_Reset(
    tibemsStreamMsg     message);

extern tibems_status
tibemsStreamMsg_WriteBoolean(
    tibemsStreamMsg     message,
    tibems_bool         value);

extern tibems_status
tibemsStreamMsg_WriteByte(
    tibemsStreamMsg     message,
    tibems_byte         value);

extern tibems_status
tibemsStreamMsg_WriteBytes(
    tibemsStreamMsg     message,
    void*               value,
    tibems_uint         size);

extern tibems_status
tibemsStreamMsg_WriteChar(
    tibemsStreamMsg     message,
    tibems_wchar        value);

extern tibems_status
tibemsStreamMsg_WriteDouble(
    tibemsStreamMsg     message,
    tibems_double       value);

extern tibems_status
tibemsStreamMsg_WriteFloat(
    tibemsStreamMsg     message,
    tibems_float        value);

extern tibems_status
tibemsStreamMsg_WriteInt(
    tibemsStreamMsg     message,
    tibems_int          value);

extern tibems_status
tibemsStreamMsg_WriteLong(
    tibemsStreamMsg     message,
    tibems_long         value);

extern tibems_status
tibemsStreamMsg_WriteShort(
    tibemsStreamMsg     message,
    tibems_short        value);

extern tibems_status
tibemsStreamMsg_WriteString(
    tibemsStreamMsg     message,
    char*               value);

extern tibems_status
tibemsStreamMsg_WriteShortArray(
    tibemsMsg           msg,
    const tibems_short* value,
    tibems_int          count);

extern tibems_status
tibemsStreamMsg_WriteIntArray(
    tibemsMsg           msg,
    const tibems_int*   value,
    tibems_int          count);

extern tibems_status
tibemsStreamMsg_WriteLongArray(
    tibemsMsg           msg,
    const tibems_long*  value,
    tibems_int          count);

extern tibems_status
tibemsStreamMsg_WriteFloatArray(
    tibemsMsg           msg,
    const tibems_float* value,
    tibems_int          count);

extern tibems_status
tibemsStreamMsg_WriteDoubleArray(
    tibemsMsg           msg,
    const tibems_double* value,
    tibems_int          count);

extern tibems_status
tibemsStreamMsg_WriteMapMsg(
    tibemsMsg           msg,
    tibemsMsg           value);

extern tibems_status
tibemsStreamMsg_WriteStreamMsg(
    tibemsMsg           msg,
    tibemsMsg           value);

extern void
tibemsStreamMsg_FreeField(
    tibemsMsgField*     field);

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibemsstreammsg_h */
