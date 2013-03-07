package fr.paris.lutece.plugins.newsletter.business.section;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * DAO implementation for {@link FreeHtmlSection}
 */
public class FreeHtmlSectionDao implements IFreeHtmlSectionDAO
{
    private static final String SQL_QUERY_FIND_BY_PRIMARY_KEY = " SELECT id_section, html_content FROM newsletter_section_free_html WHERE id_section = ? ";
    private static final String SQL_QUERY_UPDATE = " UPDATE newsletter_section_free_html SET html_content = ? WHERE id_section = ? ";
    private static final String SQL_QUERY_INSERT = " INSERT INTO newsletter_section_free_html (id_section, html_content) VALUES (?,?) ";
    private static final String SQL_QUERY_DELETE = " DELETE FROM newsletter_section_free_html WHERE id_section = ? ";
    private static final String SQL_QUERY_FIND_BY_ID_LIST = "  SELECT id_section, html_content FROM newsletter_section_free_html WHERE id_section IN ( ";

    private static final String CONSTANT_COMMA = ",";
    private static final String CONSTANT_CLOSE_PARENTHESIS = ")";

    /**
     * {@inheritDoc}
     */
    @Override
    public FreeHtmlSection findByPrimaryKey( int nId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_FIND_BY_PRIMARY_KEY, plugin );
        daoUtil.setInt( 1, nId );
        FreeHtmlSection section = null;

        daoUtil.executeQuery( );
        if ( daoUtil.next( ) )
        {
            section = new FreeHtmlSection( );
            section.setId( daoUtil.getInt( 1 ) );
            section.setHtmlContent( daoUtil.getString( 2 ) );
        }
        daoUtil.free( );
        return section;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert( FreeHtmlSection freeHtmlSection, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );
        daoUtil.setInt( 1, freeHtmlSection.getId( ) );
        daoUtil.setString( 2, freeHtmlSection.getHtmlContent( ) );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update( FreeHtmlSection freeHtmlSection, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin );
        daoUtil.setString( 1, freeHtmlSection.getHtmlContent( ) );
        daoUtil.setInt( 2, freeHtmlSection.getId( ) );
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
    public List<FreeHtmlSection> findCollection( List<Integer> listIds, Plugin plugin )
    {
        List<FreeHtmlSection> listSections = new ArrayList<FreeHtmlSection>( );
        if ( listIds != null )
        {
            List<Integer> listPrivIds = new ArrayList<Integer>( listIds );
            StringBuilder sbSql = new StringBuilder( SQL_QUERY_FIND_BY_ID_LIST );
            if (  listIds.size( ) > 0 )
            {
                sbSql.append( listPrivIds.get( 0 ) );
                listPrivIds.remove( 0 );
            }
            for ( int nId : listPrivIds )
            {
                sbSql.append( CONSTANT_COMMA );
                sbSql.append( nId );
            }
            sbSql.append( CONSTANT_CLOSE_PARENTHESIS );
            DAOUtil daoUtil = new DAOUtil( sbSql.toString( ), plugin );
            int nIndex = 1;
            for ( int nId : listIds )
            {
                daoUtil.setInt( nIndex++, nId );
            }

            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                FreeHtmlSection section = new FreeHtmlSection( );
                section.setId( daoUtil.getInt( 1 ) );
                section.setHtmlContent( daoUtil.getString( 2 ) );
                listSections.add( section );
            }
            daoUtil.free( );
        }
        return listSections;
    }
}
