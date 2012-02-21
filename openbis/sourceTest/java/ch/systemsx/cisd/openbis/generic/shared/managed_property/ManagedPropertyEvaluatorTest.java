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

package ch.systemsx.cisd.openbis.generic.shared.managed_property;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.evaluator.EvaluatorException;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedComboBoxInputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedHtmlWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedTableWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescriptionFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedOutputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.ManagedInputFieldType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.ManagedOutputWidgetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;

/**
 * @author Franz-Josef Elmer
 * @author Piotr Buczek
 */
public class ManagedPropertyEvaluatorTest extends AssertJUnit
{
    private static final String UPDATE_FROM_UI_TEST_PY = "updateFromUI-test.py";

    private static final String CONFIGURE_UI_OUTPUT_TEST_PY = "configureUIOutput-test.py";

    private static final String CONFIGURE_UI_OUTPUT_HTML_TEST_PY = "configureUIOutputHtml-test.py";

    private static final String CONFIGURE_UI_INPUT_TEST_PY = "configureUIInput-test.py";

    private static final String SCRIPT_FOLDER =
            "sourceTest/java/ch/systemsx/cisd/openbis/generic/shared/managed_property/";

    @Test
    public void testEmptyScript()
    {
        assertEquals(0, new ManagedPropertyEvaluator("").getBatchColumnNames().size());
    }

    @Test
    public void testScriptWithSyntaxError()
    {
        try
        {
            new ManagedPropertyEvaluator("a =");
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals("SyntaxError: (\"no viable alternative at input '='\", "
                    + "('<string>', 1, 2, 'a =\\n'))", ex.getMessage());
        }
    }

    @Test
    public void testConfigureUIOutput()
    {
        IManagedProperty managedProperty = new ManagedProperty();
        managedProperty.setOwnTab(false);

        String script =
                CommonTestUtils.getResourceAsString(SCRIPT_FOLDER, CONFIGURE_UI_OUTPUT_TEST_PY);
        ManagedPropertyEvaluator evaluator = new ManagedPropertyEvaluator(script);

        evaluator.configureUI(managedProperty, new SamplePropertyPE());
        assertEquals(true, managedProperty.isOwnTab());
        IManagedOutputWidgetDescription outputWidgetDescripion =
                managedProperty.getUiDescription().getOutputWidgetDescription();
        assertNotNull(outputWidgetDescripion);
        assertEquals(ManagedOutputWidgetType.TABLE,
                outputWidgetDescripion.getManagedOutputWidgetType());
        if (outputWidgetDescripion instanceof ManagedTableWidgetDescription)
        {
            ManagedTableWidgetDescription tableDescription =
                    (ManagedTableWidgetDescription) outputWidgetDescripion;
            TableModel tableModel = tableDescription.getTableModel();
            assertNotNull(tableModel);

            assertEquals(3, tableModel.getHeader().size());
            assertEquals("column1", tableModel.getHeader().get(0).getId());
            assertEquals("column2", tableModel.getHeader().get(1).getId());
            assertEquals("column3", tableModel.getHeader().get(2).getId());

            assertEquals(3, tableModel.getRows().size());
            assertEquals(SimpleTableModelBuilder.asText("v1"), tableModel.getRows().get(0)
                    .getValues().get(0));
            assertEquals(SimpleTableModelBuilder.asInteger(1), tableModel.getRows().get(0)
                    .getValues().get(1));
            assertEquals(SimpleTableModelBuilder.asDouble(1.5), tableModel.getRows().get(0)
                    .getValues().get(2));

            assertEquals(SimpleTableModelBuilder.asText("v2"), tableModel.getRows().get(1)
                    .getValues().get(0));
            assertEquals(SimpleTableModelBuilder.asInteger(2), tableModel.getRows().get(1)
                    .getValues().get(1));
            assertEquals(SimpleTableModelBuilder.asDouble(2.5), tableModel.getRows().get(1)
                    .getValues().get(2));

            assertEquals(SimpleTableModelBuilder.asText("v3"), tableModel.getRows().get(2)
                    .getValues().get(0));
            assertEquals(SimpleTableModelBuilder.asText(null), tableModel.getRows().get(2)
                    .getValues().get(1));
            assertEquals(SimpleTableModelBuilder.asText(null), tableModel.getRows().get(2)
                    .getValues().get(2));
        } else
        {
            fail("expected instance of " + ManagedTableWidgetDescription.class.getSimpleName()
                    + ", got " + outputWidgetDescripion.getClass().getSimpleName());
        }
    }

