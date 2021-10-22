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
 * DAO implementation for {@link FreeHtmlTopic}
 */
public class FreeHtmlTopicDao implements IFreeHtmlTopicDAO
{
    private static final String SQL_QUERY_FIND_BY_PRIMARY_KEY = " SELECT id_topic, html_content FROM newsletter_topic_free_html WHERE id_topic = ? ";
    private static final String SQL_QUERY_UPDATE = " UPDATE newsletter_topic_free_html SET html_content = ? WHERE id_topic = ? ";
    private static final String SQL_QUERY_INSERT = " INSERT INTO newsletter_topic_free_html (id_topic, html_content) VALUES (?,?) ";
    private static final String SQL_QUERY_DELETE = " DELETE FROM newsletter_topic_free_html WHERE id_topic = ? ";
    private static final String SQL_QUERY_FIND_BY_ID_LIST = "  SELECT id_topic, html_content FROM newsletter_topic_free_html WHERE id_topic IN ( ";

    private static final String CONSTANT_COMMA = ",";
    private static final String CONSTANT_CLOSE_PARENTHESIS = ")";

    /**
     * {@inheritDoc}
     */
    @Override
    public FreeHtmlTopic findByPrimaryKey( int nId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_FIND_BY_PRIMARY_KEY, plugin );
        daoUtil.setInt( 1, nId );
        FreeHtmlTopic topic = null;

        daoUtil.executeQuery( );
        if ( daoUtil.next( ) )
        {
            topic = new FreeHtmlTopic( );
            topic.setId( daoUtil.getInt( 1 ) );
            topic.setHtmlContent( daoUtil.getString( 2 ) );
        }
        daoUtil.free( );
        return topic;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert( FreeHtmlTopic freeHtmlTopic, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );
        daoUtil.setInt( 1, freeHtmlTopic.getId( ) );
        daoUtil.setString( 2, freeHtmlTopic.getHtmlContent( ) );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update( FreeHtmlTopic freeHtmlTopic, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin );
        daoUtil.setString( 1, freeHtmlTopic.getHtmlContent( ) );
        daoUtil.setInt( 2, freeHtmlTopic.getId( ) );
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
    public List<FreeHtmlTopic> findCollection( List<Integer> listIds, Plugin plugin )
    {
        List<FreeHtmlTopic> listTopic = new ArrayList<FreeHtmlTopic>( );
        if ( listIds != null )
        {
            List<Integer> listPrivIds = new ArrayList<Integer>( listIds );
            StringBuilder sbSql = new StringBuilder( SQL_QUERY_FIND_BY_ID_LIST );
            if ( listIds.size( ) > 0 )
            {
                sbSql.append( listPrivIds.get( 0 ) );
                listPrivIds.remove( 0 );
            }
            for ( int nId : listPrivIds )
            {
                sbSql.append( CONSTANT_COMMA );
                sbSql.append( nId );
            }
            sbSql.append( CONSTANT_CLOSE_PARENTHESIS );
            DAOUtil daoUtil = new DAOUtil( sbSql.toString( ), plugin );
            int nIndex = 1;
            for ( int nId : listIds )
            {
                daoUtil.setInt( nIndex++, nId );
            }

            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                FreeHtmlTopic topic = new FreeHtmlTopic( );
                topic.setId( daoUtil.getInt( 1 ) );
                topic.setHtmlContent( daoUtil.getString( 2 ) );
                listTopic.add( topic );
            }
            daoUtil.free( );
        }
        return listTopic;
    }
}
