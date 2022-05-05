/* 
 * Copyright (c) 2001-2019 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: tibemsAdmin.c 108237 2019-03-18 21:34:01Z $
 * 
 */

/*
 * This is a simple sample of a basic tibemsAdmin client.
 *
 *
 * Usage:  tibemsAdmin  [options]
 *
 *  where options are:
 *
 *   -server    <server-url>  Server URL.
 *                            If not specified this sample assumes a
 *                            serverUrl of "tcp://localhost:7222"
 *   -user      <user-name>   User name. Default is null.
 *   -password  <password>    User password. Default is null.
 *
 */

#include "tibemsUtilities.h"

/*-----------------------------------------------------------------------
 * Parameters
 *----------------------------------------------------------------------*/

char*                           serverUrl    = NULL;
char*                           userName     = "admin";
char*                           password     = NULL;
char*                           pk_password  = NULL;

/*-----------------------------------------------------------------------
 * Variables
 *----------------------------------------------------------------------*/

tibemsAdmin                     admin        = TIBEMS_INVALID_ADMIN_ID;
tibemsServerInfo                serverInfo   = TIBEMS_INVALID_ADMIN_ID;
tibemsQueueInfo                 queueInfo    = TIBEMS_INVALID_ADMIN_ID;
tibemsCollection                queueInfos   = NULL;
tibemsSSLParams                 sslParams    = NULL;

tibemsErrorContext              errorContext = NULL;

/*-----------------------------------------------------------------------
 * usage
 *----------------------------------------------------------------------*/
void usage() 
{
    baseUtils_print("\nUsage: tibemsAdmin [options] [ssl options]\n");
    baseUtils_print("\n");
    baseUtils_print("   where options are:\n");
    baseUtils_print("\n");
    baseUtils_print(" -server   <server URL> - EMS server URL, default is local server\n");
    baseUtils_print(" -user     <user name>  - user name, default is null\n");
    baseUtils_print(" -password <password>   - password, default is null\n");
    baseUtils_print(" -help-ssl              - help on ssl parameters\n");
    exit(0);
}

/*-----------------------------------------------------------------------
 * parseArgs
 *----------------------------------------------------------------------*/
void parseArgs(int argc, char** argv) 
{
    tibems_int                  i = 1;
    
    sslParams = tibemsSSLParams_Create();
    
    setSSLParams(sslParams,argc,argv, &pk_password);

    while(i < argc)
    {
        if (!argv[i]) 
        {
            i += 1;
        }
        else
        if (strcmp(argv[i],"-help")==0) 
        {
            usage();
        }
        else
        if (strcmp(argv[i],"-help-ssl")==0) 
        {
            sslUsage();
        }
        else
        if (strcmp(argv[i],"-server")==0) 
        {
            if ((i+1) >= argc) usage();
            serverUrl = argv[i+1];
            i += 2;
        }
        else
        if (strcmp(argv[i],"-user")==0) 
        {
            if ((i+1) >= argc) usage();
            userName = argv[i+1];
            i += 2;
        }
        else
        if (strcmp(argv[i],"-password")==0) 
        {
            if ((i+1) >= argc) usage();
            password = argv[i+1];
            i += 2;
        }
        else 
        {
            baseUtils_print("Unrecognized parameter: %s\n",argv[i]);
            usage();
        }
    }

    if(!serverUrl || strncmp(serverUrl,"ssl",3))
    {
        tibemsSSLParams_Destroy(sslParams);
        sslParams = NULL;
    }
}

/*---------------------------------------------------------------------
 * fail
 *---------------------------------------------------------------------*/
void fail(
    const char*                 message, 
    tibemsErrorContext          errContext)
{
    tibems_status               status = TIBEMS_OK;
    const char*                 str    = NULL;

    baseUtils_print("ERROR: %s\n",message);

    status = tibemsErrorContext_GetLastErrorString(errContext, &str);
    baseUtils_print("\nLast error message =\n%s\n", str);
    status = tibemsErrorContext_GetLastErrorStackTrace(errContext, &str);
    baseUtils_print("\nStack trace = \n%s\n", str);

    exit(0);
}

/*-----------------------------------------------------------------------
 * run
 *----------------------------------------------------------------------*/
