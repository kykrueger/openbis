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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator;

import java.util.Arrays;
import java.util.Collection;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.jython.evaluator.EvaluatorException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IEntityPropertyAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.XmlUtilsTest;

/**
 * @author Piotr Buczek
 */
public class DynamicPropertyCalculatorTest extends AssertJUnit
{

    @Test
    public void testGetEntityCode()
    {
        final String entityCode1 = "ecode1";
        final String entityCode2 = "ecode2";
        final JythonDynamicPropertyCalculator calculator =
                JythonDynamicPropertyCalculator.create("entity.code()");

        calculator.setEntity(createEntity(entityCode1, null));
        assertEquals(entityCode1, calculator.evalAsString());

        calculator.setEntity(createEntity(entityCode2, null));
        assertEquals(entityCode2, calculator.evalAsString());
    }

    @Test
    public void testGetEntityPropertyValue()
    {
        final JythonDynamicPropertyCalculator calculator =
                JythonDynamicPropertyCalculator.create("entity.propertyValue('p2')");

        final String entityCode = "ecode";

        IEntityPropertyAdaptor p1 = createProperty("P1", "v1");
        IEntityPropertyAdaptor p21 = createProperty("P2", "v21");
        IEntityPropertyAdaptor p22 = createProperty("P2", "v22");

        calculator.setEntity(createEntity(entityCode, null));
        assertEquals("", calculator.evalAsString());

        calculator.setEntity(createEntity(entityCode, Arrays.asList(new IEntityPropertyAdaptor[]
            { p1 })));
        assertEquals("", calculator.evalAsString()); // non-existent property

        calculator.setEntity(createEntity(entityCode, Arrays.asList(new IEntityPropertyAdaptor[]
            { p1, p21 })));
        assertEquals(p21.valueAsString(), calculator.evalAsString());

        calculator.setEntity(createEntity(entityCode, Arrays.asList(new IEntityPropertyAdaptor[]
            { p1, p22 })));
        assertEquals(p22.valueAsString(), calculator.evalAsString());
    }

    @Test
    public void testGetEntityPropertyRenderedValue()
    {
        final JythonDynamicPropertyCalculator normalPropertyCalculator =
                JythonDynamicPropertyCalculator.create("entity.propertyRendered('normalProperty')");
        final JythonDynamicPropertyCalculator xmlPropertyCalculator =
                JythonDynamicPropertyCalculator.create("entity.propertyRendered('xmlProperty')");

        final String entityCode = "ecode";

        IEntityPropertyAdaptor normalProperty = createProperty("normalProperty", "normalValue");
        IEntityPropertyAdaptor xmlProperty =
                createXmlProperty("xmlProperty", XmlUtilsTest.SIMPLE_XML, XmlUtilsTest.SIMPLE_XSLT);

        normalPropertyCalculator.setEntity(createEntity(entityCode,
                Arrays.asList(new IEntityPropertyAdaptor[]
                    { normalProperty, xmlProperty })));
        assertEquals("normalValue", normalPropertyCalculator.evalAsString());

        xmlPropertyCalculator.setEntity(createEntity(entityCode,
                Arrays.asList(new IEntityPropertyAdaptor[]
                    { normalProperty, xmlProperty })));
        assertEquals(XmlUtilsTest.SIMPLE_XML_TRANSFORMED, xmlPropertyCalculator.evalAsString());
    }

    @Test
    public void testEvaluateMultilineExpression()
    {
        final String expression = "def calculate():\n" + "\treturn entity.code()";
        final String entityCode = "ecode";
        final JythonDynamicPropertyCalculator calculator = JythonDynamicPropertyCalculator.create(expression);

        calculator.setEntity(createEntity(entityCode, null));
        assertEquals(entityCode, calculator.evalAsString());
    }

    @Test
    public void testEvaluateMultilineExpressionFailsWithNoCalculateFunction()
    {
        final String expression = "def calc():\n" + "\treturn entity.code()";
        final String entityCode = "ecode";
        final JythonDynamicPropertyCalculator calculator = JythonDynamicPropertyCalculator.create(expression);

        calculator.setEntity(createEntity(entityCode, null));
        try
        {
            calculator.evalAsString();
            fail("expected EvaluatorException");
        } catch (EvaluatorException e)
        {
            final String expectedMsg =
                    "Error occurred in line 1 of the script when evaluating 'calculate()': "
                            + "NameError: name 'calculate' is not defined";
            assertEquals("expected exception message: " + expectedMsg, expectedMsg, e.getMessage());
        }
    }

    @Test
    public void testMaterialFunction()
    {
        final String code = "CODE";
        final String typeCode = "TYPE";

        final JythonDynamicPropertyCalculator calculator =
                JythonDynamicPropertyCalculator.create("material('" + code + "', '" + typeCode + "')");
        assertEquals(MaterialIdentifier.print(code, typeCode), calculator.evalAsString());
    }

    private static IEntityAdaptor createEntity(final String code,
            final Collection<IEntityPropertyAdaptor> properties)
    {
        return new AbstractEntityAdaptor(code, properties);
    }

    private static IEntityPropertyAdaptor createProperty(final String propertyTypeCode,
            final String value)
    {
        return new BasicPropertyAdaptor(propertyTypeCode, value);
    }

    private static IEntityPropertyAdaptor createXmlProperty(final String propertyTypeCode,
            final String value, final String xmlTransformation)
    {
        return new XmlPropertyAdaptor(propertyTypeCode, value, xmlTransformation);
    }

}
