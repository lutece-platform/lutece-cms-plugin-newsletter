package fr.paris.lutece.plugins.newsletter.business.section;

import fr.paris.lutece.portal.service.plugin.Plugin;

import java.util.List;


/**
 * Interface for NewsletterSection DAO
 */
public interface INewsletterSectionDAO
{
    /**
     * Get a {@link NewsletterSection} by its primary key from the database
     * @param nId The id of the {@link NewsletterSection} to get
     * @param plugin The plugin
     * @return The {@link NewsletterSection} with the given id, or null if no
     *         {@link NewsletterSection} has this id.
     */
    NewsletterSection findByPrimaryKey( int nId, Plugin plugin );

    /**
     * Insert a new {@link NewsletterSection} into the database
     * @param newsletterSection The {@link NewsletterSection} to insert.
     * @param plugin The plugin
     */
    void insert( NewsletterSection newsletterSection, Plugin plugin );

    /**
     * Update a {@link NewsletterSection} in the database
     * @param newsletterSection The new values of the {@link NewsletterSection}.
     * @param plugin The plugin
     */
    void update( NewsletterSection newsletterSection, Plugin plugin );

    /**
     * Delete a {@link NewsletterSection} from the database
     * @param nId The id of the {@link NewsletterSection} to delete.
     * @param plugin The plugin
     */
    void remove( int nId, Plugin plugin );

    /**
     * Get the list of {@link NewsletterSection} associated with a given
     * newsletter.
     * @param nIdNewsletter The id of the newsletter
     * @param plugin The plugin
     * @return The list of {@link NewsletterSection} found.
     */
    List<NewsletterSection> findAllByIdNewsletter( int nIdNewsletter, Plugin plugin );

    /**
     * Update the order of a newsletter section
     * @param nIdNewsletterSection The id of the newsletter section to update
     * @param nNewOrder The new order of the section
     * @param plugin The plugin
     */
    void updateNewsletterSectionOrder( int nIdNewsletterSection, int nNewOrder, Plugin plugin );

    /**
     * Get the list of newsletter sections associated to a given newsletter and
     * with the given order
     * @param nIdNewsletter The id of the newsletter the section must be
     *            associated with.
     * @param nOrder The order the sections must have
     * @param plugin The plugin
     * @return The list of newsletter sections. The list should contain only one
     *         or zero element. If it has more, then it indicates that several
     *         sections have the same order and should be reordered.
     */
    List<NewsletterSection> findByNewsletterIdAndOrder( int nIdNewsletter, int nOrder, Plugin plugin );

    /**
     * Get the next available order value for sections of a newsletter
     * @param nIdNewsletter The id of the newsletter
     * @param nCategory The category of the newsletter
     * @param plugin The plugin
     * @return The next available order value
     */
    int getNewOrder( int nIdNewsletter, int nCategory, Plugin plugin );
}
