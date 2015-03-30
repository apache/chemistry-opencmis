
if [ -z "$JAVA_HOME" ]; then
  j=$(which java 2>/dev/null)
  if [ -z "$j" ]; then
 	echo "Unable to locate Java!"
    exit 1
  else
    JAVA="$j"
  fi
else
  JAVA="$JAVA_HOME/bin/java"
fi


. testtool.classpath.sh

JAVA_OPTS="-Dorg.apache.chemistry.opencmis.binding.atompub.url=http://localhost:8080/inmemory/atom \
-Dorg.apache.chemistry.opencmis.binding.spi.type=atompub \
-Dorg.apache.chemistry.opencmis.user=jens \
-Dorg.apache.chemistry.opencmis.password=dummy"

JAVA_OPTS="$JAVA_OPTS -Djava.net.useSystemProxies=true"


if [ -n "$http_proxy" ]; then
  HTTP_PROXY_HOST=$(echo $http_proxy | sed 's/http:\/\/\(.*\):.*/\1/')
  HTTP_PROXY_PORT=$(echo $http_proxy | sed 's/http:\/\/.*:\(.*\)/\1/')
  JAVA_OPTS="$JAVA_OPTS -Dhttp.proxyHost=$HTTP_PROXY_HOST -Dhttp.proxyPort=$HTTP_PROXY_PORT"
fi

if [ -n "$https_proxy" ]; then
  HTTPS_PROXY_HOST=$(echo $https_proxy | sed 's/http:\/\/\(.*\):.*/\1/')
  HTTPS_PROXY_PORT=$(echo $https_proxy | sed 's/http:\/\/.*:\(.*\)/\1/')
  JAVA_OPTS="$JAVA_OPTS -Dhttps.proxyHost=$HTTPS_PROXY_HOST -Dhttps.proxyPort=$HTTPS_PROXY_PORT"
fi

echo $CP
exec $JAVA $JAVA_OPTS -classpath $CP org.apache.chemistry.opencmis.tools.main.ObjGenApp $*
