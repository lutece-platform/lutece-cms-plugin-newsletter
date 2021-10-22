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

import java.util.Collection;

/**
 * This class provides instacnes management methods (create, find, ...) for Subscriber objects
 */
public final class SubscriberHome
{
    // Static variable pointed at the DAO instance
    private static ISubscriberDAO _dao = SpringContextService.getBean( "newsletter.subscriberDAO" );

    /**
     * Private constructor - this class need not be instantiated
     */
    private SubscriberHome( )
    {
    }

    /**
     * Create an instance of the Subscriber class
     *
     * @param subscriber
     *            the object to insert into the database
     * @param plugin
     *            the plugin
     * @return the instance created
     */
    public static Subscriber create( Subscriber subscriber, Plugin plugin )
    {
        _dao.insert( subscriber, plugin );

        return subscriber;
    }

    /**
     * Remove the subscriber whose identifier is specified in parameter
     *
     * @param nSubscriberId
     *            the subscriber's identifier
     * @param plugin
     *            the plugin
     */
    public static void remove( int nSubscriberId, Plugin plugin )
    {
        _dao.delete( nSubscriberId, plugin );
    }

    ///////////////////////////////////////////////////////////////////////////
    // Finders

    /**
     * Returns an object Subscriber from its identifier
     *
     * @param nKey
     *            the primary key of the subscriber
     * @param plugin
     *            the plugin
     * @return an instance of the class
     */
    public static Subscriber findByPrimaryKey( int nKey, Plugin plugin )
    {
        return _dao.load( nKey, plugin );
    }

    /**
     * @param plugin
     *            the plugin
     * @return a collection of objects Subscriber
     */
    public static Collection<Subscriber> findAll( Plugin plugin )
    {
        return _dao.selectAll( plugin );
    }

    /**
     * Returns a subscriber object from the email
     *
     * @param strEmail
     *            the subscriber's email
     * @param plugin
     *            the plugin
     * @return a subscriber object if it exists, null if not
     */
    public static Subscriber findByEmail( String strEmail, Plugin plugin )
    {
        return _dao.selectByEmail( strEmail, plugin );
    }

    /**
     * loads the list of subscribers for a newsletter
     *
     * @param nNewsLetterId
     *            the newsletter identifier
     * @param nBegin
     *            the rank of the first subscriber to return
     * @param nEnd
     *            the maximum number of suscribers to return
     * @param plugin
     *            the plugin
     * @return a collection of subscribers
     */
    public static Collection<Subscriber> findSubscribers( int nNewsLetterId, int nBegin, int nEnd, Plugin plugin )
    {
        return _dao.selectSubscribers( nNewsLetterId, NewsLetterConstants.CONSTANT_EMPTY_STRING, nBegin, nEnd, plugin );
    }

    /**
     * Find the collection of subscribers to send a newsletter
     * 
     * @param nNewsLetterId
     *            The id of the newsletter
     * @param nBegin
     *            The begin index of subscribers to get
     * @param nEnd
     *            The end index of subscribers to get
     * @param plugin
     *            The plugin
     * @return The collection of subscribers found
     */
    public static Collection<Subscriber> findSubscribersForSending( int nNewsLetterId, int nBegin, int nEnd, Plugin plugin )
    {
        return _dao.selectSubscribersForSending( nNewsLetterId, NewsLetterConstants.CONSTANT_EMPTY_STRING, nBegin, nEnd, plugin );
    }

    /**
     * loads the list of subscribers for a newsletter
     *
     * @param nNewsLetterId
     *            the newsletter identifier
     * @param strSearchString
     *            gets all the subscribers if null or empty and gets the subscribers whith an email containing this string otherwise
     * @param nBegin
     *            the rank of the first subscriber to return
     * @param nEnd
     *            the maximum number of suscribers to return
     * @param plugin
     *            the plugin
     * @return a collection of subscribers
     */
    public static Collection<Subscriber> findSubscribers( int nNewsLetterId, String strSearchString, int nBegin, int nEnd, Plugin plugin )
    {
        return _dao.selectSubscribers( nNewsLetterId, strSearchString, nBegin, nEnd, plugin );
    }

    /**
     * loads the list of subscribers for a newsletter
     *
     * @param nNewsLetterId
     *            the newsletter identifier
     * @param plugin
     *            the plugin
     * @return a collection of subscribers
     */
    public static Collection<Subscriber> findSubscribers( int nNewsLetterId, Plugin plugin )
    {
        return _dao.selectSubscribers( nNewsLetterId, plugin );
    }

    /**
     * Returns, for a subscriber, the number of his subscriptions
     *
     * @param nSubscriberId
     *            the subscriber's identifier
     * @param plugin
     *            the plugin
     * @return the number of subscriptions
     */
    public static int findNewsLetters( int nSubscriberId, Plugin plugin )
    {
        return _dao.selectNewsLetters( nSubscriberId, plugin );
    }

    /**
     * loads the list of subscribers
     * 
     * @param plugin
     *            the plugin
     * @return a collection of subscribers
     */
    public static Collection<Subscriber> getSubscribersList( Plugin plugin )
    {
        return _dao.selectSubscribersList( plugin );
    }
}
