/* 
 * Copyright (c) 2001-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: cssl.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibems_cssl_h
#define _INCLUDED_tibems_cssl_h

#if defined(__HOS_MVS__) || defined(__OS400__)
#include "cissl.h"
#else

#include "types.h"
#include "status.h"

#if defined(__cplusplus)
extern "C" {
#endif

#define TIBEMS_SSL_TRACE_ENV            "TIBEMS_SSL_TRACE"
#define TIBEMS_SSL_DEBUG_TRACE_ENV      "TIBEMS_SSL_DEBUG_TRACE"

#define TIBEMS_SSL_ENCODING_AUTO        (0x0000)
#define TIBEMS_SSL_ENCODING_PEM         (0x0001)
#define TIBEMS_SSL_ENCODING_DER         (0x0002)
#define TIBEMS_SSL_ENCODING_BER         (0x0004)
#define TIBEMS_SSL_ENCODING_PKCS7       (0x0010)
#define TIBEMS_SSL_ENCODING_PKCS8       (0x0020)
#define TIBEMS_SSL_ENCODING_PKCS12      (0x0040)
#define TIBEMS_SSL_ENCODING_ENTRUST     (0x0100)
#define TIBEMS_SSL_ENCODING_KEYSTORE    (0x0200)

#define TIBEMS_SSL_MIN_RENEGOTIATE_SIZE       (64L*1024L) /* bytes */
#define TIBEMS_SSL_MIN_RENEGOTIATE_INTERVAL   (15) /* in seconds */

typedef tibems_status
(*tibemsSSLHostNameVerifier) (
    const char*     connected_hostname,
    const char*     expected_hostname,
    const char*     certificate_name,
    void*           closure);

extern const char*
tibemsSSL_OpenSSLVersion(
    char*       buffer,
    tibems_int  bufsize);

extern void
tibemsSSL_SetTrace(
    tibems_bool             trace);

extern void
tibemsSSL_SetDebugTrace(
    tibems_bool             trace);

extern tibems_bool
tibemsSSL_GetTrace(void);

extern tibems_bool
tibemsSSL_GetDebugTrace(void);

extern tibemsSSLParams
tibemsSSLParams_Create(void);

extern void
tibemsSSLParams_Destroy(
    tibemsSSLParams         params);

extern tibems_status
tibemsSSLParams_SetCiphers(
    tibemsSSLParams         params,
    const char*             ciphers);

extern tibems_status
tibemsSSLParams_SetIdentity(
    tibemsSSLParams         params,
    const void*             data,
    tibems_int              size,
    tibems_int              encoding);

extern tibems_status
tibemsSSLParams_GetIdentity(
    tibemsSSLParams         params,
    const void**            data,
    tibems_int*             size,
    tibems_int*             encoding);

extern tibems_status
tibemsSSLParams_SetIdentityFile(
    tibemsSSLParams         params,
    const char*             filename,
    tibems_int              encoding);

extern tibems_status
tibemsSSLParams_AddIssuerCert(
    tibemsSSLParams         params,
    const void*             data,
    tibems_int              size,
    tibems_int              encoding);

extern tibems_status
tibemsSSLParams_AddIssuerCertFile(
    tibemsSSLParams         params,
    const char*             filename,
    tibems_int              encoding);

extern tibems_status
tibemsSSLParams_SetPrivateKey(
    tibemsSSLParams         params,
    const void*             data,
    tibems_int              size,
    tibems_int              encoding);

extern tibems_status
tibemsSSLParams_GetPrivateKey(
    tibemsSSLParams         params,
    const void**            data,
    tibems_int*             size,
    tibems_int*             encoding);

extern tibems_status
tibemsSSLParams_SetPrivateKeyFile(
    tibemsSSLParams         params,
    const char*             filename,
    tibems_int              encoding);

extern tibems_status
tibemsSSLParams_AddTrustedCert(
    tibemsSSLParams         params,
    const void*             data,
    tibems_int              size,
    tibems_int              encoding);

extern tibems_status
tibemsSSLParams_AddTrustedCertFile(
    tibemsSSLParams         params,
    const char*             filename,
    tibems_int              encoding);

extern tibems_status
tibemsSSLParams_SetAuthOnly(
    tibemsSSLParams         params,
    tibems_bool             auth_only);

extern tibems_status
tibemsSSLParams_SetVerifyHost(
    tibemsSSLParams         params,
    tibems_bool             verify);

extern tibems_status
tibemsSSLParams_SetVerifyHostName(
    tibemsSSLParams         params,
    tibems_bool             verify);

extern tibems_status
tibemsSSLParams_SetHostNameVerifier(
    tibemsSSLParams         params,
    tibemsSSLHostNameVerifier   verifier,
    const void*             closure);

extern tibems_status
tibemsSSLParams_SetExpectedHostName(
    tibemsSSLParams         params,
    const char*             expected_hostname);

extern tibems_status
tibemsSSLParams_SetRenegotiateInterval(
    tibemsSSLParams         params,
    tibems_long             milliseconds);

extern tibems_status
tibemsSSLParams_SetRenegotiateSize(
    tibemsSSLParams         params,
    tibems_long             bytes);

extern tibems_status
tibemsSSLParams_SetRandFile(
    tibemsSSLParams         params,
    const char*             rand_file);

extern tibems_status
tibemsSSLParams_SetRandEGD(
    tibemsSSLParams         params,
    const char*             rand_egd_path);

extern tibems_status
tibemsSSLParams_SetRandData(
    tibemsSSLParams         params,
    const char*             rand_data,
    tibems_int              size);

extern tibems_status
tibemsConnection_CreateSSL(
    tibemsConnection*       connection,
    const char*             brokerURL,
    const char*             clientId,
    const char*             username,
    const char*             password,
    tibemsSSLParams         params,
    const char*             pk_password);

/* deprecated as of 5.0. please use tibemsConnection_CreateSSL */
extern tibems_status
tibemsQueueConnection_CreateSSL(
    tibemsQueueConnection*  queueConnection,
    const char*             brokerURL,
    const char*             clientId,
    const char*             username,
    const char*             password,
    tibemsSSLParams         params,
    const char*             pk_password);

/* deprecated as of 5.0. please use tibemsConnection_CreateSSL */
extern tibems_status
tibemsTopicConnection_CreateSSL(
    tibemsTopicConnection*  topicConnection,
    const char*             brokerURL,
    const char*             clientId,
    const char*             username,
    const char*             password,
    tibemsSSLParams         params,
    const char*             pk_password);

#ifdef  __cplusplus
}
#endif

#endif /* defined(__HOS_MVS__) || defined(__OS400__) */

#endif /* _INCLUDED_tibems_cssl_h */
