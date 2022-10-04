/*
 * Copyright (c) 2002-2018, Mairie de Paris
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
package fr.paris.lutece.plugins.workflow.modules.formspdf.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import fr.paris.lutece.plugins.forms.business.CompositeDisplayType;
import fr.paris.lutece.plugins.forms.business.FormDisplay;
import fr.paris.lutece.plugins.forms.business.FormDisplayHome;
import fr.paris.lutece.plugins.forms.business.FormQuestionResponse;
import fr.paris.lutece.plugins.forms.business.FormQuestionResponseHome;
import fr.paris.lutece.plugins.forms.business.FormResponse;
import fr.paris.lutece.plugins.forms.business.FormResponseHome;
import fr.paris.lutece.plugins.forms.business.Question;
import fr.paris.lutece.plugins.forms.business.QuestionHome;
import fr.paris.lutece.plugins.forms.business.Step;
import fr.paris.lutece.plugins.forms.business.StepHome;
import fr.paris.lutece.plugins.genericattributes.business.Response;

/**
 * @author norbert.le.garrec
 *
 */
public final class FormsPDFUtil
{

    /**
     * Private constructor for utility function
     */
    private FormsPDFUtil( )
    {
        // DO NOTHING
    }

    /**
     * @param nFormResponseId
     *            the Id of the FormResponse to Export
     * @return The JSON String representing the FormResponse
     */
    public static String getJsonFromFormResponse( int nFormResponseId )
    {

        FormResponse formResponse = FormResponseHome.findByPrimaryKey( nFormResponseId );

        JSONObject jsonForm = new JSONObject( );

        List<Step> stepsListByForm = StepHome.getStepsListByForm( formResponse.getFormId( ) );
        for ( Step step : stepsListByForm )
        {
            jsonForm.accumulate( "step_" + step.getId( ), getJsonStepFormDisplayAndFormResponse( step.getId( ), nFormResponseId ) );
        }

        return jsonForm.toString( );
    }

    /**
     * @param nStepId
     *            the step id
     * @param nFormResponseId
     *            the FormResponse id
     * @return the JSON representation of the formResponse for the step
     */
    private static JSONObject getJsonStepFormDisplayAndFormResponse( int nStepId, int nFormResponseId )
    {
        JSONObject jsonCompositeStep = new JSONObject( );

        List<FormDisplay> formDisplayListByParent = FormDisplayHome.getFormDisplayListByParent( nStepId, 0 );
        for ( FormDisplay formDisplayChildren : formDisplayListByParent )
        {
            String strCompositeType = formDisplayChildren.getCompositeType( );

            if ( CompositeDisplayType.QUESTION.getLabel( ).equalsIgnoreCase( strCompositeType ) )
            {
                int compositeId = formDisplayChildren.getCompositeId( );
                Question question = QuestionHome.findByPrimaryKey( compositeId );
                String valueLabel = getJsonValueLabelQRFormDisplayAndFormResponse( formDisplayChildren, nFormResponseId, 0, null );
                Object value = getJsonValueQRFormDisplayAndFormResponse( formDisplayChildren, nFormResponseId, 0, null );
                if ( question != null && question.getCode( ) != null )
                {
                    jsonCompositeStep.accumulate( question.getCode( ), value );
                    if ( StringUtils.isNoneBlank( valueLabel ) )
                    {
                        jsonCompositeStep.accumulate( question.getCode( ) + "_label", valueLabel );
                    }
                } else
                {
                    jsonCompositeStep.accumulate( "question_" + formDisplayChildren.getCompositeId( ), value );
                    if ( StringUtils.isNoneBlank( valueLabel ) )
                    {
                        jsonCompositeStep.accumulate( "question_" + formDisplayChildren.getCompositeId( ) + "_label", valueLabel );
                    }
                }
            }

            else if ( CompositeDisplayType.GROUP.getLabel( ).equalsIgnoreCase( strCompositeType ) )
            {
                jsonCompositeStep.put( "group_" + formDisplayChildren.getCompositeId( ), getJsonGroupValueFormDisplayAndFormResponse( formDisplayChildren, nFormResponseId ) );
            }
        }

        return jsonCompositeStep;

    }

