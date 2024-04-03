<#--
Macro: displayEntryTypeTelephone
Description: Display an entry of type Telephone
Parameters: entry, list_responses
-->
<#macro displayEntryTypeTelephone entry, list_responses >
	<div style="display: flex; flex-wrap: wrap;">
		<div style="flex: 0 0 75%; max-width: 75%;">
			<#if list_responses?has_content>
				<#list list_responses as response>
					<p>${response.toStringValueResponse!''}</p>
				</#list>
			</#if>
	</div>
</#macro>
