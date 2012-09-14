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

////////////////////////////////////////////////////////////////////////
// 
// Groovy script to generate a types.xml file from a text file in 
// a simplified syntax
// This script expects two files names TypeDefinitions.txt and 
// PropertyDefinitions.txt in the same directory and generates
// file types.xml that can be imported into the InMemoryServer
// 
////////////////////////////////////////////////////////////////////////

def genCommons1(name, id, descr) {
    def id1 = id
    def localName = name
    def ns = ""
    
    // allow syntax name;localName;namespace in property name line
    if (id.contains(";")) {
        names = id.tokenize(";")
        if (names.size() > 0)
            id1 = names[0];
        if (names.size() > 1)
            localName = names[1];
        if (names.size() > 2)
            ns = names[2];
    }
	outFile.println("        <id>" + id1 + "</id>")
	outFile.println("        <localName>" + localName + "</localName>")
	outFile.println("        <displayName>" + name + "</displayName>")
	outFile.println("        <queryName>" + name.replaceAll(" ", "_").toUpperCase() + "</queryName>")
	outFile.println("        <description>" + descr + "</description>")
	outFile.println("        <localNamespace>" + ns + "</localNamespace>")
}

//////////////////////////////////////////////////////////////////

def genCommons2(choices, cardinality, updatability, required, queryable, orderable) {
    def cardinalityStr 
    def updatabilityStr
    def requiredStr
    def queryableStr
    def orderableStr
    
    if (updatability.toLowerCase().startsWith("readonly"))  
        updatabilityStr = "readonly"
    else if (updatability.toLowerCase().startsWith("oncreate"))  
        updatabilityStr = "oncreate"
    else if (updatability.toLowerCase().startsWith("whencheckedout"))  
        updatabilityStr = "whencheckedout"
    else
        updatabilityStr = "readwrite"
    
    if (required.toLowerCase().startsWith("true"))
        requiredStr = "true"
    else
        requiredStr = "false"

        if (cardinality.toLowerCase().startsWith("multi"))
        cardinalityStr = "multi"
    else
        cardinalityStr = "single"
    
    if (queryable.toLowerCase().startsWith("true"))
        queryableStr = "true"
    else
        queryableStr = "false"

    if (orderable.toLowerCase().startsWith("true"))
        orderableStr = "true"
    else
        orderableStr = "false"

	outFile.println("        <cardinality>" +  cardinalityStr + "</cardinality>")
	outFile.println("        <updatability>" + updatabilityStr + "</updatability>")
	outFile.println("        <inherited>false</inherited>")
	outFile.println("        <required>" + requiredStr + "</required>")
	outFile.println("        <queryable>" + queryableStr + "</queryable>")
	outFile.println("        <orderable>" + orderableStr + "</orderable>")
    outFile.println("        <openChoice>false</openChoice>")
	if (null != choices) 
	    genChoices(choices)
}

//////////////////////////////////////////////////////////////////

def genBoolean (name, uuid, descr, choices, cardinality, updatability, required, queryable, orderable) {
	outFile.println("    <propertyBooleanDefinition>")
	genCommons1(name, uuid, descr)
	outFile.println("        <propertyType>boolean</propertyType>")
	genCommons2(choices, cardinality, updatability, required, queryable, orderable)
	outFile.println("    </propertyBooleanDefinition>")
}

//////////////////////////////////////////////////////////////////

def genDate (name, uuid, descr, choices, cardinality, updatability, required, queryable, orderable) {
	outFile.println("    <propertyDateTimeDefinition>")
	genCommons1(name, uuid, descr)
	outFile.println("        <propertyType>datetime</propertyType>")
	genCommons2(choices, cardinality, updatability, required, queryable, orderable)
	outFile.println("    </propertyDateTimeDefinition>")
}

//////////////////////////////////////////////////////////////////

def genId (name, uuid, descr, choices, cardinality, updatability, required, queryable, orderable) {
	outFile.println("    <propertyIdDefinition>")
	genCommons1(name, uuid, descr)
	outFile.println("        <propertyType>id</propertyType>")
	genCommons2(choices, cardinality, updatability, required, queryable, orderable)
	outFile.println("    </propertyIdDefinition>")
}
 
45//////////////////////////////////////////////////////////////////

def genInteger (name, uuid, descr, choices, cardinality, updatability, required, queryable, orderable) {
	outFile.println("    <propertyIntegerDefinition>")
	genCommons1(name, uuid, descr)
	outFile.println("        <propertyType>integer</propertyType>")
	genCommons2(choices, cardinality, updatability, required, queryable, orderable)
	outFile.println("    </propertyIntegerDefinition>")
}

