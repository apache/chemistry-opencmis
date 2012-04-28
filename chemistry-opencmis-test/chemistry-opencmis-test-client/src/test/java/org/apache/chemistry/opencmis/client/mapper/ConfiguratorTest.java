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
package org.apache.chemistry.opencmis.client.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.apache.chemistry.opencmis.client.parser.MetadataParser;
import org.apache.chemistry.opencmis.client.parser.MetadataParserExif;
import org.apache.chemistry.opencmis.client.parser.MetadataParserTika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConfiguratorTest {

    private static final Logger LOG = LoggerFactory.getLogger(ConfiguratorTest.class.getName());
    Properties properties;
    
    @Before
    public void setUp() throws Exception {
        properties = new Properties();  
        properties.setProperty(Configurator.PREFIX + "s", "image, audio");  
        
        properties.setProperty(Configurator.PREFIX + ".audio.typeId", "apache:audio");
        properties.setProperty(Configurator.PREFIX + ".audio", "audio/mp3");  
        properties.setProperty(Configurator.PREFIX + ".audio.class", "TikaPropertyMapper.class");  
        properties.setProperty(Configurator.PREFIX + ".audio.id.artist", "artist");  
        properties.setProperty(Configurator.PREFIX + ".audio.id.album", "album");  
        properties.setProperty(Configurator.PREFIX + ".audio.id.title", "title");  
        properties.setProperty(Configurator.PREFIX + ".audio.mapperClass",
                "org.apache.chemistry.opencmis.client.mapper.PropertyMapperTika");
        properties.setProperty(Configurator.PREFIX + ".audio.parserClass",
                "org.apache.chemistry.opencmis.client.parser.MetadataParserTika");

        properties.setProperty(Configurator.PREFIX + ".image", "image/jpeg");  
        properties.setProperty(Configurator.PREFIX + ".image.typeId", "apache:image");
        properties.setProperty(Configurator.PREFIX + ".image.id.model", "model");
        properties.setProperty(Configurator.PREFIX + ".image.id.imageWidth", "width");
        properties.setProperty(Configurator.PREFIX + ".image.id.imageHeight", "height");
        properties.setProperty(Configurator.PREFIX + ".image.mapperClass",
                "org.apache.chemistry.opencmis.client.mapper.PropertyMapperTika");
        properties.setProperty(Configurator.PREFIX + ".image.parserClass",
                "org.apache.chemistry.opencmis.client.parser.MetadataParserExif");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void  testLoad() {
        Configurator cfg = new Configurator(properties);        
        assertTrue(cfg.getProperties().size() > 0);
    }
    
    @Test
    public void  testGetTypes() {
        Configurator cfg = new Configurator(properties);        
        String[] types = cfg.getTypeKeys();
        assertEquals(2, types.length);
        assertEquals("image", types[0]);
        assertEquals("audio", types[1]);
    }

    @Test
    public void  testLoadClass() {
        Configurator cfg = new Configurator(properties);
        PropertyMapper mapper = cfg.loadMapperClass("image");
        assertNotNull(mapper);
    }
    
    @Test
    public void testLoadMaps() {
        PropertyMapperTika mapper = new PropertyMapperTika();
        String typeKey = "image";
        String contentType = "image/jpeg";
        assertTrue(mapper.initialize(Configurator.PREFIX, typeKey, properties));
        mapper.buildIdMap(typeKey, properties);
        assertEquals(3, mapper.getSize());
        assertEquals(contentType, mapper.getContentTypes()[0]);
        assertEquals("apache:image", mapper.getMappedTypeId());
        assertEquals("model", mapper.getMappedPropertyId("model"));
        assertEquals("width", mapper.getMappedPropertyId("imageWidth"));
        assertEquals("height", mapper.getMappedPropertyId("imageHeight"));
        
        mapper = new PropertyMapperTika();
        typeKey = "audio";
        contentType = "audio/mp3";
        assertTrue(mapper.initialize(Configurator.PREFIX, typeKey, properties));
        mapper.buildIdMap(typeKey, properties);
        assertEquals(3, mapper.getSize());
        assertEquals(contentType, mapper.getContentTypes()[0]);
        assertEquals("apache:audio", mapper.getMappedTypeId());
        assertEquals("artist", mapper.getMappedPropertyId("artist"));
        assertEquals("album", mapper.getMappedPropertyId("album"));
        assertEquals("title", mapper.getMappedPropertyId("title"));
    }
    
    @Test
    public void testBuildMapperMap() {
        Configurator cfg = new Configurator(properties);
        cfg.buildMapperMap();
        cfg.createParsers();
        PropertyMapper mapper = cfg.getPropertyMapper("image/jpeg");
        assertNotNull(mapper);
        assertEquals("model", mapper.getMappedPropertyId("model"));
        assertEquals("width", mapper.getMappedPropertyId("imageWidth"));
        assertEquals("height", mapper.getMappedPropertyId("imageHeight"));
        assertNull(mapper.getMappedPropertyId("artist"));
        
        mapper = cfg.getPropertyMapper("audio/mp3");
        assertNotNull(mapper);
        assertEquals("artist", mapper.getMappedPropertyId("artist"));
        assertEquals("album", mapper.getMappedPropertyId("album"));
        assertEquals("title", mapper.getMappedPropertyId("title"));
        assertNull(mapper.getMappedPropertyId("model"));
    }
    
    @Test
    public void testParserMap() {
        Configurator cfg = new Configurator(properties);
        cfg.buildMapperMap();
        cfg.createParsers();
        MetadataParser parser = cfg.getParser("image/jpeg");
        assertNotNull(parser);
        assertEquals(MetadataParserExif.class, parser.getClass());
        
        parser = cfg.getParser("audio/mp3");
        assertNotNull(parser);
        assertEquals(MetadataParserTika.class, parser.getClass());
    }

    @Test
    public void testParseContentTypeConfiguration() {
        String typeId = "apache:image";
        properties = new Properties();  
        properties.setProperty(Configurator.PREFIX + "s", "image");          
        properties.setProperty(Configurator.PREFIX + ".image", "image/jpeg");  
        properties.setProperty(Configurator.PREFIX + ".image.typeId", typeId);
        Configurator cfg = new Configurator(properties);
        cfg.buildMapperMap();
        cfg.createParsers();
        assertNotNull(cfg.getParser("image/jpeg"));
        assertEquals(typeId, cfg.getParser("image/jpeg").getMappedTypeId());
        assertNull(cfg.getParser("audio/mp3"));
        assertNull(cfg.getParser("image/x-something"));
        
        properties.setProperty(Configurator.PREFIX + ".image", "image/jpeg : image/tiff:image/png");  
        cfg = new Configurator(properties);
        cfg.buildMapperMap();
        cfg.createParsers();
        assertNotNull(cfg.getParser("image/jpeg"));
        assertNotNull(cfg.getParser("image/tiff"));
        assertNotNull(cfg.getParser("image/png"));
        assertEquals(typeId, cfg.getParser("image/jpeg").getMappedTypeId());
        assertEquals(typeId, cfg.getParser("image/tiff").getMappedTypeId());
        assertEquals(typeId, cfg.getParser("image/png").getMappedTypeId());
        assertNull(cfg.getParser("audio/mp3"));
        assertNull(cfg.getParser("image/x-something"));
        
        properties.setProperty(Configurator.PREFIX + ".image", "image/*:audio/mpeg:audio/ogg");  
        cfg = new Configurator(properties);
        cfg.buildMapperMap();
        cfg.createParsers();
        assertNotNull(cfg.getParser("image/jpeg"));
        assertEquals(typeId, cfg.getParser("image/jpeg").getMappedTypeId());
        assertNotNull(cfg.getParser("image/tiff"));
        assertEquals(typeId, cfg.getParser("image/tiff").getMappedTypeId());
        assertNotNull(cfg.getParser("image/png"));
        assertEquals(typeId, cfg.getParser("image/png").getMappedTypeId());
        assertNotNull(cfg.getParser("image/x-something"));
        assertEquals(typeId, cfg.getParser("image/x-something").getMappedTypeId());
        assertNotNull(cfg.getParser("audio/mpeg"));
        assertEquals(typeId, cfg.getParser("audio/mpeg").getMappedTypeId());
        assertNotNull(cfg.getParser("audio/ogg"));
        assertEquals(typeId, cfg.getParser("audio/ogg").getMappedTypeId());
        assertNull(cfg.getParser("audio/mp3"));
        assertNull(cfg.getParser("text/plain"));
    }
}