    @Test
    public void testConfigureUIOutputHtml()
    {
        IManagedProperty managedProperty = new ManagedProperty();
        managedProperty.setOwnTab(false);

        String script =
                CommonTestUtils
                        .getResourceAsString(SCRIPT_FOLDER, CONFIGURE_UI_OUTPUT_HTML_TEST_PY);
        ManagedPropertyEvaluator evaluator = new ManagedPropertyEvaluator(script);

        evaluator.configureUI(managedProperty, new SamplePropertyPE());
        assertEquals(true, managedProperty.isOwnTab());
        IManagedOutputWidgetDescription outputWidgetDescripion =
                managedProperty.getUiDescription().getOutputWidgetDescription();
        assertNotNull(outputWidgetDescripion);
        assertEquals(ManagedOutputWidgetType.HTML,
                outputWidgetDescripion.getManagedOutputWidgetType());
        if (false == outputWidgetDescripion instanceof ManagedHtmlWidgetDescription)
        {
            fail("expected instance of " + ManagedHtmlWidgetDescription.class.getSimpleName()
                    + ", got " + outputWidgetDescripion.getClass().getSimpleName());
        }
        ManagedHtmlWidgetDescription htmlDescription =
                (ManagedHtmlWidgetDescription) outputWidgetDescripion;
        String html = htmlDescription.getHtml();
        assertNotNull(html);

        assertEquals("<p>Hello</p>", html);
    }

    @Test
    public void testConfigureUIInput()
    {
        IManagedProperty managedProperty = new ManagedProperty();
        managedProperty.setOwnTab(false);

        String script =
                CommonTestUtils.getResourceAsString(SCRIPT_FOLDER, CONFIGURE_UI_INPUT_TEST_PY);
        ManagedPropertyEvaluator evaluator = new ManagedPropertyEvaluator(script);

        evaluator.configureUI(managedProperty, new SamplePropertyPE());
        assertEquals(false, managedProperty.isOwnTab());

        List<IManagedUiAction> actions = managedProperty.getUiDescription().getActions();
        assertEquals(1, actions.size());
        IManagedUiAction action = actions.get(0);
        assertEquals("Create", action.getName());

        List<IManagedInputWidgetDescription> inputWidgets = action.getInputWidgetDescriptions();
        assertEquals(5, inputWidgets.size());
        checkInputFieldWidget(inputWidgets.get(0), ManagedInputFieldType.TEXT, "t1", null, null,
                false);
        checkInputFieldWidget(inputWidgets.get(1), ManagedInputFieldType.TEXT, "t2", "default 2",
                null, false);
        checkInputFieldWidget(inputWidgets.get(2), ManagedInputFieldType.TEXT, "t3", null,
                "description 3", false);
        checkInputFieldWidget(inputWidgets.get(3), ManagedInputFieldType.MULTILINE_TEXT, "multi",
                "default m", "multiline", false);
        checkInputFieldWidget(inputWidgets.get(4), ManagedInputFieldType.COMBO_BOX, "combo", null,
                "select from list", true);
        if (inputWidgets.get(4) instanceof ManagedComboBoxInputWidgetDescription)
        {
            ManagedComboBoxInputWidgetDescription comboBox =
                    (ManagedComboBoxInputWidgetDescription) inputWidgets.get(4);
            assertEquals("[v1, v2, v3]", comboBox.getOptions().toString());
        } else
        {
            fail("expected instance of "
                    + ManagedComboBoxInputWidgetDescription.class.getSimpleName() + ", got "
                    + inputWidgets.get(4).getClass().getSimpleName());
        }
    }

