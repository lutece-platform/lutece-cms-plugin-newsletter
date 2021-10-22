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
package fr.paris.lutece.plugins.newsletter.business.topic;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO implementation for {@link NewsletterTopic}
 */
public class NewsletterTopicDao implements INewsletterTopicDAO
{
    private static final String SQL_QUERY_NEW_PRIMARY_KEY = " SELECT MAX(id_topic) FROM newsletter_topic ";

    private static final String SQL_QUERY_SELECT = " SELECT id_topic, id_newsletter, topic_type, title, topic_order, section FROM newsletter_topic WHERE id_topic = ? ";
    private static final String SQL_QUERY_SELECT_ALL_BY_ID_NEWSLETTER = " SELECT id_topic, id_newsletter, topic_type, title, topic_order, section FROM newsletter_topic WHERE id_newsletter = ? ORDER BY section, topic_order asc ";
    private static final String SQL_QUERY_SELECT_ALL_BY_ID_NEWSLETTER_AND_ORDER = " SELECT id_topic, id_newsletter, topic_type, title, topic_order, section FROM newsletter_topic WHERE id_newsletter = ? AND topic_order = ? AND section = ? ";
    private static final String SQL_QUERY_INSERT = " INSERT INTO newsletter_topic (id_topic, id_newsletter, topic_type, title, topic_order, section) VALUES (?,?,?,?,?,?) ";
    private static final String SQL_QUERY_UPDATE = " UPDATE newsletter_topic SET id_newsletter = ?, topic_type = ?, title = ?, topic_order = ?, section = ? WHERE id_topic = ? ";
    private static final String SQL_QUERY_DELETE = " DELETE FROM newsletter_topic WHERE id_topic = ? ";
    private static final String SQL_QUERY_UPDATE_ORDER = " UPDATE newsletter_topic SET topic_order = ? WHERE id_topic = ? ";
    private static final String SQL_QUERY_FIND_LAST_ORDER = " SELECT MAX(topic_order) FROM newsletter_topic WHERE id_newsletter = ? AND section = ? ";
    private static final String SQL_QUERY_FILL_ORDER_BLANK = " UPDATE newsletter_topic SET topic_order = topic_order - 1 WHERE id_newsletter = ? AND section = ? and topic_order > ? ";
    private static final String SQL_QUERY_REMOVE_ALL_BY_ID_NEWSLETTER = " DELETE FROM newsletter_topic WHERE id_newsletter = ? ";

