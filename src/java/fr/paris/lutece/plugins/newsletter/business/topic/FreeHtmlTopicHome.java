package fr.paris.lutece.plugins.newsletter.business.topic;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.List;


/**
 * Home for FreeHtmlTopic objects
 */
public final class FreeHtmlTopicHome
{
    private static IFreeHtmlTopicDAO _dao = SpringContextService.getBean( "newsletter.freeHtmlTopicDAO" );

    /**
     * Private constructor
     */
    private FreeHtmlTopicHome( )
    {
    }

    /**
     * Get a {@link FreeHtmlTopic} by its primary key from the database
     * @param nId The id of the {@link FreeHtmlTopic} to get
     * @param plugin The plugin
     * @return The {@link FreeHtmlTopic} with the given id, or null if no
     *         {@link FreeHtmlTopic} has this id.
     */
    public static FreeHtmlTopic findByPrimaryKey( int nId, Plugin plugin )
    {
        return _dao.findByPrimaryKey( nId, plugin );
    }

    /**
     * Insert a new {@link FreeHtmlTopic} into the database
     * @param freeHtmlTopic The {@link FreeHtmlTopic} to insert.
     * @param plugin The plugin
     */
    public static void insertFreeHtmlTopic( FreeHtmlTopic freeHtmlTopic, Plugin plugin )
    {
        _dao.insert( freeHtmlTopic, plugin );
    }

    /**
     * Update a {@link FreeHtmlTopic} in the database
     * @param freeHtmlTopic The new values of the {@link FreeHtmlTopic}.
     * @param plugin The plugin
     */
    public static void updateFreeHtmlTopic( FreeHtmlTopic freeHtmlTopic, Plugin plugin )
    {
        _dao.update( freeHtmlTopic, plugin );
    }

    /**
     * Delete a {@link FreeHtmlTopic} from the database
     * @param nId The id of the {@link FreeHtmlTopic} to delete.
     * @param plugin The plugin
     */
    public static void removeFreeHtmlTopic( int nId, Plugin plugin )
    {
        _dao.remove( nId, plugin );
    }

    /**
     * Get a list of {@link FreeHtmlTopic} from a list of ids
     * @param listIds The list of ids of {@link FreeHtmlTopic} to get.
     * @param plugin The plugin
     * @return The list of {@link FreeHtmlTopic} found.
     */
    public static List<FreeHtmlTopic> getFreeHtmlTopicList( List<Integer> listIds, Plugin plugin )
    {
        return _dao.findCollection( listIds, plugin );
    }
}
