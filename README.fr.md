**# module-workflow-formspdf
# module-workflow-formspdf
FR | [EN](README.md)
## Personnaliser l’affichage des types de questions (entrytype)

Le plugin Form proposent différents types question : date, text, image, fichier etc. Pour adapter l’affichage des réponses selon leur type (Entrytype), le module-workflow-formstopdf utilise des macros Freemarker (liens wiki macro). 

Dans le code source du module, il y a une macro dont le rôle est d’appeler la macro qui correspond à l’Entrytype de la question. 

Cette macro se trouve ici : lutece-wf-module-workflow-formstopdf/webapp/WEB-INF/templates/admin/plugins/workflow/modules/formspdf/display_entries/displayEntry.ftl

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

Cette macro de “redirection” prend un paramètre “q” qui corresponds à la valeur de la réponse contenue dans “position_X”
Pour l’appeler selon la syntaxe choisie :

```xml
<#@displayEntry q=position_7/>
```
Ou 
```xml
[#@displayEntry q=position_7/]
```
![bookmarks](https://lutece.paris.fr/support/image?resource_type=wiki_image&id=615)
## Configuration la syntaxe des Macros Freemarker
Les balises freemarker sont habituellement définies de façon similaire à celle de l’html avec “<” et >”. Par exemple pour définir une macro :   “<#macro maMacro arg1/>”. Et pour l’appeler : “ <@maMacro arg1= arg1/>”.
Cette syntaxe est incompatible avec l’utilisation du RichTextEditor qui reformatte les éléments afin de prévenir les bugs d’affichage et les attaques XSS. Par exemple :  “<@maMacro arg1= arg1/>” devient "<p>&lt;@maMacro arg1= arg1/&gt;</p>”.
Pour utiliser les Bookmarks de réponse (qui sont en fait des appelle à des macros) avec le RTE, il faut modifier la syntaxe Freemarker.
Ajouter [#ftl] au tout début du template rend possible l’utilisation des macros Freemarker avec une syntaxe différente qui est compatible avec le RichTextEditor. 
```xml
[#ftl]
 ```
Pour appeler une macro : “ [@maMacro arg1= arg1/]”. 
Le module ajoute et supprime l’entête [#ftl] automatiquement lors de la modification l’option ci-dessous.  
Sans le RTE, il est possible de configurer l’affichage d’une macro, car le RTE reformate le HTML. Il est aussi plus aisé de personnaliser une macro sans RTE avec la syntaxe en “<>”, car on peut alors copier et coller les macros d’affichage existantes que l’on souhaite modifier.   
## Exemple de personnalisation
À l’étape d’édition de la template, nous avons a sélectionner l’option “sans Rich Text editor”.
Il faut récupérer la macro de l’Entrytype que l’on souhaite modifier dans le code source du module-workflow-formstopdf. 
Ici, on repère la macro : “DisplayEntryTypeText”. On fait un copier/coller du contenu de la macro dans le formulaire du template.

![macrotext](https://lutece.paris.fr/support/image?resource_type=wiki_image&id=616)

On peut maintenant modifier la macro pour avoir l’affichage souhaité.
Dans cet exemple, la couleur et le style sont personnalisés.

![custommacro](https://lutece.paris.fr/support/image?resource_type=wiki_image&id=618)

En résultat, nous avons la réponse dont le style a été personnalisé.

![result](https://lutece.paris.fr/support/image?resource_type=wiki_image&id=619)
