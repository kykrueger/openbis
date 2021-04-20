package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import java.util.*;

abstract class AbstractSnapshots
{
    protected final IDataSource dataSource;

    private final Set<String> loadedKeys = new HashSet<>();

    private final Map<String, TreeMap<Date, Snapshot>> snapshots = new HashMap<>();

    public AbstractSnapshots(IDataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    protected abstract String getKey(Snapshot snapshot);

    protected abstract List<Snapshot> doLoad(Collection<String> keysToLoad);

    public void put(String key, Snapshot snapshot)
    {
        TreeMap<Date, Snapshot> snapshotsForKey = snapshots.computeIfAbsent(key, k -> new TreeMap<>());
        snapshotsForKey.put(snapshot.from, snapshot);
    }

    public void load(Collection<String> keysToLoad)
    {
        Set<String> notLoaded = new HashSet<>(keysToLoad);
        notLoaded.removeAll(loadedKeys);

        if (notLoaded.size() > 0)
        {
            List<Snapshot> snapshots = doLoad(notLoaded);
            for (Snapshot snapshot : snapshots)
            {
                put(getKey(snapshot), snapshot);
            }
            loadedKeys.addAll(notLoaded);
        }
    }

    public Collection<Snapshot> get(Collection<String> keys)
    {
        Collection<Snapshot> snapshotsForKeys = new ArrayList<>();

        for (String key : keys)
        {
            TreeMap<Date, Snapshot> snapshotsForKey = snapshots.get(key);

            if (snapshotsForKey != null)
            {
                snapshotsForKeys.addAll(snapshotsForKey.values());
            }
        }

        return snapshotsForKeys;
    }

    public Snapshot get(String key, Date date)
    {
        TreeMap<Date, Snapshot> snapshotsForKey = snapshots.get(key);

        if (snapshotsForKey != null)
        {
            Map.Entry<Date, Snapshot> potentialEntry = snapshotsForKey.floorEntry(date);

            if (potentialEntry != null)
            {
                Snapshot potentialSnapshot = potentialEntry.getValue();
                if (potentialSnapshot.to == null || date.compareTo(potentialSnapshot.to) <= 0)
                {
                    return potentialSnapshot;
                }
            }
        }

        return null;
    }
}