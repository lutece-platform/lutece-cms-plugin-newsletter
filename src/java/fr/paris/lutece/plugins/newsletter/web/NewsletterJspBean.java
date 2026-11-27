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
import java.io.Reader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.apache.commons.lang3.Strings;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import fr.paris.lutece.plugins.newsletter.business.NewsLetter;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterHome;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterProperties;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterTemplate;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterTemplateHome;
import fr.paris.lutece.plugins.newsletter.business.NewsletterPropertiesHome;
import fr.paris.lutece.plugins.newsletter.business.Pair;
import fr.paris.lutece.plugins.newsletter.business.SendingNewsLetter;
import fr.paris.lutece.plugins.newsletter.business.SendingNewsLetterHome;
import fr.paris.lutece.plugins.newsletter.business.SendingStatistics;
import fr.paris.lutece.plugins.newsletter.business.Subscriber;
import fr.paris.lutece.plugins.newsletter.business.SubscriberHome;
import fr.paris.lutece.plugins.newsletter.business.topic.NewsletterTopic;
import fr.paris.lutece.plugins.newsletter.business.topic.NewsletterTopicHome;
import fr.paris.lutece.plugins.newsletter.service.NewsLetterRegistrationService;
import fr.paris.lutece.plugins.newsletter.service.NewsletterPlugin;
import fr.paris.lutece.plugins.newsletter.service.NewsletterResourceIdService;
import fr.paris.lutece.plugins.newsletter.service.SubscriberResourceIdService;
import fr.paris.lutece.plugins.newsletter.service.NewsletterService;
import fr.paris.lutece.plugins.newsletter.service.topic.NewsletterTopicService;
import fr.paris.lutece.plugins.newsletter.util.NewsLetterConstants;
import fr.paris.lutece.portal.business.rbac.RBAC;
import fr.paris.lutece.api.user.User;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.business.workgroup.AdminWorkgroupHome;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.business.editor.EditorProfile;
import fr.paris.lutece.portal.service.editor.EditorProfileService;
import fr.paris.lutece.portal.service.editor.RichTextEditorService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.security.SecurityTokenHandler;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.template.HtmlMarkup;
import fr.paris.lutece.portal.service.upload.MultipartItem;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.workgroup.AdminWorkgroupService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.web.cdi.mvc.Models;
import fr.paris.lutece.portal.web.constants.Messages;
import fr.paris.lutece.portal.web.constants.Parameters;
import fr.paris.lutece.portal.web.upload.MultipartHttpServletRequest;
import fr.paris.lutece.portal.web.util.IPager;
import fr.paris.lutece.portal.web.util.Pager;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.date.DateUtil;
import fr.paris.lutece.util.filesystem.UploadUtil;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.sort.AttributeComparator;
import fr.paris.lutece.util.string.StringUtil;
import fr.paris.lutece.util.url.UrlItem;

/**
 * This class provides the user interface to manage NewsLetters features: the list of the newsletters, their settings, their content (topics and
 * template), their composition, their subscribers, their sending and their archive. Every change of state is an action posted with the security
 * token of the page that shows it; a link only opens a view, or the confirmation of an action.
 */
@SessionScoped
@Named
@Controller( controllerJsp = NewsletterJspBean.CONTROLLER_JSP, controllerPath = NewsletterJspBean.CONTROLLER_PATH, right = NewsletterJspBean.RIGHT_NEWSLETTER_MANAGEMENT, securityTokenEnabled = true )
public class NewsletterJspBean extends MVCAdminJspBean
{
    /**
     * The right used for managing newsletters
     */
    public static final String RIGHT_NEWSLETTER_MANAGEMENT = "NEWSLETTER_MANAGEMENT";

    /**
     * The controller JSP
     */
    public static final String CONTROLLER_JSP = "ManageNewsLetter.jsp";

    /**
     * The controller path
     */
    public static final String CONTROLLER_PATH = "jsp/admin/plugins/newsletter/";

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 5581671308419705321L;

    private static final String CONSTANT_CSV_FILE_EXTENSION = ".csv";

    private static final String CONSTANT_ACTION_REGISTER = "register";
    private static final String CONSTANT_ACTION_PREPARE_SENDING = "prepare_sending";
    private static final String CONSTANT_ACTION_CANCEL = "cancel";
    private static final String CONSTANT_ACTION_TEST_SENDING = "test_sending";

    private static final String PROPERTY_SELECT_MANDATORY = "newsletter.manage_archive.multiSelectError";

    // Css inclusion
    private static final String SEPARATOR_PROPERTY_CSS_FILES = ";";
    private static final String SEPARATOR_CSS_FILES_CONTENT = "\n";

    // Handling of CSV

    // templates
    private static final String TEMPLATE_MANAGE_NEWSLETTERS = "admin/plugins/newsletter/manage_newsletters.html";
    private static final String TEMPLATE_MANAGE_NEWSLETTERS_PROPERTIES = "admin/plugins/newsletter/manage_newsletters_properties.html";
    private static final String TEMPLATE_MODIFY_NEWSLETTER = "admin/plugins/newsletter/modify_newsletter.html";
    private static final String TEMPLATE_CREATE_NEWSLETTER = "admin/plugins/newsletter/create_newsletter.html";
    private static final String TEMPLATE_COMPOSE_NEWSLETTER = "admin/plugins/newsletter/compose_newsletter.html";
    private static final String TEMPLATE_PREPARE_NEWSLETTER = "admin/plugins/newsletter/prepare_newsletter.html";
    private static final String TEMPLATE_CONFIRM_SEND_NEWSLETTER = "admin/plugins/newsletter/confirm_send_newsletter.html";
    private static final String TEMPLATE_SEND_NEWSLETTER = "admin/plugins/newsletter/send_newsletter.html";
    private static final String TEMPLATE_MANAGE_SUBSCRIBERS = "admin/plugins/newsletter/manage_subscribers.html";
    private static final String TEMPLATE_IMPORT_SUBSCRIBERS = "admin/plugins/newsletter/import_subscribers.html";
    private static final String TEMPLATE_MANAGE_OLD_NEWSLETTERS = "admin/plugins/newsletter/manage_old_newsletters.html";
    private static final String TEMPLATE_MANAGE_NEWSLETTER_TOPICS = "admin/plugins/newsletter/manage_newsletter_topics.html";
    private static final String TEMPLATE_MODIFY_TOPIC_CONFIG = "admin/plugins/newsletter/modify_topic_config.html";

    // views
    private static final String VIEW_MANAGE_NEWSLETTERS = "manageNewsLetters";
    private static final String VIEW_MANAGE_ARCHIVE = "manageArchive";
    private static final String VIEW_MANAGE_PROPERTIES = "manageNewsLetterProperties";
    private static final String VIEW_CONFIRM_CLEAN_SUBSCRIBERS = "confirmCleanSubscribers";
    private static final String VIEW_CREATE_NEWSLETTER = "createNewsLetter";
    private static final String VIEW_COMPOSE_NEWSLETTER = "composeNewsLetter";
    private static final String VIEW_MODIFY_NEWSLETTER = "modifyNewsLetter";
    private static final String VIEW_CONFIRM_REMOVE_SUBSCRIBER = "confirmRemoveSubscriber";
    private static final String VIEW_CONFIRM_REMOVE_SELECTED_SUBSCRIBERS = "confirmRemoveSelectedSubscribers";
    private static final String VIEW_CONFIRM_REMOVE_NEWSLETTER = "confirmRemoveNewsLetter";
    private static final String VIEW_CONFIRM_REMOVE_SENDING = "confirmRemoveSendingNewsLetter";
    private static final String VIEW_CONFIRM_REMOVE_MULTI_SENDING = "confirmRemoveMultiSendingNewsLetter";
    private static final String VIEW_MANAGE_SUBSCRIBERS = "manageSubscribers";
    private static final String VIEW_PREPARE_NEWSLETTER = "prepareNewsLetter";
    private static final String VIEW_CONFIRM_SEND_NEWSLETTER = "confirmSendNewsLetter";
    private static final String VIEW_CONFIRM_TEST_NEWSLETTER = "confirmTestNewsLetter";
    private static final String VIEW_IMPORT_SUBSCRIBERS = "importSubscribers";
    private static final String VIEW_IMPORT_DELETE_SUBSCRIBERS = "importDeleteSubscribers";
    private static final String VIEW_MANAGE_TOPICS = "manageNewsletterTopics";
    private static final String VIEW_CONFIRM_REMOVE_TOPIC = "confirmRemoveNewsletterTopic";
    private static final String VIEW_MODIFY_TOPIC_CONFIG = "modifyTopicConfig";

    // actions
    private static final String ACTION_COPY_NEWSLETTER = "copyNewsLetter";
    private static final String ACTION_MODIFY_PROPERTIES = VIEW_MANAGE_PROPERTIES;
    private static final String ACTION_CLEAN_SUBSCRIBERS = "cleanSubscribers";
    private static final String PARAMETER_RETURN_VIEW = "return_view";
    private static final String VALUE_RETURN_SUBSCRIBERS = "subscribers";
    private static final String ACTION_CREATE_NEWSLETTER = VIEW_CREATE_NEWSLETTER;
    private static final String ACTION_MODIFY_NEWSLETTER = VIEW_MODIFY_NEWSLETTER;
    private static final String ACTION_REGISTER_NEWSLETTER = "registerNewsLetter";
    private static final String ACTION_REMOVE_SUBSCRIBER = "removeSubscriber";
    private static final String ACTION_REMOVE_SELECTED_SUBSCRIBERS = "removeSelectedSubscribers";
    private static final String ACTION_REMOVE_NEWSLETTER = "removeNewsLetter";
    private static final String ACTION_REMOVE_SENDING = "removeSendingNewsLetter";
    private static final String ACTION_REMOVE_MULTI_SENDING = "removeMultiSendingNewsLetter";
    private static final String ACTION_ADD_SUBSCRIBER = "addSubscriber";
    private static final String ACTION_SEND_NEWSLETTER = "sendNewsLetter";
    private static final String ACTION_TEST_NEWSLETTER = "testNewsLetter";
    private static final String ACTION_IMPORT_SUBSCRIBERS = VIEW_IMPORT_SUBSCRIBERS;
    private static final String ACTION_MODIFY_TOPICS = "modifyNewsletterTopics";
    private static final String ACTION_REMOVE_TOPIC = "removeNewsletterTopic";
    private static final String ACTION_MODIFY_TOPIC_CONFIG = VIEW_MODIFY_TOPIC_CONFIG;

    // operations of the topics page
    private static final String OPERATION_ADD_TOPIC = "addTopic";
    private static final String OPERATION_MOVE_TOPIC = "moveTopic";
    private static final String OPERATION_CHANGE_SECTION = "changeSection";
    private static final String OPERATION_UPDATE_TEMPLATE = "updateTemplate";

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
    private static final String MARK_LUTECE_PROD_URL = "lutece_prod_url";
    private static final String MARK_WEBAPP_URL = "webapp_url";
    private static final String MARK_NEWSLETTER_TEMPLATE_ID = "newsletter_template_id";
    private static final String MARK_NEWSLETTER_TEMPLATE_MISSING = "newsletter_template_missing";
    private static final String MARK_MISSING_TEMPLATE_IDS = "missing_template_ids";
    private static final String MARK_AVAILABLE_PICTURES = "available_pictures";
    private static final String MARK_PREVIEW = "newsletter_preview";
    private static final String MARK_PREVIEW_HTML = "newsletter_preview_html";
    private static final String MARK_NEWSLETTER_OBJECT = "newsletter_object";
    private static final String MARK_MESSAGE = "message";
    private static final String MARK_MESSAGE_TYPE = "type";
    private static final String MARK_TEXT = "text";
    private static final String MARK_DATE_LAST_SEND = "newsletter_last_sent";
    private static final String MARK_IMG_PATH = "img_path";
    private static final String MARK_LUTECE_EDITOR = "lutece_editor";
    private static final String MARK_LOCALE = "locale";
    private static final String MARK_PROPERTIES = "properties";
    private static final String MARK_EDITORS_LIST = "editors_list";
    private static final String MARK_NEWSLETTER_EDITOR = "newsletter_editor";
    private static final String MARK_TEXT_EDITOR_PROFILE = "textEditorProfile";
    private static final String KEY_PROFILE_PLUGIN = "plugin";
    private static final String KEY_PROFILE_NAME = "name";
    private static final String KEY_PROFILE_EDITOR = "editor";
    private static final String KEY_PROFILE_CONFIG = "config";
    private static final String PROPERTY_EDITOR_GRAPESJS = "newsletter.manage_newsletters_properties.editorGrapesjs";
    private static final String PROPERTY_EDITOR_SITE = "newsletter.manage_newsletters_properties.editorSite";
    private static final String MARK_NEWSLETTER_LIST = "newsletters_list";
    private static final String MARK_ALLOW_CREATION = "creation_allowed";
    private static final String MARK_LIST_TOPIC_TYPES = "list_topic_types";
    private static final String MARK_SECTION_SIZES = "section_size";
    private static final String MARK_TOPIC_STATUS = "topic_status";
    private static final String MARK_TOPIC_SUMMARIES = "topic_summaries";
    private static final String TOPIC_STATUS_OK = "ok";
    private static final String TOPIC_STATUS_EMPTY = "empty";
    private static final String TOPIC_STATUS_UNCONFIGURED = "unconfigured";
    private static final String MARK_TOPICS_LIST = "topics_list";
    private static final String MARK_NEWSLETTER_COUNT_ACTIVE_SUBSCRIBERS = "newsletter_count_active_subscribers";
    private static final String MARK_NEWSLETTER_COUNT_SENDINGS = "newsletter_count_sendings";
    private static final String MARK_NEWSLETTER_LAST_SENDING_DATE = "newsletter_last_sending_date";
    private static final String MARK_NEWSLETTER_TEMPLATE_DESCRIPTION = "newsletter_template_description";
    private static final String MARK_COUNT_NEWSLETTERS = "count_newsletters";
    private static final String MARK_COUNT_ACTIVE_SUBSCRIBERS = "count_active_subscribers";
    private static final String MARK_COUNT_PENDING_SUBSCRIBERS = "count_pending_subscribers";
    private static final String MARK_COUNT_RECENT_SENDINGS = "count_recent_sendings";
    private static final String MARK_SEARCH_NAME = "search_name";
    private static final String MARK_SEARCH_DATE = "search_date";
    private static final String MARK_SUBSCRIBERS_LIST = "subscribers_list";
    private static final String MARK_WORKGROUP_LIST = "workgroup_list";
    private static final String MARK_NEWSLETTER_PAGE_ITEMS = "newsletter_page_items";
    private static final String MARK_CSS = "newsletter_css";
    private static final String MARK_UNSUBSCRIBE = "unsubscribe";
    private static final String MARK_UNSUBSCRIBE_LIST = "unsubscribe_list";
    private static final String MARK_DISPLAY_STATUS = "display_status";
    private static final String MARK_IS_ACTIVE_CAPTCHA = "is_active_captcha";
    private static final String MARK_ADD_SUBSCRIBER_RIGHT = "is_add_subscriber_right";
    private static final String MARK_IMPORT_SUBSCRIBER_RIGHT = "is_import_subscriber_right";
    private static final String MARK_EXPORT_SUBSCRIBER_RIGTH = "is_export_subscriber_right";
    private static final String MARK_CLEAN_RIGHT = "is_clean_subscriber_right";
    private static final String MARK_SUBSCRIBER_MANAGEMENT_RIGHT = "is_subscriber_management_right";
    private static final String MARK_CONTENT = "content";
    private static final String MARK_TOPIC = "topic";
    private static final String MARK_SEARCH_STRING = "search_string";
    private static final String MARK_RIGHT_MANAGE_NEWSLETTER_PROPERTIES = "right_manage_newsletter_properties";

