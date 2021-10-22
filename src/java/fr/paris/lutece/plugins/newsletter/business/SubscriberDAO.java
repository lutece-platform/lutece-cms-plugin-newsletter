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
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class provides Data Access methods for subscriber objects
 */
public final class SubscriberDAO implements ISubscriberDAO
{
    // Constants
    private static final String SQL_QUERY_INSERT = "INSERT INTO newsletter_subscriber_details ( id_subscriber , email ) VALUES ( ?, ? )";
    private static final String SQL_QUERY_DELETE = "DELETE FROM newsletter_subscriber_details WHERE id_subscriber = ? ";
    private static final String SQL_QUERY_SELECT = "SELECT email FROM newsletter_subscriber_details WHERE id_subscriber = ? ";
    private static final String SQL_QUERY_SELECT_ALL = "SELECT id_subscriber, email FROM newsletter_subscriber_details ";
    private static final String SQL_QUERY_SELECT_SUBSCRIBERS_LIST = "SELECT id_subscriber , email FROM newsletter_subscriber_details ";
    private static final String SQL_QUERY_SELECT_BY_EMAIL = "SELECT id_subscriber , email FROM newsletter_subscriber_details WHERE email = ? ";
    private static final String SQL_QUERY_SELECT_SUBSCRIBERS_BY_NEWSLETTER = "SELECT a.id_subscriber , a.email, b.confirmed, b.date_subscription FROM newsletter_subscriber_details a, newsletter_subscriber b WHERE a.id_subscriber = b.id_subscriber AND b.id_newsletter = ? AND b.confirmed = 1 ";
    private static final String SQL_QUERY_SELECT_SUBSCRIBERS_FOR_SENDING = " SELECT a.id_subscriber , a.email, b.confirmed, b.date_subscription FROM newsletter_subscriber_details a, newsletter_subscriber b WHERE a.id_subscriber = b.id_subscriber AND b.id_newsletter = ? AND a.email LIKE ? AND confirmed = 1 ORDER BY a.email LIMIT ? OFFSET ? ";
    private static final String SQL_QUERY_SELECT_SUBSCRIBERS = " SELECT a.id_subscriber , a.email, b.confirmed, b.date_subscription FROM newsletter_subscriber_details a, newsletter_subscriber b WHERE a.id_subscriber = b.id_subscriber AND b.id_newsletter = ? AND a.email LIKE ? ORDER BY a.email LIMIT ? OFFSET ? ";
    private static final String SQL_QUERY_COUNT_NEWSLETTERS_BY_SUBSCRIBER = "SELECT count(*) FROM newsletter_subscriber where id_subscriber = ? ";
    private static final String SQL_QUERY_CHECK_PRIMARY_KEY = "SELECT id_subscriber FROM newsletter_subscriber_details WHERE id_subscriber = ?";
    private static final String SQL_QUERY_NEW_PRIMARY_KEY = "SELECT max(id_subscriber) FROM newsletter_subscriber_details ";

    ///////////////////////////////////////////////////////////////////////////////////////
    // Access methods to data

    /**
     * Insert a new record in the table.
     *
     * @param subscriber
     *            the object to be inserted
     * @param plugin
     *            the Plugin
     */
    public void insert( Subscriber subscriber, Plugin plugin )
    {
        int nNewPrimaryKey = newPrimaryKey( plugin );
        subscriber.setId( nNewPrimaryKey );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );

        daoUtil.setInt( 1, subscriber.getId( ) );
        daoUtil.setString( 2, subscriber.getEmail( ) );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * Delete a record from the table
     *
     * @param nId
     *            the subscriber's identifier
     * @param plugin
     *            the Plugin
     */
    public void delete( int nId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin );
        daoUtil.setInt( 1, nId );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * loads data from a subscriber's identifier
     *
     * @param nId
     *            the subscriber's identifier
     * @param plugin
     *            the Plugin
     * @return an object Subscriber
     */
    public Subscriber load( int nId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin );
        daoUtil.setInt( 1, nId );
        daoUtil.executeQuery( );

        Subscriber subscriber = new Subscriber( );

        if ( daoUtil.next( ) )
        {
            subscriber.setId( nId );
            subscriber.setEmail( daoUtil.getString( 1 ) );
        }

        daoUtil.free( );

