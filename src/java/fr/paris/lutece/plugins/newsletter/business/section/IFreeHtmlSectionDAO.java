package fr.paris.lutece.plugins.newsletter.business.section;

import fr.paris.lutece.portal.service.plugin.Plugin;

import java.util.List;


/**
 * Interface for FreeHtmlSection DAO
 */
public interface IFreeHtmlSectionDAO
{
    /**
     * Get a {@link FreeHtmlSection} by its primary key from the database
     * @param nId The id of the {@link FreeHtmlSection} to get
     * @param plugin The plugin
     * @return The {@link FreeHtmlSection} with the given id, or null if no
     *         {@link FreeHtmlSection} has this id.
     */
    FreeHtmlSection findByPrimaryKey( int nId, Plugin plugin );

    /**
     * Insert a new {@link FreeHtmlSection} into the database
     * @param freeHtmlSection The {@link FreeHtmlSection} to insert.
     * @param plugin The plugin
     */
    void insert( FreeHtmlSection freeHtmlSection, Plugin plugin );

    /**
     * Update a {@link FreeHtmlSection} in the database
     * @param freeHtmlSection The new values of the {@link FreeHtmlSection}.
     * @param plugin The plugin
     */
    void update( FreeHtmlSection freeHtmlSection, Plugin plugin );

    /**
     * Delete a {@link FreeHtmlSection} from the database
     * @param nId The id of the {@link FreeHtmlSection} to delete.
     * @param plugin The plugin
     */
    void remove( int nId, Plugin plugin );

    /**
     * Get a list of {@link FreeHtmlSection} from a list of ids
     * @param listIds The list of ids of {@link FreeHtmlSection} to get.
     * @param plugin The plugin
     * @return The list of {@link FreeHtmlSection} found.
     */
    List<FreeHtmlSection> findCollection( List<Integer> listIds, Plugin plugin );
}
