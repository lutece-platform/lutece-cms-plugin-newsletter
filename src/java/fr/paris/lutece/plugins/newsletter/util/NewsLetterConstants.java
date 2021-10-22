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
package fr.paris.lutece.plugins.newsletter.util;

import fr.paris.lutece.portal.service.util.AppPropertiesService;

/**
 * Shared constants
 */
public final class NewsLetterConstants
{
    public static final String CONSTANT_EMPTY_STRING = "";
    public static final String CONSTANT_SLASH = "/";
    public static final String CONSTANT_PDF_FILE_TYPE = "pdf";

    ///////////////////////////////////////////////////////////////////////
    // BOOKMARKS
    public static final String MARK_ROWS = "rows";
    public static final String MARK_TEMPLATE_ID = "template_id";
    public static final String MARK_TEMPLATE_TYPE = "template_type";
    public static final String MARK_TEMPLATE_DESCRIPTION = "template_description";
    public static final String MARK_TEMPLATE_FILE_NAME = "template_file_name";
    public static final String MARK_TEMPLATE_PICTURE = "template_picture";
    public static final String MARK_TEMPLATE_SOURCE = "template_source";
    public static final String MARK_SUBSCRIBER_EMAIL = "subscriber_email";
    public static final String MARK_SUBSCRIBER_EMAIL_EACH = "@email@"; // TODO remove the @
    public static final String MARK_SUBSCRIBER_DATE = "subscriber_date";
    public static final String MARK_SUBSCRIBER_ID = "subscriber_id";
    public static final String MARK_PREVIOUS_NEXT = "previous_next";
    public static final String MARK_BASE_URL = "base_url"; // FIXME
    public static final String PROPERTY_UNSUBSCRIBE_TRUE = "TRUE"; // FIXME
    public static final String PROPERTY_UNSUBSCRIBE_FALSE = "FALSE"; // FIXME
    public static final String PROPERTY_ABSOLUTE_URL_TRUE = "TRUE"; // FIXME

    public static final String MARK_SENDING = "sending";
    public static final String WEBAPP_PATH_FOR_LINKSERVICE = "@webapp_path_for_linkservice@";

    ///////////////////////////////////////////////////////////////////////
    // bookmark to use for mail template
    public static final String MARK_CONFIRM_URL = "confirm_url";
    public static final String PROPERTY_CONFIRM_MAIL_SENDER_NAME = AppPropertiesService.getProperty( "newsletter.confirm.senderName", "CONFIRMATION" );
    public static final String PROPERTY_CONFIRM_MAIL_SENDER_ADDRESS = AppPropertiesService.getProperty( "newsletter.confirm.sender", "noreply@paris.fr" );

    ///////////////////////////////////////////////////////////////////////
    // properties
    public static final String PROPERTY_PATH_FILE_NEWSLETTER_TEMPLATE = "newsletter.path.file.newsletter.template";
    public static final String PROPERTY_BASE_URL = "lutece.prod.url";
    public static final String PROPERTY_PORTAL_JSP_PATH = "lutece.portal.path";
    public static final String PROPERTY_MAIL_MULTIPART = "newsletter.mail.multipart";
    public static final String ALL_GROUPS = "all"; // FIXME
    public static final String PROPERTY_LABEL_ALL_GROUPS = "portal.workgroup.labelAllGroups"; // FIXME
    public static final String PROPERTY_CHECKBOX_ON = "on";

    /////////////////////////////////////////////////////////////////
    // parameters
    public static final String PARAMETER_EMAIL = "email";
    public static final String PARAMETER_SUBSCRIBER_SEARCH = "subscriber_search";
    public static final String PARAMETER_SENDING_ID = "sending_id";
    public static final String PARAMETER_ACTION = "action";
    public static final String PARAMETER_NEWSLETTER_ID = "newsletter_id";
    public static final String PARAMETER_PAGE = "page";
    public static final String PARAMETER_KEY = "key";
    public static final String PARAMETER_USER_ID = "user_id";

    // newsletter templates //////////////////////////////////////////
    public static final String PARAMETER_NEWSLETTER_TEMPLATE_NAME = "newsletter_template_name";
    public static final String PARAMETER_NEWSLETTER_TEMPLATE_FILE = "newsletter_template_file";
    public static final String PARAMETER_NEWSLETTER_TEMPLATE_PICTURE = "newsletter_template_picture";
    public static final String PARAMETER_NEWSLETTER_TEMPLATE_WORKGROUP = "newsletter_template_workgroup";
    public static final String PARAMETER_NEWSLETTER_TEMPLATE_NEW_FILE = "newsletter_template_new_file";
    public static final String PARAMETER_NEWSLETTER_TEMPLATE_NEW_PICTURE = "newsletter_template_new_picture";
    public static final String PARAMETER_NEWSLETTER_TEMPLATE_TYPE = "newsletter_template_type";
    public static final String PARAMETER_NEWSLETTER_TEMPLATE_ID = "newsletter_template_id";
    public static final String PARAMETER_NEWSLETTER_TEMPLATE_SOURCE = "newsletter_template_source";
    public static final String PARAMETER_NEWSLETTER_IMPORT_PATH = "newsletter_import_path";
    public static final String MARK_TEMPLATE = "template";

