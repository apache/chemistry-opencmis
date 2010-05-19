package org.apache.chemistry.opencmis.commons.server;

public interface ObjectInfoHandler {

	ObjectInfo getObjectInfo(String repositoryId, String objectId);

	void addObjectInfo(ObjectInfo objectInfo);
}
