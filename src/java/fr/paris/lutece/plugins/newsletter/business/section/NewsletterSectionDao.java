package fr.paris.lutece.plugins.newsletter.business.section;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * DAO implementation for {@link NewsletterSection}
 */
public class NewsletterSectionDao implements INewsletterSectionDAO
{
    private static final String SQL_QUERY_NEW_PRIMARY_KEY = " SELECT MAX(id_section) FROM newsletter_section ";

    private static final String SQL_QUERY_SELECT = " SELECT id_section, id_newsletter, section_type_name, title, section_order, category FROM newsletter_section WHERE id_section = ? ";
    private static final String SQL_QUERY_SELECT_ALL_BY_ID_NEWSLETTER = " SELECT id_section, id_newsletter, section_type_name, title, section_order, category FROM newsletter_section WHERE id_newsletter = ? ORDER BY category, section_order asc ";
    private static final String SQL_QUERY_SELECT_ALL_BY_ID_NEWSLETTER_AND_ORDER = " SELECT id_section, id_newsletter, section_type_name, title, section_order, category FROM newsletter_section WHERE id_newsletter = ? AND section_order = ? AND category = ? ";
    private static final String SQL_QUERY_INSERT = " INSERT INTO newsletter_section (id_section, id_newsletter, section_type_name, title, section_order, category) VALUES (?,?,?,?,?,?) ";
    private static final String SQL_QUERY_UPDATE = " UPDATE newsletter_section SET id_newsletter = ?, section_type_name = ?, title = ?, section_order = ?, category = ? WHERE id_section = ? ";
    private static final String SQL_QUERY_DELETE = " DELETE FROM newsletter_section WHERE id_section = ? ";
    private static final String SQL_QUERY_UPDATE_ORDER = " UPDATE newsletter_section SET section_order = ? WHERE id_section = ? ";
    private static final String SQL_QUERY_FIND_LAST_ORDER = " SELECT MAX(section_order) FROM newsletter_section WHERE id_newsletter = ? AND category = ? ";
    private static final String SQL_QUERY_FILL_ORDER_BLANK = " UPDATE newsletter_section SET section_order = section_order - 1 WHERE id_newsletter = ? AND category = ? and section_order > ? ";
    
