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
package fr.paris.lutece.plugins.workflow.modules.formspdf.business;

import fr.paris.lutece.plugins.workflowcore.business.config.TaskConfig;

/**
 * Configuration for the FormsJasper workflow module
 *
 */
public class FormsPDFTaskConfig extends TaskConfig
{

    /**
     * Id of the forms to be considered as the report datasource
     */
    private int _nIdForms;

    /**
     * The format of the report to export (csv,pdf,rtf ...)
     */
    private String _strFormat;

    private int _nIdTemplate;

    /**
     * @return _nIdForms : The id of the forms to be considered as the report datasource
     */
    public int getIdForms( )
    {
        return _nIdForms;
    }

    /**
     * @param nIdForms
     *            set the id of the forms to be considered as the report datasource
     */
    public void setIdForms( int nIdForms )
    {
        this._nIdForms = nIdForms;
    }

    /**
     * @return _strFormat : The format of the report to export (csv,pdf,rtf ...)
     */
    public String getFormat( )
    {
        return _strFormat;
    }

    /**
     * @param strFormat
     *            set The format of the report to export (csv,pdf,rtf ...)
     */
    public void setFormat( String strFormat )
    {
        this._strFormat = strFormat;
    }

    /**
     * Gets the template.
     *
     * @return the template
     */
    public int getIdTemplate( )
    {
        return _nIdTemplate;
    }

    /**
     * Sets the template.
     *
     * @param _nIdTemplate
     *            the new template
     */
    public void setIdTemplate( int nIdTemplate )
    {
        this._nIdTemplate = nIdTemplate;
    }
}
