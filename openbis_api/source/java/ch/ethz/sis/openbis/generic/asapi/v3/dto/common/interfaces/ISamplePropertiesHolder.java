package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces;

import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.common.interfaces.ISamplePropertiesHolder")
public interface ISamplePropertiesHolder
{
    public Map<String, ISampleId> getSampleProperties();
}