    // I18n properties
    public static final String PROPERTY_SUBSCRIPTION_OK_TITLE_MESSAGE = "newsletter.siteMessage.subscription_ok.title";
    public static final String PROPERTY_SUBSCRIPTION_OK_ALERT_MESSAGE = "newsletter.siteMessage.subscription_ok.message";
    public static final String PROPERTY_SUBSCRIPTION_OK_ALERT_MESSAGE_CONFIRM = "newsletter.siteMessage.subscription_ok.messageConfirm";
    public static final String PROPERTY_SUBSCRIPTION_CONFIRM_TITLE_MESSAGE = "newsletter.siteMessage.subscription_confirm.title";
    public static final String PROPERTY_SUBSCRIPTION_CONFIRM_ALERT_MESSAGE = "newsletter.siteMessage.subscription_confirm.message";
    public static final String PROPERTY_UNSUBSCRIPTION_OK_TITLE_MESSAGE = "newsletter.siteMessage.unsubscription_ok.title";
    public static final String PROPERTY_UNSUBSCRIPTION_OK_ALERT_MESSAGE = "newsletter.siteMessage.unsubscription_ok.message";
    public static final String PROPERTY_NO_NEWSLETTER_CHOSEN_TITLE_MESSAGE = "newsletter.siteMessage.no_newsletter_chosen.title";
    public static final String PROPERTY_NO_NEWSLETTER_CHOSEN_ERROR_MESSAGE = "newsletter.siteMessage.no_newsletter_chosen.message";
    public static final String PROPERTY_INVALID_MAIL_TITLE_MESSAGE = "newsletter.siteMessage.invalid_mail.title";
    public static final String PROPERTY_INVALID_MAIL_ERROR_MESSAGE = "newsletter.siteMessage.invalid_mail.message";
    public static final String PROPERTY_CONFIRM_UNSUBSCRIPTION_ALERT_MESSAGE = "newsletter.siteMessage.unsubscription.message";
    public static final String PROPERTY_CONFIRM_UNSUBSCRIPTION_TITLE_MESSAGE = "newsletter.siteMessage.unsubscription.title";
    public static final String PROPERTY_MSG_REGISTRATION_OK = "newsletter.message.alert.newsletter.registration_ok.text";
    public static final String PROPERTY_ACTION_ERROR = "newsletter.message.error.action";
    public static final String PROPERTY_NO_JCAPTCHA_TITLE_MESSAGE = "newsletter.siteMessage.jcaptcha.title";
    public static final String PROPERTY_NO_JCAPTCHA_MESSAGE = "newsletter.siteMessage.jcaptcha.message";
    public static final String PROPERTY_NO_TOS_TITLE_MESSAGE = "newsletter.siteMessage.tos.title";
    public static final String PROPERTY_NO_TOS_MESSAGE = "newsletter.siteMessage.tos.message";
    public static final String PROPERTY_SUBSCRIPTION_INVALID_USER_TITLE_MESSAGE = "newsletter.siteMessage.invalid_user.title";
    public static final String PROPERTY_SUBSCRIPTION_INVALID_USER_ERROR_MESSAGE = "newsletter.siteMessage.invalid_user.message";
    public static final String PROPERTY_SUBSCRIPTION_INVALID_KEY_TITLE_MESSAGE = "newsletter.siteMessage.invalid_key.title";
    public static final String PROPERTY_SUBSCRIPTION_INVALID_KEY_ERROR_MESSAGE = "newsletter.siteMessage.invalid_key.message";

    // Newsletter archive
    public static final String PROPERTY_PAGE_NEWSLETTER_ARCHIVE_TITLE = "newsletter.page_newsletter_archive.pageTitle";
    public static final String PROPERTY_PAGE_NEWSLETTER_ARCHIVE_LABEL = "newsletter.page_newsletter_archive.pageLabel";

    // Actions
    public static final String ACTION_REGISTER = "register";
    public static final String ACTION_UNSUBSCRIBE = "unsubscribe";
    public static final String ACTION_CONFIRM_UNSUBSCRIBE = "confirm_unsubscribe";
    public static final String ACTION_CONFIRM_SUBSCRIBE = "confirm_subscribe";
    public static final String ACTION_SHOW_ARCHIVE = "show_archive";

    // Jsp
    public static final String JSP_URL_PORTAL = "../../Portal.jsp";
    public static final String JSP_URL_DO_UNSUBSCRIBE = "/jsp/site/plugins/newsletter/DoUnsubscribeNewsLetter.jsp";
    public static final String MARK_CONTENT = "content";
    public static final String MARK_CONTENT_SECTION = "content_";
    public static final String MARK_UNSUBSCRIBE_KEY = "unsubscribe_key";
    public static final String MARK_UNSUBSCRIBE_KEY_EACH = "@unsubscribe_key@";

    /**
     * Private constructor
     */
    private NewsLetterConstants( )
    {
    }
}
