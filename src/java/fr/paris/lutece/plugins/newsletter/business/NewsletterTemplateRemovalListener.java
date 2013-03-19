package fr.paris.lutece.plugins.newsletter.business;

import fr.paris.lutece.plugins.newsletter.service.NewsletterPlugin;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.util.RemovalListener;

import java.util.Locale;


/**
 * Removal listener for templates used by newsletters
 */
public class NewsletterTemplateRemovalListener implements RemovalListener
{
    private static final String MESSAGE_TEMPLATE_USED_BY_NEWSLETTER = "newsletter.message.templateUsedByNewsletter";

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canBeRemoved( String strId )
    {
        return !NewsLetterHome.isTemplateUsedByNewsletter( Integer.parseInt( strId ),
                PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRemovalRefusedMessage( String id, Locale locale )
    {
        return I18nService.getLocalizedString( MESSAGE_TEMPLATE_USED_BY_NEWSLETTER, locale );
    }

}
