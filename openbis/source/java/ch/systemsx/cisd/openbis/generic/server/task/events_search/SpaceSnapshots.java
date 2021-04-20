package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class SpaceSnapshots extends AbstractSnapshots<SpaceSnapshot>
{

    public SpaceSnapshots(IDataSource dataSource)
    {
        super(dataSource);
    }

    protected List<SpaceSnapshot> doLoad(Collection<String> spaceCodes)
    {
        List<SpaceSnapshot> snapshots = new ArrayList<>();

        List<SpacePE> spaces = dataSource.loadSpaces(new ArrayList<>(spaceCodes));
        for (SpacePE space : spaces)
        {
            SpaceSnapshot snapshot = new SpaceSnapshot();
            snapshot.from = space.getRegistrationDateInternal();
            snapshot.spaceCode = space.getCode();
            snapshot.spaceTechId = space.getId();
            snapshots.add(snapshot);
        }

        return snapshots;
    }
}
