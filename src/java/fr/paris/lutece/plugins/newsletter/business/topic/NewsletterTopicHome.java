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
     * @param nId The id of the {@link NewsletterTopic} to get
     * @param plugin The plugin
     * @return The {@link NewsletterTopic} with the given id, or null if no
     *         {@link NewsletterTopic} has this id.
     */
    public static NewsletterTopic findByPrimaryKey( int nId, Plugin plugin )
    {
        return _dao.findByPrimaryKey( nId, plugin );
    }

    /**
     * Insert a new {@link NewsletterTopic} into the database
     * @param newsletterTopic The {@link NewsletterTopic} to insert.
     * @param plugin The plugin
     */
    public static void insertNewsletterTopic( NewsletterTopic newsletterTopic, Plugin plugin )
    {
        newsletterTopic.setOrder( _dao.getNewOrder( newsletterTopic.getIdNewsletter( ), newsletterTopic.getSection( ),
                plugin ) );
        _dao.insert( newsletterTopic, plugin );
    }

    /**
     * Update a {@link NewsletterTopic} in the database
     * @param newsletterTopic The new values of the {@link NewsletterTopic}.
     * @param plugin The plugin
     */
    public static void updateNewsletterTopic( NewsletterTopic newsletterTopic, Plugin plugin )
    {
        _dao.update( newsletterTopic, plugin );
    }

    /**
     * Delete a {@link NewsletterTopic} from the database
     * @param nId The id of the {@link NewsletterTopic} to delete.
     * @param plugin The plugin
     */
    public static void removeNewsletterTopic( int nId, Plugin plugin )
    {
        _dao.remove( nId, plugin );
    }

    /**
     * Get the list of {@link NewsletterTopic} associated with a given
     * newsletter.
     * @param nIdNewsletter The id of the newsletter
     * @param plugin The plugin
     */
    public static void removeAllByIdNewsletter( int nIdNewsletter, Plugin plugin )
    {
        _dao.removeAllByIdNewsletter( nIdNewsletter, plugin );
    }

    /**
     * Get the list of {@link NewsletterTopic} associated with a given
     * newsletter.
     * @param nIdNewsletter The id of the newsletter
     * @param plugin The plugin
     * @return The list of {@link NewsletterTopic} found.
     */
    public static List<NewsletterTopic> findAllByIdNewsletter( int nIdNewsletter, Plugin plugin )
    {
        return _dao.findAllByIdNewsletter( nIdNewsletter, plugin );
    }

    /**
     * Update topic orders of a newsletter. The order of the given topic is
     * set to the new value, and the topic that had this order gets the old
     * order of the updated topic.
     * @param newsletterTopic The topic to move. The order attribute of the
     *            topic <b>MUST</b> be its old order.
     * @param nNewOrder The new order of the topic
     * @param plugin The plugin
     */
    public static void updateNewsletterTopicOrder( NewsletterTopic newsletterTopic, int nNewOrder, Plugin plugin )
    {
        List<NewsletterTopic> listTopics = _dao.findByNewsletterIdAndOrder( newsletterTopic.getIdNewsletter( ),
                nNewOrder, newsletterTopic.getSection( ), plugin );
        if ( listTopics != null && listTopics.size( ) > 0 )
        {
            _dao.updateNewsletterTopicOrder( listTopics.get( 0 ).getId( ), newsletterTopic.getOrder( ), plugin );
            if ( listTopics.size( ) > 1 )
            {
                listTopics.remove( 0 );
                int nNextOrder = _dao.getNewOrder( newsletterTopic.getIdNewsletter( ), newsletterTopic.getSection( ),
                        plugin );
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
     * @param nIdNewsletter The id of the newsletter
     * @param nSection The section
     * @param plugin The plugin
     * @return The next available order value
     */
    public static int getNewOrder( int nIdNewsletter, int nSection, Plugin plugin )
    {
        return _dao.getNewOrder( nIdNewsletter, nSection, plugin );
    }

    /**
     * Get the highest order for a given newsletter and a given section
     * @param nIdNewsletter The id of the newsletter
     * @param nSection The id of the section
     * @param plugin The plugin
     * @return The highest order actually used for the given newsletter and the
     *         given section
     */
    public static int getLastOrder( int nIdNewsletter, int nSection, Plugin plugin )
    {
        return _dao.getLastOrder( nIdNewsletter, nSection, plugin );
    }

    /**
     * Fill a blank in the order of topics of a newsletter.
     * @param nIdNewsletter The newsletter to update the topics of
     * @param nOrder The order with no topic
     * @param nSection The section of topics to update
     * @param plugin the plugin
     */
    public static void fillBlankInOrder( int nIdNewsletter, int nOrder, int nSection, Plugin plugin )
    {
        _dao.fillBlankInOrder( nIdNewsletter, nOrder, nSection, plugin );
    }
}
