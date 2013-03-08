/*
 * Copyright (c) 2002-2012, Mairie de Paris
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

import fr.paris.lutece.plugins.newsletter.business.NewsLetter;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterHome;
import fr.paris.lutece.plugins.newsletter.business.Subscriber;
import fr.paris.lutece.plugins.newsletter.business.SubscriberHome;
import fr.paris.lutece.plugins.newsletter.business.section.NewsletterSection;
import fr.paris.lutece.plugins.newsletter.business.section.NewsletterSectionHome;
import fr.paris.lutece.plugins.newsletter.service.section.NewsletterSectionService;
import fr.paris.lutece.plugins.newsletter.util.NewsLetterConstants;
import fr.paris.lutece.plugins.newsletter.util.NewsletterUtils;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.util.html.HtmlTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * The newsletter service
 * 
 */
public class NewsletterService
{
    public static final String BEAN_NAME = "newsletter.newsletterService";

    private NewsletterSectionService _newsletterSectionService = null;
    private Plugin _plugin = null;

    /**
     * Returns the instance of the singleton
     * 
     * @return The instance of the singleton
     */
    public static NewsletterService getService( )
    {
        return SpringContextService.getBean( BEAN_NAME );
    }

    /**
     * Remove a known suscriber from a newsletter
     * @param subscriber the subscriber to remove
     * @param nNewsletterId the newsletter id from which to remove the
     *            subscriber
     * @param plugin The plugin object
     */
    public void removeSubscriberFromNewsletter( Subscriber subscriber, int nNewsletterId, Plugin plugin )
    {
        /* checks newsletter exist in database */
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, plugin );

        if ( ( subscriber != null ) && ( newsletter != null ) )
        {
            int nSubscriberId = subscriber.getId( );

            /* checks if the subscriber identified is registered */
            if ( NewsLetterHome.findRegistration( nNewsletterId, nSubscriberId, plugin ) )
            {
                /* unregistration */
                NewsLetterHome.removeSubscriber( nNewsletterId, nSubscriberId, plugin );
            }

            /*
             * if the subscriber is not registered to an other newsletter, his
             * account is deleted
             */
            if ( SubscriberHome.findNewsLetters( nSubscriberId, plugin ) == 0 )
            {
                SubscriberHome.remove( nSubscriberId, plugin );
            }
        }
    }

    /**
     * Generate the html code of the newsletter according to the document and
     * newsletter templates
     * 
     * @param newsletter the newsletter to generate the HTML of
     * @param nTemplateNewsLetterId the newsletter template id
     * @param nTemplateDocumentId the document template id
     * @param strBaseUrl The base url of the portal
     * @param user the current user
     * @param locale The locale
     * @return the html code for the newsletter content of null if no template
     *         available
     */
    public String generateNewsletterHtmlCode( NewsLetter newsletter, int nTemplateNewsLetterId,
            int nTemplateDocumentId, String strBaseUrl, AdminUser user, Locale locale )
    {
        String strTemplatePath = NewsletterUtils.getHtmlTemplatePath( nTemplateNewsLetterId, getPlugin( ) );
        //        String strDocumentPath = generateDocumentsList( nNewsLetterId, nTemplateDocumentId, strBaseUrl );

        if ( strTemplatePath == null )
        {
            return null;
        }

        Map<String, Object> model = new HashMap<String, Object>( );
        List<NewsletterSection> listSections = NewsletterSectionHome.findAllByIdNewsletter( newsletter.getId( ),
                getPlugin( ) );

        // We sort the elements so that they are ordered by category and order.
        Collections.sort( listSections );

        int nCurrentCategory = 0;
        String[] strContentByCategory = new String[newsletter.getNbCategories( )];
        List<NewsletterSection> listSelectedSections = new ArrayList<NewsletterSection>( );
        for ( int i = 0; i < listSections.size( ) + 1; i++ )
        {
            NewsletterSection newsletterSection = null;
            if ( i < listSections.size( ) )
            {
                newsletterSection = listSections.get( i );
            }
            if ( newsletterSection != null && newsletterSection.getCategory( ) == nCurrentCategory )
            {
                listSelectedSections.add( newsletterSection );
            }
            else
            {
                if ( nCurrentCategory != 0 )
                {
                    StringBuilder sbCategoryContent = new StringBuilder( );
                    for ( NewsletterSection section : listSelectedSections )
                    {
                        sbCategoryContent.append( getNewsletterSectionService( ).getSectionContent( section, user,
                                locale ) );
                    }
                    strContentByCategory[nCurrentCategory - 1] = sbCategoryContent.toString( );
                }
                if ( newsletterSection != null )
                {
                    nCurrentCategory = newsletterSection.getCategory( );
                    listSelectedSections = new ArrayList<NewsletterSection>( );
                    listSelectedSections.add( newsletterSection );
                }
            }
        }

        model.put( NewsLetterConstants.MARK_CONTENT, strContentByCategory[0] );
        for ( int i = 0; i < strContentByCategory.length; i++ )
        {
            model.put( NewsLetterConstants.MARK_CONTENT_CATEGORY + Integer.toString( i + 1 ), strContentByCategory[i] );
        }
        model.put( NewsLetterConstants.MARK_BASE_URL, strBaseUrl );

        HtmlTemplate templateNewsLetter = AppTemplateService.getTemplate( strTemplatePath, locale, model );

        return templateNewsLetter.getHtml( );
    }

    /**
     * Get the instance of the newsletter plugin
     * @return the instance of the newsletter plugin
     */
    private Plugin getPlugin( )
    {
        if ( _plugin == null )
        {
            _plugin = PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
        }
        return _plugin;
    }

    /**
     * Get the NewsletterSectionService instance of this service
     * @return The NewsletterSectionService instance of this service
     */
    private NewsletterSectionService getNewsletterSectionService( )
    {
        if ( _newsletterSectionService == null )
        {
            _newsletterSectionService = NewsletterSectionService.getService( );
        }
        return _newsletterSectionService;
    }
}
