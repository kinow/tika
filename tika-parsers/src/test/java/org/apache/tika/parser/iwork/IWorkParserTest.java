/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.parser.iwork;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Office;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

/**
 * Tests if the IWork parser parses the content and metadata properly of the supported formats.
 */
public class IWorkParserTest extends TestCase {

    private IWorkPackageParser iWorkParser;
    private ParseContext parseContext;

    @Override
    protected void setUp() throws Exception {
        iWorkParser = new IWorkPackageParser();
        parseContext = new ParseContext();
        parseContext.set(Parser.class, new AutoDetectParser());
    }

    public void testParseKeynote() throws Exception {
        InputStream input = IWorkParserTest.class.getResourceAsStream("/test-documents/testKeynote.key");
        Metadata metadata = new Metadata();
        ContentHandler handler = new BodyContentHandler();
        iWorkParser.parse(input, handler, metadata, parseContext);

        // Make sure enough keys came through
        // (Exact numbers will vary based on composites)
        assertTrue("Insufficient metadata found " + metadata.size(), metadata.size() >= 6);
        List<String> metadataKeys = Arrays.asList(metadata.names());
        assertTrue("Metadata not found in " + metadataKeys, metadataKeys.contains(Metadata.CONTENT_TYPE));
        assertTrue("Metadata not found in " + metadataKeys, metadataKeys.contains(Metadata.SLIDE_COUNT.getName()));
//        assertTrue("Metadata not found in " + metadataKeys, metadataKeys.contains(Office.SLIDE_COUNT.getName()));
        assertTrue("Metadata not found in " + metadataKeys, metadataKeys.contains(TikaCoreProperties.AUTHOR.getName()));
        assertTrue("Metadata not found in " + metadataKeys, metadataKeys.contains(TikaCoreProperties.TITLE.getName()));
        
        // Check the metadata values
        assertEquals("application/vnd.apple.keynote", metadata.get(Metadata.CONTENT_TYPE));
        assertEquals("3", metadata.get(Metadata.SLIDE_COUNT));
        assertEquals("1024", metadata.get(KeynoteContentHandler.PRESENTATION_WIDTH));
        assertEquals("768", metadata.get(KeynoteContentHandler.PRESENTATION_HEIGHT));
        assertEquals("Tika user", metadata.get(TikaCoreProperties.AUTHOR));
        assertEquals("Apache tika", metadata.get(TikaCoreProperties.TITLE));

        String content = handler.toString();
        assertTrue(content.contains("A sample presentation"));
        assertTrue(content.contains("For the Apache Tika project"));
        assertTrue(content.contains("Slide 1"));
        assertTrue(content.contains("Some random text for the sake of testability."));
        assertTrue(content.contains("A nice comment"));
        assertTrue(content.contains("A nice note"));

        // test table data
        assertTrue(content.contains("Cell one"));
        assertTrue(content.contains("Cell two"));
        assertTrue(content.contains("Cell three"));
        assertTrue(content.contains("Cell four"));
        assertTrue(content.contains("Cell 5"));
        assertTrue(content.contains("Cell six"));
        assertTrue(content.contains("7"));
        assertTrue(content.contains("Cell eight"));
        assertTrue(content.contains("5/5/1985"));
    }

    // TIKA-910
    public void testKeynoteTextBoxes() throws Exception {
        InputStream input = IWorkParserTest.class.getResourceAsStream("/test-documents/testTextBoxes.key");
        Metadata metadata = new Metadata();
        ContentHandler handler = new BodyContentHandler();
        iWorkParser.parse(input, handler, metadata, parseContext);

        String content = handler.toString();
        assertTrue(content.replaceAll("\\s+", " ").contains("text1 text2 text3"));
    }

    // TIKA-910
    public void testKeynoteBulletPoints() throws Exception {
        InputStream input = IWorkParserTest.class.getResourceAsStream("/test-documents/testBulletPoints.key");
        Metadata metadata = new Metadata();
        ContentHandler handler = new BodyContentHandler();
        iWorkParser.parse(input, handler, metadata, parseContext);

        String content = handler.toString();
        assertTrue(content.replaceAll("\\s+", " ").contains("bullet point 1 bullet point 2 bullet point 3"));
    }

    // TIKA-923
    public void testKeynoteTables() throws Exception {
        InputStream input = IWorkParserTest.class.getResourceAsStream("/test-documents/testTables.key");
        Metadata metadata = new Metadata();
        ContentHandler handler = new BodyContentHandler();
        iWorkParser.parse(input, handler, metadata, parseContext);

        String content = handler.toString();
        content = content.replaceAll("\\s+", " ");
        assertTrue(content.contains("row 1 row 2 row 3"));
    }

