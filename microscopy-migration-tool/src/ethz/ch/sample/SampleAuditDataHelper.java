package ethz.ch.sample;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;

public class SampleAuditDataHelper
{
    public static void appendAuditUpdate(String fileName, SamplePermId samplePermId, Experiment experiment) throws IOException {
        String permIdRegisterer = experiment.getRegistrator().getPermId().getPermId();
        String permIdModifier = experiment.getModifier().getPermId().getPermId();
        Date registrationDate = experiment.getRegistrationDate();
        Date modificationDate = experiment.getModificationDate();
        
        String line = getSQLUpdateLine(samplePermId, permIdRegisterer, permIdModifier, registrationDate, modificationDate);
        Writer output;
        output = new FileWriter(fileName, true);
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
    
    private static String getSQLUpdateLine(SamplePermId samplePermId, String permIdRegisterer, String permIdModifier, Date registrationDate, Date modificationDate) {
        String GET_PERSON = "SELECT id FROM persons WHERE user_id = '%s'";
        String persIdRegisterer = String.format(GET_PERSON, permIdRegisterer);
        String persIdModifier = String.format(GET_PERSON, permIdModifier);
        String registrationTimestamp = getDateTimestamp(registrationDate);
        String modificationTimestamp = getDateTimestamp(modificationDate);
        String permId = samplePermId.getPermId();
        String SQL = "UPDATE samples_all SET pers_id_registerer = (%s), pers_id_modifier = (%s), registration_timestamp = '%s', modification_timestamp = '%s' WHERE perm_id = '%s';";
        return String.format(SQL, persIdRegisterer, persIdModifier, registrationTimestamp, modificationTimestamp, permId);
    }
}
