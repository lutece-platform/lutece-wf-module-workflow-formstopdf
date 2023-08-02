package fr.paris.lutece.plugins.workflow.modules.formspdf.business;

public class FormsPDFTaskTemplateDTO extends FormsPDFTaskTemplate {
	
	private String _strFormTitle;
	
	public FormsPDFTaskTemplateDTO() {
		super();
	}

	public String getFormTitle() {
		return _strFormTitle;
	}

	public void setFormTitle(String strFormTitle) {
		this._strFormTitle = strFormTitle;
	}

}