//////////////////////////////////////////////////////////////////

def genString (name, uuid, descr, choices, cardinality, updatability, required, queryable, orderable) {
	outFile.println("    <propertyStringDefinition>")
	genCommons1(name, uuid, descr)
	outFile.println("        <propertyType>string</propertyType>")
	genCommons2(choices, cardinality, updatability, required, queryable, orderable)
	outFile.println("    </propertyStringDefinition>")
}

//////////////////////////////////////////////////////////////////

def genDecimal (name, uuid, descr, choices, cardinality, updatability, required, queryable, orderable) {
	outFile.println("    <propertyDecimalDefinition>")
	genCommons1(name, uuid, descr)
	outFile.println("        <propertyType>decimal</propertyType>")
	genCommons2(choices, cardinality, updatability, required, queryable, orderable)
	outFile.println("    </propertyDecimalDefinition>")
}

//////////////////////////////////////////////////////////////////

def genChoices (choices) {
	   choices.each {
	     keyValue = it.tokenize("=")
		 String key = keyValue.get(0).trim()
		 String value = keyValue.get(1).trim()
		 // println("Found enum with key: " + key + " value: " + value)
         outFile.println("        <choice displayName=\"" + value + "\">")
         outFile.println("            <value>" + key + "</value>")
         outFile.println("        </choice>")
	   }
}

//////////////////////////////////////////////////////////////////

def genTypeDef (name, id, descr, typeKind, parentType, props) {

    String xsiType
    String baseId
    
    switch (typeKind) {
        case "document":
          xsiType = "cmisTypeDocumentDefinitionType"
          baseId = "cmis:document"
          break;
        case "folder":
          xsiType = "cmisTypeFolderDefinitionType"
          baseId = "cmis:folder"
          break;
        default:
           println("Error illegal type: " + typeKind)
    }
    if (parentType == null)
      parentType = baseId
      
    def id1 = id
    def localName = name
    def ns = ""
    
    // allow syntax name;localName;namespace in property name line
    if (id.contains(";")) {
        names = id.tokenize(";")
        if (names.size() > 0)
            id1 = names[0];
        if (names.size() > 1)
            localName = names[1];
            ns = names[2];
    }

	outFile.println("<cmisra:type xsi:type=\"" + xsiType + "\">")
    outFile.println("    <id>" + id1 + "</id>")
	outFile.println("    <localName>" + localName + "</localName>")
	outFile.println("    <localNamespace>" + ns + "</localNamespace>")
	outFile.println("    <parentId>" + parentType + "</parentId>")
	outFile.println("    <displayName>" + name + "</displayName>")
	outFile.println("    <queryName>" +  name.replaceAll(" ", "_").toUpperCase() + "</queryName>")
	outFile.println("    <description>" + descr + "</description>")
	outFile.println("    <baseId>" + baseId + "</baseId>")
	outFile.println("    <creatable>true</creatable>")
	outFile.println("    <fileable>true</fileable>")
	outFile.println("    <queryable>true</queryable>")
	outFile.println("    <fulltextIndexed>true</fulltextIndexed>")
	outFile.println("    <includedInSupertypeQuery>true</includedInSupertypeQuery>")
	outFile.println("    <controllablePolicy>false</controllablePolicy>")
	outFile.println("    <controllableACL>true</controllableACL>")
    if (typeKind == "document") {
        outFile.println("    <versionable>false</versionable>")
        outFile.println("    <contentStreamAllowed>allowed</contentStreamAllowed>")
    }
    if (props != null)
        props.each { 
            def propDef = propDefsMap.get(it)
            if (propDef != null)
                genPropertyDef (propDef.type, propDef.name, propDef.id, propDef.descr, propDef.choices, 
                   propDef.cardinality, propDef.updatability, propDef.required, propDef.queryable, propDef.orderable)
             else
                println ("Error: unknown property definition " + it + " in type definition " + name)
        }
	outFile.println("</cmisra:type>")
    outFile.println()
    outFile.flush()    
}

//////////////////////////////////////////////////////////////////

def genPropertyDef(type, name, uuid, descr, choices, cardinality, updatability, required, queryable, orderable) {
        
    switch (type) {
     case "boolean":
       genBoolean(name, uuid, descr, choices, cardinality, updatability, required, queryable, orderable)
       break
     case "datetime":
       genDate(name, uuid, descr, choices, cardinality, updatability, required, queryable, orderable)
       break
     case "id":
       genId(name, uuid, descr, choices, cardinality, updatability, required, queryable, orderable)
       break
     case "integer":
       genInteger(name, uuid, descr, choices, cardinality, updatability, required, queryable, orderable)
       break
     case "string":
       genString(name, uuid, descr, choices, cardinality, updatability, required, queryable, orderable)
       break
     case "decimal":
       genDecimal(name, uuid, descr, choices, cardinality, updatability, required, queryable, orderable)
       break
     default:
       println("!!!Error: unknown property type " + type);
    }
    
}