    // PARAMETER
    private static final String PARAMETER_COMPOSE_ACTION = "compose_action";
    private static final String PARAMETER_OPERATION = "operation";
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
    private static final String PARAMETER_TEST_RECIPIENTS = "newsletter_test_recipients";
    private static final String PARAMETER_TEST_SUBJECT = "newsletter_test_subject";
    private static final String PARAMETER_ACTIVE_CAPTCHA = "active_captcha";
    private static final String PARAMETER_ACTIVE_VALIDATION = "active_validation";
    private static final String PARAMETER_EDITOR = "editor";
    private static final String PARAMETER_SUBSCRIBERS_SELECTION = "subscriber_selection";
    private static final String PARAMETER_TOPIC_ID = "topic_id";
    private static final String PARAMETER_MOVE_UP = "move_up";
    private static final String PARAMETER_TITLE = "title";
    private static final String PARAMETER_SEARCH_NAME = "search_name";
    private static final String PARAMETER_SEARCH_DATE = "search_date";
    private static final String PARAMETER_NEWSLETTER_SELECTION = "newsletter_selection";

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
    private static final String MESSAGE_CONFIRM_CLEAN_SUBSCRIBERS = "newsletter.message.confirmCleanSubscribers";
    private static final String MESSAGE_CSV_FILE_EMPTY_OR_NOT_VALID_EMAILS = "newsletter.message.csvFileEmptyOrNotValidEmails";
    private static final String MESSAGE_COLUMN_INDEX_NOT_EXIST = "newsletter.message.csvColumnIndexNotExist";
    private static final String MESSAGE_CSV_FILE_EXTENSION = "newsletter.message.csvFileExtension";
    private static final String MESSAGE_EMAIL_EXISTS = "newsletter.message.emailExists";
    private static final String MESSAGE_FIELD_EMAIL_VALID = "newsletter.message.fieldEmailValid";
    private static final String MESSAGE_NO_SUBSCRIBER = "newsletter.message.noSubscriber";
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
    private static final String MESSAGE_SENDING_ALREADY_DONE = "newsletter.message.sendingAlreadyDone";

    private static final String MESSAGE_USER_NOT_ALLOWED_NEWSLETTER_PROPERTIES = "Newsletter properties : user not allowed to access this feature : ";

    private static final String PROPERTY_PAGE_TITLE_IMPORT = "newsletter.import_subscribers.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_IMPORT_DELETE = "newsletter.import_delete_subscribers.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_NEWSLETTERS = "newsletter.manage_newsletters.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_ARCHIVE = "newsletter.manage_archive.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_NEWSLETTERS_PROPERTIES = "newsletter.manage_newsletters_properties.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE = "newsletter.create_newsletter.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_TOPIC_CONFIGURATION = "newsletter.modify_topic_config.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CONFIRM_SEND = "newsletter.prepare_newsletter.stepConfirm";

    private static final String LABLE_COPY_NEWSLETTER_TITLE = "newsletter.manage_newsletters.copy.title";
    private static final int NEWSLETTER_NAME_MAX_LENGTH = 50;

    private static final String PROPERTY_PAGE_TITLE_COMPOSE = "newsletter.compose_newsletter.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY = "newsletter.modify_newsletter.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MANAGE_SUBSCRIBERS = "newsletter.manage_subscribers.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_PREPARE = "newsletter.prepare_newsletter.pageTitle";
    private static final String PROPERTY_SUBSCRIBERS_PER_PAGE = "newsletter.newsletterSubscribers.itemsPerPage";
    private static final String PROPERTY_NEWSLETTERS_PER_PAGE = "newsletter.newslettersPerPage";
    private static final String PROPERTY_ARCHIVE_PER_PAGE = "newsletter.archive.itemsPerPage";
    private static final String PAGER_NEWSLETTERS = "newsletterList";
    private static final String PAGER_ARCHIVE = "newsletterArchive";
    private static final String PAGER_SUBSCRIBERS = "newsletterSubscribers";
    private static final String PROPERTY_LABEL_UNSUBSCRIBE_TRUE = "newsletter.unsubscribe.true";
    private static final String PROPERTY_LABEL_UNSUBSCRIBE_FALSE = "newsletter.unsubscribe.false";
    private static final String PROPERTY_TEST_SUBJECT = "newsletter.test.subject";

    private static final String CONSTANT_DEFAULT_NEWSLETTERS_SORT_ATTRIBUTE = "name";
    private static final int CONSTANT_RECENT_SENDINGS_DAYS = 30;
    private static final String SEPARATOR_SENDING_IDS = "_";

    // constants
    private String _strNewsletterListSearchName;
    private LocalDate _dateNewsletterListSearch;
    private String [ ] _multiSelectionValues;
    private String _strSortedAttributeName;
    private Boolean _bIsAscSort;
    @Inject
    private NewsletterService _newsletterService;
    @Inject
    private SecurityTokenHandler _securityTokenHandler;
    @Inject
    private NewsletterTopicService _newsletterTopicService;
    @Inject
    private NewsLetterRegistrationService _registrationService;
    @Inject
    private EditorProfileService _editorProfileService;
    @Inject
    @ConfigProperty( name = "newsletter.confirm.limit", defaultValue = "7" )
    private int _nConfirmLimit;
    @Inject
    @ConfigProperty( name = "newsletter.limit.min", defaultValue = "0" )
    private int _nLimitMinSubscriber;
    @Inject
    @ConfigProperty( name = "newsletter.limit.max", defaultValue = "10000000" )
    private int _nLimitMaxSubscriber;
    @Inject
    @ConfigProperty( name = "newsletter.csv.import.delimiter", defaultValue = ";" )
    private String _strImportDelimiter;
    @Inject
    @ConfigProperty( name = "newsletter.csv.import.columnindex", defaultValue = "0" )
    private int _nImportEmailColumnIndex;
    @Inject
    @Pager( name = PAGER_NEWSLETTERS, listBookmark = MARK_NEWSLETTER_PAGE_ITEMS, defaultItemsPerPage = PROPERTY_NEWSLETTERS_PER_PAGE, baseUrl = CONTROLLER_PATH + CONTROLLER_JSP )
    private IPager<NewsLetter, NewsLetter> _newsletterPager;
    @Inject
    @Pager( name = PAGER_ARCHIVE, listBookmark = MARK_NEWSLETTER_LIST, defaultItemsPerPage = PROPERTY_ARCHIVE_PER_PAGE, baseUrl = CONTROLLER_PATH + CONTROLLER_JSP )
    private IPager<SendingNewsLetter, SendingNewsLetter> _archivePager;
    @Inject
    @Pager( name = PAGER_SUBSCRIBERS, listBookmark = MARK_SUBSCRIBERS_LIST, defaultItemsPerPage = PROPERTY_SUBSCRIBERS_PER_PAGE, baseUrl = CONTROLLER_PATH + CONTROLLER_JSP )
    private IPager<Subscriber, Subscriber> _subscriberPager;

