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
set M2_REPO=d:\java\maven-repository
set JAVA_HOME=d:\Java\jdk1.6.0_22-x64
set PATH=%JAVA_HOME%\bin
call cp.bat
java -Dorg.apache.chemistry.opencmis.binding.atompub.url=http://localhost:8080/inmemory/atom ^
-Dorg.apache.chemistry.opencmis.binding.spi.type=atompub ^
-Dorg.apache.chemistry.opencmis.user=jens ^
-Dorg.apache.chemistry.opencmis.password=dummy ^
-cp %CP% org.apache.chemistry.opencmis.tools.main.ObjGenApp %*
