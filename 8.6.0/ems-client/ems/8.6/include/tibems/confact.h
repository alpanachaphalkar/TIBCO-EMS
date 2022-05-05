/* 
 * Copyright (c) 2001-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: confact.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibemsconnfactory_h
#define _INCLUDED_tibemsconnfactory_h

#include "types.h"
#include "status.h"
#include "cssl.h"

#if defined(__cplusplus)
extern "C" {
#endif

/* connection factory load balance metric types */
typedef enum __tibemsFactoryLoadBalanceMetric
{

    TIBEMS_FACTORY_LOAD_BALANCE_METRIC_NONE             = 0,
    TIBEMS_FACTORY_LOAD_BALANCE_METRIC_CONNECTIONS      = 1,
    TIBEMS_FACTORY_LOAD_BALANCE_METRIC_BYTE_RATE        = 2
    
} tibemsFactoryLoadBalanceMetric;

/* destroy the connection factory */
extern tibems_status
tibemsConnectionFactory_Destroy(
    tibemsConnectionFactory             factory);

extern tibems_status
tibemsConnectionFactory_CreateConnection(
    tibemsConnectionFactory             factory,
    tibemsConnection*                   connection,
    const char*                         username,
    const char*                         password);


extern tibems_status
tibemsConnectionFactory_CreateXAConnection(
    tibemsConnectionFactory             factory,
    tibemsConnection*                   connection,
    const char*                         username,
    const char*                         password);


extern tibemsConnectionFactory
tibemsConnectionFactory_Create(void);

extern tibems_status
tibemsConnectionFactory_SetServerURL(
    tibemsConnectionFactory             factory,
    const char*                         url);

extern tibems_status
tibemsConnectionFactory_SetClientID(
    tibemsConnectionFactory             factory,
    const char*                         cid);

extern tibems_status
tibemsConnectionFactory_SetMetric(
    tibemsConnectionFactory             factory,
    tibemsFactoryLoadBalanceMetric      metric);

extern tibems_status
tibemsConnectionFactory_SetConnectAttemptCount(
    tibemsConnectionFactory             factory,
    tibems_int                          connAttempts);

extern tibems_status
tibemsConnectionFactory_SetConnectAttemptDelay(
    tibemsConnectionFactory             factory,
    tibems_int                          delay);

extern tibems_status
tibemsConnectionFactory_SetConnectAttemptTimeout(
    tibemsConnectionFactory             factory,
    tibems_int                          connAttemptTimeout);

extern tibems_status
tibemsConnectionFactory_SetReconnectAttemptCount(
    tibemsConnectionFactory             factory,
    tibems_int                          connAttempts);

extern tibems_status
tibemsConnectionFactory_SetReconnectAttemptDelay(
    tibemsConnectionFactory             factory,
    tibems_int                          delay);

tibems_status
tibemsConnectionFactory_SetReconnectAttemptTimeout(
    tibemsConnectionFactory             factory,
    tibems_int                          reconnAttemptTimeout);

extern tibems_status
tibemsConnectionFactory_SetUserName(
    tibemsConnectionFactory        factory,
    const char*                    username);

extern tibems_status
tibemsConnectionFactory_SetUserPassword(
    tibemsConnectionFactory        factory,
    const char*                    password);

extern tibems_status
tibemsConnectionFactory_SetMulticastEnabled(
    tibemsConnectionFactory             factory,
    tibems_bool                         multicastEnabled);

extern tibems_status
tibemsConnectionFactory_SetMulticastDaemon(
    tibemsConnectionFactory             factory,
    const char*                         multicastDaemon);

/*
 * sslparams will be destroyed when factory is
 * destroyed in tibemsConnectionFactory_Destroy()
 */
extern tibems_status
tibemsConnectionFactory_SetSSLParams(
    tibemsConnectionFactory             factory,
    tibemsSSLParams                     sslparams);

extern tibems_status
tibemsConnectionFactory_SetPkPassword(
    tibemsConnectionFactory             factory,
    const char*                         pk_password);

extern tibems_status 
tibemsConnectionFactory_Print(
    tibemsConnectionFactory             factory);

extern tibems_status
tibemsConnectionFactory_PrintToBuffer(
    tibemsConnectionFactory             factory,
    char*                               buffer,
    tibems_int                          maxlen);

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibemsconnfactory_h */
