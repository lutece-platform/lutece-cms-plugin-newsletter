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
package fr.paris.lutece.plugins.newsletter.service;

import fr.paris.lutece.plugins.newsletter.business.NewsLetter;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterHome;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.rbac.Permission;
import fr.paris.lutece.portal.service.rbac.ResourceIdService;
import fr.paris.lutece.portal.service.rbac.ResourceType;
import fr.paris.lutece.portal.service.rbac.ResourceTypeManager;
import fr.paris.lutece.util.ReferenceList;

import java.util.Locale;

/**
 * Resource Id service for RBAC features to control access to newsletters
 */
public class NewsletterResourceIdService extends ResourceIdService
{
    /**
     * Permission for creating a newsletter
     */
    public static final String PERMISSION_ARCHIVE = "ARCHIVE";

    /**
     * Permission for creating a newsletter
     */
    public static final String PERMISSION_CREATE = "CREATE";

    /**
     * Permission for cleaning subscribers
     */
    public static final String PERMISSION_CLEAN_SUBSCRIBERS = "CLEAN";

    /**
     * Permission for deleting a newsletter
     */
    public static final String PERMISSION_DELETE = "DELETE";

    /**
     * Permission for modifying a newsletter
     */
    public static final String PERMISSION_MODIFY = "MODIFY";

    /**
     * Permission for sending a newsletter
     */
    public static final String PERMISSION_SEND = "SEND";

    /**
     * Permission for managing a Subscribers
     */
    public static final String PERMISSION_MANAGE_SUBSCRIBERS = "MANAGE_SUBSCRIBERS";

    /**
     * Permission for exporting Subscribers
     */
    public static final String PERMISSION_EXPORT_SUBSCRIBERS = "EXPORT_SUBSCRIBERS";

    /**
     * Permission for importing Subscribers
     */
    public static final String PERMISSION_IMPORT_SUBSCRIBERS = "IMPORT_SUBSCRIBERS";

    /**
     * Permission for adding Subscriber
     */
    public static final String PERMISSION_ADD_SUBSCRIBER = "ADD_SUBSCRIBERS";

    /**
     * Permission for advanced settings management
     */
    public static final String PERMISSION_NEWSLETTER_ADVANCED_SETTINGS = "NEWSLETTER_ADVANCED_SETTINGS";

    private static final String REGEX_ID = "^[\\d]+$";

    // i18n properties
    private static final String PROPERTY_LABEL_RESOURCE_TYPE = "newsletter.newsletter.resourceType";
    private static final String PROPERTY_LABEL_CREATE = "newsletter.permission.newsletter.label.create";
    private static final String PROPERTY_LABEL_DELETE = "newsletter.permission.newsletter.label.delete";
    private static final String PROPERTY_LABEL_MODIFY = "newsletter.permission.newsletter.label.modify";
    private static final String PROPERTY_LABEL_SEND = "newsletter.permission.newsletter.label.send";
    private static final String PROPERTY_LABEL_MANAGE_SUBSCRIBERS = "newsletter.permission.newsletter.label.manageSubscribers";
    private static final String PROPERTY_LABEL_EXPORT_SUBSCRIBERS = "newsletter.permission.newsletter.label.exportSubscribers";
    private static final String PROPERTY_LABEL_IMPORT_SUBSCRIBERS = "newsletter.permission.newsletter.label.importSubscribers";
    private static final String PROPERTY_LABEL_ADD_SUBSCRIBER = "newsletter.permission.newsletter.label.addSubscriber";
    private static final String PROPERTY_LABEL_MANAGE_ARCHIVE = "newsletter.permission.newsletter.label.archive";
    private static final String PROPERTY_LABEL_CLEAN_SUBSCRIBERS = "newsletter.permission.newsletter.label.clean";
    private static final String PROPERTY_LABEL_NEWSLETTER_ADVANCED_SETTINGS = "newsletter.permission.label.newsletterAdvancedSettings";

