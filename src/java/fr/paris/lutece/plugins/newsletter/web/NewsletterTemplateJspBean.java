package fr.paris.lutece.plugins.newsletter.web;

import fr.paris.lutece.plugins.newsletter.business.NewsLetterTemplate;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterTemplateHome;
import fr.paris.lutece.plugins.newsletter.service.NewsletterPlugin;
import fr.paris.lutece.plugins.newsletter.service.NewsletterService;
import fr.paris.lutece.plugins.newsletter.service.NewsletterTemplateRemovalService;
import fr.paris.lutece.plugins.newsletter.service.NewsletterTemplateResourceIdService;
import fr.paris.lutece.plugins.newsletter.service.topic.NewsletterTopicService;
import fr.paris.lutece.plugins.newsletter.util.NewsLetterConstants;
import fr.paris.lutece.portal.business.rbac.RBAC;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.business.workgroup.AdminWorkgroupHome;
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
import fr.paris.lutece.portal.web.upload.MultipartHttpServletRequest;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.file.FileUtil;
import fr.paris.lutece.util.filesystem.UploadUtil;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.url.UrlItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;


/**
 * JspBean to manage newsletter templates.
 */
public class NewsletterTemplateJspBean extends PluginAdminPageJspBean
{
    /**
     * The right used for managing newsletter templates
     */
    public static final String RIGHT_NEWSLETTER_TEMPLATE_MANAGEMENT = "NEWSLETTER_TEMPLATE_MANAGEMENT";

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -2513112227429482685L;

    // MARKS
    private static final String MARK_NEWSLETTER_TEMPLATE_ALLOW_CREATION = "newsletter_template_allow_creation";
    private static final String MARK_NEWSLETTER_TEMPLATE_ALLOW_DELETION = "newsletter_template_allow_deletion";
    private static final String MARK_NEWSLETTER_TEMPLATE_ALLOW_MODIFICATION = "newsletter_template_allow_modification";
    private static final String MARK_NEWSLETTER_TEMPLATE_WORKGROUP_DESCRIPTION = "newsletter_template_workgroup_description";
    private static final String MARK_TEMPLATES_LIST = "template_list";
    private static final String MARK_ALLOW_CREATION = "creation_allowed";
    private static final String MARK_WORKGROUP_LIST = "workgroup_list";

    // MESSAGES
    private static final String MESSAGE_PAGE_TITLE_MANAGE_TEMPLATES = "newsletter.manage_templates.pageTitle";
    private static final String MESSAGE_PAGE_TITLE_ADD_TEMPLATE = "newsletter.add_newsletter_template.pageTitle";
    private static final String MESSAGE_PAGE_TITLE_MODIFY_TEMPLATE = "newsletter.modify_newsletter_template.pageTitle";
    private static final String MESSAGE_PAGE_TITLE_MODIFY_TEMPLATE_FILE = "newsletter.modify_newsletter_template_file.pageTitle";
    private static final String MESSAGE_NEWSLETTER_TEMPLATE = "newsletter.template.type.newsletter.label";
    private static final String MESSAGE_IMAGE_FILE_ALREADY_EXISTS = "newsletter.message.imageFileAlreadyExists";
    private static final String MESSAGE_FILE_ALREADY_EXISTS = "newsletter.message.fileAlreadyExists";
    private static final String MESSAGE_USED_TEMPLATE = "newsletter.message.usedTemplate";
    private static final String MESSAGE_CONFIRM_REMOVE_NEWSLETTER_TEMPLATE = "newsletter.message.confirmRemoveNewsletterTemplate";
    private static final String MESSAGE_WRONG_IMAGE_EXTENSION = "portal.util.message.wrongImageExtention";
    private static final String MESSAGE_WRONG_HTML_EXTENSION = "portal.util.message.wrongHtmlExtention";

    // PARAMETERS
    private static final String PARAMETER_TEMPLATE_PICTURE = "newsletter_template_picture";
    private static final String PARAMETER_TEMPLATE_FILE = "newsletter_template_file";
    private static final String PARAMETER_TEMPLATE_SECTION = "newsletter_template_section";
    private static final String PARAMETER_NEWSLETTER_TEMPLATE_NEW_PICTURE = "newsletter_template_new_picture";
    private static final String PARAMETER_NEWSLETTER_TEMPLATE_NEW_FILE = "newsletter_template_new_file";

