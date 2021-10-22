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

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

/**
 *
 * AwaitingActivationDAO provides newsletter_awaiting_confirmation table management
 */
public class AwaitingActivationDAO implements IAwaitingActivationDAO
{
    private static final String SQL_QUERY_INSERT = "INSERT INTO newsletter_awaiting_confirmation(id_user, generated_key) VALUES (?, ?)";
    private static final String SQL_QUERY_EXISTS = "SELECT id_user, generated_key FROM newsletter_awaiting_confirmation WHERE id_user = ? AND generated_key = ?";
    private static final String SQL_QUERY_DELETE = "DELETE FROM newsletter_awaiting_confirmation WHERE id_user = ? AND generated_key = ?";

    /**
     * Removes the entry
     * 
     * @param nIdUser
     *            the user id
     * @param nKey
     *            the key
     * @param plugin
     *            the plugin
     */
    public void delete( int nIdUser, int nKey, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin );
        daoUtil.setInt( 1, nIdUser );
        daoUtil.setInt( 2, nKey );

        daoUtil.executeUpdate( );

        daoUtil.free( );
    }

    /**
     * Checks if the pair user/key already exists.
     * 
     * @param nIdUser
     *            the user id
     * @param nKey
     *            the generated key
     * @param plugin
     *            the plugin
     * @return <b>true</b> if the pair already exists, <b>false</b> otherwise.
     */
    public boolean exists( int nIdUser, int nKey, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_EXISTS, plugin );
        daoUtil.setInt( 1, nIdUser );
        daoUtil.setInt( 2, nKey );

        daoUtil.executeQuery( );

        boolean bExists = daoUtil.next( );

        daoUtil.free( );

        return bExists;
    }

    /**
     * Adds a new pair user/key entry
     * 
     * @param nIdUser
     *            the user id
     * @param nKey
     *            the generated key
     * @param plugin
     *            the plugin
     */
    public void insert( int nIdUser, int nKey, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );
        daoUtil.setInt( 1, nIdUser );
        daoUtil.setInt( 2, nKey );

        daoUtil.executeUpdate( );

        daoUtil.free( );
    }
}
