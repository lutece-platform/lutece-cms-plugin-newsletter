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

import fr.paris.lutece.plugins.newsletter.util.NewsLetterConstants;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.util.ReferenceList;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;

/**
 * This class provides instances management methods (create, find, ...) for NewsLetter objects
 */
public final class NewsLetterHome
{
    // Static variable pointed at the DAO instance
    private static INewsLetterDAO _dao = SpringContextService.getBean( "newsletter.newsLetterDAO" );

    /**
     * Private constructor - this class need not be instantiated
     */
    private NewsLetterHome( )
    {
    }

    /**
     * Create an instance of the newsletter
     * 
     * @param newsLetter
     *            the object to insert into the database
     * @param plugin
     *            the Plugin
     * @return the instance created
     */
    public static NewsLetter create( NewsLetter newsLetter, Plugin plugin )
    {
        _dao.insert( newsLetter, plugin );

        return newsLetter;
    }

    /**
     * update of the newsletter which is specified in parameter
     * 
     * @param newsLetter
     *            the instance of newsletter which contains the data to store
     * @param plugin
     *            the Plugin
     * @return the new instance updated
     */
    public static NewsLetter update( NewsLetter newsLetter, Plugin plugin )
    {
        _dao.store( newsLetter, plugin );

        return newsLetter;
    }

    /**
     * Remove the record from the identifier a newsletter
     * 
     * @param nNewsLetterId
     *            the newsletter identifier
     * @param plugin
     *            the Plugin
     */
    public static void remove( int nNewsLetterId, Plugin plugin )
    {
        _dao.delete( nNewsLetterId, plugin );
    }

    ///////////////////////////////////////////////////////////////////////////
    // Finders

    /**
     * Returns an object NewsLetter from its identifier
     * 
     * @param nKey
     *            the primary key of the newsletter
     * @param plugin
     *            the Plugin
     * @return an instance of the class
     */
    public static NewsLetter findByPrimaryKey( int nKey, Plugin plugin )
    {
        return _dao.load( nKey, plugin );
    }

    /**
     * Returns a collection of NewsLetter objects
     * 
     * @param plugin
     *            the Plugin
     * @return the collection of objects
     */
    public static Collection<NewsLetter> findAll( Plugin plugin )
    {
        return _dao.selectAll( plugin );
    }

    /**
     * Returns a ReferenceList of NewsLetter ids and names
     * 
     * @param plugin
     *            the Plugin
     * @return the ReferenceList of id and name
     */
    public static ReferenceList findAllId( Plugin plugin )
    {
        return _dao.selectAllId( plugin );
    }

    /**
     * Gets a collection of NewsLetter associated with a given template
     * 
     * @param nTemplateId
     *            The id of the template
     * @param plugin
     *            the Plugin
     * @return the collection of objects
     */
    public static Collection<NewsLetter> findAllByTemplateId( int nTemplateId, Plugin plugin )
    {
        return _dao.selectAllByTemplateId( nTemplateId, plugin );
    }

    ////////////////////////////////////////////////////////////////////////////
    // Operations

    /**
     * insert a new subscriber for e newsletter. The subscriber is automatically marked as confirmed
     * 
     * @param nNewsLetterId
     *            the newsletter identifier
     * @param nSubscriberId
     *            the subscriber identifier
     * @param plugin
     *            the Plugin
     * @param tToday
     *            the day
     */
    public static void addSubscriber( int nNewsLetterId, int nSubscriberId, Timestamp tToday, Plugin plugin )
    {
        _dao.insertSubscriber( nNewsLetterId, nSubscriberId, tToday, plugin );
    }

    /**
     * insert a new subscriber for e newsletter
     * 
     * @param nNewsLetterId
     *            the newsletter identifier
     * @param nSubscriberId
     *            the subscriber identifier
     * @param bValidate
     *            <b>true</b> if user is automatically confirmed, <b>false</b> otherwise
     * @param plugin
     *            the Plugin
     * @param tToday
     *            the day
     */
    public static void addSubscriber( int nNewsLetterId, int nSubscriberId, boolean bValidate, Timestamp tToday, Plugin plugin )
    {
        _dao.insertSubscriber( nNewsLetterId, nSubscriberId, bValidate, tToday, plugin );
    }

