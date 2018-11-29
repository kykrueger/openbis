#!/usr/bin/python
# encoding=utf8
import os
import re
import shutil
import time
import settings
import systemtest.testcase
import systemtest.util as util
import urllib, urllib2
import ssl, base64
import json

from urllib2 import Request

from functools import wraps

from systemtest.testcase import TEST_DATA
from systemtest.testcase import OPENBIS_STANDARD_TECHNOLOGIES_PROJECT
from systemtest.util import printAndFlush


# Had to add the ssl wrap thing below because of a problem during the auth call
def sslwrap(func):

    @wraps(func)
    def bar(*args, **kw):
        kw['ssl_version'] = ssl.PROTOCOL_TLSv1
        return func(*args, **kw)

    return bar


ssl.wrap_socket = sslwrap(ssl.wrap_socket)

ALL_TECHNOLOGIES = ['screening', 'proteomics', 'illumina-ngs', 'eln-lims', 'microscopy', 'flow']
DATA_SOURCE_AS_PORT = '9000'
DATA_SOURCE_DSS_PORT = '9001'
HARVESTER_AS_PORT = '9002'
HARVESTER_DSS_PORT = '9003'


class TestCase(systemtest.testcase.TestCase):
    def execute(self):
        openbis_data_source = self._setupOpenbisDataSource()
        openbis_data_source.allUp()
        util.copyFromTo()

        openbis_harvester = self._setupOpenbisHarvester()
        openbis_harvester.allUp()

        self._waitUntilSyncIsFinished(openbis_harvester)
        
        self._checkData(openbis_data_source, openbis_harvester)

    def executeInDevMode(self):
        openbis_data_source = self.createOpenbisController('data_source', port=DATA_SOURCE_AS_PORT, dropDatabases=False)
        openbis_harvester = self.createOpenbisController('harvester', port=HARVESTER_AS_PORT, dropDatabases=False)
        self._checkData(openbis_data_source, openbis_harvester)
        
        big_file = self.artifactRepository.getPathToArtifact(OPENBIS_STANDARD_TECHNOLOGIES_PROJECT, 'openBIS-installation')
        big_file_folder, big_file_name = os.path.split(big_file)
        incoming_stage = "%s/data/incoming_stage" % openbis_data_source.installPath
        shutil.rmtree(incoming_stage)
        os.makedirs(incoming_stage)
        util.copyFromTo(big_file_folder, incoming_stage, big_file_name)
        os.rename("%s/%s" % (incoming_stage, big_file_name), 
                  "%s/data/incoming-default/%s" % (openbis_data_source.installPath, big_file_name))
#        openbis_data_source.waitUntilDataSetRegistrationFinished(1)

    def _checkData(self, openbis_data_source, openbis_harvester):
        material_types = openbis_data_source.queryDatabase('openbis', 'select code, description from material_types order by code')
        self.assertEquals("Material types on data source", 
                          [['COMPOUND', 'Compound'], ['CONTROL', 'Control of a control layout'], 
                           ['GEN-RELATION', 'links to genes'], ['GENE', 'Gene'], ['SIRNA', 'Oligo nucleotide']], 
                          material_types)
        material_types = openbis_harvester.queryDatabase('openbis', 
                "select code, description from material_types where code like 'DS1_%' order by code")
        self.assertEquals("Synched material types on harvester", 
                          [['DS1_COMPOUND', 'Compound'], ['DS1_CONTROL', 'Control of a control layout'], 
                           ['DS1_GEN-RELATION', 'links to genes'], ['DS1_GENE', 'Gene'], ['DS1_SIRNA', 'Oligo nucleotide']], 
                          material_types)
        self._compareDataBases("Attachments", openbis_data_source, openbis_harvester, "openbis", 
                               "select e.code as experiment, p.code as project, s.code as sample, "
                               + "a.file_name, a.version, a.title, a.description, length(value), md5(value) "
                               + "from attachments a join attachment_contents c on a.exac_id = c.id "
                               + "left join experiments e on a.expe_id = e.id "
                               + "left join projects p on a.proj_id = p.id "
                               + "left join samples s on a.samp_id=s.id order by a.file_name, a.version")

    def _compareDataBases(self, name, openbis_data_source, openbis_harvester, databaseType, sql):
        expectedContent = openbis_data_source.queryDatabase(databaseType, sql)
        synchedContent = openbis_harvester.queryDatabase(databaseType, sql)
        self.assertEquals(name, expectedContent, synchedContent)

    def _setupOpenbisDataSource(self):
        self.installOpenbis(instanceName='data_source', technologies=ALL_TECHNOLOGIES)
        openbis_data_source = self.createOpenbisController('data_source', port=DATA_SOURCE_AS_PORT)
        openbis_data_source.setDataStoreServerPort(DATA_SOURCE_DSS_PORT)
        openbis_data_source.setOpenbisPortDataStoreServer(DATA_SOURCE_AS_PORT)
        openbis_data_source.enableProjectSamples()
        openbis_data_source.setDummyAuthentication()
        openbis_data_source.setDataStoreServerProperty("host-address", "https://localhost")
        openbis_data_source.dssProperties['database.kind'] = openbis_data_source.databaseKind
        openbis_data_source.createTestDatabase('openbis')
        openbis_data_source.createTestDatabase('pathinfo')
        openbis_data_source.createTestDatabase('imaging')
        openbis_data_source.createTestDatabase('proteomics')
        openbis_data_source.enableCorePlugin('openbis-sync')
        return openbis_data_source

    def _setupOpenbisHarvester(self):
        self.installOpenbis(instanceName='harvester', technologies=ALL_TECHNOLOGIES)
        openbis_harvester = self.createOpenbisController('harvester', port=HARVESTER_AS_PORT)
        openbis_harvester.setDataStoreServerPort(HARVESTER_DSS_PORT)
        openbis_harvester.setOpenbisPortDataStoreServer(HARVESTER_AS_PORT)
        openbis_harvester.enableProjectSamples()
        openbis_harvester.setDummyAuthentication()
        openbis_harvester.setDataStoreServerProperty("host-address", "https://localhost")
        openbis_harvester.dssProperties['database.kind'] = openbis_harvester.databaseKind
        openbis_harvester.enableCorePlugin("openbis-sync")
        util.copyFromTo(self.getTemplatesFolder(), openbis_harvester.installPath, "harvester-config.txt")
        return openbis_harvester

    def _waitUntilSyncIsFinished(self, openbis_harvester):
        monitor = util.LogMonitor("%s synchronization.log" % openbis_harvester.instanceName, "%s/synchronization.log" % openbis_harvester.installPath, timeOutInMinutes=30)
        monitor.addNotificationCondition(util.RegexCondition('OPERATION.EntitySynchronizer'))
        monitor.waitUntilEvent(util.RegexCondition('OPERATION.EntitySynchronizer.DS1 - Saving the timestamp of sync start to file'))


TestCase(settings, __file__).runTest()
