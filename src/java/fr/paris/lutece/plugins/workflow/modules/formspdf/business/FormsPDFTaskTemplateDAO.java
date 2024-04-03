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
import java.util.ArrayList;
import java.util.List;

import fr.paris.lutece.plugins.workflow.modules.formspdf.service.FormsPDFPlugin;
import fr.paris.lutece.plugins.workflow.utils.WorkflowUtils;
import fr.paris.lutece.util.sql.DAOUtil;

public class FormsPDFTaskTemplateDAO implements IFormsPDFTaskTemplateDAO {
	
	 private static final String SQL_QUERY_SELECTALL = "SELECT id_template, name, id_form, is_generic, content, is_rte FROM workflow_task_formspdf_template";

	 private static final String SQL_QUERY_SELECT = SQL_QUERY_SELECTALL + " WHERE id_template = ?";
	 
	 private static final String SQL_QUERY_SELECT_WITH_FORM = "SELECT worklow_template.id_template, worklow_template.name, worklow_template.id_form, form.title, worklow_template.is_generic, worklow_template.content, worklow_template.is_rte FROM workflow_task_formspdf_template worklow_template"
	 		+ " LEFT JOIN forms_form form ON form.id_form = worklow_template.id_form";
	 
	 private static final String SQL_QUERY_SELECT_BY_ID_FORM_OR_GENERIC = SQL_QUERY_SELECTALL + " WHERE id_form = ? OR is_generic = true";
	 
	 private static final String SQL_QUERY_INSERT = "INSERT INTO workflow_task_formspdf_template ( name, id_form, is_generic, content, is_rte ) VALUES ( ?, ?, ?, ?, ? ) ";
	 
	 private static final String SQL_QUERY_DELETE = "DELETE FROM workflow_task_formspdf_template WHERE id_template = ? ";
	 
