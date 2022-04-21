/* 
 * Copyright (c) 2007-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: deststat.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibems_deststat_h
#define _INCLUDED_tibems_deststat_h

#include "types.h"
#include "status.h"

#if defined(__cplusplus)
extern "C" {
#endif

extern tibems_status 
tibemsDetailedDestStat_GetDestinationName(
    tibemsDetailedDestStat      detailedDestStat,
    char*                       destName,
    tibems_int                  name_len);

extern tibems_status 
tibemsDetailedDestStat_GetDestinationType(
    tibemsDetailedDestStat      detailedDestStat,
    tibemsDestinationType*      destType);

extern tibems_status 
tibemsDetailedDestStat_GetStatData(
    tibemsDetailedDestStat      detailedDestStat,
    tibemsStatData*             stat);

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibems_deststat_h */
