/*
 * Copyright (c) 2002-2023, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.workflow.modules.formspdf.service.task;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import fr.paris.lutece.plugins.forms.util.FormsConstants;
import fr.paris.lutece.plugins.genericattributes.business.Response;
import fr.paris.lutece.util.file.FileUtil;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;

import fr.paris.lutece.plugins.filegenerator.service.TemporaryFileGeneratorService;
import fr.paris.lutece.plugins.forms.business.Form;
import fr.paris.lutece.plugins.forms.business.FormDisplay;
import fr.paris.lutece.plugins.forms.business.FormDisplayHome;
import fr.paris.lutece.plugins.forms.business.FormHome;
import fr.paris.lutece.plugins.forms.business.FormResponse;
import fr.paris.lutece.plugins.forms.business.FormResponseHome;
import fr.paris.lutece.plugins.forms.business.Group;
import fr.paris.lutece.plugins.forms.business.GroupHome;
import fr.paris.lutece.plugins.forms.business.Question;
import fr.paris.lutece.plugins.forms.business.QuestionHome;
import fr.paris.lutece.plugins.workflow.modules.formspdf.business.FormsPDFTaskConfig;
import fr.paris.lutece.plugins.workflow.modules.formspdf.business.FormsPDFTaskTemplateHome;
import fr.paris.lutece.plugins.workflow.modules.formspdf.service.HtmlToPDFGenerator;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceHistory;
import fr.paris.lutece.plugins.workflowcore.service.config.ITaskConfigService;
import fr.paris.lutece.plugins.workflowcore.service.resource.IResourceHistoryService;
import fr.paris.lutece.plugins.workflowcore.service.task.Task;
import fr.paris.lutece.plugins.workflow.modules.formspdf.business.FormsPDFTaskTemplate;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.plugins.forms.service.provider.GenericFormsProvider;
import fr.paris.lutece.plugins.forms.business.FormQuestionResponse;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.web.l10n.LocaleService;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author norbert.le.garrec
 *
 */
@Dependent
@Named( "workflow-formspdf.formsPDFTask" )
public class FormsPDFTask extends Task
{

    /**
     * The task title
     */
    private static final String PROPERTY_LABEL_TITLE = "module.workflow.formspdf.title";
    private static final String PROPERTY_LABEL_DESCRIPTION = "module.workflow.formspdf.export.pdf.description";
    private static final String EMPTY_RESPONSE_VALUE = "module.workflow.formspdf.modify.template.replaceEmpty.defaultValue";
    private static final String FTL_SQUARE_BRACKET_TAG = "[#ftl]";
    /** Separator between a question marker and its iteration number, same as GenericFormsProvider.MARK_POSITION_ITERATION (private there) */
    private static final String MARK_ITERATION_SEPARATOR = "_";
    /** Markup wrapping the default label when displayed, so that templates can style it (the raw label stays in the response value) */
    private static final String FORMAT_EMPTY_RESPONSE_DISPLAY = "<em class=\"formspdf-empty\">%s</em>";


    /**
     * the FormJasperConfigService to manage the task configuration
     */
    @Inject
    @Named( "workflow-formspdf.formsPDFTaskConfigService" )
    private ITaskConfigService _formsPDFTaskConfigService;

    /**
     * the ResourceHistoryService to get the forms to process
     */
    @Inject
    private IResourceHistoryService _resourceHistoryService;

    /**
     * the TemporaryFileGeneratorService to generate the temporary file
     */
    @Inject
    private TemporaryFileGeneratorService _temporaryFileGeneratorService;

