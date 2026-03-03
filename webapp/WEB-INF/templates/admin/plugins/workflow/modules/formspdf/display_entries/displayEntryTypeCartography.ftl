<#--
Macro: displayEntryTypeCartography
Description: Display an entry of type Cartography
Parameters: entry, list_responses
Entry not found in basic config of forms entrytype, not tested
-->
<#macro displayEntryTypeCartography entry, list_responses >
<#assign coordinate='' >
<#assign datalayer='' >
<#if getResponseContainingTheFieldWithCode( list_responses, "coordinates_geojson" )?? >
	<#assign coordinate = getResponseContainingTheFieldWithCode( list_responses, "coordinates_geojson" ).toStringValueResponse >
</#if>
<#if getResponseContainingTheFieldWithCode( list_responses, "DataLayer" )?? >
	<#assign datalayer = getResponseContainingTheFieldWithCode( list_responses, "DataLayer" ).toStringValueResponse >
</#if>
</#macro>
