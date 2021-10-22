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

import fr.paris.lutece.plugins.newsletter.service.NewsletterPlugin;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.sql.DAOUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This class provides Data Access methods for NewsLetter objects
 */
public final class NewsLetterDAO implements INewsLetterDAO
{
    // Constants
    private static final String SQL_QUERY_SELECT = "SELECT name, description, date_last_send, html, id_newsletter_template, workgroup_key, unsubscribe, sender_mail, sender_name, test_recipients, test_subject, nb_sections  FROM newsletter_description WHERE id_newsletter = ? ";
    private static final String SQL_QUERY_SELECT_ALL = "SELECT id_newsletter , name, description, date_last_send, html, id_newsletter_template, workgroup_key, test_recipients , sender_mail, sender_name, test_subject, nb_sections FROM newsletter_description ";
    private static final String SQL_QUERY_SELECT_ALL_ID = "SELECT id_newsletter, name FROM newsletter_description ";
    private static final String SQL_QUERY_SELECT_ALL_BY_ID_TEMPLATE = "SELECT id_newsletter , name, description, date_last_send, html, id_newsletter_template, workgroup_key, test_recipients , sender_mail, sender_name, test_subject, nb_sections FROM newsletter_description WHERE id_newsletter_template = ? ";
    private static final String SQL_QUERY_SELECT_NBR_SUBSCRIBERS = "SELECT count(*) FROM newsletter_subscriber a, newsletter_subscriber_details b WHERE a.id_subscriber = b.id_subscriber AND b.email LIKE ? AND id_newsletter = ? ";
    private static final String SQL_QUERY_SELECT_NBR_ACTIVE_SUBSCRIBERS = "SELECT count(*) FROM newsletter_subscriber a, newsletter_subscriber_details b WHERE a.id_subscriber = b.id_subscriber AND b.email LIKE ? AND id_newsletter = ? AND a.confirmed = 1";
    private static final String SQL_QUERY_UPDATE = "UPDATE newsletter_description SET name = ?, description = ?, date_last_send = ?, html = ?, id_newsletter_template = ?, workgroup_key = ? , unsubscribe = ? ,sender_mail = ? ,sender_name = ? , test_recipients = ?, test_subject = ?, nb_sections = ? WHERE id_newsletter = ? ";
    private static final String SQL_QUERY_INSERT = "INSERT INTO newsletter_description ( id_newsletter , name, description, date_last_send, html, id_newsletter_template, workgroup_key, unsubscribe, sender_mail, sender_name, test_recipients , test_subject, nb_sections ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ? , ?, ?, ?, ?, ? )";
    private static final String SQL_QUERY_INSERT_SUBSCRIBER = "INSERT INTO newsletter_subscriber ( id_newsletter , id_subscriber, date_subscription, confirmed ) VALUES ( ?, ?, ?, ? )";
    private static final String SQL_QUERY_VALIDATE_SUBSCRIBER = "UPDATE newsletter_subscriber SET confirmed = 1 WHERE id_newsletter = ? AND id_subscriber = ?";
    private static final String SQL_QUERY_DELETE = "DELETE FROM newsletter_description WHERE id_newsletter = ? ";
    private static final String SQL_QUERY_DELETE_FROM_SUBSCRIBER = "DELETE FROM newsletter_subscriber WHERE id_newsletter = ? and id_subscriber = ? ";
    private static final String SQL_QUERY_DELETE_OLD_FROM_SUBSCRIBER = "DELETE FROM newsletter_subscriber WHERE date_subscription < ? and confirmed = ? ";
    private static final String SQL_QUERY_CHECK_PRIMARY_KEY = "SELECT id_newsletter FROM newsletter_description WHERE id_newsletter = ?";
    private static final String SQL_QUERY_CHECK_LINKED_PORTLET = "SELECT id_newsletter FROM  newsletter_portlet_subscribe WHERE id_newsletter = ?";
    private static final String SQL_QUERY_NEW_PRIMARY_KEY = "SELECT max(id_newsletter) FROM newsletter_description ";
    private static final String SQL_QUERY_CHECK_IS_REGISTERED = "SELECT id_newsletter FROM newsletter_subscriber WHERE id_newsletter = ? AND id_subscriber = ? ";
    private static final String SQL_QUERY_CHECK_IS_TEMPLATE_USED = "SELECT id_newsletter FROM newsletter_description WHERE id_newsletter_template = ? ";
    private static final String SQL_QUERY_DELETE_UNUSED_EMAIL = "DELETE FROM newsletter_subscriber_details WHERE id_subscriber NOT IN (SELECT id_subscriber FROM newsletter_subscriber)";

