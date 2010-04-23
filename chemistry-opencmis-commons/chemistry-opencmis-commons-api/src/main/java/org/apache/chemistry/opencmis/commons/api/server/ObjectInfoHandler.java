package org.apache.chemistry.opencmis.commons.api.server;

public interface ObjectInfoHandler {

	ObjectInfo getObjectInfo(String repositoryId, String objectId);

	void addObjectInfo(ObjectInfo objectInfo);
}
