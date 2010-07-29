rem
rem
rem Licensed to the Apache Software Foundation (ASF) under one
rem or more contributor license agreements.  See the NOTICE file
rem distributed with this work for additional information
rem regarding copyright ownership.  The ASF licenses this file
rem to you under the Apache License, Version 2.0 (the
rem "License"); you may not use this file except in compliance
rem with the License.  You may obtain a copy of the License at
rem
rem   http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing,
rem software distributed under the License is distributed on an
rem "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
rem KIND, either express or implied.  See the License for the
rem specific language governing permissions and limitations
rem under the License.
rem
rem
set M2_REPO=c:\java\maven-repository
set JAVA_HOME=C:\Java\jdk.1.6.0_14
set PATH=%JAVA_HOME%\bin

java -Dopencmis.test.atompub.url=http://localhost:8080/opencmis/atom -cp ^
./target/classes;^
%M2_REPO%/javax/activation/activation/1.1/activation-1.1.jar;^
%M2_REPO%/javax/xml/bind/jaxb-api/2.1/jaxb-api-2.1.jar;^
%M2_REPO%/javax/xml/ws/jaxws-api/2.1/jaxws-api-2.1.jar;^
%M2_REPO%/javax/jws/jsr181-api/1.0-MR1/jsr181-api-1.0-MR1.jar;^
%M2_REPO%/javax/annotation/jsr250-api/1.0/jsr250-api-1.0.jar;^
%M2_REPO%/javax/xml/soap/saaj-api/1.3/saaj-api-1.3.jar;^
%M2_REPO%/javax/xml/stream/stax-api/1.0/stax-api-1.0.jar;^
%M2_REPO%/commons-codec/commons-codec/1.4/commons-codec-1.4.jar;^
%M2_REPO%/commons-logging/commons-logging/1.1.1/commons-logging-1.1.1.jar;^
%M2_REPO%/com/sun/xml/bind/jaxb-impl/2.1.11/jaxb-impl-2.1.11.jar;^
%M2_REPO%/com/sun/xml/ws/jaxws-rt/2.1.7/jaxws-rt-2.1.7.jar;^
%M2_REPO%/net/sf/jopt-simple/jopt-simple/3.2/jopt-simple-3.2.jar;^
%M2_REPO%/junit/junit/4.8.1/junit-4.8.1.jar;^
%M2_REPO%/org/jvnet/mimepull/1.3/mimepull-1.3.jar;^
%M2_REPO%/com/sun/org/apache/xml/internal/resolver/20050927/resolver-20050927.jar;^
%M2_REPO%/com/sun/xml/messaging/saaj/saaj-impl/1.3.3/saaj-impl-1.3.3.jar;^
%M2_REPO%/stax/stax-api/1.0.1/stax-api-1.0.1.jar;^
%M2_REPO%/org/jvnet/staxex/stax-ex/1.2/stax-ex-1.2.jar;^
%M2_REPO%/com/sun/xml/stream/buffer/streambuffer/0.9/streambuffer-0.9.jar;^
%M2_REPO%/org/codehaus/woodstox/wstx-asl/3.2.3/wstx-asl-3.2.3.jar;^
%M2_REPO%/org/apache/chemistry/opencmis/chemistry-opencmis-client-bindings/0.1-incubating-SNAPSHOT/chemistry-opencmis-client-bindings-0.1-incubating-SNAPSHOT.jar;^
%M2_REPO%/org/apache/chemistry/opencmis/chemistry-opencmis-commons-impl/0.1-incubating-SNAPSHOT/chemistry-opencmis-commons-impl-0.1-incubating-SNAPSHOT.jar;^
%M2_REPO%/org/apache/chemistry/opencmis/chemistry-opencmis-commons-api/0.1-incubating-SNAPSHOT/chemistry-opencmis-commons-api-0.1-incubating-SNAPSHOT.jar;^
%M2_REPO%/org/apache/chemistry/opencmis/chemistry-opencmis-test-util/0.1-incubating-SNAPSHOT/chemistry-opencmis-test-util-0.1-incubating-SNAPSHOT.jar ^
org.apache.chemistry.opencmis.util.repository.ObjGenApp %*
