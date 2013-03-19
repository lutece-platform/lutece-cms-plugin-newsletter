package fr.paris.lutece.plugins.newsletter.service;

import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.RemovalListenerService;

/**
 * NewsletterTemplateRemovalService
 */
public final class NewsletterTemplateRemovalService
{
    private static final String BEAN_NEWSLETTER_TEMPLATE_REMOVAL_SERVICE = "newsletter.newsletterTemplateRemovalService";

    /**
     * Private constructor
     */
    private NewsletterTemplateRemovalService( )
    {
    }

    /**
     * Returns the removal service
     * @return The removal service
     */
    public static RemovalListenerService getService(  )
    {
        return (RemovalListenerService) SpringContextService.getBean( BEAN_NEWSLETTER_TEMPLATE_REMOVAL_SERVICE );
    }
}
