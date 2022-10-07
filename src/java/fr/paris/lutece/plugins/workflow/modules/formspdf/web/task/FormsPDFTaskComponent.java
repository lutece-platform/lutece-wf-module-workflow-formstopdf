/*
 * Copyright (c) 2002-2022, City of Paris
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
package fr.paris.lutece.plugins.workflow.modules.formspdf.web.task;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import fr.paris.lutece.plugins.forms.business.FormHome;
import fr.paris.lutece.plugins.workflow.modules.formspdf.business.FormsPDFTaskConfig;
import fr.paris.lutece.plugins.workflow.web.task.AbstractTaskComponent;
import fr.paris.lutece.plugins.workflowcore.service.task.ITask;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.html.HtmlTemplate;

/**
 * This class represents a component for the task {@link fr.paris.lutece.plugins.workflow.modules.formspdf.service.task.FormsPDFTask FormsPDFTask}
 *
 */
public class FormsPDFTaskComponent extends AbstractTaskComponent
{

    /**
     * the marker used for the task configuration
     */
    private static final String MARK_CONFIG = "config";

    private static final String MARK_TEMPLATE_PDF_LIST = "template_pdf_list";

    /**
     * the marker used for the forms list
     */
    private static final String MARK_FORMS_LIST = "forms_list";

    /**
     * the marker used for the list of compatible formats to generate reports
     */
    private static final String MARK_FORMATS_LIST = "formats_list";

    /**
     * the list of compatible formats to generate reports
     */
    private static final String PROPERTY_LIST_FORMATS = "workflow-formspdf.task_formspdf_config.list_formats";

    /**
     * Freemarker template of the task configuration vue
     */
    private static final String TEMPLATE_CONFIG_GLOBAL_FORMSPDF = "admin/plugins/workflow/modules/formspdf/global_formspdf_task_config.html";

    @Override
    public String getDisplayConfigForm( HttpServletRequest request, Locale locale, ITask task )
    {
        Map<String, Object> model = new HashMap<>( );
        FormsPDFTaskConfig config = getTaskConfigService( ).findByPrimaryKey( task.getId( ) );
        model.put( MARK_CONFIG, config );

        model.put( MARK_FORMS_LIST, FormHome.getFormsReferenceList( ) );
        
        String [ ] arrayListFormats = AppPropertiesService.getProperty( PROPERTY_LIST_FORMATS, "pdf" ).split( "," );
        ReferenceList listFormats = new ReferenceList( );
        for ( String strFormat : arrayListFormats )
        {
            ReferenceItem itemFormat = new ReferenceItem( );
            itemFormat.setCode( strFormat );
            itemFormat.setName( strFormat );
            listFormats.add( itemFormat );
        }

        model.put( MARK_FORMATS_LIST, listFormats );
        
        ReferenceList listTemplatePDF = new ReferenceList( );
        
        
        Properties allProps = AppPropertiesService.getProperties( );
        Enumeration<?> enumKeys = allProps.propertyNames( );
		
		while (enumKeys.hasMoreElements())
		{
			String name = (String) enumKeys.nextElement();
			if ( name.contains("template_pdf") && name.contains("name"))
        	{
				String[] strClassName = name.split(".name");
				StringBuilder strClassLocation = new StringBuilder(strClassName[0]);
				strClassLocation.append( ".path" );
				listTemplatePDF.addItem( AppPropertiesService.getProperty(strClassLocation.toString()), AppPropertiesService.getProperty(name) );
        	}
		}
        
        model.put( MARK_TEMPLATE_PDF_LIST, listTemplatePDF );

        HtmlTemplate page = AppTemplateService.getTemplate( TEMPLATE_CONFIG_GLOBAL_FORMSPDF, locale, model );
        return page.getHtml( );
    }

    @Override
    public String getDisplayTaskInformation( int nIdHistory, HttpServletRequest request, Locale locale, ITask task )
    {
        return null;
    }


    @Override
    public String doValidateTask( int nIdResource, String strResourceType, HttpServletRequest request, Locale locale, ITask task )
    {
        return null;
    }

	@Override
	public String getDisplayTaskForm(int nIdResource, String strResourceType, HttpServletRequest request, Locale locale,
			ITask task) {
		// TODO Auto-generated method stub
		return null;
	}
}
