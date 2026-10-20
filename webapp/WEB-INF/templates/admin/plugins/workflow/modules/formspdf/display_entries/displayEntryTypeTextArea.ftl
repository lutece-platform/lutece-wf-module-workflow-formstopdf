<#--
Macro: displayEntryTypeTextArea
Description: Display an entry of type text area
Parameters: entry, list_responses
-->
<#macro displayEntryTypeTextArea entry, list_responses >
<#assign iteration = 0>
<#if list_responses?has_content>
<#if iteration != 0 && response.toStringValueResponse?has_content>
    <@span>; </@span>
</#if>
<#assign iteration = iteration + 1>
    <#list list_responses as response>
        <#if ( getFieldValueByCode( entry, 'richtext' )!'' ) == 'true'>${richText( response.toStringValueResponse! )}<#else><#list (response.toStringValueResponse!)?split( '\n' ) as line>${line}<#sep><br></#sep></#list></#if>
    </#list>
</#if>
</#macro>
