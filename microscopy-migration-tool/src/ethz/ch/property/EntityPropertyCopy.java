package ethz.ch.property;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertyAssignmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import java.util.List;

public abstract class EntityPropertyCopy<ENTITY extends IPermIdHolder & IPropertiesHolder> {
    protected final String typeCode;
    protected final String oldPropertyCode;
    protected final String newPropertyCode;

    public EntityPropertyCopy(String typeCode, String oldPropertyCode, String newPropertyCode) {
        this.typeCode = typeCode;
        this.oldPropertyCode = oldPropertyCode;
        this.newPropertyCode = newPropertyCode;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public String getOldPropertyCode() {
        return oldPropertyCode;
    }

    public String getNewPropertyCode() {
        return newPropertyCode;
    }

    public abstract IPropertyAssignmentsHolder getPropertyAssignmentsHolder(String sessionToken, IApplicationServerApi v3);

    public abstract void updatePropertyAssignmentsHolder(String sessionToken, IApplicationServerApi v3);

    public abstract List<ENTITY> getEntities(String sessionToken, IApplicationServerApi v3);

    public abstract void updateEntityProperty(String sessionToken, IApplicationServerApi v3, ENTITY entity);

    public abstract EntityPropertyDelete getEntityPropertyDelete();

    public void copy(String sessionToken, IApplicationServerApi v3) {
        // Is Property B assigned to the type? If not Do
        System.out.println("5. Copy Property " + oldPropertyCode + " to Property " + newPropertyCode + " on " + typeCode);
        IPropertyAssignmentsHolder propertyAssignmentsHolder = getPropertyAssignmentsHolder(sessionToken, v3);
        boolean found = false;
        for (PropertyAssignment propertyAssignment:propertyAssignmentsHolder.getPropertyAssignments()) {
            found = propertyAssignment.getPropertyType().getCode().equals(newPropertyCode);
            if (found) {
                break;
            }
        }
        if (!found) {
            System.out.println("Property Type " + newPropertyCode + " not found on " + typeCode);
            updatePropertyAssignmentsHolder(sessionToken, v3);
            System.out.println("Property Type " + newPropertyCode + " created on " + typeCode);
        }

        // Copy
        int total = 0;
        List<ENTITY> entities = getEntities(sessionToken, v3);

        for(ENTITY entity:entities) {
            if (entity.getProperty(oldPropertyCode) != null) {
                if(!entity.getProperty(oldPropertyCode).equals(entity.getProperty(newPropertyCode))) {
                    updateEntityProperty(sessionToken, v3, entity);
                    System.out.println("[PREPARING COPY] " + entity.getPermId() + "\t" + entity.getProperty(oldPropertyCode) + "\t" + total + "/" + entities.size());
                } else {
                    System.out.println("[SKIP COPY] " + entity.getPermId() + "\t" + entity.getProperty(oldPropertyCode) + "\t" + total + "/" + entities.size());
                }
            }
            total++;
        }
        System.out.println("[DONE COPY] " + total + "/" + entities.size());

    }
}
