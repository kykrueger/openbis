package ethz.ch.tag;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ethz.ch.MetadataHelper;

public class Tag2SampleTranslator
{
    private static Map<String, List<String>> tags = new HashMap<>();
    
    public static void init(String fileName) {
        try
        {
            List<String> taglines = FileUtils.readLines(new File(fileName));
            for(String tagline:taglines) {
                String tag = tagline.split("\t")[0];
                tag = tag.substring(1, tag.length() - 1);
                String permId = tagline.split("\t")[1];
                permId = permId.substring(1, permId.length() - 1);
                List<String> tagsOfPermId = tags.get(permId);
                if(tagsOfPermId == null) {
                    tagsOfPermId = new ArrayList<>();
                    tags.put(permId, tagsOfPermId);
                }
                tagsOfPermId.add(tag);
            }
            System.out.println("Tags readed - " + taglines.size() + " tags for " + tags.keySet().size() + " entities.");
        } catch (IOException e)
        {
            System.out.println("Failed to read Tags");
        }
    }
    
    public static List<String> get(String permId) {
        return tags.get(permId);
    }
    
    public static List<SamplePermId> getOrganizationUnitsFromTags(String sessionToken, IApplicationServerApi v3, boolean COMMIT_CHANGES_TO_OPENBIS, Experiment experiment) {
        List<String> tagsRequired = tags.get(experiment.getPermId().getPermId());
        if(tagsRequired != null) {
            String spaceCode = experiment.getIdentifier().getIdentifier().split("/")[1];
            String ouCollIdentAsString = "/" +  spaceCode + "/COMMON_ORGANIZATION_UNITS/ORGANIZATION_UNITS_COLLECTION";
            ExperimentIdentifier ouCollIdent = new ExperimentIdentifier(ouCollIdentAsString);
                    
            ExperimentFetchOptions ouCollFO = new ExperimentFetchOptions();
            ouCollFO.withSamples().withProperties();
            Map<IExperimentId, Experiment> ouColls = v3.getExperiments(sessionToken, Arrays.asList(ouCollIdent), ouCollFO);
            if(!ouColls.isEmpty()) { // OU Collection created
                Experiment ouColl = ouColls.get(ouCollIdent);
                List<Sample> ousInColl = ouColl.getSamples();
                Map<String, Sample> ousByTagName = new HashMap<String, Sample>();
                for(Sample sample:ousInColl) {
                    ousByTagName.put(sample.getProperty("$NAME"), sample);
                }
                
                List<SamplePermId> ousFound = new ArrayList<>();
                List<SampleCreation> ousMissing = new ArrayList<>();
                
                for(String tagRequired:tagsRequired) {
                    if(ousByTagName.containsKey(tagRequired)) {
                        ousFound.add(ousByTagName.get(tagRequired).getPermId());
                    } else {
                        ousMissing.add(MetadataHelper.getBasicSampleCreation(ouCollIdent, "ORGANIZATION_UNIT", tagRequired));
                    }
                }
                
                if(!ousMissing.isEmpty()) {
                    List<SamplePermId> newOUs = v3.createSamples(sessionToken, ousMissing);
                    ousFound.addAll(newOUs);
                }
                
                System.out.println("Tags Found: " + ousFound.size() + " Tags Missing: " + ousMissing.size());
                
                return ousFound;
            }
        }
        return null;
    }
}
