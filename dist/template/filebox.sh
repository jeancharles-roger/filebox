#!/bin/sh
JAVA=java
OSGI_FRAMEWORK=plugins/org.eclipse.equinox.launcher_1.0.100.v20080509-1800.jar
VMOPTIONS="-Declipse.ignoreApp=false -Dosgi.noShutdown=false"
OPTIONS="-console -application org.kawane.filebox.ui.filebox"
$JAVA $VMOPTIONS -jar $OSGI_FRAMEWORK $OPTIONS