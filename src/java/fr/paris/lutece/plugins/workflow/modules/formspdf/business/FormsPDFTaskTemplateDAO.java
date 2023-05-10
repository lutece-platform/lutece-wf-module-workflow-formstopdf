package fr.paris.lutece.plugins.workflow.modules.formspdf.business;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import fr.paris.lutece.plugins.workflow.modules.formspdf.service.FormsPDFPlugin;
import fr.paris.lutece.util.sql.DAOUtil;

public class FormsPDFTaskTemplateDAO implements IFormsPDFTaskTemplateDAO {
	
	 private static final String SQL_QUERY_SELECTALL = "SELECT id_template, name, content FROM workflow_task_formspdf_template";

	 private static final String SQL_QUERY_SELECT = SQL_QUERY_SELECTALL + " WHERE id_template = ?";
	 
	 private static final String SQL_QUERY_INSERT = "INSERT INTO workflow_task_formspdf_template ( name, content ) VALUES ( ?, ? ) ";
	 
	 private static final String SQL_QUERY_DELETE = "DELETE FROM workflow_task_formspdf_template WHERE id_template = ? ";
	 
	 private static final String SQL_QUERY_UPDATE = "UPDATE workflow_task_formspdf_template SET name = ?, content = ? WHERE id_template = ?";
	 
	 @Override
	 public void insert(FormsPDFTaskTemplate formsPDFTaskTemplate)
	 {
		try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS, FormsPDFPlugin.getPlugin( ) ) )
        {
			int nIndex = 0;
			daoUtil.setString( ++nIndex, formsPDFTaskTemplate.getName() );
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
	
	private FormsPDFTaskTemplate dataToObject(DAOUtil daoUtil)
	{
		FormsPDFTaskTemplate formsPDFTaskTemplate = new FormsPDFTaskTemplate( );

		formsPDFTaskTemplate.setId( daoUtil.getInt( "id_template" ) );
		formsPDFTaskTemplate.setName( daoUtil.getString( "name" ) );
		formsPDFTaskTemplate.setContent( daoUtil.getString( "content" ) );
		
		return formsPDFTaskTemplate;
	}
}
