/*
 * Copyright 2010 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess;

import java.util.List;

import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TransactionQuery;
import net.lemnik.eodsql.Update;

import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ByteArrayMapper;

/**
 * @author Tomasz Pylak
 */
public interface IImagingQueryDAO extends TransactionQuery
{
    public static final int FETCH_SIZE = 1000;

    public static final String SQL_IMAGE =
            "select i.* from CHANNEL_STACKS, SPOTS, ACQUIRED_IMAGES, IMAGES as i "
                    + "where                                                                "
                    + "ACQUIRED_IMAGES.CHANNEL_ID = ?{1} and CHANNEL_STACKS.DS_ID = ?{2} and "
                    + "CHANNEL_STACKS.x = ?{3.x} and CHANNEL_STACKS.y = ?{3.y} and "
                    + "SPOTS.x = ?{4.x} and SPOTS.y = ?{4.y} and "
                    // joins
                    + "ACQUIRED_IMAGES.CHANNEL_STACK_ID = CHANNEL_STACKS.ID and "
                    + "CHANNEL_STACKS.SPOT_ID = SPOTS.ID ";

    public static final String SQL_NO_MULTIDIMENTIONAL_DATA_COND =
            " order by CHANNEL_STACKS.T_in_SEC, CHANNEL_STACKS.Z_in_M limit 1";

    /**
     * @return an image for the specified chanel, well and tile. If many images for different
     *         timepoints or depths exist, the first one is returned.
     */
    @Select(SQL_IMAGE + " and ACQUIRED_IMAGES.IMG_ID = i.ID " + SQL_NO_MULTIDIMENTIONAL_DATA_COND)
    public ImgImageDTO tryGetImage(long channelId, long datasetId, Location tileLocation,
            Location wellLocation);

    /**
     * @return a thumbnail for the specified chanel, well and tile. If many images for different
     *         timepoints or depths exist, the first one is returned.
     */
    @Select(SQL_IMAGE + " and ACQUIRED_IMAGES.THUMBNAIL_ID = i.ID "
            + SQL_NO_MULTIDIMENTIONAL_DATA_COND)
    public ImgImageDTO tryGetThumbnail(long channelId, long datasetId, Location tileLocation,
            Location wellLocation);

    /** @return an image for the specified chanel and channel stack or null */
    @Select("select i.* from IMAGES as i "
            + "join ACQUIRED_IMAGES on ACQUIRED_IMAGES.IMG_ID = i.ID "
            + "join CHANNEL_STACKS on ACQUIRED_IMAGES.CHANNEL_STACK_ID = CHANNEL_STACKS.ID "
            + "where                                                                "
            + "ACQUIRED_IMAGES.CHANNEL_ID = ?{1} and CHANNEL_STACKS.ID = ?{2} and "
            + "CHANNEL_STACKS.DS_ID = ?{3}")
    // The condition on dataset equality is just to ensure that the stack channel belongs the right
    // dataset.
    public ImgImageDTO tryGetImage(long channelId, Long channelStackId, long datasetId);

    /** @return a thumbnail for the specified chanel and channel stack or null */
    @Select("select i.* from IMAGES as i "
            + "join ACQUIRED_IMAGES on ACQUIRED_IMAGES.THUMBNAIL_ID = i.ID "
            + "join CHANNEL_STACKS on ACQUIRED_IMAGES.CHANNEL_STACK_ID = CHANNEL_STACKS.ID "
            + "where                                                                "
            + "ACQUIRED_IMAGES.CHANNEL_ID = ?{1} and CHANNEL_STACKS.ID = ?{2} and "
            + "CHANNEL_STACKS.DS_ID = ?{3}")
    // The condition on dataset equality is just to ensure that the stack channel belongs the right
    // dataset.
    public ImgImageDTO tryGetThumbnail(long channelId, Long channelStackId, long datasetId);

    // simple getters

    @Select("select ID from EXPERIMENTS where PERM_ID = ?{1}")
    public Long tryGetExperimentIdByPermId(String experimentPermId);

    @Select("select ID from CONTAINERS where PERM_ID = ?{1}")
    public Long tryGetContainerIdPermId(String containerPermId);

    @Select("select * from DATA_SETS where PERM_ID = ?{1}")
    public ImgDatasetDTO tryGetDatasetByPermId(String datasetPermId);

    @Select("select * from CONTAINERS where ID = ?{1}")
    public ImgContainerDTO getContainerById(long containerId);

    // join with container is needed to use spots index
    @Select("select cs.* from CHANNEL_STACKS cs               "
            + "join SPOTS s on s.id = cs.spot_id              "
            + "join CONTAINERS c on c.id = s.cont_id          "
            + "where cs.ds_id = ?{1} and s.x = ?{2} and s.y = ?{3}")
    public List<ImgChannelStackDTO> listChannelStacks(long datasetId, int spotX, int spotY);

    @Select("select count(*) from CHANNELS where DS_ID = ?{1} or EXP_ID = ?{2}")
    public int countChannelByDatasetIdOrExperimentId(long datasetId, long experimentId);

    @Select("select name from CHANNELS where DS_ID = ?{1} or EXP_ID = ?{2} order by NAME")
    public String[] getChannelNamesByDatasetIdOrExperimentId(long datasetId, long experimentId);

