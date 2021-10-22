package fr.paris.lutece.plugins.newsletter.service.topic;

import fr.paris.lutece.plugins.newsletter.business.topic.NewsletterTopic;
import fr.paris.lutece.plugins.newsletter.business.topic.NewsletterTopicHome;
import fr.paris.lutece.plugins.newsletter.service.NewsletterPlugin;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;


/**
 * Service to manage newsletter content types
 */
public class NewsletterTopicService implements Serializable
{
    /**
     * Name of the bean of this service
     */
    public static final String BEAN_NAME = "newsletter.newsletterTopicService";

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -555734991348133022L;

    /**
     * Get the service from Spring context
     * @return An instance of the service
     */
    public static NewsletterTopicService getService( )
    {
        return SpringContextService.getBean( BEAN_NAME );
    }

    /**
     * Get a reference list with every newsletter topic types
     * @param locale The locale to get the topic types name in.
     * @return A reference list containing an item for each newsletter topic
     *         type.
     */
    public ReferenceList getNewsletterTopicTypeRefList( Locale locale )
    {
        ReferenceList refListResult = new ReferenceList( );
        for ( INewsletterTopicService service : SpringContextService.getBeansOfType( INewsletterTopicService.class ) )
        {
            ReferenceItem refItem = new ReferenceItem( );
            refItem.setCode( service.getNewsletterTopicTypeCode( ) );
            refItem.setName( service.getNewsletterTopicTypeName( locale ) );
            refListResult.add( refItem );
        }
        return refListResult;
    }

    /**
     * Creates a new newsletter topic
     * @param newsletterTopic The newsletter topic to create
     * @param user The current admin user
     * @param locale The current locale
     */
    public void createNewsletterTopic( NewsletterTopic newsletterTopic, AdminUser user, Locale locale )
    {
        Plugin plugin = PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
        for ( INewsletterTopicService service : SpringContextService.getBeansOfType( INewsletterTopicService.class ) )
        {
            if ( StringUtils.equals( service.getNewsletterTopicTypeCode( ), newsletterTopic.getTopicTypeCode( ) ) )
            {
                newsletterTopic.setTitle( service.getNewsletterTopicTypeName( locale ) );
                NewsletterTopicHome.insertNewsletterTopic( newsletterTopic, plugin );
                service.createNewsletterTopic( newsletterTopic, user, locale );
            }
        }
    }

    /**
     * Removes a newsletter topic.
     * @param newsletterTopic The topic to remove
     * @param user The current admin user
     */
    public void removeNewsletterTopic( NewsletterTopic newsletterTopic, AdminUser user )
    {
        Plugin plugin = PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
        for ( INewsletterTopicService service : SpringContextService.getBeansOfType( INewsletterTopicService.class ) )
        {
            if ( StringUtils.equals( service.getNewsletterTopicTypeCode( ), newsletterTopic.getTopicTypeCode( ) ) )
            {
                service.removeNewsletterTopic( newsletterTopic.getId( ) );
            }
        }
        NewsletterTopicHome.removeNewsletterTopic( newsletterTopic.getId( ), plugin );
        NewsletterTopicHome.fillBlankInOrder( newsletterTopic.getIdNewsletter( ), newsletterTopic.getOrder( ),
                newsletterTopic.getSection( ), plugin );
    }

    /**
     * Get the configuration page of a topic
     * @param newsletterTopic The topic to get the configuration page of.
     * @param strBaseUrl the base url
     * @param user The current user
     * @param locale The locale to use
     * @return The HTML content of the configuration page of the topic
     */
    public String getConfigurationPage( NewsletterTopic newsletterTopic, String strBaseUrl, AdminUser user,
            Locale locale )
    {
        for ( INewsletterTopicService service : SpringContextService.getBeansOfType( INewsletterTopicService.class ) )
        {
            if ( StringUtils.equals( service.getNewsletterTopicTypeCode( ), newsletterTopic.getTopicTypeCode( ) ) )
            {
                return service.getConfigurationPage( newsletterTopic, strBaseUrl, user, locale );
            }
        }
        return null;
    }

