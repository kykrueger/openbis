package ethz.ch;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;

public class MigrationAttachmentsHelper
{   
    private static Map<String, List<Attachment>> attachments = new HashMap<>();
    
    public static void addAttachmentData(Experiment experiment) {
        if(!experiment.getAttachments().isEmpty()) {
            String newSampleIdentifier = "/" + experiment.getIdentifier().getIdentifier().split("/")[1] + "/" + 
                    experiment.getIdentifier().getIdentifier().split("/")[2] + "/" +
                    experiment.getCode();
            attachments.put(newSampleIdentifier, experiment.getAttachments());
        }
    }
    
    public static Map<String, List<Attachment>> getAttachments() {
        return attachments;
    }
    
    
    
}
