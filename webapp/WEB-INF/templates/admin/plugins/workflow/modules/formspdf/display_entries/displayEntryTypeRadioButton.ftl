<#--
Macro: displayEntryTypeRadioButton
Description: Display an entry of type radio button
Parameters: entry, list_responses
-->
<#macro displayEntryTypeRadioButton entry, list_responses >
			<#if list_responses?has_content>
				<#list list_responses as response>
					<p>
						<#if response.field??>${response.field.title!''}
							<#if response.field.fileImage.url??>
								<@img url=response.field.fileImage.url title=response.field.title alt=response.field.title params='width="100" height="100"' />
							</#if>
						</#if>
					</p>
				</#list>
			</#if>
</#macro>
