/*
 * Copyright (c) 2002-2014, Mairie de Paris
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

import fr.paris.lutece.plugins.newsletter.business.NewsLetterTemplate;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterTemplateHome;
import fr.paris.lutece.portal.service.html.EncodingService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.string.StringUtil;
import fr.paris.lutece.util.url.UrlItem;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import java.util.Locale;
import java.util.Optional;


/**
 * This classe provides utility methods for newsletters.
 */
public final class NewsletterUtils
{
    /**
     * Private constructor
     */
    private NewsletterUtils( )
    {
    }

    /**
     * Retrieve the html template for the given template id
     * @param nTemplateId the id of the template to retrieve
     * @param plugin the plugin
     * @return the html template to use of null if no NewsletterTemplate found
     *         for this Id
     */
    public static String getHtmlTemplatePath( int nTemplateId, Plugin plugin )
    {
        NewsLetterTemplate newsletterTemplate = NewsLetterTemplateHome.findByPrimaryKey( nTemplateId, plugin );

        if ( ( newsletterTemplate == null ) || StringUtils.isEmpty( newsletterTemplate.getFileName( ) ) )
        {
            return null;
        }

        String strTemplatePathName = AppPropertiesService
                .getProperty( NewsLetterConstants.PROPERTY_PATH_FILE_NEWSLETTER_TEMPLATE );
        strTemplatePathName += NewsLetterConstants.CONSTANT_SLASH;
        strTemplatePathName += newsletterTemplate.getFileName( );

        return strTemplatePathName;
    }

    /**
     * Cleans a string in order to make it usable in a javascript script
     * @param strIn the string to clean
     * @return the javascript escaped String
     */
    public static String convertForJavascript( String strIn )
    {
        // Convert problem characters to JavaScript Escaped values
        if ( strIn == null )
        {
            return StringUtils.EMPTY;
        }

        String strOut = strIn;

        strOut = StringUtil.substitute( strOut, "\\\\", "\\" ); // replace backslash with \\

        strOut = StringUtil.substitute( strOut, "\\\'", "'" ); // replace an single quote with \'

        strOut = StringUtil.substitute( strOut, "\\\"", "\"" ); // replace a double quote with \"

        strOut = StringUtil.substitute( strOut, "\\r", "\r\n" ); // replace CR // with \r;

        strOut = StringUtil.substitute( strOut, "\\n", "\n" ); // replace LF with \n;

        return strOut;
    }

    /**
     * Encode a string for passage in parameter in URL
     * @param strEntry the string entry
     * @return the string encoding
     */
    public static String encodeForURL( String strEntry )
    {
        return EncodingService.encodeUrl( strEntry );
    }

    /**
     * Addition of information as header of the http response
     * @param request The Http Request
     * @param response The Http Response
     * @param strFileName THe filename of the file
     * @param strFileExtension The file extension
     */
    public static void addHeaderResponse( HttpServletRequest request, HttpServletResponse response, String strFileName,
            String strFileExtension )
    {
        response.setHeader( "Content-Disposition", "attachment ;filename=\"" + strFileName + "\"" );

        if ( strFileExtension.equals( ".csv" ) )
        {
            response.setContentType( "application/csv" );
        }
        else
        {
            String strMimeType = request.getSession( ).getServletContext( ).getMimeType( strFileName );

            if ( strMimeType != null )
            {
                response.setContentType( strMimeType );
            }
            else
            {
                response.setContentType( "application/octet-stream" );
            }
        }

        response.setHeader( "Pragma", "public" );
        response.setHeader( "Expires", "0" );
        response.setHeader( "Cache-Control", "must-revalidate,post-check=0,pre-check=0" );
    }

    /**
     * Adds all parameter values to the urlItem
     * @param urlItem the urlItem
     * @param strParameterName the name of the parameter which has multiple
     *            values
     * @param values parameter values
     */
    public static void addParameters( UrlItem urlItem, String strParameterName, String[] values )
    {
        for ( String strParameterValue : values )
        {
            urlItem.addParameter( strParameterName, strParameterValue );
        }
    }

    /**
     * Get the first String of a String array. If the array is null, or if it
     * has no element, then return null.
     * @param strArrayValues The string array to get the first element of.
     * @return The first element of the array, or null if the array has no
     *         element or is null.
     */
    public static String getStringFromStringArray( String[] strArrayValues )
    {
        if ( strArrayValues != null )
        {
            return strArrayValues.length == 0 ? null : strArrayValues[0];
        }
        return null;
    }

    /**
     * Rewrite relatives url to absolutes urls
     * @param strContent The content to analyze
     * @param strBaseUrl The base url
     * @return The converted content
     */
    public static String rewriteUrls( String strContent, String strBaseUrl )
    {
        HtmlDomDocNewsletter doc = new HtmlDomDocNewsletter( strContent, strBaseUrl );
        doc.convertAllRelativesUrls( HtmlDomDocNewsletter.ELEMENT_IMG );
        doc.convertAllRelativesUrls( HtmlDomDocNewsletter.ELEMENT_A );
        doc.convertAllRelativesUrls( HtmlDomDocNewsletter.ELEMENT_FORM );
        doc.convertAllRelativesUrls( HtmlDomDocNewsletter.ELEMENT_CSS );
        doc.convertAllRelativesUrls( HtmlDomDocNewsletter.ELEMENT_JAVASCRIPT );

        return doc.getContent( );
    }

    public static Locale getLocale(ServletRequest request) {
        return Optional.ofNullable(request).map(ServletRequest::getLocale).orElse(I18nService.getDefaultLocale( ));
    }

}
