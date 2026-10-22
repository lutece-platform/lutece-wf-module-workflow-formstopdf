package fr.paris.lutece.plugins.workflow.modules.formspdf.web.task;

import java.lang.reflect.Proxy;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.workflow.modules.formspdf.business.FormsPDFTaskConfig;
import fr.paris.lutece.plugins.workflow.modules.formspdf.business.FormsPDFTaskTemplate;
import fr.paris.lutece.plugins.workflow.modules.formspdf.business.FormsPDFTaskTemplateHome;
import fr.paris.lutece.plugins.workflowcore.service.config.ITaskConfigService;
import fr.paris.lutece.plugins.workflowcore.service.task.ITask;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.test.mocks.MockHttpServletRequest;

/**
 * Information displayed in the history of a resource by the PDF generation task
 */
public class FormsPDFTaskComponentTest extends LuteceTestCase
{
    private static final int ID_TASK_WITH_TEMPLATE = 99901;
    private static final int ID_TASK_WITHOUT_CONFIG = 99902;

    /**
     * The history tells that a PDF was generated, and with which template (escaped)
     */
    @Test
    public void testHistoryShowsTheGeneration( )
    {
        FormsPDFTaskTemplate template = new FormsPDFTaskTemplate( );
        template.setName( "Receipt <b>" );
        template.setContent( "content" );
        template.setIdForm( 1 );
        template.setGeneric( true );
        FormsPDFTaskTemplateHome.create( template );

        FormsPDFTaskConfig config = new FormsPDFTaskConfig( );
        config.setIdTask( ID_TASK_WITH_TEMPLATE );
        config.setIdTemplate( template.getId( ) );
        FormsPDFTaskComponent component = new FormsPDFTaskComponent( null, newConfigService( config ) );

        try
        {
            String strInformation = component.getDisplayTaskInformation( 1, new MockHttpServletRequest( ), Locale.ENGLISH, newTask( ID_TASK_WITH_TEMPLATE ) );
            assertNotNull( strInformation );
            assertTrue( strInformation.contains( "Receipt &lt;b&gt;" ), strInformation );

            assertNotNull( component.getDisplayTaskInformation( 1, new MockHttpServletRequest( ), Locale.ENGLISH, newTask( ID_TASK_WITHOUT_CONFIG ) ) );
        }
        finally
        {
            FormsPDFTaskTemplateHome.remove( template.getId( ) );
        }
    }

    /**
     * Builds a configuration service knowing only the given configuration
     *
     * @param config
     *            the configuration
     * @return the configuration service
     */
    private static ITaskConfigService newConfigService( FormsPDFTaskConfig config )
    {
        return (ITaskConfigService) Proxy.newProxyInstance( FormsPDFTaskComponentTest.class.getClassLoader( ), new Class<?> [ ] {
                ITaskConfigService.class
        }, ( proxy, method, args ) -> "findByPrimaryKey".equals( method.getName( ) ) && config.getIdTask( ) == (Integer) args [0] ? config : null );
    }

    /**
     * Builds a task with the given id
     *
     * @param nIdTask
     *            the task id
     * @return the task
     */
    private static ITask newTask( int nIdTask )
    {
        return (ITask) Proxy.newProxyInstance( FormsPDFTaskComponentTest.class.getClassLoader( ), new Class<?> [ ] {
                ITask.class
        }, ( proxy, method, args ) -> "getId".equals( method.getName( ) ) ? nIdTask : null );
    }
}
