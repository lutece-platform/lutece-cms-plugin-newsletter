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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import au.com.bytecode.opencsv.CSVReader;
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
import fr.paris.lutece.plugins.newsletter.business.topic.NewsletterTopic;
import fr.paris.lutece.plugins.newsletter.business.topic.NewsletterTopicHome;
import fr.paris.lutece.plugins.newsletter.service.NewsLetterRegistrationService;
import fr.paris.lutece.plugins.newsletter.service.NewsletterPlugin;
import fr.paris.lutece.plugins.newsletter.service.NewsletterResourceIdService;
import fr.paris.lutece.plugins.newsletter.service.NewsletterService;
import fr.paris.lutece.plugins.newsletter.service.topic.NewsletterTopicService;
import fr.paris.lutece.plugins.newsletter.util.NewsLetterConstants;
import fr.paris.lutece.plugins.newsletter.util.NewsletterUtils;
import fr.paris.lutece.portal.business.rbac.RBAC;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.business.workgroup.AdminWorkgroupHome;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
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
import fr.paris.lutece.portal.web.util.LocalizedPaginator;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.datatable.DataTableManager;
import fr.paris.lutece.util.date.DateUtil;
import fr.paris.lutece.util.filesystem.UploadUtil;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.html.IPaginator;
import fr.paris.lutece.util.html.Paginator;
import fr.paris.lutece.util.sort.AttributeComparator;
import fr.paris.lutece.util.string.StringUtil;
import fr.paris.lutece.util.url.UrlItem;

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
     * Serial version UID
     */
    private static final long serialVersionUID = 5581671308419705321L;

    private static final String CONSTANT_CSV_FILE_EXTENSION = ".csv";
    private static final String CONSTANT_EMAIL_COLUMN_INDEX = "newsletter.csv.import.columnindex";
    private static final String PROPERTY_LIMIT_MAX_SUSCRIBER = "newsletter.limit.max";
    private static final String PROPERTY_LIMIT_MIN_SUSCRIBER = "newsletter.limit.min";

    private static final String PROPERTY_REGISTER_ACTION = "newsletter.compose_newsletter.buttonRegister";
    private static final String PROPERTY_PREPARE_SENDING_ACTION = "newsletter.compose_newsletter.buttonPrepareSending";
    private static final String PROPERTY_CANCEL_ACTION = "newsletter.compose_newsletter.buttonCancel";
    private static final String PROPERTY_TEST_SENDING_ACTION = "newsletter.compose_newsletter.buttonTestSending";
    private static final String PROPERTY_LIMIT_CONFIRM_DAYS = "newsletter.confirm.limit";
    private static final String PROPERTY_SELECT_MANDATORY = "newsletter.manage_archive.multiSelectError";

    // Css inclusion
    private static final String PROPERTY_CSS_FILES = "newsletter.css.files";
    private static final String SEPARATOR_PROPERTY_CSS_FILES = ";";
    private static final String SEPARATOR_CSS_FILES_CONTENT = "\n";

    // Handling of CSV
    private static final String PROPERTY_IMPORT_DELIMITER = "newsletter.csv.import.delimiter";

    // templates
    private static final String TEMPLATE_MANAGE_NEWSLETTERS = "admin/plugins/newsletter/manage_newsletters.html";
    private static final String TEMPLATE_MANAGE_NEWSLETTERS_PROPERTIES = "admin/plugins/newsletter/manage_newsletters_properties.html";
    private static final String TEMPLATE_MODIFY_NEWSLETTER = "admin/plugins/newsletter/modify_newsletter.html";
    private static final String TEMPLATE_CREATE_NEWSLETTER = "admin/plugins/newsletter/create_newsletter.html";
    private static final String TEMPLATE_COMPOSE_NEWSLETTER = "admin/plugins/newsletter/compose_newsletter.html";
    private static final String TEMPLATE_PREPARE_NEWSLETTER = "admin/plugins/newsletter/prepare_newsletter.html";
    private static final String TEMPLATE_SEND_NEWSLETTER = "admin/plugins/newsletter/send_newsletter.html";
    private static final String TEMPLATE_MANAGE_SUBSCRIBERS = "admin/plugins/newsletter/manage_subscribers.html";
    private static final String TEMPLATE_IMPORT_SUBSCRIBERS = "admin/plugins/newsletter/import_subscribers.html";
    private static final String TEMPLATE_MANAGE_OLD_NEWSLETTERS = "admin/plugins/newsletter/manage_old_newsletters.html";
    private static final String TEMPLATE_MANAGE_NEWSLETTER_TOPICS = "admin/plugins/newsletter/manage_newsletter_topics.html";
    private static final String TEMPLATE_MODIFY_TOPIC_CONFIG = "admin/plugins/newsletter/modify_topic_config.html";

    // marks
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
    private static final String MARK_NEWSLETTER_ID = "newsletter_id";
    private static final String MARK_IMPORT_DELETE = "import_delete";
    private static final String MARK_NEWSLETTER_CONTENT = "newsletter_content";
    private static final String MARK_HTML_CONTENT = "html_content";
    private static final String MARK_WEBAPP_URL = "webapp_url";
    private static final String MARK_NEWSLETTER_TEMPLATE_ID = "newsletter_template_id";
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
    private static final String MARK_LIST_TOPIC_TYPES = "list_topic_types";
    private static final String MARK_NEWSLETTER_TABLE_MANAGER = "table_manager";
    private static final String MARK_SECTION_SIZES = "section_size";
    private static final String MARK_SUBSCRIBERS_LIST = "subscribers_list";
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
    private static final String MARK_EXPORT_SUBSCRIBER_RIGTH = "is_export_subscriber_right";
    private static final String MARK_CLEAN_RIGHT = "is_clean_subscriber_right";
    private static final String MARK_NEWSLETTER_TOPIC_TITLE = "title";
    private static final String MARK_NEWSLETTER_TOPIC_TYPE = "topicTypeName";
    private static final String MARK_NEWSLETTER_TOPIC_ORDER = "order";
    private static final String MARK_CONTENT = "content";
    private static final String MARK_TOPIC = "topic";
    private static final String MARK_SEARCH_STRING = "search_string";
    private static final String MARK_RIGHT_MANAGE_NEWSLETTER_PROPERTIES = "right_manage_newsletter_properties";

    // PARAMETER
    private static final String PARAMETER_ACTION = "action";
    private static final String PARAMETER_NEWSLETTER_ID = "newsletter_id";
    private static final String PARAMETER_TOPIC_TYPE = "topic_type";
    private static final String PARAMETER_SENDING_NEWSLETTER_ID = "sending_newsletter_id";
    private static final String PARAMETER_TOPIC_SECTION_NUMBER = "topic_section_number";
    private static final String PARAMETER_CANCEL = "cancel";
    private static final String PARAMETER_NEWSLETTER_NAME = "newsletter_name";
    private static final String PARAMETER_NEWSLETTER_DESCRIPTION = "newsletter_description";
    private static final String PARAMETER_NEWSLETTER_SENDER_MAIL = "newsletter_sender_mail";
    private static final String PARAMETER_NEWSLETTER_SENDER_NAME = "newsletter_sender_name";
    private static final String PARAMETER_DATE_FIRST_SEND = "date_first_send";
    private static final String PARAMETER_DATE_LAST_SEND = "date_last_send";
    private static final String PARAMETER_SUBSCRIBER_ID = "subscriber_id";
    private static final String PARAMETER_NEWSLETTER_OBJECT = "newsletter_object";
    private static final String PARAMETER_GENERATE = "generate";
    private static final String PARAMETER_HTML_CONTENT = "html_content";
    private static final String PARAMETER_SUBSCRIBERS_FILE = "newsletter_import_path";
    private static final String PARAMETER_NEWSLETTER_WORKGROUP = "newsletter_workgroup";
    private static final String PARAMETER_TERM_OF_SERVICE = "tos";
    private static final String PARAMETER_NEWSLETTER_UNSUBSCRIBE = "newsletter_unsubscribe";
    private static final String PARAMETER_PAGE_INDEX = "page_index";
    private static final String PARAMETER_TEST_RECIPIENTS = "newsletter_test_recipients";
    private static final String PARAMETER_TEST_SUBJECT = "newsletter_test_subject";
    private static final String PARAMETER_ACTIVE_CAPTCHA = "active_captcha";
    private static final String PARAMETER_ACTIVE_VALIDATION = "active_validation";
    private static final String PARAMETER_SUBSCRIBERS_SELECTION = "subscriber_selection";
    private static final String PARAMETER_TOPIC_ID = "topic_id";
    private static final String PARAMETER_NEWSLETTER_TOPICS_TABLE_MANAGER = "newsletter_topics_table_manager";
    private static final String PARAMETER_MOVE_UP = "move_up";
    private static final String PARAMETER_TITLE = "title";
    private static final String PARAMETER_UPDATE_TEMPLATE = "update_template";
    private static final String PARAMETER_NEWSLETTER_SELECTION = "newsletter_selection";

    // URL
    private static final String JSP_URL_DO_COMPOSE_NEWSLETTER = "ComposeNewsLetter.jsp";
    private static final String JSP_URL_DO_PREPARE_NEWSLETTER = "DoPrepareNewsLetter.jsp";
    private static final String JSP_URL_CONFIRM_TEST_NEWSLETTER = "ConfirmTestNewsLetter.jsp";
    private static final String JSP_URL_DO_REMOVE_NEWSLETTER = "jsp/admin/plugins/newsletter/DoRemoveNewsLetter.jsp";
    private static final String JSP_URL_DO_REMOVE_SENDING_NEWSLETTER = "jsp/admin/plugins/newsletter/DoRemoveSendingNewsLetter.jsp";
    private static final String JSP_URL_DO_REMOVE_MULTI_NEWSLETTER = "jsp/admin/plugins/newsletter/DoRemoveMultiNewsLetter.jsp";
    private static final String JSP_URL_DO_REMOVE_SUBSCRIBER = "jsp/admin/plugins/newsletter/DoUnsubscribeNewsLetterAdmin.jsp";
    private static final String JSP_URL_DO_REMOVE_SELECTION = "jsp/admin/plugins/newsletter/DoRemoveSelection.jsp";
    private static final String JSP_URL_MANAGE_NEWSLETTER = "ManageNewsLetter.jsp";
    private static final String JSP_URL_MANAGE_SUBSCRIBERS = "ManageSubscribers.jsp";
    private static final String JSP_URL_MANAGE_ARCHIVE = "ManageArchive.jsp";
    private static final String JSP_URL_PREPARE_NEWSLETTER = "PrepareNewsLetter.jsp";
    private static final String JSP_URL_SEND_NEWSLETTER = "jsp/admin/plugins/newsletter/DoSendNewsLetter.jsp";
    private static final String JSP_URL_TEST_NEWSLETTER = "jsp/admin/plugins/newsletter/DoTestNewsLetter.jsp";
    private static final String JSP_URL_MANAGE_NEWSLETTER_TOPIC = "jsp/admin/plugins/newsletter/GetManageNewsletterTopics.jsp";
    private static final String JSP_URL_MODIFY_TOPIC_CONFIG = "GetModifyTopicConfig.jsp";
    private static final String JSP_URL_DO_REMOVE_TOPIC = "jsp/admin/plugins/newsletter/DoRemoveNewsletterTopic.jsp";
    private static final String JSP_URL_MANAGE_TOPICS = "GetManageNewsletterTopics.jsp";
    private static final String JSP_URL_MODIFY_NEWSLETTER = "ModifyNewsLetter.jsp";

    // messages
    private static final String MESSAGE_CONFIRM_TEST_NEWSLETTER = "newsletter.message.confirmTestNewsletter";
    private static final String MESSAGE_LINKED_TO_NEWSLETTER = "newsletter.message.linkedPortlet";
    private static final String MESSAGE_CONFIRM_CANCEL_COMPOSE = "newsletter.message.confirmCancelComposeNewsletter";
    private static final String MESSAGE_CONFIRM_REMOVE_NEWSLETTER = "newsletter.message.confirmRemoveNewsletter";
    private static final String MESSAGE_CONFIRM_REMOVE_SENDING_NEWSLETTER = "newsletter.message.confirmRemoveSendingNewsletter";
    private static final String MESSAGE_CONFIRM_REMOVE_MULTI_SENDING_NEWSLETTER = "newsletter.message.confirmRemoveMultiSendingNewsletter";
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
    private static final String MESSAGE_NO_TEMPLATE = "newsletter.message.noTemplate";
    private static final String MESSAGE_OBJECT_NOT_SPECIFIED = "newsletter.message.noObjectSpecified";
    private static final String MESSAGE_SUBSCRIBERS_CLEANED = "newsletter.message.subscribersCleaned";
    private static final String MESSAGE_PAGE_TITLE_MANAGE_TOPICS = "newsletter.manage_topics.pageTitle";
    private static final String MESSAGE_CONFIRM_REMOVE_TOPIC = "newsletter.manage_topics.confirmRemoveTopic";
    private static final String MESSAGE_FRAGMENT_NO_CHANGE = "newsletter.message.fragment_no_change";
    private static final String MESSAGE_USER_NOT_ALLOWED_NEWSLETTER_PROPERTIES = "Newsletter properties : user not allowed to access this feature : ";

    private static final String PROPERTY_PAGE_TITLE_IMPORT = "newsletter.import_subscribers.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_IMPORT_DELETE = "newsletter.import_delete_subscribers.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_NEWSLETTERS = "newsletter.manage_newsletters.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_ARCHIVE = "newsletter.manage_archive.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_NEWSLETTERS_PROPERTIES = "newsletter.manage_newsletters_properties.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE = "newsletter.create_newsletter.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_TOPIC_CONFIGURATION = "newsletter.modify_topic_config.pageTitle";

    private static final String LABEL_NEWSLETTER_TOPIC_TITLE = "newsletter.manage_topics.labelTopicTitle";
    private static final String LABEL_NEWSLETTER_TOPIC_TYPE = "newsletter.manage_topics.labelTopicType";
    private static final String LABEL_NEWSLETTER_TOPIC_ORDER = "newsletter.manage_topics.labelTopicOrder";
    private static final String LABEL_NEWSLETTER_TOPIC_SECTION = "newsletter.manage_topics.labelTopicSection";
    private static final String LABEL_NEWSLETTER_ACTION = "newsletter.manage_topics.labelActions";
    private static final String LABLE_COPY_NEWSLETTER_TITLE = "newsletter.manage_newsletters.copy.title";

    private static final String PROPERTY_PAGE_TITLE_COMPOSE = "newsletter.compose_newsletter.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY = "newsletter.modify_newsletter.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MANAGE_SUBSCRIBERS = "newsletter.manage_subscribers.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_PREPARE = "newsletter.prepare_newsletter.pageTitle";
    private static final String PROPERTY_USERS_PER_PAGE = "paginator.user.itemsPerPage";
    private static final String PROPERTY_NEWSLETTERS_PER_PAGE = "newsletter.newslettersPerPage";
    private static final String PROPERTY_LABEL_UNSUBSCRIBE_TRUE = "newsletter.unsubscribe.true";
    private static final String PROPERTY_LABEL_UNSUBSCRIBE_FALSE = "newsletter.unsubscribe.false";
    private static final String PROPERTY_TEST_SUBJECT = "newsletter.test.subject";
    private static final String PROPERTY_ITEMS_PER_PAGE = "newsletter.itemsPerPage";

    private static final int CONSTANT_DEFAULT_ITEM_PER_PAGE = 50;
    private static final String CONSTANT_FREEMARKER_MACRO_COLUMN_SECTION = "getSectionColumn";

    // constants
    private static final String JCAPTCHA_PLUGIN = "jcaptcha";
    private static final int DEFAULT_LIMIT = 7;
    private int _nItemsPerPage;
    private int _nDefaultItemsPerPage;
    private String _strCurrentPageIndex;
    private String [ ] _multiSelectionValues;
    private NewsletterService _newsletterService = NewsletterService.getService( );
    private NewsletterTopicService _newsletterTopicService = NewsletterTopicService.getService( );

    /**
     * Creates a new NewsletterJspBean object.
     */
    public NewsletterJspBean( )
    {
        _nDefaultItemsPerPage = AppPropertiesService.getPropertyInt( PROPERTY_NEWSLETTERS_PER_PAGE, 10 );
    }

    /**
     * Returns the list of newsletters
     * 
     * @param request
     *            the HTTP request
     * @return the html code for display the newsletters list
     */
    public String getManageNewsLetters( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_NEWSLETTERS );
        _strCurrentPageIndex = Paginator.getPageIndex( request, Paginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );
        _nItemsPerPage = Paginator.getItemsPerPage( request, Paginator.PARAMETER_ITEMS_PER_PAGE, _nItemsPerPage, _nDefaultItemsPerPage );

        Map<String, Object> model = new HashMap<String, Object>( );
        Collection<NewsLetter> listNewsletter = NewsLetterHome.findAll( getPlugin( ) );
        listNewsletter = AdminWorkgroupService.getAuthorizedCollection( listNewsletter, getUser( ) );

        Collection<Map<String, Object>> listNewsletterDisplay = new ArrayList<Map<String, Object>>( );

        for ( NewsLetter newsletter : listNewsletter )
        {
            Map<String, Object> newsletterDisplay = new HashMap<String, Object>( );
            newsletterDisplay.put( MARK_NEWSLETTER, newsletter );
            newsletterDisplay.put( MARK_NEWSLETTER_ALLOW_CREATION,
                    RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_CREATE, getUser( ) ) );

            newsletterDisplay.put( MARK_NEWSLETTER_ALLOW_MANAGE_ARCHIVE,
                    RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_ARCHIVE, getUser( ) ) );

            newsletterDisplay.put( MARK_NEWSLETTER_ALLOW_DELETION,
                    RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_DELETE, getUser( ) ) );
            newsletterDisplay.put( MARK_NEWSLETTER_ALLOW_MANAGE_SUBSCRIBERS,
                    RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MANAGE_SUBSCRIBERS, getUser( ) ) );
            newsletterDisplay.put( MARK_NEWSLETTER_ALLOW_EXPORT_SUBSCRIBERS,
                    RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_EXPORT_SUBSCRIBERS, getUser( ) ) );
            newsletterDisplay.put( MARK_NEWSLETTER_ALLOW_MODIFICATION,
                    RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MODIFY, getUser( ) ) );
            newsletterDisplay.put( MARK_NEWSLETTER_ALLOW_SENDING,
                    RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_SEND, getUser( ) ) );

            // The workgroup description is needed for coherence and not the key
            if ( newsletter.getWorkgroup( ).equals( NewsLetterConstants.ALL_GROUPS ) )
            {
                newsletterDisplay.put( MARK_NEWSLETTER_WORKGROUP_DESCRIPTION,
                        I18nService.getLocalizedString( NewsLetterConstants.PROPERTY_LABEL_ALL_GROUPS, getLocale( ) ) );
            }
            else
            {
                newsletterDisplay.put( MARK_NEWSLETTER_WORKGROUP_DESCRIPTION,
                        AdminWorkgroupHome.findByPrimaryKey( newsletter.getWorkgroup( ) ).getDescription( ) );
            }

            newsletterDisplay.put( MARK_NEWSLETTER_COUNT_SUBSCRIBERS, NewsLetterHome.findNbrSubscribers( newsletter.getId( ), getPlugin( ) ) );
            listNewsletterDisplay.add( newsletterDisplay );
        }

        IPaginator<Map<String, Object>> paginator = new Paginator<Map<String, Object>>( (List<Map<String, Object>>) listNewsletterDisplay, _nItemsPerPage,
                getHomeUrl( request ), PARAMETER_PAGE_INDEX, _strCurrentPageIndex );

        model.put( MARK_NEWSLETTER_LIST, paginator.getPageItems( ) );
        model.put( MARK_PAGINATOR, paginator );
        model.put( MARK_ALLOW_CREATION, isNewsletterCreationAllowed( request ) );
        model.put( MARK_NB_ITEMS_PER_PAGE, StringUtils.EMPTY + _nItemsPerPage );
        model.put( MARK_RIGHT_MANAGE_NEWSLETTER_PROPERTIES, RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID,
                NewsletterResourceIdService.PERMISSION_NEWSLETTER_ADVANCED_SETTINGS, getUser( ) ) );

        // Collection refListAllTemplates = NewsLetterTemplateHome.getTemplatesList( getPlugin( ) );
        HtmlTemplate templateList = AppTemplateService.getTemplate( TEMPLATE_MANAGE_NEWSLETTERS, getLocale( ), model );

        return getAdminPage( templateList.getHtml( ) );
    }

    /**
     * Returns the list of old newsletters
     * 
     * @param request
     *            the HTTP request
     * @return the html code for display the newsletters list
     */
    public String getManageArchive( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_ARCHIVE );
        _strCurrentPageIndex = Paginator.getPageIndex( request, Paginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );
        _nItemsPerPage = Paginator.getItemsPerPage( request, Paginator.PARAMETER_ITEMS_PER_PAGE, _nItemsPerPage, _nDefaultItemsPerPage );

        int nIdNewsletter = Integer.parseInt( request.getParameter( PARAMETER_NEWSLETTER_ID ) );
        Map<String, Object> model = new HashMap<String, Object>( );
        List<SendingNewsLetter> listNewsletter = SendingNewsLetterHome.findAllLastSendingForNewsletterId( nIdNewsletter, getPlugin( ) );

        UrlItem url = new UrlItem( request.getRequestURI( ) );
        url.addParameter( PARAMETER_NEWSLETTER_ID, nIdNewsletter );

        IPaginator<SendingNewsLetter> paginator = new Paginator<SendingNewsLetter>( listNewsletter, _nItemsPerPage, url.getUrl( ), PARAMETER_PAGE_INDEX,
                _strCurrentPageIndex );

        model.put( MARK_NEWSLETTER_LIST, paginator.getPageItems( ) );
        model.put( MARK_PAGINATOR, paginator );
        model.put( MARK_NEWSLETTER_ID, nIdNewsletter );
        model.put( MARK_NB_ITEMS_PER_PAGE, StringUtils.EMPTY + _nItemsPerPage );

        HtmlTemplate templateList = AppTemplateService.getTemplate( TEMPLATE_MANAGE_OLD_NEWSLETTERS, getLocale( ), model );

        return getAdminPage( templateList.getHtml( ) );
    }

    /**
     * Returns the newsletters properties
     * 
     * @param request
     *            the HTTP request
     * @return the html code for display the newsletters list
     * @throws AccessDeniedException
     *             If the user is not allowed to acces the feature
     */
    public String getManageNewsLettersProperties( HttpServletRequest request ) throws AccessDeniedException
    {
        if ( !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID,
                NewsletterResourceIdService.PERMISSION_NEWSLETTER_ADVANCED_SETTINGS, getUser( ) ) )
        {
            throw new AccessDeniedException( MESSAGE_USER_NOT_ALLOWED_NEWSLETTER_PROPERTIES + AdminUserService.getAdminUser( request ).getAccessCode( ) );
        }

        setPageTitleProperty( PROPERTY_PAGE_TITLE_NEWSLETTERS_PROPERTIES );

        Map<String, Object> model = new HashMap<String, Object>( );

        NewsLetterProperties properties = NewsletterPropertiesHome.find( getPlugin( ) );

        model.put( MARK_IS_ACTIVE_CAPTCHA, PluginService.isPluginEnable( JCAPTCHA_PLUGIN ) );
        model.put( MARK_WEBAPP_URL, AppPathService.getBaseUrl( request ) );
        model.put( MARK_LOCALE, getLocale( ).getLanguage( ) );
        model.put( MARK_PROPERTIES, properties );
        model.put( MARK_CLEAN_RIGHT,
                RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, null, NewsletterResourceIdService.PERMISSION_CLEAN_SUBSCRIBERS, getUser( ) ) );

        HtmlTemplate templateList = AppTemplateService.getTemplate( TEMPLATE_MANAGE_NEWSLETTERS_PROPERTIES, getLocale( ), model );

        return getAdminPage( templateList.getHtml( ) );
    }

    /**
     * Processes the update form of the newsletter properties
     * 
     * @param request
     *            The Http request
     * @return The jsp URL which displays the view of all newsletter
     * @throws AccessDeniedException
     *             If the user is not allowed to acces the feature
     */
    public String doManageNewsLetterProperties( HttpServletRequest request ) throws AccessDeniedException
    {
        if ( !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID,
                NewsletterResourceIdService.PERMISSION_NEWSLETTER_ADVANCED_SETTINGS, getUser( ) ) )
        {
            throw new AccessDeniedException( MESSAGE_USER_NOT_ALLOWED_NEWSLETTER_PROPERTIES + AdminUserService.getAdminUser( request ).getAccessCode( ) );
        }
        String strBaseUrl = AppPathService.getBaseUrl( request );
        String strActiveCaptcha = request.getParameter( PARAMETER_ACTIVE_CAPTCHA );
        String strActiveValidation = request.getParameter( PARAMETER_ACTIVE_VALIDATION );

        NewsLetterProperties properties = new NewsLetterProperties( );

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

        NewsletterPropertiesHome.update( properties, getPlugin( ) );

        UrlItem url = new UrlItem( JSP_URL_MANAGE_NEWSLETTER );

        return url.getUrl( );
    }

    /**
     * Returns the newsletter form for creation
     * 
     * @param request
     *            The Http request
     * @return the html code of the newsletter form
     */
    public String getCreateNewsLetter( HttpServletRequest request )
    {
        // RBAC permission
        if ( !isNewsletterCreationAllowed( request ) )
        {
            return getManageNewsLetters( request );
        }

        setPageTitleProperty( PROPERTY_PAGE_TITLE_CREATE );

        Map<String, Object> model = new HashMap<String, Object>( );

        // get the list of document lists
        ReferenceList listUnsubscribe = new ReferenceList( );
        listUnsubscribe.addItem( NewsLetterConstants.PROPERTY_UNSUBSCRIBE_TRUE,
                I18nService.getLocalizedString( PROPERTY_LABEL_UNSUBSCRIBE_TRUE, getLocale( ) ) );
        listUnsubscribe.addItem( NewsLetterConstants.PROPERTY_UNSUBSCRIBE_FALSE,
                I18nService.getLocalizedString( PROPERTY_LABEL_UNSUBSCRIBE_FALSE, getLocale( ) ) );

        String strBaseUrl = AppPathService.getProdUrl( request );

        model.put( MARK_DATE_LAST_SEND, DateUtil.getCurrentDateString( getLocale( ) ) );
        model.put( MARK_WORKGROUP_LIST, AdminWorkgroupService.getUserWorkgroups( getUser( ), getLocale( ) ) );
        model.put( MARK_UNSUBSCRIBE_LIST, listUnsubscribe );
        model.put( MARK_LOCALE, getLocale( ) );
        model.put( MARK_WEBAPP_URL, strBaseUrl );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_CREATE_NEWSLETTER, getLocale( ), model );

        return getAdminPage( template.getHtml( ) );
    }

    /**
     * Returns the newsletter form of newsletter composition
     * 
     * @param request
     *            The Http rquest
     * @return the html code of the newsletter composition form
     */
    public String getComposeNewsLetter( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsLetterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsLetterId, getPlugin( ) );
        AdminUser user = getUser( );

        // RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser( ) )
                || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId, NewsletterResourceIdService.PERMISSION_MODIFY, getUser( ) ) )
        {
            return getManageNewsLetters( request );
        }

        setPageTitleProperty( PROPERTY_PAGE_TITLE_COMPOSE );

        String strBaseUrl = AppPathService.getProdUrl( request );

        String strPathImageTemplate = _newsletterService.getImageFolderPath( AppPathService.getBaseUrl( request ) );

        Map<String, Object> model = new HashMap<String, Object>( );

        // Fills the template with specific values
        String strGenerate = request.getParameter( PARAMETER_GENERATE );

        int nTemplateNewsLetterId = newsletter.getNewsLetterTemplateId( );
        String strHtmlContent = null;

        if ( ( strGenerate == null ) )
        {

            strHtmlContent = ( newsletter.getHtml( ) == null ) ? NewsLetterConstants.CONSTANT_EMPTY_STRING : newsletter.getHtml( );
        }
        else
        {
            if ( nTemplateNewsLetterId == 0 )
            {
                Collection<NewsLetterTemplate> newsletterTemplatesList = NewsLetterTemplateHome.getTemplatesCollectionByType( NewsLetterTemplate.RESOURCE_TYPE,
                        getPlugin( ) );
                newsletterTemplatesList = AdminWorkgroupService.getAuthorizedCollection( newsletterTemplatesList, getUser( ) );

                for ( NewsLetterTemplate template : newsletterTemplatesList )
                {
                    if ( StringUtils.equals( template.getResourceTypeCode( ), NewsLetter.RESOURCE_TYPE ) )
                    {
                        nTemplateNewsLetterId = template.getId( );
                        break;
                    }
                }
            }
            if ( nTemplateNewsLetterId > 0 )
            {
                strHtmlContent = _newsletterService.generateNewsletterHtmlCode( newsletter, nTemplateNewsLetterId, strBaseUrl, user, getLocale( ) );
            }
            if ( strHtmlContent == null )
            {
                strHtmlContent = NewsLetterConstants.CONSTANT_EMPTY_STRING; // if no template available (newsletter and/or document), return an empty html
                                                                            // content
            }
        }

        strHtmlContent = strHtmlContent.replaceAll( NewsLetterConstants.MARK_BASE_URL, strBaseUrl );

        strHtmlContent = strHtmlContent.replaceAll( NewsLetterConstants.WEBAPP_PATH_FOR_LINKSERVICE, strBaseUrl );

        model.put( MARK_HTML_CONTENT, strHtmlContent );

        model.put( MARK_NEWSLETTER, newsletter );
        model.put( MARK_NEWSLETTER_TEMPLATE_ID, nTemplateNewsLetterId );
        model.put( MARK_REGISTER_ACTION, AppPropertiesService.getProperty( PROPERTY_REGISTER_ACTION ) );
        model.put( MARK_PREPARE_SENDING_ACTION, AppPropertiesService.getProperty( PROPERTY_PREPARE_SENDING_ACTION ) );
        model.put( MARK_CANCEL_ACTION, AppPropertiesService.getProperty( PROPERTY_CANCEL_ACTION ) );

        model.put( MARK_WEBAPP_URL, AppPathService.getBaseUrl( request ) );
        model.put( MARK_LOCALE, getLocale( ).getLanguage( ) );
        model.put( MARK_IMG_PATH, strPathImageTemplate );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_COMPOSE_NEWSLETTER, getLocale( ), model );

        return getAdminPage( template.getHtml( ) );
    }

    /**
     * Returns the newsletter form of newsletter composition
     * 
     * @param request
     *            The Http rquest
     * @return the html code of the newsletter composition form
     */
    public String doComposeNewsLetter( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsLetterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsLetterId, getPlugin( ) );

        // Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser( ) )
                || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId, NewsletterResourceIdService.PERMISSION_MODIFY, getUser( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        Collection<NewsLetterTemplate> newsletterTemplatesList = NewsLetterTemplateHome.getTemplatesCollectionByType( NewsLetterTemplate.RESOURCE_TYPE,
                getPlugin( ) );

        // composition not possible if not at least one template for newsletter
        if ( ( newsletterTemplatesList.size( ) == 0 ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_NO_TEMPLATE, AdminMessage.TYPE_STOP );
        }

        UrlItem url = new UrlItem( JSP_URL_DO_COMPOSE_NEWSLETTER );
        url.addParameter( PARAMETER_NEWSLETTER_ID, strNewsletterId );

        return url.getUrl( );
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
        // RBAC permissions, the user must have the right "clean subscribers" on all newsletters
        if ( !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, null, NewsletterResourceIdService.PERMISSION_CLEAN_SUBSCRIBERS, getUser( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        NewsLetterRegistrationService.getInstance( ).doRemoveOldUnconfirmed( );

        int nConfirmLimit = AppPropertiesService.getPropertyInt( PROPERTY_LIMIT_CONFIRM_DAYS, DEFAULT_LIMIT );
        Object [ ] messages = new String [ 1];
        messages [0] = Integer.toString( nConfirmLimit );

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
        String strSenderName = request.getParameter( PARAMETER_NEWSLETTER_SENDER_NAME );
        String strSenderMail = request.getParameter( PARAMETER_NEWSLETTER_SENDER_MAIL );
        String strTestRecipients = request.getParameter( PARAMETER_TEST_RECIPIENTS );
        String strTestSubject = request.getParameter( PARAMETER_TEST_SUBJECT );

        // RBAC permission
        if ( !isNewsletterCreationAllowed( request ) )
        {
            return getManageNewsLetters( request );
        }

        // Mandatory fields
        if ( StringUtils.isEmpty( strSenderMail ) || StringUtils.isEmpty( strTestRecipients ) || StringUtils.isEmpty( strNewsletterName )
                || StringUtils.isEmpty( strDateFirstSend ) || StringUtils.isEmpty( strWorkGroup ) || StringUtils.isEmpty( strSenderName ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
        }

        String strMessageParam = null;
        String strMessage = null;

        String strWrongEmail = isWrongEmail( strTestRecipients );
        if ( StringUtils.isNotEmpty( strWrongEmail ) )
        {
            strMessageParam = strWrongEmail;
            strMessage = MESSAGE_WRONG_EMAIL;
        }

        if ( strMessageParam == null && strMessage == null && !StringUtil.checkEmail( strSenderMail ) )
        {
            strMessageParam = strSenderMail;
            strMessage = MESSAGE_WRONG_EMAIL_SENDER;
        }

        if ( strMessageParam != null && strMessage != null )
        {
            Object [ ] messageArgs = {
                    strMessageParam
            };
            return AdminMessageService.getMessageUrl( request, strMessage, messageArgs, AdminMessage.TYPE_STOP );
        }

        NewsLetter newsletter = new NewsLetter( );
        newsletter.setName( strNewsletterName );

        Timestamp dateFirstSend = DateUtil.formatTimestamp( strDateFirstSend, getLocale( ) );

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
        newsletter.setNbSections( 1 );
        newsletter.setUnsubscribe( request.getParameter( PARAMETER_NEWSLETTER_UNSUBSCRIBE ) );

        NewsLetterHome.create( newsletter, getPlugin( ) );

        return getHomeUrl( request );
    }

    /**
     * Returns the newsletter form for modification
     * 
     * @param request
     *            The Http request
     * @return the html code of the newsletter form
     */
    public String getModifyNewsLetter( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_MODIFY );

        Map<String, Object> model = new HashMap<String, Object>( );
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin( ) );

        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser( ) )
                || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId, NewsletterResourceIdService.PERMISSION_MODIFY, getUser( ) ) )
        {
            return getManageNewsLetters( request );
        }

        ReferenceList listUnsubscribe = new ReferenceList( );
        listUnsubscribe.addItem( "TRUE", I18nService.getLocalizedString( PROPERTY_LABEL_UNSUBSCRIBE_TRUE, getLocale( ) ) );
        listUnsubscribe.addItem( "FALSE", I18nService.getLocalizedString( PROPERTY_LABEL_UNSUBSCRIBE_FALSE, getLocale( ) ) );

        String strBaseUrl = AppPathService.getProdUrl( request );

        model.put( MARK_NEWSLETTER, newsletter );
        model.put( MARK_NEWSLETTER_DESCRIPTION, newsletter.getDescription( ) );
        model.put( MARK_DATE_LAST_SENDING, DateUtil.getDateString( newsletter.getDateLastSending( ), getLocale( ) ) );
        model.put( MARK_WORKGROUP_LIST, AdminWorkgroupService.getUserWorkgroups( getUser( ), getLocale( ) ) );
        model.put( MARK_UNSUBSCRIBE_LIST, listUnsubscribe );
        model.put( MARK_LOCALE, getLocale( ) );
        model.put( MARK_WEBAPP_URL, strBaseUrl );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MODIFY_NEWSLETTER, getLocale( ), model );

        return getAdminPage( template.getHtml( ) );
    }

    /**
     * Processes the update form of the newsletter whose identifier is in the http request
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
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin( ) );
        String strTestSubject = request.getParameter( PARAMETER_TEST_SUBJECT );

        // RBAC permission
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser( ) )
                || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId, NewsletterResourceIdService.PERMISSION_MODIFY, getUser( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        // Mandatory fields
        if ( StringUtils.isEmpty( strSenderMail ) || StringUtils.isEmpty( strTestRecipients ) || StringUtils.isEmpty( strNewsletterName )
                || StringUtils.isEmpty( strDateLastSend ) || StringUtils.isEmpty( strWorkGroup ) || StringUtils.isEmpty( strSenderName ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
        }

        strTestRecipients = cleanEmails( strTestRecipients );

        String strWrongMail = isWrongEmail( strTestRecipients );
        if ( StringUtils.isNotEmpty( strWrongMail ) )
        {
            Object [ ] messageArgs = {
                    strWrongMail
            };

            return AdminMessageService.getMessageUrl( request, MESSAGE_WRONG_EMAIL, messageArgs, AdminMessage.TYPE_STOP );
        }

        if ( !StringUtil.checkEmail( strSenderMail ) )
        {
            Object [ ] messageArgs = {
                    strSenderMail
            };

            return AdminMessageService.getMessageUrl( request, MESSAGE_WRONG_EMAIL_SENDER, messageArgs, AdminMessage.TYPE_STOP );
        }

        newsletter.setName( strNewsletterName );
        newsletter.setDescription( strNewsletterDescription );
        newsletter.setWorkgroup( strWorkGroup );
        newsletter.setTestRecipients( strTestRecipients );
        newsletter.setTestSubject( strTestSubject );
        newsletter.setNewsletterSenderMail( strSenderMail );
        newsletter.setNewsletterSenderName( strSenderName );

        Timestamp dateLastSend = DateUtil.formatTimestamp( strDateLastSend, getLocale( ) );

        if ( dateLastSend != null )
        {
            newsletter.setDateLastSending( dateLastSend );
        }

        newsletter.setUnsubscribe( request.getParameter( PARAMETER_NEWSLETTER_UNSUBSCRIBE ) );
        newsletter.setWorkgroup( request.getParameter( PARAMETER_NEWSLETTER_WORKGROUP ) );

        // if not, newsletter.getDateLastSending keeps its value
        NewsLetterHome.update( newsletter, getPlugin( ) );

        String strId = Integer.toString( nNewsletterId );
        UrlItem url = new UrlItem( JSP_URL_MODIFY_NEWSLETTER );
        url.addParameter( PARAMETER_NEWSLETTER_ID, strId );

        return url.getUrl( );
    }

    /**
     * Remove spaces from a recipient list
     * 
     * @param strRecipientLists
     *            The recipient list
     * @return the recipient list without spaces before and after each email.
     */
    private String cleanEmails( String strRecipientLists )
    {
        StringBuffer strCleanTestRecipients = new StringBuffer( );
        String strDelimiter = AppPropertiesService.getProperty( PROPERTY_IMPORT_DELIMITER );

        String [ ] strEmails = strRecipientLists.split( strDelimiter );

        for ( String email : strEmails )
        {
            strCleanTestRecipients.append( email.trim( ) );
            strCleanTestRecipients.append( strDelimiter );
        }

        return strCleanTestRecipients.toString( );
    }

    /**
     * Manages the removal form of a newsletter whose identifier is in the http request
     * 
     * @param request
     *            The Http request
     * @return the html code to confirm
     */
    public String getConfirmRemoveSubscriber( HttpServletRequest request )
    {
        UrlItem urlItem = new UrlItem( JSP_URL_DO_REMOVE_SUBSCRIBER );
        int nNewsletterId = Integer.parseInt( request.getParameter( PARAMETER_NEWSLETTER_ID ) );
        int nSubscriberId = Integer.parseInt( request.getParameter( PARAMETER_SUBSCRIBER_ID ) );
        urlItem.addParameter( PARAMETER_NEWSLETTER_ID, nNewsletterId );
        urlItem.addParameter( PARAMETER_SUBSCRIBER_ID, nSubscriberId );

        return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_SUBSCRIBER, urlItem.getUrl( ), AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Manages the removal form of a newsletter whose identifier is in the http for selected users request
     * 
     * @param request
     *            The Http request
     * @return the html code to confirm
     */
    public String getConfirmRemoveSelectedSubscribers( HttpServletRequest request )
    {
        String [ ] strIdSubscribers = (String [ ]) request.getParameterMap( ).get( PARAMETER_SUBSCRIBERS_SELECTION );
        _multiSelectionValues = strIdSubscribers;

        UrlItem urlItem = new UrlItem( JSP_URL_DO_REMOVE_SELECTION );
        int nNewsletterId = Integer.parseInt( request.getParameter( PARAMETER_NEWSLETTER_ID ) );
        urlItem.addParameter( PARAMETER_NEWSLETTER_ID, nNewsletterId );

        return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_SELECTION, urlItem.getUrl( ), AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Processes the unregistration of a subscriber for a newsletter
     * 
     * @param request
     *            The Http request
     * @return the jsp URL to display the form to manage newsletters
     */
    public String doUnregistrationAdmin( HttpServletRequest request )
    {
        /* parameters */
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        int nSubscriberId = Integer.parseInt( request.getParameter( PARAMETER_SUBSCRIBER_ID ) );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin( ) );

        // RBAC permission
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser( ) ) || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                NewsletterResourceIdService.PERMISSION_MANAGE_SUBSCRIBERS, getUser( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        Subscriber subscriber = SubscriberHome.findByPrimaryKey( nSubscriberId, getPlugin( ) );

        if ( subscriber != null )
        {
            _newsletterService.removeSubscriberFromNewsletter( subscriber, nNewsletterId, getPlugin( ) );
        }

        UrlItem urlItem = new UrlItem( JSP_URL_MANAGE_SUBSCRIBERS );
        urlItem.addParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_ID, nNewsletterId );

        return urlItem.getUrl( );
    }

    /**
     * Processes the unregistration of selected subscribers
     * 
     * @param request
     *            The Http request
     * @return the jsp URL to display the form to manage newsletters
     */
    public String doRemoveSelection( HttpServletRequest request )
    {
        /* parameters */
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin( ) );

        // RBAC permission
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser( ) ) || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                NewsletterResourceIdService.PERMISSION_MANAGE_SUBSCRIBERS, getUser( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        if ( ( _multiSelectionValues != null ) && ( _multiSelectionValues.length > 0 ) )
        {
            for ( String strId : _multiSelectionValues )
            {
                Subscriber subscriber = SubscriberHome.findByPrimaryKey( Integer.parseInt( strId ), getPlugin( ) );

                if ( subscriber != null )
                {
                    _newsletterService.removeSubscriberFromNewsletter( subscriber, nNewsletterId, getPlugin( ) );
                }
            }
        }

        UrlItem urlItem = new UrlItem( JSP_URL_MANAGE_SUBSCRIBERS );
        urlItem.addParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_ID, nNewsletterId );

        return urlItem.getUrl( );
    }

    /**
     * Manages the removal form of a newsletter whose identifier is in the http request
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
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin( ) );

        // RBAC permission
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser( ) )
                || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId, NewsletterResourceIdService.PERMISSION_DELETE, getUser( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        if ( NewsLetterHome.checkLinkedPortlets( nNewsletterId ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_LINKED_TO_NEWSLETTER, AdminMessage.TYPE_STOP );
        }

        UrlItem urlItem = new UrlItem( JSP_URL_DO_REMOVE_NEWSLETTER );
        urlItem.addParameter( PARAMETER_NEWSLETTER_ID, nNewsletterId );

        return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_NEWSLETTER, urlItem.getUrl( ), AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Manages the removal form of a newsletter archive whose identifier is in the http request
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

        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );

        // RBAC permission
        if ( !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, Integer.toString( nNewsletterId ), NewsletterResourceIdService.PERMISSION_ARCHIVE,
                getUser( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        UrlItem urlItem = new UrlItem( JSP_URL_DO_REMOVE_SENDING_NEWSLETTER );
        urlItem.addParameter( PARAMETER_NEWSLETTER_ID, nNewsletterId );
        urlItem.addParameter( PARAMETER_SENDING_NEWSLETTER_ID, nSendingNewsletterId );

        return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_SENDING_NEWSLETTER, urlItem.getUrl( ), AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Manages the removal form of a newsletter archive whose identifier is in the http request
     * 
     * @param request
     *            The Http request
     * @return the html code to confirm
     */
    public String getRemoveMultiNewsLetter( HttpServletRequest request )
    {
        /* parameters */
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );

        String [ ] strSendingNewsletterId = request.getParameterValues( PARAMETER_NEWSLETTER_SELECTION );

        // RBAC permission
        if ( !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, Integer.toString( nNewsletterId ), NewsletterResourceIdService.PERMISSION_ARCHIVE,
                getUser( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }
        else
            if ( ArrayUtils.isEmpty( strSendingNewsletterId ) )
            {
                return AdminMessageService.getMessageUrl( request, PROPERTY_SELECT_MANDATORY, AdminMessage.TYPE_ERROR );
            }

        StringBuilder sb = new StringBuilder( );
        int count = 1;

        for ( String s : strSendingNewsletterId )
        {
            sb.append( s );

            if ( count != strSendingNewsletterId.length )
            {
                count++;
                sb.append( "_" );
            }
        }

        UrlItem urlItem = new UrlItem( JSP_URL_DO_REMOVE_MULTI_NEWSLETTER );
        urlItem.addParameter( PARAMETER_NEWSLETTER_ID, nNewsletterId );
        urlItem.addParameter( PARAMETER_SENDING_NEWSLETTER_ID, sb.toString( ) );

        return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_MULTI_SENDING_NEWSLETTER, urlItem.getUrl( ), AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Processes the removal form of a newsletter
     * 
     * @param request
     *            The Http request
     * @return the jsp URL to display the form to manage newsletters
     */
    public String doRemoveNewsLetter( HttpServletRequest request )
    {
        int nNewsletterId = Integer.parseInt( request.getParameter( PARAMETER_NEWSLETTER_ID ) );

        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin( ) );

        // Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser( ) ) || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE,
                Integer.toString( newsletter.getId( ) ), NewsletterResourceIdService.PERMISSION_MODIFY, getUser( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        /* Looks for the list of the subscribers */
        Collection<Subscriber> list = SubscriberHome.findSubscribers( nNewsletterId, NewsLetterConstants.CONSTANT_EMPTY_STRING,
                Integer.parseInt( AppPropertiesService.getProperty( PROPERTY_LIMIT_MIN_SUSCRIBER ) ),
                Integer.parseInt( AppPropertiesService.getProperty( PROPERTY_LIMIT_MAX_SUSCRIBER ) ), getPlugin( ) );

        for ( Subscriber subscriber : list )
        {
            NewsLetterHome.removeSubscriber( newsletter.getId( ), subscriber.getId( ), getPlugin( ) );
        }

        // removes the newsletter
        NewsletterTopicHome.removeAllByIdNewsletter( nNewsletterId, getPlugin( ) );
        NewsLetterHome.remove( nNewsletterId, getPlugin( ) );

        return getHomeUrl( request );
    }

    /**
     * Processes the removal form of a sending newsletter
     * 
     * @param request
     *            The Http request
     * @return the jsp URL to display the form to manage newsletters
     */
    public String doRemoveSendingNewsLetter( HttpServletRequest request )
    {
        int nNewsletterId = Integer.parseInt( request.getParameter( PARAMETER_NEWSLETTER_ID ) );
        int nSendingNewsletterId = Integer.parseInt( request.getParameter( PARAMETER_SENDING_NEWSLETTER_ID ) );

        if ( !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, Integer.toString( nNewsletterId ), NewsletterResourceIdService.PERMISSION_ARCHIVE,
                getUser( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        SendingNewsLetterHome.remove( nSendingNewsletterId, getPlugin( ) );

        UrlItem url = new UrlItem( JSP_URL_MANAGE_ARCHIVE );
        url.addParameter( PARAMETER_NEWSLETTER_ID, nNewsletterId );

        return url.getUrl( );
    }

    /**
     * Processes the removal form of a sending newsletter
     * 
     * @param request
     *            The Http request
     * @return the jsp URL to display the form to manage newsletters
     */
    public String doRemoveMultiNewsLetter( HttpServletRequest request )
    {
        int nNewsletterId = Integer.parseInt( request.getParameter( PARAMETER_NEWSLETTER_ID ) );
        String strSendingNewsletterId = request.getParameter( PARAMETER_SENDING_NEWSLETTER_ID );

        if ( !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, Integer.toString( nNewsletterId ), NewsletterResourceIdService.PERMISSION_ARCHIVE,
                getUser( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        String [ ] ids = strSendingNewsletterId.split( "_" );

        for ( String id : ids )
        {
            SendingNewsLetterHome.remove( Integer.parseInt( id ), getPlugin( ) );
        }

        UrlItem url = new UrlItem( JSP_URL_MANAGE_ARCHIVE );
        url.addParameter( PARAMETER_NEWSLETTER_ID, nNewsletterId );

        return url.getUrl( );
    }

    /**
     * Builds the newsletter's subscribers management page
     * 
     * @param request
     *            The HTTP request
     * @return the html code for newsletter's subscribers management page
     */
    public String getManageSubscribers( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsLetterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsLetterId, getPlugin( ) );
        NewsLetterProperties properties = NewsletterPropertiesHome.find( getPlugin( ) );

        // Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser( ) ) || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                NewsletterResourceIdService.PERMISSION_MANAGE_SUBSCRIBERS, getUser( ) ) )
        {
            return getManageNewsLetters( request );
        }

        setPageTitleProperty( PROPERTY_PAGE_TITLE_MANAGE_SUBSCRIBERS );

        String strSearchString = request.getParameter( NewsLetterConstants.PARAMETER_SUBSCRIBER_SEARCH );

        if ( StringUtils.isBlank( strSearchString ) )
        {
            strSearchString = StringUtils.EMPTY;
        }

        _strCurrentPageIndex = Paginator.getPageIndex( request, Paginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );
        _nDefaultItemsPerPage = AppPropertiesService.getPropertyInt( PROPERTY_USERS_PER_PAGE, 10 );
        _nItemsPerPage = Paginator.getItemsPerPage( request, Paginator.PARAMETER_ITEMS_PER_PAGE, _nItemsPerPage, _nDefaultItemsPerPage );

        // get a list of subscribers
        List<Subscriber> refListSubscribers = (List<Subscriber>) SubscriberHome.findSubscribers( nNewsLetterId, strSearchString,
                Integer.parseInt( AppPropertiesService.getProperty( PROPERTY_LIMIT_MIN_SUSCRIBER ) ),
                Integer.parseInt( AppPropertiesService.getProperty( PROPERTY_LIMIT_MAX_SUSCRIBER ) ), getPlugin( ) );
        UrlItem url = new UrlItem( request.getRequestURI( ) );
        url.addParameter( PARAMETER_NEWSLETTER_ID, nNewsLetterId );
        url.addParameter( NewsLetterConstants.PARAMETER_SUBSCRIBER_SEARCH, strSearchString );

        String strSortedAttributeName = request.getParameter( Parameters.SORTED_ATTRIBUTE_NAME );
        String strAscSort = null;

        if ( StringUtils.isNotBlank( strSortedAttributeName ) )
        {
            strAscSort = request.getParameter( Parameters.SORTED_ASC );
            url.addParameter( Parameters.SORTED_ATTRIBUTE_NAME, strSortedAttributeName );
            url.addParameter( Parameters.SORTED_ASC, strAscSort );

            boolean bIsAscSort = Boolean.parseBoolean( strAscSort );

            Collections.sort( refListSubscribers, new AttributeComparator( strSortedAttributeName, bIsAscSort ) );
        }

        IPaginator<Subscriber> paginator = new LocalizedPaginator<Subscriber>( refListSubscribers, _nItemsPerPage, url.getUrl( ),
                Paginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex, getLocale( ) );

        Map<String, Object> model = new HashMap<String, Object>( );
        model.put( MARK_NEWSLETTER, newsletter );
        model.put( MARK_NB_ITEMS_PER_PAGE, Integer.toString( _nItemsPerPage ) );
        model.put( MARK_SEARCH_STRING, strSearchString );
        model.put( MARK_PAGINATOR, paginator );
        model.put( MARK_SUBSCRIBERS_LIST, paginator.getPageItems( ) );
        model.put( MARK_DISPLAY_STATUS, properties.isValidationActive( ) );
        model.put( MARK_ADD_SUBSCRIBER_RIGHT,
                RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId, NewsletterResourceIdService.PERMISSION_ADD_SUBSCRIBER, getUser( ) ) );
        model.put( MARK_IMPORT_SUBSCRIBER_RIGHT,
                RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId, NewsletterResourceIdService.PERMISSION_IMPORT_SUBSCRIBERS, getUser( ) ) );
        model.put( MARK_EXPORT_SUBSCRIBER_RIGTH,
                RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_EXPORT_SUBSCRIBERS, getUser( ) ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MANAGE_SUBSCRIBERS, getLocale( ), model );

        return getAdminPage( template.getHtml( ) );
    }

    /**
     * Processes the registration of a subscriber
     * 
     * @param request
     *            The Http request
     * @return The jsp URL which displays the subscribers management page
     */
    public String doAddSubscriber( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsLetterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsLetterId, getPlugin( ) );

        // Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser( ) )
                || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId, NewsletterResourceIdService.PERMISSION_ADD_SUBSCRIBER, getUser( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        String strEmail = request.getParameter( NewsLetterConstants.PARAMETER_EMAIL );

        // Mandatory fields
        if ( ( strEmail == null ) || strEmail.equals( NewsLetterConstants.CONSTANT_EMPTY_STRING ) || !StringUtil.checkEmail( strEmail.trim( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_FIELD_EMAIL_VALID, AdminMessage.TYPE_STOP );
        }

        // Checks if a subscriber with the same email address doesn't exist yet
        Subscriber subscriber = SubscriberHome.findByEmail( strEmail, getPlugin( ) );

        if ( subscriber == null )
        {
            // The email doesn't exist, so create a new subcriber
            subscriber = new Subscriber( );
            subscriber.setEmail( strEmail.trim( ) );
            SubscriberHome.create( subscriber, getPlugin( ) );
        }

        // adds a subscriber to the current newsletter
        if ( NewsLetterHome.findRegistration( nNewsLetterId, subscriber.getId( ), getPlugin( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_EMAIL_EXISTS, AdminMessage.TYPE_STOP );
        }

        // the current date
        Timestamp tToday = new java.sql.Timestamp( new java.util.Date( ).getTime( ) );
        NewsLetterHome.addSubscriber( newsletter.getId( ), subscriber.getId( ), tToday, getPlugin( ) );

        // Returns the jsp URL to display the subscribers management page with
        // the new one
        UrlItem urlItem = new UrlItem( JSP_URL_MANAGE_SUBSCRIBERS );
        urlItem.addParameter( PARAMETER_NEWSLETTER_ID, nNewsLetterId );

        return urlItem.getUrl( );
    }

    /**
     * Builds the page of preparation before sending
     * 
     * @param request
     *            the http request
     * @return the html code for the preparation page
     */
    public String getPrepareNewsLetter( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin( ) );

        // Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser( ) )
                || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId, NewsletterResourceIdService.PERMISSION_SEND, getUser( ) ) )
        {
            return getManageNewsLetters( request );
        }

        setPageTitleProperty( PROPERTY_PAGE_TITLE_PREPARE );

        String strBaseUrl = AppPathService.getBaseUrl( request );
        Map<String, Object> model = new HashMap<String, Object>( );
        String strObject = request.getParameter( PARAMETER_NEWSLETTER_OBJECT );

        if ( strObject != null )
        {
            model.put( MARK_NEWSLETTER_OBJECT, strObject );
        }
        else
        {
            model.put( MARK_NEWSLETTER_OBJECT, StringUtils.EMPTY );
        }

        model.put( MARK_PREVIEW, newsletter.getHtml( ) );
        model.put( MARK_UNSUBSCRIBE, newsletter.getUnsubscribe( ) );
        model.put( MARK_NEWSLETTER, newsletter );
        model.put( NewsLetterConstants.MARK_BASE_URL, strBaseUrl );
        model.put( NewsLetterConstants.MARK_SUBSCRIBER_EMAIL, NewsLetterConstants.MARK_SUBSCRIBER_EMAIL_EACH );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_PREPARE_NEWSLETTER, getLocale( ), model );

        return getAdminPage( template.getHtml( ) );
    }

    /**
     * Builds the page of preparation before sending
     * 
     * @param request
     *            the Http request
     * @return the html code for the preparation page
     */
    public String doPrepareNewsLetter( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin( ) );

        // Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser( ) )
                || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId, NewsletterResourceIdService.PERMISSION_SEND, getUser( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        // allow to send only if the newsletter is not empty
        if ( StringUtils.isEmpty( newsletter.getHtml( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_SENDING_EMPTY_NOT_ALLOWED, AdminMessage.TYPE_STOP );
        }

        UrlItem urlItem = new UrlItem( JSP_URL_PREPARE_NEWSLETTER );
        urlItem.addParameter( PARAMETER_NEWSLETTER_ID, nNewsletterId );

        return urlItem.getUrl( );
    }

    /**
     * Builds the page of preparation before sending
     * 
     * @param request
     *            the http request
     * @return the html code for the preparation page
     */
    public String getPreviewNewsLetter( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin( ) );

        // Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser( ) )
                || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId, NewsletterResourceIdService.PERMISSION_SEND, getUser( ) ) )
        {
            return getManageNewsLetters( request );
        }

        String strBaseUrl = AppPathService.getBaseUrl( request );

        if ( !strBaseUrl.endsWith( NewsLetterConstants.CONSTANT_SLASH ) )
        {
            strBaseUrl += NewsLetterConstants.CONSTANT_SLASH;
        }

        HtmlTemplate templateNewsLetter = setHtmlTemplateEmail( newsletter, strBaseUrl, newsletter.getUnsubscribe( ) );

        return templateNewsLetter.getHtml( );
    }

    /**
     * Displays the confirmation page before sending the newsletter
     * 
     * @param request
     *            the http request
     * @return the html code for the confirmation page
     */
    public String doConfirmSendNewsLetter( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin( ) );

        // Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser( ) )
                || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId, NewsletterResourceIdService.PERMISSION_SEND, getUser( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        String strErrorMessage = null;

        // allow to send only if the newsletter is not empty
        if ( StringUtils.isEmpty( newsletter.getHtml( ) ) )
        {
            strErrorMessage = MESSAGE_SENDING_EMPTY_NOT_ALLOWED;
        }

        // allow to send only if at least one active subscriber
        int nNbrSubscribers = NewsLetterHome.findNbrActiveSubscribers( nNewsletterId, getPlugin( ) );

        if ( strErrorMessage == null && nNbrSubscribers == 0 )
        {
            strErrorMessage = MESSAGE_NO_SUBSCRIBER;
        }

        String strObject = request.getParameter( PARAMETER_NEWSLETTER_OBJECT );

        // Block access if no object for the newsletter specified
        if ( strErrorMessage == null && StringUtils.isEmpty( strObject ) )
        {
            strErrorMessage = MESSAGE_OBJECT_NOT_SPECIFIED;
        }

        if ( strErrorMessage != null )
        {
            return AdminMessageService.getMessageUrl( request, strErrorMessage, AdminMessage.TYPE_STOP );
        }

        UrlItem urlItem = new UrlItem( JSP_URL_SEND_NEWSLETTER );
        HashMap<String, String> requestedParameters = new HashMap<String, String>( );
        requestedParameters.put( PARAMETER_NEWSLETTER_OBJECT, strObject );
        requestedParameters.put( PARAMETER_NEWSLETTER_ID, strNewsletterId );

        // warn if the newletter html content is the same as the one of the last
        // sending for that newsletter
        SendingNewsLetter lastSending = SendingNewsLetterHome.findLastSendingForNewsletterId( nNewsletterId, getPlugin( ) );

        String strMessage = MESSAGE_CONFIRM_SEND_NEWSLETTER;
        if ( ( lastSending != null ) && lastSending.getHtml( ).equals( newsletter.getHtml( ) ) )
        {
            strMessage = MESSAGE_FRAGMENT_NO_CHANGE;
        }
        return AdminMessageService.getMessageUrl( request, strMessage, urlItem.getUrl( ), AdminMessage.TYPE_CONFIRMATION, requestedParameters );
    }

    /**
     * Displays the confirmation page before testing the newsletter
     * 
     * @param request
     *            the http request
     * @return the html code for the confirmation page
     */
    public String doConfirmTestNewsLetter( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin( ) );

        // Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser( ) )
                || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId, NewsletterResourceIdService.PERMISSION_MODIFY, getUser( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        // allow to send only if the newsletter is not empty
        if ( StringUtils.isEmpty( newsletter.getHtml( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_SENDING_EMPTY_NOT_ALLOWED, AdminMessage.TYPE_STOP );
        }

        String strObject = request.getParameter( PARAMETER_NEWSLETTER_OBJECT );

        UrlItem urlItem = new UrlItem( JSP_URL_TEST_NEWSLETTER );
        urlItem.addParameter( PARAMETER_NEWSLETTER_OBJECT, strObject );
        urlItem.addParameter( PARAMETER_NEWSLETTER_ID, nNewsletterId );

        return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_TEST_NEWSLETTER, urlItem.getUrl( ), AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Processes the testing of a newsletter
     * 
     * @param request
     *            the http request
     * @return the url of the confirmation page
     */
    public String doTestNewsLetter( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin( ) );

        // Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser( ) )
                || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId, NewsletterResourceIdService.PERMISSION_MODIFY, getUser( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        // Allow to send test if the list of test recipients is not empty
        String strTestRecipients = newsletter.getTestRecipients( );

        String strWrongEmail = isWrongEmail( strTestRecipients );
        if ( StringUtils.isNotEmpty( strWrongEmail ) )
        {
            Object [ ] messageArgs = {
                    strWrongEmail
            };

            return AdminMessageService.getMessageUrl( request, MESSAGE_WRONG_EMAIL, messageArgs, AdminMessage.TYPE_STOP );
        }

        SendingNewsLetter sending = new SendingNewsLetter( );
        sending.setNewsLetterId( nNewsletterId );
        sending.setDate( new Timestamp( new java.util.Date( ).getTime( ) ) ); // the current date

        String strObject = I18nService.getLocalizedString( PROPERTY_TEST_SUBJECT, getLocale( ) ) + newsletter.getName( ) + "]" + newsletter.getTestSubject( );

        /* lutece.properties */
        String strBaseUrl = AppPathService.getProdUrl( request );

        HtmlTemplate templateNewsLetter = setHtmlTemplateEmail( newsletter, strBaseUrl, newsletter.getUnsubscribe( ) );

        _newsletterService.sendMail( newsletter, strObject, strBaseUrl, templateNewsLetter, convertToList( newsletter.getTestRecipients( ) ) );

        return getHomeUrl( request );
    }

    /**
     * Processes the sending of a newsletter
     * 
     * @param request
     *            the http request
     * @return the url of the confirmation page
     */
    public String doSendNewsLetter( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        String strObject = request.getParameter( PARAMETER_NEWSLETTER_OBJECT );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin( ) );

        // Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser( ) )
                || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId, NewsletterResourceIdService.PERMISSION_SEND, getUser( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        // allow to send only if at list one subscriber
        int nNbrSubscribers = NewsLetterHome.findNbrSubscribers( nNewsletterId, getPlugin( ) );

        if ( nNbrSubscribers == 0 )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_NO_SUBSCRIBER, AdminMessage.TYPE_STOP );
        }

        if ( StringUtils.isEmpty( strObject ) )
        {
            strObject = newsletter.getName( );
        }

        /* lutece.properties */
        String strBaseUrl = AppPathService.getProdUrl( request );

        /* list of subscribers */
        Collection<Subscriber> list = SubscriberHome.findSubscribersForSending( nNewsletterId,
                Integer.parseInt( AppPropertiesService.getProperty( PROPERTY_LIMIT_MIN_SUSCRIBER ) ),
                Integer.parseInt( AppPropertiesService.getProperty( PROPERTY_LIMIT_MAX_SUSCRIBER ) ), getPlugin( ) );

        // Get the HtmlTemplate of email
        HtmlTemplate templateNewsLetter = setHtmlTemplateEmail( newsletter, strBaseUrl, newsletter.getUnsubscribe( ) );

        _newsletterService.sendMail( newsletter, strObject, strBaseUrl, templateNewsLetter, list );

        SendingNewsLetter sending = new SendingNewsLetter( );
        sending.setNewsLetterId( nNewsletterId );
        sending.setDate( new Timestamp( new java.util.Date( ).getTime( ) ) );
        sending.setCountSubscribers( nNbrSubscribers );

        if ( Boolean.parseBoolean( newsletter.getUnsubscribe( ) ) )
        {
            templateNewsLetter = setHtmlTemplateEmail( newsletter, strBaseUrl, Boolean.toString( false ) );
        }
        sending.setHtml( templateNewsLetter.getHtml( ) );
        sending.setEmailSubject( strObject );
        SendingNewsLetterHome.create( sending, getPlugin( ) );

        // updates the sending date
        newsletter.setDateLastSending( sending.getDate( ) );
        NewsLetterHome.update( newsletter, getPlugin( ) );

        return getHomeUrl( request );
    }

    /**
     * Processes the registration of a newsletter and loads the newsletter management page
     * 
     * @param request
     *            The Http request
     * @return The jsp URL which displays the newsletters management page
     */
    public String doRegisterNewsLetter( HttpServletRequest request )
    {
        String strAction = request.getParameter( PARAMETER_ACTION );
        String strReturn = null;

        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin( ) );

        // Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser( ) )
                || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId, NewsletterResourceIdService.PERMISSION_MODIFY, getUser( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        if ( !strAction.equals( I18nService.getLocalizedString( PROPERTY_CANCEL_ACTION, getLocale( ) ) ) )
        {
            String strBaseUrl = AppPathService.getBaseUrl( request );

            newsletter.setHtml( doClean( request.getParameter( PARAMETER_HTML_CONTENT ), strBaseUrl ) );

            NewsLetterHome.update( newsletter, getPlugin( ) );

            if ( strAction.equals( I18nService.getLocalizedString( PROPERTY_REGISTER_ACTION, getLocale( ) ) ) )
            {
                UrlItem url = new UrlItem( JSP_URL_DO_COMPOSE_NEWSLETTER );
                url.addParameter( PARAMETER_NEWSLETTER_ID, nNewsletterId );
                strReturn = url.getUrl( );
            }
            else
                if ( strAction.equals( I18nService.getLocalizedString( PROPERTY_PREPARE_SENDING_ACTION, getLocale( ) ) ) )
                {
                    UrlItem url = new UrlItem( JSP_URL_DO_PREPARE_NEWSLETTER );
                    url.addParameter( PARAMETER_NEWSLETTER_ID, nNewsletterId );
                    strReturn = url.getUrl( );
                }
                else
                    if ( strAction.equals( I18nService.getLocalizedString( PROPERTY_TEST_SENDING_ACTION, getLocale( ) ) ) )
                    {
                        UrlItem url = new UrlItem( JSP_URL_CONFIRM_TEST_NEWSLETTER );
                        url.addParameter( PARAMETER_NEWSLETTER_ID, nNewsletterId );
                        strReturn = url.getUrl( );
                    }
        }
        else
        {
            String strUrl = getHomeUrl( request );
            strReturn = AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_CANCEL_COMPOSE, strUrl, AdminMessage.TYPE_CONFIRMATION );
        }

        return strReturn;
    }

    /**
     * Builds the subscribers import page
     * 
     * @param request
     *            The HTTP request
     * @return the html code for subscribers import page
     */
    public String getImportSubscribers( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin( ) );

        // Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser( ) ) || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                NewsletterResourceIdService.PERMISSION_MANAGE_SUBSCRIBERS, getUser( ) ) )
        {
            return getManageNewsLetters( request );
        }

        setPageTitleProperty( PROPERTY_PAGE_TITLE_IMPORT );

        Map<String, Object> model = new HashMap<String, Object>( );
        model.put( MARK_NEWSLETTER_ID, nNewsletterId );
        model.put( MARK_IMPORT_DELETE, false );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_IMPORT_SUBSCRIBERS, getLocale( ), model );

        return getAdminPage( template.getHtml( ) );
    }

    /**
     * Builds the subscribers import page
     * 
     * @param request
     *            The HTTP request
     * @return the html code for subscribers import page
     */
    public String getImportDeleteSubscribers( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin( ) );

        // Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser( ) ) || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                NewsletterResourceIdService.PERMISSION_MANAGE_SUBSCRIBERS, getUser( ) ) )
        {
            return getManageNewsLetters( request );
        }

        setPageTitleProperty( PROPERTY_PAGE_TITLE_IMPORT_DELETE );

        Map<String, Object> model = new HashMap<String, Object>( );
        model.put( MARK_NEWSLETTER_ID, nNewsletterId );
        model.put( MARK_IMPORT_DELETE, true );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_IMPORT_SUBSCRIBERS, getLocale( ), model );

        return getAdminPage( template.getHtml( ) );
    }

    /**
     * Processes the import of subscribers due to a csv file and loads the subscribers management page
     * 
     * @param request
     *            The Http request
     * @return The jsp URL which displays the subscribers management page
     */
    public String doImportSubscribers( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin( ) );
        String strImportDelete = request.getParameter( MARK_IMPORT_DELETE );
        Boolean bImportDelete = Boolean.valueOf( strImportDelete );

        // Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser( ) ) || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId,
                NewsletterResourceIdService.PERMISSION_IMPORT_SUBSCRIBERS, getUser( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        try
        {
            if ( request instanceof MultipartHttpServletRequest )
            {
                // create the multipart request
                MultipartHttpServletRequest multi = (MultipartHttpServletRequest) request;

                FileItem csvItem = multi.getFile( PARAMETER_SUBSCRIBERS_FILE );
                String strMultiFileName = csvItem == null ? StringUtils.EMPTY : UploadUtil.cleanFileName( csvItem.getName( ) );
                if ( csvItem == null || StringUtils.isEmpty( strMultiFileName ) )
                {
                    return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
                }

                // test the extension of the file must be 'csv'
                String strExtension = strMultiFileName.substring( strMultiFileName.length( ) - 4, strMultiFileName.length( ) );

                if ( !strExtension.equals( CONSTANT_CSV_FILE_EXTENSION ) )
                {
                    return AdminMessageService.getMessageUrl( request, MESSAGE_CSV_FILE_EXTENSION, AdminMessage.TYPE_STOP );
                }

                Reader fileReader = new InputStreamReader( csvItem.getInputStream( ) );
                CSVReader csvReader = new CSVReader( fileReader, AppPropertiesService.getProperty( PROPERTY_IMPORT_DELIMITER ).charAt( 0 ) );

                @SuppressWarnings( "unchecked" )
                List<String [ ]> tabUsers = csvReader.readAll( );

                // the file is empty
                if ( ( tabUsers == null ) || ( tabUsers.size( ) == 0 ) )
                {
                    return AdminMessageService.getMessageUrl( request, MESSAGE_CSV_FILE_EMPTY_OR_NOT_VALID_EMAILS, AdminMessage.TYPE_STOP );
                }
                int nColumnIndex = Integer.parseInt( AppPropertiesService.getProperty( CONSTANT_EMAIL_COLUMN_INDEX ) );
                // the current date
                Timestamp tToday = new java.sql.Timestamp( new java.util.Date( ).getTime( ) );

                // Add the new users
                for ( String [ ] strEmailTemp : tabUsers )
                {
                    if ( strEmailTemp.length < nColumnIndex )
                    {
                        return AdminMessageService.getMessageUrl( request, MESSAGE_COLUMN_INDEX_NOT_EXIST, AdminMessage.TYPE_ERROR );
                    }

                    String strEmail = strEmailTemp [nColumnIndex];

                    // check if the email is not null and is valid
                    if ( ( strEmail != null ) && StringUtil.checkEmail( strEmail.trim( ) ) )
                    {
                        // Checks if a subscriber with the same email address doesn't exist yet
                        Subscriber subscriber = SubscriberHome.findByEmail( strEmail, getPlugin( ) );

                        if ( !bImportDelete )
                        {
                            if ( subscriber == null )
                            {
                                // The email doesn't exist, so create a new subcriber
                                subscriber = new Subscriber( );
                                subscriber.setEmail( strEmail );
                                SubscriberHome.create( subscriber, getPlugin( ) );
                            }

                            // adds a subscriber to the current newsletter
                            NewsLetterHome.addSubscriber( nNewsletterId, subscriber.getId( ), tToday, getPlugin( ) );
                        }
                        else
                        {
                            if ( subscriber != null )
                            {
                                NewsLetterHome.removeSubscriber( nNewsletterId, subscriber.getId( ), getPlugin( ) );
                            }
                        }
                    }
                }
            }
            UrlItem urlItem = new UrlItem( JSP_URL_MANAGE_SUBSCRIBERS );
            urlItem.addParameter( PARAMETER_NEWSLETTER_ID, nNewsletterId );

            return urlItem.getUrl( );
        }
        catch( IOException e )
        {
            throw new RuntimeException( e.getMessage( ) );
        }
    }

    /**
     * Exports the list of subscribers in a csv file format
     * 
     * @param request
     *            The Http Request
     * @param response
     *            The Http Response
     * @return The url of the management console for newsletters
     */
    public String doExportCsv( HttpServletRequest request, HttpServletResponse response )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin( ) );

        // allow to send only if at least one subscriber
        int nNbrSubscribers = NewsLetterHome.findNbrActiveSubscribers( nNewsletterId, getPlugin( ) );

        if ( nNbrSubscribers == 0 )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_NO_SUBSCRIBER_EXPORT, AdminMessage.TYPE_STOP );
        }

        String strFileName = newsletter.getName( ) + CONSTANT_CSV_FILE_EXTENSION;
        byte [ ] byteSubscribersList = _newsletterService.getSubscribersCsvExport( nNewsletterId );
        NewsletterUtils.addHeaderResponse( request, response, strFileName, CONSTANT_CSV_FILE_EXTENSION );
        response.setContentLength( byteSubscribersList.length );

        OutputStream os = null;
        try
        {
            os = response.getOutputStream( );
            os.write( byteSubscribersList );
            os.close( );
        }
        catch( IOException e )
        {
            AppLogService.error( e );
            IOUtils.closeQuietly( os );
        }

        return getHomeUrl( request );
    }

    /**
     * Get the manage topics page
     * 
     * @param request
     *            The request
     * @return The HTML to display
     */
    @SuppressWarnings( "unchecked" )
    public String getManageNewsletterTopics( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );

        if ( !StringUtils.isNumeric( strNewsletterId ) )
        {
            return getManageNewsLetters( request );
        }
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin( ) );

        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser( ) )
                || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId, NewsletterResourceIdService.PERMISSION_MODIFY, getUser( ) ) )
        {
            return getManageNewsLetters( request );
        }
        setPageTitleProperty( MESSAGE_PAGE_TITLE_MANAGE_TOPICS );

        List<NewsletterTopic> listTopics = NewsletterTopicHome.findAllByIdNewsletter( nNewsletterId, getPlugin( ) );

        // We check if we must update the template
        if ( Boolean.parseBoolean( request.getParameter( PARAMETER_UPDATE_TEMPLATE ) ) )
        {
            String strTemplateId = request.getParameter( MARK_NEWSLETTER_TEMPLATE_ID );
            if ( StringUtils.isNumeric( strTemplateId ) )
            {
                int nTemplateId = Integer.parseInt( strTemplateId );
                NewsLetterTemplate newsletterTemplate = NewsLetterTemplateHome.findByPrimaryKey( nTemplateId, getPlugin( ) );
                newsletter.setNbSections( newsletterTemplate.getSectionNumber( ) );
                newsletter.setNewsLetterTemplateId( nTemplateId );
                NewsLetterHome.update( newsletter, getPlugin( ) );
                int nNewOrder = NewsletterTopicHome.getNewOrder( nNewsletterId, newsletterTemplate.getSectionNumber( ), getPlugin( ) );
                for ( NewsletterTopic topic : listTopics )
                {
                    if ( topic.getSection( ) > newsletterTemplate.getSectionNumber( ) )
                    {
                        topic.setSection( newsletterTemplate.getSectionNumber( ) );
                        topic.setOrder( nNewOrder );
                        nNewOrder++;
                        NewsletterTopicHome.updateNewsletterTopic( topic, getPlugin( ) );
                    }
                }
            }
        }

        Map<String, Object> model = new HashMap<String, Object>( );

        // We create an array with the number of topics in every section
        Integer [ ] tblSectionSize = new Integer [ newsletter.getNbSections( )];
        for ( int i = 0; i < newsletter.getNbSections( ); i++ )
        {
            // The first section is 1, and we start from 0 so we consider the section i + 1
            tblSectionSize [i] = NewsletterTopicHome.getLastOrder( nNewsletterId, i + 1, getPlugin( ) );
        }

        ReferenceList refListTopicType = _newsletterTopicService.getNewsletterTopicTypeRefList( AdminUserService.getLocale( request ) );

        UrlItem url = new UrlItem( JSP_URL_MANAGE_NEWSLETTER_TOPIC );
        url.addParameter( PARAMETER_NEWSLETTER_ID, strNewsletterId );
        Object object = request.getSession( ).getAttribute( PARAMETER_NEWSLETTER_TOPICS_TABLE_MANAGER );
        DataTableManager<NewsletterTopic> tableManager = null;
        if ( object instanceof DataTableManager<?> )
        {
            tableManager = (DataTableManager<NewsletterTopic>) object;
            // If the table manager saved in session is not associated with this newsletter we create a new one
            if ( !StringUtils.equals( tableManager.getSortUrl( ), url.getUrl( ) ) )
            {
                tableManager = null;
            }
        }
        if ( tableManager == null )
        {
            tableManager = new DataTableManager<NewsletterTopic>( url.getUrl( ), url.getUrl( ),
                    AppPropertiesService.getPropertyInt( PROPERTY_ITEMS_PER_PAGE, CONSTANT_DEFAULT_ITEM_PER_PAGE ), true );
            tableManager.addFreeColumn( LABEL_NEWSLETTER_TOPIC_SECTION, CONSTANT_FREEMARKER_MACRO_COLUMN_SECTION );
            tableManager.addColumn( LABEL_NEWSLETTER_TOPIC_ORDER, MARK_NEWSLETTER_TOPIC_ORDER, false );
            tableManager.addColumn( LABEL_NEWSLETTER_TOPIC_TITLE, MARK_NEWSLETTER_TOPIC_TITLE, false );
            tableManager.addColumn( LABEL_NEWSLETTER_TOPIC_TYPE, MARK_NEWSLETTER_TOPIC_TYPE, false );
            tableManager.addActionColumn( LABEL_NEWSLETTER_ACTION );
        }

        tableManager.filterSortAndPaginate( request, listTopics );

        AdminUser user = AdminUserService.getAdminUser( request );
        Locale locale = AdminUserService.getLocale( request );
        String strBaseUrl = AppPathService.getBaseUrl( request );

        Collection<NewsLetterTemplate> newsletterTemplatesList = NewsLetterTemplateHome.getTemplatesCollectionByType( NewsLetterTemplate.RESOURCE_TYPE,
                getPlugin( ) );
        newsletterTemplatesList = AdminWorkgroupService.getAuthorizedCollection( newsletterTemplatesList, user );

        String strPathImageTemplate = _newsletterService.getImageFolderPath( AppPathService.getBaseUrl( request ) );

        model.put( MARK_NEWSLETTER, newsletter );
        model.put( MARK_NEWSLETTER_TABLE_MANAGER, tableManager );
        model.put( MARK_LIST_TOPIC_TYPES, refListTopicType );
        model.put( MARK_SECTION_SIZES, tblSectionSize );
        model.put( MARK_HTML_CONTENT,
                _newsletterService.generateNewsletterHtmlCode( newsletter, newsletter.getNewsLetterTemplateId( ), strBaseUrl, user, locale ) );
        model.put( MARK_WEBAPP_URL, strBaseUrl );
        model.put( MARK_LOCALE, getLocale( ) );
        model.put( MARK_LIST_NEWSLETTER_TEMPLATES, newsletterTemplatesList );
        model.put( MARK_NEWSLETTER_TEMPLATE_ID, newsletter.getNewsLetterTemplateId( ) );
        model.put( MARK_IMG_PATH, strPathImageTemplate );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MANAGE_NEWSLETTER_TOPICS, AdminUserService.getLocale( request ), model );
        String strContent = template.getHtml( );
        tableManager.clearItems( );
        request.getSession( ).setAttribute( PARAMETER_NEWSLETTER_TOPICS_TABLE_MANAGER, tableManager );

        return getAdminPage( strContent );
    }

    /**
     * Create a news topic in a newsletter
     * 
     * @param request
     *            The request
     * @return The URL to redirect to.
     */
    public String doAddNewsletterTopic( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        String strTopicType = request.getParameter( PARAMETER_TOPIC_TYPE );
        int nNewsletterId = Integer.parseInt( strNewsletterId );

        NewsletterTopic newsletterTopic = new NewsletterTopic( );
        newsletterTopic.setIdNewsletter( nNewsletterId );
        newsletterTopic.setTopicTypeCode( strTopicType );
        String strSectionNumber = request.getParameter( PARAMETER_TOPIC_SECTION_NUMBER );
        if ( StringUtils.isNumeric( strSectionNumber ) )
        {
            newsletterTopic.setSection( Integer.parseInt( strSectionNumber ) );
        }
        else
        {
            newsletterTopic.setSection( 1 );
        }
        _newsletterTopicService.createNewsletterTopic( newsletterTopic, getUser( ), AdminUserService.getLocale( request ) );

        UrlItem urlItem = new UrlItem( JSP_URL_MODIFY_TOPIC_CONFIG );
        urlItem.addParameter( PARAMETER_NEWSLETTER_ID, strNewsletterId );
        urlItem.addParameter( PARAMETER_TOPIC_ID, newsletterTopic.getId( ) );
        return urlItem.getUrl( );
    }

    /**
     * Get a confirmation page before removing a newsletter topic
     * 
     * @param request
     *            The request
     * @return The URL to redirect to.
     */
    public String confirmRemoveNewsletterTopic( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        if ( !StringUtils.isNumeric( strNewsletterId ) )
        {
            return JSP_URL_MANAGE_NEWSLETTER;
        }
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin( ) );

        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser( ) )
                || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId, NewsletterResourceIdService.PERMISSION_MODIFY, getUser( ) ) )
        {
            return JSP_URL_MANAGE_NEWSLETTER;
        }
        UrlItem urlItem = new UrlItem( JSP_URL_DO_REMOVE_TOPIC );
        urlItem.addParameter( PARAMETER_NEWSLETTER_ID, strNewsletterId );
        urlItem.addParameter( PARAMETER_TOPIC_ID, request.getParameter( PARAMETER_TOPIC_ID ) );
        urlItem.addParameter( PARAMETER_TOPIC_TYPE, request.getParameter( PARAMETER_TOPIC_TYPE ) );
        return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_TOPIC, urlItem.getUrl( ), AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Remove a newsletter topic
     * 
     * @param request
     *            The request
     * @return The URL to redirect to.
     */
    public String doRemoveNewsletterTopic( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        if ( !StringUtils.isNumeric( strNewsletterId ) )
        {
            return JSP_URL_MANAGE_NEWSLETTER;
        }
        int nNewsletterId = Integer.parseInt( strNewsletterId );
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin( ) );

        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser( ) )
                || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId, NewsletterResourceIdService.PERMISSION_MODIFY, getUser( ) ) )
        {
            return JSP_URL_MANAGE_NEWSLETTER;
        }
        String strTopicId = request.getParameter( PARAMETER_TOPIC_ID );
        if ( !StringUtils.isNumeric( strTopicId ) )
        {
            return JSP_URL_MANAGE_NEWSLETTER;
        }
        int nIdTopic = Integer.parseInt( strTopicId );
        NewsletterTopic newsletterTopic = NewsletterTopicHome.findByPrimaryKey( nIdTopic, getPlugin( ) );
        _newsletterTopicService.removeNewsletterTopic( newsletterTopic, AdminUserService.getAdminUser( request ) );

        UrlItem urlItem = new UrlItem( JSP_URL_MANAGE_TOPICS );
        urlItem.addParameter( PARAMETER_NEWSLETTER_ID, strNewsletterId );
        return urlItem.getUrl( );

    }

    /**
     * Change the order of a topic, and display the manage topics page.
     * 
     * @param request
     *            The request
     * @return The HTML content to display
     */
    public String getMoveNewsletterTopic( HttpServletRequest request )
    {
        String strTopicId = request.getParameter( PARAMETER_TOPIC_ID );

        if ( !StringUtils.isNumeric( strTopicId ) )
        {
            return getManageNewsletterTopics( request );
        }
        int nIdTopic = Integer.parseInt( strTopicId );
        String strMoveUp = request.getParameter( PARAMETER_MOVE_UP );
        NewsletterTopic newsletterTopic = NewsletterTopicHome.findByPrimaryKey( nIdTopic, getPlugin( ) );
        _newsletterTopicService.modifyNewsletterTopicOrder( newsletterTopic, Boolean.parseBoolean( strMoveUp ) );

        return getManageNewsletterTopics( request );
    }

    /**
     * Get the modification page of a newsletter topic configuration
     * 
     * @param request
     *            The request
     * @return The html content to display
     */
    public String getModifyTopicConfig( HttpServletRequest request )
    {
        String strTopicId = request.getParameter( PARAMETER_TOPIC_ID );

        if ( !StringUtils.isNumeric( strTopicId ) )
        {
            return getManageNewsletterTopics( request );
        }
        int nIdTopic = Integer.parseInt( strTopicId );

        setPageTitleProperty( PROPERTY_PAGE_TITLE_MODIFY_TOPIC_CONFIGURATION );
        NewsletterTopic newsletterTopic = NewsletterTopicHome.findByPrimaryKey( nIdTopic, getPlugin( ) );
        Locale locale = AdminUserService.getLocale( request );
        String strContent = _newsletterTopicService.getConfigurationPage( newsletterTopic, AppPathService.getBaseUrl( request ),
                AdminUserService.getAdminUser( request ), locale );

        Map<String, Object> model = new HashMap<String, Object>( );
        model.put( MARK_CONTENT, strContent );
        model.put( MARK_TOPIC, newsletterTopic );
        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MODIFY_TOPIC_CONFIG, locale, model );
        return getAdminPage( template.getHtml( ) );
    }

    /**
     * Save the configuration of a topic
     * 
     * @param request
     *            The request
     * @return The URL to redirect to
     */
    public String doModifyTopicConfig( HttpServletRequest request )
    {
        String strTopicId = request.getParameter( PARAMETER_TOPIC_ID );

        if ( !StringUtils.isNumeric( strTopicId ) )
        {
            return JSP_URL_MANAGE_NEWSLETTER;
        }
        int nIdTopic = Integer.parseInt( strTopicId );

        NewsletterTopic newsletterTopic = NewsletterTopicHome.findByPrimaryKey( nIdTopic, getPlugin( ) );

        UrlItem url = new UrlItem( JSP_URL_MANAGE_NEWSLETTER_TOPIC );
        url.addParameter( PARAMETER_NEWSLETTER_ID, Integer.toString( newsletterTopic.getIdNewsletter( ) ) );

        // If the user didn't push the 'cancel' button, we save the configuration
        if ( request.getParameter( PARAMETER_CANCEL ) == null )
        {
            String strTitle = request.getParameter( PARAMETER_TITLE );
            if ( StringUtils.isNotEmpty( strTitle ) )
            {
                newsletterTopic.setTitle( strTitle );
                NewsletterTopicHome.updateNewsletterTopic( newsletterTopic, getPlugin( ) );
            }

            @SuppressWarnings( "unchecked" )
            Map<String, String [ ]> mapParameters = request.getParameterMap( );
            _newsletterTopicService.saveConfiguration( mapParameters, newsletterTopic, AdminUserService.getAdminUser( request ),
                    AdminUserService.getLocale( request ) );
        }
        return AppPathService.getBaseUrl( request ) + url.getUrl( );
    }

    /**
     * Change the section of a newsletter topic.
     * 
     * @param request
     *            The request
     * @return The html to display
     */
    public String doChangeNewsletterTopicSection( HttpServletRequest request )
    {
        String strTopicId = request.getParameter( PARAMETER_TOPIC_ID );
        String strSection = request.getParameter( PARAMETER_TOPIC_SECTION_NUMBER );

        if ( !StringUtils.isNumeric( strTopicId ) || !StringUtils.isNumeric( strSection ) )
        {
            return getManageNewsletterTopics( request );
        }
        int nIdTopic = Integer.parseInt( strTopicId );
        int nSection = Integer.parseInt( strSection );

        NewsletterTopic newsletterTopic = NewsletterTopicHome.findByPrimaryKey( nIdTopic, getPlugin( ) );

        _newsletterTopicService.modifyNewsletterTopicSection( newsletterTopic, nSection );

        return getManageNewsletterTopics( request );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Plugin getPlugin( )
    {
        return PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
    }

    // //////////////////////////////////////////////////////////////////////////////////
    // Private Implementation

    /**
     * To translate the absolute url's in SEMI-relativre url's of the html_content ( use before insertion in db)
     * 
     * @param strContent
     *            The html code
     * @param strBaseUrl
     *            The base url
     * @return The clean code
     */
    private String doClean( String strContent, String strBaseUrl )
    {
        String strNewContent = strContent;
        strNewContent = StringUtil.substitute( strNewContent, NewsLetterConstants.WEBAPP_PATH_FOR_LINKSERVICE, strBaseUrl );

        return strNewContent;
    }

    /**
     * Tests whether all the e-mails represented by a string are valid
     * 
     * @param strRecipientLists
     *            The list of recipients
     * @return The last wrong invalid e-mail in the list or an empty String if all e-mails are valid
     */
    private String isWrongEmail( String strRecipientLists )
    {
        String strWrongEmail = StringUtils.EMPTY;

        String strDelimiter = AppPropertiesService.getProperty( PROPERTY_IMPORT_DELIMITER );

        String [ ] strEmails = strRecipientLists.split( strDelimiter );

        for ( int j = 0; j < strEmails.length; j++ )
        {
            if ( !StringUtil.checkEmail( strEmails [j] ) )
            {
                strWrongEmail = strEmails [j];
            }
        }

        return strWrongEmail;
    }

    /**
     * Check if user is authozired to create a newsletter
     * 
     * @param request
     *            The {@link HttpServletRequest}
     * @return true if creation is authorized, false otherwise
     */
    private boolean isNewsletterCreationAllowed( HttpServletRequest request )
    {
        // RBAC permission
        AdminUser user = AdminUserService.getAdminUser( request );
        if ( RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID, NewsletterResourceIdService.PERMISSION_CREATE, user ) )
        {
            return true;
        }

        Collection<NewsLetter> listNewsletter = NewsLetterHome.findAll( getPlugin( ) );
        listNewsletter = AdminWorkgroupService.getAuthorizedCollection( listNewsletter, user );

        for ( NewsLetter newsletter : listNewsletter )
        {
            if ( RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_CREATE, user ) )
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Takes a list of recipients in a form of a String and converts it into a list of subscribers
     * 
     * @param strRecipients
     *            A list of recipients as a String
     * @return A list of subscibers
     */
    private Collection<Subscriber> convertToList( String strRecipients )
    {
        Collection<Subscriber> listRecipients = new ArrayList<Subscriber>( );

        if ( StringUtils.isNotEmpty( strRecipients ) )
        {
            String strDelimiter = AppPropertiesService.getProperty( PROPERTY_IMPORT_DELIMITER );

            String [ ] strEmails = strRecipients.split( strDelimiter );

            for ( int j = 0; j < strEmails.length; j++ )
            {
                if ( StringUtil.checkEmail( strEmails [j] ) )
                {
                    Subscriber subscriber = new Subscriber( );
                    subscriber.setEmail( strEmails [j] );
                    listRecipients.add( subscriber );
                }
            }
        }

        return listRecipients;
    }

    /**
     * Get the content of a CSS file
     * 
     * @return The content into a String
     */
    private String getCssContent( )
    {
        StringBuilder sbContent = new StringBuilder( StringUtils.EMPTY );
        String strListCssFileName = AppPropertiesService.getProperty( PROPERTY_CSS_FILES );
        String strWebappPath = AppPathService.getWebAppPath( ) + NewsLetterConstants.CONSTANT_SLASH;
        if ( StringUtils.isNotEmpty( strListCssFileName ) )
        {
            for ( String strName : strListCssFileName.split( SEPARATOR_PROPERTY_CSS_FILES ) )
            {
                sbContent.append( getTextFileContent( strWebappPath + strName ) );
                sbContent.append( SEPARATOR_CSS_FILES_CONTENT );
            }
        }

        return sbContent.toString( );
    }

    /**
     * Get the content of a text file
     * 
     * @param strFileName
     *            The full name of the file
     * @return The content
     */
    private String getTextFileContent( String strFileName )
    {
        BufferedReader fileReader = null;
        StringBuilder sbSource = new StringBuilder( StringUtils.EMPTY );

        try
        {
            fileReader = new BufferedReader( new FileReader( strFileName ) );

            String line;

            line = fileReader.readLine( );

            while ( line != null )
            {
                sbSource.append( line + SEPARATOR_CSS_FILES_CONTENT );
                line = fileReader.readLine( );
            }

            fileReader.close( );
        }
        catch( FileNotFoundException e )
        {
            AppLogService.error( "plugin-newsletter - CSS '" + strFileName + "' not found ! " + e.getMessage( ) );
        }
        catch( IOException e )
        {
            AppLogService.error( "plugin-newsletter - error when reading CSS '" + strFileName + "' ! " + e.getMessage( ) );
        }
        finally
        {
            IOUtils.closeQuietly( fileReader );
        }

        return sbSource.toString( );
    }

    /**
     * Generate the final html code for email
     * 
     * @param newsletter
     *            The newsletter to generate
     * @param strBaseUrl
     *            The baseUrl (can be prod url)
     * @param strUnsubscribe
     *            'True' if a link to unsuscribe to the newsletter should be generated, false otherwise
     * @return The {@link HtmlTemplate}
     */
    private HtmlTemplate setHtmlTemplateEmail( NewsLetter newsletter, String strBaseUrl, String strUnsubscribe )
    {
        Map<String, Object> sendingModel = new HashMap<String, Object>( );
        sendingModel.put( MARK_CSS, getCssContent( ) );
        sendingModel.put( MARK_UNSUBSCRIBE, strUnsubscribe );
        sendingModel.put( MARK_NEWSLETTER_ID, newsletter.getId( ) );
        sendingModel.put( MARK_NEWSLETTER_CONTENT, newsletter.getHtml( ) );
        sendingModel.put( NewsLetterConstants.MARK_BASE_URL, strBaseUrl );
        sendingModel.put( NewsLetterConstants.MARK_SUBSCRIBER_EMAIL, NewsLetterConstants.MARK_SUBSCRIBER_EMAIL_EACH );
        sendingModel.put( NewsLetterConstants.MARK_UNSUBSCRIBE_KEY, NewsLetterConstants.MARK_UNSUBSCRIBE_KEY_EACH );

        HtmlTemplate templateNewsLetter = AppTemplateService.getTemplate( TEMPLATE_SEND_NEWSLETTER, getLocale( ), sendingModel );

        templateNewsLetter.substitute( NewsLetterConstants.WEBAPP_PATH_FOR_LINKSERVICE, strBaseUrl );

        return templateNewsLetter;
    }

    /**
     * Processes the copy of a newsletter
     * 
     * @param request
     *            The Http request
     * @return the jsp URL to display the form to manage newsletters
     */
    public String doCopyNewsLetter( HttpServletRequest request )
    {
        AdminUser user = AdminUserService.getAdminUser( request );
        Locale locale = AdminUserService.getLocale( request );
        int nNewsletterId = Integer.parseInt( request.getParameter( PARAMETER_NEWSLETTER_ID ) );

        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, getPlugin( ) );

        // Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletter, getUser( ) ) || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE,
                Integer.toString( newsletter.getId( ) ), NewsletterResourceIdService.PERMISSION_CREATE, getUser( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        Object [ ] tabNewsLetterTileCopy = {
                newsletter.getName( )
        };
        String strTitleCopySuggest = I18nService.getLocalizedString( LABLE_COPY_NEWSLETTER_TITLE, tabNewsLetterTileCopy, getLocale( ) );

        if ( strTitleCopySuggest != null )
        {
            newsletter.setName( strTitleCopySuggest );
        }

        // Copy of newsletter
        _newsletterService.copyExistingNewsletter( newsletter, user, locale );
        return getHomeUrl( request );
    }
}
