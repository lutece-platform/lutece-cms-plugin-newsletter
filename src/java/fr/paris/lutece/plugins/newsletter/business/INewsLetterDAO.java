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

import java.sql.Timestamp;
import java.util.Collection;

/*
 * The interface defining the business methods used by the newsletter plugin
 */

/**
 * The interface representing the business methods data layer must implement
 */
public interface INewsLetterDAO
{
    /**
     * Insert a new record in the table.
     * 
     * @param newsLetter
     *            the object to insert
     * @param plugin
     *            the Plugin
     */
    void insert( NewsLetter newsLetter, Plugin plugin );

    /**
     * Remove a record from the table
     * 
     * @param nNewsLetterId
     *            the newsletter identifier
     * @param plugin
     *            the Plugin
     */
    void delete( int nNewsLetterId, Plugin plugin );

    /**
     * loads the data of the newsletter from the table
     * 
     * @param nNewsLetterId
     *            the newsletter identifier
     * @param plugin
     *            the Plugin
     * @return the object inserted
     */
    NewsLetter load( int nNewsLetterId, Plugin plugin );

    /**
     * Update the record in the table
     * 
     * @param newsLetter
     *            the object to be updated
     * @param plugin
     *            the Plugin
     */
    void store( NewsLetter newsLetter, Plugin plugin );

    /**
     * Check the unicity of the primary key
     * 
     * @param nKey
     *            the key to be checked
     * @param plugin
     *            the Plugin
     * @return true if the identifier exist and false if not
     */
    boolean checkPrimaryKey( int nKey, Plugin plugin );

    /**
     * Checks whether a portlet uses the newsletter
     * 
     * 
     * @return true if the newsletter is used
     * @param nIdNewsletter
     *            the id of the newsletter
     */
    boolean checkLinkedPortlet( int nIdNewsletter );

    /**
     * Generate a new primary key to add a newsletter
     * 
     * @param plugin
     *            the Plugin
     * @return the new key
     */
    int newPrimaryKey( Plugin plugin );

    /**
     * Select the list of the newsletters available
     * 
     * @param plugin
     *            the Plugin
     * @return a collection of objects
     */
    Collection<NewsLetter> selectAll( Plugin plugin );

    /**
     * Select the list of the newsletters available
     * 
     * @param plugin
     *            the Plugin
     * @return a {@link ReferenceList} of id and name
     */
    ReferenceList selectAllId( Plugin plugin );

    /**
     * Gets a collection of NewsLetter associated with a given template
     * 
     * @param nTemplateId
     *            The id of the template
     * @param plugin
     *            the Plugin
     * @return the collection of objects
     */
    Collection<NewsLetter> selectAllByTemplateId( int nTemplateId, Plugin plugin );

    /**
     * Insert a new subscriber for a newsletter
     * 
     * @param nNewsLetterId
     *            the newsletter identifier
     * @param nSubscriberId
     *            the subscriber identifier
     * @param tToday
     *            The day
     * @param plugin
     *            the Plugin
     */
    void insertSubscriber( int nNewsLetterId, int nSubscriberId, Timestamp tToday, Plugin plugin );

    /**
     * Performs confirm unsubscription process
     * 
     * @param confirmLimitDate
     *            all unconfirmed subscriber which subscription date is below confirmLimitDate will be deleted
     * @param plugin
     *            the plugin
     */
    void deleteOldUnconfirmed( Timestamp confirmLimitDate, Plugin plugin );

    /**
     * Insert a new subscriber for a newsletter
     * 
     * @param nNewsLetterId
     *            the newsletter identifier
     * @param nSubscriberId
     *            the subscriber identifier
     * @param bValidate
     *            the validation status
     * @param tToday
     *            The day
     * @param plugin
     *            the Plugin
     */
    void insertSubscriber( int nNewsLetterId, int nSubscriberId, boolean bValidate, Timestamp tToday, Plugin plugin );

    /**
     * Updates a subscriber for a newsletter
     * 
     * @param nNewsLetterId
     *            the newsletter identifier
     * @param nSubscriberId
     *            the subscriber identifier
     * @param plugin
     *            the Plugin
     */
    void validateSubscriber( int nNewsLetterId, int nSubscriberId, Plugin plugin );

    /**
     * Remove the subscriber's inscription to a newsletter
     * 
     * @param nNewsLetterId
     *            the newsletter identifier
     * @param nSubscriberId
     *            the subscriber identifier
     * @param plugin
     *            the Plugin
     */
    void deleteSubscriber( int nNewsLetterId, int nSubscriberId, Plugin plugin );

    /**
     * check if the subscriber is not yet registered to a newsletter
     * 
     * @param nNewsLetterId
     *            the newsletter identifier
     * @param nSubscriberId
     *            the subscriber identifier
     * @param plugin
     *            the Plugin
     * @return true if he is registered and false if not
     */
    boolean isRegistered( int nNewsLetterId, int nSubscriberId, Plugin plugin );

    /**
     * controls that a template is used by a newsletter
     * 
     * @param nTemplateId
     *            the template identifier
     * @param plugin
     *            the Plugin
     * @return true if the template is used, false if not
     */
    boolean isTemplateUsed( int nTemplateId, Plugin plugin );

    /**
     * Counts the subscribers for a newsletter
     * 
     * @param nNewsLetterId
     *            the newsletter newsletter
     * @param strSearchString
     *            the string to search in the subscriber's email
     * @param plugin
     *            the Plugin
     * @return the number of subscribers
     */
    int selectNbrSubscribers( int nNewsLetterId, String strSearchString, Plugin plugin );

    /**
     * Counts the active subscribers for a newsletter
     * 
     * @param nNewsLetterId
     *            the newsletter newsletter
     * @param strSearchString
     *            the string to search in the subscriber's email
     * @param plugin
     *            the Plugin
     * @return the number of subscribers
     */
    int selectNbrActiveSubscribers( int nNewsLetterId, String strSearchString, Plugin plugin );

}
