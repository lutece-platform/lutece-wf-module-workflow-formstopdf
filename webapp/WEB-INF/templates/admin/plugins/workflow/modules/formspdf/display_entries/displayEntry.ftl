<#--
Macro: displayEntry
Description: Call the macro corresponding to the entry type
Parameters: q (forms.FormQuestionResponse) - The form question response
-->
<#include "/admin/plugins/workflow/modules/formspdf/display_entries/displayEntryTypeText.ftl" />
<#include "/admin/plugins/workflow/modules/formspdf/display_entries/displayEntryTypeArray.ftl" />
<#include "/admin/plugins/workflow/modules/formspdf/display_entries/displayEntryTypeAutoFileReading.ftl" />
<#include "/admin/plugins/workflow/modules/formspdf/display_entries/displayEntryTypeCamera.ftl" />
<#include "/admin/plugins/workflow/modules/formspdf/display_entries/displayEntryTypeCartography.ftl" />
<#include "/admin/plugins/workflow/modules/formspdf/display_entries/displayEntryTypeCheckBox.ftl" />
<#include "/admin/plugins/workflow/modules/formspdf/display_entries/displayEntryTypeComment.ftl" />
<#include "/admin/plugins/workflow/modules/formspdf/display_entries/displayEntryTypeDate.ftl" />
<#include "/admin/plugins/workflow/modules/formspdf/display_entries/displayEntryTypeFile.ftl" />
<#include "/admin/plugins/workflow/modules/formspdf/display_entries/displayEntryTypeGalleryImage.ftl" />
<#include "/admin/plugins/workflow/modules/formspdf/display_entries/displayEntryTypeGeolocation.ftl" />
<#include "/admin/plugins/workflow/modules/formspdf/display_entries/displayEntryTypeImage.ftl" />
<#include "/admin/plugins/workflow/modules/formspdf/display_entries/displayEntryTypeMyLuteceUser.ftl" />
<#include "/admin/plugins/workflow/modules/formspdf/display_entries/displayEntryTypeNumber.ftl" />
<#include "/admin/plugins/workflow/modules/formspdf/display_entries/displayEntryTypeNumbering.ftl" />
<#include "/admin/plugins/workflow/modules/formspdf/display_entries/displayEntryTypeRadioButton.ftl" />
<#include "/admin/plugins/workflow/modules/formspdf/display_entries/displayEntryTypeSelect.ftl" />
<#include "/admin/plugins/workflow/modules/formspdf/display_entries/displayEntryTypeSelectOrder.ftl" />
<#include "/admin/plugins/workflow/modules/formspdf/display_entries/displayEntryTypeTelephone.ftl" />
<#include "/admin/plugins/workflow/modules/formspdf/display_entries/displayEntryTypeTermsOfService.ftl" />
<#include "/admin/plugins/workflow/modules/formspdf/display_entries/displayEntryTypeText.ftl" />
<#include "/admin/plugins/workflow/modules/formspdf/display_entries/displayEntryTypeTextArea.ftl" />
<#macro displayEntry q=''>
<#if q?? && q?has_content>
<#assign entry=q.question.entry >
<#assign list_responses=q.getEntryResponse() >
<#if entry.entryType.beanName == "forms.entryTypeArray">
    <@displayEntryWrapper title=entry.title!''>
        <@displayEntryTypeArray entry=entry list_responses=list_responses  />
    </@displayEntryWrapper>
<#elseif entry.entryType.beanName == "forms.entryTypeAutomaticFileReading">
    <@displayEntryWrapper title=entry.title!''>
        <@displayEntryTypeAutoFileReading entry=entry list_responses=list_responses  />
    </@displayEntryWrapper>
<#elseif entry.entryType.beanName == "forms.entryTypeCamera">
    <@displayEntryWrapper title=entry.title!''>
        <@displayEntryTypeCamera entry=entry list_responses=list_responses  />
    </@displayEntryWrapper>
<#elseif entry.entryType.beanName == "forms.entryTypeCartography">
    <@displayEntryWrapper title=entry.title!''>
        <@displayEntryTypeCartography entry=entry list_responses=list_responses  />
    </@displayEntryWrapper>
<#elseif entry.entryType.beanName == "forms.entryTypeCheckbox">
    <@displayEntryTypeCheckbox entry=entry list_responses=list_responses  />
<#elseif entry.entryType.beanName == "forms.entryTypeComment">
    <@displayEntryTypeComment entry=entry list_responses=list_responses  />
<#elseif entry.entryType.beanName == "forms.entryTypeDate">
    <@displayEntryTypeDate entry=entry list_responses=list_responses  />
<#elseif entry.entryType.beanName == "forms.entryTypeFile">
    <@displayEntryTypeFile entry=entry list_responses=list_responses  />
<#elseif entry.entryType.beanName == "forms.entryTypeGalleryImage">
    <@displayEntryWrapper title=entry.title!''>
        <@displayEntryTypeGalleryImage entry=entry list_responses=list_responses  />
    </@displayEntryWrapper>
<#elseif entry.entryType.beanName == "forms.entryTypeGeolocation">
    <@displayEntryTypeGeolocation entry=entry list_responses=list_responses  />
<#elseif entry.entryType.beanName == "forms.entryTypeImage">
    <@displayEntryTypeImage entry=entry list_responses=list_responses  />
<#elseif entry.entryType.beanName == "forms.entryTypeMyLuteceUser">
    <@displayEntryTypeMyLuteceUser entry=entry list_responses=list_responses  />
<#elseif entry.entryType.beanName == "forms.entryTypeNumber">
    <@displayEntryTypeNumber entry=entry list_responses=list_responses  />
<#elseif entry.entryType.beanName == "forms.entryTypeNumbering">
    <@displayEntryTypeNumbering entry=entry list_responses=list_responses  />
<#elseif entry.entryType.beanName == "forms.entryTypeRadioButton">
    <@displayEntryTypeRadioButton entry=entry list_responses=list_responses  />
<#elseif entry.entryType.beanName == "forms.entryTypeSelect">
    <@displayEntryTypeSelect entry=entry list_responses=list_responses  />
<#elseif entry.entryType.beanName == "forms.entryTypeSelectOrder">
    <@displayEntryTypeSelectOrder entry=entry list_responses=list_responses  />
<#elseif entry.entryType.beanName == "forms.entryTypeTelephoneNumber">
    <@displayEntryTypeTelephone entry=entry list_responses=list_responses  />
<#elseif entry.entryType.beanName == "forms.entryTypeTermsOfService">
    <@displayEntryWrapper title=entry.title!''>
    <@displayEntryTypeTermsOfService entry=entry list_responses=list_responses  />
    </@displayEntryWrapper> 
<#elseif entry.entryType.beanName == "forms.entryTypeText">
    <@displayEntryTypeText entry=entry list_responses=list_responses  />
<#elseif entry.entryType.beanName == "forms.entryTypeTextArea">
    <@displayEntryTypeTextArea entry=entry list_responses=list_responses  />
<#else>
    <@displayEntryTypeText entry=entry list_responses=list_responses  />
</#if>
</#if>
</#macro>
<#macro displayEntryWrapper title=''>
<@row>
	<@column xs=12 sm=4><@h level=3>${title}</@h></@column>
	<@column xs=12 sm=8>
        <#nested>
    </@column>
</@row>
</#macro>
