/*
 * Copyright (c) 2002-2026, City of Paris
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
package fr.paris.lutece.plugins.workflow.modules.formspdf.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import freemarker.core.HTMLOutputFormat;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Renders a PDF model stored in workflow_task_formspdf_template, a FreeMarker template with the markers of the answers, as
 * FormsPDFTask does with the string templates of AppTemplateService: a template named after the hash of its content, so
 * without extension, has the HTML output format with auto-escaping (LUT-33153). Before the upgrade script a stored ?html
 * no longer parses; after it, the HTML written by the administrator is kept, an XML declaration included, and the values
 * of the answers are escaped once. The upgrade script is read and applied as the database would. Only the FreeMarker
 * engine is needed.
 */
public class FormsPDFStoredModelUpgradeTest
{
    private static final String UPGRADE_SCRIPT = "src/sql/plugins/workflow/modules/formstopdf/upgrade/update_db_workflow-formstopdf-2.0.2-2.0.3.sql";

    private static final Pattern UPDATE = Pattern.compile( "UPDATE workflow_task_formspdf_template SET (\\w+) =(.*?)\\s+WHERE", Pattern.DOTALL );
    private static final Pattern REPLACED = Pattern.compile( "'([^']*)',\\s*'([^']*)'" );

    /** A typical PDF model of a site, written before auto-escaping. */
    private static final String CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><html><head><title>Export ${form_title?html}</title></head>"
            + "<body><h1 style=\"text-align: center;\">${form_title!?html}</h1><p>Name: ${name?xhtml}, city: ${city}</p></body></html>";
    private static final String CONTENT_ESCAPE = "<#escape x as x?html><p>${form_title}</p></#escape>";
    private static final String CONTENT_RTE = "[#ftl][#escape x as x?html]<p>${form_title}</p>[/#escape]";
    private static final String FILE_NAME = "${form_title?html}_${name}";

    private static final String TITLE = "Zoé <b>\"l'école\"</b> & co";
    private static final String TITLE_ESCAPED = "Zoé &lt;b&gt;&quot;l&#39;école&quot;&lt;/b&gt; &amp; co";
    private static final Map<String, Object> MODEL = Map.of( "form_title", TITLE, "name", "Martin", "city", "Paris <15e>" );

    /**
     * Renders a stored model as the string templates of the core are rendered.
     *
     * @param strTemplate
     *            the stored model
     * @return the rendering
     * @throws IOException
     *             if the model does not parse
     * @throws TemplateException
     *             if the rendering fails
     */
    private static String render( String strTemplate ) throws IOException, TemplateException
    {
        Configuration cfg = new Configuration( Configuration.VERSION_2_3_0 );
        cfg.setOutputFormat( HTMLOutputFormat.INSTANCE );
        cfg.setAutoEscapingPolicy( Configuration.ENABLE_IF_DEFAULT_AUTO_ESCAPING_POLICY );
        StringWriter writer = new StringWriter( );
        new Template( "a1b2c3", new StringReader( strTemplate ), cfg ).process( MODEL, writer );

        return writer.toString( );
    }

    /**
     * Reads the replacements the upgrade script applies to each column, in the order of its REPLACE calls (the innermost first).
     *
     * @return the replacements (searched, replacement) by column
     * @throws IOException
     *             if the script cannot be read
     */
    private static Map<String, List<String [ ]>> upgrade( ) throws IOException
    {
        String strScript = Files.readString( Path.of( UPGRADE_SCRIPT ), StandardCharsets.UTF_8 );
        Map<String, List<String [ ]>> mapReplaced = new LinkedHashMap<>( );
        Matcher update = UPDATE.matcher( strScript );
        while ( update.find( ) )
        {
            List<String [ ]> listReplacements = new ArrayList<>( );
            Matcher replaced = REPLACED.matcher( update.group( 2 ) );
            while ( replaced.find( ) )
            {
                listReplacements.add( new String [ ] {
                        replaced.group( 1 ), replaced.group( 2 )
                } );
            }
            mapReplaced.put( update.group( 1 ), listReplacements );
        }
        return mapReplaced;
    }