    /**
     * {@inheritDoc}
     */
    @Override
    public NewsletterSection findByPrimaryKey( int nId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT );
        NewsletterSection section = null;
        daoUtil.setInt( 1, nId );
        daoUtil.executeQuery( );
        if ( daoUtil.next( ) )
        {
            int nIndex = 1;
            section = new NewsletterSection( );
            section.setId( daoUtil.getInt( nIndex++ ) );
            section.setIdNewsletter( daoUtil.getInt( nIndex++ ) );
            section.setSectionTypeCode( daoUtil.getString( nIndex++ ) );
            section.setTitle( daoUtil.getString( nIndex++ ) );
            section.setOrder( daoUtil.getInt( nIndex++ ) );
            section.setCategory( daoUtil.getInt( nIndex ) );
        }
        daoUtil.free( );
        return section;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert( NewsletterSection newsletterSection, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );
        newsletterSection.setId( newPrimaryKey( ) );
        daoUtil.setInt( 1, newsletterSection.getId( ) );
        daoUtil.setInt( 2, newsletterSection.getIdNewsletter( ) );
        daoUtil.setString( 3, newsletterSection.getSectionTypeCode( ) );
        daoUtil.setString( 4, newsletterSection.getTitle( ) );
        daoUtil.setInt( 5, newsletterSection.getOrder( ) );
        daoUtil.setInt( 6, newsletterSection.getCategory( ) );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update( NewsletterSection newsletterSection, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin );
        daoUtil.setInt( 1, newsletterSection.getIdNewsletter( ) );
        daoUtil.setString( 2, newsletterSection.getSectionTypeCode( ) );
        daoUtil.setString( 3, newsletterSection.getTitle( ) );
        daoUtil.setInt( 4, newsletterSection.getOrder( ) );
        daoUtil.setInt( 5, newsletterSection.getCategory( ) );
        daoUtil.setInt( 6, newsletterSection.getId( ) );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove( int nId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin );
        daoUtil.setInt( 1, nId );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NewsletterSection> findAllByIdNewsletter( int nIdNewsletter, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL_BY_ID_NEWSLETTER );
        List<NewsletterSection> listNewsletterSections = new ArrayList<NewsletterSection>( );
        daoUtil.setInt( 1, nIdNewsletter );
        daoUtil.executeQuery( );
        while ( daoUtil.next( ) )
        {
            int nIndex = 1;
            NewsletterSection section = new NewsletterSection( );
            section.setId( daoUtil.getInt( nIndex++ ) );
            section.setIdNewsletter( daoUtil.getInt( nIndex++ ) );
            section.setSectionTypeCode( daoUtil.getString( nIndex++ ) );
            section.setTitle( daoUtil.getString( nIndex++ ) );
            section.setOrder( daoUtil.getInt( nIndex++ ) );
            section.setCategory( daoUtil.getInt( nIndex ) );
            listNewsletterSections.add( section );
        }
        daoUtil.free( );
        return listNewsletterSections;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateNewsletterSectionOrder( int nIdNewsletterSection, int nNewOrder, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_ORDER, plugin );
        daoUtil.setInt( 1, nNewOrder );
        daoUtil.setInt( 2, nIdNewsletterSection );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NewsletterSection> findByNewsletterIdAndOrder( int nIdNewsletter, int nOrder, int nCategory,
            Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL_BY_ID_NEWSLETTER_AND_ORDER );
        List<NewsletterSection> listNewsletterSections = new ArrayList<NewsletterSection>( );
        daoUtil.setInt( 1, nIdNewsletter );
        daoUtil.setInt( 2, nOrder );
        daoUtil.setInt( 3, nCategory );
        daoUtil.executeQuery( );
        while ( daoUtil.next( ) )
        {
            int nIndex = 1;
            NewsletterSection section = new NewsletterSection( );
            section.setId( daoUtil.getInt( nIndex++ ) );
            section.setIdNewsletter( daoUtil.getInt( nIndex++ ) );
            section.setSectionTypeCode( daoUtil.getString( nIndex++ ) );
            section.setTitle( daoUtil.getString( nIndex++ ) );
            section.setOrder( daoUtil.getInt( nIndex++ ) );
            section.setCategory( daoUtil.getInt( nIndex ) );
            listNewsletterSections.add( section );
        }
        daoUtil.free( );
        return listNewsletterSections;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNewOrder( int nIdNewsletter, int nCategory, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_FIND_LAST_ORDER, plugin );
        daoUtil.setInt( 1, nIdNewsletter );
        daoUtil.setInt( 2, nCategory );
        daoUtil.executeQuery( );
        int nNewOrder = 1;
        if ( daoUtil.next( ) )
        {
            // We get the last order, and we add 1 to have the next
            nNewOrder = daoUtil.getInt( 1 ) + 1;
        }
        daoUtil.free( );
        return nNewOrder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLastOrder( int nIdNewsletter, int nCategory, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_FIND_LAST_ORDER, plugin );
        daoUtil.setInt( 1, nIdNewsletter );
        daoUtil.setInt( 2, nCategory );
        daoUtil.executeQuery( );
        int nLastOrder = 1;
        if ( daoUtil.next( ) )
        {
            nLastOrder = daoUtil.getInt( 1 );
        }
        daoUtil.free( );
        return nLastOrder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fillBlankInOrder( int nIdNewsletter, int nOrder, int nCategory, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_FILL_ORDER_BLANK, plugin );
        daoUtil.setInt( 1, nIdNewsletter );
        daoUtil.setInt( 2, nCategory );
        daoUtil.setInt( 3, nOrder );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * Get a new primary key
     * @return A new primary key
     */
    private int newPrimaryKey( )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_NEW_PRIMARY_KEY );
        daoUtil.executeQuery( );
        int nId = 1;
        if ( daoUtil.next( ) )
        {
            nId = daoUtil.getInt( 1 ) + 1;
        }
        daoUtil.free( );
        return nId;
    }
}
