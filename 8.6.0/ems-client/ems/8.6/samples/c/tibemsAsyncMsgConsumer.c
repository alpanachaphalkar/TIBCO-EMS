/* 
 * Copyright (c) 2001-2019 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: tibemsAsyncMsgConsumer.c 108237 2019-03-18 21:34:01Z $
 * 
 */

/*
 * This is a simple sample of a basic asynchronous 
 * tibemsMsgConsumer.
 *
 * This sample subscribes to specified destination and
 * receives and prints all received messages.
 * 
 * This sample quits when 'Enter' is pressed.
 *
 * Notice that the specified destination should exist in your configuration
 * or your topics/queues configuration file should allow
 * creation of the specified destination. 
 *
 * If this sample is used to receive messages published by 
 * tibemsMsgProducer sample, it must be started prior
 * to running the tibemsMsgProducer sample.
 *
 * Usage:  tibemsAsyncMsgConsumer [options]
 *
 *    where options are:
 *
 *      -server     Server URL.
 *                  If not specified this sample assumes a
 *                  serverUrl of "tcp://localhost:7222"
 *
 *      -user       User name. Default is null.
 *      -password   User password. Default is null.
 *      -topic      Topic name. Default is "topic.sample"
 *      -queue      Queue name. No default
 * 
 *
 */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "tibemsUtilities.h"

/*-----------------------------------------------------------------------
 * Parameters
 *----------------------------------------------------------------------*/

char*                           serverUrl    = NULL;
char*                           userName     = NULL;
char*                           password     = NULL;
char*                           pk_password  = NULL;
char*                           name         = "topic.sample";
tibems_bool                     useTopic     = TIBEMS_TRUE;

/*-----------------------------------------------------------------------
 * Variables
 *----------------------------------------------------------------------*/
tibemsConnectionFactory         factory      = NULL;
tibemsConnection                connection   = NULL;
tibemsSession                   session      = NULL;
tibemsMsgConsumer               msgConsumer  = NULL;
tibemsDestination               destination  = NULL;   

tibemsSSLParams                 sslParams    = NULL;
tibems_int                      receive      = 1;

tibemsErrorContext              errorContext = NULL;

/*-----------------------------------------------------------------------
 * usage
 *----------------------------------------------------------------------*/
