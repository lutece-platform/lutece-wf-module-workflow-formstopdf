/*
 * Copyright (c) 2002-2023, City of Paris
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
package fr.paris.lutece.plugins.workflow.modules.formspdf.business;

import java.sql.Statement;

import fr.paris.lutece.plugins.workflow.modules.formspdf.service.FormsPDFPlugin;
import fr.paris.lutece.plugins.workflowcore.business.config.ITaskConfigDAO;
import fr.paris.lutece.util.sql.DAOUtil;

/**
 * This class provides Data Access methods for FormsJasperTaskConfig objects
 */
public class FormsPDFTaskConfigDAO implements ITaskConfigDAO<FormsPDFTaskConfig>
{

    // Constants

    /**
     * configuration select query
     */
    private static final String SQL_QUERY_SELECT = "SELECT id_task, id_forms, format, id_template FROM workflow_task_formspdf_cf WHERE id_task = ?";

    /**
     * configuration insert query
     */
    private static final String SQL_QUERY_INSERT = "INSERT INTO workflow_task_formspdf_cf ( id_task, id_forms, format, id_template ) VALUES ( ?, ?, ?, ? ) ";

    /**
     * configuration delete query
     */
    private static final String SQL_QUERY_DELETE = "DELETE FROM workflow_task_formspdf_cf WHERE id_task = ? ";

    /**
     * configuration update query
     */
    private static final String SQL_QUERY_UPDATE = "UPDATE workflow_task_formspdf_cf SET id_task = ?, id_forms = ?, format = ?, id_template = ? WHERE id_task = ?";

    @Override
    public void insert( FormsPDFTaskConfig formsJasperTaskConfig )
    {
        @SuppressWarnings( "resource" )
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS, FormsPDFPlugin.getPlugin( ) );

        int nIndex = 0;
        daoUtil.setInt( ++nIndex, formsJasperTaskConfig.getIdTask( ) );
        daoUtil.setInt( ++nIndex, formsJasperTaskConfig.getIdForms( ) );
        daoUtil.setString( ++nIndex, formsJasperTaskConfig.getFormat( ) );
        daoUtil.setInt( ++nIndex, formsJasperTaskConfig.getIdTemplate() );

        daoUtil.executeUpdate( );
        if ( daoUtil.nextGeneratedKey( ) )
        {
            formsJasperTaskConfig.setIdTask( daoUtil.getGeneratedKeyInt( 1 ) );
        }
        daoUtil.free( );
    }

    @Override
    public void store( FormsPDFTaskConfig formsJasperTaskConfig )
    {
        @SuppressWarnings( "resource" )
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, FormsPDFPlugin.getPlugin( ) );

        int nIndex = 0;
        daoUtil.setInt( ++nIndex, formsJasperTaskConfig.getIdTask( ) );
        daoUtil.setInt( ++nIndex, formsJasperTaskConfig.getIdForms( ) );
        daoUtil.setString( ++nIndex, formsJasperTaskConfig.getFormat( ) );
        daoUtil.setInt( ++nIndex, formsJasperTaskConfig.getIdTemplate() );
        daoUtil.setInt( ++nIndex, formsJasperTaskConfig.getIdTask( ) );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    @Override
    public FormsPDFTaskConfig load( int nIdTask )
    {
        @SuppressWarnings( "resource" )
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, FormsPDFPlugin.getPlugin( ) );
        daoUtil.setInt( 1, nIdTask );
        daoUtil.executeQuery( );

        FormsPDFTaskConfig formsPDFTaskConfig = null;

        if ( daoUtil.next( ) )
        {
            formsPDFTaskConfig = new FormsPDFTaskConfig( );

            formsPDFTaskConfig.setIdTask( daoUtil.getInt( "id_task" ) );
            formsPDFTaskConfig.setIdForms( daoUtil.getInt( "id_forms" ) );
            formsPDFTaskConfig.setFormat( daoUtil.getString( "format" ) );
            formsPDFTaskConfig.setIdTemplate( daoUtil.getInt( "id_template" ) );
        }

        daoUtil.free( );
        return formsPDFTaskConfig;
    }

    @Override
    public void delete( int nIdTask )
    {
        @SuppressWarnings( "resource" )
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, FormsPDFPlugin.getPlugin( ) );
        daoUtil.setInt( 1, nIdTask );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

}
