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
package fr.paris.lutece.plugins.workflow.modules.formspdf.web.task;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;


import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.workflow.modules.formspdf.business.FormsPDFTaskTemplate;
import fr.paris.lutece.test.LuteceTestCase;

/**
 * Unit tests for the private helper methods of {@link FormsPDFTaskTemplateJspBean}: populateFormsPDFTaskTemplate, convertMacroToSuppMinor,
 * convertMacroToSquareBrackets.
 */
public class FormsPDFTaskTemplateJspBeanTest extends LuteceTestCase
{
    private static final String PARAMETER_TEMPLATE_NAME = "template_name";
    private static final String PARAMETER_TEMPLATE_ID_FORM = "template_id_form";
    private static final String PARAMETER_TEMPLATE_ASSOCIATE_FORM = "template_associate_form";
    private static final String PARAMETER_TEMPLATE_CONTENT = "template_content";
    private static final String PARAMETER_RICH_TEXT_EDITOR = "rte";
    private static final String PARAMETER_TEMPLATE_FILE_NAME = "template_filename";
    private static final String PARAMETER_TEMPLATE_REPLACE_EMPTY = "checkbox_replaceEmpty";

    // ---------- Fake HttpServletRequest, no external library needed ----------

    /**
     * Builds a minimal HttpServletRequest fake backed by a parameter map. getParameter(name) returns the mapped value; every other method returns a sensible
     * default (null / false / 0) since it is never used by the methods under test.
     */
    private HttpServletRequest fakeRequest( Map<String, String> parameters )
    {
        InvocationHandler handler = ( proxy, method, args ) -> {
            if ( "getParameter".equals( method.getName( ) ) && args != null && args.length == 1 )
            {
                return parameters.get( (String) args [0] );
            }
            Class<?> returnType = method.getReturnType( );
            if ( returnType == boolean.class )
            {
                return false;
            }
            if ( returnType == int.class )
            {
                return 0;
            }
            if ( returnType == long.class )
            {
                return 0L;
            }
            return null;
        };
        return (HttpServletRequest) Proxy.newProxyInstance( getClass( ).getClassLoader( ), new Class<?> [ ] {
                HttpServletRequest.class
        }, handler );
    }

    // ---------- Helpers to invoke private methods via reflection ----------

    private FormsPDFTaskTemplate invokePopulate( HttpServletRequest request, FormsPDFTaskTemplate template ) throws Exception
    {
        Method method = FormsPDFTaskTemplateJspBean.class.getDeclaredMethod( "populateFormsPDFTaskTemplate", HttpServletRequest.class,
                FormsPDFTaskTemplate.class );
        method.setAccessible( true );
        FormsPDFTaskTemplateJspBean jspBean = new FormsPDFTaskTemplateJspBean( );
        return (FormsPDFTaskTemplate) method.invoke( jspBean, request, template );
    }

    private String invokeConvertMacroToSuppMinor( String strTemplate ) throws Exception
    {
        Method method = FormsPDFTaskTemplateJspBean.class.getDeclaredMethod( "convertMacroToSuppMinor", String.class );
        method.setAccessible( true );
        FormsPDFTaskTemplateJspBean jspBean = new FormsPDFTaskTemplateJspBean( );
        return (String) method.invoke( jspBean, strTemplate );
    }

    private String invokeConvertMacroToSquareBrackets( String strTemplate ) throws Exception
    {
        Method method = FormsPDFTaskTemplateJspBean.class.getDeclaredMethod( "convertMacroToSquareBrackets", String.class );
        method.setAccessible( true );
        FormsPDFTaskTemplateJspBean jspBean = new FormsPDFTaskTemplateJspBean( );
        return (String) method.invoke( jspBean, strTemplate );
    }

    // ---------- populateFormsPDFTaskTemplate ----------