    /**
     * Returns the list of newsletters
     *
     * @param request
     *            the HTTP request
     * @param model
     *            the model of the page
     * @return the html code for display the newsletters list
     */
    @View( value = VIEW_MANAGE_NEWSLETTERS, defaultView = true, securityTokenAction = ACTION_COPY_NEWSLETTER )
    public String getManageNewsLetters( HttpServletRequest request, Models model )
    {
        if ( request.getParameter( PARAMETER_SEARCH_NAME ) != null || request.getParameter( PARAMETER_SEARCH_DATE ) != null )
        {
            String strRequestName = StringUtils.trimToEmpty( request.getParameter( PARAMETER_SEARCH_NAME ) );
            LocalDate dateRequest = parseSearchDate( request.getParameter( PARAMETER_SEARCH_DATE ) );
            _strNewsletterListSearchName = strRequestName;
            _dateNewsletterListSearch = dateRequest;
        }
        String strSearchName = StringUtils.defaultString( _strNewsletterListSearchName );
        LocalDate dateSearch = _dateNewsletterListSearch;

        Collection<NewsLetter> listNewsletter = NewsLetterHome.findAll( getPlugin( ) );
        listNewsletter = AdminWorkgroupService.getAuthorizedCollection( listNewsletter, (User) getUser( ) );

        if ( _strSortedAttributeName == null )
        {
            _strSortedAttributeName = CONSTANT_DEFAULT_NEWSLETTERS_SORT_ATTRIBUTE;
            _bIsAscSort = Boolean.TRUE;
        }

        if ( request.getParameter( Parameters.SORTED_ATTRIBUTE_NAME ) != null )
        {
            _strSortedAttributeName = request.getParameter( Parameters.SORTED_ATTRIBUTE_NAME );
            _bIsAscSort = Boolean.parseBoolean( request.getParameter( Parameters.SORTED_ASC ) );
        }

        UrlItem url = new UrlItem( getViewFullUrl( VIEW_MANAGE_NEWSLETTERS ) );
        url.addParameter( Parameters.SORTED_ATTRIBUTE_NAME, _strSortedAttributeName );
        url.addParameter( Parameters.SORTED_ASC, Boolean.toString( _bIsAscSort ) );
        listNewsletter = listNewsletter.stream( ).sorted( new AttributeComparator( _strSortedAttributeName, _bIsAscSort ) ).collect( Collectors.toList( ) );

        if ( StringUtils.isNotEmpty( strSearchName ) )
        {
            url.addParameter( PARAMETER_SEARCH_NAME, URLEncoder.encode( strSearchName, StandardCharsets.UTF_8 ) );
        }
        if ( dateSearch != null )
        {
            url.addParameter( PARAMETER_SEARCH_DATE, dateSearch.toString( ) );
        }

        Map<Integer, Pair<Integer, Integer>> mapSubscriberCounts = NewsLetterHome.findSubscriberCountsByNewsletter( getPlugin( ) );
        Timestamp dateRecentSendings = new Timestamp( System.currentTimeMillis( ) - TimeUnit.DAYS.toMillis( CONSTANT_RECENT_SENDINGS_DAYS ) );
        Map<Integer, SendingStatistics> mapSendingStatistics = SendingNewsLetterHome.findSendingStatisticsByNewsletter( dateRecentSendings, getPlugin( ) );
        Map<Integer, String> mapTemplateDescriptions = new HashMap<>( );
        for ( NewsLetterTemplate newsletterTemplate : NewsLetterTemplateHome.getTemplatesList( getPlugin( ) ) )
        {
            mapTemplateDescriptions.put( newsletterTemplate.getId( ), StringUtils.defaultString( newsletterTemplate.getDescription( ) ) );
        }
        Pair<Integer, Integer> emptyCounts = new Pair<>( 0, 0 );
        SendingStatistics emptyStatistics = new SendingStatistics( 0, 0, null );

        int nCountActiveSubscribers = 0;
        int nCountPendingSubscribers = 0;
        int nCountRecentSendings = 0;
        List<NewsLetter> listMatchingNewsletters = new ArrayList<>( );

        for ( NewsLetter newsletter : listNewsletter )
        {
            Pair<Integer, Integer> subscriberCounts = mapSubscriberCounts.getOrDefault( newsletter.getId( ), emptyCounts );
            SendingStatistics sendingStatistics = mapSendingStatistics.getOrDefault( newsletter.getId( ), emptyStatistics );
            nCountActiveSubscribers += subscriberCounts.getSecond( );
            nCountPendingSubscribers += subscriberCounts.getFirst( ) - subscriberCounts.getSecond( );
            nCountRecentSendings += sendingStatistics.getRecentSendings( );

            if ( matchesSearch( newsletter, sendingStatistics.getDateLastSending( ), strSearchName, dateSearch ) )
            {
                listMatchingNewsletters.add( newsletter );
            }
        }

        _newsletterPager.withBaseUrl( url.getUrl( ) ).withListItem( listMatchingNewsletters ).populateModels( request, model, getLocale( ) );
        @SuppressWarnings( "unchecked" )
        List<NewsLetter> listPageNewsletters = (List<NewsLetter>) model.get( MARK_NEWSLETTER_PAGE_ITEMS );
        List<Map<String, Object>> listNewsletterDisplay = new ArrayList<>( );

        for ( NewsLetter newsletter : listPageNewsletters )
        {
            Pair<Integer, Integer> subscriberCounts = mapSubscriberCounts.getOrDefault( newsletter.getId( ), emptyCounts );
            SendingStatistics sendingStatistics = mapSendingStatistics.getOrDefault( newsletter.getId( ), emptyStatistics );
            Map<String, Object> newsletterDisplay = new HashMap<>( );
            newsletterDisplay.put( MARK_NEWSLETTER, newsletter );
            newsletterDisplay.put( MARK_NEWSLETTER_ALLOW_CREATION,
                    RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_CREATE, (User) getUser( ) ) );
            newsletterDisplay.put( MARK_NEWSLETTER_ALLOW_MANAGE_ARCHIVE,
                    RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_ARCHIVE, (User) getUser( ) ) );
            newsletterDisplay.put( MARK_NEWSLETTER_ALLOW_DELETION,
                    RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_DELETE, (User) getUser( ) ) );
            newsletterDisplay.put( MARK_NEWSLETTER_ALLOW_MANAGE_SUBSCRIBERS,
                    RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MANAGE_SUBSCRIBERS, (User) getUser( ) ) );
            newsletterDisplay.put( MARK_NEWSLETTER_ALLOW_EXPORT_SUBSCRIBERS,
                    RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_EXPORT_SUBSCRIBERS, (User) getUser( ) ) );
            newsletterDisplay.put( MARK_NEWSLETTER_ALLOW_MODIFICATION,
                    RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MODIFY, (User) getUser( ) ) );
            newsletterDisplay.put( MARK_NEWSLETTER_ALLOW_SENDING,
                    RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_SEND, (User) getUser( ) ) );

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

            newsletterDisplay.put( MARK_NEWSLETTER_COUNT_SUBSCRIBERS, subscriberCounts.getFirst( ) );
            newsletterDisplay.put( MARK_NEWSLETTER_COUNT_ACTIVE_SUBSCRIBERS, subscriberCounts.getSecond( ) );
            newsletterDisplay.put( MARK_NEWSLETTER_COUNT_SENDINGS, sendingStatistics.getSendings( ) );
            newsletterDisplay.put( MARK_NEWSLETTER_LAST_SENDING_DATE, sendingStatistics.getDateLastSending( ) );
            newsletterDisplay.put( MARK_NEWSLETTER_TEMPLATE_DESCRIPTION, mapTemplateDescriptions.get( newsletter.getNewsLetterTemplateId( ) ) );
            listNewsletterDisplay.add( newsletterDisplay );
        }

        model.put( MARK_NEWSLETTER_LIST, listNewsletterDisplay );
        model.put( MARK_COUNT_NEWSLETTERS, listNewsletter.size( ) );
        model.put( MARK_COUNT_ACTIVE_SUBSCRIBERS, nCountActiveSubscribers );
        model.put( MARK_COUNT_PENDING_SUBSCRIBERS, nCountPendingSubscribers );
        model.put( MARK_COUNT_RECENT_SENDINGS, nCountRecentSendings );
        model.put( MARK_SEARCH_NAME, strSearchName );
        model.put( MARK_SEARCH_DATE, ( dateSearch != null ) ? java.sql.Date.valueOf( dateSearch ) : null );
        model.put( MARK_ALLOW_CREATION, isNewsletterCreationAllowed( ) );
        model.put( MARK_RIGHT_MANAGE_NEWSLETTER_PROPERTIES, RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID,
                NewsletterResourceIdService.PERMISSION_NEWSLETTER_ADVANCED_SETTINGS, (User) getUser( ) ) );

        return getPage( PROPERTY_PAGE_TITLE_NEWSLETTERS, TEMPLATE_MANAGE_NEWSLETTERS );
    }

    /**
     * Returns the list of old newsletters
     *
     * @param request
     *            the HTTP request
     * @param model
     *            the model of the page
     * @return the html code for display the newsletters list
     */
    @View( VIEW_MANAGE_ARCHIVE )
    public String getManageArchive( HttpServletRequest request, Models model )
    {
        NewsLetter newsletter = findNewsletter( request );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_ARCHIVE ) )
        {
            return redirectView( request, VIEW_MANAGE_NEWSLETTERS );
        }

        int nIdNewsletter = newsletter.getId( );
        List<SendingNewsLetter> listNewsletter = SendingNewsLetterHome.findAllLastSendingForNewsletterId( nIdNewsletter, getPlugin( ) );

        UrlItem url = new UrlItem( getViewFullUrl( VIEW_MANAGE_ARCHIVE ) );
        url.addParameter( PARAMETER_NEWSLETTER_ID, nIdNewsletter );

        _archivePager.withBaseUrl( url.getUrl( ) ).withListItem( listNewsletter ).populateModels( request, model, getLocale( ) );

        model.put( MARK_NEWSLETTER_ID, nIdNewsletter );
        model.put( MARK_NEWSLETTER, newsletter );
        fillNewsletterTabsModel( model, newsletter );

        return getPage( PROPERTY_PAGE_TITLE_ARCHIVE, TEMPLATE_MANAGE_OLD_NEWSLETTERS );
    }

    /**
     * Returns the newsletters properties
     *
     * @param request
     *            the HTTP request
     * @param model
     *            the model of the page
     * @return the html code for display the newsletters list
     * @throws AccessDeniedException
     *             If the user is not allowed to acces the feature
     */
    @View( VIEW_MANAGE_PROPERTIES )
    public String getManageNewsLettersProperties( HttpServletRequest request, Models model ) throws AccessDeniedException
    {
        checkAdvancedSettingsPermission( request );

        NewsLetterProperties properties = NewsletterPropertiesHome.find( getPlugin( ) );

        model.put( MARK_IS_ACTIVE_CAPTCHA, _registrationService.isCaptchaAvailable( ) );
        model.put( MARK_WEBAPP_URL, AppPathService.getBaseUrl( request ) );
        model.put( MARK_LOCALE, getLocale( ).getLanguage( ) );
        model.put( MARK_PROPERTIES, properties );
        model.put( MARK_EDITORS_LIST, buildEditorList( ) );
        model.put( MARK_CLEAN_RIGHT, isCleanSubscribersAllowed( ) );

        return getPage( PROPERTY_PAGE_TITLE_NEWSLETTERS_PROPERTIES, TEMPLATE_MANAGE_NEWSLETTERS_PROPERTIES );
    }

    /**
     * Processes the update form of the newsletter properties
     *
     * @param request
     *            The Http request
     * @return The redirection to the newsletters list
     * @throws AccessDeniedException
     *             If the user is not allowed to acces the feature
     */
    @Action( ACTION_MODIFY_PROPERTIES )
    public String doManageNewsLetterProperties( HttpServletRequest request ) throws AccessDeniedException
    {
        checkAdvancedSettingsPermission( request );

        String strBaseUrl = AppPathService.getBaseUrl( request );
        NewsLetterProperties properties = new NewsLetterProperties( );
        properties.setTOS( doClean( request.getParameter( PARAMETER_TERM_OF_SERVICE ), strBaseUrl ) );
        properties.setCaptchaActive( request.getParameter( PARAMETER_ACTIVE_CAPTCHA ) != null );
        properties.setValidationActive( request.getParameter( PARAMETER_ACTIVE_VALIDATION ) != null );
        properties.setEditor( buildEditorList( ).toMap( ).containsKey( request.getParameter( PARAMETER_EDITOR ) ) ? request.getParameter( PARAMETER_EDITOR )
                : NewsLetterProperties.EDITOR_GRAPESJS );

        NewsletterPropertiesHome.update( properties, getPlugin( ) );

        return redirectView( request, VIEW_MANAGE_NEWSLETTERS );
    }

    /**
     * Asks the confirmation of the cleaning of the subscribers who did not confirm their subscription in time. The cleaning returns to the
     * subscribers of all the newsletters when the request comes from them.
     *
     * @param request
     *            The Http request
     * @return the url of the confirmation message
     */
    @View( value = VIEW_CONFIRM_CLEAN_SUBSCRIBERS, securityTokenAction = ACTION_CLEAN_SUBSCRIBERS )
    public String getConfirmCleanSubscribers( HttpServletRequest request )
    {
        if ( !isCleanSubscribersAllowed( ) )
        {
            return redirectAccessDenied( request );
        }

        Object [ ] messageArgs = {
                Integer.toString( _nConfirmLimit )
        };

        UrlItem url = new UrlItem( getActionUrl( ACTION_CLEAN_SUBSCRIBERS ) );

        if ( isReturnToSubscribers( request ) )
        {
            url.addParameter( PARAMETER_RETURN_VIEW, VALUE_RETURN_SUBSCRIBERS );
        }

        return redirect( request,
                AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_CLEAN_SUBSCRIBERS, messageArgs, url.getUrl( ), AdminMessage.TYPE_CONFIRMATION ) );
    }

    /**
     * Processes subscribers cleaning
     *
     * @param request
     *            The Http request
     * @return the url of the result message
     */
    @Action( ACTION_CLEAN_SUBSCRIBERS )
    public String doCleanSubscribers( HttpServletRequest request )
    {
        if ( !isCleanSubscribersAllowed( ) )
        {
            return redirectAccessDenied( request );
        }

        _registrationService.doRemoveOldUnconfirmed( );

        Object [ ] messages = {
                Integer.toString( _nConfirmLimit )
        };

        String strBackUrl = isReturnToSubscribers( request ) ? SubscriberJspBean.CONTROLLER_PATH + SubscriberJspBean.CONTROLLER_JSP
                : getViewFullUrl( VIEW_MANAGE_PROPERTIES );

        return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_SUBSCRIBERS_CLEANED, messages, strBackUrl, AdminMessage.TYPE_INFO ) );
    }

    /**
     * Tells whether the cleaning of the subscribers has been asked from the subscribers of all the newsletters
     *
     * @param request
     *            The Http request
     * @return true if the cleaning returns to the subscribers of all the newsletters
     */
    private static boolean isReturnToSubscribers( HttpServletRequest request )
    {
        return VALUE_RETURN_SUBSCRIBERS.equals( request.getParameter( PARAMETER_RETURN_VIEW ) );
    }

    /**
     * Returns the newsletter form for creation
     *
     * @param request
     *            The Http request
     * @param model
     *            the model of the page
     * @return the html code of the newsletter form
     */
    @View( VIEW_CREATE_NEWSLETTER )
    public String getCreateNewsLetter( HttpServletRequest request, Models model )
    {
        if ( !isNewsletterCreationAllowed( ) )
        {
            return redirectView( request, VIEW_MANAGE_NEWSLETTERS );
        }

        model.put( MARK_DATE_LAST_SEND, DateUtil.getCurrentDateString( getLocale( ) ) );
        model.put( MARK_WORKGROUP_LIST, AdminWorkgroupService.getUserWorkgroups( getUser( ), getLocale( ) ) );
        model.put( MARK_UNSUBSCRIBE_LIST, buildUnsubscribeList( NewsLetterConstants.PROPERTY_UNSUBSCRIBE_TRUE, NewsLetterConstants.PROPERTY_UNSUBSCRIBE_FALSE ) );
        model.put( MARK_LOCALE, getLocale( ) );
        model.put( MARK_WEBAPP_URL, AppPathService.getProdUrl( request ) );

        return getPage( PROPERTY_PAGE_TITLE_CREATE, TEMPLATE_CREATE_NEWSLETTER );
    }

    /**
     * Processes the creation form of a new newsletter
     *
     * @param request
     *            The Http request
     * @return The redirection to the newsletters list, or the url of the error message
     */
    @Action( ACTION_CREATE_NEWSLETTER )
    public String doCreateNewsLetter( HttpServletRequest request )
    {
        String strNewsletterName = request.getParameter( PARAMETER_NEWSLETTER_NAME );
        String strNewsletterDescription = request.getParameter( PARAMETER_NEWSLETTER_DESCRIPTION );
        String strDateFirstSend = request.getParameter( PARAMETER_DATE_FIRST_SEND );
        String strWorkGroup = request.getParameter( PARAMETER_NEWSLETTER_WORKGROUP );
        String strSenderName = request.getParameter( PARAMETER_NEWSLETTER_SENDER_NAME );
        String strSenderMail = request.getParameter( PARAMETER_NEWSLETTER_SENDER_MAIL );
        String strTestRecipients = request.getParameter( PARAMETER_TEST_RECIPIENTS );
        String strTestSubject = request.getParameter( PARAMETER_TEST_SUBJECT );

        if ( !isNewsletterCreationAllowed( ) )
        {
            return redirectView( request, VIEW_MANAGE_NEWSLETTERS );
        }

        if ( StringUtils.isEmpty( strSenderMail ) || StringUtils.isEmpty( strTestRecipients ) || StringUtils.isEmpty( strNewsletterName )
                || StringUtils.isEmpty( strDateFirstSend ) || StringUtils.isEmpty( strWorkGroup ) || StringUtils.isEmpty( strSenderName ) )
        {
            return redirectMessage( request, Messages.MANDATORY_FIELDS );
        }

        String strMessageParam = null;
        String strMessage = null;

        String strWrongEmail = isWrongEmail( strTestRecipients );
        if ( StringUtils.isNotEmpty( strWrongEmail ) )
        {
            strMessageParam = strWrongEmail;
            strMessage = MESSAGE_WRONG_EMAIL;
        }

        if ( strMessageParam == null && !StringUtil.checkEmail( strSenderMail ) )
        {
            strMessageParam = strSenderMail;
            strMessage = MESSAGE_WRONG_EMAIL_SENDER;
        }

        if ( strMessageParam != null )
        {
            Object [ ] messageArgs = {
                    strMessageParam
            };
            return redirect( request, AdminMessageService.getMessageUrl( request, strMessage, messageArgs, AdminMessage.TYPE_STOP ) );
        }

        Date date = DateUtil.parseIsoDate( strDateFirstSend );

        if ( date == null )
        {
            return redirectMessage( request, MESSAGE_WRONG_DATE_LAST_SEND );
        }

        NewsLetter newsletter = new NewsLetter( );
        newsletter.setName( strNewsletterName );
        newsletter.setDateLastSending( new Timestamp( date.getTime( ) ) );
        newsletter.setDescription( strNewsletterDescription );
        newsletter.setWorkgroup( strWorkGroup );
        newsletter.setTestRecipients( strTestRecipients );
        newsletter.setTestSubject( strTestSubject );
        newsletter.setNewsletterSenderMail( strSenderMail );
        newsletter.setNewsletterSenderName( strSenderName );
        newsletter.setNbSections( 1 );
        newsletter.setUnsubscribe( request.getParameter( PARAMETER_NEWSLETTER_UNSUBSCRIBE ) );

        NewsLetterHome.create( newsletter, getPlugin( ) );

        return redirectView( request, VIEW_MANAGE_NEWSLETTERS );
    }

    /**
     * Returns the newsletter form of newsletter composition. The composition needs at least one newsletter template.
     *
     * @param request
     *            The Http rquest
     * @param model
     *            the model of the page
     * @return the html code of the newsletter composition form
     */
    @View( value = VIEW_COMPOSE_NEWSLETTER, securityTokenAction = ACTION_REGISTER_NEWSLETTER )
    public String getComposeNewsLetter( HttpServletRequest request, Models model )
    {
        NewsLetter newsletter = findNewsletter( request );
        AdminUser user = getUser( );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MODIFY ) )
        {
            return redirectView( request, VIEW_MANAGE_NEWSLETTERS );
        }

        if ( NewsLetterTemplateHome.getTemplatesCollectionByType( NewsLetterTemplate.RESOURCE_TYPE, getPlugin( ) ).isEmpty( ) )
        {
            return redirectMessage( request, MESSAGE_NO_TEMPLATE );
        }

        String strBaseUrl = AppPathService.getProdUrl( request );
        String strPathImageTemplate = _newsletterService.getImageFolderPath( AppPathService.getBaseUrl( request ) );

        int nTemplateNewsLetterId = newsletter.getNewsLetterTemplateId( );
        String strHtmlContent = null;

        if ( request.getParameter( PARAMETER_GENERATE ) == null )
        {
            strHtmlContent = ( newsletter.getHtml( ) == null ) ? NewsLetterConstants.CONSTANT_EMPTY_STRING : newsletter.getHtml( );
        }
        else
        {
            if ( nTemplateNewsLetterId == 0 )
            {
                Collection<NewsLetterTemplate> newsletterTemplatesList = NewsLetterTemplateHome.getTemplatesCollectionByType( NewsLetterTemplate.RESOURCE_TYPE,
                        getPlugin( ) );
                newsletterTemplatesList = AdminWorkgroupService.getAuthorizedCollection( newsletterTemplatesList, (User) getUser( ) );

                for ( NewsLetterTemplate template : newsletterTemplatesList )
                {
                    if ( Objects.equals( template.getResourceTypeCode( ), NewsLetter.RESOURCE_TYPE ) )
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
                strHtmlContent = NewsLetterConstants.CONSTANT_EMPTY_STRING;
            }
        }

        strHtmlContent = strHtmlContent.replaceAll( NewsLetterConstants.MARK_BASE_URL, strBaseUrl );
        strHtmlContent = strHtmlContent.replaceAll( NewsLetterConstants.WEBAPP_PATH_FOR_LINKSERVICE, strBaseUrl );

        model.put( MARK_HTML_CONTENT, toMarkup( strHtmlContent ) );
        model.put( MARK_LUTECE_PROD_URL, _newsletterService.getProdUrl( ) );
        model.put( MARK_NEWSLETTER, newsletter );
        model.put( MARK_NEWSLETTER_TEMPLATE_ID, nTemplateNewsLetterId );
        model.put( MARK_WEBAPP_URL, AppPathService.getBaseUrl( request ) );
        model.put( MARK_LOCALE, getLocale( ).getLanguage( ) );
        model.put( MARK_IMG_PATH, strPathImageTemplate );
        NewsLetterProperties properties = NewsletterPropertiesHome.find( getPlugin( ) );
        String strEditor = getChosenEditor( properties );
        model.put( MARK_LUTECE_EDITOR, properties.isLuteceEditor( ) );

        if ( StringUtils.isNotEmpty( strEditor ) )
        {
            model.put( MARK_NEWSLETTER_EDITOR, strEditor );
            model.put( MARK_TEXT_EDITOR_PROFILE, buildEditorProfile( strEditor ) );
        }
        fillNewsletterTabsModel( model, newsletter );

        return getPage( PROPERTY_PAGE_TITLE_COMPOSE, TEMPLATE_COMPOSE_NEWSLETTER );
    }

    /**
     * Returns the newsletter form for modification
     *
     * @param request
     *            The Http request
     * @param model
     *            the model of the page
     * @return the html code of the newsletter form
     */
    @View( VIEW_MODIFY_NEWSLETTER )
    public String getModifyNewsLetter( HttpServletRequest request, Models model )
    {
        NewsLetter newsletter = findNewsletter( request );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MODIFY ) )
        {
            return redirectView( request, VIEW_MANAGE_NEWSLETTERS );
        }

        model.put( MARK_NEWSLETTER, newsletter );
        model.put( MARK_NEWSLETTER_DESCRIPTION, newsletter.getDescription( ) );
        model.put( MARK_DATE_LAST_SENDING, DateUtil.getDateString( newsletter.getDateLastSending( ), getLocale( ) ) );
        model.put( MARK_WORKGROUP_LIST, AdminWorkgroupService.getUserWorkgroups( getUser( ), getLocale( ) ) );
        model.put( MARK_UNSUBSCRIBE_LIST, buildUnsubscribeList( "TRUE", "FALSE" ) );
        model.put( MARK_LOCALE, getLocale( ) );
        model.put( MARK_WEBAPP_URL, AppPathService.getProdUrl( request ) );
        fillNewsletterTabsModel( model, newsletter );

        return getPage( PROPERTY_PAGE_TITLE_MODIFY, TEMPLATE_MODIFY_NEWSLETTER );
    }

    /**
     * Processes the update form of the newsletter whose identifier is in the http request
     *
     * @param request
     *            The Http request
     * @return The redirection to the newsletters list, or the url of the error message
     */
    @Action( ACTION_MODIFY_NEWSLETTER )
    public String doModifyNewsLetter( HttpServletRequest request )
    {
        String strSenderName = request.getParameter( PARAMETER_NEWSLETTER_SENDER_NAME );
        String strSenderMail = request.getParameter( PARAMETER_NEWSLETTER_SENDER_MAIL );
        String strNewsletterName = request.getParameter( PARAMETER_NEWSLETTER_NAME );
        String strNewsletterDescription = request.getParameter( PARAMETER_NEWSLETTER_DESCRIPTION );
        String strWorkGroup = request.getParameter( PARAMETER_NEWSLETTER_WORKGROUP );
        String strDateLastSend = request.getParameter( PARAMETER_DATE_LAST_SEND );
        String strTestRecipients = request.getParameter( PARAMETER_TEST_RECIPIENTS );
        String strTestSubject = request.getParameter( PARAMETER_TEST_SUBJECT );
        NewsLetter newsletter = findNewsletter( request );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MODIFY ) )
        {
            return redirectAccessDenied( request );
        }

        if ( StringUtils.isEmpty( strSenderMail ) || StringUtils.isEmpty( strTestRecipients ) || StringUtils.isEmpty( strNewsletterName )
                || StringUtils.isEmpty( strDateLastSend ) || StringUtils.isEmpty( strWorkGroup ) || StringUtils.isEmpty( strSenderName ) )
        {
            return redirectMessage( request, Messages.MANDATORY_FIELDS );
        }

        strTestRecipients = cleanEmails( strTestRecipients );

        String strWrongMail = isWrongEmail( strTestRecipients );
        if ( StringUtils.isNotEmpty( strWrongMail ) )
        {
            Object [ ] messageArgs = {
                    strWrongMail
            };

            return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_WRONG_EMAIL, messageArgs, AdminMessage.TYPE_STOP ) );
        }

        if ( !StringUtil.checkEmail( strSenderMail ) )
        {
            Object [ ] messageArgs = {
                    strSenderMail
            };

            return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_WRONG_EMAIL_SENDER, messageArgs, AdminMessage.TYPE_STOP ) );
        }

        newsletter.setName( strNewsletterName );
        newsletter.setDescription( strNewsletterDescription );
        newsletter.setTestRecipients( strTestRecipients );
        newsletter.setTestSubject( strTestSubject );
        newsletter.setNewsletterSenderMail( strSenderMail );
        newsletter.setNewsletterSenderName( strSenderName );

        Date date = DateUtil.parseIsoDate( strDateLastSend );

        if ( date != null )
        {
            newsletter.setDateLastSending( new Timestamp( date.getTime( ) ) );
        }

        newsletter.setUnsubscribe( request.getParameter( PARAMETER_NEWSLETTER_UNSUBSCRIBE ) );
        newsletter.setWorkgroup( strWorkGroup );

        NewsLetterHome.update( newsletter, getPlugin( ) );

        return redirectView( request, VIEW_MANAGE_NEWSLETTERS );
    }

    /**
     * Asks the confirmation of the unregistration of a subscriber
     *
     * @param request
     *            The Http request
     * @return the url of the confirmation message
     */
    @View( value = VIEW_CONFIRM_REMOVE_SUBSCRIBER, securityTokenAction = ACTION_REMOVE_SUBSCRIBER )
    public String getConfirmRemoveSubscriber( HttpServletRequest request )
    {
        NewsLetter newsletter = findNewsletter( request );

        String strSubscriberId = request.getParameter( PARAMETER_SUBSCRIBER_ID );
        Subscriber subscriber = StringUtils.isNumeric( strSubscriberId ) ? SubscriberHome.findByPrimaryKey( Integer.parseInt( strSubscriberId ), getPlugin( ) )
                : null;

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MANAGE_SUBSCRIBERS ) || subscriber == null )
        {
            return redirectAccessDenied( request );
        }

        UrlItem urlItem = new UrlItem( getActionUrl( ACTION_REMOVE_SUBSCRIBER ) );
        urlItem.addParameter( PARAMETER_NEWSLETTER_ID, newsletter.getId( ) );
        urlItem.addParameter( PARAMETER_SUBSCRIBER_ID, subscriber.getId( ) );

        return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_SUBSCRIBER, new Object [ ] {
                subscriber.getEmail( )
        }, urlItem.getUrl( ), AdminMessage.TYPE_CONFIRMATION ) );
    }

    /**
     * Asks the confirmation of the unregistration of the subscribers selected in the list; the selection is kept until the action
     *
     * @param request
     *            The Http request
     * @return the url of the confirmation message
     */
    @View( value = VIEW_CONFIRM_REMOVE_SELECTED_SUBSCRIBERS, securityTokenAction = ACTION_REMOVE_SELECTED_SUBSCRIBERS )
    public String getConfirmRemoveSelectedSubscribers( HttpServletRequest request )
    {
        NewsLetter newsletter = findNewsletter( request );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MANAGE_SUBSCRIBERS ) )
        {
            return redirectAccessDenied( request );
        }

        _multiSelectionValues = request.getParameterValues( PARAMETER_SUBSCRIBERS_SELECTION );

        UrlItem urlItem = new UrlItem( getActionUrl( ACTION_REMOVE_SELECTED_SUBSCRIBERS ) );
        urlItem.addParameter( PARAMETER_NEWSLETTER_ID, newsletter.getId( ) );

        return redirect( request,
                AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_SELECTION, urlItem.getUrl( ), AdminMessage.TYPE_CONFIRMATION ) );
    }

    /**
     * Processes the unregistration of a subscriber for a newsletter
     *
     * @param request
     *            The Http request
     * @return the redirection to the subscribers of the newsletter
     */
    @Action( ACTION_REMOVE_SUBSCRIBER )
    public String doUnregistrationAdmin( HttpServletRequest request )
    {
        NewsLetter newsletter = findNewsletter( request );
        String strSubscriberId = request.getParameter( PARAMETER_SUBSCRIBER_ID );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MANAGE_SUBSCRIBERS ) || !StringUtils.isNumeric( strSubscriberId ) )
        {
            return redirectAccessDenied( request );
        }

        Subscriber subscriber = SubscriberHome.findByPrimaryKey( Integer.parseInt( strSubscriberId ), getPlugin( ) );

        if ( subscriber != null )
        {
            _newsletterService.removeSubscriberFromNewsletter( subscriber, newsletter.getId( ), getPlugin( ) );
        }

        return redirect( request, VIEW_MANAGE_SUBSCRIBERS, PARAMETER_NEWSLETTER_ID, newsletter.getId( ) );
    }

    /**
     * Processes the unregistration of selected subscribers
     *
     * @param request
     *            The Http request
     * @return the redirection to the subscribers of the newsletter
     */
    @Action( ACTION_REMOVE_SELECTED_SUBSCRIBERS )
    public String doRemoveSelection( HttpServletRequest request )
    {
        NewsLetter newsletter = findNewsletter( request );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MANAGE_SUBSCRIBERS ) )
        {
            return redirectAccessDenied( request );
        }

        if ( ArrayUtils.isNotEmpty( _multiSelectionValues ) )
        {
            for ( String strId : _multiSelectionValues )
            {
                Subscriber subscriber = StringUtils.isNumeric( strId ) ? SubscriberHome.findByPrimaryKey( Integer.parseInt( strId ), getPlugin( ) ) : null;

                if ( subscriber != null )
                {
                    _newsletterService.removeSubscriberFromNewsletter( subscriber, newsletter.getId( ), getPlugin( ) );
                }
            }
        }
        _multiSelectionValues = null;

        return redirect( request, VIEW_MANAGE_SUBSCRIBERS, PARAMETER_NEWSLETTER_ID, newsletter.getId( ) );
    }

    /**
     * Asks the confirmation of the removal of a newsletter
     *
     * @param request
     *            The Http request
     * @return the url of the confirmation message
     */
    @View( value = VIEW_CONFIRM_REMOVE_NEWSLETTER, securityTokenAction = ACTION_REMOVE_NEWSLETTER )
    public String getRemoveNewsLetter( HttpServletRequest request )
    {
        NewsLetter newsletter = findNewsletter( request );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_DELETE ) )
        {
            return redirectAccessDenied( request );
        }

        if ( NewsLetterHome.checkLinkedPortlets( newsletter.getId( ) ) )
        {
            return redirectMessage( request, MESSAGE_LINKED_TO_NEWSLETTER );
        }

        UrlItem urlItem = new UrlItem( getActionUrl( ACTION_REMOVE_NEWSLETTER ) );
        urlItem.addParameter( PARAMETER_NEWSLETTER_ID, newsletter.getId( ) );

        return redirect( request,
                AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_NEWSLETTER, urlItem.getUrl( ), AdminMessage.TYPE_CONFIRMATION ) );
    }

    /**
     * Processes the removal of a newsletter, with its subscriptions and its topics
     *
     * @param request
     *            The Http request
     * @return the redirection to the newsletters list
     */
    @Action( ACTION_REMOVE_NEWSLETTER )
    public String doRemoveNewsLetter( HttpServletRequest request )
    {
        NewsLetter newsletter = findNewsletter( request );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_DELETE ) )
        {
            return redirectAccessDenied( request );
        }

        if ( NewsLetterHome.checkLinkedPortlets( newsletter.getId( ) ) )
        {
            return redirectMessage( request, MESSAGE_LINKED_TO_NEWSLETTER );
        }

        int nNewsletterId = newsletter.getId( );
        Collection<Subscriber> list = SubscriberHome.findSubscribers( nNewsletterId, NewsLetterConstants.CONSTANT_EMPTY_STRING,
                _nLimitMinSubscriber, _nLimitMaxSubscriber, getPlugin( ) );

        for ( Subscriber subscriber : list )
        {
            NewsLetterHome.removeSubscriber( nNewsletterId, subscriber.getId( ), getPlugin( ) );
        }

        // removes the archived sendings: the next newsletter may reuse the identifier
        for ( SendingNewsLetter sending : SendingNewsLetterHome.findAllLastSendingForNewsletterId( nNewsletterId, getPlugin( ) ) )
        {
            SendingNewsLetterHome.remove( sending.getId( ), getPlugin( ) );
        }

        NewsletterTopicHome.removeAllByIdNewsletter( nNewsletterId, getPlugin( ) );
        NewsLetterHome.remove( nNewsletterId, getPlugin( ) );

        return redirectView( request, VIEW_MANAGE_NEWSLETTERS );
    }

    /**
     * Asks the confirmation of the removal of a newsletter archive
     *
     * @param request
     *            The Http request
     * @return the url of the confirmation message
     */
    @View( value = VIEW_CONFIRM_REMOVE_SENDING, securityTokenAction = ACTION_REMOVE_SENDING )
    public String getRemoveSendingNewsLetter( HttpServletRequest request )
    {
        NewsLetter newsletter = findNewsletter( request );
        String strSendingNewsletterId = request.getParameter( PARAMETER_SENDING_NEWSLETTER_ID );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_ARCHIVE ) || !StringUtils.isNumeric( strSendingNewsletterId ) )
        {
            return redirectAccessDenied( request );
        }

        UrlItem urlItem = new UrlItem( getActionUrl( ACTION_REMOVE_SENDING ) );
        urlItem.addParameter( PARAMETER_NEWSLETTER_ID, newsletter.getId( ) );
        urlItem.addParameter( PARAMETER_SENDING_NEWSLETTER_ID, strSendingNewsletterId );

        return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_SENDING_NEWSLETTER, urlItem.getUrl( ),
                AdminMessage.TYPE_CONFIRMATION ) );
    }

    /**
     * Asks the confirmation of the removal of the newsletter archives selected in the list
     *
     * @param request
     *            The Http request
     * @return the url of the confirmation message
     */
    @View( value = VIEW_CONFIRM_REMOVE_MULTI_SENDING, securityTokenAction = ACTION_REMOVE_MULTI_SENDING )
    public String getRemoveMultiNewsLetter( HttpServletRequest request )
    {
        NewsLetter newsletter = findNewsletter( request );
        String [ ] strSendingNewsletterId = request.getParameterValues( PARAMETER_NEWSLETTER_SELECTION );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_ARCHIVE ) )
        {
            return redirectAccessDenied( request );
        }

        if ( ArrayUtils.isEmpty( strSendingNewsletterId ) )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, PROPERTY_SELECT_MANDATORY, AdminMessage.TYPE_ERROR ) );
        }

        UrlItem urlItem = new UrlItem( getActionUrl( ACTION_REMOVE_MULTI_SENDING ) );
        urlItem.addParameter( PARAMETER_NEWSLETTER_ID, newsletter.getId( ) );
        urlItem.addParameter( PARAMETER_SENDING_NEWSLETTER_ID, String.join( SEPARATOR_SENDING_IDS, strSendingNewsletterId ) );

        return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_MULTI_SENDING_NEWSLETTER, urlItem.getUrl( ),
                AdminMessage.TYPE_CONFIRMATION ) );
    }

    /**
     * Processes the removal of a newsletter archive
     *
     * @param request
     *            The Http request
     * @return the redirection to the archive of the newsletter
     */
    @Action( ACTION_REMOVE_SENDING )
    public String doRemoveSendingNewsLetter( HttpServletRequest request )
    {
        NewsLetter newsletter = findNewsletter( request );
        String strSendingNewsletterId = request.getParameter( PARAMETER_SENDING_NEWSLETTER_ID );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_ARCHIVE ) || !StringUtils.isNumeric( strSendingNewsletterId ) )
        {
            return redirectAccessDenied( request );
        }

        removeSending( newsletter, Integer.parseInt( strSendingNewsletterId ) );

        return redirect( request, VIEW_MANAGE_ARCHIVE, PARAMETER_NEWSLETTER_ID, newsletter.getId( ) );
    }

    /**
     * Processes the removal of the selected newsletter archives
     *
     * @param request
     *            The Http request
     * @return the redirection to the archive of the newsletter
     */
    @Action( ACTION_REMOVE_MULTI_SENDING )
    public String doRemoveMultiNewsLetter( HttpServletRequest request )
    {
        NewsLetter newsletter = findNewsletter( request );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_ARCHIVE ) )
        {
            return redirectAccessDenied( request );
        }

        for ( String strId : StringUtils.defaultString( request.getParameter( PARAMETER_SENDING_NEWSLETTER_ID ) ).split( SEPARATOR_SENDING_IDS ) )
        {
            if ( StringUtils.isNumeric( strId ) )
            {
                removeSending( newsletter, Integer.parseInt( strId ) );
            }
        }

        return redirect( request, VIEW_MANAGE_ARCHIVE, PARAMETER_NEWSLETTER_ID, newsletter.getId( ) );
    }

    /**
     * Builds the newsletter's subscribers management page
     *
     * @param request
     *            The HTTP request
     * @param model
     *            the model of the page
     * @return the html code for newsletter's subscribers management page
     */
    @View( value = VIEW_MANAGE_SUBSCRIBERS, securityTokenAction = ACTION_ADD_SUBSCRIBER )
    public String getManageSubscribers( HttpServletRequest request, Models model )
    {
        NewsLetter newsletter = findNewsletter( request );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MANAGE_SUBSCRIBERS ) )
        {
            return redirectView( request, VIEW_MANAGE_NEWSLETTERS );
        }

        int nNewsLetterId = newsletter.getId( );
        String strNewsletterId = Integer.toString( nNewsLetterId );
        NewsLetterProperties properties = NewsletterPropertiesHome.find( getPlugin( ) );
        String strSearchString = StringUtils.defaultIfBlank( request.getParameter( NewsLetterConstants.PARAMETER_SUBSCRIBER_SEARCH ), StringUtils.EMPTY );

        List<Subscriber> refListSubscribers = new ArrayList<>( SubscriberHome.findSubscribers( nNewsLetterId, strSearchString,
                _nLimitMinSubscriber, _nLimitMaxSubscriber, getPlugin( ) ) );
        UrlItem url = new UrlItem( getViewFullUrl( VIEW_MANAGE_SUBSCRIBERS ) );
        url.addParameter( PARAMETER_NEWSLETTER_ID, nNewsLetterId );
        url.addParameter( NewsLetterConstants.PARAMETER_SUBSCRIBER_SEARCH, URLEncoder.encode( strSearchString, StandardCharsets.UTF_8 ) );

        String strSortedAttributeName = request.getParameter( Parameters.SORTED_ATTRIBUTE_NAME );

        if ( StringUtils.isNotBlank( strSortedAttributeName ) )
        {
            String strAscSort = request.getParameter( Parameters.SORTED_ASC );
            url.addParameter( Parameters.SORTED_ATTRIBUTE_NAME, strSortedAttributeName );
            url.addParameter( Parameters.SORTED_ASC, strAscSort );
            Collections.sort( refListSubscribers, new AttributeComparator( strSortedAttributeName, Boolean.parseBoolean( strAscSort ) ) );
        }

        _subscriberPager.withBaseUrl( url.getUrl( ) ).withListItem( refListSubscribers ).populateModels( request, model, getLocale( ) );

        model.put( MARK_NEWSLETTER, newsletter );
        model.put( MARK_SEARCH_STRING, strSearchString );
        model.put( MARK_DISPLAY_STATUS, properties.isValidationActive( ) );
        model.put( MARK_ADD_SUBSCRIBER_RIGHT,
                RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId, NewsletterResourceIdService.PERMISSION_ADD_SUBSCRIBER, (User) getUser( ) ) );
        model.put( MARK_IMPORT_SUBSCRIBER_RIGHT,
                RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, strNewsletterId, NewsletterResourceIdService.PERMISSION_IMPORT_SUBSCRIBERS, (User) getUser( ) ) );
        model.put( MARK_EXPORT_SUBSCRIBER_RIGTH, RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_EXPORT_SUBSCRIBERS, (User) getUser( ) ) );
        model.put( MARK_SUBSCRIBER_MANAGEMENT_RIGHT, getUser( ).checkRight( SubscriberJspBean.RIGHT_NEWSLETTER_SUBSCRIBER_MANAGEMENT ) && RBACService
                .isAuthorized( Subscriber.RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID, SubscriberResourceIdService.PERMISSION_VIEW, (User) getUser( ) ) );
        fillNewsletterTabsModel( model, newsletter );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_SUBSCRIBERS, TEMPLATE_MANAGE_SUBSCRIBERS );
    }

    /**
     * Processes the registration of a subscriber
     *
     * @param request
     *            The Http request
     * @return The redirection to the subscribers of the newsletter, or the url of the error message
     */
    @Action( ACTION_ADD_SUBSCRIBER )
    public String doAddSubscriber( HttpServletRequest request )
    {
        NewsLetter newsletter = findNewsletter( request );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_ADD_SUBSCRIBER ) )
        {
            return redirectAccessDenied( request );
        }

        String strEmail = StringUtils.trimToEmpty( request.getParameter( NewsLetterConstants.PARAMETER_EMAIL ) );

        if ( strEmail.isEmpty( ) || !StringUtil.checkEmail( strEmail ) )
        {
            return redirectMessage( request, MESSAGE_FIELD_EMAIL_VALID );
        }

        Subscriber subscriber = SubscriberHome.findByEmail( strEmail, getPlugin( ) );

        if ( subscriber == null )
        {
            subscriber = new Subscriber( );
            subscriber.setEmail( strEmail );
            SubscriberHome.create( subscriber, getPlugin( ) );
        }

        if ( NewsLetterHome.findRegistration( newsletter.getId( ), subscriber.getId( ), getPlugin( ) ) )
        {
            return redirectMessage( request, MESSAGE_EMAIL_EXISTS );
        }

        NewsLetterHome.addSubscriber( newsletter.getId( ), subscriber.getId( ), new Timestamp( System.currentTimeMillis( ) ), getPlugin( ) );

        return redirect( request, VIEW_MANAGE_SUBSCRIBERS, PARAMETER_NEWSLETTER_ID, newsletter.getId( ) );
    }

    /**
     * Builds the page of preparation before sending, with the preview of the email as the subscribers will receive it. A newsletter without
     * content cannot be sent.
     *
     * @param request
     *            the http request
     * @param model
     *            the model of the page
     * @return the html code for the preparation page
     */
    @View( VIEW_PREPARE_NEWSLETTER )
    public String getPrepareNewsLetter( HttpServletRequest request, Models model )
    {
        NewsLetter newsletter = findNewsletter( request );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_SEND ) )
        {
            return redirectView( request, VIEW_MANAGE_NEWSLETTERS );
        }

        if ( StringUtils.isEmpty( newsletter.getHtml( ) ) )
        {
            return redirectMessage( request, MESSAGE_SENDING_EMPTY_NOT_ALLOWED );
        }

        String strBaseUrl = AppPathService.getBaseUrl( request );
        String strPreviewBaseUrl = strBaseUrl.endsWith( NewsLetterConstants.CONSTANT_SLASH ) ? strBaseUrl : strBaseUrl + NewsLetterConstants.CONSTANT_SLASH;

        model.put( MARK_NEWSLETTER_OBJECT, StringUtils.defaultString( request.getParameter( PARAMETER_NEWSLETTER_OBJECT ) ) );
        model.put( MARK_PREVIEW, newsletter.getHtml( ) );
        model.put( MARK_PREVIEW_HTML, setHtmlTemplateEmail( newsletter, strPreviewBaseUrl, newsletter.getUnsubscribe( ) ).getHtml( ) );
        model.put( MARK_UNSUBSCRIBE, newsletter.getUnsubscribe( ) );
        model.put( MARK_NEWSLETTER, newsletter );
        model.put( MARK_NEWSLETTER_COUNT_ACTIVE_SUBSCRIBERS, NewsLetterHome.findNbrActiveSubscribers( newsletter.getId( ), getPlugin( ) ) );
        NewsLetterTemplate newsletterTemplate = NewsLetterTemplateHome.findByPrimaryKey( newsletter.getNewsLetterTemplateId( ), getPlugin( ) );
        model.put( MARK_NEWSLETTER_TEMPLATE_DESCRIPTION, ( newsletterTemplate != null ) ? newsletterTemplate.getDescription( ) : null );
        model.put( NewsLetterConstants.MARK_BASE_URL, strBaseUrl );
        model.put( NewsLetterConstants.MARK_SUBSCRIBER_EMAIL, NewsLetterConstants.MARK_SUBSCRIBER_EMAIL_EACH );

        return getPage( PROPERTY_PAGE_TITLE_PREPARE, TEMPLATE_PREPARE_NEWSLETTER );
    }

    /**
     * Builds the confirmation page before sending the newsletter. The newsletter must have a content, a subject and at least one subscriber to
     * send it to. The page warns when the content did not change since the last sending.
     *
     * @param request
     *            the http request
     * @param model
     *            the model of the page
     * @return the html code for the confirmation page
     */
    @View( value = VIEW_CONFIRM_SEND_NEWSLETTER, securityTokenAction = ACTION_SEND_NEWSLETTER )
    public String getConfirmSendNewsLetter( HttpServletRequest request, Models model )
    {
        NewsLetter newsletter = findNewsletter( request );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_SEND ) )
        {
            return redirectAccessDenied( request );
        }

        int nNewsletterId = newsletter.getId( );
        String strObject = request.getParameter( PARAMETER_NEWSLETTER_OBJECT );
        int nNbrSubscribers = NewsLetterHome.findNbrActiveSubscribers( nNewsletterId, getPlugin( ) );
        String strErrorMessage = null;

        if ( StringUtils.isEmpty( newsletter.getHtml( ) ) )
        {
            strErrorMessage = MESSAGE_SENDING_EMPTY_NOT_ALLOWED;
        }
        else
            if ( nNbrSubscribers == 0 )
            {
                strErrorMessage = MESSAGE_NO_SUBSCRIBER;
            }
            else
                if ( StringUtils.isEmpty( strObject ) )
                {
                    strErrorMessage = MESSAGE_OBJECT_NOT_SPECIFIED;
                }

        if ( strErrorMessage != null )
        {
            return redirectMessage( request, strErrorMessage );
        }

        SendingNewsLetter lastSending = SendingNewsLetterHome.findLastSendingForNewsletterId( nNewsletterId, getPlugin( ) );

        String strMessage = MESSAGE_CONFIRM_SEND_NEWSLETTER;
        // the sending stores the whole e-mail: compare it with the e-mail the newsletter would give now
        if ( ( lastSending != null ) && ( lastSending.getHtml( ) != null )
                && lastSending.getHtml( ).equals( setHtmlTemplateEmail( newsletter, AppPathService.getProdUrl( request ), newsletter.getUnsubscribe( ) ).getHtml( ) ) )
        {
            strMessage = MESSAGE_FRAGMENT_NO_CHANGE;
        }

        Map<String, Object> message = new HashMap<>( );
        message.put( MARK_MESSAGE_TYPE, AdminMessage.TYPE_CONFIRMATION );

        model.put( MARK_MESSAGE, message );
        model.put( MARK_TEXT, I18nService.getLocalizedString( strMessage, getLocale( ) ) );
        model.put( MARK_NEWSLETTER, newsletter );
        model.put( MARK_NEWSLETTER_OBJECT, strObject );
        model.put( MARK_NEWSLETTER_COUNT_ACTIVE_SUBSCRIBERS, nNbrSubscribers );

        return getPage( PROPERTY_PAGE_TITLE_CONFIRM_SEND, TEMPLATE_CONFIRM_SEND_NEWSLETTER );
    }

    /**
     * Asks the confirmation of the sending of the newsletter to its test recipients
     *
     * @param request
     *            the http request
     * @return the url of the confirmation message
     */
    @View( value = VIEW_CONFIRM_TEST_NEWSLETTER, securityTokenAction = ACTION_TEST_NEWSLETTER )
    public String getConfirmTestNewsLetter( HttpServletRequest request )
    {
        NewsLetter newsletter = findNewsletter( request );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MODIFY ) )
        {
            return redirectAccessDenied( request );
        }

        if ( StringUtils.isEmpty( newsletter.getHtml( ) ) )
        {
            return redirectMessage( request, MESSAGE_SENDING_EMPTY_NOT_ALLOWED );
        }

        UrlItem urlItem = new UrlItem( getActionUrl( ACTION_TEST_NEWSLETTER ) );
        urlItem.addParameter( PARAMETER_NEWSLETTER_ID, newsletter.getId( ) );

        return redirect( request,
                AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_TEST_NEWSLETTER, urlItem.getUrl( ), AdminMessage.TYPE_CONFIRMATION ) );
    }

    /**
     * Processes the testing of a newsletter: it is sent to its test recipients
     *
     * @param request
     *            the http request
     * @return the redirection to the composition of the newsletter, or the url of the error message
     */
    @Action( ACTION_TEST_NEWSLETTER )
    public String doTestNewsLetter( HttpServletRequest request )
    {
        NewsLetter newsletter = findNewsletter( request );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MODIFY ) )
        {
            return redirectAccessDenied( request );
        }

        String strWrongEmail = isWrongEmail( newsletter.getTestRecipients( ) );
        if ( StringUtils.isNotEmpty( strWrongEmail ) )
        {
            Object [ ] messageArgs = {
                    strWrongEmail
            };

            return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_WRONG_EMAIL, messageArgs, AdminMessage.TYPE_STOP ) );
        }

        String strObject = I18nService.getLocalizedString( PROPERTY_TEST_SUBJECT, getLocale( ) ) + newsletter.getName( ) + "]" + newsletter.getTestSubject( );
        String strBaseUrl = AppPathService.getProdUrl( request );
        HtmlTemplate templateNewsLetter = setHtmlTemplateEmail( newsletter, strBaseUrl, newsletter.getUnsubscribe( ) );

        _newsletterService.sendMail( newsletter, strObject, strBaseUrl, templateNewsLetter, convertToList( newsletter.getTestRecipients( ) ) );

        return redirect( request, VIEW_COMPOSE_NEWSLETTER, PARAMETER_NEWSLETTER_ID, newsletter.getId( ) );
    }

    /**
     * Processes the sending of a newsletter. The security token of the confirmation page is used once: a second submission of the same page is
     * refused, so a newsletter is not sent twice. The token is checked here rather than by the security token filter, which would answer a bare
     * 403: the user is told that the newsletter is already being sent or has already been sent.
     *
     * @param request
     *            the http request
     * @return the redirection to the newsletters list, or the url of the error message
     */
    @Action( value = ACTION_SEND_NEWSLETTER, securityTokenDisabled = true )
    public String doSendNewsLetter( HttpServletRequest request )
    {
        if ( !_securityTokenHandler.validate( request, ACTION_SEND_NEWSLETTER ) )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_SENDING_ALREADY_DONE, getViewFullUrl( VIEW_MANAGE_NEWSLETTERS ),
                    AdminMessage.TYPE_STOP ) );
        }

        NewsLetter newsletter = findNewsletter( request );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_SEND ) )
        {
            return redirectAccessDenied( request );
        }

        int nNewsletterId = newsletter.getId( );
        int nNbrSubscribers = NewsLetterHome.findNbrActiveSubscribers( nNewsletterId, getPlugin( ) );

        if ( nNbrSubscribers == 0 )
        {
            return redirectMessage( request, MESSAGE_NO_SUBSCRIBER );
        }

        String strObject = StringUtils.defaultIfEmpty( request.getParameter( PARAMETER_NEWSLETTER_OBJECT ), newsletter.getName( ) );
        String strBaseUrl = AppPathService.getProdUrl( request );

        Collection<Subscriber> list = SubscriberHome.findSubscribersForSending( nNewsletterId,
                _nLimitMinSubscriber, _nLimitMaxSubscriber, getPlugin( ) );

        HtmlTemplate templateNewsLetter = setHtmlTemplateEmail( newsletter, strBaseUrl, newsletter.getUnsubscribe( ) );

        _newsletterService.sendMail( newsletter, strObject, strBaseUrl, templateNewsLetter, list );

        SendingNewsLetter sending = new SendingNewsLetter( );
        sending.setNewsLetterId( nNewsletterId );
        sending.setDate( new Timestamp( System.currentTimeMillis( ) ) );
        sending.setCountSubscribers( nNbrSubscribers );
        sending.setHtml( templateNewsLetter.getHtml( ) );
        sending.setEmailSubject( strObject );
        SendingNewsLetterHome.create( sending, getPlugin( ) );

        newsletter.setDateLastSending( sending.getDate( ) );
        NewsLetterHome.update( newsletter, getPlugin( ) );

        return redirectView( request, VIEW_MANAGE_NEWSLETTERS );
    }

    /**
     * Processes the composition form: the content is saved, then the button pushed opens the composition again, the preparation of the sending
     * or the confirmation of the test sending. The cancel button asks the confirmation of leaving the composition, without saving.
     *
     * @param request
     *            The Http request
     * @return The redirection to the next page
     */
    @Action( ACTION_REGISTER_NEWSLETTER )
    public String doRegisterNewsLetter( HttpServletRequest request )
    {
        NewsLetter newsletter = findNewsletter( request );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MODIFY ) )
        {
            return redirectAccessDenied( request );
        }

        String strComposeAction = request.getParameter( PARAMETER_COMPOSE_ACTION );

        if ( CONSTANT_ACTION_CANCEL.equals( strComposeAction ) )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_CANCEL_COMPOSE, getViewFullUrl( VIEW_MANAGE_NEWSLETTERS ),
                    AdminMessage.TYPE_CONFIRMATION ) );
        }

        newsletter.setHtml( doClean( request.getParameter( PARAMETER_HTML_CONTENT ), AppPathService.getBaseUrl( request ) ) );
        NewsLetterHome.update( newsletter, getPlugin( ) );

        if ( CONSTANT_ACTION_PREPARE_SENDING.equals( strComposeAction ) )
        {
            return redirect( request, VIEW_PREPARE_NEWSLETTER, PARAMETER_NEWSLETTER_ID, newsletter.getId( ) );
        }

        if ( CONSTANT_ACTION_TEST_SENDING.equals( strComposeAction ) )
        {
            return redirect( request, VIEW_CONFIRM_TEST_NEWSLETTER, PARAMETER_NEWSLETTER_ID, newsletter.getId( ) );
        }

        return redirect( request, VIEW_COMPOSE_NEWSLETTER, PARAMETER_NEWSLETTER_ID, newsletter.getId( ) );
    }

    /**
     * Builds the page of the import of subscribers from a csv file
     *
     * @param request
     *            The HTTP request
     * @param model
     *            the model of the page
     * @return the html code for subscribers import page
     */
    @View( VIEW_IMPORT_SUBSCRIBERS )
    public String getImportSubscribers( HttpServletRequest request, Models model )
    {
        return getImportPage( request, model, false );
    }

    /**
     * Builds the page of the removal of subscribers listed in a csv file
     *
     * @param request
     *            The HTTP request
     * @param model
     *            the model of the page
     * @return the html code for subscribers import page
     */
    @View( value = VIEW_IMPORT_DELETE_SUBSCRIBERS, securityTokenAction = ACTION_IMPORT_SUBSCRIBERS )
    public String getImportDeleteSubscribers( HttpServletRequest request, Models model )
    {
        return getImportPage( request, model, true );
    }

    /**
     * Processes the import of subscribers due to a csv file, or their removal when the import deletes, and loads the subscribers management page
     *
     * @param request
     *            The Http request
     * @return The redirection to the subscribers of the newsletter, or the url of the error message
     */
    @Action( ACTION_IMPORT_SUBSCRIBERS )
    public String doImportSubscribers( HttpServletRequest request )
    {
        NewsLetter newsletter = findNewsletter( request );
        boolean bImportDelete = Boolean.parseBoolean( request.getParameter( MARK_IMPORT_DELETE ) );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_IMPORT_SUBSCRIBERS ) )
        {
            return redirectAccessDenied( request );
        }

        int nNewsletterId = newsletter.getId( );

        if ( request instanceof MultipartHttpServletRequest )
        {
            MultipartHttpServletRequest multi = (MultipartHttpServletRequest) request;
            MultipartItem csvItem = multi.getFile( PARAMETER_SUBSCRIBERS_FILE );
            String strMultiFileName = csvItem == null ? StringUtils.EMPTY : UploadUtil.cleanFileName( csvItem.getName( ) );

            if ( csvItem == null || StringUtils.isEmpty( strMultiFileName ) )
            {
                return redirectMessage( request, Messages.MANDATORY_FIELDS );
            }

            if ( !strMultiFileName.endsWith( CONSTANT_CSV_FILE_EXTENSION ) )
            {
                return redirectMessage( request, MESSAGE_CSV_FILE_EXTENSION );
            }

            List<String [ ]> tabUsers;
            char cDelimiter = _strImportDelimiter.charAt( 0 );

            try ( Reader fileReader = new InputStreamReader( csvItem.getInputStream( ) );
                    CSVReader csvReader = new CSVReaderBuilder( fileReader ).withCSVParser( new CSVParserBuilder( ).withSeparator( cDelimiter ).build( ) )
                            .build( ) )
            {
                tabUsers = csvReader.readAll( );
            }
            catch( IOException | CsvException e )
            {
                AppLogService.error( "Unable to read the csv file of the subscribers: {}", e.getMessage( ), e );

                return redirectMessage( request, MESSAGE_CSV_FILE_EMPTY_OR_NOT_VALID_EMAILS );
            }

            if ( tabUsers == null || tabUsers.isEmpty( ) )
            {
                return redirectMessage( request, MESSAGE_CSV_FILE_EMPTY_OR_NOT_VALID_EMAILS );
            }

            int nColumnIndex = _nImportEmailColumnIndex;
            Timestamp tToday = new Timestamp( System.currentTimeMillis( ) );

            for ( String [ ] strEmailTemp : tabUsers )
            {
                if ( strEmailTemp.length <= nColumnIndex )
                {
                    return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_COLUMN_INDEX_NOT_EXIST, AdminMessage.TYPE_ERROR ) );
                }

                String strEmail = strEmailTemp [nColumnIndex];

                if ( strEmail != null && StringUtil.checkEmail( strEmail.trim( ) ) )
                {
                    importSubscriber( nNewsletterId, strEmail, bImportDelete, tToday );
                }
            }
        }

        return redirect( request, VIEW_MANAGE_SUBSCRIBERS, PARAMETER_NEWSLETTER_ID, nNewsletterId );
    }

    /**
     * Get the manage topics page
     *
     * @param request
     *            The request
     * @param model
     *            the model of the page
     * @return The HTML to display
     */
    @View( value = VIEW_MANAGE_TOPICS, securityTokenAction = ACTION_MODIFY_TOPICS )
    public String getManageNewsletterTopics( HttpServletRequest request, Models model )
    {
        NewsLetter newsletter = findNewsletter( request );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MODIFY ) )
        {
            return redirectView( request, VIEW_MANAGE_NEWSLETTERS );
        }

        int nNewsletterId = newsletter.getId( );
        List<NewsletterTopic> listTopics = NewsletterTopicHome.findAllByIdNewsletter( nNewsletterId, getPlugin( ) );

        Integer [ ] tblSectionSize = new Integer [ newsletter.getNbSections( )];
        for ( int i = 0; i < newsletter.getNbSections( ); i++ )
        {
            tblSectionSize [i] = NewsletterTopicHome.getLastOrder( nNewsletterId, i + 1, getPlugin( ) );
        }

        ReferenceList refListTopicType = _newsletterTopicService.getNewsletterTopicTypeRefList( getLocale( ) );
        listTopics.sort( Comparator.comparingInt( NewsletterTopic::getSection ).thenComparingInt( NewsletterTopic::getOrder ) );

        AdminUser user = getUser( );
        String strBaseUrl = AppPathService.getBaseUrl( request );

        Collection<NewsLetterTemplate> newsletterTemplatesList = NewsLetterTemplateHome.getTemplatesCollectionByType( NewsLetterTemplate.RESOURCE_TYPE,
                getPlugin( ) );
        newsletterTemplatesList = AdminWorkgroupService.getAuthorizedCollection( newsletterTemplatesList, (User) user );
        List<Integer> listMissingTemplateIds = new ArrayList<>( );
        for ( NewsLetterTemplate newsletterTemplate : newsletterTemplatesList )
        {
            if ( !_newsletterService.isTemplateAvailable( newsletterTemplate ) )
            {
                listMissingTemplateIds.add( newsletterTemplate.getId( ) );
            }
        }

        model.put( MARK_NEWSLETTER, newsletter );
        model.put( MARK_TOPICS_LIST, listTopics );
        model.put( MARK_LIST_TOPIC_TYPES, refListTopicType );
        model.put( MARK_SECTION_SIZES, tblSectionSize );
        // Each topic is generated once, for its status and for the preview
        Map<Integer, String> mapTopicContents = new HashMap<>( );
        Map<String, String> mapTopicStatus = new HashMap<>( );
        Map<String, String> mapTopicSummaries = new HashMap<>( );
        for ( NewsletterTopic topic : listTopics )
        {
            String strContent = _newsletterTopicService.getTopicContent( topic, user, getLocale( ) );
            mapTopicContents.put( topic.getId( ), strContent );
            mapTopicStatus.put( Integer.toString( topic.getId( ) ), getTopicStatus( strContent ) );
            String strSummary = _newsletterTopicService.getConfigurationSummary( topic, getLocale( ) );
            if ( StringUtils.isNotBlank( strSummary ) )
            {
                mapTopicSummaries.put( Integer.toString( topic.getId( ) ), strSummary );
            }
        }

        model.put( MARK_HTML_CONTENT, toMarkup( _newsletterService.generateNewsletterPreviewHtmlCode( newsletter, mapTopicContents, strBaseUrl, getLocale( ) ) ) );
        model.put( MARK_TOPIC_STATUS, mapTopicStatus );
        model.put( MARK_TOPIC_SUMMARIES, mapTopicSummaries );
        model.put( MARK_DATE_LAST_SENDING, DateUtil.getDateString( newsletter.getDateLastSending( ), getLocale( ) ) );
        model.put( MARK_WEBAPP_URL, strBaseUrl );
        model.put( MARK_LOCALE, getLocale( ) );
        model.put( MARK_LIST_NEWSLETTER_TEMPLATES, newsletterTemplatesList );
        model.put( MARK_MISSING_TEMPLATE_IDS, listMissingTemplateIds );
        model.put( MARK_AVAILABLE_PICTURES, new ArrayList<>( _newsletterService.getTemplatePictureList( ).toMap( ).keySet( ) ) );
        model.put( MARK_NEWSLETTER_TEMPLATE_ID, newsletter.getNewsLetterTemplateId( ) );
        model.put( MARK_IMG_PATH, _newsletterService.getImageFolderPath( strBaseUrl ) );
        fillNewsletterTabsModel( model, newsletter );

        return getPage( MESSAGE_PAGE_TITLE_MANAGE_TOPICS, TEMPLATE_MANAGE_NEWSLETTER_TOPICS );
    }

    /**
     * Get the status of a topic from its generated content
     *
     * @param strContent
     *            The html content of the topic
     * @return {@code unconfigured} when the topic gives no content at all (its configuration is incomplete), {@code empty} when it has nothing
     *         to show for the period, {@code ok} otherwise
     */
    private static String getTopicStatus( String strContent )
    {
        if ( strContent == null )
        {
            return TOPIC_STATUS_UNCONFIGURED;
        }
        return StringUtils.isBlank( strContent ) ? TOPIC_STATUS_EMPTY : TOPIC_STATUS_OK;
    }

    /**
     * Processes an operation of the content page of a newsletter: the choice of its template, the addition of a topic, the move of a topic up or
     * down, or the change of its section
     *
     * @param request
     *            The request
     * @return The redirection to the content page, or to the configuration of the topic added
     */
    @Action( ACTION_MODIFY_TOPICS )
    public String doModifyNewsletterTopics( HttpServletRequest request )
    {
        NewsLetter newsletter = findNewsletter( request );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MODIFY ) )
        {
            return redirectAccessDenied( request );
        }

        String strOperation = StringUtils.defaultString( request.getParameter( PARAMETER_OPERATION ) );

        switch( strOperation )
        {
            case OPERATION_UPDATE_TEMPLATE:
                updateTemplate( request, newsletter );
                break;
            case OPERATION_ADD_TOPIC:
                NewsletterTopic newsletterTopic = addTopic( request, newsletter );
                return redirect( request, VIEW_MODIFY_TOPIC_CONFIG, PARAMETER_NEWSLETTER_ID, newsletter.getId( ), PARAMETER_TOPIC_ID, newsletterTopic.getId( ) );
            case OPERATION_MOVE_TOPIC:
                NewsletterTopic topicToMove = findTopic( request, newsletter );
                if ( topicToMove != null )
                {
                    _newsletterTopicService.modifyNewsletterTopicOrder( topicToMove, Boolean.parseBoolean( request.getParameter( PARAMETER_MOVE_UP ) ) );
                }
                break;
            case OPERATION_CHANGE_SECTION:
                NewsletterTopic topicToChange = findTopic( request, newsletter );
                String strSection = request.getParameter( PARAMETER_TOPIC_SECTION_NUMBER );
                if ( topicToChange != null && StringUtils.isNumeric( strSection ) )
                {
                    _newsletterTopicService.modifyNewsletterTopicSection( topicToChange, Integer.parseInt( strSection ) );
                }
                break;
            default:
                return redirectAccessDenied( request );
        }

        return redirect( request, VIEW_MANAGE_TOPICS, PARAMETER_NEWSLETTER_ID, newsletter.getId( ) );
    }

    /**
     * Asks the confirmation of the removal of a newsletter topic
     *
     * @param request
     *            The request
     * @return the url of the confirmation message
     */
    @View( value = VIEW_CONFIRM_REMOVE_TOPIC, securityTokenAction = ACTION_REMOVE_TOPIC )
    public String getConfirmRemoveNewsletterTopic( HttpServletRequest request )
    {
        NewsLetter newsletter = findNewsletter( request );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MODIFY ) || findTopic( request, newsletter ) == null )
        {
            return redirectAccessDenied( request );
        }

        UrlItem urlItem = new UrlItem( getActionUrl( ACTION_REMOVE_TOPIC ) );
        urlItem.addParameter( PARAMETER_NEWSLETTER_ID, newsletter.getId( ) );
        urlItem.addParameter( PARAMETER_TOPIC_ID, request.getParameter( PARAMETER_TOPIC_ID ) );

        return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_TOPIC, urlItem.getUrl( ), AdminMessage.TYPE_CONFIRMATION ) );
    }

    /**
     * Remove a newsletter topic
     *
     * @param request
     *            The request
     * @return The redirection to the content page
     */
    @Action( ACTION_REMOVE_TOPIC )
    public String doRemoveNewsletterTopic( HttpServletRequest request )
    {
        NewsLetter newsletter = findNewsletter( request );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MODIFY ) )
        {
            return redirectAccessDenied( request );
        }

        NewsletterTopic newsletterTopic = findTopic( request, newsletter );

        if ( newsletterTopic != null )
        {
            _newsletterTopicService.removeNewsletterTopic( newsletterTopic, getUser( ) );
        }

        return redirect( request, VIEW_MANAGE_TOPICS, PARAMETER_NEWSLETTER_ID, newsletter.getId( ) );
    }

    /**
     * Get the modification page of a newsletter topic configuration
     *
     * @param request
     *            The request
     * @param model
     *            the model of the page
     * @return The html content to display
     */
    @View( VIEW_MODIFY_TOPIC_CONFIG )
    public String getModifyTopicConfig( HttpServletRequest request, Models model )
    {
        NewsLetter newsletter = findNewsletter( request );
        NewsletterTopic newsletterTopic = isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MODIFY ) ? findTopic( request, newsletter ) : null;

        if ( newsletterTopic == null )
        {
            return redirectView( request, VIEW_MANAGE_NEWSLETTERS );
        }

        model.put( MARK_CONTENT,
                toMarkup( _newsletterTopicService.getConfigurationPage( newsletterTopic, AppPathService.getBaseUrl( request ), getUser( ), getLocale( ) ) ) );
        model.put( MARK_TOPIC, newsletterTopic );

        return getPage( PROPERTY_PAGE_TITLE_MODIFY_TOPIC_CONFIGURATION, TEMPLATE_MODIFY_TOPIC_CONFIG );
    }

    /**
     * Save the configuration of a topic, unless the cancel button was pushed
     *
     * @param request
     *            The request
     * @return The redirection to the content page
     */
    @Action( ACTION_MODIFY_TOPIC_CONFIG )
    public String doModifyTopicConfig( HttpServletRequest request )
    {
        NewsLetter newsletter = findNewsletter( request );
        NewsletterTopic newsletterTopic = isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MODIFY ) ? findTopic( request, newsletter ) : null;

        if ( newsletterTopic == null )
        {
            return redirectAccessDenied( request );
        }

        if ( request.getParameter( PARAMETER_CANCEL ) == null )
        {
            String strTitle = request.getParameter( PARAMETER_TITLE );
            if ( StringUtils.isNotEmpty( strTitle ) )
            {
                newsletterTopic.setTitle( strTitle );
                NewsletterTopicHome.updateNewsletterTopic( newsletterTopic, getPlugin( ) );
            }

            _newsletterTopicService.saveConfiguration( request.getParameterMap( ), newsletterTopic, getUser( ), getLocale( ) );
        }

        return redirect( request, VIEW_MANAGE_TOPICS, PARAMETER_NEWSLETTER_ID, newsletter.getId( ) );
    }

    /**
     * Processes the copy of a newsletter
     *
     * @param request
     *            The Http request
     * @return the redirection to the newsletters list
     */
    @Action( ACTION_COPY_NEWSLETTER )
    public String doCopyNewsLetter( HttpServletRequest request )
    {
        NewsLetter newsletter = findNewsletter( request );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_CREATE ) )
        {
            return redirectAccessDenied( request );
        }

        Object [ ] tabNewsLetterTileCopy = {
                newsletter.getName( )
        };
        String strTitleCopySuggest = I18nService.getLocalizedString( LABLE_COPY_NEWSLETTER_TITLE, tabNewsLetterTileCopy, getLocale( ) );

        if ( strTitleCopySuggest != null )
        {
            // The name of the copy must fit in the column of the name
            newsletter.setName( StringUtils.left( strTitleCopySuggest, NEWSLETTER_NAME_MAX_LENGTH ) );
        }

        _newsletterService.copyExistingNewsletter( newsletter, getUser( ), getLocale( ) );

        return redirectView( request, VIEW_MANAGE_NEWSLETTERS );
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
     * Builds the page of the import of subscribers, adding or removing them
     *
     * @param request
     *            The HTTP request
     * @param model
     *            the model of the page
     * @param bImportDelete
     *            true if the file lists subscribers to remove
     * @return the html code for subscribers import page
     */
    private String getImportPage( HttpServletRequest request, Models model, boolean bImportDelete )
    {
        NewsLetter newsletter = findNewsletter( request );

        if ( !isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MANAGE_SUBSCRIBERS ) )
        {
            return redirectView( request, VIEW_MANAGE_NEWSLETTERS );
        }

        model.put( MARK_IMPORT_DELETE, bImportDelete );
        model.put( MARK_NEWSLETTER, newsletter );
        fillNewsletterTabsModel( model, newsletter );

        return getPage( bImportDelete ? PROPERTY_PAGE_TITLE_IMPORT_DELETE : PROPERTY_PAGE_TITLE_IMPORT, TEMPLATE_IMPORT_SUBSCRIBERS );
    }

    /**
     * Adds a subscriber read in an import file to a newsletter, or removes him from it
     *
     * @param nNewsletterId
     *            the identifier of the newsletter
     * @param strEmail
     *            the email of the subscriber
     * @param bImportDelete
     *            true to remove the subscriber, false to add him
     * @param tToday
     *            the date of the subscription
     */
    private void importSubscriber( int nNewsletterId, String strEmail, boolean bImportDelete, Timestamp tToday )
    {
        Subscriber subscriber = SubscriberHome.findByEmail( strEmail, getPlugin( ) );

        if ( bImportDelete )
        {
            if ( subscriber != null )
            {
                NewsLetterHome.removeSubscriber( nNewsletterId, subscriber.getId( ), getPlugin( ) );
            }

            return;
        }

        if ( subscriber == null )
        {
            subscriber = new Subscriber( );
            subscriber.setEmail( strEmail );
            SubscriberHome.create( subscriber, getPlugin( ) );
        }

        NewsLetterHome.addSubscriber( nNewsletterId, subscriber.getId( ), tToday, getPlugin( ) );
    }

    /**
     * Gives a newsletter the template chosen in the content page: its number of sections becomes the one of the template, and the topics of a
     * section the template does not have move to its last section
     *
     * @param request
     *            The request
     * @param newsletter
     *            the newsletter
     */
    private void updateTemplate( HttpServletRequest request, NewsLetter newsletter )
    {
        String strTemplateId = request.getParameter( MARK_NEWSLETTER_TEMPLATE_ID );

        if ( !StringUtils.isNumeric( strTemplateId ) )
        {
            return;
        }

        NewsLetterTemplate newsletterTemplate = NewsLetterTemplateHome.findByPrimaryKey( Integer.parseInt( strTemplateId ), getPlugin( ) );

        if ( newsletterTemplate == null || newsletterTemplate.getId( ) == 0 || !AdminWorkgroupService.isAuthorized( newsletterTemplate, (User) getUser( ) ) )
        {
            return;
        }

        newsletter.setNbSections( newsletterTemplate.getSectionNumber( ) );
        newsletter.setNewsLetterTemplateId( newsletterTemplate.getId( ) );
        NewsLetterHome.update( newsletter, getPlugin( ) );

        int nNewOrder = NewsletterTopicHome.getNewOrder( newsletter.getId( ), newsletterTemplate.getSectionNumber( ), getPlugin( ) );
        for ( NewsletterTopic topic : NewsletterTopicHome.findAllByIdNewsletter( newsletter.getId( ), getPlugin( ) ) )
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

    /**
     * Creates a topic in a newsletter, of the type and in the section chosen
     *
     * @param request
     *            The request
     * @param newsletter
     *            the newsletter
     * @return the topic created
     */
    private NewsletterTopic addTopic( HttpServletRequest request, NewsLetter newsletter )
    {
        NewsletterTopic newsletterTopic = new NewsletterTopic( );
        newsletterTopic.setIdNewsletter( newsletter.getId( ) );
        newsletterTopic.setTopicTypeCode( request.getParameter( PARAMETER_TOPIC_TYPE ) );
        String strSectionNumber = request.getParameter( PARAMETER_TOPIC_SECTION_NUMBER );
        newsletterTopic.setSection( StringUtils.isNumeric( strSectionNumber ) ? Integer.parseInt( strSectionNumber ) : 1 );
        _newsletterTopicService.createNewsletterTopic( newsletterTopic, getUser( ), getLocale( ) );

        return newsletterTopic;
    }

    /**
     * Removes an archive of a newsletter
     *
     * @param newsletter
     *            the newsletter
     * @param nSendingNewsletterId
     *            the identifier of the archive, removed only when it belongs to the newsletter
     */
    private void removeSending( NewsLetter newsletter, int nSendingNewsletterId )
    {
        SendingNewsLetter sending = SendingNewsLetterHome.findByPrimaryKey( nSendingNewsletterId, getPlugin( ) );

        if ( sending != null && sending.getNewsLetterId( ) == newsletter.getId( ) )
        {
            SendingNewsLetterHome.remove( nSendingNewsletterId, getPlugin( ) );
        }
    }

    /**
     * Returns the newsletter whose identifier is in the request
     *
     * @param request
     *            the HTTP request
     * @return the newsletter, null when the identifier is missing, matches no newsletter or a newsletter outside the workgroups of the user
     */
    private NewsLetter findNewsletter( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        NewsLetter newsletter = StringUtils.isNumeric( strNewsletterId ) ? NewsLetterHome.findByPrimaryKey( Integer.parseInt( strNewsletterId ), getPlugin( ) )
                : null;

        return ( newsletter != null && AdminWorkgroupService.isAuthorized( newsletter, (User) getUser( ) ) ) ? newsletter : null;
    }

    /**
     * Returns the topic whose identifier is in the request, when it belongs to a newsletter
     *
     * @param request
     *            the HTTP request
     * @param newsletter
     *            the newsletter
     * @return the topic, null when the identifier is missing or matches no topic of the newsletter
     */
    private NewsletterTopic findTopic( HttpServletRequest request, NewsLetter newsletter )
    {
        String strTopicId = request.getParameter( PARAMETER_TOPIC_ID );

        if ( !StringUtils.isNumeric( strTopicId ) )
        {
            return null;
        }

        NewsletterTopic newsletterTopic = NewsletterTopicHome.findByPrimaryKey( Integer.parseInt( strTopicId ), getPlugin( ) );

        return ( newsletterTopic != null && newsletterTopic.getIdNewsletter( ) == newsletter.getId( ) ) ? newsletterTopic : null;
    }

    /**
     * Checks that a newsletter exists and that the user holds its workgroup and a permission on it
     *
     * @param newsletter
     *            the newsletter, may be null
     * @param strPermission
     *            the RBAC permission
     * @return true if the user may act on the newsletter
     */
    private boolean isAuthorized( NewsLetter newsletter, String strPermission )
    {
        return newsletter != null && AdminWorkgroupService.isAuthorized( newsletter, (User) getUser( ) )
                && RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, Integer.toString( newsletter.getId( ) ), strPermission, (User) getUser( ) );
    }

    /**
     * Checks that the user may manage the advanced settings of the newsletters
     *
     * @param request
     *            the HTTP request
     * @throws AccessDeniedException
     *             if the user is not allowed
     */
    private void checkAdvancedSettingsPermission( HttpServletRequest request ) throws AccessDeniedException
    {
        if ( !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID,
                NewsletterResourceIdService.PERMISSION_NEWSLETTER_ADVANCED_SETTINGS, (User) getUser( ) ) )
        {
            throw new AccessDeniedException( MESSAGE_USER_NOT_ALLOWED_NEWSLETTER_PROPERTIES + AdminUserService.getAdminUser( request ).getAccessCode( ) );
        }
    }

    /**
     * Builds the list of the editors of the composition of the newsletters: GrapesJS, the rich text editor of the back office of the site, then
     * each rich text editor the back office offers
     *
     * @return the editors, by value of the property
     */
    private ReferenceList buildEditorList( )
    {
        ReferenceList listEditorsBackOffice = RichTextEditorService.getListEditorsForBackOffice( getLocale( ) );
        String strSiteEditor = listEditorsBackOffice.toMap( ).getOrDefault( RichTextEditorService.getBackOfficeDefaultEditor( ), StringUtils.EMPTY );
        ReferenceList listEditors = new ReferenceList( );
        listEditors.addItem( NewsLetterProperties.EDITOR_GRAPESJS, I18nService.getLocalizedString( PROPERTY_EDITOR_GRAPESJS, getLocale( ) ) );
        listEditors.addItem( NewsLetterProperties.EDITOR_LUTECE, I18nService.getLocalizedString( PROPERTY_EDITOR_SITE, new Object [ ] {
                strSiteEditor
        }, getLocale( ) ) );
        listEditors.addAll( listEditorsBackOffice );

        return listEditors;
    }

    /**
     * Returns the rich text editor chosen for the composition of the newsletters when it is not the one of the back office of the site
     *
     * @param properties
     *            the properties of the newsletters
     * @return the name of the editor, empty for GrapesJS, for the editor of the back office of the site, or for an editor the back office no longer
     *         offers
     */
    private String getChosenEditor( NewsLetterProperties properties )
    {
        String strEditor = properties.getEditor( );

        if ( !properties.isLuteceEditor( ) || NewsLetterProperties.EDITOR_LUTECE.equals( strEditor )
                || strEditor.equals( RichTextEditorService.getBackOfficeDefaultEditor( ) )
                || !RichTextEditorService.getListEditorsForBackOffice( getLocale( ) ).toMap( ).containsKey( strEditor ) )
        {
            return StringUtils.EMPTY;
        }

        return strEditor;
    }

    /**
     * Builds the profile of an editor for the newsletters and the connected user, in the form the editor templates of the core read it
     *
     * @param strEditor
     *            the name of the editor
     * @return the plugin, the name of the profile, the editor and the config of the profile
     */
    private Map<String, String> buildEditorProfile( String strEditor )
    {
        EditorProfile profile = _editorProfileService.getProfile( strEditor, NewsletterPlugin.PLUGIN_NAME, getUser( ) );
        Map<String, String> mapProfile = new HashMap<>( );
        mapProfile.put( KEY_PROFILE_PLUGIN, NewsletterPlugin.PLUGIN_NAME );
        mapProfile.put( KEY_PROFILE_NAME, ( profile != null ) ? profile.getName( ) : StringUtils.EMPTY );
        mapProfile.put( KEY_PROFILE_EDITOR, strEditor );
        mapProfile.put( KEY_PROFILE_CONFIG,
                ( profile != null && StringUtils.isNotBlank( profile.getConfig( ) ) ) ? profile.getConfig( ) : EditorProfile.EMPTY_CONFIG );

        return mapProfile;
    }

    /**
     * Tells whether the user may clean the subscribers of all the newsletters
     *
     * @return true if the user holds the permission of cleaning the subscribers
     */
    private boolean isCleanSubscribersAllowed( )
    {
        return RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, null, NewsletterResourceIdService.PERMISSION_CLEAN_SUBSCRIBERS, (User) getUser( ) );
    }

    /**
     * Builds the list of the choices of the unsubscription link
     *
     * @param strTrueCode
     *            the code of the choice of the link
     * @param strFalseCode
     *            the code of the choice of no link
     * @return the list
     */
    private ReferenceList buildUnsubscribeList( String strTrueCode, String strFalseCode )
    {
        ReferenceList listUnsubscribe = new ReferenceList( );
        listUnsubscribe.addItem( strTrueCode, I18nService.getLocalizedString( PROPERTY_LABEL_UNSUBSCRIBE_TRUE, getLocale( ) ) );
        listUnsubscribe.addItem( strFalseCode, I18nService.getLocalizedString( PROPERTY_LABEL_UNSUBSCRIBE_FALSE, getLocale( ) ) );

        return listUnsubscribe;
    }

    /**
     * Redirects to the message telling the user he is not allowed
     *
     * @param request
     *            the HTTP request
     * @return the redirection
     */
    private String redirectAccessDenied( HttpServletRequest request )
    {
        return redirect( request, AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR ) );
    }

    /**
     * Redirects to a message that stops the operation
     *
     * @param request
     *            the HTTP request
     * @param strMessageKey
     *            the key of the message
     * @return the redirection
     */
    private String redirectMessage( HttpServletRequest request, String strMessageKey )
    {
        return redirect( request, AdminMessageService.getMessageUrl( request, strMessageKey, AdminMessage.TYPE_STOP ) );
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
        StringBuilder strCleanTestRecipients = new StringBuilder( );
        String strDelimiter = _strImportDelimiter;

        for ( String email : strRecipientLists.split( strDelimiter ) )
        {
            strCleanTestRecipients.append( email.trim( ) );
            strCleanTestRecipients.append( strDelimiter );
        }

        return strCleanTestRecipients.toString( );
    }

    /**
     * Reads the date entered in the newsletter search form; the date picker sends it as yyyy-MM-dd
     *
     * @param strDate
     *            the submitted value
     * @return the date, or null when the value is empty or not a date
     */
    private static LocalDate parseSearchDate( String strDate )
    {
        String strValue = StringUtils.trimToEmpty( strDate );
        if ( strValue.length( ) < 10 )
        {
            return null;
        }
        try
        {
            return LocalDate.parse( strValue.substring( 0, 10 ) );
        }
        catch( DateTimeParseException e )
        {
            return null;
        }
    }

    /**
     * Tells whether a newsletter matches the search criteria of the newsletter list: its name contains the searched text, ignoring case, and
     * it has been sent, the last time on or after the searched date
     *
     * @param newsletter
     *            the newsletter
     * @param dateLastSending
     *            the date of the last sending found in the sending archive, null when the newsletter has never been sent
     * @param strSearchName
     *            the searched text, empty for no criterion
     * @param dateSearch
     *            the searched date, null for no criterion
     * @return true if the newsletter matches every criterion given
     */
    static boolean matchesSearch( NewsLetter newsletter, Timestamp dateLastSending, String strSearchName, LocalDate dateSearch )
    {
        if ( StringUtils.isNotEmpty( strSearchName ) && !Strings.CI.contains( newsletter.getName( ), strSearchName ) )
        {
            return false;
        }
        if ( dateSearch != null )
        {
            return dateLastSending != null && !dateLastSending.toLocalDateTime( ).toLocalDate( ).isBefore( dateSearch );
        }
        return true;
    }

    /**
     * Puts in the model what the tabs shared by the newsletter screens need: the permissions that show or hide the settings, content and
     * compose entries, the subscribers, archive and sending entries, the number of subscribers, all and confirmed, and the name of the template of
     * the newsletter when its file is missing from the folder of the newsletter templates.
     *
     * @param model
     *            the model of the page
     * @param newsletter
     *            the newsletter displayed
     */
    private void fillNewsletterTabsModel( Models model, NewsLetter newsletter )
    {
        User user = (User) getUser( );
        model.put( MARK_NEWSLETTER_ALLOW_MODIFICATION, RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MODIFY, user ) );
        model.put( MARK_NEWSLETTER_ALLOW_MANAGE_SUBSCRIBERS,
                RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MANAGE_SUBSCRIBERS, user ) );
        model.put( MARK_NEWSLETTER_ALLOW_MANAGE_ARCHIVE, RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_ARCHIVE, user ) );
        model.put( MARK_NEWSLETTER_ALLOW_SENDING, RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_SEND, user ) );
        Pair<Integer, Integer> subscriberCounts = NewsLetterHome.findSubscriberCounts( newsletter.getId( ), getPlugin( ) );
        model.put( MARK_NEWSLETTER_COUNT_SUBSCRIBERS, subscriberCounts.getFirst( ) );
        model.put( MARK_NEWSLETTER_COUNT_ACTIVE_SUBSCRIBERS, subscriberCounts.getSecond( ) );
        if ( newsletter.getNewsLetterTemplateId( ) > 0 )
        {
            NewsLetterTemplate newsletterTemplate = NewsLetterTemplateHome.findByPrimaryKey( newsletter.getNewsLetterTemplateId( ), getPlugin( ) );
            if ( newsletterTemplate != null && newsletterTemplate.getId( ) > 0 && !_newsletterService.isTemplateAvailable( newsletterTemplate ) )
            {
                model.put( MARK_NEWSLETTER_TEMPLATE_MISSING, newsletterTemplate.getTemplateName( ) );
            }
        }
    }

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
        return StringUtil.substitute( strContent, NewsLetterConstants.WEBAPP_PATH_FOR_LINKSERVICE, strBaseUrl );
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
        String strDelimiter = _strImportDelimiter;

        for ( String strEmail : StringUtils.defaultString( strRecipientLists ).split( strDelimiter ) )
        {
            if ( !StringUtil.checkEmail( strEmail ) )
            {
                strWrongEmail = strEmail;
            }
        }

        return strWrongEmail;
    }

    /**
     * Check if user is authozired to create a newsletter
     *
     * @return true if creation is authorized, false otherwise
     */
    private boolean isNewsletterCreationAllowed( )
    {
        User user = (User) getUser( );
        if ( RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID, NewsletterResourceIdService.PERMISSION_CREATE, user ) )
        {
            return true;
        }

        for ( NewsLetter newsletter : AdminWorkgroupService.getAuthorizedCollection( NewsLetterHome.findAll( getPlugin( ) ), user ) )
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
        Collection<Subscriber> listRecipients = new ArrayList<>( );

        if ( StringUtils.isNotEmpty( strRecipients ) )
        {
            String strDelimiter = _strImportDelimiter;

            for ( String strEmail : strRecipients.split( strDelimiter ) )
            {
                if ( StringUtil.checkEmail( strEmail ) )
                {
                    Subscriber subscriber = new Subscriber( );
                    subscriber.setEmail( strEmail );
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
        String strListCssFileName = _newsletterService.getCssFiles( );
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
        StringBuilder sbSource = new StringBuilder( StringUtils.EMPTY );

        try ( BufferedReader fileReader = new BufferedReader( new FileReader( strFileName ) ) )
        {
            String line = fileReader.readLine( );

            while ( line != null )
            {
                sbSource.append( line ).append( SEPARATOR_CSS_FILES_CONTENT );
                line = fileReader.readLine( );
            }
        }
        catch( FileNotFoundException e )
        {
            AppLogService.error( "plugin-newsletter - CSS '{}' not found ! {}", strFileName, e.getMessage( ), e );
        }
        catch( IOException e )
        {
            AppLogService.error( "plugin-newsletter - error when reading CSS '{}' ! {}", strFileName, e.getMessage( ), e );
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
        Map<String, Object> sendingModel = new HashMap<>( );
        sendingModel.put( MARK_CSS, getCssContent( ) );
        sendingModel.put( MARK_UNSUBSCRIBE, Boolean.parseBoolean( strUnsubscribe ) );
        sendingModel.put( MARK_NEWSLETTER_ID, newsletter.getId( ) );
        sendingModel.put( MARK_NEWSLETTER_CONTENT, HtmlMarkup.of( newsletter.getHtml( ) ) );
        sendingModel.put( NewsLetterConstants.MARK_BASE_URL, strBaseUrl );
        sendingModel.put( NewsLetterConstants.MARK_SUBSCRIBER_EMAIL, NewsLetterConstants.MARK_SUBSCRIBER_EMAIL_EACH );
        sendingModel.put( NewsLetterConstants.MARK_UNSUBSCRIBE_KEY, NewsLetterConstants.MARK_UNSUBSCRIBE_KEY_EACH );

        HtmlTemplate templateNewsLetter = AppTemplateService.getTemplate( TEMPLATE_SEND_NEWSLETTER, getLocale( ), sendingModel );
        templateNewsLetter.substitute( NewsLetterConstants.WEBAPP_PATH_FOR_LINKSERVICE, strBaseUrl );

        return templateNewsLetter;
    }

    /**
     * Marks the HTML built by the application as markup for the template (auto-escaping)
     *
     * @param strHtml
     *            The HTML, may be null
     * @return The markup, or null if there is no HTML
     */
    private static Object toMarkup( String strHtml )
    {
        return ( strHtml == null ) ? null : HtmlMarkup.of( strHtml );
    }
}
