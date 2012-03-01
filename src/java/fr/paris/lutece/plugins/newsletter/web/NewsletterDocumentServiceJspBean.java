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
package fr.paris.lutece.plugins.newsletter.web;

import fr.paris.lutece.plugins.document.business.Document;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterHome;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterTemplate;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterTemplateHome;
import fr.paris.lutece.plugins.newsletter.util.NewsLetterConstants;
import fr.paris.lutece.plugins.newsletter.util.NewsletterUtils;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.web.insert.InsertServiceJspBean;
import fr.paris.lutece.portal.web.insert.InsertServiceSelectionBean;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.date.DateUtil;
import fr.paris.lutece.util.html.HtmlTemplate;

import org.apache.commons.lang.StringEscapeUtils;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;


/**
 * This class is responsible for the insertion of document lists in the newsletter.
 * It is a HtmlService.
 */
public class NewsletterDocumentServiceJspBean extends InsertServiceJspBean implements InsertServiceSelectionBean
{
    /**
     * The newsletter right needed to manage
     */
    public static final String RIGHT_NEWSLETTER_MANAGEMENT = "NEWSLETTER_MANAGEMENT";

    // templates
    private static final String TEMPLATE_SELECT_DOCUMENTS = "admin/plugins/newsletter/select_documents.html";
    private static final String TEMPLATE_INSERT_DOCEMENTS = "admin/plugins/newsletter/insert_documents.html";

    // bookmarks
    private static final String BOOKMARK_START_PUBLISHED_DATE = "start_published_date";
    private static final String MARK_DOCUMENT_LIST = "document_list";
    private static final String MARK_COMBO_DOCUMENT_LIST = "documents_lists_list";
    private static final String MARK_INPUT = "input";
    private static final String MARK_TEMPLATES_LIST = "documents_templates_list";

    // parameters
    private static final String PARAMETER_DOCUMENT_LIST_ID = "document_list_id";
    private static final String PARAMETER_TEMPLATE_ID = "template_id";
    private static final String PARAMETER_DOCUMENTS_LIST = "documents_list";
    private static final String PARAMETER_PUBLISHED_DATE = "published_date";
    private static final String PARAMETER_INPUT = "input";

    // property
    private static final String PROPERTY_FRAGMENT_COMBO_ALL_DOCUMENT_LIST_ITEM = ".documents.selection.lists.all";
    private static final String PARAMETER_PLUGIN = "plugin_name";
    private static final String MESSAGE_NO_DOCUMENT_TEMPLATE = "newsletter.message.noDocumentTemplate";
    private static final String MESSAGE_NO_DOCUMENT_CHOSEN = "newsletter.message.noDocumentChosen";

    //  the plugin instance
    private Plugin _plugin;

