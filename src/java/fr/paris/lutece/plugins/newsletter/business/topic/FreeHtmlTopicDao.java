package fr.paris.lutece.plugins.newsletter.business.topic;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * DAO implementation for {@link FreeHtmlTopic}
 */
public class FreeHtmlTopicDao implements IFreeHtmlTopicDAO
{
	private static final String SQL_QUERY_FIND_BY_PRIMARY_KEY = " SELECT id_topic, html_content FROM newsletter_topic_free_html WHERE id_topic = ? ";
	private static final String SQL_QUERY_UPDATE = " UPDATE newsletter_topic_free_html SET html_content = ? WHERE id_topic = ? ";
	private static final String SQL_QUERY_INSERT = " INSERT INTO newsletter_topic_free_html (id_topic, html_content) VALUES (?,?) ";
	private static final String SQL_QUERY_DELETE = " DELETE FROM newsletter_topic_free_html WHERE id_topic = ? ";
	private static final String SQL_QUERY_FIND_BY_ID_LIST = "  SELECT id_topic, html_content FROM newsletter_topic_free_html WHERE id_topic IN ( ";

	private static final String CONSTANT_COMMA = ",";
	private static final String CONSTANT_CLOSE_PARENTHESIS = ")";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FreeHtmlTopic findByPrimaryKey( int nId, Plugin plugin )
	{
		try( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_FIND_BY_PRIMARY_KEY, plugin ) )
		{
			daoUtil.setInt( 1, nId );
			FreeHtmlTopic topic = null;

			daoUtil.executeQuery( );
			if ( daoUtil.next( ) )
			{
				topic = new FreeHtmlTopic( );
				topic.setId( daoUtil.getInt( 1 ) );
				topic.setHtmlContent( daoUtil.getString( 2 ) );
			}
			daoUtil.free( );
			return topic;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void insert( FreeHtmlTopic freeHtmlTopic, Plugin plugin )
	{
		try( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin ) )
		{
			daoUtil.setInt( 1, freeHtmlTopic.getId( ) );
			daoUtil.setString( 2, freeHtmlTopic.getHtmlContent( ) );
			daoUtil.executeUpdate( );
			daoUtil.free( );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update( FreeHtmlTopic freeHtmlTopic, Plugin plugin )
	{
		try( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
		{
			daoUtil.setString( 1, freeHtmlTopic.getHtmlContent( ) );
			daoUtil.setInt( 2, freeHtmlTopic.getId( ) );
			daoUtil.executeUpdate( );
			daoUtil.free( );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove( int nId, Plugin plugin )
	{
		try( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin ) )
		{
			daoUtil.setInt( 1, nId );
			daoUtil.executeUpdate( );
			daoUtil.free( );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<FreeHtmlTopic> findCollection( List<Integer> listIds, Plugin plugin )
	{
		List<FreeHtmlTopic> listTopic = new ArrayList<>( );
		if ( listIds != null )
		{
			List<Integer> listPrivIds = new ArrayList<>( listIds );
			StringBuilder sbSql = new StringBuilder( SQL_QUERY_FIND_BY_ID_LIST );
			if (  !listIds.isEmpty( ) )
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
			try( DAOUtil daoUtil = new DAOUtil( sbSql.toString( ), plugin ) )
			{
				int nIndex = 1;
				for ( int nId : listIds )
				{
					daoUtil.setInt( nIndex++, nId );
				}

				daoUtil.executeQuery( );
				while ( daoUtil.next( ) )
				{
					FreeHtmlTopic topic = new FreeHtmlTopic( );
					topic.setId( daoUtil.getInt( 1 ) );
					topic.setHtmlContent( daoUtil.getString( 2 ) );
					listTopic.add( topic );
				}
				daoUtil.free( );
			}
		}
		return listTopic;
	}
}
