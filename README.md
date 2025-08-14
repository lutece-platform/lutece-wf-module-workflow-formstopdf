**# module-workflow-formspdf
# module-workflow-formspdf
EN | [FR](README.fr.md) 

This module provides a task to create a PDF from a template and the data of a form Response.

Prerequisite : the libawt_xawt.so library should be provided by the JDK.

## Customizing the display of question types (entrytype)

The Form plugin offers different types of questions: date, text, image, file etc. To adapt the display of answers according to their type (Entrytype), the module-workflow-formstopdf uses Freemarker macros (wiki macro links).

In the source code of the module, there is a macro whose role is to call the macro that corresponds to the Entrytype of the question.

This macro is here: lutece-wf-module-workflow-formstopdf/webapp/WEB-INF/templates/admin/plugins/workflow/modules/formspdf/display_entries/displayEntry.ftl

```xml
<#macro displayEntry q>
    <#if q??>
        <#assign entry=q.question.entry >
        <#assign list_responses=q.getEntryResponse() >

        <#if entry.entryType.beanName == "forms.entryTypeArray">
            <@displayEntryTypeArray entry=entry list_responses=list_responses  />
        <#elseif entry.entryType.beanName == "forms.entryTypeAutomaticFileReading">
            <@displayEntryTypeAutoFileReading entry=entry list_responses=list_responses  />
        <#elseif entry.entryType.beanName == "forms.entryTypeCamera">
            <@displayEntryTypeCamera entry=entry list_responses=list_responses  />
        <#elseif entry.entryType.beanName == "forms.entryTypeCartography">
            <@displayEntryTypeCartography entry=entry list_responses=list_responses  />
        <#elseif entry.entryType.beanName == "forms.entryTypeCheckbox">
            <@displayEntryTypeCheckbox entry=entry list_responses=list_responses  />
        <#elseif entry.entryType.beanName == "forms.entryTypeComment">
            <@displayEntryTypeComment entry=entry list_responses=list_responses  />
        <#elseif entry.entryType.beanName == "forms.entryTypeDate">
            <@displayEntryTypeDate entry=entry list_responses=list_responses  />
        <#elseif entry.entryType.beanName == "forms.entryTypeFile">
            <@displayEntryTypeFile entry=entry list_responses=list_responses  />
        <#elseif entry.entryType.beanName == "forms.entryTypeGalleryImage">
            <@displayEntryTypeGalleryImage entry=entry list_responses=list_responses  />
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
            <@displayEntryTypeTelephoneNumber entry=entry list_responses=list_responses  />
        <#elseif entry.entryType.beanName == "forms.entryTypeTermsOfService">
            <@displayEntryTypeTermsOfService entry=entry list_responses=list_responses  />
        <#elseif entry.entryType.beanName == "forms.entryTypeText">
            <@displayEntryTypeText entry=entry list_responses=list_responses  />
        <#elseif entry.entryType.beanName == "forms.entryTypeTextArea">
            <@displayEntryTypeTextArea entry=entry list_responses=list_responses  />
        <#else>
            <@displayEntryTypeText entry=entry list_responses=list_responses  />
        </#if>
    </#if>
</#macro>
```

This “redirection” macro takes a parameter “q” which corresponds to the value of the answer contained in “position_X”
To call it according to the chosen syntax:
```xml
<#@displayEntry q=position_7/>
```
Or 
```xml
[#@displayEntry q=position_7/]
```
![bookmarks](https://lutece.paris.fr/support/image?resource_type=wiki_image&id=615)
## Configuration of Freemarker Macros Syntax
Freemarker tags are usually defined in a similar way to that of html with “<” and >”. For example to define a macro: “<#macro myMacro arg1/>”. And to call it: “ <@myMacro arg1= arg1/>”.
This syntax is incompatible with the use of the RichTextEditor which reformats the elements to prevent display bugs and XSS attacks. For example: “<@myMacro arg1= arg1/>” becomes "<p>&lt;@myMacro arg1= arg1/&gt;</p>”.
To use the Response Bookmarks (which are actually calls to macros) with the RTE, you must modify the Freemarker syntax.
Adding [#ftl] at the very beginning of the template makes it possible to use Freemarker macros with a different syntax that is compatible with the RichTextEditor.
```xml
[#ftl]
 ```
To call a macro: “ [@myMacro arg1= arg1/]”.
The module automatically adds and removes the [#ftl] header when editing the option below.
Without the RTE, it is possible to configure the display of a macro, because the RTE reformats the HTML. It is also easier to customize a macro without RTE with the syntax in “<>”, because you can then copy and paste the existing display macros that you want to modify.
## Example of customization
At the template editing step, we have to select the “without Rich Text editor” option.
You have to retrieve the macro of the Entrytype that you want to modify in the source code of the module-workflow-formstopdf.
Here, we identify the macro: “DisplayEntryTypeText”. We copy/paste the content of the macro into the template form.
![macrotext](https://lutece.paris.fr/support/image?resource_type=wiki_image&id=616)
We can now modify the macro to have the desired display.
In this example, the color and style are customized.

![custommacro](https://lutece.paris.fr/support/image?resource_type=wiki_image&id=618)

As a result, we have the answer whose style has been customized.

![result](https://lutece.paris.fr/support/image?resource_type=wiki_image&id=619)

