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

package ch.systemsx.cisd.openbis.uitest.gui;

import ch.systemsx.cisd.openbis.uitest.dsl.Executor;
import ch.systemsx.cisd.openbis.uitest.layout.AddSpaceDialogLocation;
import ch.systemsx.cisd.openbis.uitest.page.AddSpaceDialog;
import ch.systemsx.cisd.openbis.uitest.request.CreateSpace;
import ch.systemsx.cisd.openbis.uitest.type.Space;

/**
 * @author anttil
 */
public class CreateSpaceGui extends Executor<CreateSpace, Space>
{
    @Override
    public Space run(CreateSpace function)
    {
        Space space = function.getSpace();
        AddSpaceDialog dialog = goTo(new AddSpaceDialogLocation());
        dialog.fillWith(space);
        dialog.save();
        return space;
    }

}
