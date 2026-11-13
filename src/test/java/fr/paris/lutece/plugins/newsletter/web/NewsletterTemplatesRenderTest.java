/*
 * Copyright (c) 2002-2026, City of Paris
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
package fr.paris.lutece.plugins.newsletter.web;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.newsletter.business.SendingNewsLetter;
import fr.paris.lutece.portal.service.editor.RichTextContentMethod;
import fr.paris.lutece.portal.service.template.HtmlMarkup;
import fr.paris.lutece.test.LuteceTestCase;
import freemarker.cache.FileTemplateLoader;
import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

/**
 * Renders with HTML auto-escaping (LUT-33153) the newsletter templates that receive HTML from Java: the sections of the
 * newsletter model, the body of the e-mail, the archived e-mail, and the terms of service written with richText(). The
 * HTML built by the application is kept, the rich text typed by an administrator is sanitized by the core, hence the
 * CDI container of LuteceTestCase.
 */
public class NewsletterTemplatesRenderTest extends LuteceTestCase
{
    private static final String TEMPLATES_ROOT = "webapp/WEB-INF/templates";
    private static final String TEMPLATE_MODEL = "skin/plugins/newsletter/templates/model_newsletter.html";
    private static final String TEMPLATE_SEND = "admin/plugins/newsletter/send_newsletter.html";
    private static final String TEMPLATE_ARCHIVE = "skin/plugins/newsletter/page_newsletter_archive.html";
    private static final String TEMPLATE_TOS = "skin/plugins/newsletter/tos.html";

    private static final String SECTION_1 = "<p class=\"intro\">Edito &amp; <strong>actualités</strong></p>";
    private static final String SECTION_2 = "<tr><td><a href=\"https://example.org/agenda\">Agenda</a></td></tr>";
    private static final String BODY = "<tr><td><h2>Lettre</h2></td></tr>";
    private static final String ARCHIVE = "<html><body><table><tr><td><p>Archive</p></td></tr></table></body></html>";
    private static final String TOS = "<p>Conditions <em>generales</em></p><script>alert(1)</script>";

    /**
     * The configuration of AbstractFreeMarkerTemplateService for HTML templates, with the richText() method of the core.
     *
     * @return the configuration
     * @throws IOException
     *             if the templates directory cannot be read
     */
    private static Configuration configuration( ) throws IOException
    {
        Configuration cfg = new Configuration( Configuration.VERSION_2_3_0 );
        cfg.setOutputFormat( HTMLOutputFormat.INSTANCE );
        cfg.setAutoEscapingPolicy( Configuration.ENABLE_IF_DEFAULT_AUTO_ESCAPING_POLICY );
        cfg.setTemplateLoader( new FileTemplateLoader( new File( TEMPLATES_ROOT ) ) );
        cfg.setSharedVariable( "richText", new RichTextContentMethod( ) );

        return cfg;
    }

    /**
     * Renders a template of the plugin.
     *
     * @param strTemplate
     *            the template path
     * @param model
     *            the model
     * @return the rendering
     * @throws IOException
     *             if the template cannot be read
     * @throws TemplateException
     *             if the rendering fails
     */
    private static String render( String strTemplate, Map<String, Object> model ) throws IOException, TemplateException
    {
        StringWriter writer = new StringWriter( );
        configuration( ).getTemplate( strTemplate ).process( model, writer );

        return writer.toString( );
    }

    /**
     * The sections given by the topics are written as HTML, an empty section is left out, as NewsletterService puts them
     * in the model.
     *
     * @throws Exception
     *             if the rendering fails
     */
    @Test
    public void testModelWritesTheSectionsAsHtml( ) throws Exception
    {
        Map<String, Object> model = new HashMap<>( );
        model.put( "content_1", HtmlMarkup.of( SECTION_1 ) );
        model.put( "content_2", HtmlMarkup.of( SECTION_2 ) );
        model.put( "content_3", HtmlMarkup.of( "" ) );
        model.put( "base_url", "https://example.org/site" );

        String strHtml = render( TEMPLATE_MODEL, model );

        assertTrue( strHtml.contains( SECTION_1 ), strHtml );
        assertTrue( strHtml.contains( SECTION_2 ), strHtml );
        assertFalse( strHtml.contains( "&lt;" ), strHtml );
        assertFalse( strHtml.contains( "1 Column Text : BEGIN" ), "an empty section should be left out: " + strHtml );
        assertTrue( strHtml.contains( "src=\"https://example.org/site/images/poweredby.png\"" ), strHtml );
    }

    /**
     * A section the template does not use (null) is left out too.
     *
     * @throws Exception
     *             if the rendering fails
     */
    @Test
    public void testModelLeavesOutAMissingSection( ) throws Exception
    {
        Map<String, Object> model = new HashMap<>( );
        model.put( "content_1", HtmlMarkup.of( SECTION_1 ) );
        model.put( "base_url", "" );

        String strHtml = render( TEMPLATE_MODEL, model );

        assertTrue( strHtml.contains( SECTION_1 ), strHtml );
        assertFalse( strHtml.contains( "1 Column Text : BEGIN" ), strHtml );
    }

    /**
     * The body of the e-mail is the HTML of the newsletter, written as is; the unsubscribe link keeps the markers the
     * sending replaces for each subscriber.
     *
     * @throws Exception
     *             if the rendering fails
     */
    @Test
    public void testEmailWritesTheNewsletterAsHtml( ) throws Exception
    {
        Map<String, Object> model = new HashMap<>( );
        model.put( "newsletter_content", HtmlMarkup.of( BODY ) );
        model.put( "base_url", "https://example.org/site/" );
        model.put( "unsubscribe", Boolean.TRUE );
        model.put( "newsletter_id", 7 );
        model.put( "subscriber_email", "@email@" );
        model.put( "unsubscribe_key", "@unsubscribe_key@" );

        String strHtml = render( TEMPLATE_SEND, model );

        assertTrue( strHtml.contains( BODY ), strHtml );
        assertTrue( strHtml.contains( "subscriber_email=@email@&newsletter_id=7&unsubscribe_key=@unsubscribe_key@" ), strHtml );
    }

    /**
     * The archive page writes the archived e-mail as HTML.
     *
     * @throws Exception
     *             if the rendering fails
     */
    @Test
    public void testArchiveWritesTheSendingAsHtml( ) throws Exception
    {
        SendingNewsLetter sending = new SendingNewsLetter( );
        sending.setHtml( ARCHIVE );
        Map<String, Object> model = new HashMap<>( );
        model.put( "sending", sending );
        model.put( "sending_html", HtmlMarkup.of( sending.getHtml( ) ) );

        assertEquals( ARCHIVE, render( TEMPLATE_ARCHIVE, model ) );
    }

    /**
     * The terms of service are written as sanitized rich text: formatting kept, scripts removed.
     *
     * @throws Exception
     *             if the rendering fails
     */
    @Test
    public void testTermsOfServiceAreSanitizedRichText( ) throws Exception
    {
        Map<String, Object> model = new HashMap<>( );
        model.put( "tos", TOS );

        String strHtml = render( TEMPLATE_TOS, model );

        assertTrue( strHtml.contains( "<p>Conditions <em>generales</em></p>" ), strHtml );
        assertFalse( strHtml.contains( "<script" ), strHtml );
        assertFalse( strHtml.contains( "alert(" ), strHtml );
        assertFalse( strHtml.contains( "&lt;" ), strHtml );
    }
}
