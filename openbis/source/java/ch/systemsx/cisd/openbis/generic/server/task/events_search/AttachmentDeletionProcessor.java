package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

class AttachmentDeletionProcessor extends GenericEventProcessor
{

    private static final SimpleDateFormat VALID_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    AttachmentDeletionProcessor(IDataSource dataSource)
    {
        super(dataSource, EventType.DELETION, EntityType.ATTACHMENT);
    }

    @Override protected void process(LastTimestamps lastTimestamps, SnapshotsFacade snapshots, EventPE oldEvent, NewEvent newEvent) throws Exception
    {
        if (oldEvent.getContent() == null || oldEvent.getContent().trim().isEmpty())
        {
            dataSource.createEventsSearch(newEvent.toNewEventPE());
            return;
        }

        @SuppressWarnings("unchecked") Map<String, Object> parsedContent =
                (Map<String, Object>) OBJECT_MAPPER.readValue(oldEvent.getContent(), Object.class);

        for (String attachmentPath : parsedContent.keySet())
        {
            @SuppressWarnings("unchecked") List<Map<String, String>> entries =
                    (List<Map<String, String>>) parsedContent.get(attachmentPath);

            for (Map<String, String> entry : entries)
            {
                String type = entry.get("type");
                String key = entry.get("key");

                if ("ATTACHMENT".equals(type) && "OWNED".equals(key))
                {
                    String entityType = entry.get("entityType");
                    String value = entry.get("value");

                    if (EntityType.PROJECT.name().equals(entityType))
                    {
                        snapshots.fillByProjectPermId(value, newEvent);
                    } else if (EntityType.EXPERIMENT.name().equals(entityType))
                    {
                        snapshots.fillByExperimentPermId(value, newEvent);
                    } else if (EntityType.SAMPLE.name().equals(entityType))
                    {
                        snapshots.fillBySamplePermId(value, newEvent);
                    }

                    newEvent.entityRegisterer = entry.get("userId");
                    newEvent.content = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(entries);

                    String validFrom = entry.get("validFrom");
                    if (validFrom != null)
                    {
                        newEvent.entityRegistrationTimestamp = VALID_TIMESTAMP_FORMAT.parse(validFrom);
                    }

                    dataSource.createEventsSearch(newEvent.toNewEventPE());
                    return;
                }
            }
        }
    }
}