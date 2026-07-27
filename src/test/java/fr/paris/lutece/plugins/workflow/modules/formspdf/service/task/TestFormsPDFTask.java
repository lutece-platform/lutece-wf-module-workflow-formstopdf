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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import fr.paris.lutece.plugins.forms.business.FormQuestionResponse;
import fr.paris.lutece.plugins.forms.business.Question;
import fr.paris.lutece.plugins.genericattributes.business.Entry;
import fr.paris.lutece.plugins.genericattributes.business.Response;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.test.LuteceTestCase;

/**
 * Unit tests for the private helper methods of {@link FormsPDFTask} :
 * replaceNullValues and removeNullEntries.
 *
 * These methods are private, so they are invoked via reflection. This test
 * follows the same conventions (JUnit3 style, extends LuteceTestCase) as the
 * existing {@code TestFormsToPdf} test in this module.
 */
public class TestFormsPDFTask extends LuteceTestCase
{
    private static final Locale FR = Locale.FRENCH;
    private static final String EMPTY_RESPONSE_KEY = "module.workflow.formspdf.modify.template.replaceEmpty.defaultValue";

    // ---------- Reflection helpers ----------

    private void invokereplaceNullValues( Map<String, Object> model, Locale locale ) throws Exception
    {
        Method method = FormsPDFTask.class.getDeclaredMethod( "replaceNullValues", Map.class, Locale.class );
        method.setAccessible( true );
        FormsPDFTask task = new FormsPDFTask( );
        method.invoke( task, model, locale );
    }

    private void invokeRemoveNullEntries( Map<String, Object> model ) throws Exception
    {
        Method method = FormsPDFTask.class.getDeclaredMethod( "removeNullEntries", Map.class );
        method.setAccessible( true );
        FormsPDFTask task = new FormsPDFTask( );
        method.invoke( task, model );
    }

    /**
     * Builds a FormQuestionResponse with a Question (and its Entry, possibly null)
     * and a list of Response objects carrying the given response values.
     */
    private FormQuestionResponse buildFormQuestionResponse( Entry entry, String... responseValues ) throws Exception
    {
        Question question = new Question( );
        setField( question, "_entry", entry );

        FormQuestionResponse fqr = new FormQuestionResponse( );
        setField( fqr, "_question", question );

        List<Response> responses = new ArrayList<>( );
        for ( String value : responseValues )
        {
            Response response = new Response( );
            response.setResponseValue( value );
            responses.add( response );
        }
        setField( fqr, "_entryResponses", responses );

        return fqr;
    }

    /**
     * Sets a private field by name via reflection, walking up the class hierarchy
     * if needed (useful since some Lutece business objects declare fields on a
     * parent class).
     */
    private void setField( Object target, String fieldName, Object value ) throws Exception
    {
        Class<?> clazz = target.getClass( );
        while ( clazz != null )
        {
            try
            {
                Field field = clazz.getDeclaredField( fieldName );
                field.setAccessible( true );
                field.set( target, value );
                return;
            }
            catch ( NoSuchFieldException e )
            {
                clazz = clazz.getSuperclass( );
            }
        }
        throw new NoSuchFieldException( fieldName + " not found on " + target.getClass( ) );
    }

    // ---------- replaceNullValues ----------

    public void testReplaceNullValuesReplacesEmptyResponseValue( ) throws Exception
    {
        Map<String, Object> model = new HashMap<>( );
        FormQuestionResponse fqr = buildFormQuestionResponse( new Entry( ), "" );
        model.put( "position_1", fqr );

        invokereplaceNullValues( model, FR );

        String expected = I18nService.getLocalizedString( EMPTY_RESPONSE_KEY, FR );
        assertEquals( expected, fqr.getEntryResponse( ).get( 0 ).getResponseValue( ) );
        assertEquals( expected, fqr.getEntryResponse( ).get( 0 ).getToStringValueResponse( ) );
    }

    public void testReplaceNullValuesReplacesNullResponseValue( ) throws Exception
    {
        Map<String, Object> model = new HashMap<>( );
        FormQuestionResponse fqr = buildFormQuestionResponse( new Entry( ), (String) null );
        model.put( "position_1", fqr );

        invokereplaceNullValues( model, FR );

        String expected = I18nService.getLocalizedString( EMPTY_RESPONSE_KEY, FR );
        assertEquals( expected, fqr.getEntryResponse( ).get( 0 ).getResponseValue( ) );
    }