    public void testParsePages() throws Exception {
        InputStream input = IWorkParserTest.class.getResourceAsStream("/test-documents/testPages.pages");
        Metadata metadata = new Metadata();
        ContentHandler handler = new BodyContentHandler();
        iWorkParser.parse(input, handler, metadata, parseContext);

        // Make sure enough keys came through
        // (Exact numbers will vary based on composites)
        assertTrue("Insufficient metadata found " + metadata.size(), metadata.size() >= 50);
        List<String> metadataKeys = Arrays.asList(metadata.names());
        assertTrue("Metadata not found in " + metadataKeys, metadataKeys.contains(Metadata.CONTENT_TYPE));
        assertTrue("Metadata not found in " + metadataKeys, metadataKeys.contains(Metadata.PAGE_COUNT.getName()));
        assertTrue("Metadata not found in " + metadataKeys, metadataKeys.contains(TikaCoreProperties.AUTHOR.getName()));
        assertTrue("Metadata not found in " + metadataKeys, metadataKeys.contains(TikaCoreProperties.TITLE.getName()));
        assertTrue("Metadata not found in " + metadataKeys, metadataKeys.contains(Metadata.LAST_MODIFIED.getName()));
        assertTrue("Metadata not found in " + metadataKeys, metadataKeys.contains(Metadata.LANGUAGE));
        
        // Check the metadata values
        assertEquals("application/vnd.apple.pages", metadata.get(Metadata.CONTENT_TYPE));
        assertEquals("Tika user", metadata.get(TikaCoreProperties.AUTHOR));
        assertEquals("Apache tika", metadata.get(TikaCoreProperties.TITLE));
        assertEquals("2010-05-09T21:34:38+0200", metadata.get(Metadata.CREATION_DATE));
        assertEquals("2010-05-09T23:50:36+0200", metadata.get(Metadata.LAST_MODIFIED));
        assertEquals("en", metadata.get(TikaCoreProperties.LANGUAGE));
        assertEquals("2", metadata.get(Metadata.PAGE_COUNT));

        String content = handler.toString();

        // text on page 1
        assertTrue(content.contains("Sample pages document"));
        assertTrue(content.contains("Some plain text to parse."));
        assertTrue(content.contains("Cell one"));
        assertTrue(content.contains("Cell two"));
        assertTrue(content.contains("Cell three"));
        assertTrue(content.contains("Cell four"));
        assertTrue(content.contains("Cell five"));
        assertTrue(content.contains("Cell six"));
        assertTrue(content.contains("Cell seven"));
        assertTrue(content.contains("Cell eight"));
        assertTrue(content.contains("Cell nine"));
        assertTrue(content.contains("Both Pages 1.x and Keynote 2.x")); // ...

        // text on page 2
        assertTrue(content.contains("A second page...."));
        assertTrue(content.contains("Extensible Markup Language")); // ...
    }

    // TIKA-904
    public void testPagesLayoutMode() throws Exception {
        InputStream input = IWorkParserTest.class.getResourceAsStream("/test-documents/testPagesLayout.pages");
        Metadata metadata = new Metadata();
        ContentHandler handler = new BodyContentHandler();

        iWorkParser.parse(input, handler, metadata, parseContext);

        String content = handler.toString();
        assertTrue(content.contains("text box 1 - here is some text"));
        assertTrue(content.contains("created in a text box in layout mode"));
        assertTrue(content.contains("text box 2 - more text!@!$@#"));
        assertTrue(content.contains("this is text inside of a green box"));
        assertTrue(content.contains("text inside of a green circle"));
    }

