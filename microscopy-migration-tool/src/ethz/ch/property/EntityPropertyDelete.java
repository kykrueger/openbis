package ethz.ch.property;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;

public abstract class EntityPropertyDelete {

    protected final String typeCode;
    protected final String propertyCode;
    protected final Boolean unassign;
    protected final Boolean delete;

    public EntityPropertyDelete(String typeCode, String propertyCode, Boolean unassign, Boolean delete) {
        this.typeCode = typeCode;
        this.propertyCode = propertyCode;
        this.unassign = unassign;
        this.delete = delete;
    }

    public abstract void deleteOldPropertyType(String sessionToken, IApplicationServerApi v3);
}
