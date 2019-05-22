package ethz.ch.sample;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ethz.ch.MasterdataHelper;
import ethz.ch.property.EntityPropertyDelete;

public class SamplePropertyDelete extends EntityPropertyDelete {

    public SamplePropertyDelete(String typeCode, String propertyCode, Boolean unassign, Boolean delete) {
        super(typeCode, propertyCode, unassign, delete);
    }

    @Override
    public void deleteOldPropertyType(String sessionToken, IApplicationServerApi v3) {
        SampleType type = MasterdataHelper.getSampleType(sessionToken, v3, typeCode);
        boolean found = false;
        for(PropertyAssignment propertyAssignment:type.getPropertyAssignments()) {
            found = propertyAssignment.getPropertyType().getCode().equals(propertyCode);
            if (found) {
                break;
            }
        }
        if (found) {
            if (unassign) {
                MasterdataHelper.updateSampleType(sessionToken, v3, typeCode, MasterdataHelper.PropertyTypeUpdateAction.REMOVE, 1, propertyCode);
                System.out.println("[PROPERTY UNASSIGN] " + "\t" + propertyCode);
            }
            if (delete) {
                MasterdataHelper.deletePropertyType(sessionToken, v3, propertyCode);
                System.out.println("[PROPERTY DELETE] " + "\t" + propertyCode);
            }
        }
    }
}
