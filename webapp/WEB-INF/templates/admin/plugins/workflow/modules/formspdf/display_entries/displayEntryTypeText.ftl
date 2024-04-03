<#--
Macro: displayEntryTypeText
Description: Display an entry of type text
Parameters: entry, list_responses
-->
<#macro displayEntryTypeText entry, list_responses >
	<div style="display: flex; flex-wrap: wrap;">
		<div style="flex: 0 0 75%; max-width: 75%;">
			<#if list_responses?has_content>
				<#list list_responses as response>
					<#if response.toStringValueResponse?matches("((http|https)://)(www.)?[a-zA-Z0-9@:%._\\+~#?&//=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%._\\+~#?&//=]*)")>
						<p>
							<@link href=response.toStringValueResponse! title='${response.toStringValueResponse!} - #i18n{portal.site.portal_footer.newWindow}' target='_blank'>${response.toStringValueResponse!''}</@link>
						</p>
					<#else>
						<p>${response.toStringValueResponse!''}</p>
					</#if>
				</#list>
			</#if>
		</div>
	</div>
</#macro>
