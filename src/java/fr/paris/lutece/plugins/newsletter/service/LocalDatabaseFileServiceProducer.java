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
package fr.paris.lutece.plugins.newsletter.service;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import fr.paris.lutece.portal.service.file.IFileDownloadUrlService;
import fr.paris.lutece.portal.service.file.IFileRBACService;
import fr.paris.lutece.portal.service.file.IFileStoreService;
import fr.paris.lutece.portal.service.file.IFileStoreServiceProvider;
import fr.paris.lutece.portal.service.file.implementation.FileStoreServiceProvider;
import fr.paris.lutece.portal.service.util.CdiHelper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

/**
 * CDI producer for the newsletter file store provider. Replaces the Spring XML
 * bean {@code localDatabaseFileService} which wrapped the core
 * {@code LocalDatabaseFileService} store with a named {@code FileStoreServiceProvider}.
 */
@ApplicationScoped
public class LocalDatabaseFileServiceProducer
{
    /**
     * Produces the newsletter file store provider. The referenced store,
     * download and RBAC services are resolved by name at runtime via
     * {@link CdiHelper#getReference}, so operators can swap implementations
     * through the plugin properties file.
     *
     * @param providerName      The logical name of this provider (matches the {@code newsletter.FileProviderStore} property)
     * @param isDefault         Whether this provider is the default file store
     * @param fileStoreImplName The CDI bean name of the backing {@link IFileStoreService}
     * @param downloadImplName  The CDI bean name of the {@link IFileDownloadUrlService}
     * @param rbacImplName      The CDI bean name of the {@link IFileRBACService}
     * @return The configured file store provider
     */
    @Produces
    @ApplicationScoped
    @Named( "newsletterDatabaseFileProvider" )
    public IFileStoreServiceProvider produceNewsletterDatabaseFileProvider(
            @ConfigProperty( name = "newsletter.localDatabaseFileService.name" ) String providerName,
            @ConfigProperty( name = "newsletter.localDatabaseFileService.default", defaultValue = "false" ) boolean isDefault,
            @ConfigProperty( name = "newsletter.localDatabaseFileService.fileStoreService" ) String fileStoreImplName,
            @ConfigProperty( name = "newsletter.localDatabaseFileService.downloadService" ) String downloadImplName,
            @ConfigProperty( name = "newsletter.localDatabaseFileService.rbacService" ) String rbacImplName )
    {
        return new FileStoreServiceProvider( providerName,
                CdiHelper.getReference( IFileStoreService.class, fileStoreImplName ),
                CdiHelper.getReference( IFileDownloadUrlService.class, downloadImplName ),
                CdiHelper.getReference( IFileRBACService.class, rbacImplName ),
                isDefault );
    }
}
