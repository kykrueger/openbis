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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.VocabularyColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.VocabularyTermColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.util.GridTestUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.VocabularyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.VocabularyTermGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * A {@link AbstractGWTTestCase} extension to test <i>Vocabulary Browser</i>.
 * 
 * @author Tomasz Pylak
 */
public class VocabularyBrowserTest extends AbstractGWTTestCase
{

    private static final String VOCABULARY_CODE = "USER.ORGANISM";

    public final void testListVocabularies()
    {
        loginAndGotoTab(ActionMenuKind.VOCABULARY_MENU_BROWSE);
        CheckTableCommand table =
                new CheckTableCommand(VocabularyGrid.GRID_ID,
                        VocabularyGrid.ListEntitiesCallback.class);
        table.expectedColumn(VocabularyColDefKind.CODE.id(), VOCABULARY_CODE);
        remoteConsole.prepare(table.expectedSize(5));

        launchTest(20000);
    }

    public final void testShowTermDetails()
    {
        loginAndGotoTab(ActionMenuKind.VOCABULARY_MENU_BROWSE);
        
        remoteConsole.prepare(new ClickOnVocabularyCmd(VOCABULARY_CODE));

        CheckTableCommand termsTable =
                new CheckTableCommand(VocabularyTermGrid.createGridId(VOCABULARY_CODE),
                        VocabularyTermGrid.ListEntitiesCallback.class);
        expectTermWithCode(termsTable, "FLY");
        expectTermWithCode(termsTable, "GORILLA");
        expectTermWithCode(termsTable, "HUMAN");
        remoteConsole.prepare(termsTable.expectedSize(5));

        launchTest(20000);
    }

    private void expectTermWithCode(CheckTableCommand termsTable, String code)
    {
        termsTable.expectedColumn(VocabularyTermColDefKind.CODE.id(), code);
    }

    public class ClickOnVocabularyCmd extends AbstractDefaultTestCommand
    {
        private final String code;

        public ClickOnVocabularyCmd(final String code)
        {
            addCallbackClass(VocabularyGrid.ListEntitiesCallback.class);
            this.code = code;
        }

        public void execute()
        {
            final Widget widget = GWTTestUtil.getWidgetWithID(VocabularyGrid.GRID_ID);
            GridTestUtils.fireDoubleClick((Grid<?>) widget, VocabularyColDefKind.CODE.id(), code);
        }
    }
}
