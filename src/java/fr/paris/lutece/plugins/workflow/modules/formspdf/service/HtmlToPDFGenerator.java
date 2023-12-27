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
package fr.paris.lutece.plugins.workflow.modules.formspdf.service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import fr.paris.lutece.plugins.forms.business.Form;
import fr.paris.lutece.plugins.forms.business.FormHome;
import fr.paris.lutece.plugins.forms.service.provider.GenericFormsProvider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities.EscapeMode;

import fr.paris.lutece.plugins.forms.business.FormResponse;
import fr.paris.lutece.plugins.html2pdf.service.PdfConverterService;
import fr.paris.lutece.plugins.html2pdf.service.PdfConverterServiceException;
import fr.paris.lutece.plugins.workflow.modules.formspdf.business.FormsPDFTaskTemplate;
import fr.paris.lutece.plugins.workflowcore.service.provider.InfoMarker;
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
     * @param formsPDFTaskTemplate
     *            the template
     */
    public HtmlToPDFGenerator( String fileName, String fileDescription, FormResponse formResponse, FormsPDFTaskTemplate formsPDFTaskTemplate )
    {
        super( fileName, fileDescription, formResponse, formsPDFTaskTemplate );
    }

    private static final boolean ZIP_EXPORT = Boolean.parseBoolean( AppPropertiesService.getProperty( "workflow-formspdf.export.pdf.zip", "false" ) );
    private static final String CONSTANT_MIME_TYPE_PDF = "application/pdf";
    private static final String CONSTANT_FORM_TITLE = "form_title";
    private static final String EXTENSION_PDF = ".pdf";
    

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
        // markers
        Form form = FormHome.findByPrimaryKey( _formsPDFTaskTemplate.getIdForm());
        Collection<InfoMarker> collectionNotifyMarkers = GenericFormsProvider.getProviderMarkerDescriptions(form);
        
        markersToModel(model, collectionNotifyMarkers);
        
        // add title
        model.put( CONSTANT_FORM_TITLE , (form != null)?form.getTitle():"" );
        
        HtmlTemplate htmltemplate = AppTemplateService.getTemplateFromStringFtl(_formsPDFTaskTemplate.getContent(), Locale.getDefault( ), model);
        
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
    
    private void markersToModel( Map<String, Object> model, Collection<InfoMarker> collectionInfoMarkers )
    {
        for ( InfoMarker infoMarker : collectionInfoMarkers )
        {
            model.put( infoMarker.getMarker(), infoMarker.getValue() );           
        }
    }

}
