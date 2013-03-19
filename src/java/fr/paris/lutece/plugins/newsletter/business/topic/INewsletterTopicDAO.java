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
     * @param nId The id of the {@link NewsletterTopic} to get
     * @param plugin The plugin
     * @return The {@link NewsletterTopic} with the given id, or null if no
     *         {@link NewsletterTopic} has this id.
     */
    NewsletterTopic findByPrimaryKey( int nId, Plugin plugin );

    /**
     * Insert a new {@link NewsletterTopic} into the database
     * @param newsletterTopic The {@link NewsletterTopic} to insert.
     * @param plugin The plugin
     */
    void insert( NewsletterTopic newsletterTopic, Plugin plugin );

    /**
     * Update a {@link NewsletterTopic} in the database
     * @param newsletterTopic The new values of the {@link NewsletterTopic}.
     * @param plugin The plugin
     */
    void update( NewsletterTopic newsletterTopic, Plugin plugin );

    /**
     * Delete a {@link NewsletterTopic} from the database
     * @param nId The id of the {@link NewsletterTopic} to delete.
     * @param plugin The plugin
     */
    void remove( int nId, Plugin plugin );

    /**
     * Get the list of {@link NewsletterTopic} associated with a given
     * newsletter.
     * @param nIdNewsletter The id of the newsletter
     * @param plugin The plugin
     * @return The list of {@link NewsletterTopic} found.
     */
    List<NewsletterTopic> findAllByIdNewsletter( int nIdNewsletter, Plugin plugin );

    /**
     * Update the order of a newsletter topic
     * @param nIdNewsletterTopic The id of the newsletter topic to update
     * @param nNewOrder The new order of the topic
     * @param plugin The plugin
     */
    void updateNewsletterTopicOrder( int nIdNewsletterTopic, int nNewOrder, Plugin plugin );

    /**
     * Get the list of newsletter topics associated to a given newsletter and
     * with the given order in a section
     * @param nIdNewsletter The id of the newsletter the topic must be
     *            associated with.
     * @param nOrder The order the topics must have
     * @param nSection The section of the Topic
     * @param plugin The plugin
     * @return The list of newsletter topics. The list should contain only one
     *         or zero element. If it has more, then it indicates that several
     *         topics have the same order and should be reordered.
     */
    List<NewsletterTopic> findByNewsletterIdAndOrder( int nIdNewsletter, int nOrder, int nSection, Plugin plugin );

    /**
     * Get the next available order value for topics of a newsletter
     * @param nIdNewsletter The id of the newsletter
     * @param nSection The section of the newsletter
     * @param plugin The plugin
     * @return The next available order value
     */
    int getNewOrder( int nIdNewsletter, int nSection, Plugin plugin );

    /**
     * Get the highest order for a given newsletter and a given section
     * @param nIdNewsletter The id of the newsletter
     * @param nSection The id of the section
     * @param plugin The plugin
     * @return The highest order actually used for the given newsletter and the
     *         given section
     */
    int getLastOrder( int nIdNewsletter, int nSection, Plugin plugin );

    /**
     * Fill a blank in the order of topics of a newsletter.
     * @param nIdNewsletter The newsletter to update the topics of
     * @param nOrder The order with no topic
     * @param nSection The section of topics to update
     * @param plugin the plugin
     */
    void fillBlankInOrder( int nIdNewsletter, int nOrder, int nSection, Plugin plugin );

    /**
     * Remove every topic associated with a given newsletter
     * @param nIdNewsletter The id of the newsletter to remove the topics of.
     * @param plugin The plugin
     */
    void removeAllByIdNewsletter( int nIdNewsletter, Plugin plugin );
}
