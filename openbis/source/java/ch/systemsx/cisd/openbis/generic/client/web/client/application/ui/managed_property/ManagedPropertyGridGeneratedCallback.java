/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.managed_property;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AsyncCallbackWithProgressBar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.shared.basic.IManagedPropertyGridInformationProvider;

/**
 * @author Piotr Buczek
 */
public class ManagedPropertyGridGeneratedCallback extends
        AbstractAsyncCallback<TableModelReference>
{
    public interface IOnGridComponentGeneratedAction
    {
        void execute(final IDisposableComponent gridComponent);
    }

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final IOnGridComponentGeneratedAction action;

    private final IManagedPropertyGridInformationProvider gridInfo;

    public static AsyncCallback<TableModelReference> create(
            IViewContext<ICommonClientServiceAsync> viewContext,
            IManagedPropertyGridInformationProvider gridInfo, IOnGridComponentGeneratedAction action)
    {
        return AsyncCallbackWithProgressBar.decorate(new ManagedPropertyGridGeneratedCallback(
                viewContext, gridInfo, action), "Generating the table...");
    }

    private ManagedPropertyGridGeneratedCallback(
            IViewContext<ICommonClientServiceAsync> viewContext,
            IManagedPropertyGridInformationProvider gridInfo, IOnGridComponentGeneratedAction action)
    {
        super(viewContext);
        this.viewContext = viewContext;
        this.gridInfo = gridInfo;
        this.action = action;
    }

    @Override
    protected void process(final TableModelReference tableModelReference)
    {
        final IDisposableComponent reportComponent =
                ManagedPropertyGrid.create(viewContext, tableModelReference, gridInfo);
        action.execute(reportComponent);
        if (StringUtils.isBlank(tableModelReference.tryGetMessage()) == false)
        {
            MessageBox.info(null, tableModelReference.tryGetMessage(), null);
        }
    }

}
