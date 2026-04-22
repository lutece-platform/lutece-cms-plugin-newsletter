package fr.paris.lutece.plugins.newsletter.service;

import java.util.List;

import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.plugins.newsletter.util.NewsLetterConstants;
import fr.paris.lutece.portal.service.file.IFileStoreServiceProvider;
import fr.paris.lutece.portal.service.file.FileServiceException;
import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.service.upload.MultipartItem;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.literal.NamedLiteral;

/**
 * Static facade over the newsletter file store provider. Static methods are kept
 * for backward compatibility with existing callers; the underlying provider is
 * resolved once through CDI and cached.
 */
public class NewsletterFileService
{
    private static volatile IFileStoreServiceProvider _provider;

    private NewsletterFileService( )
    {
    }

    /**
     * Resolves the configured file store provider via CDI, caching the result.
     *
     * @return The file store provider bound to the newsletter plugin
     */
    private static IFileStoreServiceProvider getProvider( )
    {
        IFileStoreServiceProvider local = _provider;
        if ( local == null )
        {
            synchronized ( NewsletterFileService.class )
            {
                local = _provider;
                if ( local == null )
                {
                    String fileProviderName = AppPropertiesService.getProperty( NewsLetterConstants.PROPERTY_FileProviderStore );
                    local = CDI.current( ).select( IFileStoreServiceProvider.class, NamedLiteral.of( fileProviderName ) ).get( );
                    _provider = local;
                }
            }
        }
        return local;
    }

    /**
     * Retrieves a file by its key.
     *
     * @param strFileName The file key
     * @return The stored file, or {@code null} if not found or on error
     */
    public static File getFileByKey( String strFileName )
    {
        try
        {
            return getProvider( ).getFile( strFileName );
        }
        catch ( FileServiceException e )
        {
            AppLogService.error( e );
            return null;
        }
    }

    /**
     * Returns the configured file store provider name.
     *
     * @return The provider bean name from the plugin configuration
     */
    public static String getFileStoreProvideName( )
    {
        return AppPropertiesService.getProperty( NewsLetterConstants.PROPERTY_FileProviderStore );
    }

    /**
     * Deletes a file by key if it exists in the store.
     *
     * @param strFileName The file key to delete
     */
    public static void deleteFile( String strFileName )
    {
        IFileStoreServiceProvider fileStoreServiceProvider = getProvider( );
        try
        {
            if ( fileStoreServiceProvider.getFile( strFileName ) != null )
            {
                fileStoreServiceProvider.delete( strFileName );
            }
            else
            {
                AppLogService.error( "The file does not exist in the file store: {}", strFileName );
            }
        }
        catch ( Exception e )
        {
            AppLogService.error( "Error while deleting file {}", strFileName, e );
        }
    }

    /**
     * Deletes a list of files by key if they exist in the store.
     *
     * @param listFiles The file keys to delete
     */
    public static void deleteFiles( List<String> listFiles )
    {
        IFileStoreServiceProvider fileStoreServiceProvider = getProvider( );
        for ( String strFileName : listFiles )
        {
            try
            {
                if ( fileStoreServiceProvider.getFile( strFileName ) != null )
                {
                    fileStoreServiceProvider.delete( strFileName );
                }
                else
                {
                    AppLogService.error( "The file does not exist in the file store: {}", strFileName );
                }
            }
            catch ( Exception e )
            {
                AppLogService.error( "Error while deleting file {}", strFileName, e );
            }
        }
    }

    /**
     * Stores a multipart upload item in the configured file store.
     *
     * @param luteceFile The multipart item to store
     * @return The key of the stored file, or {@code null} on error
     */
    public static String storeFileItem( MultipartItem luteceFile )
    {
        try
        {
            return getProvider( ).storeFileItem( luteceFile );
        }
        catch ( FileServiceException e )
        {
            AppLogService.error( e );
            return null;
        }
    }

    /**
     * Stores a Lutece file in the configured file store.
     *
     * @param luteceFile The file to store
     * @return The key of the stored file, or {@code null} on error
     */
    public static String storeFile( File luteceFile )
    {
        try
        {
            return getProvider( ).storeFile( luteceFile );
        }
        catch ( FileServiceException e )
        {
            AppLogService.error( e );
            return null;
        }
    }
}