    /**
     * {@inheritDoc}
     */
    @Override
    public NewsletterTopic findByPrimaryKey( int nId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin );
        NewsletterTopic topic = null;
        daoUtil.setInt( 1, nId );
        daoUtil.executeQuery( );
        if ( daoUtil.next( ) )
        {
            int nIndex = 1;
            topic = new NewsletterTopic( );
            topic.setId( daoUtil.getInt( nIndex++ ) );
            topic.setIdNewsletter( daoUtil.getInt( nIndex++ ) );
            topic.setTopicTypeCode( daoUtil.getString( nIndex++ ) );
            topic.setTitle( daoUtil.getString( nIndex++ ) );
            topic.setOrder( daoUtil.getInt( nIndex++ ) );
            topic.setSection( daoUtil.getInt( nIndex ) );
        }
        daoUtil.free( );
        return topic;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert( NewsletterTopic newsletterTopic, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );
        newsletterTopic.setId( newPrimaryKey( plugin ) );
        daoUtil.setInt( 1, newsletterTopic.getId( ) );
        daoUtil.setInt( 2, newsletterTopic.getIdNewsletter( ) );
        daoUtil.setString( 3, newsletterTopic.getTopicTypeCode( ) );
        daoUtil.setString( 4, newsletterTopic.getTitle( ) );
        daoUtil.setInt( 5, newsletterTopic.getOrder( ) );
        daoUtil.setInt( 6, newsletterTopic.getSection( ) );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update( NewsletterTopic newsletterTopic, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin );
        daoUtil.setInt( 1, newsletterTopic.getIdNewsletter( ) );
        daoUtil.setString( 2, newsletterTopic.getTopicTypeCode( ) );
        daoUtil.setString( 3, newsletterTopic.getTitle( ) );
        daoUtil.setInt( 4, newsletterTopic.getOrder( ) );
        daoUtil.setInt( 5, newsletterTopic.getSection( ) );
        daoUtil.setInt( 6, newsletterTopic.getId( ) );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove( int nId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin );
        daoUtil.setInt( 1, nId );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NewsletterTopic> findAllByIdNewsletter( int nIdNewsletter, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL_BY_ID_NEWSLETTER, plugin );
        List<NewsletterTopic> listNewsletterTopics = new ArrayList<NewsletterTopic>( );
        daoUtil.setInt( 1, nIdNewsletter );
        daoUtil.executeQuery( );
        while ( daoUtil.next( ) )
        {
            int nIndex = 1;
            NewsletterTopic topic = new NewsletterTopic( );
            topic.setId( daoUtil.getInt( nIndex++ ) );
            topic.setIdNewsletter( daoUtil.getInt( nIndex++ ) );
            topic.setTopicTypeCode( daoUtil.getString( nIndex++ ) );
            topic.setTitle( daoUtil.getString( nIndex++ ) );
            topic.setOrder( daoUtil.getInt( nIndex++ ) );
            topic.setSection( daoUtil.getInt( nIndex ) );
            listNewsletterTopics.add( topic );
        }
        daoUtil.free( );
        return listNewsletterTopics;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateNewsletterTopicOrder( int nIdNewsletterTopic, int nNewOrder, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_ORDER, plugin );
        daoUtil.setInt( 1, nNewOrder );
        daoUtil.setInt( 2, nIdNewsletterTopic );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NewsletterTopic> findByNewsletterIdAndOrder( int nIdNewsletter, int nOrder, int nSection, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL_BY_ID_NEWSLETTER_AND_ORDER, plugin );
        List<NewsletterTopic> listNewsletterTopics = new ArrayList<NewsletterTopic>( );
        daoUtil.setInt( 1, nIdNewsletter );
        daoUtil.setInt( 2, nOrder );
        daoUtil.setInt( 3, nSection );
        daoUtil.executeQuery( );
        while ( daoUtil.next( ) )
        {
            int nIndex = 1;
            NewsletterTopic topic = new NewsletterTopic( );
            topic.setId( daoUtil.getInt( nIndex++ ) );
            topic.setIdNewsletter( daoUtil.getInt( nIndex++ ) );
            topic.setTopicTypeCode( daoUtil.getString( nIndex++ ) );
            topic.setTitle( daoUtil.getString( nIndex++ ) );
            topic.setOrder( daoUtil.getInt( nIndex++ ) );
            topic.setSection( daoUtil.getInt( nIndex ) );
            listNewsletterTopics.add( topic );
        }
        daoUtil.free( );
        return listNewsletterTopics;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNewOrder( int nIdNewsletter, int nSection, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_FIND_LAST_ORDER, plugin );
        daoUtil.setInt( 1, nIdNewsletter );
        daoUtil.setInt( 2, nSection );
        daoUtil.executeQuery( );
        int nNewOrder = 1;
        if ( daoUtil.next( ) )
        {
            // We get the last order, and we add 1 to have the next
            nNewOrder = daoUtil.getInt( 1 ) + 1;
        }
        daoUtil.free( );
        return nNewOrder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLastOrder( int nIdNewsletter, int nSection, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_FIND_LAST_ORDER, plugin );
        daoUtil.setInt( 1, nIdNewsletter );
        daoUtil.setInt( 2, nSection );
        daoUtil.executeQuery( );
        int nLastOrder = 1;
        if ( daoUtil.next( ) )
        {
            nLastOrder = daoUtil.getInt( 1 );
        }
        daoUtil.free( );
        return nLastOrder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fillBlankInOrder( int nIdNewsletter, int nOrder, int nSection, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_FILL_ORDER_BLANK, plugin );
        daoUtil.setInt( 1, nIdNewsletter );
        daoUtil.setInt( 2, nSection );
        daoUtil.setInt( 3, nOrder );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAllByIdNewsletter( int nIdNewsletter, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_REMOVE_ALL_BY_ID_NEWSLETTER, plugin );
        daoUtil.setInt( 1, nIdNewsletter );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * Get a new primary key
     * 
     * @return A new primary key
     */
    private int newPrimaryKey( Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_NEW_PRIMARY_KEY, plugin );
        daoUtil.executeQuery( );
        int nId = 1;
        if ( daoUtil.next( ) )
        {
            nId = daoUtil.getInt( 1 ) + 1;
        }
        daoUtil.free( );
        return nId;
    }
}