    @Override
    public void processTask( int nIdResourceHistory, HttpServletRequest request, Locale locale )
    {

        // Get the resourceHistory to find the resource to work with
        ResourceHistory resourceHistory = _resourceHistoryService.findByPrimaryKey( nIdResourceHistory );

        // Get the task configuration
        FormsPDFTaskConfig formsPDFTaskConfig = _formsPDFTaskConfigService.findByPrimaryKey( getId( ) );

        // Id of the Form to modify
        int nIdFormResponse = 0;
        String strError = "";
        AdminUser user = null;
        FormsPDFTaskTemplate formsPDFTaskTemplate = null;
        try
        {
            Locale currentLocale = null != locale ? locale : LocaleService.getContextUserLocale( request );

            nIdFormResponse = resourceHistory.getIdResource( );

            FormResponse frep = FormResponseHome.findByPrimaryKey( nIdFormResponse );
            Form form = FormHome.findByPrimaryKey( frep.getFormId( ) );

            // TODO Gerer le cas null quand il s'agit d'une action automatique
            if ( request != null )
            {
                user = AdminUserService.getAdminUser( request );
            }
            FormResponse formResponse = FormResponseHome.findByPrimaryKey( nIdFormResponse );
            Map<String, Object> model = GenericFormsProvider.getValuesModel( formResponse, request, false );
            removeNullEntries( model );

            formsPDFTaskTemplate = FormsPDFTaskTemplateHome.findByPrimaryKey( formsPDFTaskConfig.getIdTemplate( ) );

            if ( formsPDFTaskTemplate.isReplaceEmpty( ) )
            {
                replaceEmptyResponses( model, form, currentLocale );
            }

            if ( formsPDFTaskTemplate.isRte( ) )
            {
                formsPDFTaskTemplate.setContent( addSquareBracketTag( formsPDFTaskTemplate.getContent( ) ) );
            }

            String pdfFileName = "";
            String templatePdfFileName = formsPDFTaskTemplate.getFileName( );
            if ( null != templatePdfFileName && !"".equals( templatePdfFileName ) )
            {
                if ( formsPDFTaskTemplate.isRte( ) )
                {
                    formsPDFTaskTemplate.setFileName( addSquareBracketTag( formsPDFTaskTemplate.getFileName( ) ) );
                }

                try
                {
                    String templatedName = AppTemplateService.getTemplateFromStringFtl( formsPDFTaskTemplate.getFileName( ), currentLocale, model ).getHtml( );
                    pdfFileName = Jsoup.parse( templatedName ).text( );
                }
                catch( Exception e )
                {
                    AppLogService.error( "FormsPDFTask, invalid file_name {} for template {}", ( ) -> templatePdfFileName,
                            ( ) -> formsPDFTaskConfig.getIdTemplate( ) );
                }
                if ( 0 < pdfFileName.length( ) )
                {
                    pdfFileName = "_" + pdfFileName;
                }
            }

            formsPDFTaskTemplate
                    .setContent( AppTemplateService.getTemplateFromStringFtl( formsPDFTaskTemplate.getContent( ), currentLocale, model ).getHtml( ) );
            HtmlToPDFGenerator htmltopdf = new HtmlToPDFGenerator( form.getTitle( ) + pdfFileName,
                    I18nService.getLocalizedString( PROPERTY_LABEL_DESCRIPTION, locale ), frep, formsPDFTaskTemplate );
            _temporaryFileGeneratorService.generateFile( htmltopdf, user );
        }
        catch( Exception e )
        {
            // print the error in a pdf
            formsPDFTaskTemplate.setContent( e.getMessage( ) );
            HtmlToPDFGenerator htmltopdf = new HtmlToPDFGenerator( "error", I18nService.getLocalizedString( PROPERTY_LABEL_DESCRIPTION, locale ),
                    new FormResponse( ), formsPDFTaskTemplate );
            _temporaryFileGeneratorService.generateFile( htmltopdf, user );
            throw new RuntimeException( strError, e );
        }
    }

    @Override
    public String getTitle( Locale locale )
    {
        return I18nService.getLocalizedString( PROPERTY_LABEL_TITLE, locale );
    }

    @Override
    public Map<String, String> getTaskFormEntries( Locale locale )
    {
        return null;
    }

    @Override
    public void init( )
    {
        // Do nothing

    }

    @Override
    public void doRemoveTaskInformation( int nIdHistory )
    {
        // Do Nothing

    }

    @Override
    public void doRemoveConfig( )
    {
        // _formsJasperTaskConfigService.remove( getId( ) );
        _formsPDFTaskConfigService.remove( getId( ) );
    }

