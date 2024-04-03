<#--
Macro: displayEntryTypeDate
Description: Display an entry of type date
Parameters: entry, list_responses
-->
<#macro displayEntryTypeDate entry, list_responses >
			<#if list_responses?has_content>
				<#list list_responses as response>
					<#if response.responseValue??>
						<p>${response.responseValue?number?number_to_date}</p>
					</#if>
				</#list>
			</#if>
</#macro>
