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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
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

/**
 * The changeset that decodes, once, the entities the removed input filter of the core (sanitizeFilterMode, LUT-34109) wrote into the
 * PDF models. The upgrade script is read and its REPLACE calls applied as the database would, to values the filter wrote (outputs of
 * its OWASP policy). Only the FreeMarker engine is needed.
 */
public class FormsPDFDecodeEntitiesUpgradeTest
{
    private static final String UPGRADE_SCRIPT = "src/sql/plugins/workflow/modules/formstopdf/upgrade/update_db_workflow-formstopdf-2.0.2-2.0.3.sql";
    private static final String CHANGESET_DECODE = "-- changeset workflow-formstopdf:update_db_workflow-formstopdf-2.0.2-2.0.3.sql-decode-entities\n";
    private static final String CHANGESET_HAND_ESCAPING = "-- changeset workflow-formstopdf:update_db_workflow-formstopdf-2.0.2-2.0.3.sql\n";

    /** The chain of lutece-core (update_db_lutece_core-8.0.1-8.0.2-rev20): &amp; last, so that a value is decoded once. */
    private static final List<String> CORE_CHAIN = List.of( "&#39;", "'", "&#x27;", "'", "&#34;", "\"", "&quot;", "\"", "&#96;", "`", "&#61;", "=",
            "&#64;", "@", "&#43;", "+", "&lt;", "<", "&gt;", ">", "&amp;", "&" );

    private static final Pattern UPDATE = Pattern.compile( "UPDATE workflow_task_formspdf_template SET (\\w+) =(.*?)\\s+WHERE(.*?);[ \\t]*(?:\\r?\\n|$)", Pattern.DOTALL );
    private static final Pattern REPLACED = Pattern.compile( "'((?:[^']|'')*)',\\s*'((?:[^']|'')*)'" );

    /**
     * Reads the UPDATE statements of a changeset of the upgrade script.
     *
     * @param strChangeset
     *            the header line of the changeset
     * @return by column, the WHERE clause and the replacements (searched, replacement) in the order of the REPLACE calls
     * @throws IOException
     *             if the script cannot be read
     */
    private static Map<String, Object [ ]> changeset( String strChangeset ) throws IOException
    {
        String strScript = Files.readString( Path.of( UPGRADE_SCRIPT ), StandardCharsets.UTF_8 );
        int nStart = strScript.indexOf( strChangeset );
        assertTrue( nStart >= 0, strChangeset );
        int nEnd = strScript.indexOf( "-- changeset ", nStart + strChangeset.length( ) );
        String strSection = strScript.substring( nStart, nEnd < 0 ? strScript.length( ) : nEnd );

        Map<String, Object [ ]> mapUpdates = new LinkedHashMap<>( );
        Matcher update = UPDATE.matcher( strSection );
        while ( update.find( ) )
        {
            List<String> listReplacements = new ArrayList<>( );
            Matcher replaced = REPLACED.matcher( update.group( 2 ) );
            while ( replaced.find( ) )
            {
                listReplacements.add( replaced.group( 1 ).replace( "''", "'" ) );
                listReplacements.add( replaced.group( 2 ).replace( "''", "'" ) );
            }
            mapUpdates.put( update.group( 1 ), new Object [ ] {
                    update.group( 3 ).trim( ), listReplacements
            } );
        }
        return mapUpdates;
    }

    /**
     * Applies the REPLACE calls of a changeset to a stored value of a column, as the database does.
     *
     * @param strChangeset
     *            the header line of the changeset
     * @param strColumn
     *            the column
     * @param strValue
     *            the stored value
     * @return the value after the changeset
     * @throws IOException
     *             if the script cannot be read
     */
    @SuppressWarnings( "unchecked" )
    private static String apply( String strChangeset, String strColumn, String strValue ) throws IOException
    {
        List<String> listReplacements = (List<String>) changeset( strChangeset ).get( strColumn ) [1];
        String strResult = strValue;
        for ( int i = 0; i < listReplacements.size( ); i += 2 )
        {
            strResult = strResult.replace( listReplacements.get( i ), listReplacements.get( i + 1 ) );
        }
        return strResult;
    }

    private static String decode( String strColumn, String strValue ) throws IOException
    {
        return apply( CHANGESET_DECODE, strColumn, strValue );
    }

