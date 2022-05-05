/* 
 * Copyright (c) 2001-2020 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: types.h 129333 2020-10-13 01:07:14Z $
 * 
 */

#ifndef _INCLUDED_tibemstypes_h
#define _INCLUDED_tibemstypes_h

#if defined(__cplusplus)
extern "C" {
#endif

#define TIBEMS_DEFAULT_BROKER                   "tcp://localhost:7222"
#define TIBEMS_DEFAULT_HOST                     "localhost"
#define TIBEMS_DEFAULT_PORT                     "7222"
#define TIBEMS_DEFAULT_SSL_PORT                 "7243"
#define TIBEMS_DEFAULT_MDAEMON_URL              "tcp://localhost:7444,tcp://localhost:7444"

#define TIBEMS_DEFAULT_USER                     "anonymous"

#define TIBEMS_GROUP_ALL                        "$all"
#define TIBEMS_GROUP_ADMIN                      "$admin"
#define TIBEMS_USER_ADMIN                       "admin"

#define TIBEMS_UNDELIVERED_QUEUE                "$sys.undelivered"
#define TIBEMS_REDELIVERY_DELAY_QUEUE           "$sys.redelivery.delay"
#define TIBEMS_DELAY_QUEUE_PREFIX               "$sys.delayed."

#define TIBEMS_DEFAULT_DELIVERY_MODE            (TIBEMS_PERSISTENT)
#define TIBEMS_DEFAULT_PRIORITY                 ((tibems_int)4)
#define TIBEMS_DEFAULT_TIME_TO_LIVE             ((tibems_long)0)

#define JMS_TIBCO_COMPRESS                      "JMS_TIBCO_COMPRESS"

#define JMS_TIBCO_IMPORTED                      "JMS_TIBCO_IMPORTED"
#define JMS_TIBCO_MSG_EXT                       "JMS_TIBCO_MSG_EXT"

#define JMS_TIBCO_CM_PUBLISHER                  "JMS_TIBCO_CM_PUBLISHER"
#define JMS_TIBCO_CM_SEQUENCE                   "JMS_TIBCO_CM_SEQUENCE"

/* deprecated as of 8.6.0. */
#define JMS_TIBCO_SS_SENDER                     "JMS_TIBCO_SS_SENDER"
#define JMS_TIBCO_SS_DELIVERY_MODE              "JMS_TIBCO_SS_DELIVERY_MODE"
#define JMS_TIBCO_SS_EXPIRATION                 "JMS_TIBCO_SS_EXPIRATION"
#define JMS_TIBCO_SS_LB_MODE                    "JMS_TIBCO_SS_LB_MODE"
#define JMS_TIBCO_SS_MESSAGE_ID                 "JMS_TIBCO_SS_MESSAGE_ID"
#define JMS_TIBCO_SS_PRIORITY                   "JMS_TIBCO_SS_PRIORITY"
#define JMS_TIBCO_SS_SENDER_TIMESTAMP           "JMS_TIBCO_SS_SENDER_TIMESTAMP"
#define JMS_TIBCO_SS_TYPE_NUM                   "JMS_TIBCO_SS_TYPE_NUM"
#define JMS_TIBCO_SS_CORRELATION_ID             "JMS_TIBCO_SS_CORRELATION_ID"
#define JMS_TIBCO_SS_SEQ_NUM                    "JMS_TIBCO_SS_SEQ_NUM"
#define JMS_TIBCO_SS_USER_PROP                  "JMS_TIBCO_SS_USER_PROP"

#define JMS_TIBCO_SENDER                        "JMS_TIBCO_SENDER"
#define JMS_TIBCO_DISABLE_SENDER                "JMS_TIBCO_DISABLE_SENDER"

#define JMS_TIBCO_PRESERVE_UNDELIVERED          "JMS_TIBCO_PRESERVE_UNDELIVERED"

#define JMS_TIBCO_MSG_TRACE                     "JMS_TIBCO_MSG_TRACE"

#define TIBEMS_SERVER_NAME_MAX                  (64)
#define TIBEMS_USER_NAME_MAX                    (255)
#define TIBEMS_GROUP_NAME_MAX                   (255)
#define TIBEMS_DESTINATION_MAX                  (255)
#define TIBEMS_JNDI_NAME_MAX                    (255)
#define TIBEMS_DURABLE_MAX                      (255)
#define TIBEMS_CLIENTID_MAX                     (255)
#define TIBEMS_TPORT_NAME_MAX                   (64)

/*
 * Basic EMS types for header properties.
 */

/*
 * 64 bit integer support.
 *
 * If the type your compiler uses for a 64 bit integer
 * is not covered below, define TIBEMS_I64 to be
 * the type your compiler uses for 64 bit integers.
 */

#if !defined(TIBEMS_I64)
# if defined(_WIN32) || defined(__vms)
#  define TIBEMS_I64 __int64
# elif defined(__alpha)
#  define TIBEMS_I64 long
# else
#  define TIBEMS_I64 long long
# endif
#endif


typedef signed char                             tibems_byte;
typedef unsigned char                           tibems_ubyte;
typedef short                                   tibems_short;
typedef unsigned short                          tibems_ushort;
typedef unsigned short                          tibems_wchar;
typedef int                                     tibems_int;
typedef unsigned int                            tibems_uint;
typedef TIBEMS_I64                              tibems_long;
typedef unsigned TIBEMS_I64                     tibems_ulong;

typedef float                                   tibems_float;
typedef double                                  tibems_double;

#define TIBEMS_INT_MAX                          ((tibems_int)0x7fffffff)

typedef enum
{
    TIBEMS_FALSE  = 0,
    TIBEMS_TRUE   = 1
} tibems_bool;

typedef enum
{
    TIBEMS_UNKNOWN                              = 0,
    TIBEMS_QUEUE                                = 1,
    TIBEMS_TOPIC                                = 2,
    TIBEMS_DEST_UNDEFINED                       = 256

} tibemsDestinationType;


typedef enum
{
    TIBEMS_SESSION_TRANSACTED                   = 0,
    TIBEMS_AUTO_ACKNOWLEDGE                     = 1,
    TIBEMS_CLIENT_ACKNOWLEDGE                   = 2,
    TIBEMS_DUPS_OK_ACKNOWLEDGE                  = 3,

    TIBEMS_NO_ACKNOWLEDGE                       = 22,   /* Extensions */
    TIBEMS_EXPLICIT_CLIENT_ACKNOWLEDGE          = 23,
    TIBEMS_EXPLICIT_CLIENT_DUPS_OK_ACKNOWLEDGE  = 24

} tibemsAcknowledgeMode;


typedef enum
{
    TIBEMS_NON_PERSISTENT                       = 1,
    TIBEMS_PERSISTENT                           = 2,
    TIBEMS_RELIABLE                             = 22    /* Extension */

} tibemsDeliveryMode;

typedef enum
{
    NPSEND_CHECK_DEFAULT                        = 0,
    NPSEND_CHECK_ALWAYS                         = 1,
    NPSEND_CHECK_NEVER                          = 2,
    NPSEND_CHECK_TEMP_DEST                      = 3,
    NPSEND_CHECK_AUTH                           = 4,
    NPSEND_CHECK_TEMP_AUTH                      = 5
} tibemsNpCheckMode;

/* message type */

typedef enum
{
    TIBEMS_MESSAGE_UNKNOWN                      = 0,
    TIBEMS_MESSAGE                              = 1,
    TIBEMS_BYTES_MESSAGE                        = 2,
    TIBEMS_MAP_MESSAGE                          = 3,
    TIBEMS_OBJECT_MESSAGE                       = 4,
    TIBEMS_STREAM_MESSAGE                       = 5,
    TIBEMS_TEXT_MESSAGE                         = 6,
    TIBEMS_MESSAGE_UNDEFINED                    = 256

} tibemsMsgType;


typedef union
{
    tibems_bool                                 boolValue;
    tibems_byte                                 byteValue;
    tibems_short                                shortValue;
    tibems_wchar                                wcharValue;
    tibems_int                                  intValue;
    tibems_long                                 longValue;
    tibems_float                                floatValue;
    tibems_double                               doubleValue;
    char*                                       utf8Value;
    void*                                       bytesValue;
    struct __tibemsMsg*                         msgValue;
    void*                                       arrayValue;

} tibemsData;

#define TIBEMS_NULL                             0
#define TIBEMS_BOOL                             1
#define TIBEMS_BYTE                             2
#define TIBEMS_WCHAR                            3  /* double byte */
#define TIBEMS_SHORT                            4
#define TIBEMS_INT                              5
#define TIBEMS_LONG                             6
#define TIBEMS_FLOAT                            7
#define TIBEMS_DOUBLE                           8
/* Explicit size items */
#define TIBEMS_UTF8                             9   /* UTF8-encoded String */
#define TIBEMS_BYTES                            10
/* Extended MapMsg types */
#define TIBEMS_MAP_MSG                          11
#define TIBEMS_STREAM_MSG                       12
#define TIBEMS_SHORT_ARRAY                      20
#define TIBEMS_INT_ARRAY                        21
#define TIBEMS_LONG_ARRAY                       22
#define TIBEMS_FLOAT_ARRAY                      23
#define TIBEMS_DOUBLE_ARRAY                     24

typedef struct
{
    tibems_byte                 type;   /* one of TIBEMS_BOOL etc. */
    tibems_int                  size;   /* data size in bytes, 0 if can't tell */
    tibems_int                  count;  /* only for arrays: element count */
    tibemsData                  data;

} tibemsMsgField;

/*
 * EMS Objects Mappings
 */
typedef struct __tibemsMsg*                     tibemsMsg;

typedef struct __tibemsMsg*                     tibemsBytesMsg;
typedef struct __tibemsMsg*                     tibemsMapMsg;
typedef struct __tibemsMsg*                     tibemsObjectMsg;
typedef struct __tibemsMsg*                     tibemsStreamMsg;
typedef struct __tibemsMsg*                     tibemsTextMsg;

typedef struct __tibemsMsgEnum*                 tibemsMsgEnum;

typedef void*                                   tibemsConnectionFactory;

typedef void*                                   tibemsConnection;

/* deprecated as of 5.0. please use tibemsConnection */
typedef void*                                   tibemsTopicConnection;

/* deprecated as of 5.0. please use tibemsConnection */
typedef void*                                   tibemsQueueConnection;


typedef void*                                   tibemsSession;

/* deprecated as of 5.0. please use tibemsSession */
typedef void*                                   tibemsTopicSession;

/* deprecated as of 5.0. please use tibemsSession */
typedef void*                                   tibemsQueueSession;


typedef void*                                   tibemsDestination;
typedef void*                                   tibemsTopic;
typedef void*                                   tibemsTemporaryTopic;

typedef void*                                   tibemsQueue;
typedef void*                                   tibemsTemporaryQueue;


typedef void*                                   tibemsMsgProducer;
typedef void*                                   tibemsMsgConsumer;
typedef void*                                   tibemsMsgRequestor;


/* deprecated as of 5.0. please use tibemsMsgConsumer */
typedef void*                                   tibemsTopicSubscriber;

/* deprecated as of 5.0. please use tibemsMsgProducer */
typedef void*                                   tibemsTopicPublisher;

/* deprecated as of 5.0. please use tibemsMsgRequestor */
typedef void*                                   tibemsTopicRequestor;


typedef void*                                   tibemsQueueBrowser;

/* deprecated as of 5.0. please use tibemsMsgConsumer */
typedef void*                                   tibemsQueueReceiver;

/* deprecated as of 5.0. please use tibemsMsgProducer */
typedef void*                                   tibemsQueueSender;

/* deprecated as of 5.0. please use tibemsMsgRequestor */
typedef void*                                   tibemsQueueRequestor;


typedef void*                                   tibemsExceptionListener;

typedef void*                                   tibemsConnectionMetaData;

typedef void*                                   tibemsSSLParams;

typedef void*                                   tibemsXAResource;

typedef void*                                   tibemsLookupParams;

typedef void*                                   tibemsErrorContext;

#ifdef  __cplusplus
}
#endif

#endif /* _INCLUDED_tibemstypes_h */
