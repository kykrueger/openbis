package ethz.ch.property;

import ethz.ch.sample.SamplePropertyDelete;

public class PropertyType2SampleType
{
    private String oldSampleTypeCode;
    private String oldPropertyCode;
    private String newSampleTypeCode;
    private String newPropertyCode;
    private Boolean oldPropertyUnassign;
    private Boolean oldPropertyDelete;

    public PropertyType2SampleType(String oldSampleType, String oldPropertyCode, String newSampleType, String newPropertyCode, Boolean oldPropertyUnassign, Boolean oldPropertyDelete)
    {
        super();
        this.oldSampleTypeCode = oldSampleType;
        this.oldPropertyCode = oldPropertyCode;
        this.newSampleTypeCode = newSampleType;
        this.newPropertyCode = newPropertyCode;
        this.oldPropertyUnassign = oldPropertyUnassign;
        this.oldPropertyDelete = oldPropertyDelete;
    }

    public String getOldSampleTypeCode()
    {
        return oldSampleTypeCode;
    }

    public String getOldPropertyCode()
    {
        return oldPropertyCode;
    }

    public String getNewSampleTypeCode()
    {
        return newSampleTypeCode;
    }

    public String getNewPropertyCode()
    {
        return newPropertyCode;
    }

    public EntityPropertyDelete getEntityPropertyDelete() {
        return new SamplePropertyDelete(oldSampleTypeCode, oldPropertyCode, oldPropertyUnassign, oldPropertyDelete);
    }
    
}