    /**
     * remove null values from the model
     * 
     * @param model
     *            the model containing the form question responses, mutated in place
     */
    private void removeNullEntries( Map<String, Object> model )
    {
        model.entrySet( ).removeIf( e -> {
            if ( e.getKey( ).startsWith( FormsConstants.MARK_POSITION ) )
            {
                FormQuestionResponse formQuestionResponse = (FormQuestionResponse) model.get( e.getKey( ) );
                return ( formQuestionResponse == null || formQuestionResponse.getQuestion( ).getEntry( ) == null );
            }
            else
            {
                return false;
            }
        } );
    }

    /**
     * Makes every question of the form display a value in the PDF, using a localized default label (e.g. "N/A") for the empty ones.
     * <p>
     * Two cases are handled:
     * <ul>
     * <li>the question has a {@link FormQuestionResponse} in the model but its responses are empty (blank text, no choice selected): the response values
     * are replaced, see {@link #replaceNullValues(Map, Locale)};</li>
     * <li>the question has no {@link FormQuestionResponse} in the model at all: iterations of a group the user did not add, conditionally hidden
     * questions, questions displayed only in the back office. The forms plugin stores nothing for them, so the markers proposed by the template editor
     * (<code>code_xxx_2</code>, <code>position_12_2</code>...) resolve to nothing. A synthetic response holding the default label is added under each of
     * these markers, see {@link #addMissingResponses(Map, Form, String)}.</li>
     * </ul>
     *
     * @param model
     *            the model containing the form question responses, mutated in place
     * @param form
     *            the form the response belongs to
     * @param currentLocale
     *            the locale used to resolve the default label
     */
    private void replaceEmptyResponses( Map<String, Object> model, Form form, Locale currentLocale )
    {
        replaceNullValues( model, currentLocale );
        addMissingResponses( model, form, I18nService.getLocalizedString( EMPTY_RESPONSE_VALUE, currentLocale ) );
    }

    /**
     * Replaces empty response values in the model with a localized default label (e.g. "N/A"), for keys identifying a question by its code ("code_*").
     * A question response without any response (choice entries with no selection) receives a single response holding the default label.
     *
     * @param model
     *            the model containing the form question responses, mutated in place
     * @param currentLocale
     *            the locale used to resolve the default label
     */
    private void replaceNullValues( Map<String, Object> model, Locale currentLocale )
    {
        String strDefaultValue = I18nService.getLocalizedString( EMPTY_RESPONSE_VALUE, currentLocale );

        model.forEach( ( key, value ) -> {
            if ( key.startsWith( FormsConstants.MARK_CODE ) && value instanceof FormQuestionResponse )
            {
                FormQuestionResponse formQuestionResponse = (FormQuestionResponse) value;
                List<Response> listResponse = formQuestionResponse.getEntryResponse( );

                if ( listResponse == null || listResponse.isEmpty( ) )
                {
                    listResponse = new ArrayList<>( );
                    listResponse.add( createDefaultResponse( formQuestionResponse.getQuestion( ), strDefaultValue ) );
                    formQuestionResponse.setEntryResponse( listResponse );
                    return;
                }

                listResponse.forEach( response -> {
                    if ( Strings.isEmpty( response.getResponseValue( ) ) )
                    {
                        response.setResponseValue( strDefaultValue );
                        response.setToStringValueResponse( formatDefaultValue( strDefaultValue ) );
                    }
                } );
            }
        } );
    }

