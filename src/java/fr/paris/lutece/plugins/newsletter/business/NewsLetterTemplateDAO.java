/*
 * Copyright (c) 2002-2011, Mairie de Paris
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
    private static final String SQL_QUERY_SELECT_ALL = "SELECT * FROM newsletter_template";
    private static final String SQL_QUERY_SELECT_ALL_REFERENCE = " SELECT id_template, description FROM newsletter_template ";
    private static final String SQL_QUERY_SELECT = "SELECT description, template_type, file_name, picture, workgroup_key " +
        " FROM newsletter_template WHERE id_template = ? ";
    private static final String SQL_QUERY_SELECT_TEMPLATES_IDS_BY_TYPE = "SELECT id_template, description  FROM newsletter_template WHERE template_type= ?";
    private static final String SQL_QUERY_SELECT_TEMPLATES_BY_TYPE = "SELECT id_template, description, picture, workgroup_key  FROM newsletter_template WHERE template_type= ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO newsletter_template ( id_template, template_type, description, file_name, picture, workgroup_key ) VALUES ( ?, ?, ?, ?, ?, ? )";
    private static final String SQL_QUERY_NEW_PRIMARY_KEY = "SELECT max( id_template ) FROM newsletter_template";
    private static final String SQL_QUERY_UPDATE = "UPDATE newsletter_template SET template_type = ?, description = ?, file_name = ?, picture = ?, workgroup_key = ? WHERE id_template = ?";
    private static final String SQL_QUERY_DELETE = "DELETE FROM newsletter_template WHERE id_template = ? ";

    ///////////////////////////////////////////////////////////////////////////////////////
    //Access methods to data

    /**
     * Returns the list of all templates
     * @param plugin The plugin
     * @return the collection of all templates
     */
    public Collection<NewsLetterTemplate> selectTemplatesList( Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL, plugin );
        daoUtil.executeQuery(  );

        ArrayList<NewsLetterTemplate> list = new ArrayList<NewsLetterTemplate>(  );

        while ( daoUtil.next(  ) )
        {
            NewsLetterTemplate template = new NewsLetterTemplate(  );

            template.setId( daoUtil.getInt( 1 ) );
            template.setType( daoUtil.getInt( 2 ) );
            template.setDescription( daoUtil.getString( 3 ) );
            template.setFileName( daoUtil.getString( 4 ) );
            template.setPicture( daoUtil.getString( 5 ) );
            template.setWorkgroup( daoUtil.getString( 6 ) );

            list.add( template );
        }

        daoUtil.free(  );

        return list;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    //Access methods to data

    /**
     * Returns the list of all templates
     * @param nType the type of the templates to list
     * @param plugin The plugin
     * @return the reference list of the templates corresponding to the given type
     */
    public ReferenceList selectTemplatesListByType( int nType, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_TEMPLATES_IDS_BY_TYPE, plugin );

        daoUtil.setInt( 1, nType );

        daoUtil.executeQuery(  );

        ReferenceList list = new ReferenceList(  );

        while ( daoUtil.next(  ) )
        {
            list.addItem( daoUtil.getInt( 1 ), daoUtil.getString( 2 ) );
        }

        daoUtil.free(  );

        return list;
    }

    /**
     * Returns a list of templates depending on the given type
     * @param nType the type of the templates to list
     * @param plugin The plugin
     * @return the collection of the templates corresponding to the given type
     */
    public List<NewsLetterTemplate> selectTemplatesCollectionByType( int nType, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_TEMPLATES_BY_TYPE, plugin );

        daoUtil.setInt( 1, nType );

        daoUtil.executeQuery(  );

        List<NewsLetterTemplate> list = new ArrayList<NewsLetterTemplate>(  );

        while ( daoUtil.next(  ) )
        {
            NewsLetterTemplate template = new NewsLetterTemplate(  );

            template.setId( daoUtil.getInt( 1 ) );
            template.setDescription( daoUtil.getString( 2 ) );
            template.setPicture( daoUtil.getString( 3 ) );
            template.setWorkgroup( daoUtil.getString( 4 ) );

            list.add( template );
        }

        daoUtil.free(  );

        return list;
    }

    /**
     * Insert a new record in the table.
     *
     * @param newsletter The Instance of the object NewsLetterTemplate
     * @param plugin the plugin
     */
    public void insert( NewsLetterTemplate newsletter, Plugin plugin )
    {
        newsletter.setId( newPrimaryKey( plugin ) );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );

        daoUtil.setInt( 1, newsletter.getId(  ) );
        daoUtil.setInt( 2, newsletter.getType(  ) );
        daoUtil.setString( 3, newsletter.getDescription(  ) );
        daoUtil.setString( 4, newsletter.getFileName(  ) );
        daoUtil.setString( 5, newsletter.getPicture(  ) );
        daoUtil.setString( 6, newsletter.getWorkgroup(  ) );

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * loads the data of the newsletter's template from the table
     *
     * @param nTemplateId the template identifier
     * @param plugin the plugin
     * @return the object inserted
     */
    public NewsLetterTemplate load( int nTemplateId, Plugin plugin )
    {
        NewsLetterTemplate template = new NewsLetterTemplate(  );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin );

        daoUtil.setInt( 1, nTemplateId );

        daoUtil.executeQuery(  );

        if ( daoUtil.next(  ) )
        {
            template.setId( nTemplateId );
            template.setDescription( daoUtil.getString( 1 ) );
            template.setType( Integer.parseInt( daoUtil.getString( 2 ) ) );
            template.setFileName( daoUtil.getString( 3 ) );
            template.setPicture( daoUtil.getString( 4 ) );
            template.setWorkgroup( daoUtil.getString( 5 ) );
        }

        daoUtil.free(  );

        return template;
    }

    /**
     * Calculate a new primary key to add a new NewsletterTemplate
     *
     * @param plugin the plugin
     * @return The new key.
     */
    public int newPrimaryKey( Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_NEW_PRIMARY_KEY, plugin );

        int nKey;

        daoUtil.executeQuery(  );

        if ( !daoUtil.next(  ) )
        {
            // If the table is empty
            nKey = 1;
        }

        nKey = daoUtil.getInt( 1 ) + 1;

        daoUtil.free(  );

        return nKey;
    }

    /**
     * Update a given record in the table.
     *
     * @param newsLetterTemplate The Instance of the object NewsLetterTemplate
     * @param plugin the plugin
     */
    public void store( NewsLetterTemplate newsLetterTemplate, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin );

        daoUtil.setInt( 1, newsLetterTemplate.getType(  ) );
        daoUtil.setString( 2, newsLetterTemplate.getDescription(  ) );
        daoUtil.setString( 3, newsLetterTemplate.getFileName(  ) );
        daoUtil.setString( 4, newsLetterTemplate.getPicture(  ) );
        daoUtil.setString( 5, newsLetterTemplate.getWorkgroup(  ) );
        daoUtil.setInt( 6, newsLetterTemplate.getId(  ) );

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * Remove a record from the table
     *
     * @param nNewsLetterTemplateId the template identifier
     * @param plugin the Plugin
     */
    public void delete( int nNewsLetterTemplateId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin );
        daoUtil.setInt( 1, nNewsLetterTemplateId );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * Loads the list of newsletter templates as a ReferenceList
     * @param plugin The Plugin using this data access service
     * @return A list of templates
     */
    public ReferenceList selectTemplatesByRef( Plugin plugin )
    {
        ReferenceList listTemplates = new ReferenceList(  );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL_REFERENCE, plugin );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            NewsLetterTemplate template = new NewsLetterTemplate(  );
            template.setId( daoUtil.getInt( 1 ) );
            template.setDescription( daoUtil.getString( 2 ) );

            listTemplates.addItem( template.getId(  ), template.getDescription(  ) );
        }

        daoUtil.free(  );

        return listTemplates;
    }
}
