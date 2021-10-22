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
import java.util.List;

/**
 * This class provides Data Access methods for SendingNewsLetter objects
 */
public final class SendingNewsLetterDAO implements ISendingNewsLetterDAO
{
    // Constants
    private static final String SQL_QUERY_INSERT = "INSERT INTO newsletter_sending ( id_sending, id_newsletter , date_sending, subscriber_count, html, email_subject ) VALUES ( ?, ?, ?, ?, ?, ? )";
    private static final String SQL_QUERY_DELETE = "DELETE FROM newsletter_sending WHERE id_sending = ? ";
    private static final String SQL_QUERY_SELECT = "SELECT id_newsletter, date_sending, subscriber_count, html, email_subject FROM newsletter_sending WHERE id_sending = ? ";
    private static final String SQL_QUERY_SELECT_LAST_SENDIND_BY_NEWSLETTER = " SELECT id_sending, id_newsletter, date_sending, subscriber_count, html, email_subject FROM newsletter_sending WHERE id_newsletter = ? ORDER BY date_sending DESC";
    private static final String SQL_QUERY_SELECT_ALL_SENDINDS = "SELECT id_sending, id_newsletter, date_sending, subscriber_count, html, email_subject FROM newsletter_sending ORDER BY date_sending DESC";
    private static final String SQL_QUERY_UPDATE = "UPDATE newsletter_sending SET date_sending = ? WHERE id_sending = ?";
    private static final String SQL_QUERY_CHECK_PRIMARY_KEY = "SELECT id_sending FROM newsletter_sending WHERE id_sending = ?";
    private static final String SQL_QUERY_NEW_PRIMARY_KEY = "SELECT max(id_sending) FROM newsletter_sending ";

    ///////////////////////////////////////////////////////////////////////////////////////
    // Access methods to data

    /**
     * Insert a new record in the table.
     * 
     * @param sending
     *            the object to be inserted
     * @param plugin
     *            the Plugin
     */
    public void insert( SendingNewsLetter sending, Plugin plugin )
    {
        int nNewPrimaryKey = newPrimaryKey( plugin );
        sending.setId( nNewPrimaryKey );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );

        daoUtil.setInt( 1, sending.getId( ) );
        daoUtil.setInt( 2, sending.getNewsLetterId( ) );
        daoUtil.setTimestamp( 3, sending.getDate( ) );
        daoUtil.setInt( 4, sending.getCountSubscribers( ) );
        daoUtil.setString( 5, sending.getHtml( ) );
        daoUtil.setString( 6, sending.getEmailSubject( ) );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * Delete the record in the database
     * 
     * @param nSendingId
     *            the sending Identifier
     * @param plugin
     *            the Plugin
     */
    public void delete( int nSendingId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin );
        daoUtil.setInt( 1, nSendingId );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * Loads the data from the database
     * 
     * @param nSendingId
     *            the sending identifier
     * @param plugin
     *            the Plugin
     * @return an object SendingNewsLetter
     */
    public SendingNewsLetter load( int nSendingId, Plugin plugin )
    {
        SendingNewsLetter sending = null;

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin );

        daoUtil.setInt( 1, nSendingId );
        daoUtil.executeQuery( );

        if ( daoUtil.next( ) )
        {
            sending = new SendingNewsLetter( );
            sending.setId( nSendingId );
            sending.setNewsLetterId( daoUtil.getInt( 1 ) );
            sending.setDate( daoUtil.getTimestamp( 2 ) );
            sending.setCountSubscribers( daoUtil.getInt( 3 ) );
            sending.setHtml( daoUtil.getString( 4 ) );
            sending.setEmailSubject( daoUtil.getString( 5 ) );
        }

        daoUtil.free( );

