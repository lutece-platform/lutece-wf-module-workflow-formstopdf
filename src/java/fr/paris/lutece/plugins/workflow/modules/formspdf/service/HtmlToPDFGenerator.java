/*
 * Copyright (c) 2002-2022, City of Paris
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
package fr.paris.lutece.plugins.workflow.modules.formspdf.service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities.EscapeMode;

import fr.paris.lutece.plugins.forms.business.CompositeDisplayType;
import fr.paris.lutece.plugins.forms.business.Form;
import fr.paris.lutece.plugins.forms.business.FormDisplay;
import fr.paris.lutece.plugins.forms.business.FormDisplayHome;
import fr.paris.lutece.plugins.forms.business.FormHome;
import fr.paris.lutece.plugins.forms.business.FormQuestionResponse;
import fr.paris.lutece.plugins.forms.business.FormResponse;
import fr.paris.lutece.plugins.forms.business.FormResponseStep;
import fr.paris.lutece.plugins.forms.business.Group;
import fr.paris.lutece.plugins.forms.business.GroupHome;
import fr.paris.lutece.plugins.forms.business.Question;
import fr.paris.lutece.plugins.forms.business.Step;
import fr.paris.lutece.plugins.forms.export.pdf.PdfCell;
import fr.paris.lutece.plugins.forms.service.entrytype.EntryTypeCamera;
import fr.paris.lutece.plugins.forms.service.entrytype.EntryTypeCheckBox;
import fr.paris.lutece.plugins.forms.service.entrytype.EntryTypeFile;
import fr.paris.lutece.plugins.forms.service.entrytype.EntryTypeImage;
import fr.paris.lutece.plugins.forms.service.entrytype.EntryTypeRadioButton;
import fr.paris.lutece.plugins.forms.service.entrytype.EntryTypeSelect;
import fr.paris.lutece.plugins.forms.service.entrytype.EntryTypeTermsOfService;
import fr.paris.lutece.plugins.genericattributes.business.Entry;
import fr.paris.lutece.plugins.genericattributes.business.Response;
import fr.paris.lutece.plugins.genericattributes.service.entrytype.AbstractEntryTypeComment;
import fr.paris.lutece.plugins.genericattributes.service.entrytype.EntryTypeServiceManager;
import fr.paris.lutece.plugins.genericattributes.service.entrytype.IEntryTypeService;
import fr.paris.lutece.plugins.html2pdf.service.PdfConverterService;
import fr.paris.lutece.plugins.html2pdf.service.PdfConverterServiceException;
import fr.paris.lutece.plugins.workflow.modules.formspdf.business.FormsPDFTaskTemplate;
import fr.paris.lutece.plugins.workflow.modules.formspdf.service.provider.FormsProvider;
import fr.paris.lutece.plugins.workflowcore.service.provider.InfoMarker;
import fr.paris.lutece.portal.service.file.FileService;
import fr.paris.lutece.portal.service.file.IFileStoreServiceProvider;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.html.HtmlTemplate;

// TODO: Auto-generated Javadoc
/**
 * The Class HtmlToPDFGenerator.
 */
public class HtmlToPDFGenerator extends AbstractFileGenerator
{

    /**
     * Instantiates a new html to PDF generator.
     *
     * @param fileName
     *            the file name
     * @param fileDescription
     *            the file description
     * @param formResponse
     *            the form response
     * @param formsProvider TODO
     * @param htmlTemplate
     *            the template
     */
    public HtmlToPDFGenerator( String fileName, String fileDescription, FormResponse formResponse, FormsPDFTaskTemplate formsPDFTaskTemplate, FormsProvider formsProvider )
    {
        super( fileName, fileDescription, formResponse, formsPDFTaskTemplate );
        _formsProvider = formsProvider;
    }

    private static final boolean ZIP_EXPORT = Boolean.parseBoolean( AppPropertiesService.getProperty( "workflow-formspdf.export.pdf.zip", "false" ) );
    private static final String CONSTANT_MIME_TYPE_PDF = "application/pdf";
    private static final String EXTENSION_PDF = ".pdf";
    // private static final String MARK_STEP_FORMS = "list_summary_step_display";
    private static final String KEY_LABEL_YES = "portal.util.labelYes";
    private static final String KEY_LABEL_NO = "portal.util.labelNo";
    private static final String DEFAULT_TEMPLATE_FORMSPDF = "admin/plugins/workflow/modules/formspdf/form_response_summary.html";
    
    private final FormsProvider _formsProvider;

