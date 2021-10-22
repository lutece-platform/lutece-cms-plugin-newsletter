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
package fr.paris.lutece.plugins.newsletter.business;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.util.ReferenceList;

import java.util.Collection;
import java.util.List;

/**
 * This class provides instances management methods (create, find, ...) for NewsLetterTemplate objects
 */
public final class NewsLetterTemplateHome
{
    /**
     * The data access object initialised by Spring mechanism
     */
    private static INewsLetterTemplateDAO _dao = SpringContextService.getBean( "newsletter.newsLetterTemplateDAO" );

    /**
     * Private constructor - this class need not be instantiated
     */
    private NewsLetterTemplateHome( )
    {
    }

    /**
     * Returns the list of every templates
     * 
     * @param plugin
     *            The Plugin
     * @return a collection object
     */
    public static Collection<NewsLetterTemplate> getTemplatesList( Plugin plugin )
    {
        return _dao.selectTemplatesList( plugin );
    }

    /**
     * Returns a list of templates depending on the given type
     * 
     * @param strTopicType
     *            the topic type of templates to retrieve
     * @param plugin
     *            The Plugin
     * @return a Referencelist object
     */
    public static ReferenceList getTemplatesListByType( String strTopicType, Plugin plugin )
    {
        return _dao.selectTemplatesListByType( strTopicType, plugin );
    }

    /**
     * Returns a list of templates depending on the given topic type
     * 
     * @param strTopicType
     *            the topic type of templates to retrieve
     * @param plugin
     *            The Plugin
     * @return a Collection object
     */
    public static List<NewsLetterTemplate> getTemplatesCollectionByType( String strTopicType, Plugin plugin )
    {
        return _dao.selectTemplatesCollectionByType( strTopicType, plugin );
    }

    /**
     * Creation of an instance of a newsletter template
     * 
     * @param newsletterTemplate
     *            template An instance of a newsletter template which contains the informations to store
     * @param plugin
     *            The Plugin
     * @return The instance of a newsletter template which has been created with its primary key.
     */
    public static NewsLetterTemplate create( NewsLetterTemplate newsletterTemplate, Plugin plugin )
    {
        _dao.insert( newsletterTemplate, plugin );

        return newsletterTemplate;
    }

    /**
     * Returns an object NewsLetter's template from its identifier
     * 
     * @param nKey
     *            the primary key of the newsletter's template
     * @param plugin
     *            The Plugin
     * @return an instance of the class
     */
    public static NewsLetterTemplate findByPrimaryKey( int nKey, Plugin plugin )
    {
        return _dao.load( nKey, plugin );
    }

    /**
     * Update of an instance of a newsletter template
     * 
     * @param newsletterTemplate
     *            template An instance of a newsletter template which contains the informations to store
     * @param plugin
     *            The Plugin
     * @return The instance of a newsletter template which has been updated with its primary key.
     */
    public static NewsLetterTemplate update( NewsLetterTemplate newsletterTemplate, Plugin plugin )
    {
        _dao.store( newsletterTemplate, plugin );

        return newsletterTemplate;
    }

    /**
     * Remove the record from the template identifier
     * 
     * @param nNewsLetterTemplateId
     *            the template identifier
     * @param plugin
     *            the Plugin
     */
    public static void remove( int nNewsLetterTemplateId, Plugin plugin )
    {
        _dao.delete( nNewsLetterTemplateId, plugin );
    }

    /**
     * Fetches a template list
     * 
     * @param plugin
     *            The plugin
     * @return A reference list
     */
    public static ReferenceList getTemplateListByRef( Plugin plugin )
    {
        return _dao.selectTemplatesByRef( plugin );
    }

    /**
     * Returns the list of templates associated with a given workgroup
     * 
     * @param strWorkgroupKey
     *            The workgoup key
     * @param plugin
     *            The Plugin
     * @return a collection object
     */
    public static Collection<NewsLetterTemplate> getTemplatesListByWorkgoup( String strWorkgroupKey, Plugin plugin )
    {
        return _dao.selectTemplatesListByWorkgoup( strWorkgroupKey, plugin );
    }
}