    /**
     * Parses a stored model as FormsPDFTask does, with the HTML output format of the string templates of the core.
     *
     * @param strModel
     *            the model
     * @throws IOException
     *             if the model does not parse
     */
    private static void parse( String strModel ) throws IOException
    {
        Configuration cfg = new Configuration( Configuration.VERSION_2_3_0 );
        cfg.setOutputFormat( HTMLOutputFormat.INSTANCE );
        new Template( "a1b2c3", new StringReader( strModel ), cfg );
    }

    /**
     * The changeset decodes the plain text columns with the chain of the core, and the content only for a model typed in the textarea.
     *
     * @throws Exception
     *             if the script cannot be read
     */
    @Test
    public void testChangesetDecodesThePlainTextColumnsWithTheChainOfTheCore( ) throws Exception
    {
        Map<String, Object [ ]> mapUpdates = changeset( CHANGESET_DECODE );
        assertEquals( List.of( "name", "file_name", "content" ), new ArrayList<>( mapUpdates.keySet( ) ) );
        for ( Object [ ] update : mapUpdates.values( ) )
        {
            assertEquals( CORE_CHAIN, update [1] );
        }
        assertTrue( ( (String) mapUpdates.get( "content" ) [0] ).contains( "is_rte = 0" ), "the rich text models are left as they are" );
        String strScript = Files.readString( Path.of( UPGRADE_SCRIPT ), StandardCharsets.UTF_8 );
        assertTrue( strScript.indexOf( CHANGESET_DECODE ) < strScript.indexOf( CHANGESET_HAND_ESCAPING ),
                "the entities are decoded before the hand escaping is removed" );
    }

    /**
     * A value is decoded once: what the filter wrote comes back as typed, an entity typed by the administrator stays an entity.
     *
     * @throws Exception
     *             if the script cannot be read
     */
    @Test
    public void testValuesAreDecodedOnce( ) throws Exception
    {
        assertEquals( "Modèle l'été \"A\" & B `t` = x@y + 1", decode( "name", "Modèle l&#39;été &#34;A&#34; &amp; B &#96;t&#96; &#61; x@y + 1" ) );
        assertEquals( "Modèle l'été \"A\"", decode( "name", "Modèle l&#x27;été &quot;A&quot;" ) );
        assertEquals( "A &lt; B", decode( "name", "A &amp;lt; B" ) );
        assertEquals( "A &amp; B", decode( "name", "A &amp;amp; B" ) );
        assertEquals( "export_${form_title}_<x>", decode( "file_name", "export_${form_title}_&lt;x&gt;" ) );
        assertEquals( "Sans entité", decode( "name", "Sans entité" ) );
    }

    /**
     * A model typed in the textarea and saved through the filter no longer parses (directives and string literals encoded); decoded, it
     * is the source the administrator typed, and parses.
     *
     * @throws Exception
     *             if the script cannot be read
     */
    @Test
    public void testTextareaModelParsesOnceDecoded( ) throws Exception
    {
        String strTyped = "<p>Nom : ${name}</p><#if city??><p>${city?string(\"x\")}</p></#if><@displayEntry q=position_1/>";
        String strStored = "<p>Nom : ${name}</p>&lt;#if city??&gt;<p>${city?string(&#34;x&#34;)}</p>&lt;/#if&gt;&lt;@displayEntry q&#61;position_1/&gt;";

        assertThrows( ParseException.class, ( ) -> parse( strStored ) );
        assertEquals( strTyped, decode( "content", strStored ) );
    }

    /**
     * An escape directive the filter encoded is decoded first, then removed by the changeset of the hand escaping: the model parses with
     * auto-escaping.
     *
     * @throws Exception
     *             if the script cannot be read
     */
    @Test
    public void testEncodedEscapeDirectiveIsRemovedAfterDecoding( ) throws Exception
    {
        String strStored = "&lt;#escape x as x?html&gt;<p>${form_title}</p>&lt;/#escape&gt;";
        String strUpgraded = apply( CHANGESET_HAND_ESCAPING, "content", decode( "content", strStored ) );

        assertEquals( "<p>${form_title}</p>", strUpgraded );
        assertDoesNotThrow( ( ) -> parse( strUpgraded ) );
    }
}
