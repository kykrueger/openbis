/*
 * Copyright 2011 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.server.business;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.evaluator.EvaluatorException;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * @author Kaloyan Enimanev
 */
class PropertiesBatchEvaluationErrors
{
    /**
     * limitation on the number of ErrorDetails objects kept in memory. This guards us from
     * scripts generating unique error messages for excessively large batches.
     */
    private static final int MAX_ERROR_DETAILS_KEPT = 10;

    /** the maximum number of errors displayed to the user. */
    private static final int MAX_ERROR_IN_USER_MESSAGE = 2;

    private class ErrorDetail
    {
        ScriptPE scriptPE;

        EvaluatorException evaluationError;

        String propertyCode;

        List<Integer> rows = new ArrayList<Integer>();
    }

    private PersonPE registrator;

    private Map<String, ErrorDetail> errorDetails = new HashMap<String, ErrorDetail>();

    private int totalRowsNumber;

    private int totalFailedRowsNumber;

    PropertiesBatchEvaluationErrors(PersonPE registrator, int totalRowsNumber)
    {
        this.registrator = registrator;
        this.totalRowsNumber = totalRowsNumber;
    }

    void accumulateError(int row, EvaluatorException evaluationError, String propertyCode,
            ScriptPE script)
    {
        totalFailedRowsNumber++;
        String errorMessage = evaluationError.getMessage();
        if (shouldSkipDetailsAccumulation(errorMessage))
        {
            return;
        }

        ErrorDetail details = errorDetails.get(errorMessage);
        if (details == null)
        {
            details = new ErrorDetail();
            details.evaluationError = evaluationError;
            details.scriptPE = script;
            details.propertyCode = propertyCode;
        }
        details.rows.add(row);
        errorDetails.put(errorMessage, details);
    }

    /**
     * @return <code>true</code> when the memory is full and no more errors should be
     *         accumulated.
     */
    boolean shouldSkipDetailsAccumulation(String errorMessage)
    {
        return errorDetails.containsKey(errorMessage) == false
                && errorDetails.size() >= MAX_ERROR_DETAILS_KEPT;
    }

    boolean hasErrors() {
        return totalFailedRowsNumber > 0;
    }


    /**
     * creates a messages to be displayed to the user.
     */
    String constructUserFailureMessage()
    {
        assert hasErrors() : "Cannot construct message when no errors were accumulated.";
        StringBuilder message = new StringBuilder();
        message.append("Script malfunction in ");
        message.append(totalFailedRowsNumber);
        message.append(" out of ");
        message.append(totalRowsNumber);
        message.append(" rows.");
        
        int numDisplayErrors = Math.min(errorDetails.size(), MAX_ERROR_IN_USER_MESSAGE);
        List<ErrorDetail> formatDetails = sortErrorDetailsByRow().subList(0, numDisplayErrors);
        for (ErrorDetail errDetail : formatDetails)
        {
            message.append("\n");
            appendErrorDetails(message, errDetail, false);
            message.append(": ");
            message.append(errDetail.evaluationError.getMessage());
        }

        message.append("\n");
        message.append("A detailed error report has been sent to your system administrator.");
        return message.toString();

    }

    /**
     * creates an e-mail, containg the error report
     */
    String constructErrorReportEmail()
    {
        assert hasErrors() : "Cannot construct message when no errors were accumulated.";
        StringBuilder message = new StringBuilder();
        message.append("A batch operation initiated from user ");
        message.append(registrator);
        message.append("has failed due to a script malfunction in ");
        message.append(totalFailedRowsNumber);
        message.append(" out of ");
        message.append(totalRowsNumber);
        message.append(" rows.");
        message.append("\n");

        for (ErrorDetail errDetail : sortErrorDetailsByRow())
        {
            message.append("\n\n");
            appendErrorDetails(message, errDetail, true);
            message.append(": ");
            StringWriter sw = new StringWriter();
            errDetail.evaluationError.printStackTrace(new PrintWriter(sw));
            message.append(sw.toString());
        }

        return message.toString();

    }
    
    private List<ErrorDetail> sortErrorDetailsByRow() {
        List<ErrorDetail> result = new ArrayList<ErrorDetail>(errorDetails.values());
        Collections.sort(result, new Comparator<ErrorDetail>()
            {
                public int compare(ErrorDetail o1, ErrorDetail o2)
                {
                    return o1.rows.get(0) - o2.rows.get(0);
                }
            });
        return result;
    }

    private void appendErrorDetails(StringBuilder message, ErrorDetail errDetail,
            boolean includeFullRegistratorDetails)
    {
        message.append(formatRows(errDetail.rows));
        message.append(" failed due to the property '");
        message.append(errDetail.propertyCode);
        message.append("' causing a malfuction in the script (name = '");
        message.append(errDetail.scriptPE.getName());
        message.append("', registrator = '");
        if (errDetail.scriptPE.getRegistrator() != null)
        {
            if (includeFullRegistratorDetails)
            {
                message.append(errDetail.scriptPE.getRegistrator());
            } else
            {
                message.append(errDetail.scriptPE.getRegistrator().getEmail());
            }
        }
        message.append("')");
    }

    private String formatRows(List<Integer> rows)
    {
        if (rows.size() == 1)
        {
            return "Row " + rows.get(0) + " has";
        }
        int displayRows = Math.min(rows.size(), 3);
        List<Integer> displayRowsList = rows.subList(0, displayRows);
        if (displayRows == rows.size())
        {
            return "Rows " + displayRowsList + " have";
        }
        return rows.size() + " rows including " + displayRowsList + " have";
    }
}