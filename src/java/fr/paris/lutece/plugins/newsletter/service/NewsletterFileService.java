package fr.paris.lutece.plugins.newsletter.service;

import java.util.List;

import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.plugins.newsletter.util.NewsLetterConstants;
import fr.paris.lutece.portal.service.file.IFileStoreServiceProvider;
import fr.paris.lutece.portal.service.file.FileService;
import fr.paris.lutece.portal.service.file.FileServiceException;
import fr.paris.lutece.portal.business.file.File;
import org.apache.commons.fileupload.FileItem;

public class NewsletterFileService
{
    public static File getFileByKey( String strFileName )
    {
        String fileProviderName = AppPropertiesService.getProperty( NewsLetterConstants.PROPERTY_FileProviderStore );
        IFileStoreServiceProvider fileStoreServiceProvider = FileService.getInstance( ).getFileStoreServiceProvider(fileProviderName);
        try {
			return fileStoreServiceProvider.getFile( strFileName );
		} catch (FileServiceException e) {
			AppLogService.error(e);
			return null;
		}        

    }

    public static String getFileStoreProvideName(){
        return AppPropertiesService.getProperty( NewsLetterConstants.PROPERTY_FileProviderStore );
    }
    public static void deleteFile( String strFileName )
    {
        IFileStoreServiceProvider fileStoreServiceProvider = FileService.getInstance( ).getFileStoreServiceProvider(getFileStoreProvideName());
        String strFileError = "";
        try
        {
            if ( fileStoreServiceProvider.getFile( strFileName ) != null )
            {
                fileStoreServiceProvider.delete( strFileName );
            }
            else
            {
                strFileError = "The file does not exist in the file store";
            }
        }
        catch( Exception e )
        {
            fr.paris.lutece.portal.service.util.AppLogService.error( strFileError, e );
        }
    }

    public void deleteFiles( List<String> listFiles )
    {
        IFileStoreServiceProvider fileStoreServiceProvider = FileService.getInstance( ).getFileStoreServiceProvider(getFileStoreProvideName());
        String strFileError = "";
        try
        {
            for ( String strFileName : listFiles )
            {
                if ( fileStoreServiceProvider.getFile( strFileName ) != null )
                {
                    fileStoreServiceProvider.delete( strFileName );
                }
                else
                {
                    strFileError = "The file does not exist in the file store";
                }
            }
        }
        catch( Exception e )
        {
            fr.paris.lutece.portal.service.util.AppLogService.error( strFileError, e );
        }
    }

    public static String storeFileItem( FileItem luteceFile )
    {
        IFileStoreServiceProvider fileStoreServiceProvider = FileService.getInstance( ).getFileStoreServiceProvider(getFileStoreProvideName());
        try 
        {
			return fileStoreServiceProvider.storeFileItem( luteceFile );
		} catch (FileServiceException e) {
			AppLogService.error(e);
			return null;
		}

    }
    public static String storeFile( File luteceFile )
    {
        IFileStoreServiceProvider fileStoreServiceProvider = FileService.getInstance( ).getFileStoreServiceProvider(getFileStoreProvideName());
        try 
        {
			return fileStoreServiceProvider.storeFile( luteceFile );
		} catch (FileServiceException e) {
			AppLogService.error(e);
			return null;
		}
         
    }
}
