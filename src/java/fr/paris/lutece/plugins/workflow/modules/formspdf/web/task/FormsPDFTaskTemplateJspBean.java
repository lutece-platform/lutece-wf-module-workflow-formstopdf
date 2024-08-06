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
package fr.paris.lutece.plugins.workflow.modules.formspdf.web.task;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.math.NumberUtils;

import fr.paris.lutece.plugins.forms.business.Form;
import fr.paris.lutece.plugins.forms.business.FormHome;
import fr.paris.lutece.plugins.forms.service.provider.GenericFormsProvider;
import fr.paris.lutece.plugins.workflow.modules.formspdf.business.FormsPDFTaskTemplate;
import fr.paris.lutece.plugins.workflow.modules.formspdf.business.FormsPDFTaskTemplateHome;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;

@Controller( controllerJsp = "ManageTemplates.jsp", controllerPath = "jsp/admin/plugins/workflow/modules/formspdf/", right = "WORKFLOW_MANAGEMENT" )
public class FormsPDFTaskTemplateJspBean extends MVCAdminJspBean{

	private static final long serialVersionUID = 1L;
	
	public static final int DEFAULT_ID_VALUE = NumberUtils.BYTE_MINUS_ONE;
	
	// Templates
	private static final String TEMPLATE_MANAGE_FORMS_PDF_TEMPLATES = "/admin/plugins/workflow/modules/formspdf/manage_forms_pdf_templates.html";
	private static final String TEMPLATE_MODIFY_FORMS_PDF_TEMPLATE = "/admin/plugins/workflow/modules/formspdf/modify_forms_pdf_template.html";
	
	// Views
    private static final String VIEW_MANAGE_TEMPLATES = "manageTemplates";
    private static final String VIEW_MODIFY_TEMPLATE = "modifyTemplate";
    
    // Actions
    private static final String ACTION_MODIFY_TEMPLATE = "modifyTemplate";
    private static final String ACTION_REMOVE_TEMPLATE = "removeTemplate";
    
    // Parameters
    private static final String PARAMETER_TASK_ID = "task_id";
    private static final String PARAMETER_TEMPLATE_ID = "template_id";
    private static final String PARAMETER_TEMPLATE_NAME = "template_name";
    private static final String PARAMETER_TEMPLATE_ID_FORM = "template_id_form";
    private static final String PARAMETER_TEMPLATE_ASSOCIATE_FORM = "template_associate_form";
    private static final String PARAMETER_TEMPLATE_CONTENT = "template_content";
	private static final String PARAMETER_RICH_TEXT_EDITOR = "rte";

	// Markers
	private static final String MARK_RICH_TEXT_EDITOR = "rte";
    private static final String MARK_TEMPLATE_PDF_LIST = "template_pdf_list";
    private static final String MARK_FORMS_PDF_TASK_TEMPLATE = "forms_pdf_task_template";
    private static final String MARK_TASK_ID = "task_id";
    private static final String MARK_LIST_MARKERS = "list_markers";
    private static final String MARK_FORMS_LIST = "forms_list";
    
    // session fields
    private int _nIdTask;

	//Properties

	private static final String PROPERTY_PAGE_TITLE_MANAGE_FORMS_PDF_TEMPLATES = "module.workflow.formspdf.manage.template.title";
	private static final String PROPERTY_PAGE_TITLE_MODIFY_FORMS_PDF_TEMPLATES = "module.workflow.formspdf.modify.template.title";
	
    @View( value = VIEW_MANAGE_TEMPLATES, defaultView = true )
    public String getManageTemplates( HttpServletRequest request )
    {
    	Map<String, Object> model = getModel( );
    	
        if (_nIdTask == 0)
        {
        	_nIdTask = NumberUtils.toInt( request.getParameter(PARAMETER_TASK_ID), DEFAULT_ID_VALUE);
        }
        model.put(MARK_TASK_ID, _nIdTask);
        
        model.put(MARK_TEMPLATE_PDF_LIST, FormsPDFTaskTemplateHome.findAllWithFormTitles());

		return getPage(PROPERTY_PAGE_TITLE_MANAGE_FORMS_PDF_TEMPLATES, TEMPLATE_MANAGE_FORMS_PDF_TEMPLATES, model);
    }
    
