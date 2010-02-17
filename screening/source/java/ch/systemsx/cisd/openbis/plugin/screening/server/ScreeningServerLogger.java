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

package ch.systemsx.cisd.openbis.plugin.screening.server;

import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.openbis.generic.server.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateSingleImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;

/**
 * The <i>screening</i> specific {@link AbstractServerLogger} extension.
 * 
 * @author Tomasz Pylak
 */
final class ScreeningServerLogger extends AbstractServerLogger implements IScreeningServer
{
    ScreeningServerLogger(final ISessionManager<Session> sessionManager,
            final boolean invocationSuccessful, final long elapsedTime)
    {
        super(sessionManager, invocationSuccessful, elapsedTime);
    }

    public final SampleParentWithDerived getSampleInfo(final String sessionToken,
            final TechId sampleId)
    {
        logAccess(sessionToken, "getSampleInfo", "ID(%s)", sampleId);
        return null;
    }

    public void registerSample(final String sessionToken, final NewSample newSample,
            final Collection<NewAttachment> attachments)
    {
        logTracking(sessionToken, "registerSample", "SAMPLE_TYPE(%s) SAMPLE(%s) ATTACHMENTS(%s)",
                newSample.getSampleType(), newSample.getIdentifier(), attachments.size());
    }

    public PlateContent getPlateContent(String sessionToken, TechId plateId)
    {
        logAccess(sessionToken, "getPlateContent", "PLATE(%s)", plateId.getId());
        return null;
    }

    public PlateImages getPlateContentForDataset(String sessionToken, TechId datasetId)
    {
        logAccess(sessionToken, "getPlateContentForDataset", "DATASET(%s)", datasetId.getId());
        return null;
    }

    public List<WellContent> getPlateLocations(String sessionToken, TechId geneMaterialId,
            ExperimentIdentifier experimentIdentifier)
    {
        logAccess(sessionToken, "getPlateLocations", "geneMaterialId(%s) experimentIdentifier(%s)",
                geneMaterialId.getId(), experimentIdentifier);
        return null;
    }

    public Material getMaterialInfo(String sessionToken, TechId materialId)
    {
        logAccess(sessionToken, "getMaterialInfo", "materialId(%s)", materialId.getId());
        return null;
    }

    public ExternalData getDataSetInfo(String sessionToken, TechId datasetId)
    {
        logAccess(sessionToken, "getDataSetInfo", "datasetId(%s)", datasetId.getId());
        return null;
    }

    public List<PlateSingleImageReference> listPlateImages(String sessionToken, TechId plateId)
    {
        logAccess(sessionToken, "loadAllImages", "PLATE(%s)", plateId.getId());
        return null;
    }
}
