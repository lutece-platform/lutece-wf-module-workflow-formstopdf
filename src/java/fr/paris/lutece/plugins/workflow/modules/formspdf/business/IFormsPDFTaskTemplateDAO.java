package fr.paris.lutece.plugins.workflow.modules.formspdf.business;

import java.util.List;

public interface IFormsPDFTaskTemplateDAO {

	void insert(FormsPDFTaskTemplate formsPDFTaskTemplate);

	void store(FormsPDFTaskTemplate formsPDFTaskTemplate);

	FormsPDFTaskTemplate load(int nIdTemplate);

	void delete(int nIdTemplate);

	List<FormsPDFTaskTemplate> selectAll();

}
