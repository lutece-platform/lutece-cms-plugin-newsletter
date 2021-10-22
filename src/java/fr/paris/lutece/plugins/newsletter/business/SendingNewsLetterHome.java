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
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides instances management methods (create, find, ...) for SendingNewsLetter objects
 */
public final class SendingNewsLetterHome
{
    // Static variable pointed at the DAO instance
    private static ISendingNewsLetterDAO _dao = SpringContextService.getBean( "newsletter.sendingNewsLetterDAO" );

    /**
     * Private constructor - this class need not be instantiated
     */
    private SendingNewsLetterHome( )
    {
    }

    /**
     * Create an instance of a newsletter sending
     *
     * @param sending
     *            The object to insert in the database
     * @param plugin
     *            the plugin
     * @return the instance created
     */
    public static SendingNewsLetter create( SendingNewsLetter sending, Plugin plugin )
    {
        _dao.insert( sending, plugin );

        return sending;
    }

    /**
     * remove an entry of a newsletter sending
     * 
     * @param nId
     *            the primary key of the sending newsletter
     * @param plugin
     *            the plugin
     */
    public static void remove( int nId, Plugin plugin )
    {
        _dao.delete( nId, plugin );
    }

    /**
     * Update of the sendingNewsLetter which is specified in parameter
     *
     * @param sending
     *            the object which contains the data to store
     * @param plugin
     *            the plugin
     * @return the new instance updated
     */
    public static SendingNewsLetter update( SendingNewsLetter sending, Plugin plugin )
    {
        _dao.store( sending, plugin );

        return sending;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Finders

    /**
     * Returns an object SendingNewsLetter from its identifier
     *
     * @param nKey
     *            the primary key of the sending newsletter
     * @param plugin
     *            the plugin
     * @return an instance of the class
     */
    public static SendingNewsLetter findByPrimaryKey( int nKey, Plugin plugin )
    {
        return _dao.load( nKey, plugin );
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
    public static SendingNewsLetter findLastSendingForNewsletterId( int newsletterId, Plugin plugin )
    {
        return _dao.selectLastSendingForNewsletterId( newsletterId, plugin );
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
    public static List<SendingNewsLetter> findAllLastSendingForNewsletterId( int newsletterId, Plugin plugin )
    {
        return _dao.selectAllLastSendingForNewsletterId( newsletterId, plugin );
    }

    /**
     * Returns all the sendings in the database.
     *
     * @param plugin
     *            the plugin
     * @return a list of SendingNewsLetter objects.
     */
    public static List<SendingNewsLetter> findAllSendings( Plugin plugin )
    {
        return _dao.findAllSendings( plugin );
    }

    /**
     * Returns all the sendings in the database.
     * 
     * @return a list of SendingNewsLetter objects.
     * @param listId
     *            A list of sent newsletters
     * @param plugin
     *            the plugin
     */
    public static ArrayList<SendingNewsLetter> findSendingsByIds( ArrayList<Integer> listId, Plugin plugin )
    {
        return _dao.findSendingsByIds( listId, plugin );
    }
}
