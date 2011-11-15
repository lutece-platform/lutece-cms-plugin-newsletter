/*
 * Copyright (c) 2002-2011, Mairie de Paris
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

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import fr.paris.lutece.plugins.document.business.Document;
import fr.paris.lutece.plugins.document.business.DocumentFilter;
import fr.paris.lutece.plugins.document.service.category.CategoryService;
import fr.paris.lutece.plugins.document.service.category.CategoryService.CategoryDisplay;
import fr.paris.lutece.plugins.document.service.publishing.PublishingService;
import fr.paris.lutece.plugins.newsletter.business.NewsLetter;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterHome;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterProperties;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterTemplate;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterTemplateHome;
import fr.paris.lutece.plugins.newsletter.business.NewsletterPropertiesHome;
import fr.paris.lutece.plugins.newsletter.business.SendingNewsLetter;
import fr.paris.lutece.plugins.newsletter.business.SendingNewsLetterHome;
import fr.paris.lutece.plugins.newsletter.business.Subscriber;
import fr.paris.lutece.plugins.newsletter.business.SubscriberHome;
import fr.paris.lutece.plugins.newsletter.service.NewsLetterRegistrationService;
import fr.paris.lutece.plugins.newsletter.service.NewsletterResourceIdService;
import fr.paris.lutece.plugins.newsletter.service.NewsletterService;
import fr.paris.lutece.plugins.newsletter.service.NewsletterTemplateResourceIdService;
import fr.paris.lutece.plugins.newsletter.util.NewsLetterConstants;
import fr.paris.lutece.plugins.newsletter.util.NewsletterUtils;
import fr.paris.lutece.portal.business.portlet.Portlet;
import fr.paris.lutece.portal.business.rbac.RBAC;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.business.workgroup.AdminWorkgroupHome;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.mail.MailService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.portlet.PortletService;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.service.workgroup.AdminWorkgroupService;
import fr.paris.lutece.portal.web.admin.PluginAdminPageJspBean;
import fr.paris.lutece.portal.web.constants.Messages;
import fr.paris.lutece.portal.web.constants.Parameters;
import fr.paris.lutece.portal.web.upload.MultipartHttpServletRequest;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.date.DateUtil;
import fr.paris.lutece.util.filesystem.UploadUtil;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.html.Paginator;
import fr.paris.lutece.util.mail.UrlAttachment;
import fr.paris.lutece.util.sort.AttributeComparator;
import fr.paris.lutece.util.string.StringUtil;
import fr.paris.lutece.util.url.UrlItem;

import org.apache.commons.fileupload.FileItem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * This class provides the user interface to manage NewsLetters features
 */
public class NewsletterJspBean extends PluginAdminPageJspBean
{
    /**
     * The right used for managing newsletters
     */
    public static final String RIGHT_NEWSLETTER_MANAGEMENT = "NEWSLETTER_MANAGEMENT";

    /**
     * The right used for managing newsletters properties
     */
    public static final String RIGHT_NEWSLETTER_PROPERTIES_MANAGEMENT = "NEWSLETTER_PROPERTIES_MANAGEMENT";

    /**
     * The right used for managing newsletter templates
     */
    public static final String RIGHT_NEWSLETTER_TEMPLATE_MANAGEMENT = "NEWSLETTER_TEMPLATE_MANAGEMENT";
    private static final String REGEX_ID = "^[\\d]+$";
    private static final String CONSTANT_CSV_FILE_EXTENSION = ".csv";
    private static final String CONSTANT_EMAIL_COLUMN_INDEX = ".csv.import.columnindex";
    private static final String PROPERTY_LIMIT_MAX_SUSCRIBER = "newsletter.limit.max";
    private static final String PROPERTY_LIMIT_MIN_SUSCRIBER = "newsletter.limit.min";
    private static final String PROPERTY_MAIL_HOST = "mail.server";
    private static final String PROPERTY_PATH_TEMPLATE = "path.templates";
    private static final String PROPERTY_PATH_IMAGE_NEWSLETTER_TEMPLATE = ".path.image.newsletter.template";
    private static final String PROPERTY_REGISTER_ACTION = ".compose_newsletter.buttonRegister";
    private static final String PROPERTY_PREPARE_SENDING_ACTION = ".compose_newsletter.buttonPrepareSending";
    private static final String PROPERTY_CANCEL_ACTION = ".compose_newsletter.buttonCancel";
    private static final String PROPERTY_TEST_SENDING_ACTION = ".compose_newsletter.buttonTestSending";
    private static final String PROPERTY_NO_CATEGORY = "no_category";
    private static final String PROPERTY_PROD_BASE_URL = "lutece.prod.url";
    private static final String PROPERTY_LIMIT_CONFIRM_DAYS = "newsletter.confirm.limit";
    
    //copy document's img
    private static final String PROPERTY_WEBAPP_PATH="newsletter.webapp.path";
    private static final String PROPERTY_WEBAPP_URL="newsletter.webapp.url";
    private static final String PROPERTY_NO_SECURED_IMG_FOLDER="newsletter.nosecured.img.folder.name";
    private static final String PROPERTY_NO_SECURED_IMG_OPTION="newsletter.nosecured.img.option";
    
    //Css inclusion
    private static final String PROPERTY_CSS_FILES = "newsletter.css.files";
    private static final String SEPARATOR_PROPERTY_CSS_FILES = ";";
    private static final String SEPARATOR_CSS_FILES_CONTENT = "\n";

    // Handling of CSV
    private static final String PROPERTY_IMPORT_DELIMITER = ".csv.import.delimiter";

    // templates
    private static final String TEMPLATE_MANAGE_NEWSLETTERS = "admin/plugins/newsletter/manage_newsletters.html";
    private static final String TEMPLATE_MANAGE_NEWSLETTERS_PROPERTIES = "admin/plugins/newsletter/manage_newsletters_properties.html";
    private static final String TEMPLATE_MODIFY_NEWSLETTER = "admin/plugins/newsletter/modify_newsletter.html";
    private static final String TEMPLATE_CREATE_NEWSLETTER = "admin/plugins/newsletter/create_newsletter.html";
    private static final String TEMPLATE_COMPOSE_NEWSLETTER = "admin/plugins/newsletter/compose_newsletter.html";
    private static final String TEMPLATE_PREPARE_NEWSLETTER = "admin/plugins/newsletter/prepare_newsletter.html";
    private static final String TEMPLATE_SEND_NEWSLETTER = "admin/plugins/newsletter/send_newsletter.html";
    private static final String TEMPLATE_MANAGE_NEWSLETTER_TEMPLATE = "admin/plugins/newsletter/manage_templates.html";
    private static final String TEMPLATE_CREATE_NEWSLETTER_TEMPLATE = "admin/plugins/newsletter/add_newsletter_template.html";
    private static final String TEMPLATE_MODIFY_NEWSLETTER_TEMPLATE = "admin/plugins/newsletter/modify_newsletter_template.html";
    private static final String TEMPLATE_MODIFY_NEWSLETTER_TEMPLATE_FILE = "admin/plugins/newsletter/modify_newsletter_template_file.html";
    private static final String TEMPLATE_MANAGE_SUBSCRIBERS = "admin/plugins/newsletter/manage_subscribers.html";
    private static final String TEMPLATE_IMPORT_SUBSCRIBERS = "admin/plugins/newsletter/import_subscribers.html";
    private static final String TEMPLATE_MANAGE_OLD_NEWSLETTERS = "admin/plugins/newsletter/manage_old_newsletters.html";

    //marks
    private static final String MARK_LIST_DOCUMENT_TEMPLATES = "document_templates";
    private static final String MARK_LIST_NEWSLETTER_TEMPLATES = "newsletter_templates";
    private static final String MARK_NEWSLETTER = "newsletter";
    private static final String MARK_NEWSLETTER_DESCRIPTION = "newsletter_description";
    private static final String MARK_DATE_LAST_SENDING = "date_last_sending";
    private static final String MARK_NEWSLETTER_ALLOW_CREATION = "newsletter_allow_creation";
    private static final String MARK_NEWSLETTER_ALLOW_MANAGE_ARCHIVE = "newsletter_allow_manage_archive";
    private static final String MARK_NEWSLETTER_ALLOW_DELETION = "newsletter_allow_deletion";
    private static final String MARK_NEWSLETTER_ALLOW_MODIFICATION = "newsletter_allow_modification";
    private static final String MARK_NEWSLETTER_ALLOW_MANAGE_SUBSCRIBERS = "newsletter_allow_manage_subscribers";
    private static final String MARK_NEWSLETTER_ALLOW_EXPORT_SUBSCRIBERS = "newsletter_allow_export_subscribers";
    private static final String MARK_NEWSLETTER_ALLOW_SENDING = "newsletter_allow_sending";
    private static final String MARK_NEWSLETTER_WORKGROUP_DESCRIPTION = "newsletter_workgroup_description";
    private static final String MARK_NEWSLETTER_COUNT_SUBSCRIBERS = "newsletter_count_subscribers";
    private static final String MARK_NEWSLETTER_TEMPLATE_ALLOW_CREATION = "newsletter_template_allow_creation";
    private static final String MARK_NEWSLETTER_TEMPLATE_ALLOW_DELETION = "newsletter_template_allow_deletion";
    private static final String MARK_NEWSLETTER_TEMPLATE_ALLOW_MODIFICATION = "newsletter_template_allow_modification";
    private static final String MARK_NEWSLETTER_TEMPLATE_WORKGROUP_DESCRIPTION = "newsletter_template_workgroup_description";
    private static final String MARK_NEWSLETTER_ID = "newsletter_id";
    private static final String MARK_CATEGORY_LIST = "category_list";
    private static final String MARK_NEWSLETTER_CONTENT = "newsletter_content";
    private static final String MARK_HTML_CONTENT = "html_content";
    private static final String MARK_WEBAPP_URL = "webapp_url";
    private static final String MARK_NEWSLETTER_TEMPLATE_ID = "newsletter_template_id";
    private static final String MARK_DOCUMENT_TEMPLATE_ID = "document_template_id";
    private static final String MARK_PREVIEW = "newsletter_preview";
    private static final String MARK_NEWSLETTER_OBJECT = "newsletter_object";
    private static final String MARK_REGISTER_ACTION = "register_action";
    private static final String MARK_PREPARE_SENDING_ACTION = "prepare_sending_action";
    private static final String MARK_CANCEL_ACTION = "cancel_action";
    private static final String MARK_DATE_LAST_SEND = "newsletter_last_sent";
    private static final String MARK_IMG_PATH = "img_path";
    private static final String MARK_LOCALE = "locale";
    private static final String MARK_PROPERTIES = "properties";
    private static final String MARK_NEWSLETTER_LIST = "newsletters_list";
    private static final String MARK_ALLOW_CREATION = "creation_allowed";
    private static final String MARK_PLUGIN = "plugin";
    private static final String MARK_SUBSCRIBERS_COUNT_LIST = "subscribers_count_list";
    private static final String MARK_SUBSCRIBERS_LIST = "subscribers_list";
    private static final String MARK_TEMPLATES_LIST = "template_list";
    private static final String MARK_WORKGROUP_LIST = "workgroup_list";
    private static final String MARK_PAGINATOR = "paginator";
    private static final String MARK_NB_ITEMS_PER_PAGE = "nb_items_per_page";
    private static final String MARK_CSS = "newsletter_css";
    private static final String MARK_UNSUBSCRIBE = "unsubscribe";
    private static final String MARK_UNSUBSCRIBE_LIST = "unsubscribe_list";
    private static final String MARK_DISPLAY_STATUS = "display_status";
    private static final String MARK_IS_ACTIVE_CAPTCHA = "is_active_captcha";
    private static final String MARK_ADD_SUBSCRIBER_RIGHT = "is_add_subscriber_right";
    private static final String MARK_IMPORT_SUBSCRIBER_RIGHT = "is_import_subscriber_right";
    private static final String MARK_CLEAN_RIGHT = "is_clean_subscriber_right";

    // PARAMETER
    private static final String PARAMETER_ACTION = "action";
    private static final String PARAMETER_NEWSLETTER_ID = "newsletter_id";
    private static final String PARAMETER_SENDING_NEWSLETTER_ID = "sending_newsletter_id";

    //private static final String PARAMETER_DOCUMENT_LIST_ID = "document_list_id";
    private static final String PARAMETER_CATEGORY_LIST_ID = "category_list_id";
    private static final String PARAMETER_NEWSLETTER_NAME = "newsletter_name";
    private static final String PARAMETER_NEWSLETTER_DESCRIPTION = "newsletter_description";
    private static final String PARAMETER_NEWSLETTER_NO_CATEGORY = "newsletter_no_category";
    private static final String PARAMETER_NEWSLETTER_SENDER_MAIL = "newsletter_sender_mail";
    private static final String PARAMETER_NEWSLETTER_SENDER_NAME = "newsletter_sender_name";
    private static final String PARAMETER_DATE_FIRST_SEND = "date_first_send";
    private static final String PARAMETER_DATE_LAST_SEND = "date_last_send";
    private static final String PARAMETER_SUBSCRIBER_ID = "subscriber_id";
    private static final String PARAMETER_DOCUMENT_TEMPLATE_ID = "document_template_id";
    private static final String PARAMETER_NEWSLETTER_OBJECT = "newsletter_object";
    private static final String PARAMETER_GENERATE = "generate";
    private static final String PARAMETER_HTML_CONTENT = "html_content";
    private static final String PARAMETER_SUBSCRIBERS_FILE = "newsletter_import_path";
    private static final String PARAMETER_TEMPLATE_PICTURE = "newsletter_template_picture";
    private static final String PARAMETER_TEMPLATE_FILE = "newsletter_template_file";
    private static final String PARAMETER_NEWSLETTER_WORKGROUP = "newsletter_workgroup";
    private static final String PARAMETER_TERM_OF_SERVICE = "tos";
    private static final String PARAMETER_NEWSLETTER_UNSUBSCRIBE = "newsletter_unsubscribe";
    private static final String PARAMETER_PAGE_INDEX = "page_index";
    private static final String PARAMETER_TEST_RECIPIENTS = "newsletter_test_recipients";
    private static final String PARAMETER_TEST_SUBJECT = "newsletter_test_subject";
    private static final String PARAMETER_ACTIVE_CAPTCHA = "active_captcha";
    private static final String PARAMETER_ACTIVE_VALIDATION = "active_validation";
    private static final String PARAMETER_SUBSCRIBERS_SELECTION = "subscriber_selection";

    // URL
    private static final String JSP_DO_REMOVE_NEWSLETTER_TEMPLATE = "jsp/admin/plugins/newsletter/DoRemoveNewsLetterTemplate.jsp";
    private static final String JSP_URL_DO_COMPOSE_NEWSLETTER = "ComposeNewsLetter.jsp";
    private static final String JSP_URL_DO_PREPARE_NEWSLETTER = "DoPrepareNewsLetter.jsp";
    private static final String JSP_URL_CONFIRM_TEST_NEWSLETTER = "ConfirmTestNewsLetter.jsp";
    private static final String JSP_URL_DO_REMOVE_NEWSLETTER = "jsp/admin/plugins/newsletter/DoRemoveNewsLetter.jsp";
    private static final String JSP_URL_DO_REMOVE_SENDING_NEWSLETTER = "jsp/admin/plugins/newsletter/DoRemoveSendingNewsLetter.jsp";
    private static final String JSP_URL_DO_REMOVE_SUBSCRIBER = "jsp/admin/plugins/newsletter/DoUnsubscribeNewsLetterAdmin.jsp";
    private static final String JSP_URL_DO_REMOVE_SELECTION = "jsp/admin/plugins/newsletter/DoRemoveSelection.jsp";
    private static final String JSP_URL_MANAGE_NEWSLETTER = "ManageNewsLetter.jsp";
    private static final String JSP_URL_MANAGE_NEWSLETTER_TEMPLATES = "ManageTemplates.jsp";
    private static final String JSP_URL_MANAGE_SUBSCRIBERS = "ManageSubscribers.jsp";
    private static final String JSP_URL_MANAGE_ARCHIVE = "ManageArchive.jsp";
    private static final String JSP_URL_PREPARE_NEWSLETTER = "PrepareNewsLetter.jsp";
    private static final String JSP_URL_SEND_NEWSLETTER = "jsp/admin/plugins/newsletter/DoSendNewsLetter.jsp";
    private static final String JSP_URL_TEST_NEWSLETTER = "jsp/admin/plugins/newsletter/DoTestNewsLetter.jsp";
    
