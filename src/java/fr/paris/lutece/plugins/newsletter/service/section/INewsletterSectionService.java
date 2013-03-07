package fr.paris.lutece.plugins.newsletter.service.section;

import fr.paris.lutece.plugins.newsletter.business.section.NewsletterSection;
import fr.paris.lutece.portal.business.user.AdminUser;

import java.util.Locale;
import java.util.Map;



/**
 * Interface of services that provide sections for newsletters
 */
public interface INewsletterSectionService
{
    /**
     * Get the unique code of the section type associated with this service.
     * @return The unique code of the section type associated with this service.
     */
    String getNewsletterSectionTypeCode( );

    /**
     * Get the localized name of the section type associated with this service.
     * @param locale The locale to use
     * @return The name of the section type associated with this service in the
     *         given locale.
     */
    String getNewsletterSectionTypeName( Locale locale );

    /**
     * Check if sections of this section type need a configuration or not.
     * @return True if sections of this section type has a configuration page or
     *         not.
     */
    boolean hasConfiguration( );

    /**
     * Get the configuration page of the content type.
     * @param newsletterSection The newsletter section to get the configuration
     *            of.
     * @param user The current user
     * @param locale The locale
     * @return The HTML code of the configuration page
     */
    String getConfigurationPage( NewsletterSection newsletterSection, AdminUser user, Locale locale );

    /**
     * @param mapParameters The collection of parameters of the configuration.
     *            Those parameters are request parameters in request contexts.
     * @param newsletterSection The newsletter section to get the configuration
     *            of.
     * @param user The current user
     * @param locale The locale
     * @return The title of the newsletter section.
     */
    String saveConfiguration( Map<String, Object> mapParameters, NewsletterSection newsletterSection, AdminUser user,
            Locale locale );

    /**
     * Creates a new section for a newsletter
     * @param newsletterSection The details of the section to create
     * @param user The current user
     * @param locale The locale
     */
    void createNewsletterSection( NewsletterSection newsletterSection, AdminUser user, Locale locale );

    /**
     * Remove a newsletter section from its id.
     * @param nNewsletterSectionId The id of the section to remove.
     */
    void removeNewsletterSection( int nNewsletterSectionId );

    /**
     * Get the html content of a section.
     * @param newsletterSection The section to get the html of.
     * @param user The current user
     * @param locale The locale
     * @return The html content describing the section to add to the newsletter.
     */
    String getHtmlContent( NewsletterSection newsletterSection, AdminUser user, Locale locale );
}
