package ethz.ch.experiment;

import ethz.ch.sample.SamplePropertyDelete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExperimentType2SampleType {
    private final String typeCode;
    private final String experimentToMigrateTo;
    private final Map<String, String> propertyTypesFromTo = new HashMap<String, String>();
    private final List<String> propertyTypesToDeleteAfterMigration = new ArrayList<String>();
    
    public ExperimentType2SampleType(String typeCode, String experimentToMigrateTo, String nameProperty) {
        this.typeCode = typeCode;
        this.experimentToMigrateTo = experimentToMigrateTo;
        this.propertyTypesFromTo.put(nameProperty, "$NAME");
        this.propertyTypesToDeleteAfterMigration.add(nameProperty);
    }

    public String getTypeCode()
    {
        return typeCode;
    }

    public String getExperimentToMigrateTo()
    {
        return experimentToMigrateTo;
    }

    public Map<String, String> getPropertyTypesFromTo()
    {
        return propertyTypesFromTo;
    }

    public SamplePropertyDelete getSamplePropertyDelete() {
        return new SamplePropertyDelete(typeCode, propertyTypesToDeleteAfterMigration.get(0), true, true);
    }
}