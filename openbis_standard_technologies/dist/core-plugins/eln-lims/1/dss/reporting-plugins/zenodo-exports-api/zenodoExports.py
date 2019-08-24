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
from ch.systemsx.cisd.openbis.dss.generic.shared import ServiceProvider
from ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id import CustomASServiceCode
from ch.ethz.sis.openbis.generic.asapi.v3.dto.service import CustomASServiceExecutionOptions
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

from ch.ethz.sis import JobScheduler
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

    resultUrl = sendToZenodo(tr=tr, params=params, tempZipFilePath=contentZipFilePath)
    FileUtils.forceDelete(File(exportDirPath))
    return resultUrl


def sendToZenodo(tr, params, tempZipFilePath):
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
        # publish(httpClient.newRequest(publishUrl), accessToken)

        zenodoCallable = ZenodoCallable()
        zenodoCallable.params = params
        zenodoCallable.accessToken = accessToken
        zenodoCallable.selfUrl = selfUrl
        zenodoCallable.scheduleMetadataCheck()

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

    return JSONObject(contentStr)


def addMetadata(request, accessToken):
    data = {
        'metadata': {
            'title': 'Sample upload',
            'license': 'cc-zero',
            'upload_type': 'dataset',
            'description': 'This is a sample upload',
            'creators': [{'name': userId}]
        }
    }

    addAuthenticationHeader(accessToken, request)
    jsonString = json.dumps(data)
    response = request.method(HttpMethod.PUT).content(StringContentProvider(jsonString), 'application/json').send()

    contentStr = response.getContentAsString()

    checkResponseStatus(response)


def retrieve(request, accessToken):
    addAuthenticationHeader(accessToken, request)
    response = request.method(HttpMethod.GET).send()
    contentStr = response.getContentAsString()
    checkResponseStatus(response)

    return JSONObject(contentStr)


def publish(request, accessToken):
    addAuthenticationHeader(accessToken, request)
    response = request.method(HttpMethod.POST).send()
    contentStr = response.getContentAsString()

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


class ZenodoCallable(object):
    params = None
    accessToken = None
    selfUrl = None

    def scheduleMetadataCheck(self):
        JobScheduler.scheduleRepeatedRequest(60000, 60, self.call)

    def call(self):
        httpClient = None
        result = False
        try:
            httpClient = createHttpClient()

            httpClient.setFollowRedirects(False)
            httpClient.start()

            publicationJson = retrieve(httpClient.newRequest(self.selfUrl), self.accessToken)

            if publicationJson.get('submitted'):
                operationLog.info('Publication #%d submitted. Registering metadata.' % publicationJson.get('id'))
                self.registerPublicationInOpenbis(publicationJson.get('metadata'))
                result = True
            else:
                operationLog.info('Publication #%d not submitted yet.' % publicationJson.get('id'))
        except Exception as e:
            operationLog.error('Exception at: ' + traceback.format_exc())
            operationLog.error('Exception: ' + str(e))
            raise e
        finally:
            if httpClient is not None:
                httpClient.stop()

        return result

    def registerPublicationInOpenbis(self, publicationMetadataJson):
        sessionToken = self.params.get('sessionToken')
        v3 = ServiceProvider.getV3ApplicationService()
        id = CustomASServiceCode('publication-api')
        options = CustomASServiceExecutionOptions() \
            .withParameter('method', 'insertPublication') \
            .withParameter('publicationURL', self.selfUrl) \
            .withParameter('openBISRelatedIdentifiers', 'no-id') \
            .withParameter('publicationOrganization', 'Zenodo') \
            .withParameter('name', publicationMetadataJson.get('title')) \
            .withParameter('publicationDescription', publicationMetadataJson.get('description')) \
            .withParameter('publicationType', publicationMetadataJson.get('upload_type')) \
            .withParameter('publicationIdentifier', publicationMetadataJson.get('doi'))
        result = v3.executeCustomASService(sessionToken, id, options)
        return result
