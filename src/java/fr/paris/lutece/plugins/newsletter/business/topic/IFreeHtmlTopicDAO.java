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

import java.util.List;

/**
 * Interface for FreeHtmlTopic DAO
 */
public interface IFreeHtmlTopicDAO
{
    /**
     * Get a {@link FreeHtmlTopic} by its primary key from the database
     * 
     * @param nId
     *            The id of the {@link FreeHtmlTopic} to get
     * @param plugin
     *            The plugin
     * @return The {@link FreeHtmlTopic} with the given id, or null if no {@link FreeHtmlTopic} has this id.
     */
    FreeHtmlTopic findByPrimaryKey( int nId, Plugin plugin );

    /**
     * Insert a new {@link FreeHtmlTopic} into the database
     * 
     * @param freeHtmlTopic
     *            The {@link FreeHtmlTopic} to insert.
     * @param plugin
     *            The plugin
     */
    void insert( FreeHtmlTopic freeHtmlTopic, Plugin plugin );

    /**
     * Update a {@link FreeHtmlTopic} in the database
     * 
     * @param freeHtmlTopic
     *            The new values of the {@link FreeHtmlTopic}.
     * @param plugin
     *            The plugin
     */
    void update( FreeHtmlTopic freeHtmlTopic, Plugin plugin );

    /**
     * Delete a {@link FreeHtmlTopic} from the database
     * 
     * @param nId
     *            The id of the {@link FreeHtmlTopic} to delete.
     * @param plugin
     *            The plugin
     */
    void remove( int nId, Plugin plugin );

    /**
     * Get a list of {@link FreeHtmlTopic} from a list of ids
     * 
     * @param listIds
     *            The list of ids of {@link FreeHtmlTopic} to get.
     * @param plugin
     *            The plugin
     * @return The list of {@link FreeHtmlTopic} found.
     */
    List<FreeHtmlTopic> findCollection( List<Integer> listIds, Plugin plugin );
}