    public void testReplaceNullValuesDoesNotTouchNonEmptyValue( ) throws Exception
    {
        Map<String, Object> model = new HashMap<>( );
        FormQuestionResponse fqr = buildFormQuestionResponse( new Entry( ), "réponse saisie" );
        model.put( "position_1", fqr );

        invokereplaceNullValues( model, FR );

        assertEquals( "réponse saisie", fqr.getEntryResponse( ).get( 0 ).getResponseValue( ) );
    }

    public void testReplaceNullValuesIgnoresKeysWithoutPositionPrefix( ) throws Exception
    {
        Map<String, Object> model = new HashMap<>( );
        FormQuestionResponse fqr = buildFormQuestionResponse( new Entry( ), "" );
        // key does NOT contain "position_" -> must be left untouched
        model.put( "form_title", fqr );

        invokereplaceNullValues( model, FR );

        assertEquals( "", fqr.getEntryResponse( ).get( 0 ).getResponseValue( ) );
    }

    public void testReplaceNullValuesHandlesMultipleResponsesInSameQuestion( ) throws Exception
    {
        Map<String, Object> model = new HashMap<>( );
        FormQuestionResponse fqr = buildFormQuestionResponse( new Entry( ), "", "valeur", "" );
        model.put( "position_2", fqr );

        invokereplaceNullValues( model, FR );

        String expected = I18nService.getLocalizedString( EMPTY_RESPONSE_KEY, FR );
        List<Response> responses = fqr.getEntryResponse( );
        assertEquals( expected, responses.get( 0 ).getResponseValue( ) );
        assertEquals( "valeur", responses.get( 1 ).getResponseValue( ) );
        assertEquals( expected, responses.get( 2 ).getResponseValue( ) );
    }

    public void testReplaceNullValuesWithNullFormQuestionResponseDoesNotThrow( ) throws Exception
    {
        Map<String, Object> model = new HashMap<>( );
        model.put( "position_absent", null );

        // Should not throw, thanks to the Optional.ofNullable guard in replaceNullValues
        invokereplaceNullValues( model, FR );
    }

    public void testReplaceNullValuesOnEmptyModelDoesNotThrow( ) throws Exception
    {
        Map<String, Object> model = new HashMap<>( );

        invokereplaceNullValues( model, FR );

        assertTrue( model.isEmpty( ) );
    }

    // ---------- removeNullEntries ----------

    public void testRemoveNullEntriesRemovesEntryWithNullEntry( ) throws Exception
    {
        Map<String, Object> model = new HashMap<>( );
        // entry == null on the question -> this position must be removed
        FormQuestionResponse fqr = buildFormQuestionResponse( null, "valeur" );
        model.put( "position_1", fqr );

        invokeRemoveNullEntries( model );

        assertFalse( model.containsKey( "position_1" ) );
    }

    public void testRemoveNullEntriesRemovesNullFormQuestionResponse( ) throws Exception
    {
        Map<String, Object> model = new HashMap<>( );
        model.put( "position_1", null );

        invokeRemoveNullEntries( model );

        assertFalse( model.containsKey( "position_1" ) );
    }

    public void testRemoveNullEntriesKeepsValidEntry( ) throws Exception
    {
        Map<String, Object> model = new HashMap<>( );
        FormQuestionResponse fqr = buildFormQuestionResponse( new Entry( ), "valeur" );
        model.put( "position_1", fqr );

        invokeRemoveNullEntries( model );

        assertTrue( model.containsKey( "position_1" ) );
    }

    public void testRemoveNullEntriesIgnoresKeysWithoutPositionPrefix( ) throws Exception
    {
        Map<String, Object> model = new HashMap<>( );
        // Not a "position_" key, and not even a FormQuestionResponse : must be left as-is
        model.put( "form_title", "Mon formulaire" );

        invokeRemoveNullEntries( model );

        assertEquals( "Mon formulaire", model.get( "form_title" ) );
    }

    public void testRemoveNullEntriesOnEmptyModelDoesNotThrow( ) throws Exception
    {
        Map<String, Object> model = new HashMap<>( );

        invokeRemoveNullEntries( model );

        assertTrue( model.isEmpty( ) );
    }
}