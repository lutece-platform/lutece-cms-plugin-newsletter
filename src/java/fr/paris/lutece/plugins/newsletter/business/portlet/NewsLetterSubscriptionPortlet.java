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

import fr.paris.lutece.plugins.newsletter.business.NewsLetter;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterHome;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterProperties;
import fr.paris.lutece.plugins.newsletter.business.NewsletterPropertiesHome;
import fr.paris.lutece.plugins.newsletter.service.NewsletterPlugin;
import fr.paris.lutece.portal.business.portlet.PortletHtmlContent;
import fr.paris.lutece.portal.service.captcha.ICaptchaService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.BeanUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This class represents the business object NewsLetterSubscriptionPortlet : a subscription form to the newsletters selected for the portlet. The content
 * is rendered with the FreeMarker template chosen for the portlet among the templates registered for the portlet type in the core (Section Template
 * Management feature).
 */
public class NewsLetterSubscriptionPortlet extends PortletHtmlContent
{
    // Templates
    private static final String TEMPLATE_PORTLET_DEFAULT = "skin/plugins/newsletter/portlet/newsletter_subscription_portlet.html";

    // Marks
    private static final String MARK_NEWSLETTERS = "newsletters";
    private static final String MARK_SITE_PATH = "site_path";
    private static final String MARK_CAPTCHA = "captcha";
    private static final String MARK_TOS = "tos";
    private static final String MARK_EMAIL_ERROR = "email_error";
    private static final String MARK_NO_CHOICE_ERROR = "nochoice_error";

    // Parameters
    private static final String PARAMETER_EMAIL_ERROR = "email-error";
    private static final String PARAMETER_NO_NEWSLETTER_CHOSEN = "nochoice-error";

    private static final String JCAPTCHA_PLUGIN = "jcaptcha";

    /**
     * Comparator for sorting - last sending date descending order, newsletters never sent last
     */
    private static final Comparator<NewsLetter> COMPARATOR_DATE_DESC = Comparator.comparing( NewsLetter::getDateLastSending,
            Comparator.nullsLast( Comparator.reverseOrder( ) ) );

    /**
     * Sets the identifier of the portlet type to the value specified in the plugin descriptor
     */
    public NewsLetterSubscriptionPortlet( )
    {
        setPortletTypeId( NewsLetterSubscriptionPortletHome.getInstance( ).getPortletTypeId( ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHtmlContent( HttpServletRequest request )
    {
        Plugin plugin = PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );

        Map<String, Object> model = createPortletModel( );
        model.put( MARK_NEWSLETTERS, getNewsletters( plugin ) );
        model.put( MARK_SITE_PATH, AppPathService.getPortalUrl( ) );

        NewsLetterProperties properties = NewsletterPropertiesHome.find( plugin );

        if ( properties != null )
        {
            if ( properties.isCaptchaActive( ) && PluginService.isPluginEnable( JCAPTCHA_PLUGIN ) )
            {
                Instance<ICaptchaService> captchaInstance = CDI.current( ).select( ICaptchaService.class, NamedLiteral.of( BeanUtils.BEAN_CAPTCHA_SERVICE ) );

                if ( captchaInstance.isResolvable( ) )
                {
                    model.put( MARK_CAPTCHA, captchaInstance.get( ).getHtmlCode( ) );
                }
            }

            if ( StringUtils.isNotEmpty( properties.getTOS( ) ) )
            {
                model.put( MARK_TOS, properties.getTOS( ) );
            }
        }

        if ( request != null )
        {
            if ( StringUtils.isNotBlank( request.getParameter( PARAMETER_EMAIL_ERROR ) ) )
            {
                model.put( MARK_EMAIL_ERROR, Boolean.TRUE );
            }

            if ( StringUtils.isNotBlank( request.getParameter( PARAMETER_NO_NEWSLETTER_CHOSEN ) ) )
            {
                model.put( MARK_NO_CHOICE_ERROR, Boolean.TRUE );
            }
        }

        return renderTemplate( request, TEMPLATE_PORTLET_DEFAULT, model );
    }

    /**
     * Returns the newsletters proposed by the portlet, the most recently sent first. The association table and the newsletters table live on two
     * different datasources (core and plugin pools), hence the manual join and ordering.
     * 
     * @param plugin
     *            the plugin
     * @return the newsletters
     */
    private List<NewsLetter> getNewsletters( Plugin plugin )
    {
        List<NewsLetter> listNewsletters = new ArrayList<>( );

        for ( Integer nIdNewsletter : NewsLetterSubscriptionPortletHome.findSelectedNewsletters( getId( ) ) )
        {
            NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nIdNewsletter, plugin );

            if ( newsletter != null )
            {
                listNewsletters.add( newsletter );
            }
        }

        listNewsletters.sort( COMPARATOR_DATE_DESC );

        return listNewsletters;
    }

    /**
     * Updates the current instance of the NewsLetterSubscriptionPortlet object
     */
    public void update( )
    {
        NewsLetterSubscriptionPortletHome.getInstance( ).update( this );
    }

    /**
     * Removes the current instance of the NewsLetterSubscriptionPortlet object
     */
    @Override
    public void remove( )
    {
        NewsLetterSubscriptionPortletHome.getInstance( ).remove( this );
    }
}
