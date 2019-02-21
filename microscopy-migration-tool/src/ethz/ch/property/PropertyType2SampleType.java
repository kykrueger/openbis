package ethz.ch.property;

public class PropertyType2SampleType
{
    private String oldSampleType;
    private String oldPropertyCode;
    private String newSampleType;
    private String newPropertyCode;
    
    public PropertyType2SampleType(String oldSampleType, String oldPropertyCode, String newSampleType, String newPropertyCode)
    {
        super();
        this.oldSampleType = oldSampleType;
        this.oldPropertyCode = oldPropertyCode;
        this.newSampleType = newSampleType;
        this.newPropertyCode = newPropertyCode;
    }

    public String getOldSampleType()
    {
        return oldSampleType;
    }

    public String getOldPropertyCode()
    {
        return oldPropertyCode;
    }

    public String getNewSampleType()
    {
        return newSampleType;
    }

    public String getNewPropertyCode()
    {
        return newPropertyCode;
    }
    
}