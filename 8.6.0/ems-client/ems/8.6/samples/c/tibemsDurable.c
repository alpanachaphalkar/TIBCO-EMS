/* 
 * Copyright (c) 2001-2019 TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 * 
 * $Id: tibemsDurable.c 108237 2019-03-18 21:34:01Z $
 * 
 */

/*
 * This is a simple sample of a topic durable subscriber.
 *
 * This sample subscribes to specified destination and
 * receives and prints all received messages.
 *
 * Notice that the specified destination should exist in your configuration
 * or your topics configuration file should allow
 * creation of the specified destination. 
 *
 * Usage:  tibemsDurable [options]
 *
 *    where options are:
 *
 *      -server       Server URL.
 *                    If not specified this sample assumes a
 *                    serverUrl of "tcp://localhost:7222"
 *      -user         User name. Default is null.
 *      -password     User password. Default is null.
 *      -topic        Topic name. Default is "topic.sample"
 *      -clientID     Connection Client ID. Default is null.
 *      -durable      Durable name. Default is "subscriber".
 *      -unsubscribe  Unsubscribe and quit.
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
char*                           topicName    = "topic.sample";
char*                           clientID     = NULL;
char*                           durableName  = "subscriber";
tibems_bool                     unsubscribe  = TIBEMS_FALSE;

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
    baseUtils_print("\nUsage: tibemsDurable [options] [ssl options]\n");
    baseUtils_print("\n");
    baseUtils_print("   where options are:\n");
    baseUtils_print("\n");
    baseUtils_print(" -server   <server URL> - EMS server URL, default is local server\n");
    baseUtils_print(" -user     <user name>  - user name, default is null\n");
    baseUtils_print(" -password <password>   - password, default is null\n");
    baseUtils_print(" -topic    <topic-name> - topic name, default is \"topic.sample\"\n");
    baseUtils_print(" -clientID <client-id>  - connection client ID, default is null\n");
    baseUtils_print(" -durable  <durable>    - durable name, default is \"subscriber\"\n");
    baseUtils_print(" -unsubscribe           - unsubscribe and quit\n");
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
        if (strcmp(argv[i],"-topic")==0) 
        {
            if ((i+1) >= argc) usage();
            topicName = argv[i+1];
            i += 2;
        }
        else
        if (strcmp(argv[i],"-clientID")==0) 
        {
            if ((i+1) >= argc) usage();
            clientID = argv[i+1];
            i += 2;
        }
        else
        if (strcmp(argv[i],"-durable")==0) 
        {
            if ((i+1) >= argc) usage();
            durableName = argv[i+1];
            i += 2;
        }
        else
        if (strcmp(argv[i],"-unsubscribe")==0) 
        {
            unsubscribe = TIBEMS_TRUE;
            i++;
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

/*-----------------------------------------------------------------------
 * run
 *----------------------------------------------------------------------*/
void run() 
{
    tibems_status               status      = TIBEMS_OK;
    tibemsMsg                   msg         = NULL;
    const char*                 txt         = NULL;
    tibemsMsgType               msgType     = TIBEMS_MESSAGE_UNKNOWN;
    char*                       msgTypeName = "UNKNOWN";
    
    if (!unsubscribe && !topicName) {
        baseUtils_print("***Error: must specify destination name\n");
        usage();
    }
    
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

    if(clientID)
    {
        status = tibemsConnectionFactory_SetClientID(factory,clientID);
        if (status != TIBEMS_OK) 
        {
            fail("Error setting client ID", errorContext);
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
        status = tibemsTopic_Create(&destination,topicName);
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

    if (unsubscribe)
    {
        baseUtils_print("Unsubscribing durable subscriber: '%s'\n\n",durableName);
        status = tibemsSession_Unsubscribe(session,durableName);
        if (status == TIBEMS_OK)
            baseUtils_print("Successfully unsubscribed %s\n",durableName);
        else
            fail("Error unsubscribing", errorContext);
        tibemsConnection_Close(connection);
        return;
    }
    
    baseUtils_print("Subscribing to destination: '%s'\n\n",topicName);

    /* create the consumer */
    status = tibemsSession_CreateDurableSubscriber(session,
            &msgConsumer,destination,durableName,NULL,TIBEMS_FALSE);
    if (status != TIBEMS_OK)
    {
        fail("Error creating tibemsMsgConsumer", errorContext);
    }
    
    /* start the connection */
    status = tibemsConnection_Start(connection);
    if (status != TIBEMS_OK)
    {
        fail("Error starting tibemsConnection", errorContext);
    }

    /* read messages */
    while(receive) 
    {
        /* receive the message */
        status = tibemsMsgConsumer_Receive(msgConsumer,&msg);
        if (status != TIBEMS_OK)
        {
            if (status == TIBEMS_INTR)
            {
                /* this means receive has been interrupted. This
                 * could happen if the connection has been terminated
                 * or the program closed the connection or the session.
                 * Since this program does not close anything, this 
                 * means the connection to the server is lost, it will
                 * be printed in the connection exception. So ignore it
                 * here.
                 */
                return;
            }
            fail("Error receiving message", errorContext);
        }
        if (!msg)
            break;

        /* check message type */
        status = tibemsMsg_GetBodyType(msg,&msgType);
        if (status != TIBEMS_OK)
        {
            fail("Error getting message type", errorContext);
        }

        switch(msgType)
        {
            case TIBEMS_MESSAGE:
                msgTypeName = "MESSAGE";
                break;

            case TIBEMS_BYTES_MESSAGE:
                msgTypeName = "BYTES";
                break;

            case TIBEMS_OBJECT_MESSAGE:
                msgTypeName = "OBJECT";
                break;

            case TIBEMS_STREAM_MESSAGE:
                msgTypeName = "STREAM";
                break;

            case TIBEMS_MAP_MESSAGE:
                msgTypeName = "MAP";
                break;

            case TIBEMS_TEXT_MESSAGE:
                msgTypeName = "TEXT";
                break;

            default:
                msgTypeName = "UNKNOWN";
                break;
        }

        /* publish sample sends TEXT message, if received other
         * just print entire message
         */
        if (msgType != TIBEMS_TEXT_MESSAGE)
        {
            baseUtils_print("Received %s message:\n",msgTypeName);
            tibemsMsg_Print(msg);
        }
        else
        {
            /* get the message text */
            status = tibemsTextMsg_GetText(msg,&txt);
            if (status != TIBEMS_OK)
            {
                fail("Error getting tibemsTextMsg text", errorContext);
            }

            baseUtils_print("Received TEXT message: %s\n",
                txt ? txt : "<text is set to NULL>");
        }

        /* destroy the message */
        status = tibemsMsg_Destroy(msg);
        if (status != TIBEMS_OK)
        {
            fail("Error destroying tibemsMsg", errorContext);
        }

    }
            
    /* destroy the destination */
    status = tibemsDestination_Destroy(destination);
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
    baseUtils_print("\n------------------------------------------------------------------------\n");
    baseUtils_print("tibemsMsgConsumer SAMPLE\n");
    baseUtils_print("------------------------------------------------------------------------\n");
    baseUtils_print("Server....................... %s\n",serverUrl?serverUrl:"localhost");
    baseUtils_print("User......................... %s\n",userName?userName:"(null)");
    baseUtils_print("Destination.................. %s\n",topicName);
    baseUtils_print("Durable...................... %s\n",durableName);
    baseUtils_print("------------------------------------------------------------------------\n\n");

    run();

    return 0;
}
