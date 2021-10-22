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
import fr.paris.lutece.util.ReferenceList;

import java.util.Collection;
import java.util.List;

/**
 * @author gduge
 * 
 */
public interface INewsLetterTemplateDAO
{
    /**
     * Returns the list of all templates
     * 
     * @param plugin
     *            The plugin
     * @return the collection of all templates
     */
    Collection<NewsLetterTemplate> selectTemplatesList( Plugin plugin );

    /**
     * Returns the list of all templates
     * 
     * @param strTopicType
     *            the topic type of templates
     * @param plugin
     *            The plugin
     * @return the reference list of the templates corresponding to the given type
     */
    ReferenceList selectTemplatesListByType( String strTopicType, Plugin plugin );

    /**
     * Returns a list of templates depending on the given type
     * 
     * @param strTopicType
     *            the topic type of templates to get
     * @param plugin
     *            The plugin
     * @return the collection of the templates corresponding to the given topic type
     */
    List<NewsLetterTemplate> selectTemplatesCollectionByType( String strTopicType, Plugin plugin );

    /**
     * Insert a new record in the table.
     * 
     * @param newsletter
     *            The Instance of the object NewsLetterTemplate
     * @param plugin
     *            the plugin
     */
    void insert( NewsLetterTemplate newsletter, Plugin plugin );

    /**
     * loads the data of the newsletter's template from the table
     * 
     * @param nTemplateId
     *            the template identifier
     * @param plugin
     *            the plugin
     * @return the object inserted
     */
    NewsLetterTemplate load( int nTemplateId, Plugin plugin );

    /**
     * Update a given record in the table.
     * 
     * @param newsLetterTemplate
     *            The Instance of the object NewsLetterTemplate
     * @param plugin
     *            the plugin
     */
    void store( NewsLetterTemplate newsLetterTemplate, Plugin plugin );

    /**
     * Remove a record from the table
     * 
     * @param nNewsLetterTemplateId
     *            the template identifier
     * @param plugin
     *            the Plugin
     */
    void delete( int nNewsLetterTemplateId, Plugin plugin );

    /**
     * Returns the list of all templates
     * 
     * @param plugin
     *            The plugin
     * @return the reference list of the templates
     */
    ReferenceList selectTemplatesByRef( Plugin plugin );

    /**
     * Get the list of temmplates associated with a given workgroup
     * 
     * @param strWorkgroupKey
     *            The key of the workgroup
     * @param plugin
     *            The plugin
     * @return The list of temmplates associated with a given workgroup
     */
    Collection<NewsLetterTemplate> selectTemplatesListByWorkgoup( String strWorkgroupKey, Plugin plugin );
}
