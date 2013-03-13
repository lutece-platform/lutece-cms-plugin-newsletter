package fr.paris.lutece.plugins.newsletter.business.section;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.List;


/**
 * Home for FreeHtmlSection objects
 */
public final class FreeHtmlSectionHome
{
    private static IFreeHtmlSectionDAO _dao = SpringContextService.getBean( "newsletter.freeHtmlSectionDAO" );

    /**
     * Private constructor
     */
    private FreeHtmlSectionHome( )
    {
    }

    /**
     * Get a {@link FreeHtmlSection} by its primary key from the database
     * @param nId The id of the {@link FreeHtmlSection} to get
     * @param plugin The plugin
     * @return The {@link FreeHtmlSection} with the given id, or null if no
     *         {@link FreeHtmlSection} has this id.
     */
    public static FreeHtmlSection findByPrimaryKey( int nId, Plugin plugin )
    {
        return _dao.findByPrimaryKey( nId, plugin );
    }

    /**
     * Insert a new {@link FreeHtmlSection} into the database
     * @param freeHtmlSection The {@link FreeHtmlSection} to insert.
     * @param plugin The plugin
     */
    public static void insertFreeHtmlSection( FreeHtmlSection freeHtmlSection, Plugin plugin )
    {
        _dao.insert( freeHtmlSection, plugin );
    }

    /**
     * Update a {@link FreeHtmlSection} in the database
     * @param freeHtmlSection The new values of the {@link FreeHtmlSection}.
     * @param plugin The plugin
     */
    public static void updateFreeHtmlSection( FreeHtmlSection freeHtmlSection, Plugin plugin )
    {
        _dao.update( freeHtmlSection, plugin );
    }

    /**
     * Delete a {@link FreeHtmlSection} from the database
     * @param nId The id of the {@link FreeHtmlSection} to delete.
     * @param plugin The plugin
     */
    public static void removeFreeHtmlSection( int nId, Plugin plugin )
    {
        _dao.remove( nId, plugin );
    }

    /**
     * Get a list of {@link FreeHtmlSection} from a list of ids
     * @param listIds The list of ids of {@link FreeHtmlSection} to get.
     * @param plugin The plugin
     * @return The list of {@link FreeHtmlSection} found.
     */
    public static List<FreeHtmlSection> getFreeHtmlSectionList( List<Integer> listIds, Plugin plugin )
    {
        return _dao.findCollection( listIds, plugin );
    }
}
