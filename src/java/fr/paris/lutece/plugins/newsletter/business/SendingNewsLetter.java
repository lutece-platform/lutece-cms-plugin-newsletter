/*
 * Copyright (c) 2002-2021, City of Paris
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

import java.sql.Timestamp;

/**
 * This class represents business objects SendingNewsLetter
 */
public class SendingNewsLetter
{
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private int _nId;
    private int _nNewsLetterId;
    private int _nCountSubscribers;
    private String _strHtml;
    private Timestamp _date;
    private String _emailSubject;

    /**
     * Sets the identifier of the sending
     *
     * @param nId
     *            the sending identifier
     */
    public void setId( int nId )
    {
        _nId = nId;
    }

    /**
     * Returns the identifier of the sending
     *
     * @return the sending identifier
     */
    public int getId( )
    {
        return _nId;
    }

    /**
     * Sets the identifier of the newsletter
     *
     * @param nNewsLetterId
     *            the newsletter identifier
     */
    public void setNewsLetterId( int nNewsLetterId )
    {
        _nNewsLetterId = nNewsLetterId;
    }

    /**
     * Returns the identifier of the newsletter
     *
     * @return the newsletter identifier
     */
    public int getNewsLetterId( )
    {
        return _nNewsLetterId;
    }

    /**
     * Sets the html content of the sending newsletter
     *
     * @param strHtml
     *            the newsletter html content
     */
    public void setHtml( String strHtml )
    {
        _strHtml = strHtml;
    }

    /**
     * Returns the html content of the sending newsletter
     *
     * @return the newsletter html content
     */
    public String getHtml( )
    {
        return _strHtml;
    }

    /**
     * Sets the number of subscribers
     *
     * @param nCountSubscribers
     *            the number of subscribers
     */
    public void setCountSubscribers( int nCountSubscribers )
    {
        _nCountSubscribers = nCountSubscribers;
    }

    /**
     * Returns the number of subscribers
     *
     * @return the number of subscribers
     */
    public int getCountSubscribers( )
    {
        return _nCountSubscribers;
    }

    /**
     * Sets the date of the newsletter sending
     *
     * @param date
     *            the date of the sending
     */
    public void setDate( Timestamp date )
    {
        _date = date;
    }

    /**
     * Returns the date of the newsletter sending
     *
     * @return the date of sending
     */
    public Timestamp getDate( )
    {
        return _date;
    }

    /**
     * Returns the subject of the email
     * 
     * @return the subject of the email
     */
    public String getEmailSubject( )
    {
        return _emailSubject;
    }

    /**
     * Sets the subject of the email
     * 
     * @param subject
     *            the new subject
     */
    public void setEmailSubject( String subject )
    {
        _emailSubject = subject;
    }
}
