package fr.paris.lutece.plugins.newsletter.business.section;

/**
 * Describe a free html section of a newsletter.
 */
public class FreeHtmlSection
{
    private int _nId;
    private String _strHtmlContent;

    /**
     * Get the id of the section
     * @return The id of the section
     */
    public int getId( )
    {
        return _nId;
    }

    /**
     * Set the id of the section
     * @param nId The id of the section
     */
    public void setId( int nId )
    {
        this._nId = nId;
    }

    /**
     * Get the html content of the section
     * @return The html content of the section
     */
    public String getHtmlContent( )
    {
        return _strHtmlContent;
    }

    /**
     * Get the html content of the section
     * @param strHtmlContent The html content of the section
     */
    public void setHtmlContent( String strHtmlContent )
    {
        this._strHtmlContent = strHtmlContent;
    }
}
