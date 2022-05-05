
/* 
 * Copyright (c) 2001-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: clookup.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibems_clookup_h
#define _INCLUDED_tibems_clookup_h

#include "types.h"
#include "status.h"
#include "dest.h"
#include "confact.h"

#if defined(__cplusplus)
extern "C" {
#endif



/*************************************************************************/
/* public types used by the API                                          */
/*************************************************************************/

typedef void*              tibemsLookupContext;

/*************************************************************************/
/* public API                                                            */
/*************************************************************************/
extern tibems_status
tibemsLookupContext_Create(
    tibemsLookupContext*    context,
    const char*             brokerURL,
    const char*             username,
    const char*             password);

extern tibems_status
tibemsLookupContext_CreateExternal(
    tibemsLookupContext*    context,
    tibemsLookupParams      lookupParams);

extern tibems_status
tibemsLookupContext_CreateSSL(
    tibemsLookupContext*    context,
    const char*             brokerURL,
    const char*             username,
    const char*             password,
    tibemsSSLParams         SSLparams,
    const char*             pk_password);

extern tibems_status
tibemsLookupContext_Destroy(
    tibemsLookupContext     context);
    

/* generic lookup any object type */
extern tibems_status
tibemsLookupContext_Lookup(
    tibemsLookupContext         context,
    const char*                 name,
    void**                      object);

/* lookup destination */
extern tibems_status
tibemsLookupContext_LookupDestination(
    tibemsLookupContext         context,
    const char*                 name,
    tibemsDestination*          destination);

/* lookup connection factory */
extern tibems_status
tibemsLookupContext_LookupConnectionFactory(
    tibemsLookupContext         context,
    const char*                 name,
    tibemsConnectionFactory*    factory);


extern tibemsLookupParams
tibemsLookupParams_Create(void);

extern void
tibemsLookupParams_Destroy(
    tibemsLookupParams      lparams);

extern tibems_status
tibemsLookupParams_SetLdapServerUrl(
    tibemsLookupParams      lparams,
    const char*             url);

extern char*
tibemsLookupParams_GetLdapServerUrl(
    tibemsLookupParams      lparams);

extern tibems_status
tibemsLookupParams_SetLdapBaseDN(
    tibemsLookupParams      lparams,
    const char*             basedn);

extern tibems_status
tibemsLookupParams_SetLdapPrincipal(
    tibemsLookupParams      lparams,
    const char*             principal);

extern tibems_status
tibemsLookupParams_SetLdapCredential(
    tibemsLookupParams      lparams,
    const char*             credential);

extern tibems_status
tibemsLookupParams_SetLdapSearchScope(
    tibemsLookupParams      lparams,
    const char*             scope);

extern tibems_status
tibemsLookupParams_SetLdapConnType(
    tibemsLookupParams      lparams,
    const char*             type);

extern tibems_status
tibemsLookupParams_SetLdapCAFile(
    tibemsLookupParams      lparams,
    const char*             file);

extern tibems_status
tibemsLookupParams_SetLdapCAPath(
    tibemsLookupParams      lparams,
    const char*             path);

extern tibems_status
tibemsLookupParams_SetLdapCertFile(
    tibemsLookupParams      lparams,
    const char*             file);

extern tibems_status
tibemsLookupParams_SetLdapKeyFile(
    tibemsLookupParams      lparams,
    const char*             file);

extern tibems_status
tibemsLookupParams_SetLdapRandFile(
    tibemsLookupParams      lparams,
    const char*             file);

extern tibems_status
tibemsLookupParams_SetLdapCiphers(
    tibemsLookupParams      lparams,
    const char*             ciphers);

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibems_clookup_h */