    @Test
    public void testUpdateFromUI()
    {
        IManagedProperty managedProperty = new ManagedProperty();
        IManagedUiDescription uiDescription = managedProperty.getUiDescription();
        IManagedInputWidgetDescriptionFactory widgetFactory =
                ManagedPropertyFunctions.inputWidgetFactory();

        IManagedUiAction action1 = uiDescription.addAction("a1");
        IManagedInputWidgetDescription action1w1 = widgetFactory.createTextInputField("t1");
        IManagedInputWidgetDescription action1w2 =
                widgetFactory.createTextInputField("t2").setValue("v2");
        IManagedInputWidgetDescription action1w3 =
                widgetFactory.createMultilineTextInputField("multi").setValue("multi\nline\ninput");
        IManagedInputWidgetDescription action1w4 =
                widgetFactory.createComboBoxInputField("combo", new String[]
                    { "cv1", "cv2", "cv3" }).setValue("cv1");
        action1.addInputWidgets(action1w1, action1w2, action1w3, action1w4);
        assertEquals(null, action1.getInputValue("t1"));
        assertEquals("v2", action1.getInputValue("t2"));
        assertEquals(null, action1.getInputValue("t3"));

        IManagedUiAction action2 = uiDescription.addAction("a2");
        IManagedInputWidgetDescription action2w1 =
                widgetFactory.createTextInputField("t1").setValue("v11");
        IManagedInputWidgetDescription action2w2 =
                widgetFactory.createTextInputField("t2").setValue("v22");
        action2.addInputWidgets(action2w1, action2w2);
        assertEquals("v11", action2.getInputValue("t1"));
        assertEquals("v22", action2.getInputValue("t2"));
        assertEquals(null, action2.getInputValue("t3"));

        IManagedUiAction action3 = uiDescription.addAction("a3");

        String script = CommonTestUtils.getResourceAsString(SCRIPT_FOLDER, UPDATE_FROM_UI_TEST_PY);
        ManagedPropertyEvaluator evaluator = new ManagedPropertyEvaluator(script);

        evaluator.updateFromUI(managedProperty, action1);
        assertNotNull(managedProperty.getValue());
        String[] inputTokens1 = managedProperty.getValue().split("\\|");
        assertEquals("a1", inputTokens1[0]);
        assertEquals("t1=null", inputTokens1[1]);
        assertEquals("t2=v2", inputTokens1[2]);
        assertEquals("multi=multi\nline\ninput", inputTokens1[3]);
        assertEquals("combo=cv1", inputTokens1[4]);

        evaluator.updateFromUI(managedProperty, action2);
        assertNotNull(managedProperty.getValue());
        String[] inputTokens2 = managedProperty.getValue().split("\\!");
        assertEquals("a2", inputTokens2[0]);
        assertEquals("t1=v11", inputTokens2[1]);
        assertEquals("t2=v22", inputTokens2[2]);

        try
        {
            evaluator.updateFromUI(managedProperty, action3);
            fail("expected EvaluatorException");
        } catch (EvaluatorException e)
        {
            assertEquals("action a3 is not supported", e.getCause().getMessage());
        }
    }

    private void checkInputFieldWidget(IManagedInputWidgetDescription widget,
            ManagedInputFieldType expectedType, String expectedLabel, String expectedDefaultValue,
            String expectedDescription, boolean expectedMandatory)
    {
        assertEquals(expectedType, widget.getManagedInputFieldType());
        assertEquals(expectedLabel, widget.getLabel());
        assertEquals(expectedDefaultValue, widget.getValue());
        assertEquals(expectedDescription, widget.getDescription());
        assertEquals(expectedMandatory, widget.isMandatory());
    }

    @Test
    public void testScriptWithBatchColumnNamesFunctionButMissingUpdateFromBatchFunction()
    {
        try
        {
            new ManagedPropertyEvaluator("def batchColumnNames():\n return ['A']");
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals("Function 'batchColumnNames' defined but not 'updateFromBatchInput'.",
                    ex.getMessage());
        }
    }

