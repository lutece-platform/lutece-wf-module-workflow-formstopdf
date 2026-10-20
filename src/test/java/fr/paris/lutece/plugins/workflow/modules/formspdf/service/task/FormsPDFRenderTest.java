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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.forms.business.FormQuestionResponse;
import fr.paris.lutece.plugins.forms.business.Question;
import fr.paris.lutece.plugins.genericattributes.business.Entry;
import fr.paris.lutece.plugins.genericattributes.business.EntryType;
import fr.paris.lutece.plugins.genericattributes.business.Field;
import fr.paris.lutece.plugins.genericattributes.business.Response;
import fr.paris.lutece.test.LuteceTestCase;

/**
 * Renders PDF models as {@link FormsPDFTask} does, through the string templates of AppTemplateService (HTML output format,
 * auto-escaping; LUT-33153): the HTML written in the model by the administrator is kept, the values of the answers are
 * escaped, the label of an empty answer keeps its markup, the rich texts are sanitized and the file name is text.
 */
public class FormsPDFRenderTest extends LuteceTestCase
{
    private static final Locale LOCALE = Locale.FRENCH;
    private static final String HOSTILE = "l'école <b>a</b> & \"c\" <script>alert(1)</script>";
    private static final String HOSTILE_ESCAPED = "l&#39;école &lt;b&gt;a&lt;/b&gt; &amp; &quot;c&quot; &lt;script&gt;alert(1)&lt;/script&gt;";

    /**
     * Builds the answer to a question, as GenericFormsProvider puts it in the model.
     *
     * @param strBeanName
     *            the bean name of the entry type
     * @param listFields
     *            the fields of the entry
     * @param strComment
     *            the comment of the entry
     * @param values
     *            the displayed values of the responses
     * @return the answer
     */
    private static FormQuestionResponse answer( String strBeanName, List<Field> listFields, String strComment, String... values )
    {
        EntryType entryType = new EntryType( );
        entryType.setBeanName( strBeanName );
        Entry entry = new Entry( );
        entry.setIdEntry( 7 );
        entry.setTitle( "Question" );
        entry.setEntryType( entryType );
        entry.setFields( listFields );
        entry.setComment( strComment );
        Question question = new Question( );
        question.setEntry( entry );
        List<Response> listResponses = new ArrayList<>( );
        for ( String strValue : values )
        {
            Response response = new Response( );
            response.setEntry( entry );
            response.setResponseValue( strValue );
            response.setToStringValueResponse( strValue );
            listResponses.add( response );
        }
        FormQuestionResponse formQuestionResponse = new FormQuestionResponse( );
        formQuestionResponse.setQuestion( question );
        formQuestionResponse.setEntryResponse( listResponses );

        return formQuestionResponse;
    }

    private static Map<String, Object> model( FormQuestionResponse formQuestionResponse )
    {
        Map<String, Object> model = new HashMap<>( );
        model.put( "form_title", HOSTILE );
        model.put( "position_1", formQuestionResponse );

        return model;
    }

    /**
     * The HTML of the model is kept and every answer is escaped once, in a marker as in a display macro.
     */
    @Test
    public void testModelHtmlIsKeptAndAnswersAreEscaped( )
    {
        String strHtml = FormsPDFTask.renderContent( "<h1 style=\"text-align: center;\">${form_title}</h1><div><@displayEntry q=position_1/></div>", LOCALE,
                model( answer( "forms.entryTypeText", new ArrayList<>( ), "", HOSTILE ) ) );

        assertTrue( strHtml.startsWith( "<h1 style=\"text-align: center;\">" + HOSTILE_ESCAPED + "</h1><div>" ), strHtml );
        assertTrue( strHtml.contains( HOSTILE_ESCAPED + "</span>" ), strHtml );
        assertFalse( strHtml.contains( "<b>a</b>" ), strHtml );
        assertFalse( strHtml.contains( "<script>" ), strHtml );
    }

