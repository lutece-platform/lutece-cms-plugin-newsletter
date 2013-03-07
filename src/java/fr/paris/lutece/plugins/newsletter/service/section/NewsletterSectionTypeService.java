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
public class NewsletterSectionTypeService
{
    /**
     * Name of the bean of this service
     */
    public static final String BEAN_NAME = "newsletter.newsletterSectionTypeService";

    /**
     * Get the service from Spring context
     * @return An instance of the service
     */
    public static NewsletterSectionTypeService getService( )
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
     * @param nIdSection The id of the section to remove
     * @param strSectionTypeCode The type code of the section to remove
     * @param user The current admin user
     */
    public void removeNewsletterSection( int nIdSection, String strSectionTypeCode, AdminUser user )
    {
        Plugin plugin = PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
        for ( INewsletterSectionService service : SpringContextService.getBeansOfType( INewsletterSectionService.class ) )
        {
            if ( StringUtils.equals( service.getNewsletterSectionTypeCode( ), strSectionTypeCode ) )
            {
                service.removeNewsletterSection( nIdSection );
            }
        }
        NewsletterSectionHome.removeNewsletterSection( nIdSection, plugin );
    }
}
