<#--
Macro: displayEntryTypeTermsOfService
Description: Display an entry of type "Terms of Service" with a link to the accepted terms of service
Parameters: entry, list_responses
-->
<#macro displayEntryTypeTermsOfService entry, list_responses >
<#if getResponseContainingTheFieldWithCode( list_responses, "tos" )?? >
	<#assign responseTos = getResponseContainingTheFieldWithCode( list_responses, "tos" ) >
</#if>
<#if getResponseContainingTheFieldWithCode( list_responses, "agreement" )?? >
	<#assign responseAgreement = getResponseContainingTheFieldWithCode( list_responses, "agreement" ) >
</#if>
<#if responseAgreement?? && responseAgreement.toStringValueResponse == "true">
	<@link href='${base_url!""}jsp/site/RunStandaloneApp.jsp?page=formsTermsOfService&view=acceptedTermsOfService&id_response=${(responseTos.idResponse)!}' params='onclick="javascript:openFormsTermsOfService(this.href); return false;" target="_blank"'>#i18n{forms.entryType.termsOfService.readOnly.link}</@link>
<#else>
	<@span class='text-danger'>#i18n{forms.entryType.termsOfService.readOnly.unknown}</@span>
</#if>
</#macro>
