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

import fr.paris.lutece.portal.service.rbac.RBACResource;
import fr.paris.lutece.portal.service.workgroup.AdminWorkgroupResource;
import fr.paris.lutece.portal.service.workgroup.WorkgroupRemovalListenerService;

/**
 * This class represents business objects NewsLetterTemplate
 */
public class NewsLetterTemplate implements AdminWorkgroupResource, RBACResource
{
    /**
     * The resource type key
     */
    public static final String RESOURCE_TYPE = "NEWSLETTER_TEMPLATE";
    private static final String EMPTY_STRING = "";
    private static NewsletterTemplateWorkgroupRemovalListener _listenerWorkgroup;
    private int _nId;
    private String _strTopicType;
    private String _strDescription;
    private String _strFileName;
    private String _strPicture;
    private String _strWorkgroup;
    private int _nSectionNumber;

    /**
     * Initialisation method
     */
    public static synchronized void init( )
    {
        if ( _listenerWorkgroup == null )
        {
            _listenerWorkgroup = new NewsletterTemplateWorkgroupRemovalListener( );
            WorkgroupRemovalListenerService.getService( ).registerListener( _listenerWorkgroup );
        }
    }

    /**
     * Returns the identifier of the template
     * 
     * @return the template identifier
     */
    public int getId( )
    {
        return _nId;
    }

    /**
     * Sets the identifier of the template
     * 
     * @param nId
     *            the template identifier
     */
    public void setId( int nId )
    {
        _nId = nId;
    }

    /**
     * Returns the topic type of the template
     * 
     * @return the topic type of the template
     */
    public String getTopicType( )
    {
        return _strTopicType;
    }

    /**
     * Sets the topic type of the template
     * 
     * @param strTopicType
     *            the topic type of the template
     */
    public void setTopicType( String strTopicType )
    {
        _strTopicType = strTopicType;
    }

    /**
     * Returns the description of the template
     * 
     * @return the template description
     */
    public String getDescription( )
    {
        return _strDescription;
    }

    /**
     * Sets the description of the template
     * 
     * @param strDescription
     *            the template description
     */
    public void setDescription( String strDescription )
    {
        _strDescription = ( strDescription == null ) ? EMPTY_STRING : strDescription;
    }

    /**
     * Returns the file name of the template
     * 
     * @return the template file name
     */
    public String getFileName( )
    {
        return _strFileName;
    }

    /**
     * Sets the file name of the template
     * 
     * @param strFileName
     *            the template file name
     */
    public void setFileName( String strFileName )
    {
        _strFileName = ( strFileName == null ) ? EMPTY_STRING : strFileName;
    }

    /**
     * Returns the picture of the template
     * 
     * @return the template picture
     */
    public String getPicture( )
    {
        return _strPicture;
    }

    /**
     * Sets the picture of the template
     * 
     * @param strPicture
     *            the template picture
     */
    public void setPicture( String strPicture )
    {
        _strPicture = ( strPicture == null ) ? EMPTY_STRING : strPicture;
    }

    /**
     * Return the workgroup
     * 
     * @return The workgroup
     */
    public String getWorkgroup( )
    {
        return _strWorkgroup;
    }

    /**
     * Set the workgroup
     * 
     * @param strWorkgroup
     *            The workgroup
     */
    public void setWorkgroup( String strWorkgroup )
    {
        _strWorkgroup = strWorkgroup;
    }

    /**
     * Returns the Resource Type Code that identify the resource type
     * 
     * @return The Resource Type Code
     */
    public String getResourceId( )
    {
        return EMPTY_STRING + getId( );
    }

    /**
     * Returns the resource Id of the current object
     * 
     * @return The resource Id of the current object
     */
    public String getResourceTypeCode( )
    {
        return RESOURCE_TYPE;
    }

    /**
     * Get the number of sections of this template
     * 
     * @return The number of sections of this template
     */
    public int getSectionNumber( )
    {
        return _nSectionNumber;
    }

    /**
     * Set the number of sections of this template
     * 
     * @param nSectionNumber
     *            The number of sections of this template
     */
    public void setSectionNumber( int nSectionNumber )
    {
        this._nSectionNumber = nSectionNumber;
    }
}