        return subscriber;
    }

    /**
     * Check the unicity of a primary key
     *
     * @param nKey
     *            the primary key to be checked
     * @param plugin
     *            the Plugin
     * @return true if the key exists, false if not
     */
    boolean checkPrimaryKey( int nKey, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_CHECK_PRIMARY_KEY, plugin );
        daoUtil.setInt( 1, nKey );
        daoUtil.executeQuery( );

        if ( !daoUtil.next( ) )
        {
            daoUtil.free( );

            return false;
        }

        daoUtil.free( );

        return true;
    }

    /**
     * Generates a new primary key
     *
     * @param plugin
     *            the Plugin
     * @return the new primary key
     */
    int newPrimaryKey( Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_NEW_PRIMARY_KEY, plugin );

        int nKey;

        daoUtil.executeQuery( );

        if ( !daoUtil.next( ) )
        {
            // If the table is empty
            nKey = 1;
        }

        nKey = daoUtil.getInt( 1 ) + 1;

        daoUtil.free( );

        return nKey;
    }

    /**
     * Loads the list of subscribers
     *
     * @param plugin
     *            the Plugin
     * @return a collection of objects Subscriber
     */
    public Collection<Subscriber> selectAll( Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL, plugin );
        daoUtil.executeQuery( );

        ArrayList<Subscriber> list = new ArrayList<Subscriber>( );

        while ( daoUtil.next( ) )
        {
            Subscriber subscriber = new Subscriber( );
            subscriber.setId( daoUtil.getInt( 1 ) );
            subscriber.setEmail( daoUtil.getString( 2 ) );
            subscriber.setConfirmed( daoUtil.getBoolean( 3 ) );
            list.add( subscriber );
        }

        daoUtil.free( );

        return list;
    }

    /**
     * Finds a subscriber from his email
     *
     * @param strEmail
     *            the subscriber's email
     * @param plugin
     *            the Plugin
     * @return a subscriber object if it exists, null if not
     */
    public Subscriber selectByEmail( String strEmail, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_EMAIL, plugin );
        daoUtil.setString( 1, strEmail.toLowerCase( ) );
        daoUtil.executeQuery( );

        Subscriber subscriber = null;

        if ( daoUtil.next( ) )
        {
            subscriber = new Subscriber( );
            subscriber.setId( daoUtil.getInt( 1 ) );
            subscriber.setEmail( daoUtil.getString( 2 ) );
        }

        daoUtil.free( );

        return subscriber;
    }

    /**
     * loads the list of subscribers for a newsletter
     *
     * @param nNewsLetterId
     *            the newsletter identifier
     * @param plugin
     *            the Plugin
     * @return a collection of subscribers
     */
    public Collection<Subscriber> selectSubscribers( int nNewsLetterId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_SUBSCRIBERS_BY_NEWSLETTER, plugin );
        daoUtil.setInt( 1, nNewsLetterId );
        daoUtil.executeQuery( );

        ArrayList<Subscriber> list = new ArrayList<Subscriber>( );

        while ( daoUtil.next( ) )
        {
            Subscriber subscriber = new Subscriber( );
            subscriber.setId( daoUtil.getInt( 1 ) );
            subscriber.setEmail( daoUtil.getString( 2 ) );
            subscriber.setConfirmed( daoUtil.getBoolean( 3 ) );
            subscriber.setDateSubscription( daoUtil.getTimestamp( 4 ) );
            list.add( subscriber );
        }

        daoUtil.free( );

        return list;
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
     *            the Plugin
     * @return a collection of subscribers
     */
    public Collection<Subscriber> selectSubscribers( int nNewsLetterId, String strSearchString, int nBegin, int nEnd, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_SUBSCRIBERS, plugin );

        daoUtil.setInt( 1, nNewsLetterId );
        daoUtil.setString( 2, "%" + strSearchString + "%" );
        daoUtil.setInt( 3, nEnd );
        daoUtil.setInt( 4, nBegin );

        daoUtil.executeQuery( );

        ArrayList<Subscriber> list = new ArrayList<Subscriber>( );

        while ( daoUtil.next( ) )
        {
            Subscriber subscriber = new Subscriber( );
            subscriber.setId( daoUtil.getInt( 1 ) );
            subscriber.setEmail( daoUtil.getString( 2 ) );
            subscriber.setConfirmed( daoUtil.getBoolean( 3 ) );
            subscriber.setDateSubscription( daoUtil.getTimestamp( 4 ) );

            list.add( subscriber );
        }

        daoUtil.free( );

        return list;
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
     *            the Plugin
     * @return a collection of subscribers
     */
    public Collection<Subscriber> selectSubscribersForSending( int nNewsLetterId, String strSearchString, int nBegin, int nEnd, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_SUBSCRIBERS_FOR_SENDING, plugin );

        daoUtil.setInt( 1, nNewsLetterId );
        daoUtil.setString( 2, "%" + strSearchString + "%" );
        daoUtil.setInt( 3, nEnd );
        daoUtil.setInt( 4, nBegin );

        daoUtil.executeQuery( );

        ArrayList<Subscriber> list = new ArrayList<Subscriber>( );

        while ( daoUtil.next( ) )
        {
            Subscriber subscriber = new Subscriber( );
            subscriber.setId( daoUtil.getInt( 1 ) );
            subscriber.setEmail( daoUtil.getString( 2 ) );
            subscriber.setConfirmed( daoUtil.getBoolean( 3 ) );
            subscriber.setDateSubscription( daoUtil.getTimestamp( 4 ) );

            list.add( subscriber );
        }

        daoUtil.free( );

        return list;
    }

    /**
     * Returns, for a subscriber, the number of his subscriptions
     *
     * @param nSubscriberId
     *            the subscriber's identifier
     * @param plugin
     *            the Plugin
     * @return the number of subscriptions
     */
    public int selectNewsLetters( int nSubscriberId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_COUNT_NEWSLETTERS_BY_SUBSCRIBER, plugin );

        int nCount;

        daoUtil.setInt( 1, nSubscriberId );

        daoUtil.executeQuery( );

        if ( !daoUtil.next( ) )
        {
            // If the table is empty
            nCount = 0;
        }

        nCount = daoUtil.getInt( 1 );

        daoUtil.free( );

        return nCount;
    }

    /**
     * loads the list of subscribers
     *
     * @param plugin
     *            the Plugin
     * @return a collection of subscribers
     */
    public Collection<Subscriber> selectSubscribersList( Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_SUBSCRIBERS_LIST, plugin );

        daoUtil.executeQuery( );

        ArrayList<Subscriber> list = new ArrayList<Subscriber>( );

        while ( daoUtil.next( ) )
        {
            Subscriber subscriber = new Subscriber( );
            subscriber.setId( daoUtil.getInt( 1 ) );
            subscriber.setEmail( daoUtil.getString( 2 ) );
            list.add( subscriber );
        }

        daoUtil.free( );

        return list;
    }
}
