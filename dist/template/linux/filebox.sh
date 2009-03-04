#!/bin/sh
JAVA=java
OSGI_FRAMEWORK=plugins/org.eclipse.osgi_3.4.0.v20080605-1900.jar
VMOPTIONS="-Declipse.ignoreApp=true -Dosgi.noShutdown=true -Dosgi.install.area=."
OPTIONS="-console"
$JAVA $VMOPTIONS -jar $OSGI_FRAMEWORK $OPTIONS