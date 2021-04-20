package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import java.util.*;

abstract class AbstractSnapshots<T extends AbstractSnapshot>
{
    protected final IDataSource dataSource;

    private final Set<String> loadedKeys = new HashSet<>();

    private final Map<String, TreeMap<Date, T>> snapshots = new HashMap<>();

    public AbstractSnapshots(IDataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public void put(String key, T snapshot)
    {
        TreeMap<Date, T> snapshotsForKey = snapshots.computeIfAbsent(key, k -> new TreeMap<>());
        snapshotsForKey.put(snapshot.from, snapshot);
    }

    public void load(Collection<String> keysToLoad)
    {
        Set<String> notLoaded = new HashSet<>(keysToLoad);
        notLoaded.removeAll(loadedKeys);

        if (notLoaded.size() > 0)
        {
            List<T> snapshots = doLoad(notLoaded);
            for (T snapshot : snapshots)
            {
                put(snapshot.getKey(), snapshot);
            }
            loadedKeys.addAll(notLoaded);
        }
    }

    protected abstract List<T> doLoad(Collection<String> keysToLoad);

    public Collection<T> get(Collection<String> keys)
    {
        Collection<T> snapshotsForKeys = new ArrayList<>();

        for (String key : keys)
        {
            TreeMap<Date, T> snapshotsForKey = snapshots.get(key);

            if (snapshotsForKey != null)
            {
                snapshotsForKeys.addAll(snapshotsForKey.values());
            }
        }

        return snapshotsForKeys;
    }

    public T get(String key, Date date)
    {
        TreeMap<Date, T> snapshotsForKey = snapshots.get(key);

        if (snapshotsForKey != null)
        {
            Map.Entry<Date, T> potentialEntry = snapshotsForKey.floorEntry(date);

            if (potentialEntry != null)
            {
                T potentialSnapshot = potentialEntry.getValue();
                if (potentialSnapshot.to == null || date.compareTo(potentialSnapshot.to) <= 0)
                {
                    return potentialSnapshot;
                }
            }
        }

        return null;
    }
}