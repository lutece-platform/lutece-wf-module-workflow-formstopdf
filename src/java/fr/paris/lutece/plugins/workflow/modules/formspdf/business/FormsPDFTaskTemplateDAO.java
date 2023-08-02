package fr.paris.lutece.plugins.workflow.modules.formspdf.business;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import fr.paris.lutece.plugins.workflow.modules.formspdf.service.FormsPDFPlugin;
import fr.paris.lutece.plugins.workflow.utils.WorkflowUtils;
import fr.paris.lutece.util.sql.DAOUtil;

public class FormsPDFTaskTemplateDAO implements IFormsPDFTaskTemplateDAO {
	
	 private static final String SQL_QUERY_SELECTALL = "SELECT id_template, name, id_form, is_generic, content FROM workflow_task_formspdf_template";

	 private static final String SQL_QUERY_SELECT = SQL_QUERY_SELECTALL + " WHERE id_template = ?";
	 
	 private static final String SQL_QUERY_SELECT_WITH_FORM = "SELECT worklow_template.id_template, worklow_template.name, worklow_template.id_form, form.title, worklow_template.is_generic, worklow_template.content FROM workflow_task_formspdf_template worklow_template"
	 		+ " LEFT JOIN forms_form form ON form.id_form = worklow_template.id_form";
	 
	 private static final String SQL_QUERY_SELECT_BY_ID_FORM_OR_GENERIC = SQL_QUERY_SELECTALL + " WHERE id_form = ? OR is_generic = true";
	 
	 private static final String SQL_QUERY_INSERT = "INSERT INTO workflow_task_formspdf_template ( name, id_form, is_generic, content ) VALUES ( ?, ?, ?, ? ) ";
	 
	 private static final String SQL_QUERY_DELETE = "DELETE FROM workflow_task_formspdf_template WHERE id_template = ? ";
	 
	 private static final String SQL_QUERY_UPDATE = "UPDATE workflow_task_formspdf_template SET name = ?, id_form = ?, is_generic = ?, content = ? WHERE id_template = ?";
	 
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
			
			daoUtil.executeUpdate( );
			
			if ( daoUtil.nextGeneratedKey( ) )
			{
				formsPDFTaskTemplate.setId( daoUtil.getGeneratedKeyInt( 1 ) );
			}
			daoUtil.free( );
        }
	 }
	 
	 @Override
	 public void store(FormsPDFTaskTemplate formsPDFTaskTemplate)
	 {
		@SuppressWarnings( "resource" )
		DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, FormsPDFPlugin.getPlugin( ) );
		
		int nIndex = 0;
		daoUtil.setString( ++nIndex, formsPDFTaskTemplate.getName( ) );
		daoUtil.setInt( ++nIndex, formsPDFTaskTemplate.getIdForm());
		daoUtil.setBoolean( ++nIndex, formsPDFTaskTemplate.isGeneric());
		daoUtil.setString( ++nIndex, formsPDFTaskTemplate.getContent( ) );
		
		daoUtil.setInt( ++nIndex, formsPDFTaskTemplate.getId( ) );
		
		daoUtil.executeUpdate( );
		daoUtil.free( );
	 }
	 
	 @Override
	 public FormsPDFTaskTemplate load( int nIdTemplate )
	 {
		@SuppressWarnings( "resource" )
		DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, FormsPDFPlugin.getPlugin( ) );
		daoUtil.setInt( 1, nIdTemplate );
		daoUtil.executeQuery( );
		
		FormsPDFTaskTemplate formsPDFTaskTemplate = null;
		
		if ( daoUtil.next( ) )
		{
			formsPDFTaskTemplate = dataToObject(daoUtil);
		}
		
		daoUtil.free( );
		return formsPDFTaskTemplate;
	}
	
	@Override
	public List<FormsPDFTaskTemplate> loadByIdFormPlusGenerics( int nIdForm )
	{
		List<FormsPDFTaskTemplate> listFormsPDFTaskTemplate = new ArrayList<>();
		
		@SuppressWarnings( "resource" )
		DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_ID_FORM_OR_GENERIC, FormsPDFPlugin.getPlugin( ) );
		daoUtil.setInt( 1, nIdForm );
		
		daoUtil.executeQuery( );
		while ( daoUtil.next( ) )
		{
			listFormsPDFTaskTemplate.add( dataToObject( daoUtil ) );
		}
		
		daoUtil.free( );
		return listFormsPDFTaskTemplate;
	}
	 
	@Override
	public void delete( int nIdTemplate )
	{
	    @SuppressWarnings( "resource" )
	    DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, FormsPDFPlugin.getPlugin( ) );
	    daoUtil.setInt( 1, nIdTemplate );
	    daoUtil.executeUpdate( );
	    daoUtil.free( );
	}
	
	@Override
	public List<FormsPDFTaskTemplate> selectAll()
	{
		List<FormsPDFTaskTemplate> listFormsPDFTaskTemplate = new ArrayList<>();
		
		@SuppressWarnings( "resource" )
	    DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, FormsPDFPlugin.getPlugin( ) );
		
		daoUtil.executeQuery( );
        while ( daoUtil.next( ) )
        {
        	listFormsPDFTaskTemplate.add( dataToObject( daoUtil ) );
        }
        daoUtil.free( );
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
            daoUtil.free( );
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
		
		return formsPDFTaskTemplate;
	}
}
