#!/bin/sh
# Uploads wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER} chemistry commodity packages for apache.org/dist release vote
# Script to be run from the target staging folder in people.apache.org

# Version to release
VERSION=$1
# Example of relative folder chemistry-013/org/apache/chemistry/opencmis
STAGING_FOLDER=$2
STAGING_REPO=https://repository.apache.org/content/repositories


 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-workbench/${VERSION}/chemistry-opencmis-workbench-${VERSION}-full.tar.gz             
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-workbench/${VERSION}/chemistry-opencmis-workbench-${VERSION}-full.tar.gz.asc        
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-workbench/${VERSION}/chemistry-opencmis-workbench-${VERSION}-full.tar.gz.md5    
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-workbench/${VERSION}/chemistry-opencmis-workbench-${VERSION}-full.tar.gz.asc.sha1  
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-workbench/${VERSION}/chemistry-opencmis-workbench-${VERSION}-full.tar.gz.md5       
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-workbench/${VERSION}/chemistry-opencmis-workbench-${VERSION}-full.tar.gz.sha1      
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-workbench/${VERSION}/chemistry-opencmis-workbench-${VERSION}-full.zip              
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-workbench/${VERSION}/chemistry-opencmis-workbench-${VERSION}-full.zip.asc          
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-workbench/${VERSION}/chemistry-opencmis-workbench-${VERSION}-full.zip.asc.md5      
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-workbench/${VERSION}/chemistry-opencmis-workbench-${VERSION}-full.zip.asc.sha1     
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-workbench/${VERSION}/chemistry-opencmis-workbench-${VERSION}-full.zip.md5          
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-workbench/${VERSION}/chemistry-opencmis-workbench-${VERSION}-full.zip.sha1   
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-docs.tar.gz                                      
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-docs.tar.gz.asc                                  
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-docs.tar.gz.asc.md5                              
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-docs.tar.gz.asc.sha1                             
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-docs.tar.gz.md5                                  
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-docs.tar.gz.sha1                                 
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
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-impl/${VERSION}/chemistry-opencmis-client-impl-${VERSION}-with-dependencies.tar.gz.asc        
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-impl/${VERSION}/chemistry-opencmis-client-impl-${VERSION}-with-dependencies.tar.gz.asc.md5    
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-impl/${VERSION}/chemistry-opencmis-client-impl-${VERSION}-with-dependencies.tar.gz.asc.sha1  
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-impl/${VERSION}/chemistry-opencmis-client-impl-${VERSION}-with-dependencies.tar.gz.md5       
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-impl/${VERSION}/chemistry-opencmis-client-impl-${VERSION}-with-dependencies.tar.gz.sha1      
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-impl/${VERSION}/chemistry-opencmis-client-impl-${VERSION}-with-dependencies.zip              
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-impl/${VERSION}/chemistry-opencmis-client-impl-${VERSION}-with-dependencies.zip.asc          
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-impl/${VERSION}/chemistry-opencmis-client-impl-${VERSION}-with-dependencies.zip.asc.md5      
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-impl/${VERSION}/chemistry-opencmis-client-impl-${VERSION}-with-dependencies.zip.asc.sha1     
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-impl/${VERSION}/chemistry-opencmis-client-impl-${VERSION}-with-dependencies.zip.md5          
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-client-impl/${VERSION}/chemistry-opencmis-client-impl-${VERSION}-with-dependencies.zip.sha1         
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-client.tar.gz                             
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-client.tar.gz.asc                         
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-client.tar.gz.asc.md5                      
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-client.tar.gz.asc.sha1                     
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-client.tar.gz.md5                          
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-client.tar.gz.sha1                         
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-client.zip                                  
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-client.zip.asc                              
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-client.zip.asc.md5                          
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-client.zip.asc.sha1                        
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-client.zip.md5                             
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-client.zip.sha1                            
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-server-webapps.tar.gz                     
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-server-webapps.tar.gz.asc                 
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-server-webapps.tar.gz.asc.md5             
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-server-webapps.tar.gz.asc.sha1            
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-server-webapps.tar.gz.md5
 wget --no-check-certificate ${STAGING_REPO}/${STAGING_FOLDER}/chemistry-opencmis-dist/${VERSION}/chemistry-opencmis-dist-${VERSION}-server-webapps.tar.gz.sha1                
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
