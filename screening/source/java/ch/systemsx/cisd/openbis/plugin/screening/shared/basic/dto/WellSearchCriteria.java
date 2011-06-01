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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.io.Serializable;
import java.util.Arrays;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.WellSearchGrid;

/**
 * Describes a list of materials for which we search in the {@link WellSearchGrid}.
 * 
 * @author Tomasz Pylak
 */
public class WellSearchCriteria implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    /** points to one experiment */
    public static final class SingleExperimentSearchCriteria implements ISerializable
    {
        private static final long serialVersionUID = ServiceVersionHolder.VERSION;

        private TechId experimentId;

        private String experimentPermId;

        private String experimentIdentifier; // for display purposes and links in simple view mode

        // GWT only
        @SuppressWarnings("unused")
        private SingleExperimentSearchCriteria()
        {
        }

        public SingleExperimentSearchCriteria(long experimentId, String experimentPermId,
                String experimentIdentifier)
        {
            this.experimentId = new TechId(experimentId);
            this.experimentPermId = experimentPermId;
            this.experimentIdentifier = experimentIdentifier;
        }

        public TechId getExperimentId()
        {
            return experimentId;
        }

        public String getExperimentPermId()
        {
            return experimentPermId;
        }

        public String getExperimentIdentifier()
        {
            return experimentIdentifier;
        }

        @Override
        public String toString()
        {
            return "experiment " + experimentIdentifier;
        }
    }

    /** points to one experiment or all of them */
    public static class ExperimentSearchCriteria implements IsSerializable, Serializable
    {
        private static final long serialVersionUID = ServiceVersionHolder.VERSION;

        // if null, all experiments are taken into account
        private SingleExperimentSearchCriteria experimentOrNull;

        public static final ExperimentSearchCriteria createAllExperiments()
        {
            return new ExperimentSearchCriteria(null);

        }

        public static ExperimentSearchCriteria createExperiment(
                SingleExperimentSearchCriteria experiment)
        {
            assert experiment != null : "experiment not specified";
            return new ExperimentSearchCriteria(experiment);
        }

        public static final ExperimentSearchCriteria createExperiment(
                IEntityInformationHolderWithIdentifier experiment)
        {
            return createExperiment(experiment.getId(), experiment.getPermId(),
                    experiment.getIdentifier());
        }

        private static final ExperimentSearchCriteria createExperiment(long experimentId,
                String experimentPermId, String experimentIdentifier)
        {
            return new ExperimentSearchCriteria(new SingleExperimentSearchCriteria(experimentId,
                    experimentPermId, experimentIdentifier));
        }

        // GWT only
        private ExperimentSearchCriteria()
        {
        }

        private ExperimentSearchCriteria(SingleExperimentSearchCriteria experimentOrNull)
        {
            this.experimentOrNull = experimentOrNull;
        }

        public SingleExperimentSearchCriteria tryGetExperiment()
        {
            return experimentOrNull;
        }

        @Override
        public String toString()
        {
            if (experimentOrNull == null)
            {
                return "all experiments";
            } else
            {
                return experimentOrNull.toString();
            }
        }
    }

    public static final class ExperimentSearchByProjectCriteria implements IsSerializable,
            Serializable
    {

        private static final long serialVersionUID = ServiceVersionHolder.VERSION;

        private BasicProjectIdentifier projectIdOrNull;

        public static final ExperimentSearchByProjectCriteria createAllExperimentsForProject(
                BasicProjectIdentifier projectIdentifier)
        {
            if (projectIdentifier == null)
            {
                throw new IllegalArgumentException("Project identifier cannot be null");
            }
            return new ExperimentSearchByProjectCriteria(projectIdentifier);
        }

        public static final ExperimentSearchByProjectCriteria createAllExperimentsForAllProjects()
        {
            return new ExperimentSearchByProjectCriteria(null);
        }

        private ExperimentSearchByProjectCriteria(BasicProjectIdentifier projectIdOrNull)
        {
            this.projectIdOrNull = projectIdOrNull;
        }

        public BasicProjectIdentifier tryGetProjectIdentifier()
        {
            return projectIdOrNull;
        }

        public boolean isAllExperiments()
        {
            return projectIdOrNull == null;
        }

        @Override
        public String toString()
        {
            if (isAllExperiments())
            {
                return super.toString();
            } else
            {
                return projectIdOrNull.toString();
            }
        }
    }

    public static final class MaterialSearchCodesCriteria implements IsSerializable, Serializable
    {
        private static final long serialVersionUID = ServiceVersionHolder.VERSION;

        private String[] materialCodesOrProperties;

        private String[] materialTypeCodes;

        private boolean exactMatchOnly;

        // GWT only
        @SuppressWarnings("unused")
        private MaterialSearchCodesCriteria()
        {
        }

        public MaterialSearchCodesCriteria(String[] materialCodesOrProperties,
                String[] materialTypeCodes, boolean exactMatchOnly)
        {
            this.exactMatchOnly = exactMatchOnly;
            for (int i = 0; i < materialCodesOrProperties.length; i++)
            {
                assert StringUtils.isBlank(materialCodesOrProperties[i]) == false : "material search property is blank";
                materialCodesOrProperties[i] = materialCodesOrProperties[i].toUpperCase();
            }
            this.materialCodesOrProperties = materialCodesOrProperties;
            this.materialTypeCodes = materialTypeCodes;
        }

        public String[] getMaterialCodesOrProperties()
        {
            return materialCodesOrProperties;
        }

        public boolean isExactMatchOnly()
        {
            return exactMatchOnly;
        }

        public String[] getMaterialTypeCodes()
        {
            return materialTypeCodes;
        }

        @Override
        public String toString()
        {
            return "Material codes (or properties) = " + Arrays.toString(materialCodesOrProperties)
                    + ", types = " + Arrays.toString(materialTypeCodes) + ", exactMatchOnly = "
                    + exactMatchOnly;
        }
    }

    public static final class MaterialSearchCriteria implements IsSerializable, Serializable
    {
        private static final long serialVersionUID = ServiceVersionHolder.VERSION;

        // Only one criteria is present

        // -- code or property criteria
        private MaterialSearchCodesCriteria codesOrPropertiesCriteriaOrNull;

        // -- technical id criteria
        private TechId materialIdOrNull;

        /**
         * We look for wells containing materials which have a code contained in the specified list
         * of codes and type contained in the specified list of types.
         */
        public static final MaterialSearchCriteria createCodesCriteria(String[] materialCodes,
                String[] materialTypeCodes, boolean exactMatchOnly)
        {
            return create(new MaterialSearchCodesCriteria(materialCodes, materialTypeCodes,
                    exactMatchOnly));
        }

        public static MaterialSearchCriteria create(
                MaterialSearchCodesCriteria materialCodesCriteria)
        {
            return new MaterialSearchCriteria(materialCodesCriteria, null);
        }

        public static final MaterialSearchCriteria createIdCriteria(TechId materialId)
        {
            assert materialId != null;

            return new MaterialSearchCriteria(null, materialId);
        }

        // GWT only
        private MaterialSearchCriteria()
        {
        }

        private MaterialSearchCriteria(MaterialSearchCodesCriteria codesOrPropertiesCriteriaOrNull,
                TechId materialIdOrNull)
        {
            this.codesOrPropertiesCriteriaOrNull = codesOrPropertiesCriteriaOrNull;
            this.materialIdOrNull = materialIdOrNull;
        }

        public MaterialSearchCodesCriteria tryGetMaterialCodesOrProperties()
        {
            return codesOrPropertiesCriteriaOrNull;
        }

        public TechId tryGetMaterialId()
        {
            return materialIdOrNull;
        }

        @Override
        public String toString()
        {
            if (materialIdOrNull != null)
            {
                return "Material with id = " + materialIdOrNull;
            } else if (codesOrPropertiesCriteriaOrNull != null)
            {
                return codesOrPropertiesCriteriaOrNull.toString();
            } else
            {
                throw new IllegalStateException("unexpected material search criteria");
            }
        }
    }

    private MaterialSearchCriteria materialCriteria;

    private ExperimentSearchCriteria experimentCriteria;

    // GWT
    @SuppressWarnings("unused")
    private WellSearchCriteria()
    {
    }

    public WellSearchCriteria(ExperimentSearchCriteria experimentCriteria,
            MaterialSearchCriteria materialCriteria)
    {
        assert experimentCriteria != null;
        assert materialCriteria != null;

        this.materialCriteria = materialCriteria;
        this.experimentCriteria = experimentCriteria;
    }

    @Override
    public String toString()
    {
        return "Search criteria: " + experimentCriteria + ", " + materialCriteria;
    }

    /** null means all experiments */
    public ExperimentSearchCriteria getExperimentCriteria()
    {
        return experimentCriteria;
    }

    public MaterialSearchCriteria getMaterialSearchCriteria()
    {
        return materialCriteria;
    }
}
