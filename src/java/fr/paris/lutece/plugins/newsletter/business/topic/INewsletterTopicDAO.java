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
 * Interface for NewsletterTopic DAO
 */
public interface INewsletterTopicDAO
{
    /**
     * Get a {@link NewsletterTopic} by its primary key from the database
     * 
     * @param nId
     *            The id of the {@link NewsletterTopic} to get
     * @param plugin
     *            The plugin
     * @return The {@link NewsletterTopic} with the given id, or null if no {@link NewsletterTopic} has this id.
     */
    NewsletterTopic findByPrimaryKey( int nId, Plugin plugin );

    /**
     * Insert a new {@link NewsletterTopic} into the database
     * 
     * @param newsletterTopic
     *            The {@link NewsletterTopic} to insert.
     * @param plugin
     *            The plugin
     */
    void insert( NewsletterTopic newsletterTopic, Plugin plugin );

    /**
     * Update a {@link NewsletterTopic} in the database
     * 
     * @param newsletterTopic
     *            The new values of the {@link NewsletterTopic}.
     * @param plugin
     *            The plugin
     */
    void update( NewsletterTopic newsletterTopic, Plugin plugin );

    /**
     * Delete a {@link NewsletterTopic} from the database
     * 
     * @param nId
     *            The id of the {@link NewsletterTopic} to delete.
     * @param plugin
     *            The plugin
     */
    void remove( int nId, Plugin plugin );

    /**
     * Get the list of {@link NewsletterTopic} associated with a given newsletter.
     * 
     * @param nIdNewsletter
     *            The id of the newsletter
     * @param plugin
     *            The plugin
     * @return The list of {@link NewsletterTopic} found.
     */
    List<NewsletterTopic> findAllByIdNewsletter( int nIdNewsletter, Plugin plugin );

    /**
     * Update the order of a newsletter topic
     * 
     * @param nIdNewsletterTopic
     *            The id of the newsletter topic to update
     * @param nNewOrder
     *            The new order of the topic
     * @param plugin
     *            The plugin
     */
    void updateNewsletterTopicOrder( int nIdNewsletterTopic, int nNewOrder, Plugin plugin );

    /**
     * Get the list of newsletter topics associated to a given newsletter and with the given order in a section
     * 
     * @param nIdNewsletter
     *            The id of the newsletter the topic must be associated with.
     * @param nOrder
     *            The order the topics must have
     * @param nSection
     *            The section of the Topic
     * @param plugin
     *            The plugin
     * @return The list of newsletter topics. The list should contain only one or zero element. If it has more, then it indicates that several topics have the
     *         same order and should be reordered.
     */
    List<NewsletterTopic> findByNewsletterIdAndOrder( int nIdNewsletter, int nOrder, int nSection, Plugin plugin );

    /**
     * Get the next available order value for topics of a newsletter
     * 
     * @param nIdNewsletter
     *            The id of the newsletter
     * @param nSection
     *            The section of the newsletter
     * @param plugin
     *            The plugin
     * @return The next available order value
     */
    int getNewOrder( int nIdNewsletter, int nSection, Plugin plugin );

    /**
     * Get the highest order for a given newsletter and a given section
     * 
     * @param nIdNewsletter
     *            The id of the newsletter
     * @param nSection
     *            The id of the section
     * @param plugin
     *            The plugin
     * @return The highest order actually used for the given newsletter and the given section
     */
    int getLastOrder( int nIdNewsletter, int nSection, Plugin plugin );

    /**
     * Fill a blank in the order of topics of a newsletter.
     * 
     * @param nIdNewsletter
     *            The newsletter to update the topics of
     * @param nOrder
     *            The order with no topic
     * @param nSection
     *            The section of topics to update
     * @param plugin
     *            the plugin
     */
    void fillBlankInOrder( int nIdNewsletter, int nOrder, int nSection, Plugin plugin );

    /**
     * Remove every topic associated with a given newsletter
     * 
     * @param nIdNewsletter
     *            The id of the newsletter to remove the topics of.
     * @param plugin
     *            The plugin
     */
    void removeAllByIdNewsletter( int nIdNewsletter, Plugin plugin );
}
