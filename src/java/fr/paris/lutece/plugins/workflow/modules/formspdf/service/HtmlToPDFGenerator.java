package fr.paris.lutece.plugins.workflow.modules.formspdf.service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
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
import fr.paris.lutece.portal.service.file.FileService;
import fr.paris.lutece.portal.service.file.IFileStoreServiceProvider;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.html.HtmlTemplate;

public class HtmlToPDFGenerator extends AbstractFileGenerator {
	
	public HtmlToPDFGenerator(String fileName, String fileDescription, FormResponse formResponse, String template) {
		super(fileName, fileDescription, formResponse, template);
	}

	private static final boolean ZIP_EXPORT = Boolean.parseBoolean( AppPropertiesService.getProperty( "workflow-formspdf.export.pdf.zip", "false" ) );
    private static final String CONSTANT_MIME_TYPE_PDF = "application/pdf";
    private static final String EXTENSION_PDF = ".pdf";
    
    private static final String MARK_STEP_FORMS = "list_summary_step_display";
    
    private static final String KEY_LABEL_YES = "portal.util.labelYes";
    private static final String KEY_LABEL_NO = "portal.util.labelNo";

	@Override
	public Path generateFile() throws IOException {
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

	@Override
	public String getFileName() {
		return _fileName + EXTENSION_PDF;
	}

	@Override
	public String getMimeType() {
		return CONSTANT_MIME_TYPE_PDF;
	}

	@Override
	public String getDescription() {
		return _fileDescription;
	}

	@Override
	public boolean isZippable() {
		return ZIP_EXPORT;
	}
	
	private List<String> getValueFormsReponse(FormResponse formresponse )
    {
    	List<String> lstValue =  new ArrayList<String>( );
    	Form form = FormHome.findByPrimaryKey( formresponse.getFormId( ) );

        // Filters the FormResponseStep with at least one question exportable in pdf
        List<FormResponseStep> filteredList = formresponse.getSteps( ).stream( ).filter(
                frs -> frs.getQuestions( ).stream( ).map( FormQuestionResponse::getQuestion ).map( Question::getEntry ).anyMatch( Entry::isExportablePdf ) )
                .collect( Collectors.toList( ) );
        for ( FormResponseStep formResponseStep : filteredList )
        {
        	List<PdfCell> listContent = createCellsForStep( formResponseStep );
        	for (PdfCell pdfCellValue : listContent)
            {
            	lstValue.add( pdfCellValue.getTitle() + " Reponse : " + pdfCellValue.getValue( ) );
            }
        }
        
        return lstValue;
    }
	
	private List<PdfCell> getPdfCellValueFormsReponse(FormResponse formresponse )
    {
    	List<PdfCell> listContent = new ArrayList<>( );

        // Filters the FormResponseStep with at least one question exportable in pdf
        List<FormResponseStep> filteredList = formresponse.getSteps( ).stream( ).filter(
                frs -> frs.getQuestions( ).stream( ).map( FormQuestionResponse::getQuestion ).map( Question::getEntry ).anyMatch( Entry::isExportablePdf ) )
                .collect( Collectors.toList( ) );
        for ( FormResponseStep formResponseStep : filteredList )
        {
        	//lstValue.add(createPdfCell(formResponseStep, 0 ));
        	listContent.addAll( createCellsForStep( formResponseStep ) );
        }
        
        return listContent;
    }
	
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

    private PdfCell createPdfCellNoGroup( FormResponseStep formResponseStep, FormDisplay formDisplay )
    {
        return createPdfCell( formResponseStep, formDisplay, null, 0 );
    }
    
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
            	listResponseValue.add("<input type=\"checkbox\" name=\"" + strResponseValue + " \" checked=\"checked\" />");
            }
            
            if ( ( entryTypeService instanceof EntryTypeImage || entryTypeService instanceof EntryTypeCamera ) && strResponseValue != null )
            {
            	
            	if ( response.getFile( ) != null )
            	{
            		IFileStoreServiceProvider fileStoreService = FileService.getInstance( ).getFileStoreServiceProvider( );
                	fr.paris.lutece.portal.business.file.File fileImage = fileStoreService.getFile( String.valueOf( response.getFile( ).getIdFile( ) ) );
                	
                	String encoded = Base64.getEncoder().encodeToString( fileImage.getPhysicalFile( ).getValue( ) );
                	
                	listResponseValue.add("<img src=\"data:image/jpeg;base64, " + encoded + " \" width=\"100px\" height=\"100px\" /> ");
            	}
            	
            }
            
            if ( entryTypeService instanceof EntryTypeRadioButton && strResponseValue != null )
            {
            	listResponseValue.add("<input type=\"radio\" value=\"" + strResponseValue + " \" name=\"" + strResponseValue + " \" checked=\"checked\" />");
            }
            
            if ( entryTypeService instanceof EntryTypeSelect && strResponseValue != null )
            {
            	listResponseValue.add("<select name=\"" + strResponseValue + " \" > <option>" + strResponseValue + "</option> </select>");
            }
            
            if ( strResponseValue != null )
            {
                listResponseValue.add( strResponseValue );
            }
        }

        return listResponseValue;
    }
    
    private void writeExportFile( Path directoryFile ) throws IOException
    {
    	Map<String, Object> model = new HashMap<>( );
    	String strError = "";
    	
    	model.put( MARK_STEP_FORMS, getPdfCellValueFormsReponse( _formResponse ) );
    	HtmlTemplate htmltemplate = AppTemplateService.getTemplate( _template, Locale.getDefault(), model );
    	
    	try ( OutputStream outputStream =  Files.newOutputStream( directoryFile.resolve( generateFileName( _formResponse ) + ".pdf" ) ) )
        {	
    		Document doc = Jsoup.parse(htmltemplate.getHtml(), "UTF-8");
        	doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        	doc.outputSettings().escapeMode(EscapeMode.base.xhtml);
            doc.outputSettings().charset("UTF-8");
    		
            PdfConverterService.getInstance().getPdfBuilder().reset().withHtmlContent( doc.html() ).notEditable().render(outputStream);
        } 
        catch ( PdfConverterServiceException e )
        {
        	strError = "Une erreur s'est produite lors de la generation de l'edition";
            AppLogService.error( strError, e );
            throw new RuntimeException( strError, e );
        } 
        catch (IOException e) 
        {
        	strError = "Une erreur s'est produite lors de la generation de l'edition";
            AppLogService.error( strError, e );
            throw new RuntimeException( strError, e );
		}
    	
    }

}
