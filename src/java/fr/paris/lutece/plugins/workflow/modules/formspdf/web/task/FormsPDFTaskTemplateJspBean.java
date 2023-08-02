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
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.util.html.HtmlTemplate;

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
    
	// Markers
    private static final String MARK_TEMPLATE_PDF_LIST = "template_pdf_list";
    private static final String MARK_FORMS_PDF_TASK_TEMPLATE = "forms_pdf_task_template";
    private static final String MARK_TASK_ID = "task_id";
    private static final String MARK_LIST_MARKERS = "list_markers";
    private static final String MARK_FORMS_LIST = "forms_list";
    
    // session fields
    private int _nIdTask;
    
    @View( value = VIEW_MANAGE_TEMPLATES, defaultView = true )
    public String getManageTemplates( HttpServletRequest request )
    {
    	Locale locale = getLocale( );
    	Map<String, Object> model = getModel( );
    	
        if (_nIdTask == 0)
        {
        	_nIdTask = NumberUtils.toInt( request.getParameter(PARAMETER_TASK_ID), DEFAULT_ID_VALUE);
        }
        model.put(MARK_TASK_ID, _nIdTask);
        
        model.put(MARK_TEMPLATE_PDF_LIST, FormsPDFTaskTemplateHome.findAllWithFormTitles());
    	
    	HtmlTemplate templateList = AppTemplateService.getTemplate( TEMPLATE_MANAGE_FORMS_PDF_TEMPLATES, locale, model );
    	return getAdminPage( templateList.getHtml( ) );
    }
    
    @View( value = VIEW_MODIFY_TEMPLATE )
    public String getModifyTemplate( HttpServletRequest request )
    {
    	Locale locale = getLocale( );
    	Map<String, Object> model = getModel( );
    	
    	FormsPDFTaskTemplate formsPDFTaskTemplate = null;
    	int nIdTemplate = NumberUtils.toInt( request.getParameter( PARAMETER_TEMPLATE_ID ), DEFAULT_ID_VALUE );
    	if (nIdTemplate > 0)
    	{
    		formsPDFTaskTemplate = FormsPDFTaskTemplateHome.findByPrimaryKey(nIdTemplate);
    	} else {
    		formsPDFTaskTemplate = new FormsPDFTaskTemplate();
    		formsPDFTaskTemplate.setGeneric(true);
    	}
    	model.put(MARK_FORMS_PDF_TASK_TEMPLATE, formsPDFTaskTemplate);
    	
    	model.put( MARK_FORMS_LIST, FormHome.getFormsReferenceList( ) );
    	
    	// markers
    	Form form = FormHome.findByPrimaryKey( formsPDFTaskTemplate.getIdForm());
    	model.put(MARK_LIST_MARKERS, GenericFormsProvider.getProviderMarkerDescriptions(form != null ? form : new Form()));
    	
    	HtmlTemplate templateList = AppTemplateService.getTemplate( TEMPLATE_MODIFY_FORMS_PDF_TEMPLATE, locale, model );
    	return getAdminPage( templateList.getHtml( ) );
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
		
		return formsPDFTaskTemplateToEdit;
    }

}
