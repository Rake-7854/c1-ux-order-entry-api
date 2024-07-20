#!/bin/bash

CP_REPO_ROOT="/mnt/c/git/custompoint"
APP_CLASSPATH="./target/api-1.0.0-SNAPSHOT.jar:$CP_REPO_ROOT/Properties"

# CMD_LINE_ARGS="-Xms256m -Xmx2048m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./logs/heap-dump.hprof"
CMD_LINE_ARGS="-Xms256m -Xmx2048m"
# CMD_LINE_ARGS="$CMD_LINE_ARGS -Dcom.sun.management.jmxremote -classpath $APP_CLASSPATH $JAVA_OPTS $JAVA_VER_BASED_OPTS"
CMD_LINE_ARGS="$CMD_LINE_ARGS -Dcom.sun.management.jmxremote -cp $APP_CLASSPATH $JAVA_OPTS -Dspring.config.location=./src/main/resources/application.yml"
# CMD_LINE_ARGS=$CMD_LINE_ARGS -Djava.opts=$JAVA_OPTS" -Djava.io.tmpdir="./tmp" -Dcom.sun.tools.javac.main.largebranch=true -Dorg.apache.commons.collections.enableUnsafeSerialization=true"
CMD_LINE_ARGS="$CMD_LINE_ARGS -Djava.opts=$JAVA_OPTS -Dcom.sun.tools.javac.main.largebranch=true -Dorg.apache.commons.collections.enableUnsafeSerialization=true"
CMD_LINE_ARGS="$CMD_LINE_ARGS -Dserver.port=8443 -Dserver.contextPath=/ -Dserver.servlet.contextPath=/"
CMD_LINE_ARGS="$CMD_LINE_ARGS -Dtt.arch.env.var=X4 -DXS.SERVER.TOKEN=WSAD -DXS.SERVER.ID=1 -Djxl.nogc=true -DXS.APP.LOCALE=en_US -Dfile.encoding=UTF8 -DCP_ENV=RAD -DENV_CODE=WSAD"
CMD_LINE_ARGS="$CMD_LINE_ARGS -Dtt.ParmCheckClass=com.wallace.atwinxs.framework.util.AtWinXSParmChecker -Dtt.PortalCookieClass=com.rrd.custompoint.framework.component.AuthServicesProcessorImpl -Dtt.SessionAuditClass=com.wallace.atwinxs.admin.dao.LoginAuditDAO -Dtt.SiteClass=com.wallace.atwinxs.framework.dao.LoginProcessDAO -Dtt.UserClass=com.wallace.atwinxs.framework.dao.LoginProcessDAO "
CMD_LINE_ARGS="$CMD_LINE_ARGS -DSERVER_LOG_ROOT=/mnt/c/xs2files/Logfiles"
#CMD_LINE_ARGS="$CMD_LINE_ARGS -Dtranslation=/mnt/c/xs2files/translation
#CMD_LINE_ARGS="$CMD_LINE_ARGS -Dmssqldriver=$CP_REPO_ROOT/Servers/Runtimes/sqlserver/sqljdbc4.jar"
#CMD_LINE_ARGS="$CMD_LINE_ARGS -Dthirdparty=$CP_REPO_ROOT/ThirdParty"
#CMD_LINE_ARGS="$CMD_LINE_ARGS -Dwurfl=$CP_REPO_ROOT/Servers/Runtimes/wurfl"
#CMD_LINE_ARGS="$CMD_LINE_ARGS -Djaxws=$CP_REPO_ROOT/Servers/Runtimes/jaxws-ri-2.2.10"
#CMD_LINE_ARGS="$CMD_LINE_ARGS -Dproperties=$CP_REPO_ROOT/Properties"
CMD_LINE_ARGS="$CMD_LINE_ARGS -Dmqconnector=$CP_REPO_ROOT/Servers/Runtimes/mq"
CMD_LINE_ARGS="$CMD_LINE_ARGS -Ddb2pwd_path=$CP_REPO_ROOT/Servers/Runtimes/conf/db2_pwd.txt"
CMD_LINE_ARGS="$CMD_LINE_ARGS -Djava.endorsed.dirs=$JAVA_ENDORSED -Dhttpclient.hostnameVerifier=DefaultAndLocalhost"

# run Java
echo "JAVA_HOME environment variable is set to: $JAVA_HOME"
echo "CMD_LINE_ARGS is set to: $CMD_LINE_ARGS"
java $CMD_LINE_ARGS -Dloader.main=com.rrd.c1ux.api.ApiApplication org.springframework.boot.loader.PropertiesLauncher

