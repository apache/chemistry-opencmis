package org.apache.opencmis.inmemory.storedobj.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.commons.provider.ProviderObjectFactory;
import org.apache.opencmis.inmemory.FilterParser;
import org.apache.opencmis.inmemory.NameValidator;
import org.apache.opencmis.inmemory.storedobj.api.Document;
import org.apache.opencmis.inmemory.storedobj.api.Folder;
import org.apache.opencmis.inmemory.storedobj.api.Path;
import org.apache.opencmis.inmemory.storedobj.api.StoredObject;
import org.apache.opencmis.inmemory.storedobj.api.VersionedDocument;

public class FolderImpl extends AbstractPathImpl implements Folder {
  private static final Log LOG = LogFactory.getLog(AbstractPathImpl.class.getName());

  FolderImpl(ObjectStoreImpl objStore) {
    super(objStore);
  }

  public FolderImpl(ObjectStoreImpl objStore, String name, Folder parent) {
    super(objStore);
    init(name, parent);
  }

  public void addChildFolder(Folder folder) {
    boolean hasChild;
    String name = folder.getName();
    hasChild = hasChild(name);
    if (hasChild)
      throw new RuntimeException("Cannot create folder " + name
          + ". Name already exists in parent folder");
    folder.setParent(this);
    fObjStore.storeObject(folder.getPath(), folder);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.spi.inmemory.IFolder#addChildDocument(org.apache.opencmis.client.provider
   * .spi.inmemory.storedobj.impl.DocumentImpl)
   */
  public void addChildDocument(Document doc) {
    addChildObject(doc);
  }

  public void addChildDocument(VersionedDocument doc) {
    addChildObject(doc);
  }
  
  private void addChildObject(StoredObject so) {
    String name = so.getName();
    if (!NameValidator.isValidId(name))
      throw new IllegalArgumentException(NameValidator.ERROR_ILLEGAL_NAME);

    boolean hasChild;
    hasChild = hasChild(name);
    if (hasChild)
      throw new RuntimeException("Cannot create document " + name
          + ". Name already exists in parent folder");

    String documentPath = getPath();
    if (documentPath.equals(PATH_SEPARATOR))
      documentPath += name;
    else
      documentPath += PATH_SEPARATOR + name;
    ((Path)so).setParent(this);
    fObjStore.storeObject(documentPath, so);    
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.inmemory.IFolder#getChildren()
   */
  public List<StoredObject> getChildren(int maxItems, int skipCount) {
    List<StoredObject> result = new ArrayList<StoredObject>();
    String path = getPath();
    for (String id : fObjStore.getIds()) {
      StoredObject obj = fObjStore.getObject(id);
      if (obj instanceof Path) {
        Path pathObj = (Path) obj;
        if (pathObj.getParent() != null && path.equals(pathObj.getParent().getPath()))
            result.add(obj);
      }
    }
    sortFolderList(result);

    if (maxItems < 0)
      maxItems = result.size();
    if (skipCount < 0)
      skipCount = 0;
    int from = Math.min(skipCount, result.size());
    int to = Math.min(maxItems + from, result.size());
    result = result.subList(from, to);
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.inmemory.IFolder#getFolderChildren()
   */
  public List<Folder> getFolderChildren(int maxItems, int skipCount) {
    List<Folder> result = new ArrayList<Folder>();
    String path = getPath();
    for (String id : fObjStore.getIds()) {
      StoredObject obj = fObjStore.getObject(id);
      if (obj instanceof Path) {
        Path pathObj = (Path) obj;
        if (pathObj.getParent() != null && path.equals(pathObj.getParent().getPath())
            && pathObj instanceof Folder)
          result.add((Folder)obj);
        
      }
    }
    sortFolderList(result);
    int from = Math.min(skipCount, result.size());
    int to = Math.min(maxItems + from, result.size());
    result = result.subList(from, to);
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.inmemory.IFolder#hasChild(java.lang.String)
   */
  public boolean hasChild(String name) {
    String path = getPath();
    if (path.equals(PATH_SEPARATOR))
      path = path + name;
    else
      path = path + PATH_SEPARATOR + name;
    for (String objPath : fObjStore.getIds()) {
      if (path.equals(objPath))
        return true;
    }
    return false;
  }

  public void fillProperties(List<PropertyData<?>> properties, ProviderObjectFactory objFactory,
      List<String> requestedIds) {

    super.fillProperties(properties, objFactory, requestedIds);

    // add folder specific properties
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_OBJECT_ID, requestedIds)) {
      properties.add(objFactory.createPropertyIdData(PropertyIds.CMIS_OBJECT_ID, getPath()));
      // for folders we use the path as id in this provider
    }

    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_PARENT_ID, requestedIds)) {
      String parentId = getParent() == null ? null : getParent().getId();
      if ( parentId != null )
        properties.add(objFactory.createPropertyStringData(PropertyIds.CMIS_PARENT_ID, parentId));
    }

    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_ALLOWED_CHILD_OBJECT_TYPE_IDS,
        requestedIds)) {
      String allowedChildObjects = "*"; // TODO: not yet supported
      properties.add(objFactory.createPropertyStringData(
          PropertyIds.CMIS_ALLOWED_CHILD_OBJECT_TYPE_IDS, allowedChildObjects));
    }

    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_PATH, requestedIds)) {
      String path = getPath();
      properties.add(objFactory.createPropertyStringData(PropertyIds.CMIS_PATH, path));
    }
  }

  // Helper functions
  private void init(String name, Folder parent) {
    if (!NameValidator.isValidId(name))
      throw new IllegalArgumentException(NameValidator.ERROR_ILLEGAL_NAME);
    setName(name);
    setParent(parent);
  }

  private void sortFolderList(List<? extends StoredObject> list) {

    // TODO evaluate orderBy, for now sort by path segment
    class FolderComparator implements Comparator<StoredObject> {

      public int compare(StoredObject f1, StoredObject f2) {
        String segment1 = f1.getName();
        String segment2 = f2.getName();

        return segment1.compareTo(segment2);
      }
    }

    Collections.sort(list, new FolderComparator());
  }

  public void persist() {
    // The in-memory implementation doesn't have to do anything here, but sets the id
//    fId = getPath();
  }

  public void moveChildDocument(Document doc, Folder newParent) {
    if (newParent.hasChild(doc.getName()))
      throw new IllegalArgumentException(
          "Cannot move folder, this name already exists in target.");

    String oldPath = doc.getPath(); // old path of document to move
    doc.setParent(newParent);
    String newPath = doc.getPath(); // new path of document to move
    fObjStore.removeObject(oldPath);
    fObjStore.storeObject(newPath, doc);
  }

  public List<String> getAllowedChildObjectTypeIds() {
    // TODO implement this.
    return null;
  }

}