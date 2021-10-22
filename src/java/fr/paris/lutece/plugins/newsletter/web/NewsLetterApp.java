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
package fr.paris.lutece.plugins.newsletter.web;

import fr.paris.lutece.plugins.newsletter.business.NewsLetter;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterHome;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterProperties;
import fr.paris.lutece.plugins.newsletter.business.NewsletterPropertiesHome;
import fr.paris.lutece.plugins.newsletter.service.NewsLetterArchiveService;
import fr.paris.lutece.plugins.newsletter.service.NewsLetterRegistrationService;
import fr.paris.lutece.plugins.newsletter.util.NewsLetterConstants;
import fr.paris.lutece.portal.service.captcha.CaptchaSecurityService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.web.xpages.XPage;
import fr.paris.lutece.portal.web.xpages.XPageApplication;
import fr.paris.lutece.util.html.HtmlTemplate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * This XPage handles the newsletter subscription, and the newsletter archives.
 */
public class NewsLetterApp implements XPageApplication
{
    // Templates used to generate the HTML code
    private static final String TEMPLATE_XPAGE_NEWSLETTER = "skin/plugins/newsletter/page_newsletter.html";
    private static final String TEMPLATE_XPAGE_TOS = "skin/plugins/newsletter/tos.html";

    // Properties
    private static final String PROPERTY_PATHLABEL = "newsletter.pagePathLabel";
    private static final String PROPERTY_PAGETITLE = "newsletter.pageTitle";

    // Bookmarks
    private static final String MARK_NEWSLETTERS_LIST = "newsletters_list";
    private static final String MARK_PLUGIN = "plugin";
    private static final String MARK_TOS = "tos";
    private static final String MARK_CAPTCHA = "captcha";
    private static final String MARK_IS_ACTIVE_CAPTCHA = "is_active_captcha";
    private static final String MARK_PROPERTIES = "properties";

    // parameters
    private static final String PARAMETER_VIEW_REQUIREMENT = "view_requirement";

    // Constants
    private static final String JCAPTCHA_PLUGIN = "jcaptcha";

    // Captcha
    private CaptchaSecurityService _captchaService;

    /**
     * 
     * @param request
     *            The HTTP request
     * @param plugin
     *            The plugin
     */
    public void init( HttpServletRequest request, Plugin plugin )
    {
    }

    /**
     * Returns the Newsletter XPage content depending on the request parameters and the current mode.
     * 
     * @param request
     *            The HTTP request.
     * @param nMode
     *            The current mode.
     * @param plugin
     *            The Plugin
     * @return The page content.
     * @throws fr.paris.lutece.portal.service.message.SiteMessageException
     *             Throws a message interpreted by the front office
     */
    public XPage getPage( HttpServletRequest request, int nMode, Plugin plugin ) throws SiteMessageException
    {
        String strAction = request.getParameter( NewsLetterConstants.PARAMETER_ACTION );
        init( request, plugin );

        XPage resultPage = null;

        if ( strAction == null )
        {
            return getPage( request, plugin );
        }

        if ( strAction.equals( NewsLetterConstants.ACTION_REGISTER ) )
        {
            NewsLetterRegistrationService.getInstance( ).doSubscription( request );
        }
        else
            if ( strAction.equals( NewsLetterConstants.ACTION_CONFIRM_UNSUBSCRIBE ) )
            {
                NewsLetterRegistrationService.getInstance( ).doConfirmUnSubscribe( request );
            }
            else
                if ( strAction.equals( NewsLetterConstants.ACTION_UNSUBSCRIBE ) )
                {
                    NewsLetterRegistrationService.getInstance( ).doUnSubscribe( request );
                }
                else
                    if ( strAction.equals( NewsLetterConstants.ACTION_CONFIRM_SUBSCRIBE ) )
                    {
                        NewsLetterRegistrationService.getInstance( ).doConfirmSubscribe( request );
                    }

        resultPage = getPage( request, plugin );

        return resultPage;
    }

