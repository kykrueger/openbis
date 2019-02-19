package ethz.ch;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;

public class AuditDataHelper
{
    public static void addAuditUpdate(SamplePermId samplePermId, Experiment experiment) throws IOException {
        String line = getSQLUpdateLine(samplePermId, experiment);
        Writer output;
        output = new FileWriter("openbis_audit_data_update.sql", true);
        output.append(line + "\n");
        output.close();
    }
    
    private static String getDateTimestamp(Date date) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        String nowAsISO = df.format(date);
        return nowAsISO;
    }
    
    private static String getSQLUpdateLine(SamplePermId samplePermId, Experiment experiment) {
        String GET_PERSON = "SELECT id FROM persons WHERE user_id = '%s'";
        String pers_id_registerer = String.format(GET_PERSON, experiment.getRegistrator().getPermId().getPermId());
        String pers_id_modifier = String.format(GET_PERSON, experiment.getModifier().getPermId().getPermId());
        String registration_timestamp = getDateTimestamp(experiment.getRegistrationDate());
        String modification_timestamp = getDateTimestamp(experiment.getModificationDate());
        String permId = samplePermId.getPermId();
        String SQL = "UPDATE samples_all SET pers_id_registerer = (%s), pers_id_modifier = (%s), registration_timestamp = '%s', modification_timestamp = '%s' WHERE perm_id = '%s';";
        return String.format(SQL, pers_id_registerer, pers_id_modifier, registration_timestamp, modification_timestamp, permId);
    }
}
