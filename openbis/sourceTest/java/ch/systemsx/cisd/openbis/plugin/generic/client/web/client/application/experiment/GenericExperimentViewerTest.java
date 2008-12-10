/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CategoriesBuilder;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.Login;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.OpenTab;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment_browser.ListExperiments;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment_browser.ShowExperiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.IValueAssertion;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.Row;

/**
 * A {@link AbstractGWTTestCase} extension to test {@link GenericExperimentViewer}.
 * 
 * @author Izabela Adamczyk
 */
public class GenericExperimentViewerTest extends AbstractGWTTestCase
{

    private static final String DEFAULT = "DEFAULT";

    private static final String EXP_REUSE = "EXP-REUSE";

    private static final String CISD_CISD_DEFAULT = "CISD:/CISD/DEFAULT";

    private static final String EXP_X = "EXP-X";

    private static final String A_SIMPLE_EXPERIMENT = "A simple experiment";

    private static final String DOE_JOHN = "Doe, John";

    private static final String CISD_CISD_NEMO = "CISD:/CISD/NEMO";

    private static final String SIRNA_HCS = "SIRNA_HCS";

    private static final String NEMO = "NEMO";

    private static final String EXP1 = "EXP1";

    public final void testShowExperimentDetails()
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.CATEGORIES.EXPERIMENTS,
                CategoriesBuilder.MENU_ELEMENTS.LIST));
        remoteConsole.prepare(new ListExperiments(NEMO, SIRNA_HCS));
        remoteConsole.prepare(new ShowExperiment(EXP1));
        final CheckExperiment checkExperiment = new CheckExperiment(CISD_CISD_NEMO, EXP1);
        checkExperiment.property("Experiment").asString(EXP1);
        checkExperiment.property("Experiment type").asCode(SIRNA_HCS);
        checkExperiment.property("Registrator").asPerson(DOE_JOHN);
        checkExperiment.property("Description").asProperty(A_SIMPLE_EXPERIMENT);
        checkExperiment.property("Gender").asProperty("MALE");
        final CheckTableCommand attachmentsTable =
                checkExperiment.attachmentsTable().expectedSize(1);
        attachmentsTable.expectedRow(new Row().withCell(ModelDataPropertyNames.FILE_NAME,
                "exampleExperiments.txt").withCell(ModelDataPropertyNames.VERSION, 4));
        remoteConsole.prepare(checkExperiment);

        remoteConsole.finish(60000);
        client.onModuleLoad();
    }

    public final void testShowInvalidExperimentDetails()
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.CATEGORIES.EXPERIMENTS,
                CategoriesBuilder.MENU_ELEMENTS.LIST));
        remoteConsole.prepare(new ListExperiments(DEFAULT, SIRNA_HCS));
        remoteConsole.prepare(new ShowExperiment(EXP_X));
        final CheckExperiment checkExperiment = new CheckExperiment(CISD_CISD_DEFAULT, EXP_X);
        checkExperiment.property("Experiment").asString(EXP_X);
        checkExperiment.property("Experiment type").asCode(SIRNA_HCS);
        checkExperiment.property("Registrator").asPerson(DOE_JOHN);
        checkExperiment.property("Invalidation").by(new IValueAssertion<Invalidation>()
            {
                public void assertValue(final Invalidation invalidation)
                {
                    assertEquals("Doe", invalidation.getRegistrator().getLastName());
                    assertNull(invalidation.getReason());
                }
            });
        checkExperiment.property("Description").asProperty(A_SIMPLE_EXPERIMENT);
        remoteConsole.prepare(checkExperiment);

        remoteConsole.finish(60000);
        client.onModuleLoad();
    }

    public final void testListOfAttachments()
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.CATEGORIES.EXPERIMENTS,
                CategoriesBuilder.MENU_ELEMENTS.LIST));
        remoteConsole.prepare(new ListExperiments(DEFAULT, SIRNA_HCS));
        remoteConsole.prepare(new ShowExperiment(EXP_REUSE));
        final CheckExperiment checkExperiment = new CheckExperiment(CISD_CISD_DEFAULT, EXP_REUSE);
        checkExperiment.property("Experiment").asString(EXP_REUSE);
        final CheckTableCommand attachmentsTable =
                checkExperiment.attachmentsTable().expectedSize(2);
        attachmentsTable.expectedRow(new Row().withCell(ModelDataPropertyNames.FILE_NAME,
                "exampleExperiments.txt").withCell(ModelDataPropertyNames.VERSION, 1));
        attachmentsTable.expectedRow(new Row().withCell(ModelDataPropertyNames.FILE_NAME,
                "cellPlates.txt").withCell(ModelDataPropertyNames.VERSION, 1));
        remoteConsole.prepare(checkExperiment);

        remoteConsole.finish(60000);
        client.onModuleLoad();
    }

}