void usage() 
{
    baseUtils_print("\nUsage: tibemsAsyncMsgConsumer [options] [ssl options]\n");
    baseUtils_print("\n");
    baseUtils_print("   where options are:\n");
    baseUtils_print("\n");
    baseUtils_print(" -server   <server URL> - EMS server URL, default is local server\n");
    baseUtils_print(" -user     <user name>  - user name, default is null\n");
    baseUtils_print(" -password <password>   - password, default is null\n");
    baseUtils_print(" -topic    <topic-name> - topic name, default is \"topic.sample\"\n");
    baseUtils_print(" -queue    <queue-name> - queue name, no default\n");
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
        if (strcmp(argv[i],"-topic")==0) 
        {
            if ((i+1) >= argc) usage();
            name = argv[i+1];
            i += 2;
        }
        else
        if (strcmp(argv[i],"-queue")==0) 
        {
            if ((i+1) >= argc) usage();
            name = argv[i+1];
            useTopic = TIBEMS_FALSE;
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

    exit(1);
}

/*---------------------------------------------------------------------
 * onException
 *---------------------------------------------------------------------*/
void onException(
    tibemsConnection            conn,
    tibems_status               reason,
    void*                       closure)
{
    /* print the connection exception status */

    if (reason == TIBEMS_SERVER_NOT_CONNECTED)
    {
        baseUtils_print("CONNECTION EXCEPTION: Server Disconnected\n");
        receive = 0;
    }
}

/*---------------------------------------------------------------------
 * onMessage
 *---------------------------------------------------------------------*/
void onMessage(
    tibemsMsgConsumer           consumer,
    tibemsMsg                   msg,
    void*                       closure)
{
    tibems_status               status  = TIBEMS_OK;
    const char*                 txt     = NULL;
    tibemsMsgType               type    = TIBEMS_MESSAGE;

    /* read messages */
    if (!msg)
        baseUtils_print("Received a NULL message!");

    /* get the message type */
    status = tibemsMsg_GetBodyType(msg, &type);
    if (status != TIBEMS_OK)
    {
        fail("Error getting message type", errorContext);
    }
    
    if (type == TIBEMS_TEXT_MESSAGE)
    {
        /* get the message text */
        status = tibemsTextMsg_GetText(msg,&txt);
        if (status != TIBEMS_OK)
        {
            fail("Error getting tibemsTextMsg text", errorContext);
        }
        else
            baseUtils_print("Received TEXT message: %s\n", txt);
    }
    else
    {
        baseUtils_print("Received message:\n");
        tibemsMsg_Print(msg);
    }
        
    /* destroy the message */
    status = tibemsMsg_Destroy(msg);
    if (status != TIBEMS_OK)
    {
        fail("Error destroying tibemsMsg", errorContext);
    }
}

/*-----------------------------------------------------------------------
 * run
 *----------------------------------------------------------------------*/
void run() 
{
    tibems_status               status = TIBEMS_OK;
    
    if (!name) {
        baseUtils_print("***Error: must specify destination name\n");
        usage();
    }
    
    baseUtils_print("Subscribing to destination: '%s'\n",name);
    
    status = tibemsErrorContext_Create(&errorContext);

    if (status != TIBEMS_OK)
    {
        baseUtils_print("ErrorContext create failed: %s\n", tibemsStatus_GetText(status));
        exit(1);
    }

    factory = tibemsConnectionFactory_Create();
    if (!factory)
    {
        fail("Error creating tibemsConnectionFactory", errorContext);
    }

    status = tibemsConnectionFactory_SetServerURL(factory,serverUrl);
    if (status != TIBEMS_OK) 
    {
        fail("Error setting server url", errorContext);
    }

    /* create the connection, use ssl if specified */
    if(sslParams)
    {
        status = tibemsConnectionFactory_SetSSLParams(factory,sslParams);
        if (status != TIBEMS_OK) 
        {
            fail("Error setting ssl params", errorContext);
        }
        status = tibemsConnectionFactory_SetPkPassword(factory,pk_password);
        if (status != TIBEMS_OK) 
        {
            fail("Error setting pk password", errorContext);
        }
    }

    status = tibemsConnectionFactory_CreateConnection(factory,&connection,
                                                      userName,password);
    if (status != TIBEMS_OK)
    {
        fail("Error creating tibemsConnection", errorContext);
    }

    /* set the exception listener */
    status = tibemsConnection_SetExceptionListener(connection,
            onException, NULL);
    if (status != TIBEMS_OK)
    {
        fail("Error setting exception listener", errorContext);
    }

    /* create the destination */
    if (useTopic)
        status = tibemsTopic_Create(&destination,name);
    else
        status = tibemsQueue_Create(&destination,name);
    if (status != TIBEMS_OK)
    {
        fail("Error creating tibemsDestination", errorContext);
    }

    /* create the session */
    status = tibemsConnection_CreateSession(connection,
            &session,TIBEMS_FALSE,TIBEMS_AUTO_ACKNOWLEDGE);
    if (status != TIBEMS_OK)
    {
        fail("Error creating tibemsSession", errorContext);
    }
        
    /* create the consumer */
    status = tibemsSession_CreateConsumer(session,
            &msgConsumer,destination,NULL,TIBEMS_FALSE);
    if (status != TIBEMS_OK)
    {
        fail("Error creating tibemsMsgConsumer", errorContext);
    }

    /* set the message listener */
    status = tibemsMsgConsumer_SetMsgListener(msgConsumer, onMessage, NULL);
    if (status != TIBEMS_OK)
    {
        fail("Error setting message listener", errorContext);
    }
    
    /* start the connection */
    status = tibemsConnection_Start(connection);
    if (status != TIBEMS_OK)
    {
        fail("Error starting tibemsConnection", errorContext);
    }

    /* Wait while callbacks are invoked */
    while (receive)
    {
        tibems_Sleep(1000);
    }

    /* destroy the destination */
    status = tibemsTopic_Destroy(destination);
    if (status != TIBEMS_OK)
    {
        fail("Error destroying tibemsDestination", errorContext);
    }

    /* close the connection */
    status = tibemsConnection_Close(connection);
    if (status != TIBEMS_OK)
    {
        fail("Error closing tibemsConnection", errorContext);
    }

    /* destroy the ssl params */
    if (sslParams) 
    {
        tibemsSSLParams_Destroy(sslParams);
    }

    tibemsErrorContext_Close(errorContext);
}
    
/*-----------------------------------------------------------------------
 * main
 *----------------------------------------------------------------------*/
int main(int argc, char** argv)
{
    parseArgs(argc,argv);

    /* print parameters */
    baseUtils_print("------------------------------------------------------------------------\n");
    baseUtils_print("tibemsAsyncMsgConsumer SAMPLE\n");
    baseUtils_print("------------------------------------------------------------------------\n");
    baseUtils_print("Server....................... %s\n",serverUrl?serverUrl:"localhost");
    baseUtils_print("User......................... %s\n",userName?userName:"(null)");
    baseUtils_print("Destination.................. %s\n",name);
    baseUtils_print("------------------------------------------------------------------------\n\n");

    run();

    return 0;
}
