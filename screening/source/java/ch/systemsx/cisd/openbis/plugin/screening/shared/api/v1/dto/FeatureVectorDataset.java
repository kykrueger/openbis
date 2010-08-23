package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Feature vectors of one dataset.
 * 
 * @author Tomasz Pylak
 */
public class FeatureVectorDataset implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final FeatureVectorDatasetReference dataset;

    private final List<String> featureNames;
    
    private final List<String> featureCodes;

    private final List<String> featureLabels;
    
    private final List<FeatureVector> featureVectors;

    public FeatureVectorDataset(FeatureVectorDatasetReference dataset, List<String> featureNames,
            List<FeatureVector> featureVectors)
    {
        this(dataset, featureNames, featureNames, featureVectors);
    }

    public FeatureVectorDataset(FeatureVectorDatasetReference dataset, List<String> featureCodes,
            List<String> featureLabels, List<FeatureVector> featureVectors)
    {
        this.dataset = dataset;
        this.featureNames = featureCodes;
        this.featureCodes = featureCodes;
        this.featureLabels = featureLabels;
        this.featureVectors = featureVectors;
    }
    
    /** identifier of the dataset containing feature vectors */
    public FeatureVectorDatasetReference getDataset()
    {
        return dataset;
    }

    /** names of features present in each feature vector */
    @Deprecated
    public List<String> getFeatureNames()
    {
        return featureNames;
    }

    /**
     * Returns the feature codes. If feature codes are unspecified feature names are return. This
     * will be the case if a serialized instance of a previous of this class will be deserialized.
     */
    public List<String> getFeatureCodes()
    {
        return featureCodes == null ? featureNames : featureCodes;
    }

    /**
     * Returns the feature labels. If feature codes are unspecified feature names are return. This
     * will be the case if a serialized instance of a previous of this class will be deserialized.
     */
    public List<String> getFeatureLabels()
    {
        return featureLabels == null ? featureNames : featureLabels;
    }
    
    /** all feature vectors for a dataset */
    public List<FeatureVector> getFeatureVectors()
    {
        return featureVectors;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("datasetCode: " + dataset.getDatasetCode());
        sb.append(", storeUrl: " + dataset.getDatastoreServerUrl());
        sb.append("\n\tfeatures codes: " + getFeatureCodes());
        sb.append("\n\tfeatures labels: " + getFeatureLabels());
        for (int i = 0; i < featureVectors.size(); i++)
        {
            sb.append("\n\t" + featureVectors.get(i));
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || obj instanceof FeatureVectorDataset == false)
        {
            return false;
        }
        FeatureVectorDataset that = (FeatureVectorDataset) obj;
        return dataset.getDatasetCode().equals(that.getDataset().getDatasetCode());
    }

    @Override
    public int hashCode()
    {
        return dataset.getDatasetCode().hashCode();
    }
}