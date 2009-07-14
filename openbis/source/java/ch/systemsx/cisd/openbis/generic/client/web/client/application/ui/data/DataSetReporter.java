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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;

/**
 * @author Tomasz Pylak
 */
// TODO 2009-07-08, Tomasz Pylak: implement me, this is just a stub
public class DataSetReporter
{
    protected static final String ID = GenericConstants.ID_PREFIX + "data-set-report";

    public static String createId()
    {
        return ID;
    }

    public static Component create(TableModel tableModel)
    {
        LayoutContainer panel = new LayoutContainer();
        panel.setScrollMode(Scroll.AUTO);
        Html content = new Html("TODO");
        panel.add(content);
        return panel;
    }

}