    // PROPERTIES
    private static final String PROPERTY_PATH_IMAGE_NEWSLETTER_TEMPLATE = "newsletter.path.image.newsletter.template";
    private static final String PROPERTY_PATH_TEMPLATE = "path.templates";

    private static final String TEMPLATE_MANAGE_NEWSLETTER_TEMPLATE = "admin/plugins/newsletter/manage_templates.html";
    private static final String TEMPLATE_CREATE_NEWSLETTER_TEMPLATE = "admin/plugins/newsletter/add_newsletter_template.html";
    private static final String TEMPLATE_MODIFY_NEWSLETTER_TEMPLATE = "admin/plugins/newsletter/modify_newsletter_template.html";
    private static final String TEMPLATE_MODIFY_NEWSLETTER_TEMPLATE_FILE = "admin/plugins/newsletter/modify_newsletter_template_file.html";

    // URL
    private static final String JSP_URL_MANAGE_NEWSLETTER_TEMPLATES = "ManageTemplates.jsp";
    private static final String JSP_DO_REMOVE_NEWSLETTER_TEMPLATE = "jsp/admin/plugins/newsletter/DoRemoveNewsLetterTemplate.jsp";

    private static final String CONSTANT_END_OF_LINE = "\n";

    private NewsletterTopicService _newsletterTopicService = NewsletterTopicService.getService( );

    /**
     * Builds the newsletter's templates management page
     * 
     * @param request The HTTP request
     * @return the html code for newsletter's templates management page (liste
     *         of templates + available actions)
     */
    public String getManageTemplates( HttpServletRequest request )
    {
        setPageTitleProperty( MESSAGE_PAGE_TITLE_MANAGE_TEMPLATES );

        Map<String, Object> model = new HashMap<String, Object>( );

        Collection<NewsLetterTemplate> refListAllTemplates = NewsLetterTemplateHome.getTemplatesList( getPlugin( ) );
        refListAllTemplates = AdminWorkgroupService.getAuthorizedCollection( refListAllTemplates, getUser( ) );

        Collection<Map<String, Object>> listNewsletterTemplateDisplay = new ArrayList<Map<String, Object>>( );

        for ( NewsLetterTemplate newsletterTemplate : refListAllTemplates )
        {
            Map<String, Object> newsletterTemplateDisplay = new HashMap<String, Object>( );
            newsletterTemplateDisplay.put( NewsLetterConstants.MARK_TEMPLATE, newsletterTemplate );
            newsletterTemplateDisplay.put( MARK_NEWSLETTER_TEMPLATE_ALLOW_CREATION, RBACService.isAuthorized(
                    newsletterTemplate, NewsletterTemplateResourceIdService.PERMISSION_CREATE, getUser( ) ) );

            newsletterTemplateDisplay.put( MARK_NEWSLETTER_TEMPLATE_ALLOW_DELETION, RBACService.isAuthorized(
                    newsletterTemplate, NewsletterTemplateResourceIdService.PERMISSION_DELETE, getUser( ) ) );
            newsletterTemplateDisplay.put( MARK_NEWSLETTER_TEMPLATE_ALLOW_MODIFICATION, RBACService.isAuthorized(
                    newsletterTemplate, NewsletterTemplateResourceIdService.PERMISSION_MODIFY, getUser( ) ) );

            // The workgroup description is needed for coherence and not the key
            if ( newsletterTemplate.getWorkgroup( ).equals( NewsLetterConstants.ALL_GROUPS ) )
            {
                newsletterTemplateDisplay.put( MARK_NEWSLETTER_TEMPLATE_WORKGROUP_DESCRIPTION,
                        I18nService.getLocalizedString( NewsLetterConstants.PROPERTY_LABEL_ALL_GROUPS, getLocale( ) ) );
            }
            else
            {
                newsletterTemplateDisplay.put( MARK_NEWSLETTER_TEMPLATE_WORKGROUP_DESCRIPTION, AdminWorkgroupHome
                        .findByPrimaryKey( newsletterTemplate.getWorkgroup( ) ).getDescription( ) );
            }

            listNewsletterTemplateDisplay.add( newsletterTemplateDisplay );
        }

        model.put( MARK_TEMPLATES_LIST, listNewsletterTemplateDisplay );
        model.put( MARK_ALLOW_CREATION, isNewsletterTemplateCreationAllowed( request ) );

        // get the list of all templates
        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MANAGE_NEWSLETTER_TEMPLATE, getLocale( ),
                model );

