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

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.plugins.newsletter.business.AwaitingActivationHome;
import fr.paris.lutece.plugins.newsletter.business.NewsLetter;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterHome;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterProperties;
import fr.paris.lutece.plugins.newsletter.business.NewsletterPropertiesHome;
import fr.paris.lutece.plugins.newsletter.business.Subscriber;
import fr.paris.lutece.plugins.newsletter.business.SubscriberHome;
import fr.paris.lutece.plugins.newsletter.util.NewsLetterConstants;
import fr.paris.lutece.plugins.newsletter.util.NewsletterUtils;
import fr.paris.lutece.portal.service.captcha.CaptchaSecurityService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.mail.MailService;
import fr.paris.lutece.portal.service.message.SiteMessage;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.message.SiteMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.string.StringUtil;
import fr.paris.lutece.util.url.UrlItem;


/**
 * The class responsible for the subscription and unsubscription process
 */
public final class NewsLetterRegistrationService
{
    private static final String PARAMETER_TOS = "tos";
    private static final String TEMPLATE_CONFIRM_MAIL = "admin/plugins/newsletter/confirm_mail.html";
    private static final String JSP_PORTAL = "/jsp/site/Portal.jsp";

    //properties
    private static final String PROPERTY_MESSAGE_CONFIRM_MAIL_TITLE = "newsletter.confirm_mail.title";
    private static final String PROPERTY_LIMIT_CONFIRM_DAYS = "newsletter.confirm.limit";
    private static final String REGEX_ID = "^[\\d]+$";
    private static final String JCAPTCHA_PLUGIN = "jcaptcha";

    //default values
    private static final int DEFAULT_LIMIT = 7;

    /**
     * The registration service
     */
    private static NewsLetterRegistrationService _singleton = new NewsLetterRegistrationService( );

    //Captcha
    private CaptchaSecurityService _captchaService;

    private Plugin _plugin;

    /**
     * Constructor
     */
    private NewsLetterRegistrationService( )
    {
        if ( _singleton == null )
        {
            _singleton = this;
        }
    }

    /**
     * Fetches the singleton instance
     * @return The singleton instance
     */
    public static NewsLetterRegistrationService getInstance( )
    {
        return _singleton;
    }

