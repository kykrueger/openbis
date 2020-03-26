package ch.systemsx.cisd.openbis.generic.shared.dto;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import ch.systemsx.cisd.openbis.generic.shared.IServer;

@MappedSuperclass
public abstract class EntityPropertyWithSampleDataTypePE extends EntityPropertyPE
{
    private static final long serialVersionUID = IServer.VERSION;

    private SamplePE sample;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.SAMPLE_PROP_COLUMN)
    public SamplePE getSampleValue()
    {
        return sample;
    }

    public void setSampleValue(SamplePE sample)
    {
        this.sample = sample;
    }

}
