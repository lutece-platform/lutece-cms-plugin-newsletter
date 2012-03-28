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

import fr.paris.lutece.plugins.document.business.Document;
import fr.paris.lutece.plugins.document.business.attributes.DocumentAttribute;
import fr.paris.lutece.plugins.newsletter.business.NewsLetter;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterTemplate;
import fr.paris.lutece.portal.service.util.AppLogService;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.List;


/**
 * The newsletter service
 *
 */
public class NewsletterService
{
    private static NewsletterService _singleton = new NewsletterService(  );
    private static final String FULLSTOP = ".";

    /**
    * Initialize the Newsletter service
    *
    */
    public void init(  )
    {
        NewsLetter.init(  );
        NewsLetterTemplate.init(  );
    }

    /**
     * Returns the instance of the singleton
     *
     * @return The instance of the singleton
     */
    public static NewsletterService getInstance(  )
    {
        return _singleton;
    }

    /**
     * copy specified document's type file into a given folder
     * @param document the document
     * @param strFileType the file type
     * @param strDestFolderPath the destination folder
     * @return name of the copy file or null if there is no copied file
     */
    public String copyFileFromDocument( Document document, String strFileType, String strDestFolderPath )
    {
        List<DocumentAttribute> listDocumentAttribute = document.getAttributes(  );
        String strFileName = null;

        for ( DocumentAttribute documentAttribute : listDocumentAttribute )
        {
            // if binary or is a strFileType
            if ( documentAttribute.isBinary(  ) && documentAttribute.getValueContentType(  ).contains( strFileType ) )
            {
                byte[] tabByte = documentAttribute.getBinaryValue(  );
                // fileName is composed from documentID+ documentAttributeId + documentAttributeOrder + "." + fileExtension
                strFileName = String.valueOf( document.getId(  ) ) + String.valueOf( documentAttribute.getId(  ) ) +
                    String.valueOf( documentAttribute.getAttributeOrder(  ) ) + FULLSTOP +
                    StringUtils.substringAfterLast( documentAttribute.getTextValue(  ), FULLSTOP );

                FileOutputStream fos = null;

                try
                {
                    new File( strDestFolderPath ).mkdir(  );

                    File file = new File( strDestFolderPath + strFileName );
                    fos = new FileOutputStream( file );
                    IOUtils.write( tabByte, fos );
                }
                catch ( IOException e )
                {
                    AppLogService.error( e );
                }
                catch ( Exception e )
                {
                    AppLogService.error( e );
                }
                finally
                {
                    IOUtils.closeQuietly( fos );
                }
            }
        }

        return strFileName;
    }
}
