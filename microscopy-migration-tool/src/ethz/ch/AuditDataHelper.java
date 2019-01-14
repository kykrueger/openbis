package ethz.ch;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;

public class AuditDataHelper
{
    private static class AuditData {
        public String samplePermId;
        public String modifier;
        public Date modificationDate;
        public String registrator;
        public Date registrationDate;
    }
    
    private static Map<String, AuditData> auditDatas = new HashMap<>();
    
    public static void addAuditData(String sampleIdentifier, Experiment experiment) {
        AuditData auditData = new AuditData();
        auditData.modifier = experiment.getModifier().getPermId().getPermId();
        auditData.modificationDate = experiment.getModificationDate();
        auditData.registrator = experiment.getRegistrator().getPermId().getPermId();
        auditData.registrationDate = experiment.getRegistrationDate();
        auditDatas.put(sampleIdentifier, auditData);
    }
    
    public static void addSamplePermId(String sampleIdentifier, String samplePermId) {
        auditDatas.get(sampleIdentifier).samplePermId = samplePermId;
    }
    
    public static void writeSQLAuditUpdate() {
        System.out.println("Writing openbis_audit_data_update.sql");
        String SQL_AUDIT_DATA_UPDATE = AuditDataHelper.getSQLUpdate();
        try {
            FileUtils.writeStringToFile(new File("openbis_audit_data_update.sql"), SQL_AUDIT_DATA_UPDATE, "UTF-8");
            System.out.println("openbis_audit_data_update.sql writen.");
        } catch(Exception ex) {
            System.out.println("openbis_audit_data_update.sql failed to write.");
        }
    }
    
    private static String getDateTimestamp(Date date) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        String nowAsISO = df.format(date);
        return nowAsISO;
    }
    
    private static String getSQLUpdate() {
        String UPDATE_SCRIPT = "";
        for(AuditData auditData:auditDatas.values()) {
            UPDATE_SCRIPT += getSQLUpdateLine(auditData) + "\n";
        }
        return UPDATE_SCRIPT;
    }
    
    private static String getSQLUpdateLine(AuditData auditData) {
        String GET_PERSON = "SELECT id FROM persons WHERE user_id = '%s'";
        String pers_id_registerer = String.format(GET_PERSON, auditData.registrator);
        String pers_id_modifier = String.format(GET_PERSON, auditData.modifier);
        String registration_timestamp = getDateTimestamp(auditData.registrationDate);
        String modification_timestamp = getDateTimestamp(auditData.modificationDate);
        String permId = auditData.samplePermId;
        String SQL = "UPDATE samples_all SET pers_id_registerer = (%s), pers_id_modifier = (%s), registration_timestamp = '%s', modification_timestamp = '%s' WHERE perm_id = '%s';";
        return String.format(SQL, pers_id_registerer, pers_id_modifier, registration_timestamp, modification_timestamp, permId);
    }
}
