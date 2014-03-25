#! /bin/sh
#  /etc/init.d/wsd

### BEGIN INIT INFO
# Provides:          wsd
# Required-Start:
# Required-Stop:
# Should-Start:
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Starts the Weather Station service
# Description:       This file is used to start the daemon
#                    and should be placed in /etc/init.d
### END INIT INFO

# Author:   Victor Su
# Date:     11/28/2013

# =========================================================
#              CHANGE THESE VALUES IF NEEDED

# The location of the configuration file or blank for default:
DAEMON_CONFIG_FILE=""

# =========================================================

NAME="wsd"
DESC="Weather Station Server"

# The path to Jsvc
EXEC="/usr/bin/jsvc"

# The path to the folder containing server jar
FILE_PATH="/usr/local/bin/$NAME"

# The path to the folder containing the java runtime
JAVA_HOME="/usr/lib/jvm/jdk-7-oracle-armhf/"

# Our classpath including our jar file and the Apache Commons Daemon library
CLASS_PATH="$FILE_PATH/lib/wsd-0.1.0-SNAPSHOT.jar:$FILE_PATH/lib/*"

# The fully qualified name of the class to execute
CLASS="com.vsu.wsd.WSDaemon"

# Any command line arguments to be passed to the our Java Daemon implementations init() method
ARGS=""

# add config file if set
if [ ! -z "$DAEMON_CONFIG_FILE" ]; then
   ARGS="${ARGS} ${DAEMON_CONFIG_FILE}"
fi

# The file that will contain our process identification number (pid) for other scripts/programs that need to access it.
PID="/var/run/$NAME.pid"

# The log path
LOG_PATH="/tmp"

# System.out writes to this file...
LOG_OUT="$LOG_PATH/$NAME.out"

# System.err writes to this file...
LOG_ERR="$LOG_PATH/$NAME.err"

jsvc_exec()
{
    $EXEC -home $JAVA_HOME -cwd $FILE_PATH -Djava.library.path=/usr/lib/jni -cp $CLASS_PATH -outfile $LOG_OUT -errfile $LOG_ERR -pidfile $PID $1 $CLASS $ARGS
}

case "$1" in
    start)
        echo "Starting the $DESC..."

        # Clean up log files
        if [ -f "$LOG_OUT" ]; then
            rm "$LOG_OUT"
        fi

        if [ -f "$LOG_ERR" ]; then
            rm "$LOG_ERR"
        fi

        # Start the service
        jsvc_exec

        echo "The $DESC has started."
    ;;
    stop)
        echo "Stopping the $DESC..."

        # Stop the service
        jsvc_exec "-stop"

        echo "The $DESC has stopped."
    ;;
    restart)
        if [ -f "$PID" ]; then

            echo "Restarting the $DESC..."

            # Stop the service
            jsvc_exec "-stop"

            # Start the service
            jsvc_exec

            echo "The $DESC has restarted."
        else
            echo "Daemon not running, no action taken"
            exit 1
        fi
            ;;
    *)
    echo "Usage: /etc/init.d/$NAME {start|stop|restart}" >&2
    exit 3
    ;;
esac