    /**
     * Save the configuration of a topic
     * @param mapParameters The map of parameters of the the configuration. The
     *            map contains request parameter if it is a request context.
     * @param newsletterTopic The topic to save the configuration of
     * @param user The current user, or null if there is no current user
     * @param locale The locale to use
     */
    public void saveConfiguration( Map<String, String[]> mapParameters, NewsletterTopic newsletterTopic,
            AdminUser user, Locale locale )
    {
        for ( INewsletterTopicService service : SpringContextService.getBeansOfType( INewsletterTopicService.class ) )
        {
            if ( StringUtils.equals( service.getNewsletterTopicTypeCode( ), newsletterTopic.getTopicTypeCode( ) ) )
            {
                service.saveConfiguration( mapParameters, newsletterTopic, user, locale );
            }
        }
    }

    /**
     * Move a topic up or down in its section
     * @param newsletterTopic The topic to move
     * @param bMoveUp True to move the topic up (ie to decrease its order),
     *            false to move it down (ie to increase its order)
     */
    public void modifyNewsletterTopicOrder( NewsletterTopic newsletterTopic, boolean bMoveUp )
    {
        Plugin plugin = PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
        // If we have the first topic and we try to move it up, or if we have the last topic, and we try to move it down, we don't do anything
        if ( bMoveUp
                && newsletterTopic.getOrder( ) <= 1
                || !bMoveUp
                && newsletterTopic.getOrder( ) == NewsletterTopicHome.getLastOrder( newsletterTopic.getIdNewsletter( ),
                        newsletterTopic.getSection( ), plugin ) )
        {
            return;
        }

        NewsletterTopicHome.updateNewsletterTopicOrder( newsletterTopic, bMoveUp ? newsletterTopic.getOrder( ) - 1
                : newsletterTopic.getOrder( ) + 1, plugin );
    }

    /**
     * Modify the section of a topic of a newsletter. The order of the
     * topic in its new section is the last one.
     * @param newsletterTopic The topic to update with the old values of
     *            topic and order.
     * @param nSection The new section
     */
    public void modifyNewsletterTopicSection( NewsletterTopic newsletterTopic, int nSection )
    {
        Plugin plugin = PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
        if ( newsletterTopic.getSection( ) == nSection )
        {
            return;
        }
        // We save the old values
        int nCurrentOrder = newsletterTopic.getOrder( );
        int nCurrentSection = newsletterTopic.getSection( );

        // We update the new section and order
        newsletterTopic.setSection( nSection );
        newsletterTopic
                .setOrder( NewsletterTopicHome.getNewOrder( newsletterTopic.getIdNewsletter( ), nSection, plugin ) );
        NewsletterTopicHome.updateNewsletterTopic( newsletterTopic, plugin );

        // We update ordered of the old section so that there is no blank
        NewsletterTopicHome.fillBlankInOrder( newsletterTopic.getIdNewsletter( ), nCurrentOrder, nCurrentSection,
                plugin );
    }

    /**
     * Get the html content of a newsletter topic
     * @param newsletterTopic The topic to get the content of
     * @param user The current user
     * @param locale The locale to display the content in.
     * @return The html content of the topic
     */
    public String getTopicContent( NewsletterTopic newsletterTopic, AdminUser user, Locale locale )
    {
        for ( INewsletterTopicService service : SpringContextService.getBeansOfType( INewsletterTopicService.class ) )
        {
            if ( StringUtils.equals( service.getNewsletterTopicTypeCode( ), newsletterTopic.getTopicTypeCode( ) ) )
            {
                return service.getHtmlContent( newsletterTopic, user, locale );
            }
        }
        return null;
    }

    /**
     * Get the name of the topic type from a topic type code
     * @param strTopicTypeCode The code of the topic type to get the name of
     * @return The name of the topic type, or an empty string if no topic type
     *         is found.
     */
    public String getTopicTypeName( String strTopicTypeCode )
    {
        for ( INewsletterTopicService service : SpringContextService.getBeansOfType( INewsletterTopicService.class ) )
        {
            if ( StringUtils.equals( service.getNewsletterTopicTypeCode( ), strTopicTypeCode ) )
            {
                return service.getNewsletterTopicTypeName( Locale.getDefault( ) );
            }
        }
        return StringUtils.EMPTY;
    }
}
