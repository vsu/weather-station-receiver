#!/bin/bash

case `uname` in
  CYGWIN*)
    CP=$( echo `dirname $0`/../lib/*.jar . | sed 's/ /;/g')
    ;;
  *)
    CP=$( echo `dirname $0`/../lib/*.jar . | sed 's/ /:/g')
esac
#echo $CP

# Find Java
if [ "$JAVA_HOME" = "" ] ; then
    JAVA="java"
else
    JAVA="$JAVA_HOME/bin/java"
fi

# Set Java options
if [ "$JAVA_OPTIONS" = "" ] ; then
    JAVA_OPTIONS="-Xms32m -Xmx128m"
fi

# Launch the application
$JAVA $JAVA_OPTIONS -Djava.library.path=/usr/lib/jni -cp $CP:$CLASSPATH com.vsu.wsd.Application $@

# Return the program's exit code
exit $?
