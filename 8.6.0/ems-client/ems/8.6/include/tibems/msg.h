/* 
 * Copyright (c) 2001-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: msg.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibemsmsg_h
#define _INCLUDED_tibemsmsg_h

#include <stdio.h>

#include "types.h"
#include "status.h"

#if defined(__cplusplus)
extern "C" {
#endif

extern tibems_status
tibemsMsg_Create(
    tibemsMsg*          message);

extern tibems_status
tibemsMsg_Destroy(
    tibemsMsg           message);

/* get message type */
extern tibems_status
tibemsMsg_GetBodyType(
    tibemsMsg           message,
    tibemsMsgType*      type);

/* From EMS specification */

extern tibems_status
tibemsMsg_Acknowledge(
    tibemsMsg           message);

extern tibems_status
tibemsMsg_Recover(
    tibemsMsg           message);

extern tibems_status
tibemsMsg_ClearBody(
    tibemsMsg           message);

extern tibems_status
tibemsMsg_ClearProperties(
    tibemsMsg           message);

extern tibems_status
tibemsMsg_GetPropertyNames(
    tibemsMsg           message,
    tibemsMsgEnum*      enumeration);

/* returns TIBEMS_NOT_FOUND if end of enumeration */
extern tibems_status
tibemsMsgEnum_GetNextName(
    tibemsMsgEnum       enumeration,
    const char**        name);

extern tibems_status
tibemsMsgEnum_Destroy(
    tibemsMsgEnum       enumeration);

extern tibems_status
tibemsMsg_PropertyExists(
    tibemsMsg           message,
    const char*         name,
    tibems_bool*        answer);

extern tibems_status
tibemsMsg_GetProperty(
    tibemsMsg           message,
    const char*         name,
    tibemsMsgField*     data);

/* Gets and Sets on Properties */

extern tibems_status
tibemsMsg_GetBooleanProperty(
    tibemsMsg           message,
    const char*         name,
    tibems_bool*        value);

extern tibems_status
tibemsMsg_GetByteProperty(
    tibemsMsg           message,
    const char*         name,
    tibems_byte*        value);

extern tibems_status
tibemsMsg_GetDoubleProperty(
    tibemsMsg           message,
    const char*         name,
    tibems_double*      value);

extern tibems_status
tibemsMsg_GetFloatProperty(
    tibemsMsg           message,
    const char*         name,
    tibems_float*       value);

extern tibems_status
tibemsMsg_GetIntProperty(
    tibemsMsg           message,
    const char*         name,
    tibems_int*         value);

extern tibems_status
tibemsMsg_GetLongProperty(
    tibemsMsg           message,
    const char*         name,
    tibems_long*        value);

extern tibems_status
tibemsMsg_GetShortProperty(
    tibemsMsg           message,
    const char*         name,
    tibems_short*       value);

extern tibems_status
tibemsMsg_GetStringProperty(
    tibemsMsg           message,
    const char*         name,
    const char**        value);

extern tibems_status
tibemsMsg_SetBooleanProperty(
    tibemsMsg           message,
    const char*         name,
    tibems_bool         value);

extern tibems_status
tibemsMsg_SetByteProperty(
    tibemsMsg           message,
    const char*         name,
    tibems_byte         value);

extern tibems_status
tibemsMsg_SetDoubleProperty(
    tibemsMsg           message,
    const char*         name,
    tibems_double       value);

extern tibems_status
tibemsMsg_SetFloatProperty(
    tibemsMsg           message,
    const char*         name,
    tibems_float        value);

extern tibems_status
tibemsMsg_SetIntProperty(
    tibemsMsg           message,
    const char*         name,
    tibems_int          value);

extern tibems_status
tibemsMsg_SetLongProperty(
    tibemsMsg           message,
    const char*         name,
    tibems_long         value);

extern tibems_status
tibemsMsg_SetShortProperty(
    tibemsMsg           message,
    const char*         name,
    tibems_short        value);

extern tibems_status
tibemsMsg_SetStringProperty(
    tibemsMsg           message,
    const char*         name,
    const char*         value);

/* Message Headers */

extern tibems_status
tibemsMsg_GetCorrelationID(
    tibemsMsg           message,
    const char**        value);

extern tibems_status
tibemsMsg_GetDeliveryTime(
    tibemsMsg           message,
    tibems_long*        value);

extern tibems_status
tibemsMsg_GetDeliveryMode(
    tibemsMsg           message,
    tibemsDeliveryMode* value);

