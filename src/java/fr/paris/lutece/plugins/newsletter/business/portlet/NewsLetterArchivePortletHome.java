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

import fr.paris.lutece.portal.business.portlet.IPortletInterfaceDAO;
import fr.paris.lutece.portal.business.portlet.PortletHome;
import fr.paris.lutece.portal.business.portlet.PortletTypeHome;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.ArrayList;

/**
 * This class provides instances management methods for NewsLetterArchivePortlet objects
 */
public class NewsLetterArchivePortletHome extends PortletHome
{
    // This class implements the Singleton design pattern.
    private static NewsLetterArchivePortletHome _singleton = new NewsLetterArchivePortletHome( );

    // Static variable pointed at the DAO instance
    private static INewsLetterArchivePortletDAO _dao = SpringContextService.getBean( "newsletter.newsLetterArchivePortletDAO" );

    /**
     * Constructor
     */
    public NewsLetterArchivePortletHome( )
    {
        if ( _singleton == null )
        {
            _singleton = this;
        }
    }

    /**
     * Returns the identifier of the portlet type
     * 
     * @return the portlet type identifier
     */
    public String getPortletTypeId( )
    {
        String strCurrentClassName = this.getClass( ).getName( );
        String strPortletTypeId = PortletTypeHome.getPortletTypeId( strCurrentClassName );

        return strPortletTypeId;
    }

    /**
     * Returns the instance of NewsLetterArchive Portlet
     * 
     * @return the Archive Portlet instance
     */
    public static PortletHome getInstance( )
    {
        return _singleton;
    }

    /**
     * Returns the instance of the portlet DAO singleton
     * 
     * @return the instance of the DAO singleton
     */
    public IPortletInterfaceDAO getDAO( )
    {
        return _dao;
    }

    /**
     * Associates a new sending to a given portlet.
     * 
     * @param nPortletId
     *            the identifier of the portlet.
     * @param nSendingId
     *            the identifier of the sending.
     */
    public static void insertSending( int nPortletId, int nSendingId, Plugin plugin )
    {
        _dao.insertSending( nPortletId, nSendingId, plugin );
    }

    /**
     * De-associate a sending from a given portlet.
     * 
     * @param nPortletId
     *            the identifier of the portlet.
     * @param nSendingId
     *            the identifier of the sending.
     */
    public static void removeSending( int nPortletId, int nSendingId, Plugin plugin )
    {
        _dao.removeSending( nPortletId, nSendingId, plugin );
    }

    /**
     * Returns all the sendings associated with a given portlet.
     * 
     * @param nPortletId
     *            the identifier of the portlet.
     * @return a Set of Integer containing the identifiers.
     */
    public static ArrayList<Integer> findSendingsInPortlet( int nPortletId, Plugin plugin )
    {
        return _dao.findSendingsInPortlet( nPortletId, plugin );
    }
}
