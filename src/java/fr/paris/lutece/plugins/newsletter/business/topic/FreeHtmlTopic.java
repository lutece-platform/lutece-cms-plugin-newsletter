package fr.paris.lutece.plugins.newsletter.business.topic;

/**
 * Describe a free html topic of a newsletter.
 */
public class FreeHtmlTopic
{
    private int _nId;
    private String _strHtmlContent;

    /**
     * Get the id of the topic
     * @return The id of the topic
     */
    public int getId( )
    {
        return _nId;
    }

    /**
     * Set the id of the topic
     * @param nId The id of the topic
     */
    public void setId( int nId )
    {
        this._nId = nId;
    }

    /**
     * Get the html content of the topic
     * @return The html content of the topic
     */
    public String getHtmlContent( )
    {
        return _strHtmlContent;
    }

    /**
     * Get the html content of the topic
     * @param strHtmlContent The html content of the topic
     */
    public void setHtmlContent( String strHtmlContent )
    {
        this._strHtmlContent = strHtmlContent;
    }
}
