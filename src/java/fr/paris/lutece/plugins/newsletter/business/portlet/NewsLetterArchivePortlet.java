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
package fr.paris.lutece.plugins.newsletter.business.portlet;

import fr.paris.lutece.plugins.newsletter.business.SendingNewsLetter;
import fr.paris.lutece.plugins.newsletter.business.SendingNewsLetterHome;
import fr.paris.lutece.plugins.newsletter.service.NewsletterPlugin;
import fr.paris.lutece.plugins.newsletter.util.NewsletterUtils;
import fr.paris.lutece.portal.business.portlet.Portlet;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.util.date.DateUtil;
import fr.paris.lutece.util.xml.XmlUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;


/**
 * This class represents a NewsLetterArchivePortlet.
 */
public class NewsLetterArchivePortlet extends Portlet
{
    // The names of the XML tags
    private static final String TAG_NEWSLETTER_SENDING_LIST = "newsletter-sending-list";
    private static final String TAG_NEWSLETTER_SENDING = "newsletter-sending";
    private static final String TAG_NEWSLETTER_SENDING_ID = "newsletter-sending-id";
    private static final String TAG_NEWSLETTER_SENDING_DATE = "newsletter-sending-date";
    private static final String TAG_NEWSLETTER_SENDING_SUBJECT = "newsletter-sending-subject";

    /**
     * Returns the Xml code of the Archive portlet with XML heading
     * 
     * @param request The HTTP servlet request
     * @return the Xml code of the Archive portlet
     */
    public String getXmlDocument( HttpServletRequest request )
    {
        return XmlUtil.getXmlHeader( ) + getXml( request );
    }

    /**
     * Returns the Xml code of the Archive portlet
     * 
     * @param request The HTTP servlet request
     * @return the Xml code of the Archive portlet content
     */
    public String getXml( HttpServletRequest request )
    {
        StringBuffer sbXml = new StringBuffer( );
        Plugin plugin = PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
        XmlUtil.beginElement( sbXml, TAG_NEWSLETTER_SENDING_LIST );

        ArrayList<Integer> listSendingIds = NewsLetterArchivePortletHome.findSendingsInPortlet( this.getId( ), plugin );
        ArrayList<SendingNewsLetter> listSendings = SendingNewsLetterHome.findSendingsByIds( listSendingIds, plugin );

        for ( SendingNewsLetter sending : listSendings )
        {
            XmlUtil.beginElement( sbXml, TAG_NEWSLETTER_SENDING );
            XmlUtil.addElement( sbXml, TAG_NEWSLETTER_SENDING_ID, sending.getId( ) );
            XmlUtil.addElement( sbXml, TAG_NEWSLETTER_SENDING_DATE, DateUtil.getDateString( sending.getDate( ), NewsletterUtils.getLocale(request)) );
            XmlUtil.addElementHtml( sbXml, TAG_NEWSLETTER_SENDING_SUBJECT, sending.getEmailSubject( ) );
            XmlUtil.endElement( sbXml, TAG_NEWSLETTER_SENDING );
        }

        XmlUtil.endElement( sbXml, TAG_NEWSLETTER_SENDING_LIST );

        return addPortletTags( sbXml );
    }

    /**
     * Updates the current instance of the HtmlPortlet object
     */
    public void update( )
    {
        NewsLetterArchivePortletHome.getInstance( ).update( this );
    }

    /**
     * Removes the current instance of the HtmlPortlet object
     */
    public void remove( )
    {
        NewsLetterArchivePortletHome.getInstance( ).remove( this );
    }
}
