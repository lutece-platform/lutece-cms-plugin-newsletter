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
package fr.paris.lutece.plugins.newsletter.business.topic;

import fr.paris.lutece.plugins.newsletter.service.topic.NewsletterTopicService;

import org.apache.commons.lang3.StringUtils;

/**
 * Class to describe a topic of a newsletter. this class implements the {@link java.lang.Comparable Comparable} interface.
 */
public class NewsletterTopic implements Comparable<NewsletterTopic>
{
    private int _nId;
    private int _nIdNewsletter;
    private String _strTopicTypeCode;
    private String _strTitle;
    private int _nOrder;
    private int _nSection;

    /**
     * Get the id of this newsletter topic
     * 
     * @return The id of this newsletter topic
     */
    public int getId( )
    {
        return _nId;
    }

    /**
     * Set the id of this newsletter topic
     * 
     * @param nId
     *            The id of this newsletter topic
     */
    public void setId( int nId )
    {
        this._nId = nId;
    }

    /**
     * Get the id of the newsletter associated with this newsletter topic
     * 
     * @return the id of the newsletter associated with this newsletter topic
     */
    public int getIdNewsletter( )
    {
        return _nIdNewsletter;
    }

    /**
     * Set the id of the newsletter associated with this newsletter topic
     * 
     * @param nIdNewsletter
     *            the id of the newsletter
     */
    public void setIdNewsletter( int nIdNewsletter )
    {
        this._nIdNewsletter = nIdNewsletter;
    }

    /**
     * Get the name of the topic type of this newsletter topic
     * 
     * @return The name of the topic type of this newsletter topic
     */
    public String getTopicTypeCode( )
    {
        return _strTopicTypeCode;
    }

    /**
     * Get the name of the topic type associated with this topic
     * 
     * @return the name of the topic type associated with this topic
     */
    public String getTopicTypeName( )
    {
        return NewsletterTopicService.getService( ).getTopicTypeName( getTopicTypeCode( ) );
    }

    /**
     * Set the name of the topic type of this newsletter topic
     * 
     * @param strTopicTypeCode
     *            The name of the topic type of this newsletter topic
     */
    public void setTopicTypeCode( String strTopicTypeCode )
    {
        _strTopicTypeCode = strTopicTypeCode;
    }

    /**
     * Get the title of this newsletter topic
     * 
     * @return The title of this newsletter topic
     */
    public String getTitle( )
    {
        return _strTitle;
    }

    /**
     * Set the title of this newsletter topic
     * 
     * @param strTitle
     *            The title of this newsletter topic
     */
    public void setTitle( String strTitle )
    {
        this._strTitle = strTitle;
    }

    /**
     * Get the order of the newsletter topic
     * 
     * @return The order of the newsletter topic
     */
    public int getOrder( )
    {
        return _nOrder;
    }

    /**
     * Set the order of the newsletter topic
     * 
     * @param nOrder
     *            The order of the newsletter topic
     */
    public void setOrder( int nOrder )
    {
        this._nOrder = nOrder;
    }

    /**
     * Get the section of the topic
     * 
     * @return The section of the topic
     */
    public int getSection( )
    {
        return _nSection;
    }

    /**
     * Set the section of the topic
     * 
     * @param nSection
     *            The section of the topic
     */
    public void setSection( int nSection )
    {
        this._nSection = nSection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo( NewsletterTopic o )
    {
        int nRes = 0;
        if ( getIdNewsletter( ) > o.getIdNewsletter( ) )
        {
            nRes = 1;
        }
        else
        {
            if ( nRes == 0 && getIdNewsletter( ) < o.getIdNewsletter( ) )
            {
                nRes = -1;
            }
            else
            {
                if ( nRes == 0 && getSection( ) > o.getSection( ) )
                {
                    nRes = 1;
                }
                if ( nRes == 0 && getSection( ) < o.getSection( ) )
                {
                    nRes = -1;
                }
                if ( nRes == 0 && getOrder( ) > o.getOrder( ) )
                {
                    nRes = 1;
                }
                if ( nRes == 0 && getOrder( ) < o.getOrder( ) )
                {
                    nRes = -1;
                }
            }
        }

        // If they have the same section and order, then it has to be the same object, so they should be equal
        return nRes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode( )
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + _nSection;
        result = prime * result + _nId;
        result = prime * result + _nIdNewsletter;
        result = prime * result + _nOrder;
        result = prime * result + ( ( _strTopicTypeCode == null ) ? 0 : _strTopicTypeCode.hashCode( ) );
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals( Object o )
    {
        if ( !( o instanceof NewsletterTopic ) )
        {
            return false;
        }
        NewsletterTopic otherTopic = (NewsletterTopic) o;
        return getId( ) == otherTopic.getId( ) && getIdNewsletter( ) == otherTopic.getIdNewsletter( ) && getSection( ) == otherTopic.getSection( )
                && getOrder( ) == otherTopic.getOrder( ) && StringUtils.equals( getTopicTypeCode( ), otherTopic.getTopicTypeCode( ) );
    }

}
