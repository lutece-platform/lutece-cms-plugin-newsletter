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
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.Set;

/**
 * This class provides instances management methods for NewsLetterSubscriptionPortlet objects
 */
public class NewsLetterSubscriptionPortletHome extends PortletHome
{
    // This class implements the Singleton design pattern.
    private static NewsLetterSubscriptionPortletHome _singleton = new NewsLetterSubscriptionPortletHome( );

    // Static variable pointed at the DAO instance
    private static INewsLetterSubscriptionPortletDAO _dao = SpringContextService.getBean( "newsletter.newsLetterSubscriptionPortletDAO" );

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
     * Returns the instance of NewsLetterSubscription Portlet
     *
     * @return the NewsLetterSubscription Portlet instance
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
     * Associates a new subscription to a given portlet.
     *
     * @param nPortletId
     *            the identifier of the portlet.
     * @param nSubscriptionId
     *            the identifier of the subscription.
     */
    public static void insertSubscription( int nPortletId, int nSubscriptionId )
    {
        _dao.insertSubscription( nPortletId, nSubscriptionId );
    }

    /**
     * De-associate a subscription from a given portlet.
     *
     * @param nPortletId
     *            the identifier of the portlet.
     * @param nIdNewsletter
     *            the identifier of the subscription.
     */
    public static void removeNewsletter( int nPortletId, int nIdNewsletter )
    {
        _dao.removeSubscription( nPortletId, nIdNewsletter );
    }

    /**
     * Returns all the newsletters chosen associated with a given portlet.
     *
     * @param nPortletId
     *            the identifier of the portlet.
     * @return a Set of Integer containing the identifiers.
     */
    public static Set<Integer> findSelectedNewsletters( int nPortletId )
    {
        return _dao.findSelectedNewsletters( nPortletId );
    }
}
