package fr.paris.lutece.plugins.workflow.modules.formspdf.service.task;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import fr.paris.lutece.plugins.workflowcore.business.task.ITaskType;
import fr.paris.lutece.plugins.workflowcore.business.task.TaskType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

@ApplicationScoped
public class TaskTypeFormsPDFProducer 
{
	@Produces
    @ApplicationScoped
    @Named( "workflow-formspdf.taskTypeFormsPDFTask" )
    public ITaskType produceTaskTypeFormsPDFTask( 
    		@ConfigProperty( name = "workflow-formspdf.taskTypeFormsPDFTask.key" ) String key,
            @ConfigProperty( name = "workflow-formspdf.taskTypeFormsPDFTask.titleI18nKey" ) String titleI18nKey,
            @ConfigProperty( name = "workflow-formspdf.taskTypeFormsPDFTask.beanName" ) String beanName,
            @ConfigProperty( name = "workflow-formspdf.taskTypeFormsPDFTask.configBeanName" ) String configBeanName,
            @ConfigProperty( name = "workflow-formspdf.taskTypeFormsPDFTask.configRequired", defaultValue = "false" ) boolean configRequired,
            @ConfigProperty( name = "workflow-formspdf.taskTypeFormsPDFTask.formTaskRequired", defaultValue = "false" ) boolean formTaskRequired,
            @ConfigProperty( name = "workflow-formspdf.taskTypeFormsPDFTask.taskForAutomaticAction", defaultValue = "false" ) boolean taskForAutomaticAction )
    {
        return buildTaskType( key, titleI18nKey, beanName, configBeanName, configRequired, formTaskRequired, taskForAutomaticAction );
    }
	
	private ITaskType buildTaskType( String strKey, String strTitleI18nKey, String strBeanName, String strConfigBeanName, 
			boolean bIsConfigRequired, boolean bIsFormTaskRequired, boolean bIsTaskForAutomaticAction )
    {
        TaskType taskType = new TaskType( );
        taskType.setKey( strKey );
        taskType.setTitleI18nKey( strTitleI18nKey );
        taskType.setBeanName( strBeanName );
        taskType.setConfigBeanName( strConfigBeanName );
        taskType.setConfigRequired( bIsConfigRequired );
        taskType.setFormTaskRequired( bIsFormTaskRequired );
        taskType.setTaskForAutomaticAction( bIsTaskForAutomaticAction );
        return taskType;
    }
}