package fr.paris.lutece.plugins.newsletter.service.topic;

import fr.paris.lutece.plugins.newsletter.business.topic.NewsletterTopic;
import fr.paris.lutece.portal.business.user.AdminUser;

import java.util.Locale;
import java.util.Map;


/**
 * Interface of services that provide topics for newsletters
 */
public interface INewsletterTopicService
{
    /**
     * Get the unique code of the topic type associated with this service.
     * @return The unique code of the topic type associated with this service.
     */
    String getNewsletterTopicTypeCode( );

    /**
     * Get the localized name of the topic type associated with this service.
     * @param locale The locale to use
     * @return The name of the topic type associated with this service in the
     *         given locale.
     */
    String getNewsletterTopicTypeName( Locale locale );

    /**
     * Check if topics of this topic type need a configuration or not.
     * @return True if topics of this topic type has a configuration page or
     *         not.
     */
    boolean hasConfiguration( );

    /**
     * Get the configuration page of the content type.
     * @param newsletterTopic The newsletter topic to get the configuration
     *            of.
     * @param strBaseUrl The base url
     * @param user The current user
     * @param locale The locale
     * @return The HTML code of the configuration page
     */
    String getConfigurationPage( NewsletterTopic newsletterTopic, String strBaseUrl, AdminUser user, Locale locale );

    /**
     * @param mapParameters The collection of parameters of the configuration.
     *            Those parameters are request parameters in request contexts.
     * @param newsletterTopic The newsletter topic to get the configuration
     *            of.
     * @param user The current user
     * @param locale The locale
     */
    void saveConfiguration( Map<String, String[]> mapParameters, NewsletterTopic newsletterTopic, AdminUser user,
            Locale locale );

    /**
     * Creates a new topic for a newsletter
     * @param newsletterTopic The details of the topic to create
     * @param user The current user
     * @param locale The locale
     */
    void createNewsletterTopic( NewsletterTopic newsletterTopic, AdminUser user, Locale locale );

    /**
     * Remove a newsletter topic from its id.
     * @param nNewsletterTopicId The id of the topic to remove.
     */
    void removeNewsletterTopic( int nNewsletterTopicId );

    /**
     * Get the html content of a topic.
     * @param newsletterTopic The topic to get the html of.
     * @param user The current user
     * @param locale The locale
     * @return The html content describing the topic to add to the newsletter.
     */
    String getHtmlContent( NewsletterTopic newsletterTopic, AdminUser user, Locale locale );
    
    /**
     * Copy a topic for a newsletter.
     * @param id of the old newsletterTopic
     * @param newsletterTopic The details of the topic to create
     * @param user The current user
     * @param locale The locale
     */
    void copyNewsletterTopic( int oldTopicId, NewsletterTopic newsletterTopic, AdminUser user, Locale locale );
}
