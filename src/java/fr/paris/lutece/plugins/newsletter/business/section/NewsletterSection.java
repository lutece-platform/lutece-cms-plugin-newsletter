package fr.paris.lutece.plugins.newsletter.business.section;

import org.apache.commons.lang.StringUtils;


/**
 * Class to describe a section of a newsletter. this class implements the
 * {@link java.lang.Comparable Comparable} interface.
 */
public class NewsletterSection implements Comparable<NewsletterSection>
{
    private int _nId;
    private int _nIdNewsletter;
    private String _strSectionTypeCode;
    private String _strTitle;
    private int _nOrder;
    private int _nCategory;

    /**
     * Get the id of this newsletter section
     * @return The id of this newsletter section
     */
    public int getId( )
    {
        return _nId;
    }

    /**
     * Set the id of this newsletter section
     * @param nId The id of this newsletter section
     */
    public void setId( int nId )
    {
        this._nId = nId;
    }

    /**
     * Get the id of the newsletter associated with this newsletter section
     * @return the id of the newsletter associated with this newsletter section
     */
    public int getIdNewsletter( )
    {
        return _nIdNewsletter;
    }

    /**
     * Set the id of the newsletter associated with this newsletter section
     * @param nIdNewsletter the id of the newsletter
     */
    public void setIdNewsletter( int nIdNewsletter )
    {
        this._nIdNewsletter = nIdNewsletter;
    }

    /**
     * Get the name of the section type of this newsletter section
     * @return The name of the section type of this newsletter section
     */
    public String getSectionTypeCode( )
    {
        return _strSectionTypeCode;
    }

    /**
     * Set the name of the section type of this newsletter section
     * @param strSectionTypeCode The name of the section type of this newsletter
     *            section
     */
    public void setSectionTypeCode( String strSectionTypeCode )
    {
        _strSectionTypeCode = strSectionTypeCode;
    }

    /**
     * Get the title of this newsletter section
     * @return The title of this newsletter section
     */
    public String getTitle( )
    {
        return _strTitle;
    }

    /**
     * Set the title of this newsletter section
     * @param strTitle The title of this newsletter section
     */
    public void setTitle( String strTitle )
    {
        this._strTitle = strTitle;
    }

    /**
     * Get the order of the newsletter section
     * @return The order of the newsletter section
     */
    public int getOrder( )
    {
        return _nOrder;
    }

    /**
     * Set the order of the newsletter section
     * @param nOrder The order of the newsletter section
     */
    public void setOrder( int nOrder )
    {
        this._nOrder = nOrder;
    }

    /**
     * Get the category of the section
     * @return The category of the section
     */
    public int getCategory( )
    {
        return _nCategory;
    }

    /**
     * Set the category of the section
     * @param nCategory The category of the section
     */
    public void setCategory( int nCategory )
    {
        this._nCategory = nCategory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo( NewsletterSection o )
    {
        if ( getIdNewsletter( ) > o.getIdNewsletter( ) )
        {
            return 1;
        }
        if ( getIdNewsletter( ) < o.getIdNewsletter( ) )
        {
            return -1;
        }
        if ( getCategory( ) > o.getCategory( ) )
        {
            return 1;
        }
        if ( getCategory( ) < o.getCategory( ) )
        {
            return -1;
        }
        if ( getOrder( ) > o.getOrder( ) )
        {
            return 1;
        }
        if ( getOrder( ) < o.getOrder( ) )
        {
            return -1;
        }

        // If they have the same category and order, then it has to be the same object, so they should be equal
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals( Object o )
    {
        if ( !( o instanceof NewsletterSection ) )
        {
            return false;
        }
        NewsletterSection otherSection = (NewsletterSection) o;
        return getId( ) == otherSection.getId( ) && getIdNewsletter( ) == otherSection.getIdNewsletter( )
                && getCategory( ) == otherSection.getCategory( ) && getOrder( ) == otherSection.getOrder( )
                && StringUtils.equals( getSectionTypeCode( ), otherSection.getSectionTypeCode( ) );
    }
}
