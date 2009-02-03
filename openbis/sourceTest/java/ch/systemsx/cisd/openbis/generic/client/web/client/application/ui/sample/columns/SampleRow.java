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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns;

import ch.systemsx.cisd.openbis.generic.client.shared.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.renderer.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.util.RendererTestUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.Row;

/**
 * @author Franz-Josef Elmer
 */
public class SampleRow extends Row
{

    private final String code;

    private String groupIdentifier;

    public SampleRow(final String code)
    {
        super();
        this.code = code;
        withCell(CommonSampleColDefKind.CODE, code);
    }

    public SampleRow identifier(final String instanceCode)
    {
        return identifier(instanceCode, null);
    }

    public SampleRow identifier(final String instanceCode, final String groupCodeOrNull)
    {
        withCell(CommonSampleColDefKind.DATABASE_INSTANCE, instanceCode);
        if (groupCodeOrNull == null)
        {
            withCell(CommonSampleColDefKind.GROUP, "");
        } else
        {
            withCell(CommonSampleColDefKind.GROUP, groupCodeOrNull);
        }
        groupIdentifier = createGroupIdentifier(instanceCode, groupCodeOrNull);
        withCell(CommonSampleColDefKind.SAMPLE_IDENTIFIER, groupIdentifier + code);
        return this;
    }

    public SampleRow experiment(final String projectCode, final String experimentCode)
    {
        withCell(CommonSampleColDefKind.PROJECT, projectCode);
        withCell(CommonSampleColDefKind.EXPERIMENT, experimentCode);
        final String experimentIdentifier = groupIdentifier + projectCode + "/" + experimentCode;
        withCell(CommonSampleColDefKind.EXPERIMENT_IDENTIFIER, experimentIdentifier);
        return this;
    }

    public SampleRow noExperiment()
    {
        withCell(CommonSampleColDefKind.PROJECT, null);
        withCell(CommonSampleColDefKind.EXPERIMENT, null);
        withCell(CommonSampleColDefKind.EXPERIMENT_IDENTIFIER, null);
        return this;
    }

    private String createGroupIdentifier(final String instanceCode, final String groupCodeOrNull)
    {
        String identifier = instanceCode + ":/";
        if (groupCodeOrNull != null)
        {
            identifier += groupCodeOrNull + "/";
        }
        return identifier;
    }

    public SampleRow invalid()
    {
        withInvalidation(true);
        // overwrite previous code
        withCell(CommonSampleColDefKind.CODE, RendererTestUtil.invalidCode(code));
        return this;
    }

    public SampleRow valid()
    {
        withInvalidation(false);
        // just to be sure - overwrite previous code
        withCell(CommonSampleColDefKind.CODE, code);
        return this;
    }

    private void withInvalidation(boolean isInvalid)
    {
        withCell(CommonSampleColDefKind.IS_INVALID, SimpleYesNoRenderer.render(isInvalid));
    }

    public SampleRow derivedFromAncestor(final String ancestorCode, final int level)
    {
        final String identifier =
                new ParentGeneratedFromSampleColDef(level, "dummy").getIdentifier();
        withCell(identifier, ancestorCode);
        return this;
    }

    /**
     * Creates a {@link SampleRow} with given <var>propertyCode</var> associated to given <i>value</i>.
     * <p>
     * Note that we assume that computed {@link PropertyType} is from internal namespace.
     * </p>
     */
    public final SampleRow property(final String propertyCode, final Object value)
    {
        final PropertyType propertyType = createPropertyType(propertyCode, true);
        final String identifier = new PropertySampleColDef(propertyType, true).getIdentifier();
        withCell(identifier, value);
        return this;
    }

    /**
     * Creates a {@link SampleRow} with given <var>propertyCode</var> associated to given <i>value</i>.
     */
    public final SampleRow property(final String propertyCode, final boolean internalNamespace,
            final Object value)
    {
        final PropertyType propertyType = createPropertyType(propertyCode, internalNamespace);
        final String identifier = new PropertySampleColDef(propertyType, true).getIdentifier();
        withCell(identifier, value);
        return this;
    }

    private final static PropertyType createPropertyType(final String propertyCode,
            final boolean internalNamespace)
    {
        final PropertyType propertyType = new PropertyType();
        propertyType.setInternalNamespace(internalNamespace);
        propertyType.setSimpleCode(propertyCode);
        return propertyType;
    }

    private final void withCell(final CommonSampleColDefKind columnKind, final String value)
    {
        withCell(columnKind.id(), value);
    }
}
