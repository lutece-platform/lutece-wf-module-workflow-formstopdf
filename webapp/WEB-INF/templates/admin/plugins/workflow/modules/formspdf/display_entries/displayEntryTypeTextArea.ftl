<#--
Macro: displayEntryTypeTextArea
Description: Display an entry of type text area
Parameters: entry, list_responses
-->
<#macro displayEntryTypeTextArea entry, list_responses >
		<#if list_responses?has_content>
				<#list list_responses as response>
					${response.toStringValueResponse!?replace("\n", "<br>")}
				</#list>
			</#if>
</#macro>
