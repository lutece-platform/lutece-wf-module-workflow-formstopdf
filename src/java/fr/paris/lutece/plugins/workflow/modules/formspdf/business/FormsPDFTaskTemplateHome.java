package fr.paris.lutece.plugins.workflow.modules.formspdf.business;

import java.util.List;

import fr.paris.lutece.portal.service.spring.SpringContextService;

public final class FormsPDFTaskTemplateHome {

	private static IFormsPDFTaskTemplateDAO _dao = SpringContextService.getBean( "workflow-formspdf.formsPDFTaskTemplateDAO" );
	
	private FormsPDFTaskTemplateHome()
	{
		
	}
	
	public static FormsPDFTaskTemplate create( FormsPDFTaskTemplate formsPDFTaskTemplate )
    {
		_dao.insert(formsPDFTaskTemplate);
		return formsPDFTaskTemplate;
    }
	
	public static FormsPDFTaskTemplate update( FormsPDFTaskTemplate formsPDFTaskTemplate )
    {
		_dao.store(formsPDFTaskTemplate);
		return formsPDFTaskTemplate;
    }
	
	public static void remove( int nIdTemplate )
    {
        _dao.delete( nIdTemplate );
    }
	
	public static FormsPDFTaskTemplate findByPrimaryKey( int nIdTemplate )
	{
		return _dao.load( nIdTemplate );
	}
	
	public static List<FormsPDFTaskTemplate> findAll()
	{
		return _dao.selectAll();
	}
}