    /**
     * Generate file.
     *
     * @return the path
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public Path generateFile( ) throws IOException
    {
        Path directoryFile = Paths.get( TMP_DIR, _fileName );
        if ( !directoryFile.toFile( ).exists( ) )
        {
            directoryFile.toFile( ).mkdir( );
        }
        writeExportFile( directoryFile );
        if ( hasMultipleFiles( ) )
        {
            return directoryFile;
        }
        File [ ] files = directoryFile.toFile( ).listFiles( ( File f ) -> f.getName( ).endsWith( EXTENSION_PDF ) );
        return files [0].toPath( );
    }

    /**
     * Gets the file name.
     *
     * @return the file name
     */
    @Override
    public String getFileName( )
    {
        return _fileName + EXTENSION_PDF;
    }

    /**
     * Gets the mime type.
     *
     * @return the mime type
     */
    @Override
    public String getMimeType( )
    {
        return CONSTANT_MIME_TYPE_PDF;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    @Override
    public String getDescription( )
    {
        return _fileDescription;
    }

    /**
     * Checks if is zippable.
     *
     * @return true, if is zippable
     */
    @Override
    public boolean isZippable( )
    {
        return ZIP_EXPORT;
    }

    /**
     * Gets the value forms reponse.
     *
     * @param formresponse
     *            the formresponse
     * @return the value forms reponse
     */
    @SuppressWarnings("unused")
	@Deprecated
    private List<String> getValueFormsReponse( FormResponse formresponse )
    {
        List<String> lstValue = new ArrayList<String>( );
        Form form = FormHome.findByPrimaryKey( formresponse.getFormId( ) );

        // Filters the FormResponseStep with at least one question exportable in pdf
        List<FormResponseStep> filteredList = formresponse.getSteps( ).stream( ).filter(
                frs -> frs.getQuestions( ).stream( ).map( FormQuestionResponse::getQuestion ).map( Question::getEntry ).anyMatch( Entry::isExportablePdf ) )
                .collect( Collectors.toList( ) );
        for ( FormResponseStep formResponseStep : filteredList )
        {
            List<PdfCell> listContent = createCellsForStep( formResponseStep );
            for ( PdfCell pdfCellValue : listContent )
            {
                lstValue.add( pdfCellValue.getTitle( ) + " Reponse : " + pdfCellValue.getValue( ) );
            }
        }

        return lstValue;
    }

    /**
     * Gets the pdf cell value forms reponse.
     *
     * @param formresponse
     *            the formresponse
     * @return the pdf cell value forms reponse
     */
    @SuppressWarnings("unused")
	@Deprecated
    private List<PdfCell> getPdfCellValueFormsReponse( FormResponse formresponse )
    {
        List<PdfCell> listContent = new ArrayList<>( );

        // Filters the FormResponseStep with at least one question exportable in pdf
        List<FormResponseStep> filteredList = formresponse.getSteps( ).stream( ).filter(
                frs -> frs.getQuestions( ).stream( ).map( FormQuestionResponse::getQuestion ).map( Question::getEntry ).anyMatch( Entry::isExportablePdf ) )
                .collect( Collectors.toList( ) );
        for ( FormResponseStep formResponseStep : filteredList )
        {
            listContent.addAll( createCellsForStep( formResponseStep ) );
        }

        return listContent;
    }

    /**
     * Creates the cells for step.
     *
     * @param formResponseStep
     *            the form response step
     * @return the list
     */
    @SuppressWarnings("unused")
	@Deprecated
    private List<PdfCell> createCellsForStep( FormResponseStep formResponseStep )
    {
        Step step = formResponseStep.getStep( );

        List<FormDisplay> listStepFormDisplay = FormDisplayHome.getFormDisplayListByParent( step.getId( ), 0 );

        List<PdfCell> listContent = new ArrayList<>( );
        for ( FormDisplay formDisplay : listStepFormDisplay )
        {
            if ( CompositeDisplayType.GROUP.getLabel( ).equals( formDisplay.getCompositeType( ) ) )
            {
                List<PdfCell> listContentGroup = createCellsForGroup( formResponseStep, formDisplay );
                listContent.addAll( listContentGroup );
            }
            else
            {
                PdfCell cell = createPdfCellNoGroup( formResponseStep, formDisplay );
                if ( cell != null )
                {
                    listContent.add( cell );
                }
            }
        }
        return listContent;
    }

    /**
     * Creates the cells for group.
     *
     * @param formResponseStep
     *            the form response step
     * @param formDisplay
     *            the form display
     * @return the list
     */
    @SuppressWarnings("unused")
	@Deprecated
    private List<PdfCell> createCellsForGroup( FormResponseStep formResponseStep, FormDisplay formDisplay )
    {
        List<PdfCell> listContent = new ArrayList<>( );
        Group group = GroupHome.findByPrimaryKey( formDisplay.getCompositeId( ) );

        List<FormDisplay> listGroupDisplay = FormDisplayHome.getFormDisplayListByParent( formResponseStep.getStep( ).getId( ), formDisplay.getId( ) );
        for ( int ite = 0; ite < group.getIterationMax( ); ite++ )
        {
            for ( FormDisplay formDisplayGroup : listGroupDisplay )
            {
                PdfCell cell = createPdfCell( formResponseStep, formDisplayGroup, group, ite );
                if ( cell != null )
                {
                    listContent.add( cell );
                }
            }
        }
        return listContent;
    }

    /**
     * Creates the pdf cell no group.
     *
     * @param formResponseStep
     *            the form response step
     * @param formDisplay
     *            the form display
     * @return the pdf cell
     * 
     */
    private PdfCell createPdfCellNoGroup( FormResponseStep formResponseStep, FormDisplay formDisplay )
    {
        return createPdfCell( formResponseStep, formDisplay, null, 0 );
    }

    /**
     * Creates the pdf cell.
     *
     * @param formResponseStep
     *            the form response step
     * @param formDisplay
     *            the form display
     * @param group
     *            the group
     * @param iterationNumber
     *            the iteration number
     * @return the pdf cell
     */
    @SuppressWarnings("unused")
	@Deprecated
    private PdfCell createPdfCell( FormResponseStep formResponseStep, FormDisplay formDisplay, Group group, int iterationNumber )
    {
        FormQuestionResponse formQuestionResponse = formResponseStep.getQuestions( ).stream( )
                .filter( fqr -> fqr.getQuestion( ).getEntry( ).isExportablePdf( ) )
                .filter( fqr -> fqr.getQuestion( ).getId( ) == formDisplay.getCompositeId( ) )
                .filter( fqr -> fqr.getQuestion( ).getIterationNumber( ) == iterationNumber ).findFirst( ).orElse( null );

        if ( formQuestionResponse != null )
        {
            String key = formQuestionResponse.getQuestion( ).getTitle( );
            List<String> listResponseValue = getResponseValue( formQuestionResponse, iterationNumber );
            if ( CollectionUtils.isNotEmpty( listResponseValue ) )
            {
                PdfCell cell = new PdfCell( );
                cell.setTitle( key );
                cell.setValue( listResponseValue.stream( ).filter( StringUtils::isNotEmpty ).collect( Collectors.joining( ";" ) ) );
                if ( group != null )
                {
                    String groupName = group.getTitle( );
                    if ( group.getIterationMax( ) > 1 )
                    {
                        int iteration = iterationNumber + 1;
                        groupName += " (" + iteration + ")";
                    }
                    cell.setGroup( groupName );
                }
                return cell;
            }
        }
        return null;
    }

    /**
     * Gets the response value.
     *
     * @param formQuestionResponse
     *            the form question response
     * @param iteration
     *            the iteration
     * @return the response value
     */
    @SuppressWarnings("unused")
	@Deprecated
    private List<String> getResponseValue( FormQuestionResponse formQuestionResponse, int iteration )
    {
        Entry entry = formQuestionResponse.getQuestion( ).getEntry( );

        IEntryTypeService entryTypeService = EntryTypeServiceManager.getEntryTypeService( entry );
        List<String> listResponseValue = new ArrayList<>( );
        if ( entryTypeService instanceof AbstractEntryTypeComment )
        {
            return listResponseValue;
        }

        if ( entryTypeService instanceof EntryTypeTermsOfService )
        {
            boolean aggrement = formQuestionResponse.getEntryResponse( ).stream( )
                    .filter( response -> response.getField( ).getCode( ).equals( EntryTypeTermsOfService.FIELD_AGREEMENT_CODE ) )
                    .map( Response::getResponseValue ).map( Boolean::valueOf ).findFirst( ).orElse( false );

            if ( aggrement )
            {
                listResponseValue.add( I18nService.getLocalizedString( KEY_LABEL_YES, I18nService.getDefaultLocale( ) ) );
            }
            else
            {
                listResponseValue.add( I18nService.getLocalizedString( KEY_LABEL_NO, I18nService.getDefaultLocale( ) ) );
            }
            return listResponseValue;

        }
        for ( Response response : formQuestionResponse.getEntryResponse( ) )
        {
            if ( response.getIterationNumber( ) != -1 && response.getIterationNumber( ) != iteration )
            {
                continue;
            }

            String strResponseValue = entryTypeService.getResponseValueForRecap( entry, null, response, I18nService.getDefaultLocale( ) );

            if ( entryTypeService instanceof EntryTypeCheckBox && strResponseValue != null )
            {
                listResponseValue.add( "<input type=\"checkbox\" name=\"" + strResponseValue + " \" checked=\"checked\" />" );
            }

            if ( ( entryTypeService instanceof EntryTypeImage || entryTypeService instanceof EntryTypeCamera ) && strResponseValue != null )
            {

                if ( response.getFile( ) != null )
                {
                    IFileStoreServiceProvider fileStoreService = FileService.getInstance( ).getFileStoreServiceProvider( );
                    fr.paris.lutece.portal.business.file.File fileImage = fileStoreService.getFile( String.valueOf( response.getFile( ).getIdFile( ) ) );

                    String encoded = Base64.getEncoder( ).encodeToString( fileImage.getPhysicalFile( ).getValue( ) );

                    listResponseValue.add( "<img src=\"data:image/jpeg;base64, " + encoded + " \" width=\"100px\" height=\"100px\" /> " );
                }

            }
            if ( entryTypeService instanceof EntryTypeFile && response.getFile( ) != null )
            {
                IFileStoreServiceProvider fileStoreService = FileService.getInstance( ).getFileStoreServiceProvider( );
                listResponseValue.add( fileStoreService.getFile( String.valueOf( response.getFile( ).getIdFile( ) ) ).getTitle( ) );
            }

            if ( entryTypeService instanceof EntryTypeRadioButton && strResponseValue != null )
            {
                listResponseValue.add( "<input type=\"radio\" value=\"" + strResponseValue + " \" name=\"" + strResponseValue + " \" checked=\"checked\" />" );
            }

            if ( entryTypeService instanceof EntryTypeSelect && strResponseValue != null )
            {
                listResponseValue.add( "<select name=\"" + strResponseValue + " \" > <option>" + strResponseValue + "</option> </select>" );
            }

            if ( strResponseValue != null )
            {
                listResponseValue.add( strResponseValue );
            }
        }

        return listResponseValue;
    }

    /**
     * Write export file.
     *
     * @param directoryFile
     *            the directory file
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void writeExportFile( Path directoryFile ) throws IOException
    {
        Map<String, Object> model = new HashMap<>( );
        String strError = "";

        markersToModel(model, _formsProvider.provideMarkerValues());
        
        HtmlTemplate htmltemplate = null;
        if (_formsPDFTaskTemplate == null)
        {
        	htmltemplate = AppTemplateService.getTemplate(DEFAULT_TEMPLATE_FORMSPDF, Locale.getDefault(), model);
        } else {
        	htmltemplate = AppTemplateService.getTemplateFromStringFtl(_formsPDFTaskTemplate.getContent(), Locale.getDefault( ), model);
        }
        
        try ( OutputStream outputStream = Files.newOutputStream( directoryFile.resolve( generateFileName( _formResponse ) + ".pdf" ) ) )
        {
            Document doc = Jsoup.parse( htmltemplate.getHtml( ), "UTF-8" );
            doc.outputSettings( ).syntax( Document.OutputSettings.Syntax.xml );
            doc.outputSettings( ).escapeMode( EscapeMode.base.xhtml );
            doc.outputSettings( ).charset( "UTF-8" );

            PdfConverterService.getInstance( ).getPdfBuilder( ).reset( ).withHtmlContent( doc.html( ) ).notEditable( ).render( outputStream );
        }
        catch( PdfConverterServiceException e )
        {
            strError = "Une erreur s'est produite lors de la generation de l'edition";
            AppLogService.error( strError, e );
            throw new RuntimeException( strError, e );
        }
        catch( IOException e )
        {
            strError = "Une erreur s'est produite lors de la generation de l'edition";
            AppLogService.error( strError, e );
            throw new RuntimeException( strError, e );
        }

    }
    
    private Map<String, Object> markersToModel( Map<String, Object> model, Collection<InfoMarker> collectionInfoMarkers )
    {
        for ( InfoMarker infoMarker : collectionInfoMarkers )
        {
            model.put( infoMarker.getMarker(), infoMarker.getValue() );
        }
        return model;
    }

}