//////////////////////////////////////////////////////////////////

def readPropertiesFile(inputFile) {    
    def propDefMap = [:]
    def inFile = new File(inputFile).newReader() 
    
    while (inFile.readLine() != null) {   
       def typeLine = inFile.readLine()
       def name = inFile.readLine().trim()
       println("   processing property: " + name + " type: " + typeLine)
       def id = inFile.readLine().trim()
       def descr = inFile.readLine().trim()
       def attrs = inFile.readLine().trim()
       def choices = null;
       def cardinality, updatability, required, queryable, orderable
       

       type = typeLine.tokenize().get(0)
       def arr = attrs.split(",")
       cardinality = arr.length > 0 ? arr[0].trim() : null
       updatability = arr.length > 1 ? arr[1].trim() : null
       required = arr.length > 2 ? arr[2].trim() : null
       queryable = arr.length > 2 ? arr[3].trim() : "false"
       orderable = arr.length > 2 ? arr[4].trim() : "false"
       
       if (typeLine.contains("enum:")) {
           choices = typeLine[typeLine.indexOf("enum:")+ 5..typeLine.length()-1].tokenize(";")
       }
       propDefMap.put(name, [name:name, id:id, descr:descr, type:type, choices:choices, cardinality:cardinality,
               updatability:updatability, required:required, queryable:queryable, orderable:orderable])
    }
    
    return propDefMap;  
}

//////////////////////////////////////////////////////////////////

def readTypeDefinitions(inputFile) {
    def typeDefMap = [:]
    def inReader = new File(inputFile).newReader() 
    // def inReader = new StringReader(typeDefInput)
    def line = inReader.readLine()
    while (line != null) {   
       def kind = inReader.readLine().trim()
       def idLine = inReader.readLine().trim()
       def name = inReader.readLine().trim()
       def descr = inReader.readLine().trim()
       def arr = idLine.split(":")
       def id = arr.length>0 ? arr[0].trim() : null
       def superType = arr.length>1 ? arr[1].trim() : null

       def propDefs = []
       def readProperties = true;
       while (readProperties) {
           line = inReader.readLine()
           if (line != null && line.size() > 0) 
               propDefs.push(line.trim())
           else
              readProperties = false;
       }
       println("Reading type def " + name)
       typeDefMap.put(name, [name: name, id: id, kind: kind, super: superType, descr: descr, props: propDefs])
    }
    return typeDefMap
}

//////////////////////////////////////////////////////////////////

def writeTypes(typeDefsMap) {
    outFile.println("<chem:typeDefinitions xmlns=\"http://docs.oasis-open.org/ns/cmis/core/200908/\"")
    outFile.println("     xmlns:cmisra=\"http://docs.oasis-open.org/ns/cmis/restatom/200908/\"")
    outFile.println("     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"")
    outFile.println("     xmlns:chem=\"http://chemistry.apache.org/schemas/TypeDefnitions\">")
    for ( e in typeDefsMap   ) {
        println("generating type" +  e.value.name)  
        genTypeDef (e.value.name, e.value.id, e.value.descr, e.value.kind, e.value.super, e.value.props) 
    }
    outFile.println("</chem:typeDefinitions>")
}

//////////////////////////////////////////////////////////////////

def writePropDefs(propDefsMap) {
    for ( e in propDefsMap   ) {
        println("generating property" +  e.value.name)  
        
        genPropertyDef (e.value.type, e.value.name, e.value.id, e.value.descr, e.value.choices,
            e.value.cardinality, e.value.updatability, e.value.required) 
    }
}

//////////////////////////////////////////////////////////////////
//  Main program

println("Starting...")
def propsFile = "PropertyDefinitions.txt";
def typesFile = "TypeDefinitions.txt";
def outFileName = "types.xml";
outFile = new File(outFileName).newPrintWriter()

println("Reading type definitions from file " + typesFile);
typeDefsMap = readTypeDefinitions(typesFile)
println("Reading property definitions from file " + propsFile);
propDefsMap = readPropertiesFile(propsFile)
println("Writing out to " + outFileName);      
outFile.println("<!-- Generated Output file with type definitions. Do not edit! -->")
writeTypes(typeDefsMap)
outFile.close()
println("...Done.")
