 =================================================================
 TIBCO Enterprise Message Service
 Copyright (c) 2001-2013 TIBCO Software Inc.
 ALL RIGHTS RESERVED
 =================================================================

This file describes the Central Admin logger configuration. The sample
configuration is specified in the `logger.properties` file. To use this
file you'll need to modify at the very least the `java.util.logging.FileHandler.pattern`
property to have the path on your machine where you want the logs to be
written:

```
java.util.logging.FileHandler.pattern = /some/directory/path/logs/emsca_%u.log
```

On Windows installations, specify any path using forward slashes:
```
java.util.logging.FileHandler.pattern = c:/some/directory/path/logs/emsca_%u.log
```

Finally, to inform the JVM of your logging configuration you'll have to edit
the emsca launch script as described later in the document in the section titled
`Specifying the logging configuration to EMSCA`.


EMSCA Logger Configuration
=========================

The EMSCA server uses java logger configuration. It also provides 
Jetty logger bridge so that logging done by the Jetty component of the 
application is property logged to the standard Java logger.

The Java logger api and its configuration is extensively documented
<http://docs.oracle.com/javase/1.5.0/docs/guide/logging/overview.html>
by Oracle, and provides flexible logger setup. You can configure handlers 
(console, file, etc) to log using different log levels.

The standard Java Log Levels are, in descending order:

```
ALL (turn all logging)
SEVERE (highest value)
WARNING
INFO
CONFIG
FINE
FINER
FINEST (lowest value)
OFF (turns logging off)
```

The standard configuration will associate a logger handler with its log level:

```
handler = level
```

One important observation is the Jetty logging machinery supports different 
logging levels.  Valid Jetty log levels in descending order are:

```
ALL
WARN
INFO
DEBUG
OFF
```

EMSCA provides a JettyToJava logger named "com.tibco.messaging.emsca.jetty" which
is set as the default logger for Jetty. This logger supports the following levels, which
map to the above Jetty log levels.

```
ALL
WARNING
INFO
FINEST
OFF
```

To configure the jetty logger for EMSCA simply a line with the desired log level:

```
com.tibco.messaging.emsca.jetty.level = level
```


Specifying the logging configuration to EMSCA
---------------------------------------------

Because Java logging is a JVM option, it is specified as a java property. 
You will have to modify the EMSCA launcher to add the option 
`-Djava.util.logging.config.file=/path/to/this/file` on the launcher script:

[On UNIX]
```
exec java -Djava.util.logging.config.file=/path/to/logger/file.properties -classpath ..."
```

[On Windows]
```
java java -Djava.util.logging.config.file=c:/path/to/logger/file.properties -classpath ...
```
