package fr.paris.lutece.plugins.workflow.modules.formspdf.service.provider;

import javax.servlet.http.HttpServletRequest;

import fr.paris.lutece.plugins.forms.service.provider.GenericFormsProvider;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceHistory;

public class FormsProvider extends GenericFormsProvider {

	public FormsProvider(ResourceHistory resourceHistory, HttpServletRequest request) {
		super(resourceHistory, request);
	}

	@Override
	public String provideDemandTypeId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String provideDemandReference() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String provideCustomerConnectionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String provideCustomerId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String provideCustomerEmail() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String provideCustomerMobilePhone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String provideSmsSender() {
		// TODO Auto-generated method stub
		return null;
	}

}