    @Test
    public void testScriptWithBatchColumnNamesFunctionWhichDoesNotReturnAList()
    {
        try
        {
            new ManagedPropertyEvaluator("def batchColumnNames():\n return 42\n"
                    + "def updateFromBatchInput():\n  None");
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals("Function 'batchColumnNames' doesn't return a List "
                    + "but an object of type 'java.lang.Integer': 42", ex.getMessage());
        }
    }

    @Test
    public void testScriptWithBatchColumnNamesFunctionWhichReturnLowerCaseNames()
    {
        try
        {
            new ManagedPropertyEvaluator(
                    "def batchColumnNames():\n return ['abc', 'A', 'e42', 42]\n"
                            + "def updateFromBatchInput():\n  None");
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals("The following batch column names "
                    + "as returned by function 'batchColumnNames' "
                    + "are not in upper case: [abc, e42]", ex.getMessage());
        }
    }

    @Test
    public void testGetBatchColumnNamesScript()
    {
        ManagedPropertyEvaluator evaluator =
                new ManagedPropertyEvaluator("def batchColumnNames():\n return ['A', 42]\n"
                        + "def updateFromBatchInput():\n  None");
        assertEquals("[A, 42]", evaluator.getBatchColumnNames().toString());
    }

    @Test
    public void testUpdateFromBatchInputWithNoScript()
    {
        ManagedPropertyEvaluator evaluator = new ManagedPropertyEvaluator("");
        ManagedProperty property = new ManagedProperty();
        property.setPropertyTypeCode("p");
        Map<String, String> bindings = new HashMap<String, String>();
        bindings.put("", "42");

        evaluator.updateFromBatchInput(property, bindings);

        assertEquals("42", property.getValue());
    }

    @Test
    public void testUpdateFromBatchInputWithNoColumns()
    {
        ManagedPropertyEvaluator evaluator =
                new ManagedPropertyEvaluator("def updateFromBatchInput(bindings):\n"
                        + "  property.setValue(bindings.get(''))");
        ManagedProperty property = new ManagedProperty();
        property.setPropertyTypeCode("p");
        Map<String, String> bindings = new HashMap<String, String>();
        bindings.put("", "42");

        evaluator.updateFromBatchInput(property, bindings);

        assertEquals("42", property.getValue());
    }

    @Test
    public void testUpdateFromBatchInputWithColumns()
    {
        ManagedPropertyEvaluator evaluator =
                new ManagedPropertyEvaluator("def batchColumnNames():\n return ['A', 'B']\n"
                        + "def updateFromBatchInput(bindings):\n"
                        + "  property.setValue(bindings.get('A') + bindings.get('B'))");
        ManagedProperty property = new ManagedProperty();
        property.setPropertyTypeCode("p");
        Map<String, String> bindings = new HashMap<String, String>();
        bindings.put("A", "4");
        bindings.put("B", "2");

        evaluator.updateFromBatchInput(property, bindings);

        assertEquals("42", property.getValue());
    }

    @Test
    public void testUpdateFromBatchInputWithAccessToOriginalColumns()
    {
        ManagedPropertyEvaluator evaluator =
                new ManagedPropertyEvaluator("def batchColumnNames():\n return ['BC1', 'BC2']\n"
                        + "def updateFromBatchInput(bindings):\n"
                        + "  property.setValue(bindings.get('BC1') + ' ' + "
                        + "bindings.get('BC2') + ' ' + "
                        + "bindings.get(originalColumnNameBindingKey('OC')))");
        ManagedProperty property = new ManagedProperty();
        property.setPropertyTypeCode("p");
        Map<String, String> bindings = new HashMap<String, String>();
        bindings.put("BC1", "b1");
        bindings.put("BC2", "b2");
        bindings.put(ManagedPropertyFunctions.originalColumnNameBindingKey("OC"), "original");

        evaluator.updateFromBatchInput(property, bindings);

        assertEquals("b1 b2 original", property.getValue());
    }

}
