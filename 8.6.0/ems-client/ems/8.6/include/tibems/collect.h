/* 
 * Copyright (c) 2007-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: collect.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibems_collect_h
#define _INCLUDED_tibems_collect_h

#include "types.h"
#include "status.h"

#if defined(__cplusplus)
extern "C" {
#endif

extern tibems_status 
tibemsCollection_GetFirst(
    tibemsCollection            collection,
    tibemsCollectionObj*        obj);

extern tibems_status 
tibemsCollection_GetNext(
    tibemsCollection            collection,
    tibemsCollectionObj*        obj);

extern tibems_status 
tibemsCollection_GetCount(
    tibemsCollection            collection,
    tibems_int*                 count);

extern tibems_status 
tibemsCollection_Destroy(
    tibemsCollection            collection);

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibems_collect_h */