    // messages
    private static final String MESSAGE_CONFIRM_TEST_NEWSLETTER = "newsletter.message.confirmTestNewsletter";
    private static final String MESSAGE_LINKED_TO_NEWSLETTER = "newsletter.message.linkedPortlet";
    private static final String MESSAGE_CONFIRM_CANCEL_COMPOSE = "newsletter.message.confirmCancelComposeNewsletter";
    private static final String MESSAGE_CONFIRM_REMOVE_NEWSLETTER = "newsletter.message.confirmRemoveNewsletter";
    private static final String MESSAGE_CONFIRM_REMOVE_SENDING_NEWSLETTER = "newsletter.message.confirmRemoveSendingNewsletter";
    private static final String MESSAGE_CONFIRM_REMOVE_NEWSLETTER_TEMPLATE = "newsletter.message.confirmRemoveNewsletterTemplate";
    private static final String MESSAGE_CONFIRM_REMOVE_SUBSCRIBER = "newsletter.message.confirmRemoveSubscriber";
    private static final String MESSAGE_CONFIRM_REMOVE_SELECTION = "newsletter.message.confirmRemoveSelection";
    private static final String MESSAGE_CONFIRM_SEND_NEWSLETTER = "newsletter.message.confirmSendNewsletter";
    private static final String MESSAGE_CSV_FILE_EMPTY_OR_NOT_VALID_EMAILS = "newsletter.message.csvFileEmptyOrNotValidEmails";
    private static final String MESSAGE_COLUMN_INDEX_NOT_EXIST = "newsletter.message.csvColumnIndexNotExist";
    private static final String MESSAGE_CSV_FILE_EXTENSION = "newsletter.message.csvFileExtension";
    private static final String MESSAGE_EMAIL_EXISTS = "newsletter.message.emailExists";
    private static final String MESSAGE_FIELD_EMAIL_VALID = "newsletter.message.fieldEmailValid";
    private static final String MESSAGE_NO_SUBSCRIBER = "newsletter.message.noSubscriber";
    private static final String MESSAGE_NO_SUBSCRIBER_EXPORT = "newsletter.message.noSubscriberExport";
    private static final String MESSAGE_WRONG_EMAIL = "newsletter.message.wrongEmail";
    private static final String MESSAGE_WRONG_EMAIL_SENDER = "newsletter.message.wrongEmailSender";
    private static final String MESSAGE_WRONG_DATE_LAST_SEND = "newsletter.message.wrongDateLastSend";
    private static final String MESSAGE_SENDING_EMPTY_NOT_ALLOWED = "newsletter.message.sendingEmptyNotAllowed";
    private static final String MESSAGE_USED_TEMPLATE = "newsletter.message.usedTemplate";
    private static final String MESSAGE_NO_TEMPLATE = "newsletter.message.noTemplate";
    private static final String MESSAGE_OBJECT_NOT_SPECIFIED = "newsletter.message.noObjectSpecified";
    private static final String PROPERTY_PAGE_TITLE_IMPORT = "newsletter.import_subscribers.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_NEWSLETTERS = "newsletter.manage_newsletters.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_ARCHIVE = "newsletter.manage_archive.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_NEWSLETTERS_PROPERTIES = "newsletter.manage_newsletters_properties.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE = "newsletter.create_newsletter.pageTitle";
    private static final String MESSAGE_FILE_ALREADY_EXISTS = "newsletter.message.fileAlreadyExists";
    private static final String MESSAGE_IMAGE_FILE_ALREADY_EXISTS = "newsletter.message.imageFileAlreadyExists";
    private static final String MESSAGE_SUBSCRIBERS_CLEANED = "newsletter.message.subscribersCleaned";
    
    //Uncategorized document labels
    private static final String PROPERTY_LABEL_COMPOSE_UNCATEGORIZED_DOCUMENTS = "newsletter.compose_newsletter.uncategorizedDocuments.label";
    private static final String PROPERTY_PAGE_TITLE_COMPOSE = "newsletter.compose_newsletter.pageTitle";
    private static final String PROPERTY_LABEL_MODIFY_UNCATEGORIZED_DOCUMENTS = "newsletter.modify_newsletter.uncategorizedDocuments.label";
    private static final String PROPERTY_PAGE_TITLE_MODIFY = "newsletter.modify_newsletter.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MANAGE_TEMPLATES = "newsletter.manage_templates.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_ADD_TEMPLATE = "newsletter.add_newsletter_template.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_TEMPLATE = "newsletter.modify_newsletter_template.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_TEMPLATE_FILE = "newsletter.modify_newsletter_template_file.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MANAGE_SUBSCRIBERS = "newsletter.manage_subscribers.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_PREPARE = "newsletter.prepare_newsletter.pageTitle";
    private static final String MESSAGE_FRAGMENT_NO_CHANGE = "newsletter.message.fragment_no_change";
    private static final String PROPERTY_USERS_PER_PAGE = "paginator.user.itemsPerPage";
    private static final String PROPERTY_NEWSLETTERS_PER_PAGE = "newsletter.newslettersPerPage";
    private static final String PROPERTY_LABEL_UNSUBSCRIBE_TRUE = "newsletter.unsubscribe.true";
    private static final String PROPERTY_LABEL_UNSUBSCRIBE_FALSE = "newsletter.unsubscribe.false";
    private static final String MARK_SEARCH_STRING = "search_string";
    private static final String MARK_VIRTUAL_HOSTS = "virtual_hosts";
    private static final String PROPERTY_VIRTUAL_HOST = "virtualHost.";
    private static final String PROPERTY_TEST_SUBJECT = "newsletter.test.subject";
    private static final String PROPERTY_ABSOLUTE_URL_MAIL = "newsletter.absolute.mail.url";
    private static final String SUFFIX_BASE_URL = ".baseUrl";

    //constants
    private static final String JCAPTCHA_PLUGIN = "jcaptcha";
    private int _nItemsPerPage;
    private int _nDefaultItemsPerPage;
    private String _strCurrentPageIndex;
    private String[] _multiSelectionValues;
    private static int DEFAULT_LIMIT = 7;
    private static final String FILE_TYPE="image";
    private static final String SLASH="/";
    

    /**
     * Creates a new NewsletterJspBean object.
     */
    public NewsletterJspBean(  )
    {
        _nDefaultItemsPerPage = AppPropertiesService.getPropertyInt( PROPERTY_NEWSLETTERS_PER_PAGE, 10 );
    }

    /**
     * Returns the list of newsletters
     *
     * @param request the HTTP request
     * @return the html code for display the newsletters list
     */
    public String getManageNewsLetters( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_NEWSLETTERS );
        _strCurrentPageIndex = Paginator.getPageIndex( request, Paginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );
        _nItemsPerPage = Paginator.getItemsPerPage( request, Paginator.PARAMETER_ITEMS_PER_PAGE, _nItemsPerPage,
                _nDefaultItemsPerPage );

        HashMap model = new HashMap(  );
        Collection<NewsLetter> listNewsletter = NewsLetterHome.findAll( getPlugin(  ) );
        listNewsletter = AdminWorkgroupService.getAuthorizedCollection( listNewsletter, getUser(  ) );

        Collection<Map<String, Object>> listNewsletterDisplay = new ArrayList<Map<String, Object>>(  );

        for ( NewsLetter newsletter : listNewsletter )
        {
            Map<String, Object> newsletterDisplay = new HashMap<String, Object>(  );
            newsletterDisplay.put( MARK_NEWSLETTER, newsletter );
            newsletterDisplay.put( MARK_NEWSLETTER_ALLOW_CREATION,
                RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_CREATE, getUser(  ) ) );

