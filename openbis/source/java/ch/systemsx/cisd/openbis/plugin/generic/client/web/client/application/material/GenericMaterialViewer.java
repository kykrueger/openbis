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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material;

import java.util.Set;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> material viewer.
 * 
 * @author Piotr Buczek
 */
public final class GenericMaterialViewer extends AbstractViewer<IGenericClientServiceAsync>
        implements IDatabaseModificationObserver
{
    private static final String PREFIX = "generic-material-viewer_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private final String materialIdentifier;

    private Material originalMaterial;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            final String materialIdentifier)
    {
        GenericMaterialViewer viewer = new GenericMaterialViewer(viewContext, materialIdentifier);
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    private GenericMaterialViewer(final IViewContext<IGenericClientServiceAsync> viewContext,
            final String materialIdentifier)
    {
        super(viewContext, "Material " + materialIdentifier, createId(materialIdentifier));
        this.materialIdentifier = materialIdentifier;
        reloadData();
    }

    public static String createId(String materialIdentifier)
    {
        return ID_PREFIX + materialIdentifier;
    }

    private static void addSection(final LayoutContainer lc, final Widget w)
    {
        lc.add(w, new RowData(-1, -1, new Margins(5)));
    }

    /**
     * Load the material information.
     */
    protected void reloadData()
    {
        viewContext.getService().getMaterialInfo(materialIdentifier,
                new MaterialInfoCallback(viewContext, this));
    }

    public static final class MaterialInfoCallback extends AbstractAsyncCallback<Material>
    {
        private final GenericMaterialViewer genericMaterialViewer;

        private MaterialInfoCallback(final IViewContext<IGenericClientServiceAsync> viewContext,
                final GenericMaterialViewer genericSampleViewer)
        {
            super(viewContext);
            this.genericMaterialViewer = genericSampleViewer;
        }

        //
        // AbstractAsyncCallback
        //

        /**
         * Sets the {@link Material} for this <var>generic</var> material viewer.
         * <p>
         * This method triggers the whole <i>GUI</i> construction.
         * </p>
         */
        @Override
        protected final void process(final Material result)
        {
            genericMaterialViewer.setOriginalMaterial(result);
            genericMaterialViewer.enableEdit(true);
            genericMaterialViewer.removeAll();
            genericMaterialViewer.setScrollMode(Scroll.AUTO);
            addSection(genericMaterialViewer, new MaterialPropertiesSection(result, viewContext));
            genericMaterialViewer.layout();
        }
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { DatabaseModificationKind.edit(ObjectKind.MATERIAL),
                    DatabaseModificationKind.createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                    DatabaseModificationKind.createOrDelete(ObjectKind.VOCABULARY_TERM) };
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        reloadData(); // reloads everything
    }

    void setOriginalMaterial(Material result)
    {
        this.originalMaterial = result;
    }

    @Override
    protected void showEntityEditor()
    {
        assert originalMaterial != null;
        showEntityEditor(viewContext, EntityKind.MATERIAL, originalMaterial.getMaterialType(),
                originalMaterial);
    }

}