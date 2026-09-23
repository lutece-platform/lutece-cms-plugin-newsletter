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
package fr.paris.lutece.plugins.newsletter.business.portlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.newsletter.business.SendingNewsLetter;
import fr.paris.lutece.plugins.newsletter.business.SendingNewsLetterHome;
import fr.paris.lutece.plugins.newsletter.service.NewsletterPlugin;
import fr.paris.lutece.portal.business.portlet.Portlet;
import fr.paris.lutece.portal.business.portlet.PortletHome;
import fr.paris.lutece.portal.business.portlet.PortletTemplate;
import fr.paris.lutece.portal.business.portlet.PortletTemplateHome;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.portal.PortalService;
import fr.paris.lutece.portal.web.LocalVariables;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.test.mocks.MockHttpServletRequest;
import fr.paris.lutece.test.mocks.MockHttpServletResponse;

/**
 * Rendering of the newsletter archive portlet with the FreeMarker templates registered in the core
 */
public class NewsLetterArchivePortletRenderingTest extends LuteceTestCase
{
    private static final String PORTLET_NAME = "NewsLetterArchivePortletRenderingTest title";
    private static final String SENDING_SUBJECT = "NewsLetterArchivePortletRenderingTest subject";
    private static final String MARKER_PORTLET = "portlet-newsletter-archive";
    private static final String MARKER_ARCHIVE_LINK = "ViewNewsletterArchive.jsp?sending_id=";
    private static final int UNKNOWN_TEMPLATE_ID = 99999;

    private Plugin _plugin;
    private SendingNewsLetter _sending;
    private NewsLetterArchivePortlet _portlet;

    @BeforeEach
    @Override
    protected void setUp( ) throws Exception
    {
        super.setUp( );
        _plugin = PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );

        _sending = new SendingNewsLetter( );
        _sending.setNewsLetterId( 1 );
        _sending.setDate( new Timestamp( System.currentTimeMillis( ) ) );
        _sending.setCountSubscribers( 0 );
        _sending.setHtml( "<p>archive</p>" );
        _sending.setEmailSubject( SENDING_SUBJECT );
        SendingNewsLetterHome.create( _sending, _plugin );

        _portlet = new NewsLetterArchivePortlet( );
        _portlet.setPageId( PortalService.getRootPageId( ) );
        _portlet.setStyleId( 0 );
        _portlet.setColumn( 1 );
        _portlet.setOrder( 1 );
        _portlet.setName( PORTLET_NAME );
        _portlet.setStatus( Portlet.STATUS_PUBLISHED );
        _portlet.setDisplayPortletTitle( 0 );
        _portlet.setDeviceDisplayFlags( Portlet.FLAG_DISPLAY_ON_NORMAL_DEVICE | Portlet.FLAG_DISPLAY_ON_LARGE_DEVICE | Portlet.FLAG_DISPLAY_ON_XLARGE_DEVICE );
        NewsLetterArchivePortletHome.getInstance( ).create( _portlet );
        NewsLetterArchivePortletHome.insertSending( _portlet.getId( ), _sending.getId( ), _plugin );
    }

    @AfterEach
    @Override
    protected void tearDown( ) throws Exception
    {
        if ( _portlet != null )
        {
            NewsLetterArchivePortletHome.getInstance( ).remove( _portlet );
        }
        if ( _sending != null )
        {
            SendingNewsLetterHome.remove( _sending.getId( ), _plugin );
        }
        LocalVariables.remove( );
        super.tearDown( );
    }

    /**
     * The shipped template is registered in the core for the portlet type
     */
    @Test
    public void testShippedTemplateRegistered( )
    {
        List<PortletTemplate> listTemplates = PortletTemplateHome.findByPortletType( NewsLetterArchivePortletHome.getInstance( ).getPortletTypeId( ) );
        assertEquals( 1, listTemplates.size( ), "the shipped template should be registered in the core for the archive portlet type" );
    }

    /**
     * The template chosen for the portlet is persisted by the core
     */
    @Test
    public void testTemplateStoredWithThePortlet( )
    {
        PortletTemplate template = PortletTemplateHome.findByPortletType( NewsLetterArchivePortletHome.getInstance( ).getPortletTypeId( ) ).get( 0 );
        _portlet.setIdTemplate( template.getId( ) );
        _portlet.update( );

        assertEquals( template.getId( ), PortletHome.findByPrimaryKey( _portlet.getId( ) ).getIdTemplate( ) );
        assertTrue( PortletTemplateHome.isTemplateUsed( template.getId( ) ) );
    }

    /**
     * Every shipped template renders the wrapper, the title, the archived sending and the device display classes
     */
    @Test
    public void testRenderEveryShippedTemplate( )
    {
        MockHttpServletRequest request = new MockHttpServletRequest( );
        LocalVariables.setLocal( null, request, new MockHttpServletResponse( ) );

        for ( PortletTemplate template : PortletTemplateHome.findByPortletType( NewsLetterArchivePortletHome.getInstance( ).getPortletTypeId( ) ) )
        {
            _portlet.setIdTemplate( template.getId( ) );
            String strContent = _portlet.getHtmlContent( request );

            assertTrue( strContent.contains( MARKER_PORTLET ), "template " + template.getTemplatePath( ) + " should render the portlet wrapper" );
            assertTrue( strContent.contains( PORTLET_NAME ), "template " + template.getTemplatePath( ) + " should render the portlet title" );
            assertTrue( strContent.contains( SENDING_SUBJECT ), "template " + template.getTemplatePath( ) + " should render the sending subject" );
            assertTrue( strContent.contains( MARKER_ARCHIVE_LINK + _sending.getId( ) ), "template " + template.getTemplatePath( ) + " should link to the archive" );
            assertTrue( strContent.contains( "d-none d-md-block" ), "template " + template.getTemplatePath( ) + " should hide the portlet on small devices" );
        }
    }

    /**
     * An unknown template falls back to the default one and a hidden title is not rendered
     */
    @Test
    public void testFallbackToDefaultTemplate( )
    {
        MockHttpServletRequest request = new MockHttpServletRequest( );
        LocalVariables.setLocal( null, request, new MockHttpServletResponse( ) );
        _portlet.setIdTemplate( UNKNOWN_TEMPLATE_ID );
        _portlet.setDisplayPortletTitle( 1 );

        String strContent = _portlet.getHtmlContent( request );

        assertTrue( strContent.contains( MARKER_PORTLET ), "the default template should render the portlet wrapper" );
        assertTrue( strContent.contains( SENDING_SUBJECT ), "the default template should render the sending subject" );
        assertFalse( strContent.contains( PORTLET_NAME ), "a hidden portlet title should not be rendered" );
    }
}