    @Select(sql = "select id from CHANNELS where DS_ID = ?{1} or EXP_ID = ?{2} order by NAME", fetchSize = FETCH_SIZE)
    public long[] getChannelIdsByDatasetIdOrExperimentId(long datasetId, long experimentId);

    @Select(sql = "select * from CHANNELS where EXP_ID = ?{1} order by name", fetchSize = FETCH_SIZE)
    public List<ImgChannelDTO> getChannelsByExperimentId(long experimentId);

    @Select("select * from SPOTS where cont_id = ?{1}")
    public List<ImgSpotDTO> listSpots(long contId);

    @Select("select * from FEATURE_DEFS where DS_ID = ?{1}")
    public List<ImgFeatureDefDTO> listFeatureDefsByDataSetId(long dataSetId);

    @Select(sql = "select * from FEATURE_VALUES where FD_ID = ?{1.id} order by T_in_SEC, Z_in_M", resultSetBinding = FeatureVectorDataObjectBinding.class)
    public List<ImgFeatureValuesDTO> getFeatureValues(ImgFeatureDefDTO featureDef);

    // generate ids

    @Select("select nextval('images_id_seq')")
    public long createImageId();

    @Select("select nextval('channel_stacks_id_seq')")
    public long createChannelStackId();

    // batch updates

    @Update(sql = "insert into CHANNEL_STACKS (ID, X, Y, Z_in_M, T_in_SEC, DS_ID, SPOT_ID) values "
            + "(?{1.id}, ?{1.column}, ?{1.row}, ?{1.z}, ?{1.t}, ?{1.datasetId}, ?{1.spotId})", batchUpdate = true)
    public void addChannelStacks(List<ImgChannelStackDTO> channelStacks);

    @Update(sql = "insert into IMAGES (ID, PATH, PAGE, COLOR) values "
            + "(?{1.id}, ?{1.filePath}, ?{1.page}, ?{1.colorComponentAsString})", batchUpdate = true)
    public void addImages(List<ImgImageDTO> images);

    @Update(sql = "insert into ACQUIRED_IMAGES (IMG_ID, THUMBNAIL_ID, CHANNEL_STACK_ID, CHANNEL_ID) values "
            + "(?{1.imageId}, ?{1.thumbnailId}, ?{1.channelStackId}, ?{1.channelId})", batchUpdate = true)
    public void addAcquiredImages(List<ImgAcquiredImageDTO> acquiredImages);

    // inserts

    @Select("insert into EXPERIMENTS (PERM_ID) values (?{1}) returning ID")
    public long addExperiment(String experimentPermId);

    @Select("insert into CHANNELS (NAME, DESCRIPTION, WAVELENGTH, DS_ID, EXP_ID) values "
            + "(?{1.name}, ?{1.description}, ?{1.wavelength}, ?{1.datasetId}, ?{1.experimentId}) returning ID")
    public long addChannel(ImgChannelDTO channel);

    @Select("insert into CONTAINERS (PERM_ID, SPOTS_WIDTH, SPOTS_HEIGHT, EXPE_ID) values "
            + "(?{1.permId}, ?{1.numberOfColumns}, ?{1.numberOfRows}, ?{1.experimentId}) returning ID")
    public long addContainer(ImgContainerDTO container);

    @Select("insert into DATA_SETS (PERM_ID, FIELDS_WIDTH, FIELDS_HEIGHT, CONT_ID, IS_MULTIDIMENSIONAL) values "
            + "(?{1.permId}, ?{1.fieldNumberOfColumns}, "
            + "?{1.fieldNumberOfRows}, ?{1.containerId}, ?{1.isMultidimensional}) returning ID")
    public long addDataset(ImgDatasetDTO dataset);

    @Select("insert into SPOTS (X, Y, CONT_ID) values "
            + "(?{1.column}, ?{1.row}, ?{1.containerId}) returning ID")
    public long addSpot(ImgSpotDTO spot);

    @Select("insert into FEATURE_DEFS (NAME, CODE, DESCRIPTION, DS_ID) values "
            + "(?{1.name}, ?{1.code}, ?{1.description}, ?{1.dataSetId}) RETURNING ID")
    public long addFeatureDef(ImgFeatureDefDTO featureDef);

    @Select(sql = "insert into FEATURE_VALUES (VALUES, Z_in_M, T_in_SEC, FD_ID) values "
            + "(?{1.byteArray}, ?{1.z}, ?{1.t}, ?{1.featureDefId}) RETURNING ID", parameterBindings =
        { ByteArrayMapper.class })
    public long addFeatureValues(ImgFeatureValuesDTO featureValues);

    // updates

    @Update("update CHANNELS "
            + "set DESCRIPTION = ?{1.description}, WAVELENGTH = ?{1.wavelength} "
            + "where ID = ?{1.id}")
    public void updateChannel(ImgChannelDTO channel);

    @Select("select ID from CHANNELS where (DS_ID = ?{1} or EXP_ID = ?{2}) and NAME = upper(?{3})")
    public Long tryGetChannelIdByChannelNameDatasetIdOrExperimentId(long id, long experimentId,
            String chosenChannel);

}