    /**
     * A model written with the rich text editor (square bracket syntax, see FormsPDFTask.addSquareBracketTag) renders the same way.
     */
    @Test
    public void testSquareBracketSyntax( )
    {
        String strHtml = FormsPDFTask.renderContent( "[#ftl]<p>${form_title}</p>[@displayEntry q=position_1/]", LOCALE,
                model( answer( "forms.entryTypeText", new ArrayList<>( ), "", HOSTILE ) ) );

        assertTrue( strHtml.startsWith( "<p>" + HOSTILE_ESCAPED + "</p>" ), strHtml );
        assertFalse( strHtml.contains( "<b>a</b>" ), strHtml );
    }

    /**
     * The label of an empty answer keeps its markup (its CSS class is styled by HtmlToPDFGenerator), the answers around it are escaped.
     */
    @Test
    public void testEmptyAnswerLabelKeepsItsMarkup( )
    {
        String strHtml = FormsPDFTask.renderContent( "<@displayEntry q=position_1/>", LOCALE, model( answer( "forms.entryTypeText", new ArrayList<>( ), "",
                FormsPDFTask.EMPTY_RESPONSE_START + "N/A" + FormsPDFTask.EMPTY_RESPONSE_END, "<span class=\"formspdf-empty\">x</span>" ) ) );

        assertTrue( strHtml.contains( "<span class=\"formspdf-empty\">N/A</span>" ), strHtml );
        assertTrue( strHtml.contains( "&lt;span class=&quot;formspdf-empty&quot;&gt;x&lt;/span&gt;" ), strHtml );
        assertFalse( strHtml.contains( FormsPDFTask.EMPTY_RESPONSE_START ), strHtml );
        assertFalse( strHtml.contains( FormsPDFTask.EMPTY_RESPONSE_END ), strHtml );
    }

    /**
     * A text area answer keeps its line breaks as br elements, its text escaped.
     */
    @Test
    public void testTextAreaLineBreaks( )
    {
        String strHtml = FormsPDFTask.renderContent( "<@displayEntry q=position_1/>", LOCALE,
                model( answer( "forms.entryTypeTextArea", new ArrayList<>( ), "", "a<b\nc & d" ) ) );

        assertTrue( strHtml.contains( "a&lt;b<br>c &amp; d" ), strHtml );
    }

    /**
     * A rich text answer and the comment of an entry are HTML entered in the forms: rendered by richText( ), their tags kept and sanitized.
     */
    @Test
    public void testRichTextsAreSanitized( )
    {
        Field richText = new Field( );
        richText.setCode( "richtext" );
        richText.setValue( "true" );
        List<Field> listFields = new ArrayList<>( );
        listFields.add( richText );
        String strTextArea = FormsPDFTask.renderContent( "<@displayEntry q=position_1/>", LOCALE,
                model( answer( "forms.entryTypeTextArea", listFields, "", "<p>rich <strong>text</strong></p><script>alert(1)</script>" ) ) );
        String strComment = FormsPDFTask.renderContent( "<@displayEntry q=position_1/>", LOCALE,
                model( answer( "forms.entryTypeComment", new ArrayList<>( ), "<p>Read <em>this</em></p><script>alert(2)</script>" ) ) );

        assertTrue( strTextArea.contains( "<p>rich <strong>text</strong></p>" ), strTextArea );
        assertFalse( strTextArea.contains( "<script>" ), strTextArea );
        assertTrue( strComment.contains( "<p>Read <em>this</em></p>" ), strComment );
        assertFalse( strComment.contains( "<script>" ), strComment );
    }

    /**
     * The file name is the text of the rendering: the answers as entered, without entities, tags nor marks of the empty answers.
     */
    @Test
    public void testFileNameIsText( )
    {
        String strFileName = FormsPDFTask.renderFileName( "${form_title}_${position_1.entryResponse[0].toStringValueResponse}", LOCALE,
                model( answer( "forms.entryTypeText", new ArrayList<>( ), "", FormsPDFTask.EMPTY_RESPONSE_START + "N/A" + FormsPDFTask.EMPTY_RESPONSE_END ) ) );

        assertEquals( "l'école <b>a</b> & \"c\" <script>alert(1)</script>_N/A", strFileName );
    }
}
