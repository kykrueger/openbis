package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelatedEntities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;

/**
 * A {@link IOriginalDataProvider} implementation for data sets related to other entities.
 * 
 * @author Piotr Buczek
 */
final class ListRelatedDataSetOriginalDataProvider extends
        AbstractOriginalDataProvider<ExternalData>
{

    private final DataSetRelatedEntities entities;

    ListRelatedDataSetOriginalDataProvider(final ICommonServer commonServer,
            final String sessionToken, final DataSetRelatedEntities entities)
    {
        super(commonServer, sessionToken);
        this.entities = entities;
    }

    //
    // AbstractOriginalDataProvider
    //

    @Override
    public final List<ExternalData> getFullOriginalData()
    {
        final List<ExternalData> hits =
                commonServer.listRelatedDataSets(sessionToken, entities, false);
        return hits;
    }
}
