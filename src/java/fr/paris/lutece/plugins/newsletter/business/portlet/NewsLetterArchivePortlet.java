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

import fr.paris.lutece.plugins.newsletter.business.SendingNewsLetter;
import fr.paris.lutece.plugins.newsletter.business.SendingNewsLetterHome;
import fr.paris.lutece.plugins.newsletter.service.NewsletterPlugin;
import fr.paris.lutece.portal.business.portlet.PortletHtmlContent;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This class represents the business object NewsLetterArchivePortlet : the list of the newsletter sendings archived in the portlet. The content is
 * rendered with the FreeMarker template chosen for the portlet among the templates registered for the portlet type in the core (Section Template
 * Management feature).
 */
public class NewsLetterArchivePortlet extends PortletHtmlContent
{
    // Templates
    private static final String TEMPLATE_PORTLET_DEFAULT = "skin/plugins/newsletter/portlet/newsletter_archive_portlet.html";

    // Marks
    private static final String MARK_SENDINGS = "sendings";

    /**
     * Sets the identifier of the portlet type to the value specified in the plugin descriptor
     */
    public NewsLetterArchivePortlet( )
    {
        setPortletTypeId( NewsLetterArchivePortletHome.getInstance( ).getPortletTypeId( ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHtmlContent( HttpServletRequest request )
    {
        Plugin plugin = PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );

        ArrayList<Integer> listSendingIds = NewsLetterArchivePortletHome.findSendingsInPortlet( getId( ), plugin );
        List<SendingNewsLetter> listSendings = SendingNewsLetterHome.findSendingsByIds( listSendingIds, plugin );

        Map<String, Object> model = createPortletModel( );
        model.put( MARK_SENDINGS, listSendings );

        return renderTemplate( request, TEMPLATE_PORTLET_DEFAULT, model );
    }

    /**
     * Updates the current instance of the NewsLetterArchivePortlet object
     */
    public void update( )
    {
        NewsLetterArchivePortletHome.getInstance( ).update( this );
    }

    /**
     * Removes the current instance of the NewsLetterArchivePortlet object
     */
    @Override
    public void remove( )
    {
        NewsLetterArchivePortletHome.getInstance( ).remove( this );
    }
}