    /**
     * @param formDisplay
     *            the FormDisplay representing the group
     * @param nFormResponseId
     *            the FormResponse id
     * @return the JSON representation of the formResponse for the group
     */
    private static JSONArray getJsonGroupValueFormDisplayAndFormResponse( FormDisplay formDisplay, int nFormResponseId )
    {
        List<FormDisplay> listChildrenDisplay = FormDisplayHome.getFormDisplayListByParent( formDisplay.getStepId( ), formDisplay.getId( ) );
        JSONArray jsonGroup = new JSONArray( );

        List<Integer> positionMax = new ArrayList<>( );
        positionMax.add( 0 );
        int nIndex = 0;

        while ( nIndex <= positionMax.get( 0 ) )
        {
            JSONObject jsonGroupItem = new JSONObject( );
            for ( FormDisplay formDisplayChild : listChildrenDisplay )
            {
                jsonGroupItem.accumulate( "question_" + formDisplayChild.getCompositeId( ), getJsonValueQRFormDisplayAndFormResponse( formDisplayChild, nFormResponseId, nIndex, positionMax ) );
                String valueLabel = getJsonValueLabelQRFormDisplayAndFormResponse( formDisplayChild, nFormResponseId, nIndex, positionMax );
                if ( StringUtils.isNoneBlank( valueLabel ) )
                {
                    jsonGroupItem.accumulate( "question_" + formDisplayChild.getCompositeId( ) + "_label", valueLabel );
                }

            }

            jsonGroup.put( jsonGroupItem );
            nIndex = nIndex + 1;
        }
        return jsonGroup;

    }

    /**
     * @param formDisplay
     *            the FormDisplay representing the QuestionResponse
     * @param nFormResponseId
     *            the FormResponse id
     * @param nIndex
     *            the Iteration Number of the response to get
     * @param positionMax
     *            the max Iteration Number from the responses
     * @return the JSON representation of the formResponse for the QuestionResponse
     */
    private static Object getJsonValueQRFormDisplayAndFormResponse( FormDisplay formDisplay, int nFormResponseId, int nIndex, List<Integer> positionMax )
    {

        List<FormQuestionResponse> formQuestionResponseList = FormQuestionResponseHome.findFormQuestionResponseByResponseQuestion( nFormResponseId, formDisplay.getCompositeId( ) );

        // Récupération de la position max possible pour récupérer l'ensemble des groupes de réponses (O2T 82280)
        updatePositionMax( positionMax, formQuestionResponseList );

        for ( FormQuestionResponse formQuestionResponse : formQuestionResponseList )
        {
            JSONObject jsonField = new JSONObject( );

            for ( Response responseForms : formQuestionResponse.getEntryResponse( ) )
            {
                if ( responseForms.getIterationNumber( ) == nIndex || responseForms.getIterationNumber( ) < 0 )
                {
                    if ( formQuestionResponse.getEntryResponse( ).size( ) > 1 )
                    {
                        if ( responseForms.getField( ) != null )
                        {
                            jsonField.accumulate( responseForms.getField( ).getValue( ), responseForms.getToStringValueResponse( ) );
                        } else
                        {
                            jsonField.accumulate( "", responseForms.getToStringValueResponse( ) );
                        }
                    } else
                    {
                        return responseForms.getToStringValueResponse( );
                    }
                }
            }

            if ( formQuestionResponse.getEntryResponse( ) != null && formQuestionResponse.getEntryResponse( ).size( ) > 1 )
            {
                return jsonField;
            }
        }

        return "";

    }

    /**
     * Gets the json value label QR form display and form response.
     *
     * @param formDisplay
     *            the form display
     * @param nFormResponseId
     *            the n form response id
     * @param nIndex
     *            the n index
     * @param positionMax
     *            the position max
     * @return the json value label QR form display and form response
     */
    private static String getJsonValueLabelQRFormDisplayAndFormResponse( FormDisplay formDisplay, int nFormResponseId, int nIndex, List<Integer> positionMax )
    {

        List<FormQuestionResponse> formQuestionResponseList = FormQuestionResponseHome.findFormQuestionResponseByResponseQuestion( nFormResponseId, formDisplay.getCompositeId( ) );

        // Récupération de la position max possible pour récupérer l'ensemble des groupes de réponses (O2T 82280)
        updatePositionMax( positionMax, formQuestionResponseList );

        for ( FormQuestionResponse formQuestionResponse : formQuestionResponseList )
        {
            for ( Response responseForms : formQuestionResponse.getEntryResponse( ) )
            {
                if ( responseForms.getIterationNumber( ) == nIndex || responseForms.getIterationNumber( ) < 0 )
                {
                    if ( formQuestionResponse.getEntryResponse( ).size( ) == 1 && responseForms.getField( ) != null )
                    {
                        return responseForms.getField( ).getTitle( );
                    }
                }
            }
        }

        return "";

    }

    /**
     *
     * @param positionMax
     *            position actuelle
     * @param formQuestionResponseList
     *            liste des questions/réponses
     */
    private static void updatePositionMax( List<Integer> positionMax, List<FormQuestionResponse> formQuestionResponseList )
    {
        // Récupération de la position max
        for ( FormQuestionResponse formQuestionResponse : formQuestionResponseList )
        {
            for ( Response responseForms : formQuestionResponse.getEntryResponse( ) )
            {
                if ( positionMax != null && !positionMax.isEmpty( ) && positionMax.get( 0 ) < responseForms.getIterationNumber( ) )
                {
                    positionMax.set( 0, responseForms.getIterationNumber( ) );
                }
            }
        }
    }

}
