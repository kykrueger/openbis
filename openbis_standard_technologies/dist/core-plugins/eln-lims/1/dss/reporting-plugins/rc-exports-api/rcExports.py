#
# Copyright 2016 ETH Zuerich, Scientific IT Services
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
import threading
import time
import urllib2
import xml.etree.ElementTree as ET
from ch.systemsx.cisd.common.logging import LogCategory
from ch.systemsx.cisd.openbis.dss.generic.shared import ServiceProvider
from java.io import File
from org.apache.log4j import Logger

from exportsApi import generateZipFile, cleanUp, displayResult, findEntitiesToExport, validateDataSize, getConfigurationProperty

operationLog = Logger.getLogger(str(LogCategory.OPERATION) + ".rcExports.py")


def process(tr, params, tableBuilder):
    method = params.get("method")
    isOk = False

    # Set user using the service

    tr.setUserId(userId)
    if method == "exportAll":
        isOk = expandAndExport(tr, params)

    displayResult(isOk, tableBuilder)


def expandAndExport(tr, params):
    #Services used during the export process
    # TO-DO Login on the services as ETL server but on behalf of the user that makes the call
    sessionToken = params.get("sessionToken")
    v3 = ServiceProvider.getV3ApplicationService()

    entitiesToExport = findEntitiesToExport(params)
    validateDataSize(entitiesToExport, tr)

    includeRoot = params.get("includeRoot")

    operationLog.info("Found " + str(len(entitiesToExport)) + " entities to export, export thread will start")
    thread = threading.Thread(target=export, args=(tr, sessionToken, entitiesToExport, includeRoot))
    thread.daemon = True
    thread.start()

    return True


def export(tr, sessionToken, entities, includeRoot):
    #Create temporal folder
    tempDirName = "export_" + str(time.time())
    tempDirPathFile = File.createTempFile(tempDirName, None)
    tempDirPathFile.delete()
    tempDirPathFile.mkdir()
    tempDirPath = tempDirPathFile.getCanonicalPath()
    tempZipFileName = tempDirName + ".zip"
    tempZipFilePath = tempDirPath + ".zip"

    generateZipFile(entities, includeRoot, sessionToken, tempDirPath, tempZipFilePath)

    #Send Email
    sendToDSpace(tr, tempZipFileName, tempZipFilePath)

    cleanUp(tempDirPath, tempZipFilePath)
    return True


def sendToDSpace(tr, tempZipFileName, tempZipFilePath):
    collectionUrls = fetchServiceDocument(tr)
    print('URLs: %s' % collectionUrls)
    print('URLs count: %s' % len(collectionUrls))

def fetchServiceDocument(tr):
    user = getConfigurationProperty(tr, 'user')
    password = getConfigurationProperty(tr, 'password')
    realm = getConfigurationProperty(tr, 'realm')
    url = getConfigurationProperty(tr, 'service-document-url')
    pm = urllib2.HTTPPasswordMgr()
    pm.add_password(realm, url, user, password)
    authHandler = urllib2.HTTPBasicAuthHandler(pm)
    opener = urllib2.build_opener(authHandler)
    urllib2.install_opener(opener)
    response = urllib2.urlopen(url)
    xmlText = response.read()

    xmlRoot = ET.fromstring(xmlText)
    collections = xmlRoot.findall('./xmlns:workspace/xmlns:collection[@href]', namespaces=dict(xmlns='http://www.w3.org/2007/app'))

    collectionUrls = map(lambda collection: collection.attrib['href'], collections)
    return collectionUrls
