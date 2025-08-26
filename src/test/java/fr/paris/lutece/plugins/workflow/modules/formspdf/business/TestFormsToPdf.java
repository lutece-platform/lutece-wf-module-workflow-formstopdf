package fr.paris.lutece.plugins.workflow.modules.formspdf.business;


import fr.paris.lutece.plugins.workflowcore.business.config.ITaskConfigDAO;
import fr.paris.lutece.test.LuteceTestCase;

public class TestFormsToPdf extends LuteceTestCase 
{
	private ITaskConfigDAO<FormsPDFTaskConfig> _formsPDFTaskConfigDAO = new FormsPDFTaskConfigDAO( );
	private IFormsPDFTaskTemplateDAO _formsPDFTaskTemplateDAO = new FormsPDFTaskTemplateDAO( );

	public void testIsActiveWhenActivationIsTrue( )
    {
	  	 // default test for CI
        assert( true );
    }

	//Test operations on workflow_task_formspdf_cf table
	public void testFormsPDFConfigTableCRUD( )
	{
		FormsPDFTaskConfig formsPDFTaskConfig = new FormsPDFTaskConfig( );
	    formsPDFTaskConfig.setIdForms( 1 );
		formsPDFTaskConfig.setFormat( "format1" );
		formsPDFTaskConfig.setIdTemplate( 2 );

		//Test insert/load
		_formsPDFTaskConfigDAO.insert( formsPDFTaskConfig );		
		FormsPDFTaskConfig formsPDFTaskConfigLoaded = _formsPDFTaskConfigDAO.load( formsPDFTaskConfig.getIdTask( ) );

		assertEquals( formsPDFTaskConfig.getIdForms( ), formsPDFTaskConfigLoaded.getIdForms( ) );
		assertEquals( formsPDFTaskConfig.getFormat( ), formsPDFTaskConfigLoaded.getFormat( ) );
		assertEquals( formsPDFTaskConfig.getIdTemplate( ), formsPDFTaskConfigLoaded.getIdTemplate( ) );

	    //Test update
		formsPDFTaskConfig.setFormat("format2");
		_formsPDFTaskConfigDAO.store( formsPDFTaskConfig );
		formsPDFTaskConfigLoaded = _formsPDFTaskConfigDAO.load( formsPDFTaskConfig.getIdTask( ) );

		assertEquals( formsPDFTaskConfig.getFormat( ), formsPDFTaskConfigLoaded.getFormat( ) );

		//Test delete
		_formsPDFTaskConfigDAO.delete( formsPDFTaskConfig.getIdTask( ) );

		assertNull( _formsPDFTaskConfigDAO.load( formsPDFTaskConfig.getIdTask( ) ) );
	}

	//Test CRUD operations on workflow_task_formspdf_template table
	public void testFormsPDFTemplateTableCRUD( )
	{
		FormsPDFTaskTemplate formsPDFTaskTemplate = new FormsPDFTaskTemplate( );
		formsPDFTaskTemplate.setName( "template1" );
		formsPDFTaskTemplate.setContent( "content template1" );
		formsPDFTaskTemplate.setIdForm( 1 );
	    formsPDFTaskTemplate.setGeneric (true );
		formsPDFTaskTemplate.setRte( false );

		//Test insert/load
		_formsPDFTaskTemplateDAO.insert( formsPDFTaskTemplate );		
		FormsPDFTaskTemplate formsPDFTaskTemplateLoaded = _formsPDFTaskTemplateDAO.load( formsPDFTaskTemplate.getId( ) );

		assertEquals( formsPDFTaskTemplate.getName( ), formsPDFTaskTemplateLoaded.getName( ) );
		assertEquals( formsPDFTaskTemplate.getContent( ), formsPDFTaskTemplateLoaded.getContent( ) );
		assertEquals( formsPDFTaskTemplate.getIdForm( ), formsPDFTaskTemplateLoaded.getIdForm( ) );
		assertEquals( formsPDFTaskTemplate.isGeneric( ), formsPDFTaskTemplateLoaded.isGeneric( ) );
		assertEquals( formsPDFTaskTemplate.isRte( ), formsPDFTaskTemplateLoaded.isRte( ) );

		//Test update
		formsPDFTaskTemplate.setName("template2");
		_formsPDFTaskTemplateDAO.store( formsPDFTaskTemplate );
		formsPDFTaskTemplateLoaded = _formsPDFTaskTemplateDAO.load( formsPDFTaskTemplate.getId( ) );

		assertEquals( formsPDFTaskTemplate.getName( ), formsPDFTaskTemplateLoaded.getName( ) );

		//Test delete
		_formsPDFTaskTemplateDAO.delete( formsPDFTaskTemplate.getId( ) );

		assertNull( _formsPDFTaskTemplateDAO.load( formsPDFTaskTemplate.getId( ) ) );
	} 
}