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
package fr.paris.lutece.plugins.workflow.modules.formspdf.service.task;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.forms.business.FormQuestionResponse;
import fr.paris.lutece.plugins.genericattributes.business.Response;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.test.LuteceTestCase;

/**
 * Unit tests for {@link FormsPDFTask#replaceNullEntries(Map, Locale)}. The method is private, so it is invoked via reflection.
 */
public class FormsPDFTaskReplaceNullEntriesTest extends LuteceTestCase
{
    private static final Locale FR = Locale.FRENCH;
    private static final String EMPTY_RESPONSE_KEY = "module.workflow.formspdf.modify.template.replaceEmpty.defaultValue";

    /**
     * Invokes the private replaceNullEntries method via reflection.
     */
    @SuppressWarnings( "unchecked" )
    private void invokeReplaceNullEntries( Map<String, Object> model, Locale locale ) throws Exception
    {
        Method method = FormsPDFTask.class.getDeclaredMethod( "replaceNullEntries", Map.class, Locale.class );
        method.setAccessible( true );
        FormsPDFTask task = new FormsPDFTask( );
        method.invoke( task, model, locale );
    }

    private FormQuestionResponse buildFormQuestionResponse( String... responseValues )
    {
        FormQuestionResponse fqr = new FormQuestionResponse( );
        List<Response> responses = new java.util.ArrayList<>( );
        for ( String value : responseValues )
        {
            Response response = new Response( );
            response.setResponseValue( value );
            responses.add( response );
        }
        fqr.setEntryResponse( responses );
        return fqr;
    }

    @Test
    public void testEmptyResponseValueIsReplacedByLocalizedDefault( ) throws Exception
    {
        Map<String, Object> model = new HashMap<>( );
        FormQuestionResponse fqr = buildFormQuestionResponse( "" );
        model.put( "code_question_1", fqr );

        invokeReplaceNullEntries( model, FR );

        String expected = I18nService.getLocalizedString( EMPTY_RESPONSE_KEY, FR );
        assertEquals( expected, fqr.getEntryResponse( ).get( 0 ).getResponseValue( ) );
    }

    @Test
    public void testNullResponseValueIsReplacedByLocalizedDefault( ) throws Exception
    {
        Map<String, Object> model = new HashMap<>( );
        FormQuestionResponse fqr = buildFormQuestionResponse( (String) null );
        model.put( "code_question_1", fqr );

        invokeReplaceNullEntries( model, FR );

        String expected = I18nService.getLocalizedString( EMPTY_RESPONSE_KEY, FR );
        assertEquals( expected, fqr.getEntryResponse( ).get( 0 ).getResponseValue( ) );
    }

    @Test
    public void testNonEmptyResponseValueIsNotModified( ) throws Exception
    {
        Map<String, Object> model = new HashMap<>( );
        FormQuestionResponse fqr = buildFormQuestionResponse( "réponse existante" );
        model.put( "code_question_1", fqr );

        invokeReplaceNullEntries( model, FR );

        assertEquals( "réponse existante", fqr.getEntryResponse( ).get( 0 ).getResponseValue( ) );
    }

    @Test
    public void testKeysWithoutCodePrefixAreIgnored( ) throws Exception
    {
        Map<String, Object> model = new HashMap<>( );
        FormQuestionResponse fqr = buildFormQuestionResponse( "" );
        model.put( "position_1", fqr );

        invokeReplaceNullEntries( model, FR );

        assertEquals( "", fqr.getEntryResponse( ).get( 0 ).getResponseValue( ) );
    }

    @Test
    public void testMultipleResponsesInSameQuestionAreAllProcessed( ) throws Exception
    {
        Map<String, Object> model = new HashMap<>( );
        FormQuestionResponse fqr = buildFormQuestionResponse( "", "valeur", "" );
        model.put( "code_question_multi", fqr );

        invokeReplaceNullEntries( model, FR );

        String expected = I18nService.getLocalizedString( EMPTY_RESPONSE_KEY, FR );
        List<Response> responses = fqr.getEntryResponse( );
        assertEquals( expected, responses.get( 0 ).getResponseValue( ) );
        assertEquals( "valeur", responses.get( 1 ).getResponseValue( ) );
        assertEquals( expected, responses.get( 2 ).getResponseValue( ) );
    }

    @Test
    public void testNullFormQuestionResponseInModelDoesNotThrow( ) throws Exception
    {
        Map<String, Object> model = new HashMap<>( );
        model.put( "code_question_absent", null );

        invokeReplaceNullEntries( model, FR );
    }

    @Test
    public void testEmptyModelDoesNotThrow( ) throws Exception
    {
        Map<String, Object> model = new HashMap<>( );
        invokeReplaceNullEntries( model, FR );
        assertTrue( model.isEmpty( ) );
    }
}
