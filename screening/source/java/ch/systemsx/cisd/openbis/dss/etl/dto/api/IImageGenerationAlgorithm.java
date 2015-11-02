package ch.systemsx.cisd.openbis.dss.etl.dto.api;

import java.awt.image.BufferedImage;
import java.util.List;

import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSet;
import ch.systemsx.cisd.openbis.dss.etl.IImageProvider;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetInformation;

/**
 * Algorithm for creating a representative thumbnails.
 * 
 * @author Antti Luomi
 */
public interface IImageGenerationAlgorithm
{
    /**
     * Creates thumbnails for the specified data set info.
     */
    public List<BufferedImage> generateImages(ImageDataSetInformation information, 
            List<IDataSet> thumbnailDatasets, IImageProvider imageProvider);
    
    /**
     * Returns the code of the data set to be registered containing these representative thumbnails.
     */
    public String getDataSetTypeCode();
    
    /**
     * Returns the thumbnail file name for the specified index. The index specifies the corresponding
     * image returned by {@link #generateImages(ImageDataSetInformation, List, IImageProvider)}.
     * Note, all file names generated by this method have to be different.
     */
    public String getImageFileName(int index);
}