extern tibems_status
tibemsMsg_GetDestination(
    tibemsMsg           message,
    tibemsDestination*  value);

extern tibems_status
tibemsMsg_GetExpiration(
    tibemsMsg           message,
    tibems_long*        value);

extern tibems_status
tibemsMsg_GetMessageID(
    tibemsMsg           message,
    const char**        value);

extern tibems_status
tibemsMsg_GetPriority(
    tibemsMsg           message,
    tibems_int*         value); 

extern tibems_status
tibemsMsg_GetRedelivered(
    tibemsMsg           message,
    tibems_bool*        value);

extern tibems_status
tibemsMsg_GetReplyTo(
    tibemsMsg           message,
    tibemsDestination*  value);

extern tibems_status
tibemsMsg_GetTimestamp(
    tibemsMsg           message,
    tibems_long*        value);

extern tibems_status
tibemsMsg_GetType(
    tibemsMsg           message,
    const char**        value);

extern tibems_status
tibemsMsg_SetCorrelationID(
    tibemsMsg           message,
    const char*         value);

extern tibems_status
tibemsMsg_SetDeliveryMode(
    tibemsMsg           message,
    tibemsDeliveryMode  value);

extern tibems_status
tibemsMsg_SetDestination(
    tibemsMsg           message,
    tibemsDestination   value);

extern tibems_status
tibemsMsg_SetExpiration(
    tibemsMsg           message,
    tibems_long         value);

extern tibems_status
tibemsMsg_SetMessageID(
    tibemsMsg           message,
    const char*         value);

extern tibems_status
tibemsMsg_SetPriority(
    tibemsMsg           message,
    tibems_int          value); 

extern tibems_status
tibemsMsg_SetRedelivered(
    tibemsMsg           message,
    tibems_bool         value);

extern tibems_status
tibemsMsg_SetReplyTo(
    tibemsMsg           message,
    tibemsDestination   value);

extern tibems_status
tibemsMsg_SetTimestamp(
    tibemsMsg           message,
    tibems_long         value);

extern tibems_status
tibemsMsg_SetType(
    tibemsMsg           message,
    const char*         value);

/* deprecated:  use tibemsMsg_GetByteSize */
extern tibems_int
tibemsMsg_ByteSize(
    tibemsMsg           message);

extern tibems_status
tibemsMsg_GetEncoding(
    const tibemsMsg     msg,
    const char**        value);

extern tibems_status
tibemsMsg_SetEncoding(
    tibemsMsg           msg,
    const char*         value);

extern tibems_status
tibemsMsg_GetByteSize(
    tibemsMsg           msg,
    tibems_int*         size);

extern tibems_status
tibemsMsg_GetAsBytes(
    const tibemsMsg     msg,
    const void**        bytes,
    tibems_int*         actual_size);

extern tibems_status
tibemsMsg_GetAsBytesCopy(
    const tibemsMsg     msg,
    void*               bytes,
    tibems_int          avail_size,
    tibems_int*         actual_size);

extern tibems_status
tibemsMsg_CreateFromBytes(
    tibemsMsg*          msgPtr,
    const void*         bytes);

/* make a read-only message writeable again -  NOT PART OF EMS SPEC  */
extern tibems_status
tibemsMsg_MakeWriteable(
    tibemsMsg           msg);


/* make a copy of an existing message */
tibems_status
tibemsMsg_CreateCopy(
    const tibemsMsg     msg,
    tibemsMsg*          copy);

/* Prints message, for debugging purposes only */
extern void
tibemsMsg_Print(
    tibemsMsg           message);

/*
 * deprecated, please use tibemsMsg_PrintToBuffer
 */
extern void
tibemsMsg_PrintFile(
    tibemsMsg           message,
    FILE*               file);

extern tibems_status
tibemsMsg_PrintToBuffer(
    tibemsMsg           message,
    char*               buffer,
    tibems_int          maxlen);

extern void
tibemsMsgField_Print(
    tibemsMsgField*     field);

/*
 * deprecated, please use tibemsMsgField_PrintToBuffer
 */
extern void
tibemsMsgField_PrintFile(
    tibemsMsgField*     field,
    FILE*               file);

extern tibems_status
tibemsMsgField_PrintToBuffer(
    tibemsMsgField*     field,
    char*               buffer,
    tibems_int          maxlen);

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibemsmsg_h */
