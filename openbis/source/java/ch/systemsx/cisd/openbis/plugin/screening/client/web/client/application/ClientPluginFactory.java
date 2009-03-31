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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application;

import java.util.Collections;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.Component;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DummyComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.ClientPluginAdapter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.EditableExperiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.EditableSample;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.IEditableEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.SampleTypeCode;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;

/**
 * {@link IClientPluginFactory} implementation for <i>Screening</i> technology.
 * <p>
 * Currently, this implementation only runs for a sample of type {@link SampleTypeCode#CELL_PLATE}.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class ClientPluginFactory extends
        AbstractClientPluginFactory<IScreeningClientServiceAsync>
{

    public ClientPluginFactory(final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        super(originalViewContext);
    }

    //
    // AbstractClientPluginFactory
    //

    @Override
    protected final IViewContext<IScreeningClientServiceAsync> createViewContext(
            final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        return new ScreeningViewContext(originalViewContext);
    }

    //
    // IClientPluginFactory
    //

    public final Set<String> getEntityTypeCodes(final EntityKind entityKind)
    {
        if (entityKind == EntityKind.SAMPLE)
        {
            return Collections.singleton(SampleTypeCode.MASTER_PLATE.getCode());
        }
        return Collections.emptySet();
    }

    @SuppressWarnings("unchecked")
    public <T extends EntityType, S extends EntityTypePropertyType<T>, P extends EntityProperty<T, S>, I extends IIdentifierHolder, V extends IEditableEntity<T, S, P>> IClientPlugin<T, S, P, I, V> createClientPlugin(
            final EntityKind entityKind)
    {
        if (EntityKind.EXPERIMENT.equals(entityKind))
        {
            return (IClientPlugin<T, S, P, I, V>) new ExperimentClientPlugin();
        }
        if (EntityKind.SAMPLE.equals(entityKind))
        {
            return (IClientPlugin<T, S, P, I, V>) new SampleClientPlugin();
        }
        throw new UnsupportedOperationException("IClientPlugin for entity kind '" + entityKind
                + "' not implemented yet.");
    }

    //
    // Helper classes
    //

    private final class SampleClientPlugin
            implements
            IClientPlugin<SampleType, SampleTypePropertyType, SampleProperty, IIdentifierHolder, EditableSample>
    {
        //
        // IViewClientPlugin
        //

        public final ITabItemFactory createEntityViewer(final IIdentifierHolder sample)
        {
            final String sampleIdentifier = sample.getIdentifier();
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        final ScreeningSampleViewer sampleViewer =
                                new ScreeningSampleViewer(getViewContext(), sampleIdentifier);
                        // TODO 2009-03-31, Tomasz Pylak: make aware of db modifications
                        return DefaultTabItem.createUnaware(sampleIdentifier, sampleViewer, false);
                    }

                    public String getId()
                    {
                        return ScreeningSampleViewer.createId(sampleIdentifier);
                    }
                };
        }

        public final DatabaseModificationAwareWidget createRegistrationForEntityType(
                final SampleType sampleTypeCode)
        {
            return DatabaseModificationAwareWidget.wrapUnaware(new DummyComponent());
        }

        public final Widget createBatchRegistrationForEntityType(final SampleType sampleType)
        {
            return new DummyComponent();
        }

        public ITabItemFactory createEntityEditor(final EditableSample entity)
        {
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        return createDummyTab(entity.getIdentifier());
                    }

                    public String getId()
                    {
                        return DummyComponent.ID;
                    }
                };
        }

    }

    private final static class ExperimentClientPlugin
            extends
            ClientPluginAdapter<ExperimentType, ExperimentTypePropertyType, ExperimentProperty, IIdentifierHolder, EditableExperiment>
    {

        //
        // IViewClientPlugin
        //

        @Override
        public final ITabItemFactory createEntityViewer(final IIdentifierHolder identifier)
        {
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        return createDummyTab(identifier.getIdentifier());
                    }

                    public String getId()
                    {
                        return DummyComponent.ID;
                    }
                };
        }
    }

    private static ITabItem createDummyTab(final String identifier)
    {
        Component component = new DummyComponent();
        return DefaultTabItem.createUnaware(identifier, component, false);
    }
}