    /**
     * Inserts Html code by the insert service
     * @see fr.paris.lutece.portal.web.insertservice.HtmlServiceSelectionBean#getInsertServiceSelectorUI(javax.servlet.http.HttpServletRequest)
     * @param request The Http request
     * @return The string representation of the category
     */
    public String getInsertServiceSelectorUI( HttpServletRequest request )
    {
        _plugin = PluginService.getPlugin( request.getParameter( PARAMETER_PLUGIN ) );

        Locale locale = AdminUserService.getLocale( request );

        // get the document list from request
        String strDocumentListId = request.getParameter( PARAMETER_DOCUMENT_LIST_ID );
        strDocumentListId = ( strDocumentListId != null ) ? strDocumentListId : "0";

        int nDocumentListId = Integer.parseInt( strDocumentListId );

        // get template from request
        String strTemplateId = request.getParameter( PARAMETER_TEMPLATE_ID );
        strTemplateId = ( strTemplateId != null ) ? strTemplateId : "0";

        String strPublishedDate = request.getParameter( PARAMETER_PUBLISHED_DATE );
        strPublishedDate = ( strPublishedDate != null ) ? strPublishedDate : "";

        Timestamp publishedDate = DateUtil.getTimestamp( strPublishedDate );
        HashMap model = new HashMap(  );

        // Criteria
        // Combo of availlable document list
        ReferenceList listDocumentlists = NewsLetterHome.getDocumentLists(  );
        listDocumentlists.addItem( 0,
            I18nService.getLocalizedString( _plugin.getName(  ) + PROPERTY_FRAGMENT_COMBO_ALL_DOCUMENT_LIST_ITEM, locale ) );
        model.put( MARK_COMBO_DOCUMENT_LIST, listDocumentlists );

        // re-display the published date field
        model.put( BOOKMARK_START_PUBLISHED_DATE, strPublishedDate );

        // Document list
        Collection list = NewsLetterHome.findDocumentsByDateAndList( nDocumentListId, publishedDate );
        model.put( MARK_DOCUMENT_LIST, list );

        ReferenceList templateList = NewsLetterTemplateHome.getTemplatesListByType( NewsLetterTemplate.CONSTANT_ID_DOCUMENT,
                _plugin );
        model.put( MARK_TEMPLATES_LIST, templateList );

        //Replace portal path for editor and document display
        String strWebappUrl = AppPathService.getBaseUrl( request );
        model.put( NewsLetterConstants.WEBAPP_PATH_FOR_LINKSERVICE, strWebappUrl );
        model.put( PARAMETER_PLUGIN, _plugin.getName(  ) );
        model.put( MARK_INPUT, request.getParameter( PARAMETER_INPUT ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_SELECT_DOCUMENTS, locale, model );

        return template.getHtml(  );
    }

    /**
     * Search for a list of documents
     * Actually does the same as getHtmlSelectorUI
     * @param request the http request
     * @return the found documents
     */
    public String doSearchDocuments( HttpServletRequest request )
    {
        return getInsertServiceSelectorUI( request );
    }

    /**
     * Insert the selected documents as a piece of html code into the html editor
     * @param request the http request
     * @return the html code to display (this code uses a javascript to close the selection window and insert the document list into the editor)
     */
    public String doInsert( HttpServletRequest request )
    {
        _plugin = PluginService.getPlugin( request.getParameter( PARAMETER_PLUGIN ) );

        String strBaseUrl = AppPathService.getBaseUrl( request );
        String strInput = request.getParameter( PARAMETER_INPUT );
        HashMap model = new HashMap(  );
        String strTemplateId = request.getParameter( PARAMETER_TEMPLATE_ID );
        strTemplateId = ( strTemplateId != null ) ? strTemplateId : "0";

        int nTemplateId = Integer.parseInt( strTemplateId );
        String[] strDocumentsIdsList = request.getParameterValues( PARAMETER_DOCUMENTS_LIST );

        if ( ( strTemplateId == null ) || strTemplateId.equals( "0" ) || strTemplateId.equals( "" ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_NO_DOCUMENT_TEMPLATE, AdminMessage.TYPE_STOP );
        }

        if ( ( strDocumentsIdsList == null ) || strDocumentsIdsList.equals( "" ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_NO_DOCUMENT_CHOSEN, AdminMessage.TYPE_STOP );
        }

        Locale locale = AdminUserService.getLocale( request );
        Collection<String> documentsList = new ArrayList<String>(  );

        if ( strDocumentsIdsList != null )
        {
            // retrieves the html template in order to use it to display the list of documents
            String strPathDocumentTemplate = NewsletterUtils.getHtmlTemplatePath( nTemplateId, _plugin );

            for ( int i = 0; i < strDocumentsIdsList.length; i++ )
            {
                int nDocumentId = Integer.parseInt( strDocumentsIdsList[i] );
                Document document = NewsLetterHome.findDocumentById( nDocumentId );
                String strDocumentHtmlCode = NewsletterUtils.fillTemplateWithDocumentInfos( strPathDocumentTemplate,
                        document, 0, _plugin, locale, strBaseUrl );
                documentsList.add( strDocumentHtmlCode );
            }
        }

        model.put( NewsLetterConstants.MARK_DOCUMENTS_LIST, documentsList );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_INSERT_DOCEMENTS, locale, model );
        template.substitute( NewsLetterConstants.WEBAPP_PATH_FOR_LINKSERVICE, strBaseUrl );

        return insertUrl( request, strInput, StringEscapeUtils.escapeJavaScript( template.getHtml(  ) ) );
    }
}
