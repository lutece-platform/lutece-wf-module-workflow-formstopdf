<#--
Macro: displayEntryTypeNumbering
Description: Display an entry of type numbering
Parameters: entry, list_responses
-->
<#macro displayEntryTypeNumbering entry, list_responses >
	<div style="display: flex; flex-wrap: wrap;">
			<#if list_responses?has_content>
				<#list list_responses as response>
					<p>${response.toStringValueResponse!''}</p>
				</#list>
			</#if>
	</div>
</#macro>