    @View( value = VIEW_MODIFY_TEMPLATE )
    public String getModifyTemplate( HttpServletRequest request )
    {
    	Map<String, Object> model = getModel( );
		FormsPDFTaskTemplate formsPDFTaskTemplate = null;
		int nIdTemplate = NumberUtils.toInt( request.getParameter( PARAMETER_TEMPLATE_ID ), DEFAULT_ID_VALUE );
		
    	if (nIdTemplate > 0)
    	{
    		formsPDFTaskTemplate = FormsPDFTaskTemplateHome.findByPrimaryKey(nIdTemplate);
    	} 
    	else 
    	{
    		formsPDFTaskTemplate = new FormsPDFTaskTemplate();
    		formsPDFTaskTemplate.setGeneric(true);
			formsPDFTaskTemplate.setRte(true);
    	}
		Boolean isRichTextEditor = Boolean.parseBoolean( request.getParameter( PARAMETER_RICH_TEXT_EDITOR ) );
		if(request.getParameter( PARAMETER_RICH_TEXT_EDITOR ) == null)
		{
			isRichTextEditor = formsPDFTaskTemplate.isRte();
		} 
			model.put(MARK_RICH_TEXT_EDITOR, isRichTextEditor );
		// format the content
		if (isRichTextEditor) {
			if(!formsPDFTaskTemplate.isRte()) {
				formsPDFTaskTemplate.setContent(convertMacroToSquareBrackets(formsPDFTaskTemplate.getContent()));
			}
		} else {
			if(formsPDFTaskTemplate.isRte()) {
				formsPDFTaskTemplate.setContent(convertMacroToSuppMinor(formsPDFTaskTemplate.getContent()));
			}
		}
    	model.put(MARK_FORMS_PDF_TASK_TEMPLATE, formsPDFTaskTemplate);
    	
    	model.put( MARK_FORMS_LIST, FormHome.getFormsReferenceList( ) );
    	
    	// markers
    	Form form = FormHome.findByPrimaryKey( formsPDFTaskTemplate.getIdForm( ) );
    	model.put(MARK_LIST_MARKERS, GenericFormsProvider.getProviderMarkerDescriptions( form ) );

		return getPage(PROPERTY_PAGE_TITLE_MODIFY_FORMS_PDF_TEMPLATES, TEMPLATE_MODIFY_FORMS_PDF_TEMPLATE, model);
	}
    
    @Action( value = ACTION_MODIFY_TEMPLATE )
    public String doModifyTemplate( HttpServletRequest request )
    {
    	int nIdTemplate = NumberUtils.toInt( request.getParameter( PARAMETER_TEMPLATE_ID ), DEFAULT_ID_VALUE );
    	FormsPDFTaskTemplate formsPDFTaskTemplateToEdit = FormsPDFTaskTemplateHome.findByPrimaryKey(nIdTemplate);
    	
    	if (formsPDFTaskTemplateToEdit == null)
    	{
    		formsPDFTaskTemplateToEdit = new FormsPDFTaskTemplate();
    		populateFormsPDFTaskTemplate(request, formsPDFTaskTemplateToEdit);
    		FormsPDFTaskTemplateHome.create(formsPDFTaskTemplateToEdit);
    	} else {
    		populateFormsPDFTaskTemplate(request, formsPDFTaskTemplateToEdit);
    		FormsPDFTaskTemplateHome.update(formsPDFTaskTemplateToEdit);
    	}
    	
    	return redirectView( request, VIEW_MANAGE_TEMPLATES );
    }
    
    @Action( value = ACTION_REMOVE_TEMPLATE )
    public String doRemoveTemplate( HttpServletRequest request )
    {
    	int nIdTemplate = NumberUtils.toInt( request.getParameter( PARAMETER_TEMPLATE_ID ), DEFAULT_ID_VALUE );
    	
    	if (nIdTemplate > DEFAULT_ID_VALUE)
    	{
    		FormsPDFTaskTemplateHome.remove(nIdTemplate);
    	}
    	return redirectView( request, VIEW_MANAGE_TEMPLATES );
    }
    
    /**
     * 
     * @param request
     * @param formsPDFTaskTemplateToEdit
     * @return the populated template
     */
    private FormsPDFTaskTemplate populateFormsPDFTaskTemplate(HttpServletRequest request, FormsPDFTaskTemplate formsPDFTaskTemplateToEdit)
    {
    	formsPDFTaskTemplateToEdit.setName(request.getParameter( PARAMETER_TEMPLATE_NAME ));
    	
    	boolean isAssociateForm = Boolean.parseBoolean(request.getParameter(PARAMETER_TEMPLATE_ASSOCIATE_FORM));
    	formsPDFTaskTemplateToEdit.setGeneric(!isAssociateForm);
    	if (isAssociateForm)
    	{
    		formsPDFTaskTemplateToEdit.setIdForm(NumberUtils.toInt( request.getParameter( PARAMETER_TEMPLATE_ID_FORM ), DEFAULT_ID_VALUE));
    	}
    	else
    	{
    		formsPDFTaskTemplateToEdit.setIdForm(DEFAULT_ID_VALUE);
    	}
    	
		formsPDFTaskTemplateToEdit.setContent(request.getParameter( PARAMETER_TEMPLATE_CONTENT ));
		formsPDFTaskTemplateToEdit.setRte(Boolean.parseBoolean( request.getParameter( PARAMETER_RICH_TEXT_EDITOR ) ));
		
		return formsPDFTaskTemplateToEdit;
    }

	/**
	 * Convert the macro to display responses to the usual supp and minor
	 * @param strtemplate
	 * @return the string with the macro converted
	 */
	private String convertMacroToSuppMinor(String strtemplate)
	{
		strtemplate = strtemplate.replaceAll("\\[@displayEntry q=position_(\\d+)/]", "<@displayEntry q=position_$1/>");
		return strtemplate;
	}

	/**
	 * Convert the macro to display responses to the square brackets
	 * @param strtemplate
	 * @return the string with the macro converted
	 */
	private String convertMacroToSquareBrackets(String strtemplate)
	{
		strtemplate = strtemplate.replaceAll("<@displayEntry q=position_(\\d+)/>", "[@displayEntry q=position_$1/]");
		return strtemplate;
	}

}
