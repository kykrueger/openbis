package ethz.ch.experiment;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;

public class Experiment2Sample
{
    private ExperimentType2SampleType config;
    private ExperimentPermId experimentPermId;
    
    
    public Experiment2Sample(ExperimentType2SampleType config, ExperimentPermId experimentPermId)
    {
        super();
        this.config = config;
        this.experimentPermId = experimentPermId;
    }
    
    public ExperimentType2SampleType getConfig()
    {
        return config;
    }

    public ExperimentPermId getExperimentPermId()
    {
        return experimentPermId;
    }
    
}
