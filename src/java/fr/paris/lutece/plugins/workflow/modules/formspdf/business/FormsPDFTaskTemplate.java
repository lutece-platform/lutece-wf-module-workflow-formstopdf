package fr.paris.lutece.plugins.workflow.modules.formspdf.business;

public class FormsPDFTaskTemplate {

	private int _nId;

	private String _strName;
	
	private int _nIdForm;
	
	private boolean _bGeneric;

	private String _strContent;
	
	public int getId() {
		return _nId;
	}

	public void setId(int nId) {
		this._nId = nId;
	}

	public String getName() {
		return _strName;
	}

	public void setName(String strName) {
		this._strName = strName;
	}

	public String getContent() {
		return _strContent;
	}

	public void setContent(String strContent) {
		this._strContent = strContent;
	}

	public int getIdForm() {
		return _nIdForm;
	}

	public void setIdForm(int nIdForm) {
		this._nIdForm = nIdForm;
	}

	public boolean isGeneric() {
		return _bGeneric;
	}

	public void setGeneric(boolean bGeneric) {
		this._bGeneric = bGeneric;
	}

}
