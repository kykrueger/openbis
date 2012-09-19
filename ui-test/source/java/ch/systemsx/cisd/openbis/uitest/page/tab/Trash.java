/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.page.tab;

import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Lazy;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.page.common.TopBar;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.DeletionConfirmationBox;

public class Trash extends TopBar
{

    @Locate("empty-trash-button")
    private Button empty;

    @Lazy
    @Locate("deletion-confirmation-dialog")
    private DeletionConfirmationBox deletionDialog;

    public Trash empty()
    {
        this.empty.click();
        deletionDialog.confirm();
        return get(Trash.class);
    }

    @Override
    public String toString()
    {
        return "Trash";
    }
}