    /**
     * Adds a synthetic response holding the default label under every marker of the form that has no response in the model. The markers are computed
     * exactly like the ones proposed by the template editor (see GenericFormsProvider.getProviderMarkerDescriptions): one marker per question, or one
     * marker per iteration for the questions of a group whose iteration max is greater than one.
     *
     * @param model
     *            the model containing the form question responses, mutated in place
     * @param form
     *            the form the response belongs to
     * @param strDefaultValue
     *            the localized default label
     */
    private void addMissingResponses( Map<String, Object> model, Form form, String strDefaultValue )
    {
        List<FormDisplay> listFormDisplay = FormDisplayHome.getFormDisplayByForm( form.getId( ) );
        Map<Integer, FormDisplay> mapFormDisplayById = listFormDisplay.stream( ).collect( Collectors.toMap( FormDisplay::getId, Function.identity( ) ) );

        for ( FormDisplay formDisplay : listFormDisplay )
        {
            if ( !FormsConstants.MARK_QUESTION.equals( formDisplay.getCompositeType( ) ) )
            {
                continue;
            }

            Question question = QuestionHome.findByPrimaryKey( formDisplay.getCompositeId( ) );
            if ( question == null || question.getEntry( ) == null )
            {
                continue;
            }

            int nIterationMax = 1;
            FormDisplay parentDisplay = mapFormDisplayById.get( formDisplay.getParentId( ) );
            if ( parentDisplay != null && FormsConstants.MARK_GROUP.equals( parentDisplay.getCompositeType( ) ) )
            {
                Group group = GroupHome.findByPrimaryKey( parentDisplay.getCompositeId( ) );
                if ( group != null )
                {
                    nIterationMax = group.getIterationMax( );
                }
            }

            String strCodeKey = FormsConstants.MARK_CODE + FileUtil.normalizeFileName( question.getCode( ) );
            String strPositionKey = FormsConstants.MARK_POSITION + question.getId( );

            if ( nIterationMax <= 1 )
            {
                putDefaultResponseIfAbsent( model, strCodeKey, strPositionKey, question, strDefaultValue );
            }
            else
            {
                for ( int nIteration = 0; nIteration < nIterationMax; nIteration++ )
                {
                    Question iteratedQuestion = new Question( question );
                    iteratedQuestion.setIterationNumber( nIteration );
                    putDefaultResponseIfAbsent( model, strCodeKey + MARK_ITERATION_SEPARATOR + nIteration,
                            strPositionKey + MARK_ITERATION_SEPARATOR + nIteration, iteratedQuestion, strDefaultValue );
                }
            }
        }
    }

    /**
     * Puts a synthetic {@link FormQuestionResponse} holding the default label under the given code and position keys, unless one of them is already
     * present in the model.
     *
     * @param model
     *            the model, mutated in place
     * @param strCodeKey
     *            the "code_*" key of the question
     * @param strPositionKey
     *            the "position_*" key of the question
     * @param question
     *            the question, with its entry loaded (the display macros dispatch on the entry type)
     * @param strDefaultValue
     *            the localized default label
     */
    private void putDefaultResponseIfAbsent( Map<String, Object> model, String strCodeKey, String strPositionKey, Question question,
            String strDefaultValue )
    {
        if ( model.containsKey( strCodeKey ) || model.containsKey( strPositionKey ) )
        {
            return;
        }

        FormQuestionResponse formQuestionResponse = new FormQuestionResponse( );
        formQuestionResponse.setQuestion( question );
        formQuestionResponse.setIdStep( question.getIdStep( ) );
        List<Response> listResponse = new ArrayList<>( );
        listResponse.add( createDefaultResponse( question, strDefaultValue ) );
        formQuestionResponse.setEntryResponse( listResponse );

        model.put( strCodeKey, formQuestionResponse );
        model.put( strPositionKey, formQuestionResponse );
    }

    /**
     * Creates a response holding the default label. The response value receives the raw label and the string value its displayable form, since the
     * display macros read one or the other depending on the entry type.
     *
     * @param question
     *            the question, may be null
     * @param strDefaultValue
     *            the localized default label
     * @return the response
     */
    private Response createDefaultResponse( Question question, String strDefaultValue )
    {
        Response response = new Response( );
        if ( question != null )
        {
            response.setEntry( question.getEntry( ) );
        }
        response.setResponseValue( strDefaultValue );
        response.setToStringValueResponse( formatDefaultValue( strDefaultValue ) );
        return response;
    }

    /**
     * Wraps the default label in the markup used to display it, so that it stands out from the values entered by the user and can be styled by the
     * templates through its CSS class.
     *
     * @param strDefaultValue
     *            the localized default label
     * @return the displayable label
     */
    private String formatDefaultValue( String strDefaultValue )
    {
        return String.format( FORMAT_EMPTY_RESPONSE_DISPLAY, strDefaultValue );
    }

    /**
     * Add square bracket tag at the beginning of the template to process the template with the brackets included in the RTE
     * 
     * @param strtemplate
     * @return
     */

    private String addSquareBracketTag( String strtemplate )
    {
        strtemplate = FTL_SQUARE_BRACKET_TAG + strtemplate;

        return strtemplate;
    }
}
