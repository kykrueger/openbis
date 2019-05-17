package ethz.ch.property;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;

public abstract class EntityPropertyDelete {

    protected final String typeCode;
    protected final String propertyCode;

    public EntityPropertyDelete(String typeCode, String propertyCode) {
        this.typeCode = typeCode;
        this.propertyCode = propertyCode;
    }

    public abstract void deleteOldPropertyType(String sessionToken, IApplicationServerApi v3);
}
