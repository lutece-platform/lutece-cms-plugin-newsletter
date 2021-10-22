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
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.List;

/**
 * Home for NewsletterTopic objects
 */
public final class NewsletterTopicHome
{
    private static INewsletterTopicDAO _dao = SpringContextService.getBean( "newsletter.newsletterTopicDAO" );

    /**
     * Private constructor
     */
    private NewsletterTopicHome( )
    {
    }

    /**
     * Get a {@link NewsletterTopic} by its primary key from the database
     * 
     * @param nId
     *            The id of the {@link NewsletterTopic} to get
     * @param plugin
     *            The plugin
     * @return The {@link NewsletterTopic} with the given id, or null if no {@link NewsletterTopic} has this id.
     */
    public static NewsletterTopic findByPrimaryKey( int nId, Plugin plugin )
    {
        return _dao.findByPrimaryKey( nId, plugin );
    }

    /**
     * Insert a new {@link NewsletterTopic} into the database
     * 
     * @param newsletterTopic
     *            The {@link NewsletterTopic} to insert.
     * @param plugin
     *            The plugin
     */
    public static void insertNewsletterTopic( NewsletterTopic newsletterTopic, Plugin plugin )
    {
        newsletterTopic.setOrder( _dao.getNewOrder( newsletterTopic.getIdNewsletter( ), newsletterTopic.getSection( ), plugin ) );
        _dao.insert( newsletterTopic, plugin );
    }

    /**
     * Update a {@link NewsletterTopic} in the database
     * 
     * @param newsletterTopic
     *            The new values of the {@link NewsletterTopic}.
     * @param plugin
     *            The plugin
     */
    public static void updateNewsletterTopic( NewsletterTopic newsletterTopic, Plugin plugin )
    {
        _dao.update( newsletterTopic, plugin );
    }

    /**
     * Delete a {@link NewsletterTopic} from the database
     * 
     * @param nId
     *            The id of the {@link NewsletterTopic} to delete.
     * @param plugin
     *            The plugin
     */
    public static void removeNewsletterTopic( int nId, Plugin plugin )
    {
        _dao.remove( nId, plugin );
    }

    /**
     * Get the list of {@link NewsletterTopic} associated with a given newsletter.
     * 
     * @param nIdNewsletter
     *            The id of the newsletter
     * @param plugin
     *            The plugin
     */
    public static void removeAllByIdNewsletter( int nIdNewsletter, Plugin plugin )
    {
        _dao.removeAllByIdNewsletter( nIdNewsletter, plugin );
    }

    /**
     * Get the list of {@link NewsletterTopic} associated with a given newsletter.
     * 
     * @param nIdNewsletter
     *            The id of the newsletter
     * @param plugin
     *            The plugin
     * @return The list of {@link NewsletterTopic} found.
     */
    public static List<NewsletterTopic> findAllByIdNewsletter( int nIdNewsletter, Plugin plugin )
    {
        return _dao.findAllByIdNewsletter( nIdNewsletter, plugin );
    }

    /**
     * Update topic orders of a newsletter. The order of the given topic is set to the new value, and the topic that had this order gets the old order of the
     * updated topic.
     * 
     * @param newsletterTopic
     *            The topic to move. The order attribute of the topic <b>MUST</b> be its old order.
     * @param nNewOrder
     *            The new order of the topic
     * @param plugin
     *            The plugin
     */
    public static void updateNewsletterTopicOrder( NewsletterTopic newsletterTopic, int nNewOrder, Plugin plugin )
    {
        List<NewsletterTopic> listTopics = _dao.findByNewsletterIdAndOrder( newsletterTopic.getIdNewsletter( ), nNewOrder, newsletterTopic.getSection( ),
                plugin );
        if ( listTopics != null && listTopics.size( ) > 0 )
        {
            _dao.updateNewsletterTopicOrder( listTopics.get( 0 ).getId( ), newsletterTopic.getOrder( ), plugin );
            if ( listTopics.size( ) > 1 )
            {
                listTopics.remove( 0 );
                int nNextOrder = _dao.getNewOrder( newsletterTopic.getIdNewsletter( ), newsletterTopic.getSection( ), plugin );
                for ( NewsletterTopic topic : listTopics )
                {
                    _dao.updateNewsletterTopicOrder( topic.getId( ), nNextOrder++, plugin );
                }
            }
        }
        _dao.updateNewsletterTopicOrder( newsletterTopic.getId( ), nNewOrder, plugin );
    }

    /**
     * Get the next available order value for topics of a newsletter
     * 
     * @param nIdNewsletter
     *            The id of the newsletter
     * @param nSection
     *            The section
     * @param plugin
     *            The plugin
     * @return The next available order value
     */
    public static int getNewOrder( int nIdNewsletter, int nSection, Plugin plugin )
    {
        return _dao.getNewOrder( nIdNewsletter, nSection, plugin );
    }

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
    public static int getLastOrder( int nIdNewsletter, int nSection, Plugin plugin )
    {
        return _dao.getLastOrder( nIdNewsletter, nSection, plugin );
    }

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
    public static void fillBlankInOrder( int nIdNewsletter, int nOrder, int nSection, Plugin plugin )
    {
        _dao.fillBlankInOrder( nIdNewsletter, nOrder, nSection, plugin );
    }
}
