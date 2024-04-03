<#--
Macro: displayEntryTypeFile
Description: Display an entry of type file
Parameters: entry, list_responses
-->
<#macro displayEntryTypeFile entry, list_responses >
			<#if list_responses?has_content>
				<#list list_responses as response>
					<p>
						<#if response.file?exists>
							<#if response.file.fileKey?exists>
								<@link href='${base_url!""}jsp/admin/plugins/forms/FormsDownloadFile.jsp?id_file=${response.file.idFile}&id_response=${response.idResponse}'>
									${response.file.title}(${response.file.size} O)
								</@link>
							</#if>
						</#if>
					</p>
				</#list>
			</#if>
</#macro>
