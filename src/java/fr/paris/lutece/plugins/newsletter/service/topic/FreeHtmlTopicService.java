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
package fr.paris.lutece.plugins.newsletter.service.topic;

import fr.paris.lutece.plugins.newsletter.business.topic.FreeHtmlTopic;
import fr.paris.lutece.plugins.newsletter.business.topic.FreeHtmlTopicHome;
import fr.paris.lutece.plugins.newsletter.business.topic.NewsletterTopic;
import fr.paris.lutece.plugins.newsletter.service.NewsletterPlugin;
import fr.paris.lutece.plugins.newsletter.util.NewsletterUtils;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.util.html.HtmlTemplate;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * Service to manage topics with free html.
 */
public class FreeHtmlTopicService implements INewsletterTopicService
{

    /**
     * Code of the topic type
     */
    public static final String NEWSLETTER_FREE_HTML_TOPIC_TYPE_CODE = "FREE_HTML";

    private static final String NEWSLETTER_FREE_HTML_TOPIC_TYPE_NAME = "newsletter.topic.freeHtmlTopicType";

    // MARKS
    private static final String MARK_HTML_TOPIC = "htmlTopic";
    private static final String MARK_WEBAPP_URL = "webapp_url";
    private static final String MARK_LOCALE = "locale";

    // PARAMETERS
    private static final String PARAMETER_CONTENT = "html_content";

    // TEMPLATES
    private static final String TEMPLATE_MODIFY_FREE_HTML_CONFIGURATION = "admin/plugins/newsletter/free_html/modify_config_free_html.html";

    private Plugin _plugin;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNewsletterTopicTypeCode( )
    {
        return NEWSLETTER_FREE_HTML_TOPIC_TYPE_CODE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNewsletterTopicTypeName( Locale locale )
    {
        return I18nService.getLocalizedString( NEWSLETTER_FREE_HTML_TOPIC_TYPE_NAME, locale );
    }

    /**
     * {@inheritDoc}
     * 
     * @return This method always returns <i>true</i>.
     */
    public boolean hasConfiguration( )
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationPage( NewsletterTopic newsletterTopic, String strBaseUrl, AdminUser user, Locale locale )
    {
        FreeHtmlTopic htmlTopic = FreeHtmlTopicHome.findByPrimaryKey( newsletterTopic.getId( ), getPlugin( ) );
        Map<String, Object> model = new HashMap<String, Object>( );

        model.put( MARK_HTML_TOPIC, htmlTopic );
        model.put( MARK_WEBAPP_URL, strBaseUrl );
        model.put( MARK_LOCALE, locale );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MODIFY_FREE_HTML_CONFIGURATION, locale, model );

        return template.getHtml( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveConfiguration( Map<String, String [ ]> mapParameters, NewsletterTopic newsletterTopic, AdminUser user, Locale locale )
    {
        String strContent = NewsletterUtils.getStringFromStringArray( mapParameters.get( PARAMETER_CONTENT ) );
        if ( StringUtils.isNotEmpty( strContent ) )
        {
            FreeHtmlTopic topic = FreeHtmlTopicHome.findByPrimaryKey( newsletterTopic.getId( ), getPlugin( ) );
            topic.setHtmlContent( strContent );
            FreeHtmlTopicHome.updateFreeHtmlTopic( topic, getPlugin( ) );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createNewsletterTopic( NewsletterTopic newsletterTopic, AdminUser user, Locale locale )
    {
        FreeHtmlTopic freeHtmlTopic = new FreeHtmlTopic( );
        freeHtmlTopic.setId( newsletterTopic.getId( ) );
        freeHtmlTopic.setHtmlContent( StringUtils.EMPTY );
        FreeHtmlTopicHome.insertFreeHtmlTopic( freeHtmlTopic, getPlugin( ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeNewsletterTopic( int nNewsletterTopicId )
    {
        FreeHtmlTopicHome.removeFreeHtmlTopic( nNewsletterTopicId, getPlugin( ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHtmlContent( NewsletterTopic newsletterTopic, AdminUser user, Locale locale )
    {
        FreeHtmlTopic freeHtmlTopic = FreeHtmlTopicHome.findByPrimaryKey( newsletterTopic.getId( ), getPlugin( ) );
        return freeHtmlTopic.getHtmlContent( );
    }

    /**
     * Get the instance of the newsletter plugin
     * 
     * @return the newsletter plugin
     */
    private Plugin getPlugin( )
    {
        if ( _plugin == null )
        {
            _plugin = PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
        }
        return _plugin;
    }

    @Override
    public void copyNewsletterTopic( int oldTopicId, NewsletterTopic newsletterTopic, AdminUser user, Locale locale )
    {
        FreeHtmlTopic oldFreeHtmlTopic = FreeHtmlTopicHome.findByPrimaryKey( oldTopicId, getPlugin( ) );

        FreeHtmlTopic freeHtmlTopic = new FreeHtmlTopic( );
        freeHtmlTopic.setId( newsletterTopic.getId( ) );
        if ( oldFreeHtmlTopic != null )
        {
            freeHtmlTopic.setHtmlContent( oldFreeHtmlTopic.getHtmlContent( ) );
        }
        else
        {
            freeHtmlTopic.setHtmlContent( StringUtils.EMPTY );
        }
        FreeHtmlTopicHome.insertFreeHtmlTopic( freeHtmlTopic, getPlugin( ) );
    }
}
