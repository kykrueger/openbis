package ethz.ch.sample;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertyAssignmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ethz.ch.MasterdataHelper;
import ethz.ch.MetadataHelper;
import ethz.ch.property.EntityPropertyCopy;

import java.util.List;

public class SamplePropertyCopy extends EntityPropertyCopy<Sample> {

    public SamplePropertyCopy(String typeCode, String oldPropertyCode, String newPropertyCode) {
        super(typeCode, oldPropertyCode, newPropertyCode);
    }

    @Override
    public IPropertyAssignmentsHolder getPropertyAssignmentsHolder(String sessionToken, IApplicationServerApi v3) {
        return MasterdataHelper.getSampleType(sessionToken, v3, typeCode);
    }

    @Override
    public void updatePropertyAssignmentsHolder(String sessionToken, IApplicationServerApi v3) {
        MasterdataHelper.updateSampleType(sessionToken, v3, typeCode, 1, newPropertyCode);
    }

    @Override
    public List<Sample> getEntities(String sessionToken, IApplicationServerApi v3) {
        return MetadataHelper.getSamples(sessionToken, v3, typeCode);
    }

    @Override
    public void updateEntityProperty(String sessionToken, IApplicationServerApi v3, Sample entity) {
        MetadataHelper.updateSampleProperty(sessionToken, v3, entity.getPermId().getPermId(), newPropertyCode, entity.getProperty(oldPropertyCode));
    }
}
