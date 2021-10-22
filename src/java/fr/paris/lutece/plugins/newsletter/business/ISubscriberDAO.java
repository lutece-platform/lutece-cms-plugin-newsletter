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

import java.util.Collection;

/**
 * The interface representing the business methods of the subscriber
 */
public interface ISubscriberDAO
{
    /**
     * Insert a new record in the table.
     *
     * @param subscriber
     *            the object to be inserted
     * @param plugin
     *            the Plugin
     */
    void insert( Subscriber subscriber, Plugin plugin );

    /**
     * Delete a record from the table
     *
     * @param nId
     *            the subscriber's identifier
     * @param plugin
     *            the Plugin
     */
    void delete( int nId, Plugin plugin );

    /**
     * loads data from a subscriber's identifier
     *
     * @param nId
     *            the subscriber's identifier
     * @param plugin
     *            the Plugin
     * @return an object Subscriber
     */
    Subscriber load( int nId, Plugin plugin );

    /**
     * Loads the list of subscribers
     *
     * @param plugin
     *            the Plugin
     * @return a collection of objects Subscriber
     */
    Collection<Subscriber> selectAll( Plugin plugin );

    /**
     * Finds a subscriber from his email
     *
     * @param strEmail
     *            the subscriber's email
     * @param plugin
     *            the Plugin
     * @return a subscriber object if it exists, null if not
     */
    Subscriber selectByEmail( String strEmail, Plugin plugin );

    /**
     * loads the list of subscribers for a newsletter
     *
     * @param nNewsLetterId
     *            the newsletter identifier
     * @param plugin
     *            the Plugin
     * @return a collection of subscribers
     */
    Collection<Subscriber> selectSubscribers( int nNewsLetterId, Plugin plugin );

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
     *            the Plugin
     * @return a collection of subscribers
     */
    Collection<Subscriber> selectSubscribers( int nNewsLetterId, String strSearchString, int nBegin, int nEnd, Plugin plugin );

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
     *            the Plugin
     * @return a collection of subscribers
     */
    Collection<Subscriber> selectSubscribersForSending( int nNewsLetterId, String strSearchString, int nBegin, int nEnd, Plugin plugin );

    /**
     * Returns, for a subscriber, the number of his subscriptions
     *
     * @param nSubscriberId
     *            the subscriber's identifier
     * @param plugin
     *            the Plugin
     * @return the number of subscriptions
     */
    int selectNewsLetters( int nSubscriberId, Plugin plugin );

    /**
     * loads the list of subscribers
     *
     * @param plugin
     *            the Plugin
     * @return a collection of subscribers
     */
    Collection<Subscriber> selectSubscribersList( Plugin plugin );
}