void run() 
{
    tibems_status               status = TIBEMS_OK;
    tibems_int                  count;
    char                        nameBuf[1024];

    status = tibemsErrorContext_Create(&errorContext);

    if (status != TIBEMS_OK)
    {
        baseUtils_print("ErrorContext create failed: %s\n", tibemsStatus_GetText(status));
        exit(1);
    }

    status = tibemsAdmin_Create(&admin, serverUrl, userName, password, sslParams);

    if (status != TIBEMS_OK)
    {
        fail("Error creating tibemsAdmin connection", errorContext);
    }

    status = tibemsAdmin_GetInfo(admin, &serverInfo);
    if (status != TIBEMS_OK)
    {
        fail("Error getting tibemsServerInfo", errorContext);
    }

    status = tibemsServerInfo_GetQueueCount(serverInfo, &count);
    if (status != TIBEMS_OK)
    {
        fail("Error getting queue count", errorContext);
    }
    baseUtils_print("Queue Count = %d\n", count);

    status = tibemsServerInfo_GetTopicCount(serverInfo, &count);
    if (status != TIBEMS_OK)
    {
        fail("Error getting topic count", errorContext);
    }
    baseUtils_print("Topic Count = %d\n", count);

    status = tibemsServerInfo_GetProducerCount(serverInfo, &count);
    if (status != TIBEMS_OK)
    {
        fail("Error getting producer count", errorContext);
    }
    baseUtils_print("Producer Count = %d\n", count);

    status = tibemsServerInfo_GetConsumerCount(serverInfo, &count);
    if (status != TIBEMS_OK)
    {
        fail("Error getting consumer count", errorContext);
    }
    baseUtils_print("Consumer Count = %d\n", count);

    status = tibemsAdmin_GetQueues(admin, &queueInfos, ">", TIBEMS_DEST_GET_NOTEMP);
    if (status != TIBEMS_OK)
    {
        fail("Error getting queue collection", errorContext);
    }

    status = tibemsCollection_GetFirst(queueInfos, (&queueInfo));
    if (status != TIBEMS_OK)
    {
        fail("Error getting first queue in collection", errorContext);
    }

    status = tibemsQueueInfo_GetName(queueInfo, nameBuf, sizeof(nameBuf));
    if (status != TIBEMS_OK)
    {
        fail("Error getting first queue name", errorContext);
    }
    baseUtils_print("queue name of first queue in collection = %s\n", nameBuf);

    while (status != TIBEMS_NOT_FOUND)
    {
        status = tibemsCollection_GetNext(queueInfos, &queueInfo);
        if (status == TIBEMS_NOT_FOUND)
        {
            status = TIBEMS_OK;
            break;
        }
        if (status != TIBEMS_OK)
        {
            fail("Error getting next queue in collection", errorContext);
        }

        status = tibemsQueueInfo_GetName(queueInfo, nameBuf, sizeof(nameBuf));
        if (status != TIBEMS_OK)
        {
            fail("Error getting next queue name", errorContext);
        }
        baseUtils_print("queue name of next queue in collection = %s\n", nameBuf);

        tibemsQueueInfo_Destroy(queueInfo);
    }

    status = tibemsAdmin_Close(admin);
    if (status != TIBEMS_OK)
    {
        fail("Error closing tibemsAdmin connection", errorContext);
    }

    /* destroy the ssl params */
    if (sslParams) 
    {
        tibemsSSLParams_Destroy(sslParams);
    }

    tibemsCollection_Destroy(queueInfos); 
    tibemsServerInfo_Destroy(serverInfo);

    tibemsErrorContext_Close(errorContext);
}
    
/*-----------------------------------------------------------------------
 * main
 *----------------------------------------------------------------------*/
int main(int argc, char** argv)
{
    parseArgs(argc, argv);

    /* print parameters */
    baseUtils_print("\n------------------------------------------------------------------------\n");
    baseUtils_print("tibemsAdmin SAMPLE\n");
    baseUtils_print("------------------------------------------------------------------------\n");
    baseUtils_print("Server....................... %s\n",serverUrl?serverUrl:"localhost");
    baseUtils_print("User......................... %s\n",userName?userName:"(null)");
    baseUtils_print("------------------------------------------------------------------------\n\n");

    run();

    return 0;
}
