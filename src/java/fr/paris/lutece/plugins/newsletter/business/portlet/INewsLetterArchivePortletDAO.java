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
import fr.paris.lutece.portal.service.plugin.Plugin;

import java.util.ArrayList;

/**
 * This class provides Data Access methods for NewsLetterArchivePortlet objects
 */
public interface INewsLetterArchivePortletDAO extends IPortletInterfaceDAO
{
    /**
     * Associates a new sending to a given portlet.
     * 
     * @param nPortletId
     *            the identifier of the portlet.
     * @param nSendingId
     *            the identifier of the sending.
     * @param plugin
     *            the plugin
     */
    void insertSending( int nPortletId, int nSendingId, Plugin plugin );

    /**
     * De-associate a sending from a given portlet.
     * 
     * @param nPortletId
     *            the identifier of the portlet.
     * @param plugin
     *            the plugin
     * @param nSendingId
     *            the identifier of the sending.
     */
    void removeSending( int nPortletId, int nSendingId, Plugin plugin );

    /**
     * Returns all the sendings associated with a given portlet.
     * 
     * @param nPortletId
     *            the identifier of the portlet.
     * @param plugin
     *            the plugin
     * @return a Set of Integer objects containing the identifers of the sendings.
     */
    ArrayList<Integer> findSendingsInPortlet( int nPortletId, Plugin plugin );
}
