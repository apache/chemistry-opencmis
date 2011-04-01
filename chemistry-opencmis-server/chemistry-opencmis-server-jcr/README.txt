CMIS server implementation on top of JCR
========================================

Getting started
---------------
To get a running instance on top of a transient JCR repository 
(Apache Jackrabbit) use the develop profile:

  mvn -o -Dlog4j.configuration=file:./log4j.config -Pdevelop jetty:run
  
This builds the web application with Apache Jackrabbit included and deploys it into
Jetty. The JCR repository is now available via CMIS at

  http://localhost:8080/chemistry-opencmis-server-jcr/atom
  
Use admin/admin to log in. 


Features and limitations
------------------------
General:
- Mapping is implemented as follows: JCR node type nt:file with JCR mixin mix:simpleVersionable 
  is mapped to CMIS object type cmis:document. JCR node type nt:file without JCR mixin 
  mix:simpleVersionable is mapped to CMIS object type cmis:unversioned-document. All other JCR
  node types are mapped to CMIS object type cmis:folder.
  
Versioning: 
- Each version of a CMIS document corresponds to a version in JCR

- A private working copy in CMIS corresponds to the actual node in JCR

- CMIS checkout/in are mapped to JCR checkout/in respectively

- CMIS cancelCheckout is mapped to a restore operation of the base version in JCR. 
  This has the side effect of creating a new version when canceling a checkout. 

- checkin comment is not supported

- All versions are major

- The JCR version name is mapped to the CMIS version label as follows: 
  if the JCR version name matches (\d+)(\.(\d+))?.* the value of the third group 
  is appended with ".0" and then used for the CMIS version label. Otherwise the JCR
  version name is used for the CMIS version label.

- Deleting of single versions is not supported

Query:
- CMIS IN_TREE and CMIS IN_FOLDER predicates must not occur more than once in the 
  WHERE clause. 
  
- CMIS IN_TREE and CMIS IN_FOLDER predicates may only occur in affirmative position 
  in the WHERE clause. A literal 'p' in a boolean expression 'X'is affirmative if 
  there exists a boolean expression 'Y' such that 'p' AND Y = X'. 
  
- <>, <, > comparison operators are not allowed on cmis:name

- CRX's Lucene index has some latency until it is up to date. This can cause queries
  to miss documents right after creating them.
  
- CMIS IN and CMIS ANY operators are not supported. 

- The following columns are not supported in queries: cmis:baseTypeId, 
  cmis:changeToken
  
- The following columns are not supported in queries for cmis:document and 
  cmis:unversioned-document: cmis:isImmutable, cmis:isLatestVersion, cmis:isMajorVersion,
  cmis:isLatestMajorVersion, cmis:versionLabel, cmis:versionSeriesId, 
  cmis:isVersionSeriesCheckedOut, cmis:versionSeriesCheckedOutId, 
  cmis:versionSeriesCheckedOutBy, cmis:checkinComment, cmis:contentStreamLength,
  cmis:contentStreamId