    /**
     * validates a new subscriber for a newsletter
     * 
     * @param nNewsLetterId
     *            the newsletter identifier
     * @param nSubscriberId
     *            the subscriber identifier
     * @param plugin
     *            the Plugin
     */
    public static void validateSubscriber( int nNewsLetterId, int nSubscriberId, Plugin plugin )
    {
        _dao.validateSubscriber( nNewsLetterId, nSubscriberId, plugin );
    }

    /**
     * removes an subscriber's inscription for a newsletter
     * 
     * @param nNewsLetterId
     *            the newsletter identifier
     * @param nSubscriberId
     *            the subscriber identifier
     * @param plugin
     *            the Plugin
     */
    public static void removeSubscriber( int nNewsLetterId, int nSubscriberId, Plugin plugin )
    {
        _dao.deleteSubscriber( nNewsLetterId, nSubscriberId, plugin );
    }

    /**
     * Performs confirm unsubscription process
     * 
     * @param nConfirmLimit
     *            How many days before deleting a subscriber
     * @param plugin
     *            the plugin
     */
    public static void removeOldUnconfirmed( int nConfirmLimit, Plugin plugin )
    {
        Calendar cal = Calendar.getInstance( );
        cal.add( Calendar.DATE, -nConfirmLimit );

        Timestamp limitDate = new java.sql.Timestamp( cal.getTimeInMillis( ) );
        _dao.deleteOldUnconfirmed( limitDate, plugin );
    }

    /**
     * Returns the number of subscriber for a newsletter
     * 
     * @param nNewsLetterId
     *            the identifier of the newsletter
     * @param plugin
     *            the Plugin
     * @return the number of subscriber for a newsletter
     */
    public static int findNbrSubscribers( int nNewsLetterId, Plugin plugin )
    {
        return _dao.selectNbrSubscribers( nNewsLetterId, NewsLetterConstants.CONSTANT_EMPTY_STRING, plugin );
    }

    /**
     * Returns the number of active subscriber for a newsletter
     * 
     * @param nNewsLetterId
     *            the identifier of the newsletter
     * @param plugin
     *            the Plugin
     * @return the number of subscriber for a newsletter
     */
    public static int findNbrActiveSubscribers( int nNewsLetterId, Plugin plugin )
    {
        return _dao.selectNbrActiveSubscribers( nNewsLetterId, NewsLetterConstants.CONSTANT_EMPTY_STRING, plugin );
    }

    /**
     * controls that a subscriber is not yet registered for a newsletter
     * 
     * @param nNewsLetterId
     *            the newsletter identifier
     * @param nSubscriberId
     *            the subscriber identifier
     * @param plugin
     *            the Plugin
     * @return true if he is registered, false if not
     */
    public static boolean findRegistration( int nNewsLetterId, int nSubscriberId, Plugin plugin )
    {
        return _dao.isRegistered( nNewsLetterId, nSubscriberId, plugin );
    }

    /**
     * controls that a template is used by a newsletter
     * 
     * @param nTemplateId
     *            the template identifier
     * @param plugin
     *            the Plugin
     * @return true if the template is used, false if not
     */
    public static boolean isTemplateUsedByNewsletter( int nTemplateId, Plugin plugin )
    {
        return _dao.isTemplateUsed( nTemplateId, plugin );
    }

    /**
     * Verifies if a portlet uses a newsletter
     * 
     * @return true if portlet uses newsletter
     * @param nIdNewsletter
     *            the template identifier
     */
    public static boolean checkLinkedPortlets( int nIdNewsletter )
    {
        return _dao.checkLinkedPortlet( nIdNewsletter );
    }

}
