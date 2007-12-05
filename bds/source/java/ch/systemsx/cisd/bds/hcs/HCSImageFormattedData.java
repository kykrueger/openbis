/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds.hcs;

import java.util.HashSet;
import java.util.Set;

import ch.systemsx.cisd.bds.AbstractFormattedData;
import ch.systemsx.cisd.bds.Constants;
import ch.systemsx.cisd.bds.DataStructureV1_0;
import ch.systemsx.cisd.bds.Format;
import ch.systemsx.cisd.bds.FormattedDataContext;
import ch.systemsx.cisd.bds.IFormatParameters;
import ch.systemsx.cisd.bds.IFormattedData;
import ch.systemsx.cisd.bds.Utilities;
import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.exception.StorageException;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.INode;

/**
 * {@link IFormattedData} implementation for <i>HCS (High-Content Screening) with Images</i>. It is associated with
 * {@link HCSImageFormat1_0}.
 * 
 * @author Christian Ribeaud
 */
public final class HCSImageFormattedData extends AbstractFormattedData implements IHCSImageFormattedData
{

    /** The <i>column</i> (or <i>x</i>) coordinate. */
    public static final String COLUMN = "column";

    /** The <i>row</i> (or <i>y</i>) coordinate. */
    public static final String ROW = "row";

    public HCSImageFormattedData(final FormattedDataContext context)
    {
        super(context);
    }

    private final boolean containsOriginalData()
    {
        return (Boolean) getFormatParameters().getValue(HCSImageFormat1_0.CONTAINS_ORIGINAL_DATA);
    }

    private final Geometry getWellGeometry()
    {
        return (Geometry) getFormatParameters().getValue(WellGeometry.WELL_GEOMETRY);
    }

    private final Geometry getPlateGeometry()
    {
        return (Geometry) getFormatParameters().getValue(PlateGeometry.PLATE_GEOMETRY);
    }

    private final int getChannelCount()
    {
        return getChannelList().getChannelCount();
    }

    private final ChannelList getChannelList()
    {
        return (ChannelList) getFormatParameters().getValue(ChannelList.NUMBER_OF_CHANNELS);
    }

    private final static void checkLocation(final Geometry geometry, final Location location)
    {
        if (geometry.contains(location) == false)
        {
            throw new IllegalArgumentException(String.format("Given geometry '%s' does not contain location '%s'",
                    geometry, location));
        }

    }

    private final void checkChannel(final int channel)
    {
        if (channel < 1)
        {
            throw new IndexOutOfBoundsException(String.format("Channel index starts at 1.", channel));
        }
        final int channelCount = getChannelCount();
        if (channel > channelCount)
        {
            throw new IndexOutOfBoundsException(String.format("%d > %d", channel, channelCount));
        }
    }

    /** From given <var>wellLocation</var> creates the leaf file name that is found in <code>data/standard</code>. */
    final static String createWellFileName(final Location wellLocation)
    {
        assert wellLocation != null : "Well location can not be null.";
        return ROW + wellLocation.y + "_" + COLUMN + wellLocation.x + ".tiff";
    }

    private final IDirectory getStandardDataDirectory()
    {
        return Utilities.getSubDirectory(dataDirectory, DataStructureV1_0.DIR_STANDARD);
    }

    private final IDirectory getOriginalDataDirectory()
    {
        return Utilities.getSubDirectory(dataDirectory, DataStructureV1_0.DIR_ORIGINAL);
    }

    private final static String getPlateColumnDir(final Location plateLocation)
    {
        return COLUMN + plateLocation.x;
    }

    private final static String getPlateRowDirName(final Location plateLocation)
    {
        return ROW + plateLocation.y;
    }

    private final static String getChannelName(final int channel)
    {
        return Channel.CHANNEL + channel;
    }

    //
    // IHCSFormattedData
    //

    public final INode tryGetStandardNodeAt(final int channel, final Location plateLocation, final Location wellLocation)
    {
        checkChannel(channel);
        assert plateLocation != null : "Plate location can not be null.";
        assert wellLocation != null : "Well location can not be null.";
        checkLocation(getPlateGeometry(), plateLocation);
        checkLocation(getWellGeometry(), wellLocation);
        final IDirectory standardDir = getStandardDataDirectory();
        final IDirectory channelDir = Utilities.getSubDirectory(standardDir, getChannelName(channel));
        final IDirectory plateRowDir = Utilities.getSubDirectory(channelDir, getPlateRowDirName(plateLocation));
        final IDirectory plateColumnDir = Utilities.getSubDirectory(plateRowDir, getPlateColumnDir(plateLocation));
        return plateColumnDir.tryGetNode(createWellFileName(wellLocation));
    }

    public final NodePath addStandardNode(final String originalFilePath, int channel, final Location plateLocation,
            final Location wellLocation)
    {
        // This will check all parameters but originalFilePath.
        INode node = null;
        // TODO 2007-12-05, Christian Ribeaud: Improve this.
        try
        {
            node = tryGetStandardNodeAt(channel, plateLocation, wellLocation);
        } catch (DataStructureException ex)
        {
            // Nothing to do here.
        }
        if (node != null)
        {
            throw new DataStructureException(String.format(
                    "A node already exists at channel %d, plate location '%s' and well location '%s'.", channel,
                    plateLocation, wellLocation));
        }
        assert originalFilePath != null : "Given original file name can not be null.";
        final IDirectory standardDir = getStandardDataDirectory();
        final IDirectory channelDir = Utilities.getOrCreateSubDirectory(standardDir, getChannelName(channel));
        final IDirectory plateRowDir = Utilities.getOrCreateSubDirectory(channelDir, getPlateRowDirName(plateLocation));
        final IDirectory plateColumnDir =
                Utilities.getOrCreateSubDirectory(plateRowDir, getPlateColumnDir(plateLocation));
        final String wellFileName = createWellFileName(wellLocation);
        final INode originalNode = getOriginalDataDirectory().tryGetNode(originalFilePath);
        if (originalNode == null)
        {
            throw new DataStructureException(String.format("No original node with name '%s' could be found.",
                    originalFilePath));
        }
        if (containsOriginalData())
        {
            node = plateColumnDir.tryAddLink(wellFileName, originalNode);
        } else
        {
            node = plateColumnDir.tryAddNode(wellFileName, originalNode);
        }
        if (node == null)
        {
            throw new StorageException(
                    String
                            .format(
                                    "Original file name '%s' could not be added at channel %d, plate location '%s' and well location '%s'.",
                                    originalFilePath, channel, plateLocation, wellLocation));
        }
        final char sep = Constants.PATH_SEPARATOR;
        final String standardNodePath =
                channelDir.getName() + sep + plateRowDir.getName() + sep + plateColumnDir.getName() + sep + wellFileName;
        return new NodePath(node, standardNodePath);
    }

    //
    // AbstractFormattedData
    //

    public final Format getFormat()
    {
        return HCSImageFormat1_0.HCS_IMAGE_1_0;
    }

    @Override
    protected final void assertValidFormatAndFormatParameters()
    {
        super.assertValidFormatAndFormatParameters();
        final IFormatParameters formatParameters = getFormatParameters();
        final Set<String> notPresent = new HashSet<String>();
        for (final String formatParameterName : format.getParameterNames())
        {
            if (formatParameters.containsParameter(formatParameterName) == false)
            {
                notPresent.add(formatParameterName);
            }
        }
        if (notPresent.isEmpty() == false)
        {
            throw new DataStructureException(String.format(
                    "Following format parameters '%s' could not be found in the structure.", notPresent));
        }

    }
}
