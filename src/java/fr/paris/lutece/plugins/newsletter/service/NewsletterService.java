/*
 * Copyright (c) 2002-2012, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.newsletter.service;

import fr.paris.lutece.plugins.newsletter.business.NewsLetter;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterHome;
import fr.paris.lutece.plugins.newsletter.business.Subscriber;
import fr.paris.lutece.plugins.newsletter.business.SubscriberHome;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.spring.SpringContextService;


/**
 * The newsletter service
 *
 */
public class NewsletterService
{
    public static final String BEAN_NAME = "newsletter.newsletterService";

    /**
     * Returns the instance of the singleton
     * 
     * @return The instance of the singleton
     */
    public static NewsletterService getService( )
    {
        return SpringContextService.getBean( BEAN_NAME );
    }

    /**
     * Remove a known suscriber from a newsletter
     * @param subscriber the subscriber to remove
     * @param nNewsletterId the newsletter id from which to remove the
     *            subscriber
     * @param plugin The plugin object
     */
    public void removeSubscriberFromNewsletter( Subscriber subscriber, int nNewsletterId, Plugin plugin )
    {
        /* checks newsletter exist in database */
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, plugin );

        if ( ( subscriber != null ) && ( newsletter != null ) )
        {
            int nSubscriberId = subscriber.getId( );

            /* checks if the subscriber identified is registered */
            if ( NewsLetterHome.findRegistration( nNewsletterId, nSubscriberId, plugin ) )
            {
                /* unregistration */
                NewsLetterHome.removeSubscriber( nNewsletterId, nSubscriberId, plugin );
            }

            /*
             * if the subscriber is not registered to an other newsletter, his
             * account is deleted
             */
            if ( SubscriberHome.findNewsLetters( nSubscriberId, plugin ) == 0 )
            {
                SubscriberHome.remove( nSubscriberId, plugin );
            }
        }
    }
}
