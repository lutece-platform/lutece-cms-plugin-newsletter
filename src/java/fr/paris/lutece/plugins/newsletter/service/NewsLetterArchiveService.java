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
package fr.paris.lutece.plugins.newsletter.service;

import fr.paris.lutece.plugins.newsletter.business.SendingNewsLetter;
import fr.paris.lutece.plugins.newsletter.business.SendingNewsLetterHome;
import fr.paris.lutece.plugins.newsletter.util.NewsLetterConstants;
import fr.paris.lutece.portal.service.message.SiteMessage;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.message.SiteMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.util.html.HtmlTemplate;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;


/**
 * The service that renders the archived newsletters
 */
public final class NewsLetterArchiveService
{
    private static final String REGEX_ID = "^[\\d]+$";
    private static final String TEMPLATE_VIEW_NEWSLETTER_ARCHIVE = "skin/plugins/newsletter/page_newsletter_archive.html";
    private static NewsLetterArchiveService _singleton = new NewsLetterArchiveService( );

    /**
     * Constructor
     */
    private NewsLetterArchiveService( )
    {
        if ( _singleton == null )
        {
            _singleton = this;
        }
    }

    /**
     * Fetches the instance of the class
     * @return The singleton
     */
    public static NewsLetterArchiveService getInstance( )
    {
        return _singleton;
    }

    /**
     * Returns the Newsletter archive XPage content depending on the request
     * parameters and the current mode.
     * @return The page content.
     * @param request The HTTP request.
     * @throws SiteMessageException If parameters are not correct
     */
    public String getShowArchivePage( HttpServletRequest request ) throws SiteMessageException
    {
        Plugin plugin = PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
        String strSendingId = request.getParameter( NewsLetterConstants.PARAMETER_SENDING_ID );
        String strBaseUrl = AppPathService.getBaseUrl( request );

        if ( ( strSendingId == null ) || !strSendingId.matches( REGEX_ID ) )
        {
            SiteMessageService.setMessage( request, NewsLetterConstants.PROPERTY_NO_NEWSLETTER_CHOSEN_TITLE_MESSAGE,
                    SiteMessage.TYPE_ERROR );
            return StringUtils.EMPTY;
        }

        int nSendingId = Integer.parseInt( strSendingId );
        SendingNewsLetter sending = SendingNewsLetterHome.findByPrimaryKey( nSendingId, plugin );

        if ( ( sending == null ) || StringUtils.isEmpty( sending.getHtml( ) ) )
        {
            SiteMessageService.setMessage( request, NewsLetterConstants.PROPERTY_NO_NEWSLETTER_CHOSEN_TITLE_MESSAGE,
                    SiteMessage.TYPE_ERROR );
            return StringUtils.EMPTY;
        }

        Map<String, Object> model = new HashMap<String, Object>( );
        model.put( NewsLetterConstants.MARK_SENDING, sending );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_VIEW_NEWSLETTER_ARCHIVE, request.getLocale( ),
                model );
        template.substitute( NewsLetterConstants.WEBAPP_PATH_FOR_LINKSERVICE, strBaseUrl );

        return template.getHtml( );
    }
}
