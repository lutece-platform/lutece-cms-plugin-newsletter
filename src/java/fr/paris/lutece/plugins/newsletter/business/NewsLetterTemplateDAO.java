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
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class provides Data Access methods for NewsLetter's templates objects
 */
public final class NewsLetterTemplateDAO implements INewsLetterTemplateDAO
{
    // Constants
    private static final String SQL_QUERY_SELECT_ALL = "SELECT id_template, description, file_name, picture, workgroup_key, topic_type, sections FROM newsletter_template ORDER BY id_template asc ";
    private static final String SQL_QUERY_SELECT_ALL_BY_WORKGOUP_KEY = "SELECT id_template, description, file_name, picture, workgroup_key, topic_type, sections FROM newsletter_template WHERE workgroup_key = ?";
    private static final String SQL_QUERY_SELECT_ALL_REFERENCE = " SELECT id_template, description FROM newsletter_template ";
    private static final String SQL_QUERY_SELECT = "SELECT id_template, description, file_name, picture, workgroup_key, topic_type, sections FROM newsletter_template WHERE id_template = ? ";
    private static final String SQL_QUERY_SELECT_TEMPLATES_IDS_BY_TYPE = "SELECT id_template, description  FROM newsletter_template WHERE topic_type = ?";
    private static final String SQL_QUERY_SELECT_TEMPLATES_BY_TYPE = "SELECT id_template, description, file_name, picture, workgroup_key, topic_type, sections FROM newsletter_template WHERE topic_type= ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO newsletter_template ( id_template, description, file_name, picture, workgroup_key, topic_type, sections ) VALUES ( ?, ?, ?, ?, ?, ?, ? )";
    private static final String SQL_QUERY_NEW_PRIMARY_KEY = "SELECT max( id_template ) FROM newsletter_template";
    private static final String SQL_QUERY_UPDATE = "UPDATE newsletter_template SET description = ?, file_name = ?, picture = ?, workgroup_key = ?, topic_type = ?, sections = ? WHERE id_template = ?";
    private static final String SQL_QUERY_DELETE = "DELETE FROM newsletter_template WHERE id_template = ? ";

    ///////////////////////////////////////////////////////////////////////////////////////
    // Access methods to data

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<NewsLetterTemplate> selectTemplatesList( Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL, plugin );
        daoUtil.executeQuery( );

        ArrayList<NewsLetterTemplate> list = new ArrayList<NewsLetterTemplate>( );

        while ( daoUtil.next( ) )
        {
            NewsLetterTemplate template = new NewsLetterTemplate( );

            template.setId( daoUtil.getInt( 1 ) );
            template.setDescription( daoUtil.getString( 2 ) );
            template.setFileName( daoUtil.getString( 3 ) );
            template.setPicture( daoUtil.getString( 4 ) );
            template.setWorkgroup( daoUtil.getString( 5 ) );
            template.setTopicType( daoUtil.getString( 6 ) );
            template.setSectionNumber( daoUtil.getInt( 7 ) );

            list.add( template );
        }

        daoUtil.free( );

        return list;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Access methods to data

    /**
     * {@inheritDoc}
     */
    @Override
    public ReferenceList selectTemplatesListByType( String strTopicType, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_TEMPLATES_IDS_BY_TYPE, plugin );

        daoUtil.setString( 1, strTopicType );

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
    public List<NewsLetterTemplate> selectTemplatesCollectionByType( String strTopicType, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_TEMPLATES_BY_TYPE, plugin );

        daoUtil.setString( 1, strTopicType );

        daoUtil.executeQuery( );

        List<NewsLetterTemplate> list = new ArrayList<NewsLetterTemplate>( );

        while ( daoUtil.next( ) )
        {
            NewsLetterTemplate template = new NewsLetterTemplate( );

            template.setId( daoUtil.getInt( 1 ) );
            template.setDescription( daoUtil.getString( 2 ) );
            template.setFileName( daoUtil.getString( 3 ) );
            template.setPicture( daoUtil.getString( 4 ) );
            template.setWorkgroup( daoUtil.getString( 5 ) );
            template.setTopicType( daoUtil.getString( 6 ) );
            template.setSectionNumber( daoUtil.getInt( 7 ) );

            list.add( template );
        }