    private static final String CONSTANT_PERCENT = "%";

    ///////////////////////////////////////////////////////////////////////////////////////
    // Access methods to data

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert( NewsLetter newsLetter, Plugin plugin )
    {
        int nNewPrimaryKey = newPrimaryKey( plugin );
        newsLetter.setId( nNewPrimaryKey );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );

        int nIndex = 1;
        daoUtil.setInt( nIndex++, newsLetter.getId( ) );
        daoUtil.setString( nIndex++, newsLetter.getName( ) );
        daoUtil.setString( nIndex++, newsLetter.getDescription( ) );
        daoUtil.setTimestamp( nIndex++, newsLetter.getDateLastSending( ) );
        daoUtil.setString( nIndex++, newsLetter.getHtml( ) );
        daoUtil.setInt( nIndex++, newsLetter.getNewsLetterTemplateId( ) );
        daoUtil.setString( nIndex++, newsLetter.getWorkgroup( ) );
        daoUtil.setString( nIndex++, newsLetter.getUnsubscribe( ) );
        daoUtil.setString( nIndex++, newsLetter.getNewsletterSenderMail( ) );
        daoUtil.setString( nIndex++, newsLetter.getNewsletterSenderName( ) );
        daoUtil.setString( nIndex++, newsLetter.getTestRecipients( ) );
        daoUtil.setString( nIndex++, newsLetter.getTestSubject( ) );
        daoUtil.setInt( nIndex, newsLetter.getNbSections( ) );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete( int nNewsLetterId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin );
        daoUtil.setInt( 1, nNewsLetterId );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NewsLetter load( int nNewsLetterId, Plugin plugin )
    {
        NewsLetter newsLetter = new NewsLetter( );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin );
        daoUtil.setInt( 1, nNewsLetterId );
        daoUtil.executeQuery( );

        if ( daoUtil.next( ) )
        {
            int nIndex = 1;
            newsLetter.setId( nNewsLetterId );
            newsLetter.setName( daoUtil.getString( nIndex++ ) );
            newsLetter.setDescription( daoUtil.getString( nIndex++ ) );
            newsLetter.setDateLastSending( daoUtil.getTimestamp( nIndex++ ) );
            newsLetter.setHtml( daoUtil.getString( nIndex++ ) );
            newsLetter.setNewsLetterTemplateId( daoUtil.getInt( nIndex++ ) );
            newsLetter.setWorkgroup( daoUtil.getString( nIndex++ ) );
            newsLetter.setUnsubscribe( daoUtil.getString( nIndex++ ) );
            newsLetter.setNewsletterSenderMail( daoUtil.getString( nIndex++ ) );
            newsLetter.setNewsletterSenderName( daoUtil.getString( nIndex++ ) );
            newsLetter.setTestRecipients( daoUtil.getString( nIndex++ ) );
            newsLetter.setTestSubject( daoUtil.getString( nIndex++ ) );
            newsLetter.setNbSections( daoUtil.getInt( nIndex ) );
        }

        daoUtil.free( );