    /**
     * Applies the upgrade script to a stored value of a column, as REPLACE does.
     *
     * @param strColumn
     *            the column
     * @param strValue
     *            the stored value
     * @return the value after the upgrade
     * @throws IOException
     *             if the script cannot be read
     */
    private static String applyUpgrade( String strColumn, String strValue ) throws IOException
    {
        String strUpgraded = strValue;
        for ( String [ ] replacement : upgrade( ).get( strColumn ) )
        {
            strUpgraded = strUpgraded.replace( replacement [0], replacement [1] );
        }
        return strUpgraded;
    }

    /**
     * Before the upgrade, a stored ?html (or an escape directive) stops the parse: the PDF generation would fail when the task runs.
     */
    @Test
    public void testStoredHandEscapingNoLongerParses( )
    {
        assertThrows( ParseException.class, ( ) -> render( CONTENT ) );
        assertThrows( ParseException.class, ( ) -> render( CONTENT_ESCAPE ) );
        assertThrows( ParseException.class, ( ) -> render( CONTENT_RTE ) );
        assertThrows( ParseException.class, ( ) -> render( FILE_NAME ) );
    }

    /**
     * After the upgrade, the model keeps its HTML and its XML declaration, and every answer is escaped once, the one written with ?html as
     * before, the one written without it as well (it was written raw).
     *
     * @throws Exception
     *             if the rendering fails
     */
    @Test
    public void testUpgradedModelKeepsItsHtmlAndEscapesTheAnswers( ) throws Exception
    {
        String strHtml = render( applyUpgrade( "content", CONTENT ) );

        assertEquals( "<?xml version=\"1.0\" encoding=\"UTF-8\"?><html><head><title>Export " + TITLE_ESCAPED + "</title></head>"
                + "<body><h1 style=\"text-align: center;\">" + TITLE_ESCAPED + "</h1><p>Name: Martin, city: Paris &lt;15e&gt;</p></body></html>", strHtml );
        assertEquals( "<p>" + TITLE_ESCAPED + "</p>", render( applyUpgrade( "content", CONTENT_ESCAPE ) ) );
        assertEquals( "<p>" + TITLE_ESCAPED + "</p>", render( applyUpgrade( "content", CONTENT_RTE ) ) );
        assertEquals( TITLE_ESCAPED + "_Martin", render( applyUpgrade( "file_name", FILE_NAME ) ) );
    }

    /**
     * The upgrade script covers the two templated columns, removes every hand escaping built-in, keeps an XML declaration, and a second run
     * changes nothing.
     *
     * @throws Exception
     *             if the script cannot be read
     */
    @Test
    public void testUpgradeCoversEveryColumnAndIsIdempotent( ) throws Exception
    {
        assertEquals( List.of( "content", "file_name" ), new ArrayList<>( upgrade( ).keySet( ) ) );
        for ( String strColumn : List.of( "content", "file_name" ) )
        {
            String strOnce = applyUpgrade( strColumn, "${a!?html} ${b?xhtml} ${c?xml} ${d?rtf} ${e?web_safe}" );
            assertEquals( "${a!} ${b} ${c} ${d} ${e}", strOnce, strColumn );
            assertEquals( strOnce, applyUpgrade( strColumn, strOnce ), strColumn );
        }
        String strDeclaration = "<?xml version=\"1.0\"?><p>${a}</p>";
        assertEquals( strDeclaration, applyUpgrade( "content", strDeclaration ) );
        String strUpgraded = applyUpgrade( "content", CONTENT );
        assertEquals( strUpgraded, applyUpgrade( "content", strUpgraded ) );
        assertTrue( strUpgraded.startsWith( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" ), strUpgraded );
    }
}