        daoUtil.free( );

        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert( NewsLetterTemplate newsletter, Plugin plugin )
    {
        newsletter.setId( newPrimaryKey( plugin ) );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );

        daoUtil.setInt( 1, newsletter.getId( ) );
        daoUtil.setString( 2, newsletter.getDescription( ) );
        daoUtil.setString( 3, newsletter.getFileName( ) );
        daoUtil.setString( 4, newsletter.getPicture( ) );
        daoUtil.setString( 5, newsletter.getWorkgroup( ) );
        daoUtil.setString( 6, newsletter.getTopicType( ) );
        daoUtil.setInt( 7, newsletter.getSectionNumber( ) );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NewsLetterTemplate load( int nTemplateId, Plugin plugin )
    {
        NewsLetterTemplate template = new NewsLetterTemplate( );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin );

        daoUtil.setInt( 1, nTemplateId );

        daoUtil.executeQuery( );

        if ( daoUtil.next( ) )
        {
            template.setId( daoUtil.getInt( 1 ) );
            template.setDescription( daoUtil.getString( 2 ) );
            template.setFileName( daoUtil.getString( 3 ) );
            template.setPicture( daoUtil.getString( 4 ) );
            template.setWorkgroup( daoUtil.getString( 5 ) );
            template.setTopicType( daoUtil.getString( 6 ) );
            template.setSectionNumber( daoUtil.getInt( 7 ) );
        }

        daoUtil.free( );

        return template;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store( NewsLetterTemplate newsLetterTemplate, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin );

        daoUtil.setString( 1, newsLetterTemplate.getDescription( ) );
        daoUtil.setString( 2, newsLetterTemplate.getFileName( ) );
        daoUtil.setString( 3, newsLetterTemplate.getPicture( ) );
        daoUtil.setString( 4, newsLetterTemplate.getWorkgroup( ) );
        daoUtil.setString( 5, newsLetterTemplate.getTopicType( ) );
        daoUtil.setInt( 6, newsLetterTemplate.getSectionNumber( ) );
        daoUtil.setInt( 7, newsLetterTemplate.getId( ) );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete( int nNewsLetterTemplateId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin );
        daoUtil.setInt( 1, nNewsLetterTemplateId );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReferenceList selectTemplatesByRef( Plugin plugin )
    {
        ReferenceList listTemplates = new ReferenceList( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL_REFERENCE, plugin );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            NewsLetterTemplate template = new NewsLetterTemplate( );
            template.setId( daoUtil.getInt( 1 ) );
            template.setDescription( daoUtil.getString( 2 ) );

            listTemplates.addItem( template.getId( ), template.getDescription( ) );
        }

        daoUtil.free( );

        return listTemplates;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<NewsLetterTemplate> selectTemplatesListByWorkgoup( String strWorkgroupKey, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL_BY_WORKGOUP_KEY, plugin );
        daoUtil.setString( 1, strWorkgroupKey );
        daoUtil.executeQuery( );

        ArrayList<NewsLetterTemplate> list = new ArrayList<NewsLetterTemplate>( );

        while ( daoUtil.next( ) )
        {
            NewsLetterTemplate template = new NewsLetterTemplate( );

            template.setId( daoUtil.getInt( 1 ) );
            template.setDescription( daoUtil.getString( 2 ) );
            template.setFileName( daoUtil.getString( 3 ) );
            template.setPicture( daoUtil.getString( 4 ) );
            template.setWorkgroup( daoUtil.getString( 5 ) );
            template.setTopicType( daoUtil.getString( 6 ) );
            template.setSectionNumber( daoUtil.getInt( 7 ) );

            list.add( template );
        }

        daoUtil.free( );

        return list;
    }

    /**
     * Calculate a new primary key to add a new NewsletterTemplate
     * 
     * @param plugin
     *            the plugin
     * @return The new key.
     */
    private int newPrimaryKey( Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_NEW_PRIMARY_KEY, plugin );

        int nKey;

        daoUtil.executeQuery( );

        if ( !daoUtil.next( ) )
        {
            // If the table is empty
            nKey = 1;
        }
        else
        {
            nKey = daoUtil.getInt( 1 ) + 1;
        }

        daoUtil.free( );

        return nKey;
    }
}
