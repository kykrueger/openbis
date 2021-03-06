"""
Created on Feb 20, 2014

@author: Aaron Ponti
"""

import re
from ch.systemsx.cisd.openbis.dss.etl.dto.api import SimpleImageDataConfig
from ch.systemsx.cisd.openbis.dss.etl.dto.api import SimpleImageContainerDataConfig
from ch.systemsx.cisd.openbis.dss.etl.dto.api import ChannelColor
from ch.systemsx.cisd.openbis.dss.etl.dto.api import ImageIdentifier
from ch.systemsx.cisd.openbis.dss.etl.dto.api import ImageMetadata
from ch.systemsx.cisd.openbis.dss.etl.dto.api import OriginalDataStorageFormat
from ch.systemsx.cisd.openbis.dss.etl.dto.api import ChannelColorRGB
from ch.systemsx.cisd.openbis.dss.etl.dto.api import Channel
from ch.systemsx.cisd.openbis.dss.etl.dto.api.impl import MaximumIntensityProjectionGenerationAlgorithm;

class TestAlgorithm(MaximumIntensityProjectionGenerationAlgorithm):
    def __init__(self):
        MaximumIntensityProjectionGenerationAlgorithm.__init__(self, 'MICROSCOPY_REPRESENTATIVE_IMG')
    def imageToBeIgnored(self, imageFileInfo):
        return False

class MicroscopySingleDatasetConfig(SimpleImageContainerDataConfig):
    """Image data configuration class for multi datasets image files."""

    # MetadataReader used to extract relevant metadata information using
    # the LOCI bio-formats library.
    _metadataReader = None

    # Number of the series to register (for a multi-series dataset.
    _seriesNum = 0

    def __init__(self, metadataReader, seriesNum=0):
        """Constructor.

        @param metadataReader: MetadataReader object (with extracted metadata)
        @param seriesNum: Int Number of the series to register. All other series in
                   the file will be ignored.
        """

        # Store the MetadataReader
        self._metadataReader = metadataReader

        # Store the series number
        self._seriesNum = seriesNum

        # This is microscopy data
        self.setMicroscopyData(True)

        # Store raw data in original form
        self.setOriginalDataStorageFormat(OriginalDataStorageFormat.UNCHANGED)

        # Set the image library
        self.setImageLibrary("BioFormats")

        # Disable thumbnail generation by ImageMagick
        self.setUseImageMagicToGenerateThumbnails(False)
        
        # Enable thumbnail generation
        self.setGenerateThumbnails(True)

        # Set the recognized extensions
        self.setRecognizedImageExtensions(['lsm', 'stk', 'lif', 'nd2'])

        # Set the dataset type
        self.setDataSetType("MICROSCOPY_IMG")
        self.setImageGenerationAlgorithm(TestAlgorithm())

    def createChannel(self, channelCode):
        """Create a channel from the channelCode with the name as read from
        the file via the MetadataReader.

        @param channelCode Code of the channel as generated by extractImagesMetadata().
        """

        # Get the indices of series and channel from the channel code
        (seriesIndx, channelIndx) = self._getSeriesAndChannelNumbers(channelCode)

        if seriesIndx != self._seriesNum:
            return

        # Try extracting the channel colors for the given series
        try:
            channelNames = self._metadataReader.getMetadata()[seriesIndx]['channelNames']
        except IndexError:
            raise("Could not channel name for series " + str(seriesIndx) + " from MetadataReader.")

        # Try extracting the name
        try:
            name = channelNames[channelIndx]
        except:
            raise("Could not extract name with index " + channelIndx)

        # In case no name was found, assign default name
        if name == "":
            name = "No name"

        # Return the color
        return Channel(channelCode, name, self._getChannelColorRGB(channelCode))


    def extractImagesMetadata(self, imagePath, imageIdentifiers):
        """Overrides extractImagesMetadata method making sure to store
        both series and channel indices in the channel code to be reused
        later to extract color information and other metadata.

        The channel code is in the form SERIES-(\d+)_CHANNEL-(\d+).

        Only metadata for the relevant series number is returned!

        @param imagePath Full path to the file to process
        @param imageIdentifiers Array of ImageIdentifier's

        @see constructor.
        """

        # Initialize array of metadata entries
        metaData = []

        # Iterate over all image identifiers
        for id in imageIdentifiers:

            # Extract the info from the image identifier
            ch = id.colorChannelIndex
            plane = id.focalPlaneIndex
            series = id.seriesIndex
            timepoint = id.timeSeriesIndex

            # Make sure to process only the relevant series
            if series != self._seriesNum:
                continue

            # Build the channel code
            channelCode = "SERIES-" + str(series) + "_CHANNEL-" + str(ch)

            # Initialize a new ImageMetadata object
            imageMetadata = ImageMetadata();

            # Fill in all information
            imageMetadata.imageIdentifier = id
            imageMetadata.seriesNumber = series
            imageMetadata.timepoint = timepoint
            imageMetadata.depth = plane
            imageMetadata.channelCode = channelCode
            imageMetadata.well = "IGNORED"
            imageMetadata.tileNumber = 1

            # Append metadata for current image
            metaData.append(imageMetadata)
        return metaData


    def _getChannelColorRGB(self, channelCode):
        """Returns a ChannelColorRGB instantiated with the RGB color components
        extracted from the file by the MetadataReader.

        @param channelCode Code of the channel as generated by extractImagesMetadata().
        """

        # Get the indices of series and channel from the channel code
        (seriesIndx, channelIndx) = self._getSeriesAndChannelNumbers(channelCode)

        if seriesIndx != self._seriesNum:
            return

        # Try extracting the channel colors for the given series
        try:
            channelColors = \
            self._metadataReader.getMetadata()[seriesIndx]['channelColors']
        except IndexError:
            raise("Could not extract channel colors for series " + str(seriesIndx) + " from MetadataReader.")

        # Try extracting the color
        try:
            color = channelColors[channelIndx]
            R = color[0]
            G = color[1]
            B = color[2]
        except:
            raise("Could not extract color with index " + channelIndx)

        return ChannelColorRGB(R, G, B)


    def _getSeriesAndChannelNumbers(self, channelCode):
        """Extract series and channel number from channel code in
        the form SERIES-(\d+)_CHANNEL-(\d+) to a tuple
        (seriesIndx, channelIndx).

        @param channelCode Code of the channel as generated by extractImagesMetadata().
        """

        # Get the indices of series and channel from the channel code
        p = re.compile("SERIES-(\d+)_CHANNEL-(\d+)")
        m = p.match(channelCode)
        if m is None or len(m.groups()) != 2:
            raise Exception("Could not extract series and channel number!")

        # Now assign the indices
        seriesIndx = int(m.group(1))
        channelIndx = int(m.group(2))

        # Return them
        return seriesIndx, channelIndx
