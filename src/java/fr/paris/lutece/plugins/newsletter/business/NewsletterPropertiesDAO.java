/*
 * Copyright (c) 2002-2014, Mairie de Paris
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
package fr.paris.lutece.plugins.newsletter.business;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import org.apache.commons.lang.StringUtils;


/**
 * This class provides Data Access methods for NewsletterProperties objects
 */
public final class NewsletterPropertiesDAO implements INewsletterPropertiesDAO
{
	// Constants   
	private static final String SQL_QUERY_SELECT = "SELECT validation_activated, captcha_activated, tos FROM newsletter_properties ";
	private static final String SQL_QUERY_UPDATE = "UPDATE newsletter_properties SET validation_activated = ?, captcha_activated = ?, tos = ?";

	/**
	 * loads data from NewsLetterProperties
	 * @param plugin the Plugin
	 * @return an object NewsLetterProperties
	 */
	public NewsLetterProperties load( Plugin plugin )
	{
		try( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin ) )
		{
			daoUtil.executeQuery( );

			NewsLetterProperties properties = new NewsLetterProperties( );

			if ( daoUtil.next( ) )
			{
				properties.setValidationActive( daoUtil.getBoolean( 1 ) );
				properties.setCaptchaActive( daoUtil.getBoolean( 2 ) );

				String strTos = daoUtil.getString( 3 );
				if ( StringUtils.isNotEmpty( strTos ) )
				{
					properties.setTOS( strTos );
				}
				else
				{
					properties.setTOS( null );
				}
			}

			daoUtil.free( );

			return properties;
		}
	}

	/**
	 * Update the record in the table
	 * @param properties the instance of properties class to be updated
	 * @param plugin the Plugin
	 */
	public void store( NewsLetterProperties properties, Plugin plugin )
	{
		try( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
		{
			daoUtil.setBoolean( 1, properties.isValidationActive( ) );
			daoUtil.setBoolean( 2, properties.isCaptchaActive( ) );
			daoUtil.setString( 3, properties.getTOS( ) );

			daoUtil.executeUpdate( );
			daoUtil.free( );
		}
	}
}
