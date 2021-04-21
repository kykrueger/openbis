package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class SpaceSnapshots extends AbstractSnapshots
{

    public SpaceSnapshots(IDataSource dataSource)
    {
        super(dataSource);
    }

    protected void doLoad(Collection<String> spaceCodes)
    {
        List<SpacePE> spaces = dataSource.loadSpaces(new ArrayList<>(spaceCodes));
        for (SpacePE space : spaces)
        {
            Snapshot snapshot = new Snapshot();
            snapshot.from = space.getRegistrationDateInternal();
            snapshot.entityCode = space.getCode();
            snapshot.entityPermId = space.getId().toString();

            put(snapshot.entityCode, snapshot);
        }
    }
}