        return getAdminPage( template.getHtml( ) );
    }

    /**
     * Builds the newsletter's templates creation page
     * 
     * @param request The HTTP request
     * @return the html code for newsletter's templates creation page
     */
    public String getCreateNewsLetterTemplate( HttpServletRequest request )
    {
        if ( !isNewsletterTemplateCreationAllowed( request ) )
        {
            return getManageTemplates( request );
        }

        setPageTitleProperty( MESSAGE_PAGE_TITLE_ADD_TEMPLATE );

        // get the list of template types
        // nothing should be checked
        Map<String, Object> model = new HashMap<String, Object>( );
        model.put( NewsLetterConstants.MARK_TEMPLATE_TYPE,
                buildTemplateTypeList( AdminUserService.getLocale( request ) ) );
        model.put( MARK_WORKGROUP_LIST, AdminWorkgroupService.getUserWorkgroups( getUser( ), getLocale( ) ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_CREATE_NEWSLETTER_TEMPLATE, getLocale( ),
                model );

        return getAdminPage( template.getHtml( ) );
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

        NewsLetterTemplate newsletterTemplate = new NewsLetterTemplate( );

        try
        {
            // initialize the paths
            String strPathImageNewsletterTemplate = AppPathService.getPath( PROPERTY_PATH_IMAGE_NEWSLETTER_TEMPLATE );
            String strPathFileNewsletterTemplate = AppPathService.getPath( PROPERTY_PATH_TEMPLATE )
                    + AppPropertiesService.getProperty( NewsLetterConstants.PROPERTY_PATH_FILE_NEWSLETTER_TEMPLATE );

            // create the multipart request
            if ( request instanceof MultipartHttpServletRequest )
            {
                MultipartHttpServletRequest multi = (MultipartHttpServletRequest) request;

                // Mandatory fields
                String strTopicType = multi.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_TYPE );
                String strDescription = multi.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_NAME );
                String strWorkgroup = multi.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_WORKGROUP );

                FileItem imageItem = multi.getFile( PARAMETER_TEMPLATE_PICTURE );

                String strImageFileName = imageItem == null ? null : UploadUtil.cleanFileName( imageItem.getName( ) );

                FileItem modelItem = multi.getFile( PARAMETER_TEMPLATE_FILE );
                String strTemplateFileName = modelItem == null ? null : UploadUtil.cleanFileName( modelItem.getName( ) );
                String strSectionNumber = request.getParameter( PARAMETER_TEMPLATE_SECTION );

                if ( StringUtils.isEmpty( strWorkgroup ) || StringUtils.isEmpty( strTopicType )
                        || StringUtils.isEmpty( strDescription ) || imageItem == null
                        || StringUtils.isEmpty( strImageFileName ) || !FileUtil.hasImageExtension( strImageFileName )
                        || modelItem == null || StringUtils.isEmpty( strTemplateFileName )
                        || !FileUtil.hasHtmlExtension( strTemplateFileName )
                        || !StringUtils.isNumeric( strSectionNumber ) )
                {
                    return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS,
                            AdminMessage.TYPE_STOP );
                }

                // create the directory if it doesn't exist
                if ( !new File( strPathImageNewsletterTemplate ).exists( ) )
                {
                    File fDirectory = new File( strPathImageNewsletterTemplate );
                    if ( !fDirectory.exists( ) )
                    {
                        fDirectory.mkdir( );
                    }
                }

                File fileImage = new File( strPathImageNewsletterTemplate + File.separator + strImageFileName );

                if ( fileImage.exists( ) )
                {
                    return AdminMessageService.getMessageUrl( request, MESSAGE_IMAGE_FILE_ALREADY_EXISTS,
                            AdminMessage.TYPE_STOP );
                }

                int nSections = Integer.parseInt( strSectionNumber );

                File fileTemplate = new File( strPathFileNewsletterTemplate + File.separator + strTemplateFileName );
                if ( fileTemplate.exists( ) )
                {
                    return AdminMessageService.getMessageUrl( request, MESSAGE_FILE_ALREADY_EXISTS,
                            AdminMessage.TYPE_STOP );
                }

                // if files are ok, save them
                imageItem.write( fileImage );
                newsletterTemplate.setPicture( strImageFileName );

                modelItem.write( fileTemplate );
                newsletterTemplate.setFileName( strTemplateFileName );

                // Complete the newsLetterTemplate
                newsletterTemplate.setDescription( strDescription );
                newsletterTemplate.setTopicType( strTopicType );
                newsletterTemplate.setWorkgroup( strWorkgroup );
                if ( nSections <= 0 )
                {
                    nSections = 1;
                }
                newsletterTemplate.setSectionNumber( nSections );
                NewsLetterTemplateHome.create( newsletterTemplate, getPlugin( ) );
            }
        }
        catch ( Exception e )
        {
            AppLogService.error( e.getMessage( ), e );
        }

        return getHomeUrl( request );
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
        NewsLetterTemplate newsletterTemplate = NewsLetterTemplateHome.findByPrimaryKey( nIdTemplate, getPlugin( ) );

        //Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletterTemplate, getUser( ) )
                || !RBACService.isAuthorized( NewsLetterTemplate.RESOURCE_TYPE,
                        Integer.toString( newsletterTemplate.getId( ) ),
                        NewsletterTemplateResourceIdService.PERMISSION_MODIFY, getUser( ) ) )
        {
            return getManageTemplates( request );
        }

        setPageTitleProperty( MESSAGE_PAGE_TITLE_MODIFY_TEMPLATE );

        // get the list of template types
        Map<String, Object> model = new HashMap<String, Object>( );
        model.put( NewsLetterConstants.MARK_TEMPLATE_TYPE,
                buildTemplateTypeList( AdminUserService.getLocale( request ) ) );
        model.put( MARK_WORKGROUP_LIST, AdminWorkgroupService.getUserWorkgroups( getUser( ), getLocale( ) ) );
        model.put( NewsLetterConstants.MARK_TEMPLATE, newsletterTemplate );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MODIFY_NEWSLETTER_TEMPLATE, getLocale( ),
                model );

        return getAdminPage( template.getHtml( ) );
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
            String strPathImageNewsletterTemplate = AppPathService.getPath( PROPERTY_PATH_IMAGE_NEWSLETTER_TEMPLATE );
            String strPathFileNewsletterTemplate = AppPathService.getPath( PROPERTY_PATH_TEMPLATE )
                    + AppPropertiesService.getProperty( NewsLetterConstants.PROPERTY_PATH_FILE_NEWSLETTER_TEMPLATE );

            // create the multipart request
            if ( request instanceof MultipartHttpServletRequest )
            {
                MultipartHttpServletRequest multi = (MultipartHttpServletRequest) request;

                // creation of the NewsLetterTemplate
                NewsLetterTemplate newsletterTemplate = NewsLetterTemplateHome.findByPrimaryKey(
                        Integer.parseInt( multi.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_ID ) ),
                        getPlugin( ) );

                //Workgroup & RBAC permissions
                if ( !AdminWorkgroupService.isAuthorized( newsletterTemplate, getUser( ) )
                        || !RBACService.isAuthorized( NewsLetterTemplate.RESOURCE_TYPE,
                                Integer.toString( newsletterTemplate.getId( ) ),
                                NewsletterTemplateResourceIdService.PERMISSION_MODIFY, getUser( ) ) )
                {
                    return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED,
                            AdminMessage.TYPE_ERROR );
                }

                // Mandatory fields
                String strType = multi.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_TYPE );
                String strDescription = multi.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_NAME );
                String strWorkgroup = multi.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_WORKGROUP );
                String strSectionNumber = request.getParameter( PARAMETER_TEMPLATE_SECTION );

                if ( StringUtils.isEmpty( strDescription ) || StringUtils.isEmpty( strWorkgroup )
                        || !StringUtils.isNumeric( strSectionNumber ) )
                {
                    return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS,
                            AdminMessage.TYPE_STOP );
                }
                int nSections = Integer.parseInt( strSectionNumber );

                // Names of the old files
                String strOldFileName = newsletterTemplate.getFileName( );
                String strOldImageName = newsletterTemplate.getPicture( );

                FileItem imageItem = multi.getFile( PARAMETER_NEWSLETTER_TEMPLATE_NEW_PICTURE );
                String strImageFileName = null;
                File fileImage = null;

                if ( ( imageItem != null ) && ( imageItem.getSize( ) != 0 ) )
                {
                    strImageFileName = UploadUtil.cleanFileName( imageItem.getName( ) );
                    String strError = null;
                    if ( !FileUtil.hasImageExtension( strImageFileName ) )
                    {
                        strError = MESSAGE_WRONG_IMAGE_EXTENSION;
                    }

                    String strFullPathNewImageFileName = strPathImageNewsletterTemplate + File.separator
                            + strImageFileName;
                    String strFullPathOldImageFileName = strPathImageNewsletterTemplate + File.separator
                            + strOldImageName;
                    fileImage = new File( strFullPathNewImageFileName );

                    if ( fileImage.exists( ) && !( strFullPathNewImageFileName ).equals( strFullPathOldImageFileName ) )
                    {
                        strError = MESSAGE_IMAGE_FILE_ALREADY_EXISTS;
                    }
                    if ( strError != null )
                    {
                        return AdminMessageService.getMessageUrl( request, strError, AdminMessage.TYPE_STOP );
                    }

                    // we delete the old picture
                    File oldImageFile = new File( strFullPathOldImageFileName );
                    oldImageFile.delete( );
                }

                FileItem modelItem = multi.getFile( PARAMETER_NEWSLETTER_TEMPLATE_NEW_FILE );

                if ( ( modelItem != null ) && ( modelItem.getSize( ) != 0 ) )
                {
                    String strFileName = UploadUtil.cleanFileName( modelItem.getName( ) );
                    String strFullPathNewFileName = strPathFileNewsletterTemplate + File.separator + strFileName;
                    String strFullPathOldFileName = strPathFileNewsletterTemplate + File.separator + strOldFileName;
                    File fileTemplate = new File( strFullPathNewFileName );
                    String strError = null;
                    if ( fileTemplate.exists( ) && !( strFullPathNewFileName ).equals( strFullPathOldFileName ) )
                    {
                        strError = MESSAGE_FILE_ALREADY_EXISTS;
                    }
                    if ( !FileUtil.hasHtmlExtension( strFileName ) )
                    {
                        strError = MESSAGE_WRONG_HTML_EXTENSION;
                    }
                    if ( strError != null )
                    {
                        return AdminMessageService.getMessageUrl( request, strError, AdminMessage.TYPE_STOP );
                    }

                    // we delete the old file
                    File oldFile = new File( strFullPathOldFileName );
                    oldFile.delete( );

                    modelItem.write( fileTemplate );
                    newsletterTemplate.setFileName( strFileName );
                }

                //if the two files are ok, write them
                if ( imageItem != null && fileImage != null && strImageFileName != null )
                {
                    imageItem.write( fileImage );
                    newsletterTemplate.setPicture( strImageFileName );
                }

                // Complete the newsLetterTemplate
                newsletterTemplate.setDescription( strDescription );
                newsletterTemplate.setTopicType( strType );
                newsletterTemplate.setWorkgroup( strWorkgroup );

                int nOldSectionNumber = newsletterTemplate.getSectionNumber( );
                NewsletterService.getService( ).modifySectionNumber( nOldSectionNumber, nSections,
                        newsletterTemplate.getId( ) );
                if ( nSections > 0 )
                {
                    newsletterTemplate.setSectionNumber( nSections );
                }
                NewsLetterTemplateHome.update( newsletterTemplate, getPlugin( ) );
            }
        }
        catch ( Exception e )
        {
            AppLogService.error( e.getMessage( ), e );
        }

        return getHomeUrl( request );
    }

    /**
     * Builds the newsletter's templates modification page (with the
     * modification of the file content)
     * 
     * @param request The HTTP request
     * @return the html code for newsletter's templates creation page
     */
    public String getModifyNewsLetterTemplateFile( HttpServletRequest request )
    {
        setPageTitleProperty( MESSAGE_PAGE_TITLE_MODIFY_TEMPLATE_FILE );

        Map<String, Object> model = new HashMap<String, Object>( );

        BufferedReader fileReader = null;
        try
        {
            int nIdTemplate = Integer.parseInt( request
                    .getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_ID ) );
            NewsLetterTemplate newsletterTemplate = NewsLetterTemplateHome.findByPrimaryKey( nIdTemplate, getPlugin( ) );

            //Workgroup & RBAC permissions
            if ( !AdminWorkgroupService.isAuthorized( newsletterTemplate, getUser( ) )
                    || !RBACService.isAuthorized( NewsLetterTemplate.RESOURCE_TYPE,
                            Integer.toString( newsletterTemplate.getId( ) ),
                            NewsletterTemplateResourceIdService.PERMISSION_MODIFY, getUser( ) ) )
            {
                return getManageTemplates( request );
            }

            // get the file content
            String strPathFileNewsletterTemplate = AppPathService.getPath( PROPERTY_PATH_TEMPLATE )
                    + AppPropertiesService.getProperty( NewsLetterConstants.PROPERTY_PATH_FILE_NEWSLETTER_TEMPLATE );

            String strFileName = newsletterTemplate.getFileName( );
            fileReader = new BufferedReader( new FileReader( strPathFileNewsletterTemplate + File.separator
                    + strFileName ) );

            StringBuilder sbSource = new StringBuilder( );
            String line = fileReader.readLine( );

            while ( line != null )
            {
                sbSource.append( line + CONSTANT_END_OF_LINE );
                line = fileReader.readLine( );
            }

            fileReader.close( );

            model.put( NewsLetterConstants.MARK_TEMPLATE_TYPE,
                    buildTemplateTypeList( AdminUserService.getLocale( request ) ) );

            model.put( NewsLetterConstants.MARK_TEMPLATE_SOURCE, sbSource.toString( ) );
            model.put( NewsLetterConstants.MARK_TEMPLATE_FILE_NAME, strFileName );
            model.put( NewsLetterConstants.MARK_TEMPLATE, newsletterTemplate );
        }
        catch ( FileNotFoundException f )
        {
            AppLogService.debug( f );
        }
        catch ( IOException i )
        {
            AppLogService.debug( i );
        }
        finally
        {
            IOUtils.closeQuietly( fileReader );
        }
        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MODIFY_NEWSLETTER_TEMPLATE_FILE, getLocale( ),
                model );

        return getAdminPage( template.getHtml( ) );
    }

    /**
     * Processes the modification form of a newsletter template modified by hand
     * by recovering the parameters in the http request
     * 
     * @param request the http request
     * @return The Jsp URL of the process result
     */
    public String doModifyNewsletterTemplateFile( HttpServletRequest request )
    {
        FileWriter fileWriter = null;
        try
        {
            // initialize the paths
            String strPathImageNewsletterTemplate = AppPathService.getPath( PROPERTY_PATH_IMAGE_NEWSLETTER_TEMPLATE );
            String strPathFileNewsletterTemplate = AppPathService.getPath( PROPERTY_PATH_TEMPLATE )
                    + AppPropertiesService.getProperty( NewsLetterConstants.PROPERTY_PATH_FILE_NEWSLETTER_TEMPLATE );

            // create the multipart request
            if ( request instanceof MultipartHttpServletRequest )
            {
                MultipartHttpServletRequest multi = (MultipartHttpServletRequest) request;

                // creation of the NewsLetterTemplate
                NewsLetterTemplate newsletterTemplate = NewsLetterTemplateHome.findByPrimaryKey(
                        Integer.parseInt( multi.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_ID ) ),
                        getPlugin( ) );

                //Workgroup & RBAC permissions
                if ( !AdminWorkgroupService.isAuthorized( newsletterTemplate, getUser( ) )
                        || !RBACService.isAuthorized( NewsLetterTemplate.RESOURCE_TYPE,
                                Integer.toString( newsletterTemplate.getId( ) ),
                                NewsletterTemplateResourceIdService.PERMISSION_MODIFY, getUser( ) ) )
                {
                    return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED,
                            AdminMessage.TYPE_ERROR );
                }

                // Mandatory fields
                String strType = multi.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_TYPE );
                String strDescription = multi.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_NAME );
                String strSectionNumber = request.getParameter( PARAMETER_TEMPLATE_SECTION );

                if ( StringUtils.isEmpty( strDescription ) || !StringUtils.isNumeric( strSectionNumber ) )
                {
                    return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS,
                            AdminMessage.TYPE_STOP );
                }
                int nSections = Integer.parseInt( strSectionNumber );

                // Names of the old files
                String strOldFileName = newsletterTemplate.getFileName( );
                String strOldImageName = newsletterTemplate.getPicture( );

                FileItem imageItem = multi.getFile( PARAMETER_NEWSLETTER_TEMPLATE_NEW_PICTURE );

                if ( ( imageItem != null ) && ( imageItem.getSize( ) != 0 ) )
                {
                    String strFileName = UploadUtil.cleanFileName( imageItem.getName( ) );
                    imageItem.write( new File( strPathImageNewsletterTemplate + File.separator + strFileName ) );
                    newsletterTemplate.setPicture( strFileName );

                    // we delete the old picture
                    File oldImageFile = new File( strPathImageNewsletterTemplate + File.separator + strOldImageName );
                    oldImageFile.delete( );
                }

                // Writes the new content of the file.
                String fileContent = multi.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_SOURCE );

                fileWriter = new FileWriter( strPathFileNewsletterTemplate + File.separator + strOldFileName );
                fileWriter.write( fileContent );
                fileWriter.close( );

                int nOldSectionNumber = newsletterTemplate.getSectionNumber( );
                NewsletterService.getService( ).modifySectionNumber( nOldSectionNumber, nSections,
                        newsletterTemplate.getId( ) );

                // Complete the newsLetterTemplate
                newsletterTemplate.setDescription( strDescription );
                newsletterTemplate.setTopicType( strType );
                if ( nSections > 0 )
                {
                    newsletterTemplate.setSectionNumber( nSections );
                }
                NewsLetterTemplateHome.update( newsletterTemplate, getPlugin( ) );
            }
        }
        catch ( IOException io )
        {
            AppLogService.error( io.getMessage( ), io );
        }
        catch ( Exception e )
        {
            AppLogService.error( e.getMessage( ), e );
        }
        finally
        {
            IOUtils.closeQuietly( fileWriter );
        }

        return getHomeUrl( request );
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
        String strNewsletterTemplateId = request.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_ID );
        int nNewsletterTemplateId = Integer.parseInt( strNewsletterTemplateId );
        NewsLetterTemplate newsletterTemplate = NewsLetterTemplateHome.findByPrimaryKey( nNewsletterTemplateId,
                getPlugin( ) );

        //Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsletterTemplate, getUser( ) )
                || !RBACService.isAuthorized( NewsLetterTemplate.RESOURCE_TYPE,
                        Integer.toString( newsletterTemplate.getId( ) ),
                        NewsletterTemplateResourceIdService.PERMISSION_DELETE, getUser( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }
        List<String> listMessages = new ArrayList<String>( );
        if ( !NewsletterTemplateRemovalService.getService( ).checkForRemoval( strNewsletterTemplateId, listMessages,
                AdminUserService.getLocale( request ) ) )
        {
            Object[] args = { listMessages.get( 0 ) };
            return AdminMessageService.getMessageUrl( request, MESSAGE_USED_TEMPLATE, args, AdminMessage.TYPE_STOP );
        }

        UrlItem url = new UrlItem( JSP_DO_REMOVE_NEWSLETTER_TEMPLATE );
        url.addParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_ID,
                Integer.parseInt( request.getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_ID ) ) );

        return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_NEWSLETTER_TEMPLATE, url.getUrl( ),
                AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Processes the removal form of a newsletter template
     * 
     * @param request The Http request
     * @return the jsp URL to display the form to manage newsletter templates
     */
    public String doRemoveNewsLetterTemplate( HttpServletRequest request )
    {
        int nNewsletterTemplateId = Integer.parseInt( request
                .getParameter( NewsLetterConstants.PARAMETER_NEWSLETTER_TEMPLATE_ID ) );

        NewsLetterTemplate newsLetterTemplate = NewsLetterTemplateHome.findByPrimaryKey( nNewsletterTemplateId,
                getPlugin( ) );

        //Workgroup & RBAC permissions
        if ( !AdminWorkgroupService.isAuthorized( newsLetterTemplate, getUser( ) )
                || !RBACService.isAuthorized( NewsLetterTemplate.RESOURCE_TYPE,
                        Integer.toString( newsLetterTemplate.getId( ) ),
                        NewsletterTemplateResourceIdService.PERMISSION_DELETE, getUser( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR );
        }

        String strFileName = newsLetterTemplate.getFileName( );
        String strPictureName = newsLetterTemplate.getPicture( );

        // removes the file
        String strPathFileNewsletterTemplate = AppPathService.getPath( PROPERTY_PATH_TEMPLATE )
                + AppPropertiesService.getProperty( NewsLetterConstants.PROPERTY_PATH_FILE_NEWSLETTER_TEMPLATE );
        File file = new File( strPathFileNewsletterTemplate + NewsLetterConstants.CONSTANT_SLASH + strFileName );

        if ( file.exists( ) )
        {
            file.delete( );
        }

        // removes the picture
        String strPathImageNewsletterTemplate = AppPathService.getPath( PROPERTY_PATH_IMAGE_NEWSLETTER_TEMPLATE );
        File picture = new File( strPathImageNewsletterTemplate + NewsLetterConstants.CONSTANT_SLASH + strPictureName );

        if ( picture.exists( ) )
        {
            picture.delete( );
        }

        // removes the newsletter template from the database
        NewsLetterTemplateHome.remove( nNewsletterTemplateId, getPlugin( ) );

        // loads the newsletter templates management page
        // If the operation occurred well returns on the info of the newsletter
        UrlItem url = new UrlItem( JSP_URL_MANAGE_NEWSLETTER_TEMPLATES );

        return url.getUrl( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Plugin getPlugin( )
    {
        return PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
    }

    /**
     * Build a radio buttons list of template types from properties
     * @param locale The locale
     * @return the html code for the radio buttons list
     */
    private ReferenceList buildTemplateTypeList( Locale locale )
    {
        ReferenceList refTemplateTypeList = _newsletterTopicService.getNewsletterTopicTypeRefList( locale );
        ReferenceItem refItemTemplate = new ReferenceItem( );
        refItemTemplate.setCode( NewsLetterTemplate.RESOURCE_TYPE );
        refItemTemplate.setName( I18nService.getLocalizedString( MESSAGE_NEWSLETTER_TEMPLATE, locale ) );
        refTemplateTypeList.add( 0, refItemTemplate );

        return refTemplateTypeList;
    }

    /**
     * Check if user is authorized to create a newsletter template
     * @param request The {@link HttpServletRequest}
     * @return true if creation is authorized, false otherwise
     */
    private boolean isNewsletterTemplateCreationAllowed( HttpServletRequest request )
    {
        //RBAC permission
        AdminUser user = AdminUserService.getAdminUser( request );
        if ( RBACService.isAuthorized( NewsLetterTemplate.RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID,
                NewsletterTemplateResourceIdService.PERMISSION_CREATE, user ) )
        {
            return true;
        }

        Collection<NewsLetterTemplate> listNewsletterTemplates = NewsLetterTemplateHome.getTemplatesList( getPlugin( ) );
        listNewsletterTemplates = AdminWorkgroupService.getAuthorizedCollection( listNewsletterTemplates, user );

        for ( NewsLetterTemplate newsletterTemplate : listNewsletterTemplates )
        {
            if ( RBACService.isAuthorized( newsletterTemplate, NewsletterTemplateResourceIdService.PERMISSION_CREATE,
                    user ) )
            {
                return true;
            }
        }

        return false;
    }

}
