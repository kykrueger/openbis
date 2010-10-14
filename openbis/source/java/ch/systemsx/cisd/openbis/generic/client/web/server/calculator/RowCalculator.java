/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.calculator;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.evaluator.Evaluator;
import ch.systemsx.cisd.common.evaluator.EvaluatorException;
import ch.systemsx.cisd.common.utilities.Template;
import ch.systemsx.cisd.openbis.generic.shared.basic.PrimitiveValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ParameterWithValue;

/**
 * @author Franz-Josef Elmer
 */
class RowCalculator
{
    private static final String INITIAL_SCRIPT = "from "
            + StandardFunctions.class.getCanonicalName() + " import *\n"
            + "def int(x):return toInt(x)\n" + "def float(x):return toFloat(x)\n";

    private final Evaluator evaluator;

    private final Row row;

    public RowCalculator(ITableDataProvider provider, String expression)
    {
        this(provider, expression, Collections.<ParameterWithValue> emptySet());
    }

    public RowCalculator(ITableDataProvider provider, String expression,
            Set<ParameterWithValue> parameters)
    {
        evaluator =
                new Evaluator(substitudeParameters(expression, parameters), Math.class,
                        INITIAL_SCRIPT);
        row = new Row(provider);
        evaluator.set("row", row);
    }

    public void setRowData(List<? extends Comparable<?>> rowValues)
    {
        row.setRowData(rowValues);
    }

    public PrimitiveValue getTypedResult()
    {
        Object value = evaluator.eval();
        if (value == null)
        {
            return PrimitiveValue.NULL;
        }
        if (value instanceof Long)
        {
            return new PrimitiveValue((Long) value);
        } else if (value instanceof Double)
        {
            return new PrimitiveValue((Double) value);
        } else
        {
            return new PrimitiveValue(value.toString());
        }
    }

    public boolean evalToBoolean() throws EvaluatorException
    {
        return evaluator.evalToBoolean();
    }

    public int evalToInt() throws EvaluatorException
    {
        return evaluator.evalToInt();
    }

    public BigInteger evalToBigInt() throws EvaluatorException
    {
        return evaluator.evalToBigInt();
    }

    public double evalToDouble() throws EvaluatorException
    {
        return evaluator.evalToDouble();
    }

    public String evalAsString() throws EvaluatorException
    {
        return evaluator.evalAsString();
    }

    private String substitudeParameters(String originalExpression,
            Set<ParameterWithValue> parameters)
    {
        Template template = new Template(originalExpression);
        for (ParameterWithValue pw : parameters)
        {
            template.bind(pw.getParameter(), pw.getValue());
        }
        return template.createText();
    }

}
