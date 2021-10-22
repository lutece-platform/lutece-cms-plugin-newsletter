/*
 * Copyright (c) 2002-2021, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.newsletter.util;

import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

/**
 * This classes provides implementation to retrieve urls from specified tags on an HTML page.
 */
public class HtmlDomDocNewsletter
{
    public static final String CONSTANT_STATIC_URL = "https?://[^/]+/";
    public static final String CONSTANT_PROTOCOL_DELIMITER = ":";

    // Definition of some basic html elements
    /**
     * To define a CSS, html element must have:
     * <ul>
     * <li>"link" tag name</li>
     * <li>"rel" attribute equal to "stylesheet"</li>
     * </ul>
     * The url is contained in the attributed named "href"
     */
    public static final ElementUrl ELEMENT_CSS;

    /**
     * To define a javascript, html element must have:
     * <ul>
     * <li>"script" tag name</li>
     * <li>"type" attribute equal to "text/javascript"</li>
     * </ul>
     * The url is contained in the attributed named "src"
     */
    public static final ElementUrl ELEMENT_JAVASCRIPT;

    /**
     * To define an image, html element must have:
     * <ul>
     * <li>"img" tag name</li>
     * </ul>
     * The url is contained in the attributed named "src"
     */
    public static final ElementUrl ELEMENT_IMG;

    /**
     * To define a anchor, a element must have:
     * <ul>
     * <li>"a" tag name</li>
     * </ul>
     * The url is contained in the attributed named "href"
     */
    public static final ElementUrl ELEMENT_A;

    /**
     * To define a form, form element must have:
     * <ul>
     * <li>"form" tag name</li>
     * </ul>
     * The url is contained in the attributed named "action"
     */
    public static final ElementUrl ELEMENT_FORM;

    private static final String PROPERTY_LUTECE_ENCODING = "lutece.encoding";

    static
    {
        ELEMENT_CSS = new ElementUrl( "link", "href", "rel", "stylesheet" );
        ELEMENT_JAVASCRIPT = new ElementUrl( "script", "src", "type", "text/javascript" );
        ELEMENT_IMG = new ElementUrl( "img", "src", null, null );
        ELEMENT_A = new ElementUrl( "a", "href", null, null );
        ELEMENT_FORM = new ElementUrl( "form", "action", null, null );
    }

    private Document _content;
    private String _strBaseUrl;

    /**
     * Instantiates an HtmlDocument after having built the DOM tree.
     * 
     * @param strHtml
     *            The Html code to be parsed.
     * @param strBaseUrl
     *            The Base url used to retrieve urls.
     */
    public HtmlDomDocNewsletter( String strHtml, String strBaseUrl )
    {
        // use of tidy to retrieve the DOM tree
        Tidy tidy = new Tidy( );
        tidy.setQuiet( true );
        tidy.setShowWarnings( false );

        String strEncoding = null;

        try
        {
            strEncoding = AppPropertiesService.getProperty( PROPERTY_LUTECE_ENCODING );
            tidy.setInputEncoding( strEncoding );
            _content = tidy.parseDOM( new ByteArrayInputStream( strHtml.getBytes( strEncoding ) ), null );
        }
        catch( UnsupportedEncodingException e )
        {
            AppLogService.error( "Error when parsing Html document (Newsletter) : UnsupporterEncodingException (" + strEncoding + ")", e );
        }

        _strBaseUrl = ( strBaseUrl == null ) ? "" : strBaseUrl;
    }

    /**
     * Get the relatives urls of all html elements specified by elementType and convert its to absolutes urls
     * 
     * @param elementType
     *            the type of element to get
     */
    public void convertAllRelativesUrls( ElementUrl elementType )
    {
        NodeList nodes = getDomDocument( ).getElementsByTagName( elementType.getTagName( ) );

        for ( int i = 0; i < nodes.getLength( ); i++ )
        {
            Node node = nodes.item( i );
            NamedNodeMap attributes = node.getAttributes( );

            // Test if the element matches the required attribute
            if ( elementType.getTestedAttributeName( ) != null )
            {
                String strRel = attributes.getNamedItem( elementType.getTestedAttributeName( ) ).getNodeValue( );

                if ( !elementType.getTestedAttributeValue( ).equals( strRel ) )
                {
                    continue;
                }
            }

            // Retrieve the url, then test if it matches the base url
            Node nodeAttribute = attributes.getNamedItem( elementType.getAttributeName( ) );

            if ( nodeAttribute != null )
            {
                String strSrc = nodeAttribute.getNodeValue( );

                if ( !strSrc.matches( CONSTANT_STATIC_URL ) && !strSrc.contains( CONSTANT_PROTOCOL_DELIMITER ) )
                {
                    nodeAttribute.setNodeValue( getBaseUrl( ) + strSrc );
                }
            }
        }
    }

    /**
     * Get the document content
     * 
     * @return The String content
     */
    public String getContent( )
    {
        DOMSource domSource = new DOMSource( _content );
        StringWriter writer = new StringWriter( );
        StreamResult result = new StreamResult( writer );
        TransformerFactory tf = TransformerFactory.newInstance( );
        Transformer transformer;

        try
        {
            transformer = tf.newTransformer( );
            transformer.transform( domSource, result );
        }
        catch( TransformerConfigurationException e )
        {
            AppLogService.error( e.getMessage( ) );

            return null;
        }
        catch( TransformerException e )
        {
            AppLogService.error( e.getMessage( ) );

            return null;
        }

        String stringResult = writer.toString( );

        return stringResult;
    }

    /**
     * Get the document used by this instance
     * 
     * @return The document used by this instance
     */
    protected org.w3c.dom.Document getDomDocument( )
    {
        return _content;
    }

    /**
     * Get the base url
     * 
     * @return The base url
     */
    protected String getBaseUrl( )
    {
        return _strBaseUrl;
    }

    /**
     * provide a description for the HTML elements to be parsed
     */
    protected static class ElementUrl
    {
        private String _strTagName;
        private String _strAttributeName;
        private String _strTestedAttributeName;
        private String _strTestedAttributeValue;

        /**
         * Instanciates an ElementUrl
         * 
         * @param strTagName
         *            the tag name to get (example: link, script, img, ...)
         * @param strAttributeName
         *            the attribute name to get (example: src, href, ...)
         * @param strTestedAttributeName
         *            the attribute name to test
         * @param strTestedAttributeValue
         *            the value of the attribute to test : if the value of the attribute strTestedAttributeName equals strTestedAttributeValue, then we get the
         *            element's url, else we do nothing.
         */
        public ElementUrl( String strTagName, String strAttributeName, String strTestedAttributeName, String strTestedAttributeValue )
        {
            _strTagName = strTagName;
            _strAttributeName = strAttributeName;
            _strTestedAttributeName = strTestedAttributeName;
            _strTestedAttributeValue = strTestedAttributeValue;
        }

        /**
         * Returns the attributeName
         * 
         * @return the attributeName
         */
        public String getAttributeName( )
        {
            return _strAttributeName;
        }

        /**
         * Returns the tagName
         * 
         * @return the tagName
         */
        public String getTagName( )
        {
            return _strTagName;
        }

        /**
         * Returns the testedAttributeName
         * 
         * @return the testedAttributeName
         */
        public String getTestedAttributeName( )
        {
            return _strTestedAttributeName;
        }

        /**
         * Returns the testedAttributeValue
         * 
         * @return the testedAttributeValue
         */
        public String getTestedAttributeValue( )
        {
            return _strTestedAttributeValue;
        }
    }
}
