package ethz.ch.property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ethz.ch.MetadataHelper;

public class Property2SampleTranslator
{
    public static void translate(String sessionToken, 
            IApplicationServerApi v3, 
            IDataStoreServerApi v3dss,
            Property2Sample toMigrate, 
            boolean COMMIT_CHANGES_TO_OPENBIS) throws Exception {
            
        System.out.println("[START]\t" + toMigrate.getSamplePermId());
        Sample sample = MetadataHelper.getSample(sessionToken, v3, toMigrate.getSamplePermId());
        String propertyValue = sample.getProperties().get(toMigrate.getConfig().getOldPropertyCode());
        
        // If has not being assigned yet.
        if(propertyValue == null || propertyValue.isEmpty()) {
            System.out.println("[FINISH EMPTY]\t" + toMigrate.getSamplePermId());
            return;
        }
        
        ISampleId sampleId = null;
        // 1. Try to find property sample.
        sampleId = getSampleWithExperimentAndName(sessionToken, v3, toMigrate, sample.getExperiment().getIdentifier(), propertyValue);
        
        // 2. If property sample doesn't exist. Create a basic sample holding it and assign it to the experiment
        if(sampleId == null) {
            SampleCreation sampleCreation = new SampleCreation();
            sampleCreation.setTypeId(new EntityTypePermId(toMigrate.getConfig().getNewSampleTypeCode()));
            sampleCreation.getProperties().put(toMigrate.getConfig().getNewPropertyCode(), propertyValue);
            
            sampleCreation.setSpaceId(sample.getSpace().getPermId());
            sampleCreation.setExperimentId(sample.getExperiment().getPermId());
            
            List<ISampleId> parentIds = new ArrayList<>();
            parentIds.add(sample.getContainer().getPermId());
            
            sampleId = v3.createSamples(sessionToken, Collections.singletonList(sampleCreation)).iterator().next();
            
            // Add to cache to speed up calls
            sampleCache.get(sample.getExperiment().getIdentifier()).put(propertyValue, sampleId);
            
            System.out.println("Sample Property\t" + propertyValue + "\t" + sample.getExperiment().getPermId());
        }
        
        // 3. Assign the property sample as parent of the sample.
        // 4. Delete old property to not creating it twice on retry.
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample.getPermId());
        sampleUpdate.setContainerId(null);
        sampleUpdate.getParentIds().add(sampleId, sample.getContainer().getPermId());
        sampleUpdate.setProperty(toMigrate.getConfig().getOldPropertyCode(), null);
        v3.updateSamples(sessionToken, Collections.singletonList(sampleUpdate));
        System.out.println("Set Parent\t" + propertyValue + "\t" + sample.getIdentifier().getIdentifier());
        System.out.println("Set Parent\t" + sample.getContainer().getIdentifier() + "\t" + sample.getIdentifier().getIdentifier());
        System.out.println("Delete Container" + sample.getContainer().getIdentifier() + "\t" + sample.getIdentifier().getIdentifier());
        System.out.println("Delete property\t" + toMigrate.getConfig().getOldPropertyCode() + "\t" + sample.getIdentifier().getIdentifier());
        System.out.println("[FINISH]\t" + toMigrate.getSamplePermId());
    }
    
    private static Map<ExperimentIdentifier, Map<String, ISampleId>> sampleCache = new HashMap<>();
    
    public static ISampleId getSampleWithExperimentAndName(String sessionToken, IApplicationServerApi v3, Property2Sample toMigrate, ExperimentIdentifier experimentIdentifier, String name) {
        Map<String, ISampleId> esPcache = sampleCache.get(experimentIdentifier);
        if(esPcache == null) {
            esPcache = new HashMap<>();
            sampleCache.put(experimentIdentifier, esPcache);
        }
        ISampleId sampleForProperty = esPcache.get(name);
        return sampleForProperty;
    }
}
