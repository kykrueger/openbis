package ethz.ch.property;

public class PropertyType2SampleType
{
    private String oldSampleTypeCode;
    private String oldPropertyCode;
    private String newSampleTypeCode;
    private String newPropertyCode;
    
    public PropertyType2SampleType(String oldSampleType, String oldPropertyCode, String newSampleType, String newPropertyCode)
    {
        super();
        this.oldSampleTypeCode = oldSampleType;
        this.oldPropertyCode = oldPropertyCode;
        this.newSampleTypeCode = newSampleType;
        this.newPropertyCode = newPropertyCode;
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
    
}