package ethz.ch.dataset;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertyAssignmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ethz.ch.MasterdataHelper;
import ethz.ch.MetadataHelper;
import ethz.ch.property.EntityPropertyCopy;

import java.util.List;

public class DataSetPropertyCopy extends EntityPropertyCopy<DataSet> {

    public DataSetPropertyCopy(String typeCode, String oldPropertyCode, String newPropertyCode) {
        super(typeCode, oldPropertyCode, newPropertyCode);
    }

    @Override
    public IPropertyAssignmentsHolder getPropertyAssignmentsHolder(String sessionToken, IApplicationServerApi v3) {
        return MasterdataHelper.getDataSetType(sessionToken, v3, typeCode);
    }

    @Override
    public void updatePropertyAssignmentsHolder(String sessionToken, IApplicationServerApi v3) {
        MasterdataHelper.updateDataSetType(sessionToken, v3, typeCode, 1, newPropertyCode);
    }

    @Override
    public List<DataSet> getEntities(String sessionToken, IApplicationServerApi v3) {
        return MetadataHelper.getDataSets(sessionToken, v3, typeCode);
    }

    @Override
    public void updateEntityProperty(String sessionToken, IApplicationServerApi v3, DataSet entity) {
        MetadataHelper.updateDataSetProperty(sessionToken, v3, entity.getPermId().getPermId(), newPropertyCode, entity.getProperty(oldPropertyCode));
    }
}
