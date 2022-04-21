/* 
 * Copyright (c) 2001-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: bmsg.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibemsbytesmsg_h
#define _INCLUDED_tibemsbytesmsg_h

#include "types.h"
#include "status.h"

#if defined(__cplusplus)
extern "C" {
#endif

extern tibems_status
tibemsBytesMsg_Create(
    tibemsBytesMsg*     message);

extern tibems_status
tibemsBytesMsg_GetBodyLength(
    tibemsMsg           msg,
    tibems_int*         return_length);
        
extern tibems_status
tibemsBytesMsg_ReadBoolean(
    tibemsBytesMsg      message,
    tibems_bool*        value);

extern tibems_status
tibemsBytesMsg_ReadByte(
    tibemsBytesMsg      message,
    tibems_byte*        value);

extern tibems_status
tibemsBytesMsg_ReadBytes(
    tibemsBytesMsg      message,
    const void**        value,
    tibems_int          requested_length,
    tibems_int*         return_length);

extern tibems_status
tibemsBytesMsg_ReadChar(
    tibemsBytesMsg      message,
    tibems_wchar*       value);

extern tibems_status
tibemsBytesMsg_ReadDouble(
    tibemsBytesMsg      message,
    tibems_double*      value);

extern tibems_status
tibemsBytesMsg_ReadFloat(
    tibemsBytesMsg      message,
    tibems_float*       value);

extern tibems_status
tibemsBytesMsg_ReadInt(
    tibemsBytesMsg      message,
    tibems_int*         value);

extern tibems_status
tibemsBytesMsg_ReadLong(
    tibemsBytesMsg      message,
    tibems_long*        value);

extern tibems_status
tibemsBytesMsg_ReadShort(
    tibemsBytesMsg      message,
    tibems_short*       value);

extern tibems_status
tibemsBytesMsg_ReadUnsignedByte(
    tibemsBytesMsg      message,
    tibems_int*         value);

extern tibems_status
tibemsBytesMsg_ReadUnsignedShort(
    tibemsBytesMsg      message,
    tibems_int*         value);

extern tibems_status
tibemsBytesMsg_ReadUTF(
    tibemsBytesMsg      message,
    const char**        value,
    tibems_int*         length);

extern tibems_status
tibemsBytesMsg_WriteBoolean(
    tibemsBytesMsg      message,
    tibems_bool         value);

extern tibems_status
tibemsBytesMsg_WriteByte(
    tibemsBytesMsg      message,
    tibems_byte         value);

extern tibems_status
tibemsBytesMsg_WriteBytes(
    tibemsBytesMsg      message,
    const void*         value,
    tibems_uint         size);

extern tibems_status
tibemsBytesMsg_WriteChar(
    tibemsBytesMsg      message,
    tibems_wchar        value);

extern tibems_status
tibemsBytesMsg_WriteDouble(
    tibemsBytesMsg      message,
    tibems_double       value);

extern tibems_status
tibemsBytesMsg_WriteFloat(
    tibemsBytesMsg      message,
    tibems_float        value);

extern tibems_status
tibemsBytesMsg_WriteInt(
    tibemsBytesMsg      message,
    tibems_int          value);

extern tibems_status
tibemsBytesMsg_WriteLong(
    tibemsBytesMsg      message,
    tibems_long         value);

extern tibems_status
tibemsBytesMsg_WriteShort(
    tibemsBytesMsg      message,
    tibems_short        value);

extern tibems_status
tibemsBytesMsg_WriteUTF(
    tibemsBytesMsg      message,
    const char*         value);

extern tibems_status
tibemsBytesMsg_Reset(
    tibemsBytesMsg      message);

extern tibems_status
tibemsBytesMsg_GetBytes(
    tibemsBytesMsg      message,
    void**              bytes,
    tibems_uint*        byteSize);

extern tibems_status
tibemsBytesMsg_SetBytes(
    tibemsBytesMsg      message,
    const void*         bytes,
    tibems_uint         byteSize);

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibemsbytesmsg_h */
