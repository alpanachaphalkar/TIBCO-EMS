/* 
 * Copyright (c) 2001-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: xares.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibemsxares_h
#define _INCLUDED_tibemsxares_h

#include "types.h"
#include "status.h"

#if defined(__cplusplus)
extern "C" {
#endif

/*
 * XID structure id defined in X/Open xa.h header file.
 */
#ifndef XA_H
typedef struct xid_t XID;
#endif


/*
 * EMS XA object C API.
 */

extern tibems_status
tibemsXAConnection_Create(
    tibemsConnection*           connection,
    const char*                 brokerURL,
    const char*                 clientId,
    const char*                 username,
    const char*                 password);

extern tibems_status
tibemsXAConnection_CreateSSL(
    tibemsConnection*           connection,
    const char*                 brokerURL,
    const char*                 clientId,
    const char*                 username,
    const char*                 password,
    tibemsSSLParams             params,
    const char*                 pk_password);

extern tibems_status
tibemsXAConnection_CreateXASession(
    tibemsConnection            connection,
    tibemsSession*              session);

extern tibems_status
tibemsXAConnection_Close(
    tibemsConnection            connection);

extern tibems_status
tibemsXASession_Close(
    tibemsSession               session);

extern tibems_status
tibemsXASession_GetSession(
    tibemsSession               xaSession,
    tibemsSession*              session);

extern tibems_status
tibemsXASession_GetXAResource(
    tibemsSession               session,
    tibemsXAResource*           xaResource);

/*
 * Extensions for Classic X/Open XA.
 *
 * If an xa_info string was passed in to the xa_open function then there is no
 * need to create an XAConnection or an XASession as they will have been
 * created by the xa_open call. Use the functions below to obtain these
 * objects. The objects are deleted by xa_close so it is an error to close
 * them explicitly.
 */

extern tibems_status
tibemsXAConnection_Get(
    tibemsConnection*           connection,
    const char*                 brokerURL);

extern tibems_status
tibemsXAConnection_GetXASession(
    tibemsConnection            connection,
    tibemsSession*              xaSession);


/*
 * JTA XAResource object C API.
 */

extern tibems_status
tibemsXAResource_Commit(
    tibemsXAResource            xaResource,
    XID*                        xid,
    tibems_bool                 onePhase);

extern tibems_status
tibemsXAResource_End(
    tibemsXAResource            xaResource,
    XID*                        xid,
    int                         flags);

extern tibems_status
tibemsXAResource_Forget(
    tibemsXAResource            xaResource,
    XID*                        xid);

extern tibems_status
tibemsXAResource_GetTransactionTimeout(
    tibemsXAResource            xaResource,
    tibems_int*                 seconds);

extern tibems_status
tibemsXAResource_isSameRM(
    tibemsXAResource            xaResource,
    tibemsXAResource            xaResource2,
    tibems_bool*                result);

extern tibems_status
tibemsXAResource_Prepare(
    tibemsXAResource            xaResource,
    XID*                        xid);

extern tibems_status
tibemsXAResource_Recover(
    tibemsXAResource            xaResource,
    XID*                        xids,
    tibems_int                  desiredCount,
    tibems_int*                 returnedCount,
    tibems_int                  flag);

extern tibems_status
tibemsXAResource_Rollback(
    tibemsXAResource            xaResource,
    XID*                        xid);

extern tibems_status
tibemsXAResource_SetTransactionTimeout(
    tibemsXAResource            xaResource,
    tibems_int                  seconds);

extern tibems_status
tibemsXAResource_Start(
    tibemsXAResource            xaResource,
    XID*                        xid,
    tibems_int                  flags);

/*
 * Extension for mapping from Classic X/Open XA to JTA.
 *
 * Setting an RMID for an XAResource object identifies the XAResource object
 * to the "xa_" functions in the Classic X/Open XA interface.  The association
 * between a given RMID and the XAResource object is broken under the
 * following conditions:
 *
 *  closing the related XA Session object
 *  closing the related XA Connection object
 */
extern tibems_status
tibemsXAResource_SetRMID(
    tibemsXAResource            xaResource,
    tibems_int                  rmid);

extern tibems_status
tibemsXAResource_GetRMID(
    tibemsXAResource            xaResource,
    tibems_int*                 rmid);

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibemsxares_h */
