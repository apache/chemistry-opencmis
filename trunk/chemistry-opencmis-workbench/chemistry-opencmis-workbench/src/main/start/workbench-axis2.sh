#!/bin/sh

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# @version@

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


SCRIPT_DIR=$(dirname "$0")
cd "$SCRIPT_DIR/lib"

WCP="."
for i in *.jar; do
  WCP="$i:${WCP}"
done

# use variable CUSTOM_JAVA_OPTS to set additional JAVA options

# uncomment the following lines to configure HTTP proxy

# export http_proxy=http://<proxy>:<port>
# export https_proxy=https://<proxy>:<port>
# export no_proxy=localhost,127.0.0.0,.local


JAVA_PROXY_CONF=$($JAVA -classpath $WCP org.apache.chemistry.opencmis.workbench.ProxyDetector -j -s)
JAVA_OPTS="$JAVA_PROXY_CONF -Dorg.apache.chemistry.opencmis.binding.webservices.jaxws.impl=axis2"

exec $JAVA $JAVA_OPTS $CUSTOM_JAVA_OPTS -classpath $WCP org.apache.chemistry.opencmis.workbench.Workbench &