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
     * @param nId The id of the {@link FreeHtmlTopic} to get
     * @param plugin The plugin
     * @return The {@link FreeHtmlTopic} with the given id, or null if no
     *         {@link FreeHtmlTopic} has this id.
     */
    FreeHtmlTopic findByPrimaryKey( int nId, Plugin plugin );

    /**
     * Insert a new {@link FreeHtmlTopic} into the database
     * @param freeHtmlTopic The {@link FreeHtmlTopic} to insert.
     * @param plugin The plugin
     */
    void insert( FreeHtmlTopic freeHtmlTopic, Plugin plugin );

    /**
     * Update a {@link FreeHtmlTopic} in the database
     * @param freeHtmlTopic The new values of the {@link FreeHtmlTopic}.
     * @param plugin The plugin
     */
    void update( FreeHtmlTopic freeHtmlTopic, Plugin plugin );

    /**
     * Delete a {@link FreeHtmlTopic} from the database
     * @param nId The id of the {@link FreeHtmlTopic} to delete.
     * @param plugin The plugin
     */
    void remove( int nId, Plugin plugin );

    /**
     * Get a list of {@link FreeHtmlTopic} from a list of ids
     * @param listIds The list of ids of {@link FreeHtmlTopic} to get.
     * @param plugin The plugin
     * @return The list of {@link FreeHtmlTopic} found.
     */
    List<FreeHtmlTopic> findCollection( List<Integer> listIds, Plugin plugin );
}
