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
package org.apache.chemistry.opencmis.util.content.loremipsum;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.chemistry.opencmis.util.content.loremipsum.LoremIpsum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LoremIpsumTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoremIpsumTest.class);

    String sample = "One two three four five six. Seven eight nine ten eleven twelve. "
		+ "\n\n"
		+ "Thirteen fourteen fifteen sixteen. Seventeen eighteen nineteen twenty.";
       
    String dictionary = "a bb ccc dddd eeeee ffffff ggggggg hhhhhhhh iiiiiiiii jjjjjjjjjj kkkkkkkkkkk llllllllllll";
    LoremIpsum generator = new LoremIpsum(sample, dictionary);
    
	@Before
	public void setUp() throws Exception {
	    dictionary.split(" ");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void  test_mean() {
		int[] ia1 = {1, 2, 3, 4};
        assertEquals(2.5d, LoremIpsum.mean(ia1), 0.01d);
        int[] ia2 = {6, 6, 4, 4};
        assertEquals(5.0d, LoremIpsum.mean(ia2), 0.01d);
	}
	
    @Test
	public void  test_mean_empty() {
		int[] ia1 = {};
        assertEquals(0.0d, LoremIpsum.mean(ia1), 0.01d);
    }
    
    @Test
	public void  test_variance() {
		double[] ia1 = {6.0d, 6.0d, 4.0d, 4.0d};
        assertEquals(1.0d, LoremIpsum.variance(ia1), 0.01d);
        double[] ia2 = {1.0d, 2.0d, 3.0d, 4.0d};
        assertEquals(1.25d, LoremIpsum.variance(ia2), 0.01d);
    }
    
    @Test
	public void  test_sigma() {
		double[] ia1 = {6.0d, 6.0d, 4.0d, 4.0d};
        double[] ia2 = {1.0d, 2.0d, 3.0d, 4.0d};
        assertEquals(1.0d, LoremIpsum.sigma(ia1), 0.01d);
        assertEquals(Math.sqrt(1.25), LoremIpsum.sigma(ia2), 0.01d);
    }
    
    @Test
	public void  test_sigma_empty() {
		int[] ia1 = {};
        assertEquals(0.0d, LoremIpsum.sigma(ia1), 0.01d);
    }
    
    @Test
	public void test_split_sentences() {
    	String[] sentences1 = {"Hello", "Hi"};
    	assertArrayEquals (sentences1, LoremIpsum.splitSentences("Hello. Hi."));
        String[] sentences2 = {"One two three four five six", 
                                 "Seven eight nine ten eleven twelve", 
                                 "Thirteen fourteen fifteen sixteen", 
                                 "Seventeen eighteen nineteen twenty"}; 
        assertArrayEquals(sentences2, LoremIpsum.splitSentences(sample));
    }
    
    @Test
	public void test_split_sentences_empty() {
    	String[] sentences = {};
    	assertArrayEquals(sentences, LoremIpsum.splitSentences(""));
    }
    
    @Test
	public void test_split_sentences_trailing() {
    	String[] sentences1 = {"Hello", "Hi", "Hello"};    	
    	assertArrayEquals(sentences1, LoremIpsum.splitSentences("Hello. Hi. Hello"));
    	String[] sentences2 = {"Hello", "Hi", "Hello"};
    	assertArrayEquals(sentences2, LoremIpsum.splitSentences("  Hello. Hi. Hello  "));
        String[] sentences3 = {"Hello", "Hi", "Hello"};
        assertArrayEquals(sentences3, LoremIpsum.splitSentences("..  Hello... Hi.... Hello  ")); 
    }

    @Test
	public void test_split_paragraphs() {
    	String[] paragraphs = {"One two three four five six. Seven eight nine ten eleven twelve.",
    			"Thirteen fourteen fifteen sixteen. Seventeen eighteen nineteen twenty."};
    	assertArrayEquals(paragraphs, LoremIpsum.splitParagraphs(sample));
    }
    
    @Test
	public void test_split_paragraphs_empty() {
    	String[] paragraphs = {};
    	assertArrayEquals(paragraphs, LoremIpsum.splitParagraphs(""));
    }
    
    @Test
	public void test_split_paragraphs_trailing() {
    	String[] paragraphs = {"Hello", "Hi"};
    	assertArrayEquals(paragraphs, LoremIpsum.splitParagraphs("Hello\n\nHi"));
    	assertArrayEquals(paragraphs, LoremIpsum.splitParagraphs("Hello\n\nHi\n"));
    	assertArrayEquals(paragraphs, LoremIpsum.splitParagraphs("Hello\n\nHi\n\n"));
    	assertArrayEquals(paragraphs, LoremIpsum.splitParagraphs("Hello\n\nHi\n\n\n"));
    	assertArrayEquals(paragraphs, LoremIpsum.splitParagraphs("Hello\n\nHi\n\n\n\n\n\n"));
    	assertArrayEquals(paragraphs, LoremIpsum.splitParagraphs("\nHello\n\nHi"));
    	assertArrayEquals(paragraphs, LoremIpsum.splitParagraphs("\n\nHello\n\nHi"));
    	assertArrayEquals(paragraphs, LoremIpsum.splitParagraphs("\n\n\nHello\n\nHi"));
    	assertArrayEquals(paragraphs, LoremIpsum.splitParagraphs("\n\n\n\n\n\nHello\n\nHi"));
    }
    
    @Test
	public void test_split_words() {
    	String[] words = {"One", "two", "three", "four"};
    	assertArrayEquals(words, LoremIpsum.splitWords("One two three four"));    	
    	assertArrayEquals(words, LoremIpsum.splitWords("  One    two  three  four   ")); 
    }
                
    @Test
	public void test_split_words_empty() {
    	String[] words = {};
    	assertArrayEquals(words, LoremIpsum.splitWords(""));
	}
    
    @Test
	public void test_choose_closest() {
    	Integer[] intArray1 ={1,2,3,4};
        assertEquals(1, LoremIpsum.chooseClosest(intArray1, 1));
        Integer[] intArray2 ={1,2,3,4};
        assertEquals(4, LoremIpsum.chooseClosest(intArray2, 4));
        assertEquals(4, LoremIpsum.chooseClosest(intArray2, 20));
        assertEquals(1, LoremIpsum.chooseClosest(intArray2, -10));
        Integer[] intArray3 ={1,4};
        assertEquals(1, LoremIpsum.chooseClosest(intArray3, 2));
        assertEquals(4, LoremIpsum.chooseClosest(intArray3, 3));
        Integer[] intArray4 ={1,3};
        assertEquals(1, LoremIpsum.chooseClosest(intArray4, 2));
        Integer[] intArray5 ={3,1};
        assertEquals(3, LoremIpsum.chooseClosest(intArray5, 2));
        Integer[] intArray6 ={1};
        assertEquals(1, LoremIpsum.chooseClosest(intArray6, 200));
    }

    @Test
	public void test_sentence_mean() {
        assertEquals(5.0d, generator.getSentenceMean(), 0.01d);
	}
   
    @Test
	public void test_paragraph_mean() {
    	assertEquals(2.0d, generator.getParagraphMean(), 0.01d);
    }
        
    @Test
	public void test_sentence_sigma() {
        assertEquals(1.0d, generator.getSentenceSigma(), 0.01d);
    }
        
    @Test
	public void test_paragraph_sigma() {
        assertEquals(0.0d, generator.getParagraphSigma(), 0.01d);
    }
        
    @Test
	public void test_sample() {
        assertEquals(generator.getSample(), sample);
    }

    @Test
	public void test_dictionary() {
        assertEquals(generator.getDictionary(), dictionary);
    }

    @Test
	public void test_set_dictionary() {
        String newdict = "a b c";
        generator.setDictionary(newdict);
        assertEquals(generator.getDictionary(), newdict);
	}
    
    @Test 
    public void test_init_no_sample() {
    	doGenerate("");
    	doGenerate(" ");
    	doGenerate("\n\n");
    	doGenerate("  \n\n  ");
    	doGenerate(" .\n\n .");
    }
    
    private void doGenerate(String text) {
    	try {
    		generator = new LoremIpsum(text, dictionary);
    		generator.generateParagraph(false);
    		fail("Sample text " + text + " should generate exception.");
    	} catch (RuntimeException e) {
    		assertTrue(e.getMessage().contains("Invalid sample text"));
    	}
    }
    
    @Test 
    public void test_init_no_dict() {
    	doGenerateNoDict("");
    	doGenerateNoDict(" ");
    	doGenerateNoDict("\n\n");
    	doGenerateNoDict("  \n\n  ");
    }
    
    private void doGenerateNoDict(String dict) {
    	try {
    		generator = new LoremIpsum(sample, dict);
    		generator.generateParagraph(false);
    		fail("Dictionary " + dict + " should generate exception.");
    	} catch (RuntimeException e) {
    		assertEquals(e.getMessage(), "Invalid dictionary.");
    	}
    }

    @Test 
    public void testGenerate() {
    	LOG.debug("Generate new text: ");
    	String newDict = "me you he the One two three four five six Seven eight nine ten eleven twelve "
       		+ "Thirteen fourteen fifteen sixteen Seventeen eighteen nineteen twenty joe fred some";
    	String[] newParagraphs = new String[4];
    	generator.setDictionary(newDict);
    	for (int i=0; i<newParagraphs.length; i++) {
    		newParagraphs[i] = generator.generateParagraph(false);
    		LOG.debug(newParagraphs[i]);
    		LOG.debug("");
    	}
    	assertFalse(newParagraphs[0].equals(newParagraphs[1]));
    	assertFalse(newParagraphs[0].equals(newParagraphs[2]));
    	assertFalse(newParagraphs[0].equals(newParagraphs[3]));
    	assertFalse(newParagraphs[1].equals(newParagraphs[2]));
    	assertFalse(newParagraphs[1].equals(newParagraphs[3]));
    	assertFalse(newParagraphs[2].equals(newParagraphs[3]));
    }
    
    @Test 
    public void testGenerateLoreIpsum() {
    	LOG.debug("Generate new Lore Ipsum text: ");
    	LoremIpsum ipsum = new LoremIpsum();
    	String[] newParagraphs = new String[4];
    	for (int i=0; i<newParagraphs.length; i++) {
    		newParagraphs[i] = ipsum.generateParagraph(false);
    		LOG.debug(newParagraphs[i]);
    		LOG.debug("");
    		LOG.debug("");
    	}
    }
    
    @Test 
    public void testGenerateLoreIpsumHtml1() {
    	LOG.debug("Generate new Lore Ipsum as html paragraphs:");
    	LoremIpsum ipsum = new LoremIpsum();
    	String output = ipsum.generateParagraphsHtml(2048, true);
    	LOG.debug(output);
    	LOG.debug("");
    }
    
    @Test 
    public void testGenerateLoreIpsumHtml2() {
    	LOG.debug("Generate new Lore Ipsum as one html paragraph:");
    	LoremIpsum ipsum = new LoremIpsum();
    	String output = ipsum.generateOneParagraphHtml(2048, true);
    	LOG.debug(output);
    	LOG.debug("");
    }
    
    @Test 
    public void testGenerateLoreIpsumHtml3() {
        LOG.debug("Generate new Lore Ipsum as full html document: ");
    	LoremIpsum ipsum = new LoremIpsum();
    	String output = ipsum.generateParagraphsFullHtml(2048, true);
    	LOG.debug(output);
    	LOG.debug("");
    }
    
    @Test 
    public void testGenerateLoreIpsumPlainText() {
    	LOG.debug("Generate new Lore Ipsum as plain text: ");
    	LoremIpsum ipsum = new LoremIpsum();
    	String output = ipsum.generateParagraphsPlainText(2048, true);
    	LOG.debug(output);
    	LOG.debug("");
    }
    
    @Test 
    public void testGenerateLoreIpsumPlainTextFormatted() {
    	LOG.debug("Generate new Lore Ipsum as plain text with 60 columns: ");
    	LoremIpsum ipsum = new LoremIpsum();
    	String output = ipsum.generateParagraphsPlainText(256, 60, false);
    	LOG.debug(output);
    	LOG.debug("");
    }
        
    @Test 
    public void testGenerateLoreIpsumHtml1Writer() throws IOException {
        LOG.debug("Generate new Lore Ipsum as html paragraphs with PrintWriter:");
        LoremIpsum ipsum = new LoremIpsum();
        StringWriter writer = new StringWriter();
        ipsum.generateParagraphsHtml(writer, 2048, true);
        LOG.debug(writer.toString());
        LOG.debug("End Test.");
    }
    
    @Test 
    public void testGenerateLoreIpsumHtml2Writer() throws IOException  {
        LOG.debug("Generate new Lore Ipsum as full html paragraph with PrintWriter:");
        LoremIpsum ipsum = new LoremIpsum();
        StringWriter writer = new StringWriter();
        ipsum.generateParagraphsFullHtml(writer, 2048, true);
        LOG.debug(writer.toString());
        LOG.debug("End Test.");
    }
    
    @Test 
    public void testGenerateLoreIpsumPlainTextWriter() throws IOException  {
        LOG.debug("Generate new Lore Ipsum as plain text with PrintWriter: ");
        LoremIpsum ipsum = new LoremIpsum();
        StringWriter writer = new StringWriter();
        ipsum.generateParagraphsPlainText(writer, 2048, true);
        LOG.debug(writer.toString());
        LOG.debug("End Test.");
    }
    
    @Test 
    public void testGenerateLoreIpsumPlainTextFormattedWriter() throws IOException {
        LOG.debug("Generate new Lore Ipsum as plain text with 60 columns with PrintWriter: ");
        LoremIpsum ipsum = new LoremIpsum();
        StringWriter writer = new StringWriter();
        ipsum.generateParagraphsPlainText(writer, 256, 60, false);
        LOG.debug(writer.toString());
        LOG.debug("End Test.");
    }
    
    @Test 
    public void testGenerateLoreIpsumGerman() throws Exception {
    	LOG.debug("Generate new Lore Ipsum Ferry Tale: ");
    	InputStream is = this.getClass().getResourceAsStream("/HaenselUndGretel.txt");
    	
    	// read stream into a string
    	final char[] buffer = new char[0x10000];
    	StringBuilder sample = new StringBuilder();
    	Reader in = new InputStreamReader(is, "ISO-8859-1");
    	int read;
    	do {
    	  read = in.read(buffer, 0, buffer.length);
    	  if (read>0) {
    	    sample.append(buffer, 0, read);
    	  }
    	} while (read>=0);

    	
    	LoremIpsum ipsum = new LoremIpsum(sample.toString());
    	String output = ipsum.generateParagraphsPlainText(4096, 80, false);
    	LOG.debug(output);
        LOG.debug("End Test.");
    }
    
}