    /**
     * Performs the subscription process
     * Throw a SiteMessage
     * @param request The Http request
     * @throws fr.paris.lutece.portal.service.message.SiteMessageException The
     *             error message thrown to the user
     */
    public void doSubscription( HttpServletRequest request ) throws SiteMessageException
    {
        String strEmail = request.getParameter( NewsLetterConstants.PARAMETER_EMAIL );
        String[] arrayNewsletters = request.getParameterValues( NewsLetterConstants.PARAMETER_NEWSLETTER_ID );
        String strTOS = request.getParameter( PARAMETER_TOS );

        if ( ( strEmail == null ) || !StringUtil.checkEmail( strEmail ) )
        {
            SiteMessageService.setMessage( request, NewsLetterConstants.PROPERTY_INVALID_MAIL_ERROR_MESSAGE,
                    NewsLetterConstants.PROPERTY_INVALID_MAIL_TITLE_MESSAGE, SiteMessage.TYPE_STOP );
        }

        if ( arrayNewsletters == null )
        {
            SiteMessageService.setMessage( request, NewsLetterConstants.PROPERTY_NO_NEWSLETTER_CHOSEN_ERROR_MESSAGE,
                    NewsLetterConstants.PROPERTY_NO_NEWSLETTER_CHOSEN_TITLE_MESSAGE, SiteMessage.TYPE_STOP );
        }
        else
        {
            NewsLetterProperties properties = NewsletterPropertiesHome.find( getPlugin( ) );

            //test the requirement
            if ( properties.getTOS( ) != null )
            {
                if ( strTOS == null )
                {
                    SiteMessageService.setMessage( request, NewsLetterConstants.PROPERTY_NO_TOS_MESSAGE,
                            NewsLetterConstants.PROPERTY_NO_TOS_TITLE_MESSAGE, SiteMessage.TYPE_STOP );
                }
            }

            //test the captcha
            if ( PluginService.isPluginEnable( JCAPTCHA_PLUGIN ) && properties.isCaptchaActive( ) )
            {
                _captchaService = new CaptchaSecurityService( );

                if ( !_captchaService.validate( request ) )
                {
                    SiteMessageService.setMessage( request, NewsLetterConstants.PROPERTY_NO_JCAPTCHA_MESSAGE,
                            NewsLetterConstants.PROPERTY_NO_JCAPTCHA_TITLE_MESSAGE, SiteMessage.TYPE_STOP );
                }
            }

            //Checks if a subscriber with the same email address doesn't exist yet
            Subscriber subscriber = SubscriberHome.findByEmail( strEmail, getPlugin( ) );

            if ( subscriber == null )
            {
                // The email doesn't exist, so create a new subcriber
                subscriber = new Subscriber( );
                subscriber.setEmail( strEmail );
                subscriber = SubscriberHome.create( subscriber, getPlugin( ) );
            }

            for ( String strId : arrayNewsletters )
            {
                NewsLetterHome.addSubscriber( Integer.parseInt( strId ), subscriber.getId( ),
                        !properties.isValidationActive( ), new Timestamp( new Date( ).getTime( ) ), getPlugin( ) );
            }

            if ( properties.isValidationActive( ) )
            {
                // generate validation key
                Random random = new Random( );
                int nAlea = random.nextInt( );
                // add pair in db
                AwaitingActivationHome.create( subscriber.getId( ), nAlea, getPlugin( ) );

                StringBuilder sbUrl = new StringBuilder( AppPathService.getBaseUrl( request ) );
                sbUrl.append( JSP_PORTAL );

                UrlItem urlItem = new UrlItem( sbUrl.toString( ) );
                urlItem.addParameter( NewsLetterConstants.PARAMETER_PAGE, NewsletterPlugin.PLUGIN_NAME );
                urlItem.addParameter( NewsLetterConstants.PARAMETER_ACTION,
                        NewsLetterConstants.ACTION_CONFIRM_SUBSCRIBE );
                urlItem.addParameter( NewsLetterConstants.PARAMETER_KEY, nAlea );
                urlItem.addParameter( NewsLetterConstants.PARAMETER_EMAIL, strEmail );
                urlItem.addParameter( NewsLetterConstants.PARAMETER_USER_ID, subscriber.getId( ) );
                NewsletterUtils.addParameters( urlItem, NewsLetterConstants.PARAMETER_NEWSLETTER_ID,
                        request.getParameterValues( NewsLetterConstants.PARAMETER_NEWSLETTER_ID ) );

                Map<Object, String> model = new HashMap<Object, String>( );
                model.put( NewsLetterConstants.MARK_CONFIRM_URL, urlItem.getUrl( ) );

                HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_CONFIRM_MAIL, request.getLocale( ),
                        model );

                MailService.sendMailHtml( subscriber.getEmail( ),
                        NewsLetterConstants.PROPERTY_CONFIRM_MAIL_SENDER_NAME,
                        NewsLetterConstants.PROPERTY_CONFIRM_MAIL_SENDER_ADDRESS,
                        I18nService.getLocalizedString( PROPERTY_MESSAGE_CONFIRM_MAIL_TITLE, request.getLocale( ) ),
                        template.getHtml( ) );
            }

            String strMessage;

            if ( properties.isValidationActive( ) )
            {
                strMessage = NewsLetterConstants.PROPERTY_SUBSCRIPTION_OK_ALERT_MESSAGE_CONFIRM;
            }
            else
            {
                strMessage = NewsLetterConstants.PROPERTY_SUBSCRIPTION_OK_ALERT_MESSAGE;
            }

            SiteMessageService.setMessage( request, strMessage,
                    NewsLetterConstants.PROPERTY_SUBSCRIPTION_OK_TITLE_MESSAGE, SiteMessage.TYPE_INFO );
        }
    }

    /**
     * Get the confirmation page to suscribe a user
     * @param request The request
     * @throws SiteMessageException Site message exception
     */
    public void doConfirmSubscribe( HttpServletRequest request ) throws SiteMessageException
    {
        String[] arrayNewsletters = request.getParameterValues( NewsLetterConstants.PARAMETER_NEWSLETTER_ID );

        String strEmail = request.getParameter( NewsLetterConstants.PARAMETER_EMAIL );

        if ( StringUtils.isBlank( strEmail ) || !StringUtil.checkEmail( strEmail ) )
        {
            SiteMessageService.setMessage( request, NewsLetterConstants.PROPERTY_INVALID_MAIL_ERROR_MESSAGE,
                    NewsLetterConstants.PROPERTY_INVALID_MAIL_TITLE_MESSAGE, SiteMessage.TYPE_STOP );
        }

        String strIdUser = request.getParameter( NewsLetterConstants.PARAMETER_USER_ID );
        int nIdUser = -1;

        try
        {
            nIdUser = Integer.parseInt( strIdUser );
        }
        catch ( NumberFormatException nfe )
        {
            SiteMessageService.setMessage( request,
                    NewsLetterConstants.PROPERTY_SUBSCRIPTION_INVALID_USER_ERROR_MESSAGE,
                    NewsLetterConstants.PROPERTY_SUBSCRIPTION_INVALID_USER_TITLE_MESSAGE, SiteMessage.TYPE_STOP );
        }

        String strKey = request.getParameter( NewsLetterConstants.PARAMETER_KEY );
        int nKey = 0;

        try
        {
            nKey = Integer.parseInt( strKey );
        }
        catch ( NumberFormatException nfe )
        {
            SiteMessageService.setMessage( request,
                    NewsLetterConstants.PROPERTY_SUBSCRIPTION_INVALID_KEY_ERROR_MESSAGE,
                    NewsLetterConstants.PROPERTY_SUBSCRIPTION_INVALID_KEY_TITLE_MESSAGE, SiteMessage.TYPE_STOP );
        }

        if ( arrayNewsletters == null )
        {
            SiteMessageService.setMessage( request, NewsLetterConstants.PROPERTY_NO_NEWSLETTER_CHOSEN_ERROR_MESSAGE,
                    NewsLetterConstants.PROPERTY_NO_NEWSLETTER_CHOSEN_TITLE_MESSAGE, SiteMessage.TYPE_STOP );
        }
        else
        {
            //Checks if a subscriber with the same email address doesn't exist yet
            Subscriber subscriber = SubscriberHome.findByEmail( strEmail, getPlugin( ) );

            if ( subscriber == null )
            {
                SiteMessageService.setMessage( request,
                        NewsLetterConstants.PROPERTY_SUBSCRIPTION_INVALID_USER_ERROR_MESSAGE,
                        NewsLetterConstants.PROPERTY_SUBSCRIPTION_INVALID_USER_TITLE_MESSAGE, SiteMessage.TYPE_STOP );
                return;
            }

            boolean bValidKey = AwaitingActivationHome.checkKey( nIdUser, nKey, getPlugin( ) );

            if ( !bValidKey )
            {
                SiteMessageService.setMessage( request,
                        NewsLetterConstants.PROPERTY_SUBSCRIPTION_INVALID_KEY_ERROR_MESSAGE,
                        NewsLetterConstants.PROPERTY_SUBSCRIPTION_INVALID_KEY_TITLE_MESSAGE, SiteMessage.TYPE_STOP );
            }

            for ( String strIdNewsLetter : arrayNewsletters )
            {
                try
                {
                    NewsLetterHome.validateSubscriber( Integer.parseInt( strIdNewsLetter ), subscriber.getId( ),
                            getPlugin( ) );
                }
                catch ( NumberFormatException nfe )
                {
                    AppLogService.error( "NewsLetterRegistrationService.doConfirmSubscribe() " + nfe );
                }
            }

            // remove validation key entry
            AwaitingActivationHome.remove( nIdUser, nKey, getPlugin( ) );
        }

        SiteMessageService.setMessage( request, NewsLetterConstants.PROPERTY_SUBSCRIPTION_CONFIRM_ALERT_MESSAGE,
                SiteMessage.TYPE_INFO, request.getRequestURI( ),
                NewsLetterConstants.PROPERTY_SUBSCRIPTION_CONFIRM_TITLE_MESSAGE, null );
    }

    /**
     * Performs unsubscription process
     * Throw a SiteMessage
     * @param request The http request
     * @throws fr.paris.lutece.portal.service.message.SiteMessageException The
     *             error message handled by the front office
     */
    public void doUnSubscribe( HttpServletRequest request ) throws SiteMessageException
    {
        String strEmail = request.getParameter( NewsLetterConstants.PARAMETER_EMAIL );
        String strKey = request.getParameter( NewsLetterConstants.MARK_UNSUBSCRIBE_KEY );

        if ( ( strEmail == null ) || !StringUtil.checkEmail( strEmail )
                || !StringUtils.equals( strKey, NewsletterService.getService( ).getUnsubscriptionKey( strEmail ) ) )
        {
            SiteMessageService.setMessage( request, NewsLetterConstants.PROPERTY_INVALID_MAIL_ERROR_MESSAGE,
                    SiteMessage.TYPE_ERROR );
        }

        String strNewsletterId = request.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_ID );

        if ( ( strNewsletterId == null ) || !strNewsletterId.matches( REGEX_ID ) )
        {
            SiteMessageService.setMessage( request, NewsLetterConstants.PROPERTY_NO_NEWSLETTER_CHOSEN_ERROR_MESSAGE,
                    SiteMessage.TYPE_ERROR );
            return;
        }

        // strNewsletterId cannot be null (exception already thrown by SiteMessageService)
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        Plugin plugin = PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
        Subscriber subscriber = SubscriberHome.findByEmail( strEmail, plugin );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, plugin );

        if ( ( subscriber != null ) && ( newsletter != null ) )
        {
            int nSubscriberId = subscriber.getId( );

            if ( NewsLetterHome.findRegistration( nNewsletterId, nSubscriberId, plugin ) )
            {
                NewsLetterHome.removeSubscriber( nNewsletterId, nSubscriberId, plugin );
            }

            if ( SubscriberHome.findNewsLetters( nSubscriberId, plugin ) == 0 )
            {
                SubscriberHome.remove( nSubscriberId, plugin );
            }
        }

        UrlItem urlItem = new UrlItem( request.getRequestURI( ) );
        SiteMessageService.setMessage( request, NewsLetterConstants.PROPERTY_UNSUBSCRIPTION_OK_ALERT_MESSAGE, null,
                NewsLetterConstants.PROPERTY_UNSUBSCRIPTION_OK_TITLE_MESSAGE, urlItem.getUrl( ), null,
                SiteMessage.TYPE_INFO );
    }

    /**
     * Performs confirm unsubscription process
     * @param request The http request
     * @throws SiteMessageException The error message handled by the front
     *             office
     */
    public void doConfirmUnSubscribe( HttpServletRequest request ) throws SiteMessageException
    {
        UrlItem urlItem = new UrlItem( request.getRequestURI( ) );
        urlItem.addParameter( NewsLetterConstants.PARAMETER_PAGE, NewsletterPlugin.PLUGIN_NAME );
        urlItem.addParameter( NewsLetterConstants.PARAMETER_ACTION, NewsLetterConstants.ACTION_UNSUBSCRIBE );
        urlItem.addParameter( NewsLetterConstants.PARAMETER_EMAIL,
                request.getParameter( NewsLetterConstants.MARK_SUBSCRIBER_EMAIL ) );
        urlItem.addParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_ID,
                request.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_ID ) );
        urlItem.addParameter( NewsLetterConstants.MARK_UNSUBSCRIBE_KEY,
                request.getParameter( NewsLetterConstants.MARK_UNSUBSCRIBE_KEY ) );
        SiteMessageService.setMessage( request, NewsLetterConstants.PROPERTY_CONFIRM_UNSUBSCRIPTION_ALERT_MESSAGE,
                null, NewsLetterConstants.PROPERTY_CONFIRM_UNSUBSCRIPTION_TITLE_MESSAGE, urlItem.getUrl( ), null,
                SiteMessage.TYPE_INFO );
    }

    /**
     * Performs confirm unsubscription process
     * @return logs the logs
     */
    public String doRemoveOldUnconfirmed( )
    {
        StringBuffer sbLogs = new StringBuffer( );
        sbLogs.append( "\r\n[Start] Starting cleaning newsletter subscribers daemon...\r\n" );

        long lDuration = System.currentTimeMillis( );
        Plugin plugin = PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
        int nConfirmLimit = AppPropertiesService.getPropertyInt( PROPERTY_LIMIT_CONFIRM_DAYS, DEFAULT_LIMIT );
        NewsLetterHome.removeOldUnconfirmed( nConfirmLimit, plugin );
        sbLogs.append( "\r\n[End] Duration : " + ( System.currentTimeMillis( ) - lDuration ) + " milliseconds\r\n" );

        return sbLogs.toString( );
    }

    /**
     * Get the newsletter plugin
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
}