    public void testParseNumbers() throws Exception {
        InputStream input = IWorkParserTest.class.getResourceAsStream("/test-documents/testNumbers.numbers");
        Metadata metadata = new Metadata();
        ContentHandler handler = new BodyContentHandler();

        iWorkParser.parse(input, handler, metadata, parseContext);

        // Make sure enough keys came through
        // (Exact numbers will vary based on composites)
        assertTrue("Insufficient metadata found " + metadata.size(), metadata.size() >= 8);
        List<String> metadataKeys = Arrays.asList(metadata.names());
        assertTrue("Metadata not found in " + metadataKeys, metadataKeys.contains(Metadata.CONTENT_TYPE));
        assertTrue("Metadata not found in " + metadataKeys, metadataKeys.contains(Metadata.PAGE_COUNT.getName()));
        assertTrue("Metadata not found in " + metadataKeys, metadataKeys.contains(TikaCoreProperties.AUTHOR.getName()));
        assertTrue("Metadata not found in " + metadataKeys, metadataKeys.contains(Metadata.COMMENT));
        assertTrue("Metadata not found in " + metadataKeys, metadataKeys.contains(Metadata.TITLE));
        assertTrue("Metadata not found in " + metadataKeys, metadataKeys.contains(TikaCoreProperties.TITLE.getName()));
        
        // Check the metadata values
        assertEquals("2", metadata.get(Metadata.PAGE_COUNT));
        assertEquals("Tika User", metadata.get(TikaCoreProperties.AUTHOR));
        assertEquals("Account checking", metadata.get(TikaCoreProperties.TITLE));
        assertEquals("a comment", metadata.get(Metadata.COMMENT));

        String content = handler.toString();
        assertTrue(content.contains("Category"));
        assertTrue(content.contains("Home"));
        assertTrue(content.contains("-226"));
        assertTrue(content.contains("-137.5"));
        assertTrue(content.contains("Checking Account: 300545668"));
        assertTrue(content.contains("4650"));
        assertTrue(content.contains("Credit Card"));
        assertTrue(content.contains("Groceries"));
        assertTrue(content.contains("-210"));
        assertTrue(content.contains("Food"));
        assertTrue(content.contains("Try adding your own account transactions to this table."));
    }

    public void testParseNumbersTableHeaders() throws Exception {
        InputStream input = IWorkParserTest.class.getResourceAsStream("/test-documents/tableHeaders.numbers");
        Metadata metadata = new Metadata();
        ContentHandler handler = new BodyContentHandler();
        iWorkParser.parse(input, handler, metadata, parseContext);

        String content = handler.toString();
        for(int header=1;header<=5;header++) {
          assertTrue(content.contains("header" + header));
        }
        for(int row=1;row<=3;row++) {
          assertTrue(content.contains("row" + row));
        }
    }

    /**
     * We don't currently support password protected Pages files, as
     *  we don't know how the encryption works (it's not regular Zip
     *  Encryption). See TIKA-903 for details
     */
    public void testParsePagesPasswordProtected() throws Exception {
       // Document password is "tika", but we can't use that yet...
       InputStream input = IWorkParserTest.class.getResourceAsStream("/test-documents/testPagesPwdProtected.pages");
       Metadata metadata = new Metadata();
       ContentHandler handler = new BodyContentHandler();

       iWorkParser.parse(input, handler, metadata, parseContext);

       // Content will be empty
       String content = handler.toString();
       assertEquals("", content);
       
       // Will have been identified as encrypted
       assertEquals("application/x-tika-iworks-protected", metadata.get(Metadata.CONTENT_TYPE));
    }
    
    /**
     * Check we get headers, footers and footnotes from Pages
     */
    public void testParsePagesHeadersFootersFootnotes() throws Exception {
       String footnote = "Footnote: Do a lot of people really use iWork?!?!";
       String header = "THIS IS SOME HEADER TEXT";
       String footer = "THIS IS SOME FOOTER TEXT";
       
       InputStream input = IWorkParserTest.class.getResourceAsStream("/test-documents/testPagesHeadersFootersFootnotes.pages");
       Metadata metadata = new Metadata();
       ContentHandler handler = new BodyContentHandler();

       iWorkParser.parse(input, handler, metadata, parseContext);
       String contents = handler.toString();

       // Check regular text
       assertContains(contents, "Both Pages 1.x"); // P1
       assertContains(contents, "understanding the Pages document"); // P1
       assertContains(contents, "should be page 2"); // P2
       
       // Check for headers, footers and footnotes
       assertContains(contents, header);
       assertContains(contents, footer);
       assertContains(contents, footnote);
    }
    
    /**
     * Check we get annotations (eg comments) from Pages
     */
    public void testParsePagesAnnotations() throws Exception {
       String commentA = "comment about the APXL file";
       String commentB = "comment about UIMA";
       
       
       InputStream input = IWorkParserTest.class.getResourceAsStream("/test-documents/testPagesComments.pages");
       Metadata metadata = new Metadata();
       ContentHandler handler = new BodyContentHandler();

       iWorkParser.parse(input, handler, metadata, parseContext);
       String contents = handler.toString();

       // Check regular text
       assertContains(contents, "Both Pages 1.x"); // P1
       assertContains(contents, "understanding the Pages document"); // P1
       assertContains(contents, "should be page 2"); // P2
       
       // Check for comments
       assertContains(contents, commentA);
       assertContains(contents, commentB);
    }
    
    public void assertContains(String haystack, String needle) {
       assertTrue(needle + " not found in:\n" + haystack, haystack.contains(needle));
    }
}