        return sending;
    }

    /**
     * Update the record in the table
     * 
     * @param sending
     *            an instance of the class SendingNewsLetter
     * @param plugin
     *            the Plugin
     */
    public void store( SendingNewsLetter sending, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin );
        daoUtil.setTimestamp( 1, sending.getDate( ) );
        daoUtil.setInt( 2, sending.getId( ) );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * Check the unicity of a primary key
     * 
     * @param nKey
     *            the identifier to check
     * @param plugin
     *            the Plugin
     * @return true if the identifier exist and false if not
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
     * Generate a new primary key to add a new sending
     * 
     * @param plugin
     *            the Plugin
     * @return the primary key
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
     * Returns the last sending performed for the newsletter of given id
     * 
     * @param newsletterId
     *            the newsletter id for wich we need the last sending
     * @param plugin
     *            the plugin
     * @return the last sending for the given newsletter id - null if no sending found
     */
    public SendingNewsLetter selectLastSendingForNewsletterId( int newsletterId, Plugin plugin )
    {
        SendingNewsLetter sending = null;

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_LAST_SENDIND_BY_NEWSLETTER, plugin );
        daoUtil.setInt( 1, newsletterId );
        daoUtil.executeQuery( );

        if ( daoUtil.next( ) )
        {
            sending = new SendingNewsLetter( );
            sending.setId( daoUtil.getInt( 1 ) );
            sending.setNewsLetterId( daoUtil.getInt( 2 ) );
            sending.setDate( daoUtil.getTimestamp( 3 ) );
            sending.setCountSubscribers( daoUtil.getInt( 4 ) );
            sending.setHtml( daoUtil.getString( 5 ) );
            sending.setEmailSubject( daoUtil.getString( 6 ) );
        }

        daoUtil.free( );

        return sending;
    }

    /**
     * Returns all the last sending performed for the newsletter of given id
     * 
     * @param newsletterId
     *            the newsletter id for wich we need the last sending
     * @param plugin
     *            the plugin
     * @return the last sending for the given newsletter id - null if no sending found
     */
    public List<SendingNewsLetter> selectAllLastSendingForNewsletterId( int newsletterId, Plugin plugin )
    {
        SendingNewsLetter sending = null;
        List<SendingNewsLetter> results = new ArrayList<SendingNewsLetter>( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_LAST_SENDIND_BY_NEWSLETTER, plugin );
        daoUtil.setInt( 1, newsletterId );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            sending = new SendingNewsLetter( );
            sending.setId( daoUtil.getInt( 1 ) );
            sending.setNewsLetterId( daoUtil.getInt( 2 ) );
            sending.setDate( daoUtil.getTimestamp( 3 ) );
            sending.setCountSubscribers( daoUtil.getInt( 4 ) );
            sending.setHtml( daoUtil.getString( 5 ) );
            sending.setEmailSubject( daoUtil.getString( 6 ) );

            results.add( sending );
        }

        daoUtil.free( );

        return results;
    }

    /**
     * Returns all the sendings in the database.
     * 
     * @param plugin
     *            the plugin
     * @return a list of SendingNewsLetter objects.
     */
    public List<SendingNewsLetter> findAllSendings( Plugin plugin )
    {
        List<SendingNewsLetter> results = new ArrayList<SendingNewsLetter>( );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL_SENDINDS, plugin );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            SendingNewsLetter sending = new SendingNewsLetter( );
            sending.setId( daoUtil.getInt( 1 ) );
            sending.setNewsLetterId( daoUtil.getInt( 2 ) );
            sending.setDate( daoUtil.getTimestamp( 3 ) );
            sending.setCountSubscribers( daoUtil.getInt( 4 ) );
            sending.setHtml( daoUtil.getString( 5 ) );
            sending.setEmailSubject( daoUtil.getString( 6 ) );

            results.add( sending );
        }

        daoUtil.free( );

        return results;
    }

    /**
     * Returns all the sendings in the database.
     * 
     * @return a list of SendingNewsLetter objects.
     * @param listId
     *            A list of newsletter id
     * @param plugin
     *            the plugin
     */
    public ArrayList<SendingNewsLetter> findSendingsByIds( ArrayList<Integer> listId, Plugin plugin )
    {
        ArrayList<SendingNewsLetter> results = new ArrayList<SendingNewsLetter>( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL_SENDINDS, plugin );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            if ( listId.contains( Integer.valueOf( daoUtil.getInt( 1 ) ) ) )
            {
                SendingNewsLetter sending = new SendingNewsLetter( );
                sending.setId( daoUtil.getInt( 1 ) );
                sending.setNewsLetterId( daoUtil.getInt( 2 ) );
                sending.setDate( daoUtil.getTimestamp( 3 ) );
                sending.setCountSubscribers( daoUtil.getInt( 4 ) );
                sending.setHtml( daoUtil.getString( 5 ) );
                sending.setEmailSubject( daoUtil.getString( 6 ) );

                results.add( sending );
            }
        }

        daoUtil.free( );

        return results;
    }
}