	 private static final String SQL_QUERY_UPDATE = "UPDATE workflow_task_formspdf_template SET name = ?, id_form = ?, is_generic = ?, content = ?, is_rte = ? WHERE id_template = ?";
	 @Override
	 public void insert(FormsPDFTaskTemplate formsPDFTaskTemplate)
	 {
		try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS, FormsPDFPlugin.getPlugin( ) ) )
        {
			int nIndex = 0;
			daoUtil.setString( ++nIndex, formsPDFTaskTemplate.getName() );
			daoUtil.setInt( ++nIndex, formsPDFTaskTemplate.getIdForm());
			daoUtil.setBoolean( ++nIndex, formsPDFTaskTemplate.isGeneric());
			daoUtil.setString( ++nIndex, formsPDFTaskTemplate.getContent() );
			daoUtil.setBoolean( ++nIndex, formsPDFTaskTemplate.isRte() );
			
			daoUtil.executeUpdate( );
			
			if ( daoUtil.nextGeneratedKey( ) )
			{
				formsPDFTaskTemplate.setId( daoUtil.getGeneratedKeyInt( 1 ) );
			}
        }
	 }
	 
	 @Override
	 public void store(FormsPDFTaskTemplate formsPDFTaskTemplate)
	 {
		 try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, FormsPDFPlugin.getPlugin( ) ))
		 {

			 int nIndex = 0;
			 daoUtil.setString(++nIndex, formsPDFTaskTemplate.getName());
			 daoUtil.setInt(++nIndex, formsPDFTaskTemplate.getIdForm());
			 daoUtil.setBoolean(++nIndex, formsPDFTaskTemplate.isGeneric());
			 daoUtil.setString(++nIndex, formsPDFTaskTemplate.getContent());
			 daoUtil.setBoolean(++nIndex, formsPDFTaskTemplate.isRte());

			 daoUtil.setInt(++nIndex, formsPDFTaskTemplate.getId());

			 daoUtil.executeUpdate();
		 }
	 }
	 
	 @Override
	 public FormsPDFTaskTemplate load( int nIdTemplate )
	 {
		try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, FormsPDFPlugin.getPlugin( ) ))
		{
			daoUtil.setInt(1, nIdTemplate);
			daoUtil.executeQuery();

			FormsPDFTaskTemplate formsPDFTaskTemplate = null;

			if (daoUtil.next())
			{
				formsPDFTaskTemplate = dataToObject(daoUtil);
			}

			return formsPDFTaskTemplate;
		}
	}
	
	@Override
	public List<FormsPDFTaskTemplate> loadByIdFormPlusGenerics( int nIdForm )
	{
		List<FormsPDFTaskTemplate> listFormsPDFTaskTemplate = new ArrayList<>();
		
		try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_ID_FORM_OR_GENERIC, FormsPDFPlugin.getPlugin( ) ))
		{
			daoUtil.setInt(1, nIdForm);

			daoUtil.executeQuery();
			while (daoUtil.next())
			{
				listFormsPDFTaskTemplate.add(dataToObject(daoUtil));
			}
		}
		return listFormsPDFTaskTemplate;
	}
	 
	@Override
	public void delete( int nIdTemplate )
	{
	    try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, FormsPDFPlugin.getPlugin( ) ))
		{
			daoUtil.setInt( 1, nIdTemplate );
			daoUtil.executeUpdate( );
		}
	}
	
	@Override
	public List<FormsPDFTaskTemplate> selectAll()
	{
		List<FormsPDFTaskTemplate> listFormsPDFTaskTemplate = new ArrayList<>();

		try (	DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, FormsPDFPlugin.getPlugin( ) ))
		{
			daoUtil.executeQuery();
			while (daoUtil.next())
			{
				listFormsPDFTaskTemplate.add(dataToObject(daoUtil));
			}
		}
		return listFormsPDFTaskTemplate;
	}
	
	@Override
	public List<FormsPDFTaskTemplateDTO> selectAllWithForms()
	{
		List<FormsPDFTaskTemplateDTO> listFormsPDFTaskTemplateDto = new ArrayList<>();
		
		try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_WITH_FORM, WorkflowUtils.getPlugin( ) ) )
        {
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
            	listFormsPDFTaskTemplateDto.add( dataToObjectDto( daoUtil ) );
            }
        }
        return listFormsPDFTaskTemplateDto;
	}
	
	private FormsPDFTaskTemplateDTO dataToObjectDto(DAOUtil daoUtil)
	{
		FormsPDFTaskTemplate formsPDFTaskTemplate = dataToObject(daoUtil);
		FormsPDFTaskTemplateDTO formsPDFTaskTemplateDto = new FormsPDFTaskTemplateDTO();
		formsPDFTaskTemplateDto.setId(formsPDFTaskTemplate.getId());
		formsPDFTaskTemplateDto.setName(formsPDFTaskTemplate.getName());
		formsPDFTaskTemplateDto.setGeneric(formsPDFTaskTemplate.isGeneric());
		formsPDFTaskTemplateDto.setIdForm(formsPDFTaskTemplate.getIdForm());
		formsPDFTaskTemplateDto.setContent(formsPDFTaskTemplate.getContent());
		formsPDFTaskTemplateDto.setRte(formsPDFTaskTemplate.isRte());
		formsPDFTaskTemplateDto.setFormTitle(daoUtil.getString( "title" ));
		
		return formsPDFTaskTemplateDto;
	}
	
	private FormsPDFTaskTemplate dataToObject(DAOUtil daoUtil)
	{
		FormsPDFTaskTemplate formsPDFTaskTemplate = new FormsPDFTaskTemplate( );

		formsPDFTaskTemplate.setId( daoUtil.getInt( "id_template" ) );
		formsPDFTaskTemplate.setName( daoUtil.getString( "name" ) );
		formsPDFTaskTemplate.setIdForm(daoUtil.getInt( "id_form" ) );
		formsPDFTaskTemplate.setGeneric(daoUtil.getBoolean( "is_generic" ) );
		formsPDFTaskTemplate.setContent( daoUtil.getString( "content" ) );
		formsPDFTaskTemplate.setRte( daoUtil.getBoolean( "is_rte" ) );
		
		return formsPDFTaskTemplate;
	}
}
