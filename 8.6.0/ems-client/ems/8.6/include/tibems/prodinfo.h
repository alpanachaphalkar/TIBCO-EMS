/* 
 * Copyright (c) 2007-2016 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: prodinfo.h 90180 2016-12-13 23:00:37Z $
 * 
 */

#ifndef _INCLUDED_tibems_prodinfo_h
#define _INCLUDED_tibems_prodinfo_h

#include "types.h"
#include "status.h"

#if defined(__cplusplus)
extern "C" {
#endif

extern tibems_status 
tibemsProducerInfo_GetCreateTime(
    tibemsProducerInfo          producerInfo,
    tibems_long*                created);

extern tibems_status 
tibemsProducerInfo_GetDestinationName(
    tibemsProducerInfo          producerInfo,
    char*                       destName,
    tibems_int                  name_len);

extern tibems_status 
tibemsProducerInfo_GetDestinationType(
    tibemsProducerInfo          producerInfo,
    tibemsDestinationType*      destType);

extern tibems_status 
tibemsProducerInfo_GetDetailedStatistics(
    tibemsProducerInfo          producerInfo,
    tibemsCollection*           details);

extern tibems_status
tibemsProducerInfo_GetID(
    tibemsProducerInfo          producerInfo,
    tibems_long*                id);

extern tibems_status
tibemsProducerInfo_GetStatistics(
    tibemsProducerInfo          producerInfo,
    tibemsStatData*             stat);

extern tibems_status 
tibemsProducerInfo_Destroy(
    tibemsProducerInfo          producerInfo);

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibems_prodinfo_h */