            newsletterDisplay.put( MARK_NEWSLETTER_ALLOW_MANAGE_ARCHIVE,
                RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_ARCHIVE, getUser(  ) ) );

            newsletterDisplay.put( MARK_NEWSLETTER_ALLOW_DELETION,
                RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_DELETE, getUser(  ) ) );
            newsletterDisplay.put( MARK_NEWSLETTER_ALLOW_MANAGE_SUBSCRIBERS,
                RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MANAGE_SUBSCRIBERS,
                    getUser(  ) ) );
            newsletterDisplay.put( MARK_NEWSLETTER_ALLOW_EXPORT_SUBSCRIBERS,
                RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_EXPORT_SUBSCRIBERS,
                    getUser(  ) ) );
            newsletterDisplay.put( MARK_NEWSLETTER_ALLOW_MODIFICATION,
                RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MODIFY, getUser(  ) ) );
            newsletterDisplay.put( MARK_NEWSLETTER_ALLOW_SENDING,
                RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_SEND, getUser(  ) ) );

            //The workgroup description is needed for coherence and not the key
            if ( newsletter.getWorkgroup(  ).equals( NewsLetterConstants.ALL_GROUPS ) )
            {
                newsletterDisplay.put( MARK_NEWSLETTER_WORKGROUP_DESCRIPTION,
                    I18nService.getLocalizedString( NewsLetterConstants.PROPERTY_LABEL_ALL_GROUPS, getLocale(  ) ) );
            }
            else
            {
                newsletterDisplay.put( MARK_NEWSLETTER_WORKGROUP_DESCRIPTION,
                    AdminWorkgroupHome.findByPrimaryKey( newsletter.getWorkgroup(  ) ).getDescription(  ) );
            }

            newsletterDisplay.put( MARK_NEWSLETTER_COUNT_SUBSCRIBERS,
                NewsLetterHome.findNbrSubscribers( newsletter.getId(  ), getPlugin(  ) ) );
            listNewsletterDisplay.add( newsletterDisplay );
        }

        Paginator paginator = new Paginator( (List<Map<String, Object>>) listNewsletterDisplay, _nItemsPerPage,
                getHomeUrl( request ), PARAMETER_PAGE_INDEX, _strCurrentPageIndex );

        model.put( MARK_NEWSLETTER_LIST, paginator.getPageItems(  ) );
        model.put( MARK_PAGINATOR, paginator );
        model.put( MARK_ALLOW_CREATION, isNewsletterCreationAllowed( request ) );
        model.put( MARK_NB_ITEMS_PER_PAGE, "" + _nItemsPerPage );

        // Collection refListAllTemplates = NewsLetterTemplateHome.getTemplatesList( getPlugin(  ) );
        HtmlTemplate templateList = AppTemplateService.getTemplate( TEMPLATE_MANAGE_NEWSLETTERS, getLocale(  ), model );

        return getAdminPage( templateList.getHtml(  ) );
    }

    /**
     * Returns the list of old newsletters
     *
     * @param request the HTTP request
     * @return the html code for display the newsletters list
     */
    public String getManageArchive( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_ARCHIVE );
        _strCurrentPageIndex = Paginator.getPageIndex( request, Paginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );
        _nItemsPerPage = Paginator.getItemsPerPage( request, Paginator.PARAMETER_ITEMS_PER_PAGE, _nItemsPerPage,
                _nDefaultItemsPerPage );

        int nIdNewsletter = Integer.parseInt( request.getParameter( PARAMETER_NEWSLETTER_ID ) );
        HashMap model = new HashMap(  );
        List<SendingNewsLetter> listNewsletter = SendingNewsLetterHome.findAllLastSendingForNewsletterId( nIdNewsletter,
                getPlugin(  ) );

        UrlItem url = new UrlItem( request.getRequestURI(  ) );
        url.addParameter( PARAMETER_NEWSLETTER_ID, nIdNewsletter );

        Paginator paginator = new Paginator( listNewsletter, _nItemsPerPage, url.getUrl(  ), PARAMETER_PAGE_INDEX,
                _strCurrentPageIndex );

        model.put( MARK_NEWSLETTER_LIST, paginator.getPageItems(  ) );
        model.put( MARK_PAGINATOR, paginator );
        model.put( MARK_NEWSLETTER_ID, nIdNewsletter );
        model.put( MARK_NB_ITEMS_PER_PAGE, "" + _nItemsPerPage );

        HtmlTemplate templateList = AppTemplateService.getTemplate( TEMPLATE_MANAGE_OLD_NEWSLETTERS, getLocale(  ),
                model );

        return getAdminPage( templateList.getHtml(  ) );
    }

    /**
     * Returns the  newsletters properties
     *
     * @param request the HTTP request
     * @return the html code for display the newsletters list
     */
    public String getManageNewsLettersProperties( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_NEWSLETTERS_PROPERTIES );

        HashMap model = new HashMap(  );

        NewsLetterProperties properties = NewsletterPropertiesHome.find( getPlugin(  ) );

        model.put( MARK_IS_ACTIVE_CAPTCHA, PluginService.isPluginEnable( JCAPTCHA_PLUGIN ) );
        model.put( MARK_WEBAPP_URL, AppPathService.getBaseUrl( request ) );
        model.put( MARK_LOCALE, getLocale(  ).getLanguage(  ) );
        model.put( MARK_PROPERTIES, properties );
        model.put( MARK_CLEAN_RIGHT, RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, null,
                    NewsletterResourceIdService.PERMISSION_CLEAN_SUBSCRIBERS, getUser(  ) ));

        HtmlTemplate templateList = AppTemplateService.getTemplate( TEMPLATE_MANAGE_NEWSLETTERS_PROPERTIES,
                getLocale(  ), model );

        return getAdminPage( templateList.getHtml(  ) );
    }

    /**
     * Processes the update form of the newsletter properties
     *
     * @param request
     *            The Http request
     * @return The jsp URL which displays the view of all newsletter
     */
    public String doManageNewsLetterProperties( HttpServletRequest request )
    {
        String strBaseUrl = AppPathService.getBaseUrl( request );
        String strActiveCaptcha = request.getParameter( PARAMETER_ACTIVE_CAPTCHA );
        String strActiveValidation = request.getParameter( PARAMETER_ACTIVE_VALIDATION );

        NewsLetterProperties properties = new NewsLetterProperties(  );

        properties.setTOS( doClean( request.getParameter( PARAMETER_TERM_OF_SERVICE ), strBaseUrl ) );

        if ( strActiveCaptcha != null )
        {
            properties.setCaptchaActive( true );
        }
        else
        {
            properties.setCaptchaActive( false );
        }

        if ( strActiveValidation != null )
        {
            properties.setValidationActive( true );
        }
        else
        {
            properties.setValidationActive( false );
        }

        NewsletterPropertiesHome.update( properties, getPlugin(  ) );

        UrlItem url = new UrlItem( JSP_URL_MANAGE_NEWSLETTER );

        return url.getUrl(  );
    }

    /**
     * Returns the newsletter form for creation
     *
     * @param request The Http request
     * @return the html code of the newsletter form
     */
    public String getCreateNewsLetter( HttpServletRequest request )
    {
        //RBAC permission
        if ( !isNewsletterCreationAllowed( request ) )
        {
            return getManageNewsLetters( request );
        }

        setPageTitleProperty( PROPERTY_PAGE_TITLE_CREATE );

        AdminUser user = getUser(  );
        HashMap model = new HashMap(  );

        // get the list of document lists
        ReferenceList listUnsubscribe = new ReferenceList(  );
        listUnsubscribe.addItem( NewsLetterConstants.PROPERTY_UNSUBSCRIBE_TRUE,
            I18nService.getLocalizedString( PROPERTY_LABEL_UNSUBSCRIBE_TRUE, getLocale(  ) ) );
        listUnsubscribe.addItem( NewsLetterConstants.PROPERTY_UNSUBSCRIBE_FALSE,
            I18nService.getLocalizedString( PROPERTY_LABEL_UNSUBSCRIBE_FALSE, getLocale(  ) ) );

        Collection<CategoryDisplay> listCategoriesDisplay = new ArrayList<CategoryDisplay>(  );
        listCategoriesDisplay = CategoryService.getAllCategoriesDisplay( user );

        String strBaseUrl = getProdUrl( request );

        model.put( MARK_CATEGORY_LIST, listCategoriesDisplay );
        model.put( MARK_DATE_LAST_SEND, DateUtil.getCurrentDateString( getLocale(  ) ) );
        model.put( MARK_WORKGROUP_LIST, AdminWorkgroupService.getUserWorkgroups( getUser(  ), getLocale(  ) ) );
        model.put( MARK_UNSUBSCRIBE_LIST, listUnsubscribe );
        model.put( MARK_LOCALE, getLocale(  ) );
        model.put( MARK_WEBAPP_URL, strBaseUrl );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_CREATE_NEWSLETTER, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Returns the newsletter form of newsletter composition
     *
     * @param request The Http rquest
     * @return the html code of the newsletter composition form
     */
    public String doComposeNewsLetter( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsLetterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsLetterId, getPlugin(  ) );

        //Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser(  ) ) ||
                !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                    NewsletterResourceIdService.PERMISSION_MODIFY, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        Collection newsletterTemplatesList = NewsLetterTemplateHome.getTemplatesCollectionByType( NewsLetterTemplate.CONSTANT_ID_NEWSLETTER,
                getPlugin(  ) );

        Collection documentTemplatesList = NewsLetterTemplateHome.getTemplatesCollectionByType( NewsLetterTemplate.CONSTANT_ID_DOCUMENT,
                getPlugin(  ) );

        // composition not possible if not at least one template for newsletters
        // and one for documents
        if ( ( newsletterTemplatesList.size(  ) == 0 ) || ( documentTemplatesList.size(  ) == 0 ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_NO_TEMPLATE, AdminMessage.TYPE_STOP );
        }

        UrlItem url = new UrlItem( JSP_URL_DO_COMPOSE_NEWSLETTER );
        url.addParameter( PARAMETER_NEWSLETTER_ID, strNewsletterId );

        return url.getUrl(  );
    }

    /**
     * Returns the newsletter form of newsletter composition
     *
     * @param request The Http rquest
     * @return the html code of the newsletter composition form
     */
    public String getComposeNewsLetter( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsLetterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsLetterId, getPlugin(  ) );
        AdminUser user = getUser(  );

        // RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser(  ) ) ||
                !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                    NewsletterResourceIdService.PERMISSION_MODIFY, getUser(  ) ) )
        {
            return getManageNewsLetters( request );
        }

        setPageTitleProperty( PROPERTY_PAGE_TITLE_COMPOSE );

        Collection<NewsLetterTemplate> newsletterTemplatesList = NewsLetterTemplateHome.getTemplatesCollectionByType( NewsLetterTemplate.CONSTANT_ID_NEWSLETTER,
                getPlugin(  ) );
        newsletterTemplatesList = (ArrayList<NewsLetterTemplate>) AdminWorkgroupService.getAuthorizedCollection( newsletterTemplatesList,
                getUser(  ) );

        Collection<NewsLetterTemplate> documentTemplatesList = NewsLetterTemplateHome.getTemplatesCollectionByType( NewsLetterTemplate.CONSTANT_ID_DOCUMENT,
                getPlugin(  ) );
        documentTemplatesList = (ArrayList<NewsLetterTemplate>) AdminWorkgroupService.getAuthorizedCollection( documentTemplatesList,
                getUser(  ) );

        String strBaseUrl = getProdUrl( request );
        String strPathImageTemplate = AppPathService.getBaseUrl( request ) +
            AppPropertiesService.getProperty( getPlugin(  ).getName(  ) + PROPERTY_PATH_IMAGE_NEWSLETTER_TEMPLATE );

        HashMap model = new HashMap(  );

        // Fills the template with specific values
        String strGenerate = request.getParameter( PARAMETER_GENERATE );

        int nTemplateNewsLetterId = 0;
        int nTemplateDocumentId = 0;
        String strHtmlContent;

        if ( ( strGenerate == null ) )
        {
            nTemplateNewsLetterId = newsletter.getNewsLetterTemplateId(  );

            if ( nTemplateNewsLetterId == 0 )
            {
                nTemplateNewsLetterId = ( newsletterTemplatesList.size(  ) > 0 )
                    ? newsletterTemplatesList.iterator(  ).next(  ).getId(  ) : 0;
            }

            nTemplateDocumentId = newsletter.getDocumentTemplateId(  );

            if ( nTemplateDocumentId == 0 )
            {
                nTemplateDocumentId = ( documentTemplatesList.size(  ) > 0 )
                    ? documentTemplatesList.iterator(  ).next(  ).getId(  ) : 0;
            }

            strHtmlContent = ( newsletter.getHtml(  ) == null ) ? NewsLetterConstants.CONSTANT_EMPTY_STRING
                                                                : newsletter.getHtml(  );
        }
        else
        {
            String strNewsletterTemplateId = request.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_ID );
            String strDocumentTemplateId = request.getParameter( PARAMETER_DOCUMENT_TEMPLATE_ID );

            if ( ( strNewsletterTemplateId != null ) && ( strNewsletterTemplateId.matches( REGEX_ID ) ) )
            {
                nTemplateNewsLetterId = Integer.parseInt( strNewsletterTemplateId );
            }

            if ( ( strDocumentTemplateId != null ) && strDocumentTemplateId.matches( REGEX_ID ) )
            {
                nTemplateDocumentId = Integer.parseInt( strDocumentTemplateId );
            }

            strHtmlContent = generateNewsletterHtmlCode( nNewsLetterId, nTemplateNewsLetterId, nTemplateDocumentId,
                    strBaseUrl, user );

            if ( strHtmlContent == null )
            {
                strHtmlContent = NewsLetterConstants.CONSTANT_EMPTY_STRING; //if no template available (newsletter and/or document), return an empty html content
            }
        }

        strHtmlContent = strHtmlContent.replaceAll( NewsLetterConstants.MARK_BASE_URL, strBaseUrl );
        if ( strBaseUrl.endsWith( NewsLetterConstants.CONSTANT_SLASH ) )
        {
            strBaseUrl = strBaseUrl.substring( 0, strBaseUrl.length(  ) - 1 );
        }
        strHtmlContent = strHtmlContent.replaceAll( NewsLetterConstants.WEBAPP_PATH_FOR_LINKSERVICE, strBaseUrl );

        model.put( MARK_HTML_CONTENT, strHtmlContent );

        // get the list of document lists associated to the newsletter
        int[] arrayCategoryListIds = NewsLetterHome.findNewsletterCategoryIds( nNewsLetterId, getPlugin(  ) );

        // get the list of all document lists
        ReferenceList listCategoryList = NewsLetterHome.getAllCategories( getUser(  ) );
        listCategoryList.addItem( NewsLetterConstants.PROPERTY_UNCATEGORIZED_DOCUMENTS_KEY,
            I18nService.getLocalizedString( PROPERTY_LABEL_COMPOSE_UNCATEGORIZED_DOCUMENTS, getLocale(  ) ) );

        String[] strSelectedCategoryList = new String[arrayCategoryListIds.length];

        for ( int i = 0; i < arrayCategoryListIds.length; i++ )
        {
            strSelectedCategoryList[i] = String.valueOf( arrayCategoryListIds[i] );
        }

        listCategoryList.checkItems( strSelectedCategoryList );
        model.put( MARK_CATEGORY_LIST, listCategoryList );
        model.put( MARK_LIST_NEWSLETTER_TEMPLATES, newsletterTemplatesList );
        model.put( MARK_LIST_DOCUMENT_TEMPLATES, documentTemplatesList );
        model.put( MARK_NEWSLETTER, newsletter );
        model.put( MARK_NEWSLETTER_TEMPLATE_ID, nTemplateNewsLetterId );
        model.put( MARK_DOCUMENT_TEMPLATE_ID, nTemplateDocumentId );
        model.put( MARK_REGISTER_ACTION,
            AppPropertiesService.getProperty( getPlugin(  ).getName(  ) + PROPERTY_REGISTER_ACTION ) );
        model.put( MARK_PREPARE_SENDING_ACTION,
            AppPropertiesService.getProperty( getPlugin(  ).getName(  ) + PROPERTY_PREPARE_SENDING_ACTION ) );
        model.put( MARK_CANCEL_ACTION,
            AppPropertiesService.getProperty( getPlugin(  ).getName(  ) + PROPERTY_CANCEL_ACTION ) );

        model.put( MARK_WEBAPP_URL, AppPathService.getBaseUrl( request ) );
        model.put( MARK_LOCALE, getLocale(  ).getLanguage(  ) );
        model.put( MARK_IMG_PATH, strPathImageTemplate );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_COMPOSE_NEWSLETTER, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }
    
    /**
     * Processes subscribers cleaning
     *
     * @param request
     *            The Http request
     * @return The jsp URL 
     */
    public String doCleanSubscribers( HttpServletRequest request )
    {
    	//RBAC permissions, the user must have the right "clean subscribers" on all newsletters
        if ( !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, null,
                    NewsletterResourceIdService.PERMISSION_CLEAN_SUBSCRIBERS, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }
        
    	NewsLetterRegistrationService.getInstance(  ).doRemoveOldUnconfirmed(  );    	
    	int nConfirmLimit = AppPropertiesService.getPropertyInt( PROPERTY_LIMIT_CONFIRM_DAYS, DEFAULT_LIMIT );
    	Object[] messages = new String[1];
    	messages[0] = Integer.toString( nConfirmLimit );
    	return AdminMessageService.getMessageUrl( request, MESSAGE_SUBSCRIBERS_CLEANED, messages, AdminMessage.TYPE_INFO );
    }

    /**
     * Processes the creation form of a new newsletter
     *
     * @param request
     *            The Http request
     * @return The jsp URL which displays the view of the created newsletter
     */
    public String doCreateNewsLetter( HttpServletRequest request )
    {
        // retrieve name and date
        String strNewsletterName = request.getParameter( PARAMETER_NEWSLETTER_NAME );
        String strNewsletterDescription = request.getParameter( PARAMETER_NEWSLETTER_DESCRIPTION );
        String strDateFirstSend = request.getParameter( PARAMETER_DATE_FIRST_SEND );
        String strWorkGroup = request.getParameter( PARAMETER_NEWSLETTER_WORKGROUP );
        String[] strCategoryIds = request.getParameterValues( PARAMETER_CATEGORY_LIST_ID );
        String strSenderName = request.getParameter( PARAMETER_NEWSLETTER_SENDER_NAME );
        String strSenderMail = request.getParameter( PARAMETER_NEWSLETTER_SENDER_MAIL );
        String strTestRecipients = request.getParameter( PARAMETER_TEST_RECIPIENTS );
        String strTestSubject = request.getParameter( PARAMETER_TEST_SUBJECT );
        String strNoCategory = request.getParameter( PARAMETER_NEWSLETTER_NO_CATEGORY );

        //RBAC permission
        if ( !isNewsletterCreationAllowed( request ) )
        {
            return getManageNewsLetters( request );
        }

        // Mandatory fields
        if ( ( strSenderMail == null ) || strSenderMail.equals( "" ) || ( strTestRecipients == null ) ||
                strTestRecipients.equals( "" ) || ( strNewsletterName == null ) || strNewsletterName.equals( "" ) ||
                ( strDateFirstSend == null ) || strDateFirstSend.equals( "" ) ||
                ( ( strCategoryIds == null ) && ( strNoCategory == null ) ) ||
                ( ( strCategoryIds != null ) && strCategoryIds.equals( "" ) ) ||
                ( ( strNoCategory != null ) && strNoCategory.equals( "" ) ) || ( strWorkGroup == null ) ||
                strWorkGroup.equals( "" ) || ( strSenderName == null ) || strSenderName.equals( "" ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
        }

        if ( !isWrongEmail( strTestRecipients ).equals( "" ) )
        {
            Object[] messageArgs = { isWrongEmail( strTestRecipients ) };

            return AdminMessageService.getMessageUrl( request, MESSAGE_WRONG_EMAIL, messageArgs, AdminMessage.TYPE_STOP );
        }

        if ( !StringUtil.checkEmail( strSenderMail ) )
        {
            Object[] messageArgs = { strSenderMail };

            return AdminMessageService.getMessageUrl( request, MESSAGE_WRONG_EMAIL_SENDER, messageArgs,
                AdminMessage.TYPE_STOP );
        }

        NewsLetter newsletter = new NewsLetter(  );
        newsletter.setName( strNewsletterName );

        Timestamp dateFirstSend = DateUtil.formatTimestamp( strDateFirstSend, getLocale(  ) );

        if ( dateFirstSend != null )
        {
            newsletter.setDateLastSending( dateFirstSend );
        }
        else
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_WRONG_DATE_LAST_SEND, AdminMessage.TYPE_STOP );
        }

        newsletter.setDescription( strNewsletterDescription );
        newsletter.setWorkgroup( strWorkGroup );
        newsletter.setTestRecipients( strTestRecipients );
        newsletter.setTestSubject( strTestSubject );
        newsletter.setNewsletterSenderMail( strSenderMail );
        newsletter.setNewsletterSenderName( strSenderName );
        newsletter.setUnsubscribe( request.getParameter( PARAMETER_NEWSLETTER_UNSUBSCRIBE ) );

        NewsLetterHome.create( newsletter, getPlugin(  ) );

        if ( ( strNoCategory == null ) || !strNoCategory.equals( PROPERTY_NO_CATEGORY ) )
        {
            // Associate categories to a newsletter
            for ( int i = 0; i < strCategoryIds.length; i++ )
            {
                int nCategoryId = Integer.parseInt( strCategoryIds[i] );
                NewsLetterHome.associateNewsLetterDocumentList( newsletter.getId(  ), nCategoryId, getPlugin(  ) );
            }
        }
        else
        {
            NewsLetterHome.associateNewsLetterDocumentList( newsletter.getId(  ), -1, getPlugin(  ) );
        }

        return getHomeUrl( request );
    }

    /**
     * Returns the newsletter form for modification
     *
     * @param request    The Http request
     * @return the html code of the newsletter form
     */
    public String getModifyNewsLetter( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_MODIFY );

        Map model = new HashMap(  );
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin(  ) );

        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser(  ) ) ||
                !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                    NewsletterResourceIdService.PERMISSION_MODIFY, getUser(  ) ) )
        {
            return getManageNewsLetters( request );
        }

        ReferenceList listUnsubscribe = new ReferenceList(  );
        listUnsubscribe.addItem( "TRUE",
            I18nService.getLocalizedString( PROPERTY_LABEL_UNSUBSCRIBE_TRUE, getLocale(  ) ) );
        listUnsubscribe.addItem( "FALSE",
            I18nService.getLocalizedString( PROPERTY_LABEL_UNSUBSCRIBE_FALSE, getLocale(  ) ) );

        String strBaseUrl = getProdUrl( request );

        model.put( MARK_NEWSLETTER, newsletter );
        model.put( MARK_NEWSLETTER_DESCRIPTION, newsletter.getDescription(  ) );
        model.put( MARK_DATE_LAST_SENDING, DateUtil.getDateString( newsletter.getDateLastSending(  ), getLocale(  ) ) );
        model.put( MARK_WORKGROUP_LIST, AdminWorkgroupService.getUserWorkgroups( getUser(  ), getLocale(  ) ) );
        model.put( MARK_UNSUBSCRIBE_LIST, listUnsubscribe );
        model.put( MARK_LOCALE, getLocale(  ) );
        model.put( MARK_WEBAPP_URL, strBaseUrl );

        // get the list of categories associated to the newsletter
        int[] arrayCategoryListIds = NewsLetterHome.findNewsletterCategoryIds( nNewsletterId, getPlugin(  ) );

        // get the list of all document lists
        ReferenceList listCategoryList = NewsLetterHome.getAllCategories( getUser(  ) );
        listCategoryList.addItem( NewsLetterConstants.PROPERTY_UNCATEGORIZED_DOCUMENTS_KEY,
            I18nService.getLocalizedString( PROPERTY_LABEL_MODIFY_UNCATEGORIZED_DOCUMENTS, getLocale(  ) ) );

        String[] strSelectedCategoryList = new String[arrayCategoryListIds.length];

        for ( int i = 0; i < arrayCategoryListIds.length; i++ )
        {
            strSelectedCategoryList[i] = String.valueOf( arrayCategoryListIds[i] );
        }

        listCategoryList.checkItems( strSelectedCategoryList );
        model.put( MARK_CATEGORY_LIST, listCategoryList );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MODIFY_NEWSLETTER, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Processes the update form of the newsletter whose identifier is in the
     * http request
     *
     * @param request
     *            The Http request
     * @return The jsp URL which displays the view of the updated newsletter
     */
    public String doModifyNewsLetter( HttpServletRequest request )
    {
        // retrieve the required parameters
        String strSenderName = request.getParameter( PARAMETER_NEWSLETTER_SENDER_NAME );
        String strSenderMail = request.getParameter( PARAMETER_NEWSLETTER_SENDER_MAIL );
        String strNewsletterName = request.getParameter( PARAMETER_NEWSLETTER_NAME );
        String strNewsletterDescription = request.getParameter( PARAMETER_NEWSLETTER_DESCRIPTION );
        String strWorkGroup = request.getParameter( PARAMETER_NEWSLETTER_WORKGROUP );
        String strDateLastSend = request.getParameter( PARAMETER_DATE_LAST_SEND );
        String strTestRecipients = request.getParameter( PARAMETER_TEST_RECIPIENTS );
        String[] strCategoryIds = request.getParameterValues( PARAMETER_CATEGORY_LIST_ID );
        String strNoCategory = request.getParameter( PARAMETER_NEWSLETTER_NO_CATEGORY );
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin(  ) );
        String strTestSubject = request.getParameter( PARAMETER_TEST_SUBJECT );

        //RBAC permission
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser(  ) ) ||
                !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                    NewsletterResourceIdService.PERMISSION_MODIFY, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        // Mandatory fields
        if ( ( strSenderMail == null ) || strSenderMail.equals( "" ) || ( strTestRecipients == null ) ||
                strTestRecipients.equals( "" ) || ( strNewsletterName == null ) || strNewsletterName.equals( "" ) ||
                ( ( strCategoryIds == null ) && ( strNoCategory == null ) ) ||
                ( ( strCategoryIds != null ) && strCategoryIds.equals( "" ) ) ||
                ( ( strNoCategory != null ) && strNoCategory.equals( "" ) ) || ( strDateLastSend == null ) ||
                strDateLastSend.equals( "" ) || ( strWorkGroup == null ) || strWorkGroup.equals( "" ) ||
                ( strSenderName == null ) || strSenderName.equals( "" ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
        }

        strTestRecipients = cleanEmails( strTestRecipients );

        if ( !isWrongEmail( strTestRecipients ).equals( "" ) )
        {
            Object[] messageArgs = { isWrongEmail( strTestRecipients ) };

            return AdminMessageService.getMessageUrl( request, MESSAGE_WRONG_EMAIL, messageArgs, AdminMessage.TYPE_STOP );
        }

        if ( !StringUtil.checkEmail( strSenderMail ) )
        {
            Object[] messageArgs = { strSenderMail };

            return AdminMessageService.getMessageUrl( request, MESSAGE_WRONG_EMAIL_SENDER, messageArgs,
                AdminMessage.TYPE_STOP );
        }

        newsletter.setName( strNewsletterName );
        newsletter.setDescription( strNewsletterDescription );
        newsletter.setWorkgroup( strWorkGroup );
        newsletter.setTestRecipients( strTestRecipients );
        newsletter.setTestSubject( strTestSubject );
        newsletter.setNewsletterSenderMail( strSenderMail );
        newsletter.setNewsletterSenderName( strSenderName );

        Timestamp dateLastSend = DateUtil.formatTimestamp( strDateLastSend, getLocale(  ) );

        if ( dateLastSend != null )
        {
            newsletter.setDateLastSending( dateLastSend );
        }

        newsletter.setUnsubscribe( request.getParameter( PARAMETER_NEWSLETTER_UNSUBSCRIBE ) );
        newsletter.setWorkgroup( request.getParameter( PARAMETER_NEWSLETTER_WORKGROUP ) );

        // if not, newsletter.getDateLastSending keeps its value
        NewsLetterHome.update( newsletter, getPlugin(  ) );
        NewsLetterHome.removeNewsLetterDocumentList( nNewsletterId, getPlugin(  ) );

        if ( ( strNoCategory == null ) || !strNoCategory.equals( PROPERTY_NO_CATEGORY ) )
        {
            // recreate the category list with the new selection
            for ( int i = 0; i < strCategoryIds.length; i++ )
            {
                int nCategoryId = Integer.parseInt( strCategoryIds[i] );
                NewsLetterHome.associateNewsLetterDocumentList( nNewsletterId, nCategoryId, getPlugin(  ) );
            }
        }

        String strId = Integer.toString( nNewsletterId );
        UrlItem url = new UrlItem( JSP_URL_MANAGE_NEWSLETTER );
        url.addParameter( PARAMETER_NEWSLETTER_ID, strId );

        return url.getUrl(  );
    }

    /**
     * Remove spaces from a recipient list
     *
     * @param strRecipientLists  The recipient list
     * @return the recipient list without spaces before and after each email.
     */
    private String cleanEmails( String strRecipientLists )
    {
        StringBuffer strCleanTestRecipients = new StringBuffer(  );
        String strDelimiter = AppPropertiesService.getProperty( getPlugin(  ).getName(  ) + PROPERTY_IMPORT_DELIMITER );

        String[] strEmails = strRecipientLists.split( strDelimiter );

        for ( String email : strEmails )
        {
            strCleanTestRecipients.append( email.trim(  ) );
            strCleanTestRecipients.append( strDelimiter );
        }

        return strCleanTestRecipients.toString(  );
    }

    /**
    * Manages the removal form of a newsletter whose identifier is in the http
    * request
    *
    * @param request  The Http request
    * @return the html code to confirm
    */
    public String getConfirmRemoveSubscriber( HttpServletRequest request )
    {
        UrlItem urlItem = new UrlItem( JSP_URL_DO_REMOVE_SUBSCRIBER );
        int nNewsletterId = Integer.parseInt( request.getParameter( PARAMETER_NEWSLETTER_ID ) );
        int nSubscriberId = Integer.parseInt( request.getParameter( PARAMETER_SUBSCRIBER_ID ) );
        urlItem.addParameter( PARAMETER_NEWSLETTER_ID, nNewsletterId );
        urlItem.addParameter( PARAMETER_SUBSCRIBER_ID, nSubscriberId );

        return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_SUBSCRIBER, urlItem.getUrl(  ),
            AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Manages the removal form of a newsletter whose identifier is in the http for selected users
     * request
     *
     * @param request  The Http request
     * @return the html code to confirm
     */
    public String getConfirmRemoveSelectedSubscribers( HttpServletRequest request )
    {
        String[] strIdSubscribers = (String[]) request.getParameterMap(  ).get( PARAMETER_SUBSCRIBERS_SELECTION );
        _multiSelectionValues = strIdSubscribers;

        UrlItem urlItem = new UrlItem( JSP_URL_DO_REMOVE_SELECTION );
        int nNewsletterId = Integer.parseInt( request.getParameter( PARAMETER_NEWSLETTER_ID ) );
        urlItem.addParameter( PARAMETER_NEWSLETTER_ID, nNewsletterId );

        return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_SELECTION, urlItem.getUrl(  ),
            AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Processes the unregistration of a subscriber for a newsletter
     *
     * @param request The Http request
     * @return the jsp URL to display the form to manage newsletters
     */
    public String doUnregistrationAdmin( HttpServletRequest request )
    {
        /* parameters */
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        int nSubscriberId = Integer.parseInt( request.getParameter( PARAMETER_SUBSCRIBER_ID ) );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin(  ) );

        //RBAC permission
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser(  ) ) ||
                !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                    NewsletterResourceIdService.PERMISSION_MANAGE_SUBSCRIBERS, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        Subscriber subscriber = SubscriberHome.findByPrimaryKey( nSubscriberId, getPlugin(  ) );

        if ( subscriber != null )
        {
            removeSubscriberFromNewsletter( subscriber, nNewsletterId, getPlugin(  ) );
        }

        UrlItem urlItem = new UrlItem( JSP_URL_MANAGE_SUBSCRIBERS );
        urlItem.addParameter( NewsLetterConstants.PARAMETER_PLUGIN_NAME, NewsLetterConstants.PROPERTY_PLUGIN_NAME );
        urlItem.addParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_ID, nNewsletterId );

        return urlItem.getUrl(  );
    }

    /**
     * Processes the unregistration of selected subscribers
     *
     * @param request The Http request
     * @return the jsp URL to display the form to manage newsletters
     */
    public String doRemoveSelection( HttpServletRequest request )
    {
        /* parameters */
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin(  ) );

        //RBAC permission
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser(  ) ) ||
                !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                    NewsletterResourceIdService.PERMISSION_MANAGE_SUBSCRIBERS, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        for ( String strId : _multiSelectionValues )
        {
            Subscriber subscriber = SubscriberHome.findByPrimaryKey( Integer.parseInt( strId ), getPlugin(  ) );

            if ( subscriber != null )
            {
                removeSubscriberFromNewsletter( subscriber, nNewsletterId, getPlugin(  ) );
            }
        }

        UrlItem urlItem = new UrlItem( JSP_URL_MANAGE_SUBSCRIBERS );
        urlItem.addParameter( NewsLetterConstants.PARAMETER_PLUGIN_NAME, NewsLetterConstants.PROPERTY_PLUGIN_NAME );
        urlItem.addParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_ID, nNewsletterId );

        return urlItem.getUrl(  );
    }

    /**
     * Manages the removal form of a newsletter whose identifier is in the http
     * request
     *
     * @param request
     *            The Http request
     * @return the html code to confirm
     */
    public String getRemoveNewsLetter( HttpServletRequest request )
    {
        /* parameters */
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin(  ) );

        //RBAC permission
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser(  ) ) ||
                !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                    NewsletterResourceIdService.PERMISSION_DELETE, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        if ( NewsLetterHome.checkLinkedPortlets( nNewsletterId ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_LINKED_TO_NEWSLETTER, AdminMessage.TYPE_STOP );
        }

        UrlItem urlItem = new UrlItem( JSP_URL_DO_REMOVE_NEWSLETTER );
        urlItem.addParameter( PARAMETER_NEWSLETTER_ID, nNewsletterId );

        return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_NEWSLETTER, urlItem.getUrl(  ),
            AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Manages the removal form of a newsletter archive whose identifier is in the http
     * request
     *
     * @param request
     *            The Http request
     * @return the html code to confirm
     */
    public String getRemoveSendingNewsLetter( HttpServletRequest request )
    {
        /* parameters */
        String strSendingNewsletterId = request.getParameter( PARAMETER_SENDING_NEWSLETTER_ID );
        int nSendingNewsletterId = Integer.parseInt( strSendingNewsletterId );
        SendingNewsLetter archive = SendingNewsLetterHome.findByPrimaryKey( nSendingNewsletterId, getPlugin(  ) );

        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );

        //RBAC permission
        if ( !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, Integer.toString( nNewsletterId ),
                    NewsletterResourceIdService.PERMISSION_ARCHIVE, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        UrlItem urlItem = new UrlItem( JSP_URL_DO_REMOVE_SENDING_NEWSLETTER );
        urlItem.addParameter( PARAMETER_NEWSLETTER_ID, nNewsletterId );
        urlItem.addParameter( PARAMETER_SENDING_NEWSLETTER_ID, nSendingNewsletterId );

        return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_SENDING_NEWSLETTER,
            urlItem.getUrl(  ), AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Manages the removal form of a newsletter template whose identifier is in
     * the http request
     *
     * @param request The Http request
     * @return the html code to confirm
     */
    public String getRemoveNewsLetterTemplate( HttpServletRequest request )
    {
        int nNewsletterTemplateId = Integer.parseInt( request.getParameter( 
                    NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_ID ) );
        NewsLetterTemplate newsletterTemplate = NewsLetterTemplateHome.findByPrimaryKey( nNewsletterTemplateId,
                getPlugin(  ) );

        //Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletterTemplate, getUser(  ) ) ||
                !RBACService.isAuthorized( NewsLetterTemplate.RESOURCE_TYPE,
                    Integer.toString( newsletterTemplate.getId(  ) ),
                    NewsletterTemplateResourceIdService.PERMISSION_DELETE, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        if ( NewsLetterHome.findTemplate( nNewsletterTemplateId, getPlugin(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_USED_TEMPLATE, AdminMessage.TYPE_STOP );
        }
        else
        {
            UrlItem url = new UrlItem( JSP_DO_REMOVE_NEWSLETTER_TEMPLATE );
            url.addParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_ID,
                Integer.parseInt( request.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_ID ) ) );

            return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_NEWSLETTER_TEMPLATE,
                url.getUrl(  ), AdminMessage.TYPE_CONFIRMATION );
        }
    }

    /**
     * Processes the removal form of a newsletter
     *
     * @param request The Http request
     * @return the jsp URL to display the form to manage newsletters
     */
    public String doRemoveNewsLetter( HttpServletRequest request )
    {
        int nNewsletterId = Integer.parseInt( request.getParameter( PARAMETER_NEWSLETTER_ID ) );

        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin(  ) );

        //Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser(  ) ) ||
                !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, Integer.toString( newsletter.getId(  ) ),
                    NewsletterResourceIdService.PERMISSION_MODIFY, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        /* Looks for the list of the subscribers */
        Collection<Subscriber> list = SubscriberHome.findSubscribers( nNewsletterId,
                NewsLetterConstants.CONSTANT_EMPTY_STRING,
                Integer.parseInt( AppPropertiesService.getProperty( PROPERTY_LIMIT_MIN_SUSCRIBER ) ),
                Integer.parseInt( AppPropertiesService.getProperty( PROPERTY_LIMIT_MAX_SUSCRIBER ) ), getPlugin(  ) );

        for ( Subscriber subscriber : list )
        {
            NewsLetterHome.removeSubscriber( newsletter.getId(  ), subscriber.getId(  ), getPlugin(  ) );
        }

        // removes relationship between the newsletter and document list
        NewsLetterHome.removeNewsLetterDocumentList( nNewsletterId, getPlugin(  ) );

        // removes the newsletter
        NewsLetterHome.remove( nNewsletterId, getPlugin(  ) );

        return getHomeUrl( request );
    }

    /**
     * Processes the removal form of a sending newsletter
     *
     * @param request The Http request
     * @return the jsp URL to display the form to manage newsletters
     */
    public String doRemoveSendingNewsLetter( HttpServletRequest request )
    {
        int nNewsletterId = Integer.parseInt( request.getParameter( PARAMETER_NEWSLETTER_ID ) );
        int nSendingNewsletterId = Integer.parseInt( request.getParameter( PARAMETER_SENDING_NEWSLETTER_ID ) );

        if ( !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, Integer.toString( nNewsletterId ),
                    NewsletterResourceIdService.PERMISSION_ARCHIVE, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        SendingNewsLetterHome.remove( nSendingNewsletterId, getPlugin(  ) );

        UrlItem url = new UrlItem( JSP_URL_MANAGE_ARCHIVE );
        url.addParameter( PARAMETER_NEWSLETTER_ID, nNewsletterId );

        return url.getUrl(  );
    }

    /**
     * Processes the removal form of a newsletter template
     *
     * @param request  The Http request
     * @return the jsp URL to display the form to manage newsletter templates
     */
    public String doRemoveNewsLetterTemplate( HttpServletRequest request )
    {
        int nNewsletterTemplateId = Integer.parseInt( request.getParameter( 
                    NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_ID ) );

        NewsLetterTemplate newsLetterTemplate = NewsLetterTemplateHome.findByPrimaryKey( nNewsletterTemplateId,
                getPlugin(  ) );

        //Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsLetterTemplate, getUser(  ) ) ||
                !RBACService.isAuthorized( NewsLetterTemplate.RESOURCE_TYPE,
                    Integer.toString( newsLetterTemplate.getId(  ) ),
                    NewsletterTemplateResourceIdService.PERMISSION_DELETE, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        String strFileName = newsLetterTemplate.getFileName(  );
        String strPictureName = newsLetterTemplate.getPicture(  );

        // removes the file
        String strPathFileNewsletterTemplate = AppPathService.getPath( PROPERTY_PATH_TEMPLATE ) +
            AppPropertiesService.getProperty( getPlugin(  ).getName(  ) +
                NewsLetterConstants.PROPERTY_PATH_FILE_NEWSLETTER_TEMPLATE );
        File file = new File( strPathFileNewsletterTemplate + "/" + strFileName );

        if ( file.exists(  ) )
        {
            file.delete(  );
        }

        // removes the picture
        String strPathImageNewsletterTemplate = AppPathService.getPath( getPlugin(  ).getName(  ) +
                PROPERTY_PATH_IMAGE_NEWSLETTER_TEMPLATE );
        File picture = new File( strPathImageNewsletterTemplate + "/" + strPictureName );

        if ( picture.exists(  ) )
        {
            picture.delete(  );
        }

        // removes the newsletter template from the database
        NewsLetterTemplateHome.remove( nNewsletterTemplateId, getPlugin(  ) );

        // loads the newsletter templates management page
        // If the operation occurred well returns on the info of the newsletter
        UrlItem url = new UrlItem( JSP_URL_MANAGE_NEWSLETTER_TEMPLATES );

        return url.getUrl(  );
    }

    /**
     * Builds the newsletter's templates management page
     *
     * @param request The HTTP request
     * @return the html code for newsletter's templates management page (liste
     *         of templates + available actions)
     */
    public String getManageTemplates( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_MANAGE_TEMPLATES );

        HashMap model = new HashMap(  );

        Collection<NewsLetterTemplate> refListAllTemplates = NewsLetterTemplateHome.getTemplatesList( getPlugin(  ) );
        refListAllTemplates = (ArrayList<NewsLetterTemplate>) AdminWorkgroupService.getAuthorizedCollection( refListAllTemplates,
                getUser(  ) );

        Collection<Map<String, Object>> listNewsletterTemplateDisplay = new ArrayList<Map<String, Object>>(  );

        for ( NewsLetterTemplate newsletterTemplate : refListAllTemplates )
        {
            Map<String, Object> newsletterTemplateDisplay = new HashMap<String, Object>(  );
            newsletterTemplateDisplay.put( NewsLetterConstants.MARK_TEMPLATE, newsletterTemplate );
            newsletterTemplateDisplay.put( MARK_NEWSLETTER_TEMPLATE_ALLOW_CREATION,
                RBACService.isAuthorized( newsletterTemplate, NewsletterTemplateResourceIdService.PERMISSION_CREATE,
                    getUser(  ) ) );

            newsletterTemplateDisplay.put( MARK_NEWSLETTER_TEMPLATE_ALLOW_DELETION,
                RBACService.isAuthorized( newsletterTemplate, NewsletterTemplateResourceIdService.PERMISSION_DELETE,
                    getUser(  ) ) );
            newsletterTemplateDisplay.put( MARK_NEWSLETTER_TEMPLATE_ALLOW_MODIFICATION,
                RBACService.isAuthorized( newsletterTemplate, NewsletterTemplateResourceIdService.PERMISSION_MODIFY,
                    getUser(  ) ) );

            //The workgroup description is needed for coherence and not the key
            if ( newsletterTemplate.getWorkgroup(  ).equals( NewsLetterConstants.ALL_GROUPS ) )
            {
                newsletterTemplateDisplay.put( MARK_NEWSLETTER_TEMPLATE_WORKGROUP_DESCRIPTION,
                    I18nService.getLocalizedString( NewsLetterConstants.PROPERTY_LABEL_ALL_GROUPS, getLocale(  ) ) );
            }
            else
            {
                newsletterTemplateDisplay.put( MARK_NEWSLETTER_TEMPLATE_WORKGROUP_DESCRIPTION,
                    AdminWorkgroupHome.findByPrimaryKey( newsletterTemplate.getWorkgroup(  ) ).getDescription(  ) );
            }

            listNewsletterTemplateDisplay.add( newsletterTemplateDisplay );
        }

        model.put( MARK_TEMPLATES_LIST, listNewsletterTemplateDisplay );
        model.put( MARK_ALLOW_CREATION, isNewsletterTemplateCreationAllowed( request ) );
        model.put( MARK_PLUGIN, getPlugin(  ) );

        // get the list of all templates
        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MANAGE_NEWSLETTER_TEMPLATE, getLocale(  ),
                model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Builds the newsletter's templates creation page
     *
     * @param request  The HTTP request
     * @return the html code for newsletter's templates creation page
     */
    public String getAddNewsLetterTemplate( HttpServletRequest request )
    {
        if ( !isNewsletterTemplateCreationAllowed( request ) )
        {
            return getManageTemplates( request );
        }

        setPageTitleProperty( PROPERTY_PAGE_TITLE_ADD_TEMPLATE );

        // get the list of template types
        // nothing should be checked
        String strDefaultCheckedType = "";
        Map model = new HashMap(  );
        model.put( NewsLetterConstants.MARK_TEMPLATE_TYPE, buildTemplateTypeList( strDefaultCheckedType ) );
        model.put( MARK_WORKGROUP_LIST, AdminWorkgroupService.getUserWorkgroups( getUser(  ), getLocale(  ) ) );
        model.put( MARK_PLUGIN, getPlugin(  ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_CREATE_NEWSLETTER_TEMPLATE, getLocale(  ),
                model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Builds the newsletter's templates modification page
     *
     * @param request The HTTP request
     * @return the html code for newsletter's templates creation page
     */
    public String getModifyNewsLetterTemplate( HttpServletRequest request )
    {
        String strIdTemplate = request.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_ID );
        int nIdTemplate = Integer.parseInt( strIdTemplate );
        NewsLetterTemplate newsletterTemplate = NewsLetterTemplateHome.findByPrimaryKey( nIdTemplate, getPlugin(  ) );

        //Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletterTemplate, getUser(  ) ) ||
                !RBACService.isAuthorized( NewsLetterTemplate.RESOURCE_TYPE,
                    Integer.toString( newsletterTemplate.getId(  ) ),
                    NewsletterTemplateResourceIdService.PERMISSION_MODIFY, getUser(  ) ) )
        {
            return getManageTemplates( request );
        }

        setPageTitleProperty( PROPERTY_PAGE_TITLE_MODIFY_TEMPLATE );

        // get the list of template types
        String strDefaultCheckedType = newsletterTemplate.getType(  ) + "";
        Map model = new HashMap(  );
        model.put( NewsLetterConstants.MARK_TEMPLATE_TYPE, buildTemplateTypeList( strDefaultCheckedType ) );
        model.put( MARK_WORKGROUP_LIST, AdminWorkgroupService.getUserWorkgroups( getUser(  ), getLocale(  ) ) );
        model.put( NewsLetterConstants.MARK_TEMPLATE, newsletterTemplate );
        model.put( MARK_PLUGIN, getPlugin(  ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MODIFY_NEWSLETTER_TEMPLATE, getLocale(  ),
                model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Builds the newsletter's templates modification page (with the
     * modification of the file content)
     *
     * @param request  The HTTP request
     * @return the html code for newsletter's templates creation page
     */
    public String getModifyNewsLetterTemplateFile( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_MODIFY_TEMPLATE_FILE );

        Map model = new HashMap(  );

        try
        {
            int nIdTemplate = Integer.parseInt( request.getParameter( 
                        NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_ID ) );
            NewsLetterTemplate newsletterTemplate = NewsLetterTemplateHome.findByPrimaryKey( nIdTemplate, getPlugin(  ) );

            //Workgroup & RBAC permissions
            if ( !AdminWorkgroupService.isAuthorized( newsletterTemplate, getUser(  ) ) ||
                    !RBACService.isAuthorized( NewsLetterTemplate.RESOURCE_TYPE,
                        Integer.toString( newsletterTemplate.getId(  ) ),
                        NewsletterTemplateResourceIdService.PERMISSION_MODIFY, getUser(  ) ) )
            {
                return getManageTemplates( request );
            }

            // get the file content
            String strPathFileNewsletterTemplate = AppPathService.getPath( PROPERTY_PATH_TEMPLATE ) +
                AppPropertiesService.getProperty( getPlugin(  ).getName(  ) +
                    NewsLetterConstants.PROPERTY_PATH_FILE_NEWSLETTER_TEMPLATE );

            String strFileName = newsletterTemplate.getFileName(  );
            BufferedReader fileReader = new BufferedReader( new FileReader( strPathFileNewsletterTemplate +
                        File.separator + strFileName ) );

            String strSource = "";
            String line = fileReader.readLine(  );

            while ( line != null )
            {
                strSource += ( line + "\n" );
                line = fileReader.readLine(  );
            }

            fileReader.close(  );

            String strDefaultCheckedType = newsletterTemplate.getType(  ) + "";

            model.put( NewsLetterConstants.MARK_TEMPLATE_TYPE, buildTemplateTypeList( strDefaultCheckedType ) );

            model.put( NewsLetterConstants.MARK_TEMPLATE_SOURCE, strSource );
            model.put( NewsLetterConstants.MARK_TEMPLATE_FILE_NAME, strFileName );
            model.put( NewsLetterConstants.MARK_TEMPLATE, newsletterTemplate );
            model.put( MARK_PLUGIN, getPlugin(  ) );
        }
        catch ( FileNotFoundException f )
        {
            AppLogService.debug( f );
        }
        catch ( IOException i )
        {
            AppLogService.debug( i );
        }

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MODIFY_NEWSLETTER_TEMPLATE_FILE,
                getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Build a radio buttons list of template types from properties
     * @param strCheckedType the element to be selected
     * @return the html code for the radio buttons list
     */
    private static ReferenceList buildTemplateTypeList( String strCheckedType )
    {
        ReferenceList refTemplateTypeList = new ReferenceList(  );
        refTemplateTypeList.addItem( NewsLetterTemplate.CONSTANT_ID_NEWSLETTER,
            NewsLetterTemplate.TEMPLATE_NAMES[NewsLetterTemplate.CONSTANT_ID_NEWSLETTER] );
        refTemplateTypeList.addItem( NewsLetterTemplate.CONSTANT_ID_DOCUMENT,
            NewsLetterTemplate.TEMPLATE_NAMES[NewsLetterTemplate.CONSTANT_ID_DOCUMENT] );
        refTemplateTypeList.checkItems( new String[] { strCheckedType } );

        return refTemplateTypeList;
    }

    /**
     * Builds the newsletter's subscribers management page
     *
     * @param request The HTTP request
     * @return the html code for newsletter's subscribers management page
     */
    public String getManageSubscribers( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsLetterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsLetterId, getPlugin(  ) );
        NewsLetterProperties properties = NewsletterPropertiesHome.find( getPlugin(  ) );

        //Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser(  ) ) ||
                !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                    NewsletterResourceIdService.PERMISSION_MANAGE_SUBSCRIBERS, getUser(  ) ) )
        {
            return getManageNewsLetters( request );
        }

        setPageTitleProperty( PROPERTY_PAGE_TITLE_MANAGE_SUBSCRIBERS );

        HashMap model = new HashMap(  );
       

        model.put( MARK_NEWSLETTER, newsletter );

        String strSearchString = request.getParameter( NewsLetterConstants.PARAMETER_SUBSCRIBER_SEARCH );

        if ( strSearchString == null )
        {
            strSearchString = NewsLetterConstants.CONSTANT_EMPTY_STRING;
        }

        _strCurrentPageIndex = Paginator.getPageIndex( request, Paginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );
        _nDefaultItemsPerPage = AppPropertiesService.getPropertyInt( PROPERTY_USERS_PER_PAGE, 10 );
        _nItemsPerPage = Paginator.getItemsPerPage( request, Paginator.PARAMETER_ITEMS_PER_PAGE, _nItemsPerPage,
                _nDefaultItemsPerPage );

        // get a list of subscribers
        List<Subscriber> refListSubscribers = (List<Subscriber>) SubscriberHome.findSubscribers( nNewsLetterId, strSearchString,
                Integer.parseInt( AppPropertiesService.getProperty( PROPERTY_LIMIT_MIN_SUSCRIBER ) ),
                Integer.parseInt( AppPropertiesService.getProperty( PROPERTY_LIMIT_MAX_SUSCRIBER ) ), getPlugin(  ) );
        UrlItem url = new UrlItem( request.getRequestURI(  ) );
        url.addParameter( PARAMETER_NEWSLETTER_ID, nNewsLetterId );
        url.addParameter( NewsLetterConstants.PARAMETER_SUBSCRIBER_SEARCH, strSearchString );
        
        String strSortedAttributeName = request.getParameter( Parameters.SORTED_ATTRIBUTE_NAME );
        String strAscSort = null;

        if ( strSortedAttributeName != null )
        {
            strAscSort = request.getParameter( Parameters.SORTED_ASC );

            boolean bIsAscSort = Boolean.parseBoolean( strAscSort );

            Collections.sort( refListSubscribers, new AttributeComparator( strSortedAttributeName, bIsAscSort ) );
        }

        Paginator paginator = new Paginator( refListSubscribers, _nItemsPerPage, url.getUrl(  ),
                Paginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );
        model.put( MARK_NB_ITEMS_PER_PAGE, "" + _nItemsPerPage );
        model.put( MARK_SEARCH_STRING, strSearchString );
        model.put( MARK_PAGINATOR, paginator );
        model.put( MARK_SUBSCRIBERS_LIST, paginator.getPageItems(  ) );
        model.put( MARK_DISPLAY_STATUS, properties.isValidationActive(  ) );
        model.put( MARK_ADD_SUBSCRIBER_RIGHT, RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                NewsletterResourceIdService.PERMISSION_ADD_SUBSCRIBER, getUser(  ) ) );
        model.put( MARK_IMPORT_SUBSCRIBER_RIGHT, RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                NewsletterResourceIdService.PERMISSION_IMPORT_SUBSCRIBERS, getUser(  ) ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MANAGE_SUBSCRIBERS, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Processes the registration of a subscriber
     *
     * @param request The Http request
     * @return The jsp URL which displays the subscribers management page
     */
    public String doAddSubscriber( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsLetterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsLetterId, getPlugin(  ) );

        //Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser(  ) ) ||
                !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                    NewsletterResourceIdService.PERMISSION_ADD_SUBSCRIBER, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        String strEmail = request.getParameter( NewsLetterConstants.PARAMETER_EMAIL );

        // Mandatory fields
        if ( ( strEmail == null ) || strEmail.equals( NewsLetterConstants.CONSTANT_EMPTY_STRING ) ||
                !StringUtil.checkEmail( strEmail.trim(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_FIELD_EMAIL_VALID, AdminMessage.TYPE_STOP );
        }

        // Checks if a subscriber with the same email address doesn't exist yet
        Subscriber subscriber = SubscriberHome.findByEmail( strEmail, getPlugin(  ) );

        if ( subscriber == null )
        {
            // The email doesn't exist, so create a new subcriber
            subscriber = new Subscriber(  );
            subscriber.setEmail( strEmail.trim(  ) );
            SubscriberHome.create( subscriber, getPlugin(  ) );
        }

        // adds a subscriber to the current newsletter
        if ( NewsLetterHome.findRegistration( nNewsLetterId, subscriber.getId(  ), getPlugin(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_EMAIL_EXISTS, AdminMessage.TYPE_STOP );
        }

        // the current date
        Timestamp tToday = new java.sql.Timestamp( new java.util.Date(  ).getTime(  ) );
        NewsLetterHome.addSubscriber( newsletter.getId(  ), subscriber.getId(  ), tToday, getPlugin(  ) );

        // Returns the jsp URL to display the subscribers management page with
        // the new one
        UrlItem urlItem = new UrlItem( JSP_URL_MANAGE_SUBSCRIBERS );
        urlItem.addParameter( PARAMETER_NEWSLETTER_ID, nNewsLetterId );

        return urlItem.getUrl(  );
    }

    /**
     * Processes the creation form of a new newsletter template by recovering
     * the parameters in the http request
     *
     * @param request the http request
     * @return The Jsp URL of the process result
     */
    public String doCreateNewsletterTemplate( HttpServletRequest request )
    {
        if ( !isNewsletterTemplateCreationAllowed( request ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        NewsLetterTemplate newsletterTemplate = new NewsLetterTemplate(  );

        try
        {
            // initialize the paths
            String strPathImageNewsletterTemplate = AppPathService.getPath( getPlugin(  ).getName(  ) +
                    PROPERTY_PATH_IMAGE_NEWSLETTER_TEMPLATE );
            String strPathFileNewsletterTemplate = AppPathService.getPath( PROPERTY_PATH_TEMPLATE ) +
                AppPropertiesService.getProperty( getPlugin(  ).getName(  ) +
                    NewsLetterConstants.PROPERTY_PATH_FILE_NEWSLETTER_TEMPLATE );

            // create the multipart request
            MultipartHttpServletRequest multi = (MultipartHttpServletRequest) request;

            // Mandatory fields
            String strType = multi.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_TYPE );
            String strDescription = multi.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_NAME );
            String strWorkgroup = multi.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_WORKGROUP );

            if ( ( strWorkgroup == null ) || ( strWorkgroup.equals( "" ) ) )
            {
                return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
            }

            if ( ( strType == null ) || ( strType.equals( "" ) ) )
            {
                return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
            }

            if ( ( strDescription == null ) || ( strDescription.equals( "" ) ) )
            {
                return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
            }

            FileItem imageItem = multi.getFile( PARAMETER_TEMPLATE_PICTURE );
            String strImageFileName = UploadUtil.cleanFileName( imageItem.getName(  ) );

            if ( ( imageItem == null ) || strImageFileName.equals( "" ) )
            {
                return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
            }

            //create the directory if it doesn't exist
            if ( !new File( strPathImageNewsletterTemplate ).exists(  ) )
            {
                File fDirectory = new File( strPathImageNewsletterTemplate );
                fDirectory.mkdir(  );
            }

            File fileImage = new File( strPathImageNewsletterTemplate + File.separator + strImageFileName );

            if ( fileImage.exists(  ) )
            {
                return AdminMessageService.getMessageUrl( request, MESSAGE_IMAGE_FILE_ALREADY_EXISTS,
                    AdminMessage.TYPE_STOP );
            }

            FileItem modelItem = multi.getFile( PARAMETER_TEMPLATE_FILE );
            String strTemplateFileName = UploadUtil.cleanFileName( modelItem.getName(  ) );

            if ( ( modelItem == null ) || "".equals( strTemplateFileName ) )
            {
                return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
            }

            File fileTemplate = new File( strPathFileNewsletterTemplate + File.separator + strTemplateFileName );

            if ( fileTemplate.exists(  ) )
            {
                return AdminMessageService.getMessageUrl( request, MESSAGE_FILE_ALREADY_EXISTS, AdminMessage.TYPE_STOP );
            }

            //if files are ok, save them
            imageItem.write( fileImage );
            newsletterTemplate.setPicture( strImageFileName );

            modelItem.write( fileTemplate );
            newsletterTemplate.setFileName( strTemplateFileName );

            // Complete the newsLetterTemplate
            newsletterTemplate.setDescription( strDescription );
            newsletterTemplate.setType( Integer.parseInt( strType ) );
            newsletterTemplate.setWorkgroup( strWorkgroup );
            NewsLetterTemplateHome.create( newsletterTemplate, getPlugin(  ) );
        }
        catch ( Exception e )
        {
            AppLogService.error( e.getMessage(  ), e );
        }

        return getHomeUrl( request );
    }

    /**
     * Processes the modification form of a newsletter template modified by hand
     * by recovering the parameters in the http request
     *
     * @param request  the http request
     * @return The Jsp URL of the process result
     */
    public String doModifyNewsletterTemplateFile( HttpServletRequest request )
    {
        try
        {
            // initialize the paths
            String strPathImageNewsletterTemplate = AppPathService.getPath( getPlugin(  ).getName(  ) +
                    PROPERTY_PATH_IMAGE_NEWSLETTER_TEMPLATE );
            String strPathFileNewsletterTemplate = AppPathService.getPath( PROPERTY_PATH_TEMPLATE ) +
                AppPropertiesService.getProperty( getPlugin(  ).getName(  ) +
                    NewsLetterConstants.PROPERTY_PATH_FILE_NEWSLETTER_TEMPLATE );

            // create the multipart request
            MultipartHttpServletRequest multi = (MultipartHttpServletRequest) request;

            // creation of the NewsLetterTemplate
            NewsLetterTemplate newsletterTemplate = NewsLetterTemplateHome.findByPrimaryKey( Integer.parseInt( 
                        multi.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_ID ) ), getPlugin(  ) );

            //Workgroup & RBAC permissions
            if ( !AdminWorkgroupService.isAuthorized( newsletterTemplate, getUser(  ) ) ||
                    !RBACService.isAuthorized( NewsLetterTemplate.RESOURCE_TYPE,
                        Integer.toString( newsletterTemplate.getId(  ) ),
                        NewsletterTemplateResourceIdService.PERMISSION_MODIFY, getUser(  ) ) )
            {
                return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
            }

            // Mandatory fields
            String strType = multi.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_TYPE );
            String strDescription = multi.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_NAME );

            if ( strDescription.equals( "" ) )
            {
                return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
            }

            // Names of the old files
            String strOldFileName = newsletterTemplate.getFileName(  );
            String strOldImageName = newsletterTemplate.getPicture(  );

            FileItem imageItem = multi.getFile( "newsletter_template_new_picture" );

            if ( ( imageItem != null ) && ( imageItem.getSize(  ) != 0 ) )
            {
                String strFileName = UploadUtil.cleanFileName( imageItem.getName(  ) );
                imageItem.write( new File( strPathImageNewsletterTemplate + File.separator + strFileName ) );
                newsletterTemplate.setPicture( strFileName );

                // we delete the old picture
                File oldImageFile = new File( strPathImageNewsletterTemplate + File.separator + strOldImageName );
                oldImageFile.delete(  );
            }

            // Writes the new content of the file.
            String fileContent = multi.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_SOURCE );

            FileWriter fileWriter = new FileWriter( strPathFileNewsletterTemplate + File.separator + strOldFileName );
            fileWriter.write( fileContent );
            fileWriter.close(  );

            // Complete the newsLetterTemplate
            newsletterTemplate.setDescription( strDescription );
            newsletterTemplate.setType( Integer.parseInt( strType ) );
            NewsLetterTemplateHome.update( newsletterTemplate, getPlugin(  ) );
        }
        catch ( IOException io )
        {
            AppLogService.error( io.getMessage(  ), io );
        }
        catch ( Exception e )
        {
            AppLogService.error( e.getMessage(  ), e );
        }

        return getHomeUrl( request );
    }

    /**
     * Processes the modification form of a newsletter template by recovering
     * the parameters in the http request
     *
     * @param request the http request
     * @return The Jsp URL of the process result
     */
    public String doModifyNewsletterTemplate( HttpServletRequest request )
    {
        try
        {
            // initialize the paths
            String strPathImageNewsletterTemplate = AppPathService.getPath( getPlugin(  ).getName(  ) +
                    PROPERTY_PATH_IMAGE_NEWSLETTER_TEMPLATE );
            String strPathFileNewsletterTemplate = AppPathService.getPath( PROPERTY_PATH_TEMPLATE ) +
                AppPropertiesService.getProperty( getPlugin(  ).getName(  ) +
                    NewsLetterConstants.PROPERTY_PATH_FILE_NEWSLETTER_TEMPLATE );

            // create the multipart request
            MultipartHttpServletRequest multi = (MultipartHttpServletRequest) request;

            // creation of the NewsLetterTemplate
            NewsLetterTemplate newsletterTemplate = NewsLetterTemplateHome.findByPrimaryKey( Integer.parseInt( 
                        multi.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_ID ) ), getPlugin(  ) );

            //Workgroup & RBAC permissions
            if ( !AdminWorkgroupService.isAuthorized( newsletterTemplate, getUser(  ) ) ||
                    !RBACService.isAuthorized( NewsLetterTemplate.RESOURCE_TYPE,
                        Integer.toString( newsletterTemplate.getId(  ) ),
                        NewsletterTemplateResourceIdService.PERMISSION_MODIFY, getUser(  ) ) )
            {
                return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
            }

            // Mandatory fields
            String strType = multi.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_TYPE );
            String strDescription = multi.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_NAME );
            String strWorkgroup = multi.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_WORKGROUP );

            if ( strDescription.equals( "" ) )
            {
                return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
            }

            if ( ( strWorkgroup == null ) || ( strWorkgroup.equals( "" ) ) )
            {
                return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
            }

            // Names of the old files
            String strOldFileName = newsletterTemplate.getFileName(  );
            String strOldImageName = newsletterTemplate.getPicture(  );

            FileItem imageItem = multi.getFile( "newsletter_template_new_picture" ); //Todo
            String strImageFileName = null;
            File fileImage = null;

            if ( ( imageItem != null ) && ( imageItem.getSize(  ) != 0 ) )
            {
                strImageFileName = UploadUtil.cleanFileName( imageItem.getName(  ) );

                String strFullPathNewImageFileName = strPathImageNewsletterTemplate + File.separator +
                    strImageFileName;
                String strFullPathOldImageFileName = strPathImageNewsletterTemplate + File.separator + strOldImageName;
                fileImage = new File( strFullPathNewImageFileName );

                if ( fileImage.exists(  ) && !( strFullPathNewImageFileName ).equals( strFullPathOldImageFileName ) )
                {
                    return AdminMessageService.getMessageUrl( request, MESSAGE_IMAGE_FILE_ALREADY_EXISTS,
                        AdminMessage.TYPE_STOP );
                }

                // we delete the old picture
                File oldImageFile = new File( strFullPathOldImageFileName );
                oldImageFile.delete(  );
            }

            FileItem modelItem = multi.getFile( "newsletter_template_new_file" );

            if ( ( modelItem != null ) && ( modelItem.getSize(  ) != 0 ) )
            {
                String strFileName = UploadUtil.cleanFileName( modelItem.getName(  ) );
                String strFullPathNewFileName = strPathFileNewsletterTemplate + File.separator + strFileName;
                String strFullPathOldFileName = strPathFileNewsletterTemplate + File.separator + strOldFileName;
                File fileTemplate = new File( strFullPathNewFileName );

                if ( fileTemplate.exists(  ) && !( strFullPathNewFileName ).equals( strFullPathOldFileName ) )
                {
                    return AdminMessageService.getMessageUrl( request, MESSAGE_FILE_ALREADY_EXISTS,
                        AdminMessage.TYPE_STOP );
                }

                // we delete the old file
                File oldFile = new File( strFullPathOldFileName );
                oldFile.delete(  );

                modelItem.write( fileTemplate );
                newsletterTemplate.setFileName( strFileName );
            }

            //if the two files are ok, write them
            if ( ( fileImage != null ) && ( strImageFileName != null ) )
            {
                imageItem.write( fileImage );
                newsletterTemplate.setPicture( strImageFileName );
            }

            // Complete the newsLetterTemplate
            newsletterTemplate.setDescription( strDescription );
            newsletterTemplate.setType( Integer.parseInt( strType ) );
            newsletterTemplate.setWorkgroup( strWorkgroup );
            NewsLetterTemplateHome.update( newsletterTemplate, getPlugin(  ) );
        }
        catch ( IOException io )
        {
            AppLogService.error( io.getMessage(  ), io );
        }
        catch ( Exception e )
        {
            AppLogService.error( e.getMessage(  ), e );
        }

        return getHomeUrl( request );
    }

    /**
     * Builds the page of preparation before sending
     *
     * @param request the http request
     * @return the html code for the preparation page
     */
    public String getPrepareNewsLetter( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin(  ) );

        //Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser(  ) ) ||
                !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                    NewsletterResourceIdService.PERMISSION_SEND, getUser(  ) ) )
        {
            return getManageNewsLetters( request );
        }

        setPageTitleProperty( PROPERTY_PAGE_TITLE_PREPARE );

        String strBaseUrl = AppPathService.getBaseUrl( request );
        HashMap model = new HashMap(  );
        String strObject = request.getParameter( PARAMETER_NEWSLETTER_OBJECT );

        if ( strObject != null )
        {
            model.put( MARK_NEWSLETTER_OBJECT, strObject );
        }
        else
        {
            model.put( MARK_NEWSLETTER_OBJECT, "" );
        }

        model.put( MARK_PREVIEW, newsletter.getHtml(  ) );
        model.put( MARK_UNSUBSCRIBE, newsletter.getUnsubscribe(  ) );
        model.put( MARK_NEWSLETTER, newsletter );
        model.put( NewsLetterConstants.MARK_BASE_URL, strBaseUrl );
        model.put( NewsLetterConstants.MARK_SUBSCRIBER_EMAIL, NewsLetterConstants.MARK_SUBSCRIBER_EMAIL_EACH );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_PREPARE_NEWSLETTER, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Builds the page of preparation before sending
     *
     * @param request  the Http request
     * @return the html code for the preparation page
     */
    public String doPrepareNewsLetter( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin(  ) );

        //Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser(  ) ) ||
                !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                    NewsletterResourceIdService.PERMISSION_SEND, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        // allow to send only if the newsletter is not empty
        if ( ( newsletter.getHtml(  ) == null ) || newsletter.getHtml(  ).equals( "" ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_SENDING_EMPTY_NOT_ALLOWED, AdminMessage.TYPE_STOP );
        }

        UrlItem urlItem = new UrlItem( JSP_URL_PREPARE_NEWSLETTER );
        urlItem.addParameter( PARAMETER_NEWSLETTER_ID, nNewsletterId );

        return urlItem.getUrl(  );
    }

    /**
     * Builds the page of preparation before sending
     *
     * @param request the http request
     * @return the html code for the preparation page
     */
    public String getPreviewNewsLetter( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin(  ) );

        //Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser(  ) ) ||
                !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                    NewsletterResourceIdService.PERMISSION_SEND, getUser(  ) ) )
        {
            return getManageNewsLetters( request );
        }

        String strBaseUrl = AppPathService.getBaseUrl( request );

        if ( !strBaseUrl.endsWith( NewsLetterConstants.CONSTANT_SLASH ) )
        {
            strBaseUrl += NewsLetterConstants.CONSTANT_SLASH;
        }

        HtmlTemplate templateNewsLetter = setHtmlTemplateEmail( newsletter, strBaseUrl, newsletter.getUnsubscribe(  ) );

        return templateNewsLetter.getHtml(  );
    }

    /**
     * Displays the confirmation page before sending the newsletter
     *
     * @param request the http request
     * @return the html code for the confirmation page
     */
    public String doConfirmSendNewsLetter( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin(  ) );

        //Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser(  ) ) ||
                !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                    NewsletterResourceIdService.PERMISSION_SEND, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        // allow to send only if the newsletter is not empty
        if ( ( newsletter.getHtml(  ) == null ) || newsletter.getHtml(  ).equals( "" ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_SENDING_EMPTY_NOT_ALLOWED, AdminMessage.TYPE_STOP );
        }

        // allow to send only if at least one active subscriber
        int nNbrSubscribers = NewsLetterHome.findNbrActiveSubscribers( nNewsletterId, getPlugin(  ) );

        if ( nNbrSubscribers == 0 )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_NO_SUBSCRIBER, AdminMessage.TYPE_STOP );
        }

        String strObject = request.getParameter( PARAMETER_NEWSLETTER_OBJECT );

        //Block access if no object for the newsletter specified
        if ( ( strObject == null ) || strObject.equals( "" ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_OBJECT_NOT_SPECIFIED, AdminMessage.TYPE_STOP );
        }

        UrlItem urlItem = new UrlItem( JSP_URL_SEND_NEWSLETTER );
        HashMap<String, String> requestedParameters = new HashMap<String, String>(  );
        requestedParameters.put( PARAMETER_NEWSLETTER_OBJECT, strObject );
        requestedParameters.put( PARAMETER_NEWSLETTER_ID, strNewsletterId );

        // warn if the newletter html content is the same as the one of the last
        // sending for that newsletter
        SendingNewsLetter lastSending = SendingNewsLetterHome.findLastSendingForNewsletterId( nNewsletterId,
                getPlugin(  ) );

        if ( ( lastSending != null ) && lastSending.getHtml(  ).equals( newsletter.getHtml(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_FRAGMENT_NO_CHANGE, urlItem.getUrl(  ),
                AdminMessage.TYPE_CONFIRMATION, requestedParameters );
        }
        else
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_SEND_NEWSLETTER, urlItem.getUrl(  ),
                AdminMessage.TYPE_CONFIRMATION, requestedParameters );
        }
    }

    /**
     * Displays the confirmation page before testing the newsletter
     *
     * @param request the http request
     * @return the html code for the confirmation page
     */
    public String doConfirmTestNewsLetter( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin(  ) );

        //Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser(  ) ) ||
                !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                    NewsletterResourceIdService.PERMISSION_MODIFY, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        // allow to send only if the newsletter is not empty
        if ( ( newsletter.getHtml(  ) == null ) || newsletter.getHtml(  ).equals( "" ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_SENDING_EMPTY_NOT_ALLOWED, AdminMessage.TYPE_STOP );
        }

        String strObject = request.getParameter( PARAMETER_NEWSLETTER_OBJECT );

        UrlItem urlItem = new UrlItem( JSP_URL_TEST_NEWSLETTER );
        urlItem.addParameter( PARAMETER_NEWSLETTER_OBJECT, strObject );
        urlItem.addParameter( PARAMETER_NEWSLETTER_ID, nNewsletterId );

        return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_TEST_NEWSLETTER, urlItem.getUrl(  ),
            AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Processes the testing of a newsletter
     *
     * @param request  the http request
     * @return the url of the confirmation page
     */
    public String doTestNewsLetter( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin(  ) );

        //Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser(  ) ) ||
                !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                    NewsletterResourceIdService.PERMISSION_MODIFY, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        //Allow to send test if the list of test recipients is not empty
        String strTestRecipients = newsletter.getTestRecipients(  );

        if ( !isWrongEmail( strTestRecipients ).equals( "" ) )
        {
            Object[] messageArgs = { isWrongEmail( strTestRecipients ) };

            return AdminMessageService.getMessageUrl( request, MESSAGE_WRONG_EMAIL, messageArgs, AdminMessage.TYPE_STOP );
        }

        SendingNewsLetter sending = new SendingNewsLetter(  );
        sending.setNewsLetterId( nNewsletterId );
        sending.setDate( new Timestamp( new java.util.Date(  ).getTime(  ) ) ); // the
                                                                                // current
                                                                                // date

        String strObject = I18nService.getLocalizedString( PROPERTY_TEST_SUBJECT, getLocale(  ) ) +
            newsletter.getName(  ) + "]" + newsletter.getTestSubject(  );

        /* lutece.properties */
        String strBaseUrl = getProdUrl( request );

        HtmlTemplate templateNewsLetter = setHtmlTemplateEmail( newsletter, strBaseUrl, newsletter.getUnsubscribe(  ) );

        sendMail( newsletter, strObject, strBaseUrl, templateNewsLetter,
            convertToList( newsletter.getTestRecipients(  ) ) );

        return getHomeUrl( request );
    }

    /**
     * Processes the sending of a newsletter
     *
     * @param request  the http request
     * @return the url of the confirmation page
     */
    public String doSendNewsLetter( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        String strObject = request.getParameter( PARAMETER_NEWSLETTER_OBJECT );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin(  ) );

        //Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser(  ) ) ||
                !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                    NewsletterResourceIdService.PERMISSION_SEND, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        // allow to send only if at list one subscriber
        int nNbrSubscribers = NewsLetterHome.findNbrSubscribers( nNewsletterId, getPlugin(  ) );

        if ( nNbrSubscribers == 0 )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_NO_SUBSCRIBER, AdminMessage.TYPE_STOP );
        }

        if ( ( strObject == null ) || ( strObject.trim(  ).equals( "" ) ) )
        {
            strObject = newsletter.getName(  );
        }

        /* lutece.properties */
        String strBaseUrl = getProdUrl( request );

        /* list of subscribers */
        Collection<Subscriber> list = SubscriberHome.findSubscribersForSending( nNewsletterId,
                Integer.parseInt( AppPropertiesService.getProperty( PROPERTY_LIMIT_MIN_SUSCRIBER ) ),
                Integer.parseInt( AppPropertiesService.getProperty( PROPERTY_LIMIT_MAX_SUSCRIBER ) ), getPlugin(  ) );

        //Get the HtmlTemplate of email
        HtmlTemplate templateNewsLetter = setHtmlTemplateEmail( newsletter, strBaseUrl, newsletter.getUnsubscribe(  ) );

        sendMail( newsletter, strObject, strBaseUrl, templateNewsLetter, list );

        SendingNewsLetter sending = new SendingNewsLetter(  );
        sending.setNewsLetterId( nNewsletterId );
        sending.setDate( new Timestamp( new java.util.Date(  ).getTime(  ) ) );
        sending.setCountSubscribers( nNbrSubscribers );

        HtmlTemplate templateNewsLetterForArchive = setHtmlTemplateEmail( newsletter, strBaseUrl,
                Boolean.toString( false ) );
        sending.setHtml( templateNewsLetterForArchive.getHtml(  ) );
        sending.setEmailSubject( strObject );
        SendingNewsLetterHome.create( sending, getPlugin(  ) );

        // updates the sending date
        newsletter.setDateLastSending( sending.getDate(  ) );
        NewsLetterHome.update( newsletter, getPlugin(  ) );

        return getHomeUrl( request );
    }

    /**
     * Processes the registration of a newsletter and loads the newsletter
     * management page
     *
     * @param request  The Http request
     * @return The jsp URL which displays the newsletters management page
     */
    public String doRegisterNewsLetter( HttpServletRequest request )
    {
        String strAction = request.getParameter( PARAMETER_ACTION );
        String strReturn = null;

        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin(  ) );

        //Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser(  ) ) ||
                !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                    NewsletterResourceIdService.PERMISSION_MODIFY, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        if ( !strAction.equals( I18nService.getLocalizedString( getPlugin(  ).getName(  ) + PROPERTY_CANCEL_ACTION,
                        getLocale(  ) ) ) )
        {
            newsletter.setNewsLetterTemplateId( Integer.parseInt( request.getParameter( 
                        NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_ID ) ) );
            newsletter.setDocumentTemplateId( Integer.parseInt( request.getParameter( PARAMETER_DOCUMENT_TEMPLATE_ID ) ) );

            String strBaseUrl = AppPathService.getBaseUrl( request );
            newsletter.setHtml( doClean( request.getParameter( PARAMETER_HTML_CONTENT ), strBaseUrl ) );
            NewsLetterHome.update( newsletter, getPlugin(  ) );

            if ( strAction.equals( I18nService.getLocalizedString( getPlugin(  ).getName(  ) +
                            PROPERTY_REGISTER_ACTION, getLocale(  ) ) ) )
            {
                // register action
                strReturn = getHomeUrl( request );
            }
            else if ( strAction.equals( I18nService.getLocalizedString( getPlugin(  ).getName(  ) +
                            PROPERTY_PREPARE_SENDING_ACTION, getLocale(  ) ) ) )
            {
                UrlItem url = new UrlItem( JSP_URL_DO_PREPARE_NEWSLETTER );
                url.addParameter( PARAMETER_NEWSLETTER_ID, nNewsletterId );
                strReturn = url.getUrl(  );
            }
            else if ( strAction.equals( I18nService.getLocalizedString( getPlugin(  ).getName(  ) +
                            PROPERTY_TEST_SENDING_ACTION, getLocale(  ) ) ) )
            {
                UrlItem url = new UrlItem( JSP_URL_CONFIRM_TEST_NEWSLETTER );
                url.addParameter( PARAMETER_NEWSLETTER_ID, nNewsletterId );
                strReturn = url.getUrl(  );
            }
        }
        else
        {
            String strUrl = getHomeUrl( request );
            strReturn = AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_CANCEL_COMPOSE, strUrl,
                    AdminMessage.TYPE_CONFIRMATION );
        }

        return strReturn;
    }

    /**
     * Builds the subscribers import page
     *
     * @param request The HTTP request
     * @return the html code for subscribers import page
     */
    public String getImportSubscribers( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin(  ) );

        //Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser(  ) ) ||
                !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                    NewsletterResourceIdService.PERMISSION_MANAGE_SUBSCRIBERS, getUser(  ) ) )
        {
            return getManageNewsLetters( request );
        }

        setPageTitleProperty( PROPERTY_PAGE_TITLE_IMPORT );

        HashMap model = new HashMap(  );
        model.put( MARK_NEWSLETTER_ID, nNewsletterId );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_IMPORT_SUBSCRIBERS, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Processes the import of subscribers due to a csv file and loads the
     * subscribers management page
     * @param request The Http request
     * @return The jsp URL which displays the subscribers management page
     */
    public String doImportSubscribers( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin(  ) );

        //Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser(  ) ) ||
                !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                    NewsletterResourceIdService.PERMISSION_IMPORT_SUBSCRIBERS, getUser(  ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        try
        {
            // create the multipart request
            MultipartHttpServletRequest multi = (MultipartHttpServletRequest) request;

            FileItem csvItem = multi.getFile( PARAMETER_SUBSCRIBERS_FILE );

            String strMultiFileName = UploadUtil.cleanFileName( csvItem.getName(  ) );

            if ( ( csvItem == null ) || strMultiFileName.equals( "" ) )
            {
                return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
            }

            // test the extension of the file must be 'csv'
            String strExtension = strMultiFileName.substring( strMultiFileName.length(  ) - 4,
                    strMultiFileName.length(  ) );

            if ( !strExtension.equals( CONSTANT_CSV_FILE_EXTENSION ) )
            {
                return AdminMessageService.getMessageUrl( request, MESSAGE_CSV_FILE_EXTENSION, AdminMessage.TYPE_STOP );
            }

            Reader fileReader = new InputStreamReader( csvItem.getInputStream(  ) );
            CSVReader csvReader = new CSVReader( fileReader,
                    AppPropertiesService.getProperty( getPlugin(  ).getName(  ) + PROPERTY_IMPORT_DELIMITER ).charAt( 0 ) );

            List<String[]> tabUsers = csvReader.readAll(  );

            // the file is empty
            if ( ( tabUsers == null ) || ( tabUsers.size(  ) == 0 ) )
            {
                return AdminMessageService.getMessageUrl( request, MESSAGE_CSV_FILE_EMPTY_OR_NOT_VALID_EMAILS,
                    AdminMessage.TYPE_STOP );
            }
            else
            {
                // Add the new users
                for ( String[] strEmailTemp : tabUsers )
                {
                    int nColumnIndex = Integer.parseInt( AppPropertiesService.getProperty( getPlugin(  ).getName(  ) +
                                CONSTANT_EMAIL_COLUMN_INDEX ) );

                    if ( strEmailTemp.length < nColumnIndex )
                    {
                        return AdminMessageService.getMessageUrl( request, MESSAGE_COLUMN_INDEX_NOT_EXIST,
                            AdminMessage.TYPE_ERROR );
                    }

                    String strEmail = strEmailTemp[nColumnIndex];

                    //check if the email is not null and is valid
                    if ( ( strEmail != null ) && StringUtil.checkEmail( strEmail.trim(  ) ) )
                    {
                        // Checks if a subscriber with the same email address doesn't exist yet
                        Subscriber subscriber = SubscriberHome.findByEmail( strEmail, getPlugin(  ) );

                        if ( subscriber == null )
                        {
                            // The email doesn't exist, so create a new subcriber
                            subscriber = new Subscriber(  );
                            subscriber.setEmail( strEmail );
                            SubscriberHome.create( subscriber, getPlugin(  ) );
                        }

                        // the current date
                        Timestamp tToday = new java.sql.Timestamp( new java.util.Date(  ).getTime(  ) );

                        // adds a subscriber to the current newsletter
                        NewsLetterHome.addSubscriber( nNewsletterId, subscriber.getId(  ), tToday, getPlugin(  ) );
                    }
                }
            }

            UrlItem urlItem = new UrlItem( JSP_URL_MANAGE_SUBSCRIBERS );
            urlItem.addParameter( PARAMETER_NEWSLETTER_ID, nNewsletterId );

            return urlItem.getUrl(  );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e.getMessage(  ) );
        }
    }

    /**
     * Remove a known suscriber from a newsletter
     *
     * @param subscriber the subscriber to remove
     * @param nNewsletterId the newsletter id from which to remove the subscriber
     * @param plugin The plugin object
     */
    private void removeSubscriberFromNewsletter( Subscriber subscriber, int nNewsletterId, Plugin plugin )
    {
        /* checks newsletter exist in database */
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, plugin );

        if ( ( subscriber != null ) && ( newsletter != null ) )
        {
            int nSubscriberId = subscriber.getId(  );

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
     * Generate the html code for the documents corresponding
     * to the documents associated with the newsletter and to the date of the last
     * sending of the newsletter
     *
     * @param nNewsLetterId the newsletter associated to categories
     * @param nTemplateId the document id to use
     * @param strBaseUrl the url of the portal
     * @return the html code for the document list of null if no document template available
     */
    private String generateDocumentsList( int nNewsLetterId, int nTemplateId, String strBaseUrl )
    {
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsLetterId, getPlugin(  ) );
        int[] arrayCategoriesIds = NewsLetterHome.findNewsletterCategoryIds( nNewsLetterId, getPlugin(  ) );
        String strTemplatePath = NewsletterUtils.getHtmlTemplatePath( nTemplateId, getPlugin(  ) );

        if ( strTemplatePath == null )
        {
            return null;
        }

        DocumentFilter documentFilter = new DocumentFilter(  );

        if ( ( arrayCategoriesIds.length > 0 ) && ( arrayCategoriesIds[0] > -1 ) )
        {
            documentFilter.setCategoriesId( arrayCategoriesIds );
        }

        Collection<Document> listDocuments = PublishingService.getInstance(  )
                                                              .getPublishedDocumentsSinceDate( newsletter.getDateLastSending(  ),
                documentFilter, getLocale(  ) );

        StringBuffer sbDocumentLists = new StringBuffer(  );

        // get html from templates
        for ( Document document : listDocuments )
        {
            Collection<Portlet> porletCollec = PublishingService.getInstance(  )
                                                                .getPortletsByDocumentId( Integer.toString( 
                        document.getId(  ) ) );

            //the document insert in the buffer must be publish in a authorized portlet
            if ( PortletService.getInstance(  ).getAuthorizedPortletCollection( porletCollec, getUser(  ) ).size(  ) > 0 )
            {
                Map<String, Object> model = new HashMap<String, Object>(  );
                model.put( NewsLetterConstants.MARK_DOCUMENT, document );
                
				// if noSecuredImg is true, it will copy all document's picture in a no secured folder
				String strNoSecuredImg = AppPropertiesService.getProperty( PROPERTY_NO_SECURED_IMG_OPTION );

				if ( ( strNoSecuredImg != null ) && strNoSecuredImg.equalsIgnoreCase( Boolean.TRUE.toString() ) )
				{	
					String strImgFolder = AppPropertiesService.getProperty( PROPERTY_NO_SECURED_IMG_FOLDER ) + SLASH;
					String pictureName = NewsletterService.getInstance().copyFileFromDocument( document, FILE_TYPE, AppPropertiesService.getProperty( PROPERTY_WEBAPP_PATH, AppPathService.getWebAppPath() + SLASH ) + strImgFolder );
					if ( pictureName != null )
					{
						model.put( MARK_IMG_PATH, AppPropertiesService.getProperty( PROPERTY_WEBAPP_URL ) + strImgFolder + pictureName );
					}
				}
                
                ReferenceList hostKeysList = new ReferenceList(  );

                try
                {
                    if ( AppPathService.getAvailableVirtualHosts(  ) != null )
                    {
                        hostKeysList = AppPathService.getAvailableVirtualHosts(  );

                        ReferenceList listHosts = new ReferenceList(  );

                        for ( int j = 0; j < hostKeysList.size(  ); j++ )
                        {
                            listHosts.addItem( hostKeysList.get( j ).getName(  ),
                                AppPropertiesService.getProperty( PROPERTY_VIRTUAL_HOST +
                                    hostKeysList.get( j ).getCode(  ) + SUFFIX_BASE_URL ) );
                        }

                        model.put( MARK_VIRTUAL_HOSTS, listHosts );
                    }
                }
                catch ( NullPointerException e )
                {
                    AppLogService.error( e.getMessage(  ), e );
                }

                model.put( NewsLetterConstants.MARK_BASE_URL, strBaseUrl );
                model.put( NewsLetterConstants.MARK_DOCUMENT_PORTLETS_COLLEC, porletCollec );

                HtmlTemplate template = AppTemplateService.getTemplate( strTemplatePath, getLocale(  ), model );

                sbDocumentLists.append( template.getHtml(  ) );
            }
        }

        return sbDocumentLists.toString(  );
    }

    /**
     * Generate the html code of the newsletter according to the document and
     * newsletter templates
     *
     * @param nNewsLetterId the newsletter id
     * @param nTemplateNewsLetterId  the newsletter template id
     * @param nTemplateDocumentId the document template id
     * @param strBaseUrl The base url of the portal
     * @param user the current user
     * @return the html code for the newsletter content of null if no template available
     */
    private String generateNewsletterHtmlCode( int nNewsLetterId, int nTemplateNewsLetterId, int nTemplateDocumentId,
        String strBaseUrl, AdminUser user )
    {
        String strTemplatePath = NewsletterUtils.getHtmlTemplatePath( nTemplateNewsLetterId, getPlugin(  ) );
        String strDocumentPath = generateDocumentsList( nNewsLetterId, nTemplateDocumentId, strBaseUrl );

        if ( strTemplatePath == null )
        {
            return null;
        }

        HashMap model = new HashMap(  );
        model.put( NewsLetterConstants.MARK_DOCUMENT_LIST, strDocumentPath );
        model.put( NewsLetterConstants.MARK_BASE_URL, strBaseUrl );

        HtmlTemplate templateNewsLetter = AppTemplateService.getTemplate( strTemplatePath, getLocale(  ), model );

        return templateNewsLetter.getHtml(  );
    }

    // //////////////////////////////////////////////////////////////////////////////////
    // Private Implementation

    /**
     * To translate the absolute url's in SEMI-relativre url's of the
     * html_content ( use before insertion in db)
     *
     * @param strContent The html code
     * @param strBaseUrl The base url
     * @return The clean code
     */
    private String doClean( String strContent, String strBaseUrl )
    {
        if ( strBaseUrl.endsWith( NewsLetterConstants.CONSTANT_SLASH ) )
        {
            strBaseUrl = strBaseUrl.substring( 0, strBaseUrl.length(  ) - 1 );
        }

        String strNewContent = strContent;
        strNewContent = StringUtil.substitute( strNewContent, NewsLetterConstants.WEBAPP_PATH_FOR_LINKSERVICE, strBaseUrl );

        return strNewContent;
    }

    /**
     * Determine if we must send mail in MHTML
     *
     * @param plugin  The Plugin used to retrieve plugin's name
     * @return true whether MHTML is needed
     */
    private boolean isMhtmlActivated( Plugin plugin )
    {
        String strProperty = AppPropertiesService.getProperty( getPlugin(  ).getName(  ) +
                NewsLetterConstants.PROPERTY_MAIL_MULTIPART );

        return ( strProperty != null ) && Boolean.valueOf( strProperty ).booleanValue(  );
    }

    /**
     * Tests whether all the e-mails represented by a string are valid
     * @param strRecipientLists The list of recipients
     * @return The last wrong invalid e-mail in the list or an empty String if all e-mails are valid
     */
    private String isWrongEmail( String strRecipientLists )
    {
        String strWrongEmail = "";

        String strDelimiter = AppPropertiesService.getProperty( getPlugin(  ).getName(  ) + PROPERTY_IMPORT_DELIMITER );

        String[] strEmails = strRecipientLists.split( strDelimiter );

        for ( int j = 0; j < strEmails.length; j++ )
        {
            if ( !StringUtil.checkEmail( strEmails[j] ) )
            {
                strWrongEmail = strEmails[j];
            }
        }

        return strWrongEmail;
    }

    /**
     * Takes a list of recipients in a form of a String and converts it into a list of subscribers
     *  @param strRecipients A list of recipients as a String
     *  @return A list of subscibers
     */
    private Collection<Subscriber> convertToList( String strRecipients )
    {
        Collection<Subscriber> listRecipients = new ArrayList<Subscriber>(  );

        if ( ( strRecipients != null ) && !strRecipients.equals( "" ) )
        {
            String strDelimiter = AppPropertiesService.getProperty( getPlugin(  ).getName(  ) +
                    PROPERTY_IMPORT_DELIMITER );

            String[] strEmails = strRecipients.split( strDelimiter );

            for ( int j = 0; j < strEmails.length; j++ )
            {
                if ( StringUtil.checkEmail( strEmails[j] ) )
                {
                    Subscriber subscriber = new Subscriber(  );
                    subscriber.setEmail( strEmails[j] );
                    listRecipients.add( subscriber );
                }
            }
        }

        return listRecipients;
    }

    /**
     * Check if user is authozired to create a newsletter
     * @param request The {@link HttpServletRequest}
     * @return true if creation is authorized, false otherwise
     */
    private boolean isNewsletterCreationAllowed( HttpServletRequest request )
    {
        //RBAC permission
        if ( RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID,
                    NewsletterResourceIdService.PERMISSION_CREATE, getUser(  ) ) )
        {
            return true;
        }

        Collection<NewsLetter> listNewsletter = NewsLetterHome.findAll( getPlugin(  ) );
        listNewsletter = AdminWorkgroupService.getAuthorizedCollection( listNewsletter, getUser(  ) );

        for ( NewsLetter newsletter : listNewsletter )
        {
            if ( RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_CREATE, getUser(  ) ) )
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if user is authozired to create a newsletter template
     * @param request The {@link HttpServletRequest}
     * @return true if creation is authorized, false otherwise
     */
    private boolean isNewsletterTemplateCreationAllowed( HttpServletRequest request )
    {
        //RBAC permission
        if ( RBACService.isAuthorized( NewsLetterTemplate.RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID,
                    NewsletterTemplateResourceIdService.PERMISSION_CREATE, getUser(  ) ) )
        {
            return true;
        }

        Collection<NewsLetterTemplate> listNewsletterTemplates = NewsLetterTemplateHome.getTemplatesList( getPlugin(  ) );
        listNewsletterTemplates = AdminWorkgroupService.getAuthorizedCollection( listNewsletterTemplates, getUser(  ) );

        for ( NewsLetterTemplate newsletterTemplate : listNewsletterTemplates )
        {
            if ( RBACService.isAuthorized( newsletterTemplate, NewsletterTemplateResourceIdService.PERMISSION_CREATE,
                        getUser(  ) ) )
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the content of a CSS file
     *
     * @return The content into a String
     */
    private String getCssContent(  )
    {
        String strContent = "";
        String strListCssFileName = AppPropertiesService.getProperty( PROPERTY_CSS_FILES );

        if ( ( strListCssFileName != null ) && !strListCssFileName.equals( "" ) )
        {
            for ( String strName : strListCssFileName.split( SEPARATOR_PROPERTY_CSS_FILES ) )
            {
                strContent += getTextFileContent( AppPathService.getWebAppPath(  ) + "/" + strName );
                strContent += SEPARATOR_CSS_FILES_CONTENT;
            }
        }

        return strContent;
    }

    /**
     * Get the content of a text file
     * @param strFileName The full name of the file
     * @return The content
     */
    private String getTextFileContent( String strFileName )
    {
        BufferedReader fileReader;
        String strSource = "";

        try
        {
            fileReader = new BufferedReader( new FileReader( strFileName ) );

            String line;

            line = fileReader.readLine(  );

            while ( line != null )
            {
                strSource += ( line + SEPARATOR_CSS_FILES_CONTENT );
                line = fileReader.readLine(  );
            }

            fileReader.close(  );
        }
        catch ( FileNotFoundException e )
        {
            AppLogService.error( "plugin-newsletter - CSS '" + strFileName + "'not found ! " + e.getMessage(  ) );
        }
        catch ( IOException e )
        {
            AppLogService.error( "plugin-newsletter - error when reading CSS '" + strFileName + "'! " +
                e.getMessage(  ) );
        }

        return strSource;
    }

    /**
     * Generate the final html code for email
     * @param newsletter The newsletter to generate
     * @param strBaseUrl The baseUrl (can be prod url)
     * @return The {@link HtmlTemplate}
     */
    private HtmlTemplate setHtmlTemplateEmail( NewsLetter newsletter, String strBaseUrl, String strUnsubscribe )
    {
        HashMap sendingModel = new HashMap(  );
        sendingModel.put( MARK_CSS, getCssContent(  ) );
        sendingModel.put( MARK_UNSUBSCRIBE, strUnsubscribe );
        sendingModel.put( MARK_NEWSLETTER_ID, newsletter.getId(  ) );
        sendingModel.put( MARK_NEWSLETTER_CONTENT, newsletter.getHtml(  ) );
        sendingModel.put( NewsLetterConstants.MARK_BASE_URL, strBaseUrl );
        sendingModel.put( NewsLetterConstants.MARK_SUBSCRIBER_EMAIL, NewsLetterConstants.MARK_SUBSCRIBER_EMAIL_EACH );

        HtmlTemplate templateNewsLetter = AppTemplateService.getTemplate( TEMPLATE_SEND_NEWSLETTER, getLocale(  ),
                sendingModel );

        templateNewsLetter.substitute( NewsLetterConstants.WEBAPP_PATH_FOR_LINKSERVICE, strBaseUrl );

        return templateNewsLetter;
    }

    /**
     * Send the newsletter to a list of subscribers
     * @param newsletter The newsletter to send
     * @param strObject The email object
     * @param strBaseUrl The baseUrl (can be prod url)
     * @param templateNewsletter The generated template
     * @param listSubscribers The list of subscribers (date and id can be null, only email is used)
     */
    private void sendMail( NewsLetter newsletter, String strObject, String strBaseUrl, HtmlTemplate templateNewsletter,
        Collection<Subscriber> listSubscribers )
    {
        List<UrlAttachment> urlAttachments = null;

        if ( isMhtmlActivated( getPlugin(  ) ) )
        { // we use absolute urls if there is no preproduction process

            boolean useAbsoluteUrl = isAbsoluteUrl(  );
            String strTemplate = templateNewsletter.getHtml(  );
            strTemplate = StringUtil.substitute( strTemplate, strBaseUrl, NewsLetterConstants.WEBAPP_PATH_FOR_LINKSERVICE );
            urlAttachments = MailService.getUrlAttachmentList( strTemplate, strBaseUrl, useAbsoluteUrl );

            // all images, css urls are relative
            if ( !useAbsoluteUrl )
            {
                templateNewsletter.substitute( strBaseUrl, strBaseUrl.replaceFirst( "https?://[^/]+/", "/" ) );
            }
            else
            {
            	String strContent = NewsletterUtils.rewriteUrls( templateNewsletter.getHtml(  ), strBaseUrl );
                templateNewsletter = new HtmlTemplate( strContent );
            }
        }

        for ( Subscriber subscriber : listSubscribers )
        {
            HtmlTemplate t = new HtmlTemplate( templateNewsletter );
            t.substitute( NewsLetterConstants.MARK_SUBSCRIBER_EMAIL_EACH, subscriber.getEmail(  ) ); //t.substitute( NewsLetterConstants.MARK_SUBSCRIBER_EMAIL, strTestRecipientMail );

            String strNewsLetterCode = t.getHtml(  );

            if ( ( urlAttachments == null ) || ( urlAttachments.size(  ) == 0 ) )
            {
                MailService.sendMailHtml( subscriber.getEmail(  ), newsletter.getNewsletterSenderName(  ),
                    newsletter.getNewsletterSenderMail(  ), strObject, strNewsLetterCode );
            }
            else
            {
                MailService.sendMailMultipartHtml( subscriber.getEmail(  ), newsletter.getNewsletterSenderName(  ),
                    newsletter.getNewsletterSenderMail(  ), strObject, strNewsLetterCode, urlAttachments );
            }
        }
    }

    /**
     * Return the webapp prod url (or base url if null)
     * @param request The HTTP request
     * @return The prod url
     */
    private String getProdUrl( HttpServletRequest request )
    {
        String strBaseUrl = AppPropertiesService.getProperty( PROPERTY_PROD_BASE_URL );

        if ( ( strBaseUrl == null ) || strBaseUrl.equals( "" ) )
        {
            strBaseUrl = AppPathService.getBaseUrl( request );
        }

        if ( !strBaseUrl.endsWith( NewsLetterConstants.CONSTANT_SLASH ) )
        {
            strBaseUrl += NewsLetterConstants.CONSTANT_SLASH;
        }

        return strBaseUrl;
    }

    /**
     * Check the property in property file to know if url must be absolutes or relatives
     * @return true if absolute or false else
     */
    private boolean isAbsoluteUrl(  )
    {
        boolean useAbsoluteUrl = false;
        String strUseAbsoluteUrl = AppPropertiesService.getProperty( PROPERTY_ABSOLUTE_URL_MAIL );

        if ( ( strUseAbsoluteUrl != null ) && strUseAbsoluteUrl.equalsIgnoreCase( Boolean.TRUE.toString(  ) ) )
        {
            useAbsoluteUrl = true;
        }

        return useAbsoluteUrl;
    }

    /**
     * Exports the list of subscribers in a csv file format
     * @param request The Http Request
     * @param response  The Http Response
     * @return The url of the management console for newsletters
     */
    public String doExportCsv( HttpServletRequest request, HttpServletResponse response )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin(  ) );

        // allow to send only if at least one subscriber
        int nNbrSubscribers = NewsLetterHome.findNbrActiveSubscribers( nNewsletterId, getPlugin(  ) );

        if ( nNbrSubscribers == 0 )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_NO_SUBSCRIBER_EXPORT, AdminMessage.TYPE_STOP );
        }

        String strFileName = newsletter.getName(  ) + CONSTANT_CSV_FILE_EXTENSION;
        byte[] byteSubscribersList = exportToCsv( nNewsletterId );
        NewsletterUtils.addHeaderResponse( request, response, strFileName, CONSTANT_CSV_FILE_EXTENSION );
        response.setContentLength( (int) byteSubscribersList.length );

        try
        {
            OutputStream os = response.getOutputStream(  );
            os.write( byteSubscribersList );
            os.close(  );
        }
        catch ( IOException e )
        {
            AppLogService.error( e );
        }

        return getHomeUrl( request );
    }

    /**
     * Fetches the list of subscribers on a specific newsletter
     * @param nNewsletterId The id of the newsletter
     * @return The byte representation of the list of subscribers
     */
    private byte[] exportToCsv( int nNewsletterId )
    {
        byte[] byteSubscribersList = null;

        try
        {
            ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream(  );
            CSVWriter writer = new CSVWriter( new BufferedWriter( new OutputStreamWriter( byteArrayStream, "UTF-8" ) ) );
            Collection<Subscriber> listSubscriber = SubscriberHome.findSubscribers( nNewsletterId, getPlugin(  ) );

            for ( Subscriber subscriber : listSubscriber )
            {
                String[] arraySubscriber = new String[3];
                arraySubscriber[0] = Integer.toString( subscriber.getId(  ) );
                arraySubscriber[1] = subscriber.getEmail(  );
                arraySubscriber[2] = subscriber.getDateSubscription(  ).toString(  );
                writer.writeNext( arraySubscriber );
            }

            writer.close(  );
            byteSubscribersList = byteArrayStream.toByteArray(  );
        }
        catch ( UnsupportedEncodingException e )
        {
            AppLogService.error( e );
        }
        catch ( IOException e )
        {
            AppLogService.error( e );
        }

        return byteSubscribersList;
    }
    

}
