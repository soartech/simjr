/*
 * Copyright (c) 2010, Soar Technology, Inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * * Neither the name of Soar Technology, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without the specific prior written permission of Soar Technology, Inc.
 * 
 * THIS SOFTWARE IS PROVIDED BY SOAR TECHNOLOGY, INC. AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SOAR TECHNOLOGY, INC. OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.soartech.simjr.util;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public final class JDomTools
{
    private static Logger logger = LoggerFactory.getLogger(JDomTools.class);

    /** Convert a JDOM element to an xml string
     * 
     * @param element to convert
     * @return the xml string
     */
    public static String xmlToString(Element element, boolean pretty)
    {
        XMLOutputter outputter = new XMLOutputter(pretty ? Format.getPrettyFormat() : Format.getCompactFormat());
        StringWriter w = new StringWriter(256);
        try
        {
            outputter.output(element, w);
        } catch (IOException e)
        {
            logger.error("Exception writing XML", e);
        }
        return w.toString();
    }
    
    public static String xmlToString(Element element)
    {
        return xmlToString(element, false);
    }

    /** Convert a JDOM document to an xml string
     * 
     * @param doc to convert
     * @return the xml string
     */
    public static String xmlToString(Document doc, boolean pretty)
    {
        return xmlToString(doc.getRootElement(), pretty);
    }
    
    public static String xmlToString(Document doc)
    {
        return xmlToString(doc, false);
    }
        

    /** Convert a string to a JDOM document
     * 
     * @param xml the xml-string to convert
     * @return the converted JDOM document or an empty document on parse failure
     */
    public static Document parse(String xml)
    {
        SAXBuilder builder = new SAXBuilder();
        builder.setValidation(false);
        builder.setIgnoringElementContentWhitespace(true);
        try
        {
            return builder.build(new StringReader(xml));
        } 
        catch (JDOMException e)
        {
            logger.error("Failed to parse:\n" + xml + "\n", e);
        } 
        catch (IOException e)
        {
            logger.error("IO error:\n" + xml + "\n", e);
        }
        return new Document();
    }
    
    /**
     * Parse an XML file.
     * 
     * @param fileName The path of the file to parse
     * @return A new document, empty on error.
     */
    public static Document parseFile(String fileName)
    {
        SAXBuilder builder = new SAXBuilder();
        builder.setValidation(false);
        builder.setIgnoringElementContentWhitespace(true);
        try
        {
            return builder.build(new FileReader(fileName));
        } 
        catch (JDOMException e)
        {
            logger.error("Failed to parse file '" + fileName + "'\n", e);
        } 
        catch (IOException e)
        {
            logger.error("IO error while reading file '" + fileName + "'\n", e);
        }
        return new Document();
        
    }

    public static String prettyXmlString(String xml)
    {
        Document doc = parse(xml);
        return xmlToString(doc, true);
    }
    public static String prettyXmlString(Element xml)
    {
        return xmlToString(xml, true);
    }
    public static String prettyXmlString(Document xml)
    {
        return xmlToString(xml, true);
    }
}
