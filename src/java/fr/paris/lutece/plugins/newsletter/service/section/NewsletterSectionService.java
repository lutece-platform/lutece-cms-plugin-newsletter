package fr.paris.lutece.plugins.newsletter.service.section;

import fr.paris.lutece.plugins.newsletter.business.section.NewsletterSection;
import fr.paris.lutece.plugins.newsletter.business.section.NewsletterSectionHome;
import fr.paris.lutece.plugins.newsletter.service.NewsletterPlugin;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;


/**
 * Service to manage newsletter content types
 */
public class NewsletterSectionService
{
    /**
     * Name of the bean of this service
     */
    public static final String BEAN_NAME = "newsletter.newsletterSectionService";

    /**
     * Get the service from Spring context
     * @return An instance of the service
     */
    public static NewsletterSectionService getService( )
    {
        return SpringContextService.getBean( BEAN_NAME );
    }
    /**
     * Get a reference list with every newsletter section types
     * @param locale The locale to get the section types name in.
     * @return A reference list containing an item for each newsletter section
     *         type.
     */
    public ReferenceList getNewsletterSectionTypeRefList( Locale locale )
    {
        ReferenceList refListResult = new ReferenceList( );
        for ( INewsletterSectionService service : SpringContextService.getBeansOfType( INewsletterSectionService.class ) )
        {
            ReferenceItem refItem = new ReferenceItem( );
            refItem.setCode( service.getNewsletterSectionTypeCode( ) );
            refItem.setName( service.getNewsletterSectionTypeName( locale ) );
            refListResult.add( refItem );
        }
        return refListResult;
    }

    /**
     * Creates a new newsletter section
     * @param newsletterSection The newsletter section to create
     * @param user The current admin user
     * @param locale The current locale
     */
    public void createNewsletterSection( NewsletterSection newsletterSection, AdminUser user, Locale locale )
    {
        Plugin plugin = PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
        for ( INewsletterSectionService service : SpringContextService.getBeansOfType( INewsletterSectionService.class ) )
        {
            if ( StringUtils.equals( service.getNewsletterSectionTypeCode( ), newsletterSection.getSectionTypeCode( ) ) )
            {
                newsletterSection.setTitle( service.getNewsletterSectionTypeName( locale ) );
                NewsletterSectionHome.insertNewsletterSection( newsletterSection, plugin );
                service.createNewsletterSection( newsletterSection, user, locale );
            }
        }
    }

    /**
     * Removes a newsletter section.
     * @param newsletterSection The section to remove
     * @param user The current admin user
     */
    public void removeNewsletterSection( NewsletterSection newsletterSection, AdminUser user )
    {
        Plugin plugin = PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
        for ( INewsletterSectionService service : SpringContextService.getBeansOfType( INewsletterSectionService.class ) )
        {
            if ( StringUtils.equals( service.getNewsletterSectionTypeCode( ), newsletterSection.getSectionTypeCode( ) ) )
            {
                service.removeNewsletterSection( newsletterSection.getId( ) );
            }
        }
        NewsletterSectionHome.removeNewsletterSection( newsletterSection.getId( ), plugin );
        NewsletterSectionHome.fillBlankInOrder( newsletterSection.getId( ), newsletterSection.getOrder( ),
                newsletterSection.getCategory( ), plugin );
    }

    /**
     * Get the configuration page of a section
     * @param nIdSection The id of the section
     * @param user The current user
     * @param locale The locale to use
     * @return The HTML content of the configuration page of the section
     */
    public String getConfigurationPage( int nIdSection, AdminUser user, Locale locale )
    {
        Plugin plugin = PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
        NewsletterSection newsletterSection = NewsletterSectionHome.findByPrimaryKey( nIdSection, plugin );
        for ( INewsletterSectionService service : SpringContextService.getBeansOfType( INewsletterSectionService.class ) )
        {
            if ( StringUtils.equals( service.getNewsletterSectionTypeCode( ), newsletterSection.getSectionTypeCode( ) ) )
            {
                return service.getConfigurationPage( newsletterSection, user, locale );
            }
        }
        return null;
    }
    
    /**
     * Move a section up or down in its category
     * @param newsletterSection The section to move
     * @param bMoveUp True to move the section up (ie to decrease its order),
     *            false to move it down (ie to increase its order)
     */
    public void modifyNewsletterSectionOrder( NewsletterSection newsletterSection, boolean bMoveUp )
    {
        Plugin plugin = PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
        // If we have the first section and we try to move it up, or if we have the last section, and we try to move it down, we don't do anything
        if ( bMoveUp
                && newsletterSection.getOrder( ) <= 1
                || !bMoveUp
                && newsletterSection.getOrder( ) == NewsletterSectionHome.getLastOrder(
                        newsletterSection.getIdNewsletter( ), newsletterSection.getCategory( ), plugin ) )
        {
            return;
        }

        NewsletterSectionHome.updateNewsletterSectionOrder( newsletterSection,
                bMoveUp ? newsletterSection.getOrder( ) - 1 : newsletterSection.getOrder( ) + 1, plugin );
    }

    /**
     * Modify the category of a section of a newsletter. The order of the
     * section in its new category is the last one.
     * @param newsletterSection The section to update with the old values of
     *            section and order.
     * @param nCategory The new category
     */
    public void modifyNewsletterSectionCategory( NewsletterSection newsletterSection, int nCategory )
    {
        Plugin plugin = PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
        if ( newsletterSection.getCategory( ) == nCategory )
        {
            return;
        }
        // We save the old values
        int nCurrentOrder = newsletterSection.getOrder( );
        int nCurrentCategory = newsletterSection.getCategory( );

        // We update the new category and order
        newsletterSection.setCategory( nCategory );
        newsletterSection.setOrder( NewsletterSectionHome.getNewOrder( newsletterSection.getIdNewsletter( ), nCategory,
                plugin ) );
        NewsletterSectionHome.updateNewsletterSection( newsletterSection, plugin );

        // We update ordered of the old category so that there is no blank
        NewsletterSectionHome.fillBlankInOrder( newsletterSection.getIdNewsletter( ), nCurrentOrder, nCurrentCategory,
                plugin );
    }

    /**
     * Get the html content of a newsletter section
     * @param newsletterSection The section to get the content of
     * @param user The current user
     * @param locale The locale to display the content in
     * @return The html content of the section
     */
    public String getSectionContent( NewsletterSection newsletterSection, AdminUser user, Locale locale )
    {
        for ( INewsletterSectionService service : SpringContextService.getBeansOfType( INewsletterSectionService.class ) )
        {
            if ( StringUtils.equals( service.getNewsletterSectionTypeCode( ), newsletterSection.getSectionTypeCode( ) ) )
            {
                return service.getHtmlContent( newsletterSection, user, locale );
            }
        }
        return null;
    }
}
