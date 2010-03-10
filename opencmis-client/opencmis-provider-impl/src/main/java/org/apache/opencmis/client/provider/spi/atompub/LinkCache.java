/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.opencmis.client.provider.spi.atompub;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.opencmis.client.provider.cache.Cache;
import org.apache.opencmis.client.provider.cache.impl.CacheImpl;
import org.apache.opencmis.client.provider.cache.impl.ContentTypeCacheLevelImpl;
import org.apache.opencmis.client.provider.cache.impl.LruCacheLevelImpl;
import org.apache.opencmis.client.provider.cache.impl.MapCacheLevelImpl;
import org.apache.opencmis.client.provider.spi.Session;
import org.apache.opencmis.commons.SessionParameter;
import org.apache.opencmis.commons.impl.UrlBuilder;

/**
 * Link cache.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class LinkCache implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final int CACHE_SIZE_REPOSITORIES = 10;
  private static final int CACHE_SIZE_TYPES = 100;
  private static final int CACHE_SIZE_OBJECTS = 400;

  private Cache fLinkCache;
  private Cache fTypeLinkCache;
  private Cache fCollectionLinkCache;
  private Cache fTemplateCache;
  private Cache fRepositoryLinkCache;

  /**
   * Constructor.
   */
  public LinkCache(Session session) {
    int repCount = session.get(SessionParameter.CACHE_SIZE_REPOSITORIES, CACHE_SIZE_REPOSITORIES);
    if (repCount < 1) {
      repCount = CACHE_SIZE_REPOSITORIES;
    }

    int typeCount = session.get(SessionParameter.CACHE_SIZE_TYPES, CACHE_SIZE_TYPES);
    if (typeCount < 1) {
      typeCount = CACHE_SIZE_TYPES;
    }

    int objCount = session.get(SessionParameter.CACHE_SIZE_OBJECTS, CACHE_SIZE_OBJECTS);
    if (objCount < 1) {
      objCount = CACHE_SIZE_OBJECTS;
    }

    fLinkCache = new CacheImpl("Link Cache");
    fLinkCache.initialize(new String[] {
        MapCacheLevelImpl.class.getName() + " " + MapCacheLevelImpl.CAPACITY + "=" + repCount, // repository
        LruCacheLevelImpl.class.getName() + " " + LruCacheLevelImpl.MAX_ENTRIES + "=" + objCount, // id
        MapCacheLevelImpl.class.getName() + " " + MapCacheLevelImpl.CAPACITY + "=16", // rel
        ContentTypeCacheLevelImpl.class.getName() + " " + MapCacheLevelImpl.CAPACITY + "=3,"
            + MapCacheLevelImpl.SINGLE_VALUE + "=true" // type
    });

    fTypeLinkCache = new CacheImpl("Type Link Cache");
    fTypeLinkCache.initialize(new String[] {
        MapCacheLevelImpl.class.getName() + " " + MapCacheLevelImpl.CAPACITY + "=" + repCount, // repository
        LruCacheLevelImpl.class.getName() + " " + LruCacheLevelImpl.MAX_ENTRIES + "=" + typeCount, // id
        MapCacheLevelImpl.class.getName() + " " + MapCacheLevelImpl.CAPACITY + "=16", // rel
        ContentTypeCacheLevelImpl.class.getName() + " " + MapCacheLevelImpl.CAPACITY + "=3,"
            + MapCacheLevelImpl.SINGLE_VALUE + "=true"// type
    });

    fCollectionLinkCache = new CacheImpl("Collection Link Cache");
    fCollectionLinkCache.initialize(new String[] {
        MapCacheLevelImpl.class.getName() + " " + MapCacheLevelImpl.CAPACITY + "=" + repCount, // repository
        MapCacheLevelImpl.class.getName() + " " + MapCacheLevelImpl.CAPACITY + "=8" // collection
    });

    fTemplateCache = new CacheImpl("URI Template Cache");
    fTemplateCache.initialize(new String[] {
        MapCacheLevelImpl.class.getName() + " " + MapCacheLevelImpl.CAPACITY + "=" + repCount, // repository
        MapCacheLevelImpl.class.getName() + " " + MapCacheLevelImpl.CAPACITY + "=6" // type
    });

    fRepositoryLinkCache = new CacheImpl("Repository Link Cache");
    fRepositoryLinkCache.initialize(new String[] {
        MapCacheLevelImpl.class.getName() + " " + MapCacheLevelImpl.CAPACITY + "=" + repCount, // repository
        MapCacheLevelImpl.class.getName() + " " + MapCacheLevelImpl.CAPACITY + "=6" // rel
    });
  }

  /**
   * Adds a link.
   */
  public void addLink(String repositoryId, String id, String rel, String type, String link) {
    fLinkCache.put(link, repositoryId, id, rel, type);
  }

  /**
   * Removes all links of an object.
   */
  public void removeLinks(String repositoryId, String id) {
    fLinkCache.remove(repositoryId, id);
  }

  /**
   * Gets a link.
   */
  public String getLink(String repositoryId, String id, String rel, String type) {
    return (String) fLinkCache.get(repositoryId, id, rel, type);
  }

  /**
   * Gets a link.
   */
  public String getLink(String repositoryId, String id, String rel) {
    return getLink(repositoryId, id, rel, null);
  }

  /**
   * Checks a link.
   */
  public int checkLink(String repositoryId, String id, String rel, String type) {
    return fLinkCache.check(repositoryId, id, rel, type);
  }

  /**
   * Adds a type link.
   */
  public void addTypeLink(String repositoryId, String id, String rel, String type, String link) {
    fTypeLinkCache.put(link, repositoryId, id, rel, type);
  }

  /**
   * Removes all links of a type.
   */
  public void removeTypeLinks(String repositoryId, String id) {
    fTypeLinkCache.remove(repositoryId, id);
  }

  /**
   * Gets a type link.
   */
  public String getTypeLink(String repositoryId, String id, String rel, String type) {
    return (String) fTypeLinkCache.get(repositoryId, id, rel, type);
  }

  /**
   * Gets a type link.
   */
  public String getTypeLink(String repositoryId, String id, String rel) {
    return getLink(repositoryId, id, rel, null);
  }

  /**
   * Adds a collection.
   */
  public void addCollection(String repositoryId, String collection, String link) {
    fCollectionLinkCache.put(link, repositoryId, collection);
  }

  /**
   * Gets a collection.
   */
  public String getCollection(String repositoryId, String collection) {
    return (String) fCollectionLinkCache.get(repositoryId, collection);
  }

  /**
   * Adds an URI template.
   */
  public void addTemplate(String repositoryId, String type, String link) {
    fTemplateCache.put(link, repositoryId, type);
  }

  /**
   * Gets an URI template and replaces place holders with the given parameters.
   */
  public String getTemplateLink(String repositoryId, String type, Map<String, Object> parameters) {
    String template = (String) fTemplateCache.get(repositoryId, type);
    if (template == null) {
      return null;
    }

    StringBuilder result = new StringBuilder();
    StringBuilder param = new StringBuilder();

    boolean paramMode = false;
    for (int i = 0; i < template.length(); i++) {
      char c = template.charAt(i);

      if (paramMode) {
        if (c == '}') {
          paramMode = false;

          String paramValue = UrlBuilder.normalizeParameter(parameters.get(param.toString()));
          if (paramValue != null) {
            try {
              result.append(URLEncoder.encode(paramValue, "UTF-8"));
            }
            catch (UnsupportedEncodingException e) {
              result.append(paramValue);
            }
          }

          param = new StringBuilder();
        }
        else {
          param.append(c);
        }
      }
      else {
        if (c == '{') {
          paramMode = true;
        }
        else {
          result.append(c);
        }
      }
    }

    return result.toString();
  }

  /**
   * Adds a collection.
   */
  public void addRepositoryLink(String repositoryId, String rel, String link) {
    fRepositoryLinkCache.put(link, repositoryId, rel);
  }

  /**
   * Gets a collection.
   */
  public String getRepositoryLink(String repositoryId, String rel) {
    return (String) fRepositoryLinkCache.get(repositoryId, rel);
  }

  /**
   * Removes all entries of the given repository from the caches.
   */
  public void clearRepository(String repositoryId) {
    fLinkCache.remove(repositoryId);
    fTypeLinkCache.remove(repositoryId);
    fCollectionLinkCache.remove(repositoryId);
    fTemplateCache.remove(repositoryId);
    fRepositoryLinkCache.remove(repositoryId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Link Cache [link cache=" + fLinkCache + ", type link cache=" + fTypeLinkCache
        + ", collection link cache=" + fCollectionLinkCache + ", repository link cache="
        + fRepositoryLinkCache + ",  template cache=" + fTemplateCache + "]";
  }
}
