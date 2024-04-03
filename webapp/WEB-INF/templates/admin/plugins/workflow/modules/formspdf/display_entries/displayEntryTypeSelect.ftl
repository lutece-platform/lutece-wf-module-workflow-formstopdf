<#--
Macro: displayEntryTypeSelect
Description: Display an entry of type select
Parameters: entry, list_responses
-->
<#macro displayEntryTypeSelect entry, list_responses >
		<div style="flex: 0 0 75%; max-width: 75%;"><#if list_responses?has_content><#list list_responses as response><p><#if response.field??>${response.field.title!''}</#if></p></#list></#if></div>
</#macro>