        return newsLetter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store( NewsLetter newsLetter, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin );
        int nIndex = 1;
        daoUtil.setString( nIndex++, newsLetter.getName( ) );
        daoUtil.setString( nIndex++, newsLetter.getDescription( ) );
        daoUtil.setTimestamp( nIndex++, newsLetter.getDateLastSending( ) );
        daoUtil.setString( nIndex++, newsLetter.getHtml( ) );
        daoUtil.setInt( nIndex++, newsLetter.getNewsLetterTemplateId( ) );
        daoUtil.setString( nIndex++, newsLetter.getWorkgroup( ) );
        daoUtil.setString( nIndex++, newsLetter.getUnsubscribe( ) );
        daoUtil.setString( nIndex++, newsLetter.getNewsletterSenderMail( ) );
        daoUtil.setString( nIndex++, newsLetter.getNewsletterSenderName( ) );
        daoUtil.setString( nIndex++, newsLetter.getTestRecipients( ) );
        daoUtil.setString( nIndex++, newsLetter.getTestSubject( ) );
        daoUtil.setInt( nIndex++, newsLetter.getNbSections( ) );
        daoUtil.setInt( nIndex, newsLetter.getId( ) );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkPrimaryKey( int nKey, Plugin plugin )
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
     * {@inheritDoc}
     */
    @Override
    public boolean checkLinkedPortlet( int nIdNewsletter )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_CHECK_LINKED_PORTLET, PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME ) );
        daoUtil.setInt( 1, nIdNewsletter );
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
     * {@inheritDoc}
     */
    @Override
    public int newPrimaryKey( Plugin plugin )
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
     * {@inheritDoc}
     */
    @Override
    public Collection<NewsLetter> selectAll( Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL, plugin );
        daoUtil.executeQuery( );

        ArrayList<NewsLetter> list = new ArrayList<NewsLetter>( );

        while ( daoUtil.next( ) )
        {
            int nIndex = 1;
            NewsLetter newsLetter = new NewsLetter( );
            newsLetter.setId( daoUtil.getInt( nIndex++ ) );
            newsLetter.setName( daoUtil.getString( nIndex++ ) );
            newsLetter.setDescription( daoUtil.getString( nIndex++ ) );
            newsLetter.setDateLastSending( daoUtil.getTimestamp( nIndex++ ) );
            newsLetter.setHtml( daoUtil.getString( nIndex++ ) );
            newsLetter.setNewsLetterTemplateId( daoUtil.getInt( nIndex++ ) );
            newsLetter.setWorkgroup( daoUtil.getString( nIndex++ ) );
            newsLetter.setTestRecipients( daoUtil.getString( nIndex++ ) );
            newsLetter.setNewsletterSenderMail( daoUtil.getString( nIndex++ ) );
            newsLetter.setNewsletterSenderName( daoUtil.getString( nIndex++ ) );
            newsLetter.setTestSubject( daoUtil.getString( nIndex++ ) );
            newsLetter.setNbSections( daoUtil.getInt( nIndex ) );
            list.add( newsLetter );
        }

        daoUtil.free( );

        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReferenceList selectAllId( Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL_ID, plugin );
        daoUtil.executeQuery( );

        ReferenceList list = new ReferenceList( );

        while ( daoUtil.next( ) )
        {
            list.addItem( daoUtil.getInt( 1 ), daoUtil.getString( 2 ) );
        }

        daoUtil.free( );

        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<NewsLetter> selectAllByTemplateId( int nTemplateId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL_BY_ID_TEMPLATE, plugin );
        daoUtil.setInt( 1, nTemplateId );
        daoUtil.executeQuery( );

        ArrayList<NewsLetter> list = new ArrayList<NewsLetter>( );

        while ( daoUtil.next( ) )
        {
            int nIndex = 1;
            NewsLetter newsLetter = new NewsLetter( );
            newsLetter.setId( daoUtil.getInt( nIndex++ ) );
            newsLetter.setName( daoUtil.getString( nIndex++ ) );
            newsLetter.setDescription( daoUtil.getString( nIndex++ ) );
            newsLetter.setDateLastSending( daoUtil.getTimestamp( nIndex++ ) );
            newsLetter.setHtml( daoUtil.getString( nIndex++ ) );
            newsLetter.setNewsLetterTemplateId( daoUtil.getInt( nIndex++ ) );
            newsLetter.setWorkgroup( daoUtil.getString( nIndex++ ) );
            newsLetter.setTestRecipients( daoUtil.getString( nIndex++ ) );
            newsLetter.setNewsletterSenderMail( daoUtil.getString( nIndex++ ) );
            newsLetter.setNewsletterSenderName( daoUtil.getString( nIndex++ ) );
            newsLetter.setTestSubject( daoUtil.getString( nIndex++ ) );
            newsLetter.setNbSections( daoUtil.getInt( nIndex ) );
            list.add( newsLetter );
        }

        daoUtil.free( );

        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertSubscriber( int nNewsLetterId, int nSubscriberId, Timestamp tToday, Plugin plugin )
    {
        insertSubscriber( nNewsLetterId, nSubscriberId, true, tToday, plugin );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertSubscriber( int nNewsLetterId, int nSubscriberId, boolean bValidate, Timestamp tToday, Plugin plugin )
    {
        // Check if the subscriber is yet registered for the newsletter
        if ( isRegistered( nNewsLetterId, nSubscriberId, plugin ) )
        {
            return;
        }

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_SUBSCRIBER, plugin );

        daoUtil.setInt( 1, nNewsLetterId );
        daoUtil.setInt( 2, nSubscriberId );
        daoUtil.setTimestamp( 3, tToday );
        daoUtil.setBoolean( 4, bValidate );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteSubscriber( int nNewsLetterId, int nSubscriberId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_FROM_SUBSCRIBER, plugin );

        daoUtil.setInt( 1, nNewsLetterId );
        daoUtil.setInt( 2, nSubscriberId );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteOldUnconfirmed( Timestamp confirmLimitDate, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_OLD_FROM_SUBSCRIBER, plugin );
        daoUtil.setTimestamp( 1, confirmLimitDate );
        daoUtil.setBoolean( 2, false );

        daoUtil.executeUpdate( );
        daoUtil.free( );

        daoUtil = new DAOUtil( SQL_QUERY_DELETE_UNUSED_EMAIL, plugin );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRegistered( int nNewsLetterId, int nSubscriberId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_CHECK_IS_REGISTERED, plugin );

        daoUtil.setInt( 1, nNewsLetterId );
        daoUtil.setInt( 2, nSubscriberId );
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
     * {@inheritDoc}
     */
    @Override
    public boolean isTemplateUsed( int nTemplateId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_CHECK_IS_TEMPLATE_USED, plugin );

        daoUtil.setInt( 1, nTemplateId );
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
     * {@inheritDoc}
     */
    @Override
    public int selectNbrSubscribers( int nNewsLetterId, String strSearchString, Plugin plugin )
    {
        int nCount;

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_NBR_SUBSCRIBERS, plugin );

        daoUtil.setString( 1, CONSTANT_PERCENT + strSearchString + CONSTANT_PERCENT );
        daoUtil.setInt( 2, nNewsLetterId );

        daoUtil.executeQuery( );

        if ( !daoUtil.next( ) )
        {
            // If the table is empty
            nCount = 0;
        }
        else
        {
            nCount = daoUtil.getInt( 1 );
        }

        daoUtil.free( );

        return nCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int selectNbrActiveSubscribers( int nNewsLetterId, String strSearchString, Plugin plugin )
    {
        int nCount;

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_NBR_ACTIVE_SUBSCRIBERS, plugin );

        daoUtil.setString( 1, CONSTANT_PERCENT + strSearchString + CONSTANT_PERCENT );
        daoUtil.setInt( 2, nNewsLetterId );

        daoUtil.executeQuery( );

        if ( !daoUtil.next( ) )
        {
            // If the table is empty
            nCount = 0;
        }
        else
        {
            nCount = daoUtil.getInt( 1 );
        }

        daoUtil.free( );

        return nCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateSubscriber( int nNewsLetterId, int nSubscriberId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_VALIDATE_SUBSCRIBER, plugin );

        daoUtil.setInt( 1, nNewsLetterId );
        daoUtil.setInt( 2, nSubscriberId );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }
}
