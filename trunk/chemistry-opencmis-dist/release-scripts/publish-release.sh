#!/bin/bash
#Publishes dist artifacts and stages CMS site to production

# Version to release
VERSION=$1
TARGET_DIST_DIR=/www/www.apache.org/dist/chemistry/opencmis/${VERSION}

# Deploys dist packages
echo "Deploying dist packages"
mkdir ${TARGET_DIST_DIR} 
cp ~/public_html/chemistry/opencmis/${VERSION}/dist/* ${TARGET_DIST_DIR}

echo "Publishing site with CMS"
publish.pl chemistry ${USER}

echo "Release pushed"

