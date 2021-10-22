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

import java.util.ArrayList;
import java.util.List;

/**
 * The skeleton data access methods for SendingNewsletter
 *
 */
public interface ISendingNewsLetterDAO
{
    /**
     * Insert a new record in the table.
     *
     * @param sending
     *            the object to be inserted
     * @param plugin
     *            the Plugin
     */
    void insert( SendingNewsLetter sending, Plugin plugin );

    /**
     * Delete the record in the database
     * 
     * @param nSendingId
     *            the sending Identifier
     * @param plugin
     *            the Plugin
     */
    void delete( int nSendingId, Plugin plugin );

    /**
     * Loads the data from the database
     *
     * @param nSendingId
     *            the sending identifier
     * @param plugin
     *            the Plugin
     * @return an object SendingNewsLetter
     */
    SendingNewsLetter load( int nSendingId, Plugin plugin );

    /**
     * Update the record in the table
     *
     * @param sending
     *            an instance of the class SendingNewsLetter
     * @param plugin
     *            the Plugin
     */
    void store( SendingNewsLetter sending, Plugin plugin );

    /**
     * Returns the last sending performed for the newsletter of given id
     * 
     * @param newsletterId
     *            the newsletter id for wich we need the last sending
     * @param plugin
     *            the plugin
     * @return the last sending for the given newsletter id - null if no sending found
     */
    SendingNewsLetter selectLastSendingForNewsletterId( int newsletterId, Plugin plugin );

    /**
     * Returns all the sendings in the database.
     *
     * @param plugin
     *            the plugin
     * @return a list of SendingNewsLetter objects.
     */
    List<SendingNewsLetter> findAllSendings( Plugin plugin );

    /**
     * Returns the sendings corresponding to a list of ids.
     * 
     * @return a list of SendingNewsLetter objects.
     * @param listId
     *            A list of id corresponding to sent newsletters
     * @param plugin
     *            the plugin
     */
    ArrayList<SendingNewsLetter> findSendingsByIds( ArrayList<Integer> listId, Plugin plugin );

    /**
     * Returns all the last sending performed for the newsletter of given id
     * 
     * @param newsletterId
     *            the newsletter id for wich we need the last sending
     * @param plugin
     *            the plugin
     * @return the last sending for the given newsletter id - null if no sending found
     */
    List<SendingNewsLetter> selectAllLastSendingForNewsletterId( int newsletterId, Plugin plugin );
}
