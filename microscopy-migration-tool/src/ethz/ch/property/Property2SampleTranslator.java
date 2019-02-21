package ethz.ch.property;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;

public class Property2SampleTranslator
{
    public static void translate(String sessionToken, 
            IApplicationServerApi v3, 
            IDataStoreServerApi v3dss,
            Property2Sample toMigrate, 
            boolean COMMIT_CHANGES_TO_OPENBIS) throws Exception {
            System.out.println("[START]\t" + toMigrate.getSamplePermId());
            
    }
}