    @Test
    public void testPopulateWithAssociatedFormSetsIdForm( ) throws Exception
    {
        Map<String, String> params = new HashMap<>( );
        params.put( PARAMETER_TEMPLATE_NAME, "Mon template" );
        params.put( PARAMETER_TEMPLATE_ASSOCIATE_FORM, "true" );
        params.put( PARAMETER_TEMPLATE_ID_FORM, "5" );
        params.put( PARAMETER_TEMPLATE_CONTENT, "<p>contenu</p>" );
        params.put( PARAMETER_RICH_TEXT_EDITOR, "true" );
        params.put( PARAMETER_TEMPLATE_FILE_NAME, "export.pdf" );
        params.put( PARAMETER_TEMPLATE_REPLACE_EMPTY, "true" );

        FormsPDFTaskTemplate template = new FormsPDFTaskTemplate( );
        invokePopulate( fakeRequest( params ), template );

        assertEquals( "Mon template", template.getName( ) );
        assertFalse( template.isGeneric( ) );
        assertEquals( 5, template.getIdForm( ) );
        assertEquals( "<p>contenu</p>", template.getContent( ) );
        assertTrue( template.isRte( ) );
        assertEquals( "export.pdf", template.getFileName( ) );
        assertTrue( template.isReplaceEmpty( ) );
    }

    @Test
    public void testPopulateWithoutAssociatedFormUsesDefaultIdForm( ) throws Exception
    {
        Map<String, String> params = new HashMap<>( );
        params.put( PARAMETER_TEMPLATE_NAME, "Générique" );
        params.put( PARAMETER_TEMPLATE_ASSOCIATE_FORM, "false" );
        params.put( PARAMETER_TEMPLATE_CONTENT, "<p>contenu générique</p>" );
        params.put( PARAMETER_RICH_TEXT_EDITOR, "false" );
        params.put( PARAMETER_TEMPLATE_FILE_NAME, "" );
        params.put( PARAMETER_TEMPLATE_REPLACE_EMPTY, "false" );

        FormsPDFTaskTemplate template = new FormsPDFTaskTemplate( );
        invokePopulate( fakeRequest( params ), template );

        assertTrue( template.isGeneric( ) );
        assertEquals( FormsPDFTaskTemplateJspBean.DEFAULT_ID_VALUE, template.getIdForm( ) );
        assertFalse( template.isRte( ) );
        assertFalse( template.isReplaceEmpty( ) );
    }

    @Test
    public void testPopulateWithMissingReplaceEmptyParameterDefaultsToFalse( ) throws Exception
    {
        // Simulates an unchecked checkbox: the parameter is simply absent from the request
        Map<String, String> params = new HashMap<>( );
        params.put( PARAMETER_TEMPLATE_NAME, "Test" );
        params.put( PARAMETER_TEMPLATE_ASSOCIATE_FORM, "false" );
        params.put( PARAMETER_TEMPLATE_CONTENT, "contenu" );
        params.put( PARAMETER_RICH_TEXT_EDITOR, "false" );
        // PARAMETER_TEMPLATE_FILE_NAME and PARAMETER_TEMPLATE_REPLACE_EMPTY intentionally omitted

        FormsPDFTaskTemplate template = new FormsPDFTaskTemplate( );
        invokePopulate( fakeRequest( params ), template );

        assertFalse( template.isReplaceEmpty( ) );
    }

    // ---------- convertMacroToSuppMinor ----------

    @Test
    public void testConvertMacroToSuppMinorReplacesSquareBrackets( ) throws Exception
    {
        String input = "reponse : [@displayEntry q=position_3/]";
        String result = invokeConvertMacroToSuppMinor( input );
        assertEquals( "reponse : <@displayEntry q=position_3/>", result );
    }

    @Test
    public void testConvertMacroToSuppMinorWithNullReturnsNull( ) throws Exception
    {
        assertNull( invokeConvertMacroToSuppMinor( null ) );
    }

    @Test
    public void testConvertMacroToSuppMinorWithNoMacroLeavesUnchanged( ) throws Exception
    {
        String input = "aucun signet ici";
        assertEquals( input, invokeConvertMacroToSuppMinor( input ) );
    }

    // ---------- convertMacroToSquareBrackets ----------

    @Test
    public void testConvertMacroToSquareBracketsReplacesAngleBrackets( ) throws Exception
    {
        String input = "reponse : <@displayEntry q=position_7/>";
        String result = invokeConvertMacroToSquareBrackets( input );
        assertEquals( "reponse : [@displayEntry q=position_7/]", result );
    }

    @Test
    public void testConvertMacroToSquareBracketsWithNullReturnsNull( ) throws Exception
    {
        assertNull( invokeConvertMacroToSquareBrackets( null ) );
    }

    @Test
    public void testRoundTripConversionIsIdempotent( ) throws Exception
    {
        String original = "[@displayEntry q=position_1/]";
        String converted = invokeConvertMacroToSuppMinor( original );
        String backToOriginal = invokeConvertMacroToSquareBrackets( converted );
        assertEquals( original, backToOriginal );
    }
}