    /**
     * Get the main page of this app.
     * 
     * @param request
     *            The request
     * @param plugin
     *            The newsletter plugin
     * @return The Html content to display
     */
    private XPage getPage( HttpServletRequest request, Plugin plugin )
    {
        XPage page = new XPage( );

        if ( request.getParameter( PARAMETER_VIEW_REQUIREMENT ) != null )
        {
            // See conditional use
            page.setTitle( I18nService.getLocalizedString( PROPERTY_PATHLABEL, request.getLocale( ) ) );
            page.setPathLabel( I18nService.getLocalizedString( PROPERTY_PAGETITLE, request.getLocale( ) ) );
            page.setContent( getRequirement( request, plugin ) );
        }
        else
        {
            page.setPathLabel( I18nService.getLocalizedString( PROPERTY_PATHLABEL, request.getLocale( ) ) );
            page.setTitle( I18nService.getLocalizedString( PROPERTY_PAGETITLE, request.getLocale( ) ) );

            HashMap<String, Object> model = new HashMap<String, Object>( );
            Collection<NewsLetter> list = NewsLetterHome.findAll( plugin );
            NewsLetterProperties properties = NewsletterPropertiesHome.find( plugin );
            model.put( MARK_PROPERTIES, properties );
            model.put( MARK_NEWSLETTERS_LIST, list );
            model.put( MARK_TOS, properties.getTOS( ) );

            model.put( MARK_PLUGIN, plugin );

            boolean bIsCaptchaEnabled = PluginService.isPluginEnable( JCAPTCHA_PLUGIN );
            model.put( MARK_IS_ACTIVE_CAPTCHA, bIsCaptchaEnabled );

            if ( bIsCaptchaEnabled )
            {
                _captchaService = new CaptchaSecurityService( );
                model.put( MARK_CAPTCHA, _captchaService.getHtmlCode( ) );
            }
            HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_XPAGE_NEWSLETTER, request.getLocale( ), model );
            page.setContent( template.getHtml( ) );
        }

        return page;
    }

    /**
     * Returns the Newsletter archive XPage content depending on the request parameters and the current mode.
     * 
     * @return The page content.
     * @param request
     *            The HTTP request.
     * @throws SiteMessageException
     *             A site message exception
     */
    public String getShowArchivePage( HttpServletRequest request ) throws SiteMessageException
    {
        return NewsLetterArchiveService.getInstance( ).getShowArchivePage( request );
    }

    /**
     * Performs the subscription process
     * 
     * @param request
     *            The Http request
     * @throws fr.paris.lutece.portal.service.message.SiteMessageException
     *             The error message thrown to the user
     */
    public void doSubscription( HttpServletRequest request ) throws SiteMessageException
    {
        NewsLetterRegistrationService.getInstance( ).doSubscription( request );
    }

    /**
     * Performs unsubscription process
     * 
     * @param request
     *            The http request
     * @throws fr.paris.lutece.portal.service.message.SiteMessageException
     *             The error message handled by the front office
     */
    public void doUnSubscribe( HttpServletRequest request ) throws SiteMessageException
    {
        NewsLetterRegistrationService.getInstance( ).doUnSubscribe( request );
    }

    /**
     * Confirm a subscribtion
     * 
     * @param request
     *            The request
     * @throws SiteMessageException
     *             A site message exception
     */
    public void doConfirmSubscribe( HttpServletRequest request ) throws SiteMessageException
    {
        NewsLetterRegistrationService.getInstance( ).doConfirmSubscribe( request );
    }

    /**
     * Return the newsletter requirement
     * 
     * @param request
     *            The HTTP request
     * @param plugin
     *            The Plugin
     * @return the form recap
     **/
    private String getRequirement( HttpServletRequest request, Plugin plugin )
    {
        Map<String, Object> model = new HashMap<String, Object>( );
        Locale locale = request.getLocale( );

        NewsLetterProperties properties = NewsletterPropertiesHome.find( plugin );
        model.put( MARK_TOS, properties.getTOS( ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_XPAGE_TOS, locale, model );

        return template.getHtml( );
    }

}
