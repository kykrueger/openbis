package ethz.ch.dataset;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ethz.ch.MasterdataHelper;
import ethz.ch.property.EntityPropertyDelete;

public class DataSetPropertyDelete extends EntityPropertyDelete {

    public DataSetPropertyDelete(String typeCode, String propertyCode) {
        super(typeCode, propertyCode);
    }

    @Override
    public void deleteOldPropertyType(String sessionToken, IApplicationServerApi v3) {
        DataSetType type = MasterdataHelper.getDataSetType(sessionToken, v3, typeCode);
        boolean found = false;
        for(PropertyAssignment propertyAssignment:type.getPropertyAssignments()) {
            found = propertyAssignment.getPropertyType().getCode().equals(propertyCode);
            if (found) {
                break;
            }
        }
        if (found) {
            MasterdataHelper.updateDataSetType(sessionToken, v3, typeCode, MasterdataHelper.PropertyTypeUpdateAction.REMOVE, 1, propertyCode);
            MasterdataHelper.deletePropertyType(sessionToken, v3, propertyCode);
            System.out.println("[PROPERTY DELETE] " + "\t" + propertyCode);
        }
    }
}
