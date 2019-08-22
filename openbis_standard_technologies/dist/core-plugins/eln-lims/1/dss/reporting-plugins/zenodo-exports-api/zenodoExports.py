#
# Copyright 2016 ETH Zuerich, Scientific IT Services
#
# Licensed under the Apache License, Version 2.0 (the 'License');
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an 'AS IS' BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

from __future__ import print_function

import json
import traceback

import time
from ch.systemsx.cisd.common.logging import LogCategory
from ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id import CustomASServiceCode
from ch.ethz.sis.openbis.generic.asapi.v3.dto.service import CustomASServiceExecutionOptions
from com.sun.xml.internal.ws.policy.privateutil.PolicyUtils import ServiceProvider
from java.io import File
from java.nio.file import Paths
from org.apache.commons.io import FileUtils
from org.apache.log4j import Logger
from org.eclipse.jetty.client import HttpClient
from org.eclipse.jetty.client.util import MultiPartContentProvider
from org.eclipse.jetty.client.util import PathContentProvider
from org.eclipse.jetty.client.util import StringContentProvider
from org.eclipse.jetty.http import HttpMethod
from org.eclipse.jetty.util.ssl import SslContextFactory
from org.json import JSONObject

from exportsApi import findEntitiesToExport, validateDataSize, getConfigurationProperty, generateZipFile, checkResponseStatus, displayResult, cleanUp

operationLog = Logger.getLogger(str(LogCategory.OPERATION) + '.zenodoExports.py')


def process(tr, params, tableBuilder):
    method = params.get('method')

    # Set user using the service
    tr.setUserId(userId)

    if method == 'exportAll':
        resultUrl = expandAndExport(tr, params)
        displayResult(resultUrl is not None, tableBuilder, '{"url": "' + resultUrl + '"}' if resultUrl is not None else None)


def expandAndExport(tr, params):
    entitiesToExport = findEntitiesToExport(params)
    validateDataSize(entitiesToExport, tr)

    operationLog.info('Found ' + str(len(entitiesToExport)) + ' entities to export')
    exportUrl = export(entities=entitiesToExport, tr=tr, params=params)

    # registerPublicationInOpenbis(params=params, exportUrl=exportUrl)

    return exportUrl


def registerPublicationInOpenbis(params, exportUrl):
    sessionToken = params.get('sessionToken')
    v3 = ServiceProvider.getV3ApplicationService()
    id = CustomASServiceCode('publication-api')
    options = CustomASServiceExecutionOptions() \
        .withParameter('method', 'insertPublication') \
        .withParameter('publicationURL', exportUrl)
    # .withParameter('openBISRelatedIdentifiers', ...)
    # .withParameter('publicationOrganization', ...)
    # .withParameter('name', ...)
    # .withParameter('publicationType', ...)
    # .withParameter('publicationIdentifier', ...)
    result = v3.executeCustomASService(sessionToken, id, options)


def export(entities, tr, params):
    #Create temporal folder
    timeNow = time.time()

    exportDirName = 'export_' + str(timeNow)
    exportDir = File.createTempFile(exportDirName, None)
    exportDirPath = exportDir.getCanonicalPath()
    exportDir.delete()
    exportDir.mkdir()

    contentZipFileName = 'content.zip'
    contentDirName = 'content_' + str(timeNow)
    contentDir = File.createTempFile(contentDirName, None, exportDir)
    contentDirPath = contentDir.getCanonicalPath()
    contentDir.delete()
    contentDir.mkdir()

    contentZipFilePath = exportDirPath + '/' + contentZipFileName

    exportZipFilePath = exportDirPath + '.zip'

    generateZipFile(entities, params, contentDirPath, contentZipFilePath)
    FileUtils.forceDelete(File(contentDirPath))

    resultUrl = sendToZenodo(tr=tr, tempZipFilePath=contentZipFilePath)
    FileUtils.forceDelete(File(exportDirPath))
    return resultUrl


def sendToZenodo(tr, tempZipFilePath):
    depositRootUrl = str(getConfigurationProperty(tr, 'zenodoUrl')) + '/api/deposit/depositions'

    accessToken = str(getConfigurationProperty(tr, 'accessToken'))
    operationLog.info('accessToken: %s' % accessToken)

    httpClient = None
    try:
        httpClient = createHttpClient()

        httpClient.setFollowRedirects(False)
        httpClient.start()

        depositionData = createDepositionResource(httpClient.newRequest(depositRootUrl), accessToken)

        depositionLinks = depositionData.get('links')
        depositUrl = depositionLinks.get('files')
        selfUrl = depositionLinks.get('self')
        publishUrl = depositionLinks.get('publish')

        submitFile(httpClient.newRequest(depositUrl), accessToken, tempZipFilePath)
        addMetadata(httpClient.newRequest(selfUrl), accessToken)
        publish(httpClient.newRequest(publishUrl), accessToken)

        retrieve(httpClient.newRequest(selfUrl), accessToken)

        result = depositionLinks.get('html')
        return result
    except Exception as e:
        operationLog.error('Exception at: ' + traceback.format_exc())
        operationLog.error('Exception: ' + str(e))
        raise e
    finally:
        if httpClient is not None:
            httpClient.stop()


def submitFile(request, accessToken, tempZipFilePath):
    multiPart = MultiPartContentProvider()
    multiPart.addFilePart('file', 'content.zip', PathContentProvider(Paths.get(tempZipFilePath)), None)
    multiPart.close()
    addAuthenticationHeader(accessToken, request)
    response = request.method(HttpMethod.POST).content(multiPart).send()
    checkResponseStatus(response)
    contentStr = response.getContentAsString()

    print('submitFile(). contentStr="%s"' % contentStr)

    return JSONObject(contentStr)


def addMetadata(request, accessToken):
    data = {
        'metadata': {
            'title': 'Sample upload',
            'license': 'cc-zero',
            'upload_type': 'poster',
            'description': 'This is a sample upload',
            'creators': [{'name': userId, 'affiliation': 'Zenodo'}]
        }
    }

    addAuthenticationHeader(accessToken, request)
    jsonString = json.dumps(data)
    print('addMetadata(). jsonString="%s"' % jsonString)
    response = request.method(HttpMethod.PUT).content(StringContentProvider(jsonString), 'application/json').send()

    contentStr = response.getContentAsString()
    print('addMetadata(). contentStr="%s"' % contentStr)

    checkResponseStatus(response)


def retrieve(request, accessToken):
    addAuthenticationHeader(accessToken, request)
    response = request.method(HttpMethod.GET).send()

    contentStr = response.getContentAsString()
    print('retrieve(). retrieve="%s"' % contentStr)

    checkResponseStatus(response)


def publish(request, accessToken):
    addAuthenticationHeader(accessToken, request)
    response = request.method(HttpMethod.POST).send()

    contentStr = response.getContentAsString()
    print('publish(). contentStr="%s"' % contentStr)

    checkResponseStatus(response)


def createDepositionResource(request, accessToken):
    addAuthenticationHeader(accessToken, request)
    response = request.method(HttpMethod.POST).content(StringContentProvider('{}'), 'application/json').send()
    checkResponseStatus(response)

    contentStr = response.getContentAsString()
    return JSONObject(contentStr)


def addAuthenticationHeader(accessToken, request):
    request.header('Authorization', 'Bearer ' + accessToken)


def createHttpClient():
    sslContextFactory = SslContextFactory()
    sslContextFactory.setTrustAll(True)
    return HttpClient(sslContextFactory)
