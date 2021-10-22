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
package fr.paris.lutece.plugins.newsletter.business.portlet;

import fr.paris.lutece.plugins.newsletter.service.NewsletterPlugin;
import fr.paris.lutece.portal.business.portlet.Portlet;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;

/**
 * This class provides Data Access methods for NewsLetterArchivePortlet objects
 */
public final class NewsLetterArchivePortletDAO implements INewsLetterArchivePortletDAO
{
    // Constants
    private static final String SQL_QUERY_SELECT = "SELECT id_portlet FROM core_portlet WHERE id_portlet = ?";
    private static final String SQL_QUERY_SELECT_SENDINGS_BY_PORTLET = "SELECT id_sending FROM newsletter_portlet_archive  WHERE id_portlet = ?";
    private static final String SQL_QUERY_INSERT_SENDING = "INSERT INTO newsletter_portlet_archive ( id_portlet, id_sending ) VALUES ( ?, ? )";
    private static final String SQL_QUERY_DELETE_SENDING = "DELETE FROM newsletter_portlet_archive  WHERE id_portlet = ? AND id_sending = ?";
    private static final String SQL_QUERY_DELETE = "DELETE FROM newsletter_portlet_archive  WHERE id_portlet=? ";

    ///////////////////////////////////////////////////////////////////////////////////////
    // Access methods to data

    /**
     * Inserts a new record in the table. Not implemented.
     * 
     * @param portlet
     *            the object to be inserted
     */
    public void insert( Portlet portlet )
    {
        // Not implemented.
    }

    /**
     * Deletes a record from the table.
     * 
     * @param nPortletId
     *            the portlet id
     * 
     */
    public void delete( int nPortletId )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME ) );
        daoUtil.setInt( 1, nPortletId );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * Loads the data of the portlet from the table.
     * 
     * @param nPortletId
     *            the portlet id
     * @return the Portlet object
     */
    public Portlet load( int nPortletId )
    {
        NewsLetterArchivePortlet portlet = new NewsLetterArchivePortlet( );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT );

        daoUtil.setInt( 1, nPortletId );

        daoUtil.executeQuery( );

        if ( daoUtil.next( ) )
        {
            portlet.setId( nPortletId );
        }

        daoUtil.free( );

        return portlet;
    }

    /**
     * Updates the record in the table. Not implemented.
     * 
     * @param portlet
     *            the instance of Portlet class to be updated
     */
    public void store( Portlet portlet )
    {
        // Not implemented.
    }

    /**
     * Associates a new sending to a given portlet.
     * 
     * @param nPortletId
     *            the identifier of the portlet.
     * @param nSendingId
     *            the identifier of the sending.
     */
    public void insertSending( int nPortletId, int nSendingId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_SENDING, plugin );
        daoUtil.setInt( 1, nPortletId );
        daoUtil.setInt( 2, nSendingId );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * De-associate a sending from a given portlet.
     * 
     * @param nPortletId
     *            the identifier of the portlet.
     * @param nSendingId
     *            the identifier of the sending.
     */
    public void removeSending( int nPortletId, int nSendingId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_SENDING, plugin );
        daoUtil.setInt( 1, nPortletId );
        daoUtil.setInt( 2, nSendingId );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * Returns all the sendings associated with a given portlet.
     * 
     * @param nPortletId
     *            the identifier of the portlet.
     * @return a Set of Integer objects containing the identifers of the sendings.
     */
    public ArrayList<Integer> findSendingsInPortlet( int nPortletId, Plugin plugin )
    {
        ArrayList<Integer> results = new ArrayList<Integer>( );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_SENDINGS_BY_PORTLET, plugin );
        daoUtil.setInt( 1, nPortletId );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            results.add( Integer.valueOf( daoUtil.getInt( 1 ) ) );
        }

        daoUtil.free( );

        return results;
    }
}
