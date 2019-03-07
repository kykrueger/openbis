package ethz.ch.property;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;

public class Property2Sample
{
    private PropertyType2SampleType config;
    private SamplePermId samplePermId;
    
    
    public Property2Sample(PropertyType2SampleType config, SamplePermId samplePermId)
    {
        super();
        this.config = config;
        this.samplePermId = samplePermId;
    }
    
    public PropertyType2SampleType getConfig()
    {
        return config;
    }

    public SamplePermId getSamplePermId()
    {
        return samplePermId;
    }    
    
}