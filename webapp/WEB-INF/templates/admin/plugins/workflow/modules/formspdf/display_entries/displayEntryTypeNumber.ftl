<#--
Macro: displayEntryTypeNumber
Description: Display an entry of type number
Parameters: entry, list_responses
-->
<#macro displayEntryTypeNumber entry, list_responses >
	<div style="display: flex; flex-wrap: wrap;">
			<#if list_responses?has_content>
				<#list list_responses as response>
						<p>${response.toStringValueResponse!''}</p>
				</#list>
			</#if>
</#macro>