    /** Creates a new instance of NewsletterResourceIdService */
    public NewsletterResourceIdService( )
    {
        setPluginName( NewsletterPlugin.PLUGIN_NAME );
    }

    /**
     * Initializes the service
     */
    public void register( )
    {
        ResourceType rt = new ResourceType( );
        rt.setResourceIdServiceClass( NewsletterResourceIdService.class.getName( ) );
        rt.setPluginName( NewsletterPlugin.PLUGIN_NAME );
        rt.setResourceTypeKey( NewsLetter.RESOURCE_TYPE );
        rt.setResourceTypeLabelKey( PROPERTY_LABEL_RESOURCE_TYPE );

        Permission p = new Permission( );
        p.setPermissionKey( PERMISSION_CREATE );
        p.setPermissionTitleKey( PROPERTY_LABEL_CREATE );
        rt.registerPermission( p );

        p = new Permission( );
        p.setPermissionKey( PERMISSION_DELETE );
        p.setPermissionTitleKey( PROPERTY_LABEL_DELETE );
        rt.registerPermission( p );

        p = new Permission( );
        p.setPermissionKey( PERMISSION_MODIFY );
        p.setPermissionTitleKey( PROPERTY_LABEL_MODIFY );
        rt.registerPermission( p );

        p = new Permission( );
        p.setPermissionKey( PERMISSION_SEND );
        p.setPermissionTitleKey( PROPERTY_LABEL_SEND );
        rt.registerPermission( p );

        p = new Permission( );
        p.setPermissionKey( PERMISSION_MANAGE_SUBSCRIBERS );
        p.setPermissionTitleKey( PROPERTY_LABEL_MANAGE_SUBSCRIBERS );
        rt.registerPermission( p );

        p = new Permission( );
        p.setPermissionKey( PERMISSION_EXPORT_SUBSCRIBERS );
        p.setPermissionTitleKey( PROPERTY_LABEL_EXPORT_SUBSCRIBERS );
        rt.registerPermission( p );

        p = new Permission( );
        p.setPermissionKey( PERMISSION_IMPORT_SUBSCRIBERS );
        p.setPermissionTitleKey( PROPERTY_LABEL_IMPORT_SUBSCRIBERS );
        rt.registerPermission( p );

        p = new Permission( );
        p.setPermissionKey( PERMISSION_ADD_SUBSCRIBER );
        p.setPermissionTitleKey( PROPERTY_LABEL_ADD_SUBSCRIBER );
        rt.registerPermission( p );

        p = new Permission( );
        p.setPermissionKey( PERMISSION_ARCHIVE );
        p.setPermissionTitleKey( PROPERTY_LABEL_MANAGE_ARCHIVE );
        rt.registerPermission( p );

        p = new Permission( );
        p.setPermissionKey( PERMISSION_CLEAN_SUBSCRIBERS );
        p.setPermissionTitleKey( PROPERTY_LABEL_CLEAN_SUBSCRIBERS );
        rt.registerPermission( p );

        p = new Permission( );
        p.setPermissionKey( PERMISSION_NEWSLETTER_ADVANCED_SETTINGS );
        p.setPermissionTitleKey( PROPERTY_LABEL_NEWSLETTER_ADVANCED_SETTINGS );
        rt.registerPermission( p );

        ResourceTypeManager.registerResourceType( rt );
    }

    /**
     * Returns a list of resource ids
     * 
     * @param locale
     *            The current locale
     * @return A list of resource ids
     */
    public ReferenceList getResourceIdList( Locale locale )
    {
        return NewsLetterHome.findAllId( PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME ) );
    }

    /**
     * Returns the Title of a given resource
     * 
     * @param strId
     *            The Id of the resource
     * @param locale
     *            The current locale
     * @return The Title of a given resource
     */
    public String getTitle( String strId, Locale locale )
    {
        if ( ( strId == null ) || !strId.matches( REGEX_ID ) )
        {
            return null;
        }

        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( Integer.parseInt( strId ), PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME ) );

        return ( newsletter != null ) ? newsletter.getName( ) : null;
    }
}
