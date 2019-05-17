package ethz.ch.property;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PropertyCopy {
    private String sampleTypeCode;
    private String oldPropertyCode;
    private String newPropertyCode;

    public PropertyCopy(String sampleTypeCode, String oldPropertyCode, String newPropertyCode) {
        this.sampleTypeCode = sampleTypeCode;
        this.oldPropertyCode = oldPropertyCode;
        this.newPropertyCode = newPropertyCode;
    }

    public String getSampleTypeCode() {
        return sampleTypeCode;
    }

    public String getOldPropertyCode() {
        return oldPropertyCode;
    }

    public String getNewPropertyCode() {
        return newPropertyCode;
    }

    public void copy(String sessionToken, IApplicationServerApi v3) {
        // Is Property B assigned to the type? If not Do
        System.out.println("5. Copy Property " + this.getOldPropertyCode() + " to Property " + this.getNewPropertyCode() + " on " + this.getSampleTypeCode());
        EntityTypePermId id = new EntityTypePermId(this.getSampleTypeCode());
        SampleTypeFetchOptions sampleTypeFetchOptions = new SampleTypeFetchOptions();
        sampleTypeFetchOptions.withPropertyAssignments().withPropertyType();
        Map<IEntityTypeId, SampleType> sampleTypes = v3.getSampleTypes(sessionToken, Arrays.asList(id), sampleTypeFetchOptions);
        SampleType sampleType = sampleTypes.get(id);
        boolean found = false;
        for (PropertyAssignment propertyAssignment:sampleType.getPropertyAssignments()) {
            found = propertyAssignment.getPropertyType().getCode().equals(this.getNewPropertyCode());
            if (found) {
                break;
            }
        }
        if (!found) {
            System.out.println("Property Type " + this.getNewPropertyCode() + " not found on " + this.getSampleTypeCode());

            SampleTypeUpdate update = new SampleTypeUpdate();
            update.setTypeId(id);
            ListUpdateValue.ListUpdateActionAdd add = new ListUpdateValue.ListUpdateActionAdd();
            PropertyAssignmentCreation propertyAssignmentCreation = new PropertyAssignmentCreation();
            propertyAssignmentCreation.setOrdinal(1);
            propertyAssignmentCreation.setPropertyTypeId(new PropertyTypePermId(this.getNewPropertyCode()));
            add.setItems(Arrays.asList(propertyAssignmentCreation));
            update.setPropertyAssignmentActions(Arrays.asList(add));
            v3.updateSampleTypes(sessionToken, Arrays.asList(update));
            System.out.println("Property Type " + this.getNewPropertyCode() + " created on " + this.getSampleTypeCode());
        }

        // Copy
        int total = 0;
        SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
        sampleSearchCriteria.withType().withCode().thatEquals(this.getSampleTypeCode());
        SampleFetchOptions sampleFetchOptions = new SampleFetchOptions();
        sampleFetchOptions.withProperties();
        SearchResult<Sample> samples = v3.searchSamples(sessionToken, sampleSearchCriteria, sampleFetchOptions);
        List<SampleUpdate> sampleUpdates = new ArrayList<SampleUpdate>();
        for(Sample sample:samples.getObjects()) {
            if (sample.getProperty(this.getOldPropertyCode()) != null) {
                if(!sample.getProperty(this.getOldPropertyCode()).equals(sample.getProperty(this.getNewPropertyCode()))) {
                    SampleUpdate update = new SampleUpdate();
                    update.setSampleId(sample.getPermId());
                    update.setProperty(this.getNewPropertyCode(), sample.getProperty(this.getOldPropertyCode()));
                    sampleUpdates.add(update);
                    System.out.println("[PREPARING] " + sample.getIdentifier() + "\t" + sample.getProperty(this.getOldPropertyCode()) + "\t" + sampleUpdates.size() + "/" + samples.getTotalCount());
                } else {
                    System.out.println("[SKIP] " + sample.getIdentifier() + "\t" + sample.getProperty(this.getOldPropertyCode()) + "\t" + sampleUpdates.size() + "/" + samples.getTotalCount());
                }
            }
            total++;
        }
        v3.updateSamples(sessionToken, sampleUpdates);
        System.out.println("[DONE] " + total + "/" + samples.getTotalCount());

    }
}
