#! /usr/bin/env python

import commonImageDropbox
import commonDropbox

#reload(commonDropbox)
#reload(commonImageDropbox)

# Global variable storing AbstractPropertiesParser
datasetMetadataParser = None

def rollback_service(service, throwable):
    global datasetMetadataParser
    commonDropbox.createFailureStatus(datasetMetadataParser, throwable, incoming)

def register(incomingPath):
    global datasetMetadataParser
    datasetMetadataParser = commonDropbox.DerivedDatasetMetadataParser(incomingPath)
    iBrain2DatasetId = datasetMetadataParser.getIBrain2DatasetId()
    openbisDatasetParentPermId = datasetMetadataParser.getParentDatasetPermId()

    tr = service.transaction(incoming, factory)
    (space, plate) = commonDropbox.tryGetConnectedPlate(state, openbisDatasetParentPermId, iBrain2DatasetId, incomingPath)
    if plate == None:
        return
    
    imageDataset = commonImageDropbox.IBrain2ImageDataSetConfig()
    imageDataset.setOverviewImageDatasetType()
    commonDropbox.ensureOrDieNoChildrenOfType(openbisDatasetParentPermId, imageDataset.getDataSetType(), incomingPath, tr)

    imageDataset.setPlate(space, plate)
    imageDataset.setFileFormatType("JPG")
    imageDataset.setRecognizedImageExtensions(["jpg", "jpeg", "png", "gif"])    
    imageDataset.setStoreChannelsOnExperimentLevel(False)
    imageDataset.setGenerateThumbnails(True)
    imageDataset.setMaxThumbnailWidthAndHeight(imageDataset.THUMBANAIL_SIZE)

    commonDropbox.setImageDatasetPropertiesAndRegister(imageDataset, datasetMetadataParser, incoming, service, factory, tr)

if incoming.isDirectory():
    register(incoming.getPath())

