package fr.paris.lutece.plugins.workflow.modules.formspdf.business;

public class FormsPDFTaskTemplate {

	private int _nId;

	private String _strName;

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

}
