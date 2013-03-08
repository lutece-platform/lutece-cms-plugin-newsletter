package fr.paris.lutece.plugins.newsletter.business.section;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.List;


/**
 * Home for NewsletterSection objects
 */
public class NewsletterSectionHome
{
    private static INewsletterSectionDAO _dao = SpringContextService.getBean( "newsletter.newsletterSectionDAO" );

    /**
     * Get a {@link NewsletterSection} by its primary key from the database
     * @param nId The id of the {@link NewsletterSection} to get
     * @param plugin The plugin
     * @return The {@link NewsletterSection} with the given id, or null if no
     *         {@link NewsletterSection} has this id.
     */
    public static NewsletterSection findByPrimaryKey( int nId, Plugin plugin )
    {
        return _dao.findByPrimaryKey( nId, plugin );
    }

    /**
     * Insert a new {@link NewsletterSection} into the database
     * @param newsletterSection The {@link NewsletterSection} to insert.
     * @param plugin The plugin
     */
    public static void insertNewsletterSection( NewsletterSection newsletterSection, Plugin plugin )
    {
        newsletterSection.setOrder( _dao.getNewOrder( newsletterSection.getIdNewsletter( ),
                newsletterSection.getCategory( ), plugin ) );
        _dao.insert( newsletterSection, plugin );
    }

    /**
     * Update a {@link NewsletterSection} in the database
     * @param newsletterSection The new values of the {@link NewsletterSection}.
     * @param plugin The plugin
     */
    public static void updateNewsletterSection( NewsletterSection newsletterSection, Plugin plugin )
    {
        _dao.update( newsletterSection, plugin );
    }

    /**
     * Delete a {@link NewsletterSection} from the database
     * @param nId The id of the {@link NewsletterSection} to delete.
     * @param plugin The plugin
     */
    public static void removeNewsletterSection( int nId, Plugin plugin )
    {
        _dao.remove( nId, plugin );
    }

    /**
     * Get the list of {@link NewsletterSection} associated with a given
     * newsletter.
     * @param nIdNewsletter The id of the newsletter
     * @param plugin The plugin
     * @return The list of {@link NewsletterSection} found.
     */
    public static List<NewsletterSection> findAllByIdNewsletter( int nIdNewsletter, Plugin plugin )
    {
        return _dao.findAllByIdNewsletter( nIdNewsletter, plugin );
    }

    /**
     * Update section orders of a newsletter. The order of the given section is
     * set to the new value, and the section that had this order gets the old
     * order of the updated section.
     * @param newsletterSection The section to move. The order attribute of the
     *            section <b>MUST</b> be its old order.
     * @param nNewOrder The new order of the section
     * @param plugin The plugin
     */
    public static void updateNewsletterSectionOrder( NewsletterSection newsletterSection, int nNewOrder, Plugin plugin )
    {
        List<NewsletterSection> listSections = _dao.findByNewsletterIdAndOrder( newsletterSection.getIdNewsletter( ),
                nNewOrder, newsletterSection.getCategory( ), plugin );
        if ( listSections != null && listSections.size( ) > 0 )
        {
            _dao.updateNewsletterSectionOrder( listSections.get( 0 ).getId( ), newsletterSection.getOrder( ), plugin );
            if ( listSections.size( ) > 1 )
            {
                listSections.remove( 0 );
                int nNextOrder = _dao.getNewOrder( newsletterSection.getIdNewsletter( ),
                        newsletterSection.getCategory( ), plugin );
                for ( NewsletterSection section : listSections )
                {
                    _dao.updateNewsletterSectionOrder( section.getId( ), nNextOrder++, plugin );
                }
            }
        }
        _dao.updateNewsletterSectionOrder( newsletterSection.getId( ), nNewOrder, plugin );
    }

    /**
     * Get the next available order value for sections of a newsletter
     * @param nIdNewsletter The id of the newsletter
     * @param nCategory The category
     * @param plugin The plugin
     * @return The next available order value
     */
    public static int getNewOrder( int nIdNewsletter, int nCategory, Plugin plugin )
    {
        return _dao.getNewOrder( nIdNewsletter, nCategory, plugin );
    }

    /**
     * Get the highest order for a given newsletter and a given category
     * @param nIdNewsletter The id of the newsletter
     * @param nCategory The id of the category
     * @param plugin The plugin
     * @return The highest order actually used for the given newsletter and the
     *         given category
     */
    public static int getLastOrder( int nIdNewsletter, int nCategory, Plugin plugin )
    {
        return _dao.getLastOrder( nIdNewsletter, nCategory, plugin );
    }

    /**
     * Fill a blank in the order of sections of a newsletter.
     * @param nIdNewsletter The newsletter to update the sections of
     * @param nOrder The order with no section
     * @param nCategory The category of sections to update
     * @param plugin the plugin
     */
    public static void fillBlankInOrder( int nIdNewsletter, int nOrder, int nCategory, Plugin plugin )
    {
        _dao.fillBlankInOrder( nIdNewsletter, nOrder, nCategory, plugin );
    }
}
