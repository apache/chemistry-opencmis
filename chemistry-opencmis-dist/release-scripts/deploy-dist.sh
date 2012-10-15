#!/bin/sh
# Uploads wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER} chemistry commodity packages for apache.org/dist release vote

# Version to release
VERSION=$1
# Example of relative folder orgapachechemistry-013/org/apache/chemistry/opencmis
STAGING_FOLDER=$2
STAGING_REPO=https://repository.apache.org/content/repositories


 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-workbench/${VERSION}/chemistry-opencmis-workbench-${VERSION}-full.zip.asc.sha1
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-workbench/${VERSION}/chemistry-opencmis-workbench-${VERSION}-full.zip.md5
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-workbench/${VERSION}/chemistry-opencmis-workbench-${VERSION}-full.zip.sha1
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-docs.zip
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-docs.zip.asc
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-docs.zip.asc.md5
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-docs.zip.asc.sha1
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-docs.zip.md5
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-docs.zip.sha1
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis/${VERSION}/chemistry-opencmis-${VERSION}-source-release.zip
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis/${VERSION}/chemistry-opencmis-${VERSION}-source-release.zip.asc
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis/${VERSION}/chemistry-opencmis-${VERSION}-source-release.zip.asc.md5
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis/${VERSION}/chemistry-opencmis-${VERSION}-source-release.zip.asc.sha1
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis/${VERSION}/chemistry-opencmis-${VERSION}-source-release.zip.md5
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis/${VERSION}/chemistry-opencmis-${VERSION}-source-release.zip.sha1
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-impl/${VERSION}/chemistry-opencmis-client-impl-${VERSION}-with-dependencies.tar.gz
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-impl/${VERSION}/chemistry-opencmis-client-impl-${VERSION}-with-dependencies.zip.asc.md5
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-impl/${VERSION}/chemistry-opencmis-client-impl-${VERSION}-with-dependencies.zip.asc.sha1
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-impl/${VERSION}/chemistry-opencmis-client-impl-${VERSION}-with-dependencies.zip.md5
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-impl/${VERSION}/chemistry-opencmis-client-impl-${VERSION}-with-dependencies.zip.sha1
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-client.zip
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-client.zip.asc
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-client.zip.asc.md5
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-client.zip.asc.sha1
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-client.zip.md5
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-client.zip.sha1
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-server-webapps.zip
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-server-webapps.zip.asc
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-server-webapps.zip.asc.md5
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-server-webapps.zip.asc.sha1
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-server-webapps.zip.md5
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-server-webapps.zip.sha1
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-server-bindings/${VERSION}/chemistry-opencmis-server-bindings-${VERSION}.war
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-server-bindings/${VERSION}/chemistry-opencmis-server-bindings-${VERSION}.war.asc
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-server-bindings/${VERSION}/chemistry-opencmis-server-bindings-${VERSION}.war.asc.md5
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-server-bindings/${VERSION}/chemistry-opencmis-server-bindings-${VERSION}.war.asc.sha1
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-server-bindings/${VERSION}/chemistry-opencmis-server-bindings-${VERSION}.war.md5
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-server-bindings/${VERSION}/chemistry-opencmis-server-bindings-${VERSION}.war.sha1
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-server-jcr/${VERSION}/chemistry-opencmis-server-jcr-${VERSION}.war
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-server-jcr/${VERSION}/chemistry-opencmis-server-jcr-${VERSION}.war.asc
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-server-jcr/${VERSION}/chemistry-opencmis-server-jcr-${VERSION}.war.asc.md5
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-server-jcr/${VERSION}/chemistry-opencmis-server-jcr-${VERSION}.war.asc.sha1
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-server-jcr/${VERSION}/chemistry-opencmis-server-jcr-${VERSION}.war.md5
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-server-jcr/${VERSION}/chemistry-opencmis-server-jcr-${VERSION}.war.sha1

 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-osgi-server/${VERSION}/chemistry-opencmis-osgi-server-${VERSION}.jar
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-osgi-server/${VERSION}/chemistry-opencmis-osgi-server-${VERSION}.jar.asc
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-osgi-server/${VERSION}/chemistry-opencmis-osgi-server-${VERSION}.jar.asc.md5
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-osgi-server/${VERSION}/chemistry-opencmis-osgi-server-${VERSION}.jar.asc.sha1
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-osgi-server/${VERSION}/chemistry-opencmis-osgi-server-${VERSION}.jar.md5
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-osgi-server/${VERSION}/chemistry-opencmis-osgi-server-${VERSION}.jar.sha1

 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-osgi-client/${VERSION}/chemistry-opencmis-osgi-client-${VERSION}.jar
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-osgi-client/${VERSION}/chemistry-opencmis-osgi-client-${VERSION}.jar.asc
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-osgi-client/${VERSION}/chemistry-opencmis-osgi-client-${VERSION}.jar.asc.md5
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-osgi-client/${VERSION}/chemistry-opencmis-osgi-client-${VERSION}.jar.asc.sha1
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-osgi-client/${VERSION}/chemistry-opencmis-osgi-client-${VERSION}.jar.md5
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-osgi-client/${VERSION}/chemistry-opencmis-osgi-client-${VERSION}.jar.sha1

 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-android-client/${VERSION}/chemistry-opencmis-android-client-${VERSION}-pack.zip
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-android-client/${VERSION}/chemistry-opencmis-android-client-${VERSION}-pack.zip.asc
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-android-client/${VERSION}/chemistry-opencmis-android-client-${VERSION}-pack.zip.md5
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-android-client/${VERSION}/chemistry-opencmis-android-client-${VERSION}-pack.zip.sha1
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-android-client/${VERSION}/chemistry-opencmis-android-client-${VERSION}-pack.zip.asc.md5

 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-bindings-weblogic/${VERSION}/chemistry-opencmis-client-bindings-weblogic-${VERSION}-pack.zip
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-bindings-weblogic/${VERSION}/chemistry-opencmis-client-bindings-weblogic-${VERSION}-pack.zip.asc
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-bindings-weblogic/${VERSION}/chemistry-opencmis-client-bindings-weblogic-${VERSION}-pack.zip.md5
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-bindings-weblogic/${VERSION}/chemistry-opencmis-client-bindings-weblogic-${VERSION}-pack.zip.sha1
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-bindings-weblogic/${VERSION}/chemistry-opencmis-client-bindings-weblogic-${VERSION}-pack.zip.asc.md5

 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-bindings-websphere/${VERSION}/chemistry-opencmis-client-bindings-websphere-${VERSION}-pack.zip
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-bindings-websphere/${VERSION}/chemistry-opencmis-client-bindings-websphere-${VERSION}-pack.zip.asc
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-bindings-websphere/${VERSION}/chemistry-opencmis-client-bindings-websphere-${VERSION}-pack.zip.md5
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-bindings-websphere/${VERSION}/chemistry-opencmis-client-bindings-websphere-${VERSION}-pack.zip.sha1
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-bindings-websphere/${VERSION}/chemistry-opencmis-client-bindings-websphere-${VERSION}-pack.zip.asc.md5

 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-bridge/${VERSION}/chemistry-opencmis-bridge-0.8.0.war
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-bridge/${VERSION}/chemistry-opencmis-bridge-0.8.0.war.asc
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-bridge/${VERSION}/chemistry-opencmis-bridge-0.8.0.war.md5
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-bridge/${VERSION}/chemistry-opencmis-bridge-0.8.0.war.sha1
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-bridge/${VERSION}/chemistry-opencmis-bridge-0.8.0.war.asc.md5