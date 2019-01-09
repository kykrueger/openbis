#!/usr/bin/python
# encoding=utf8
import os
import re
import shutil
import time
import settings
import systemtest.testcase
import systemtest.util as util
import ssl, base64
import json
from random import randint

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

#ALL_TECHNOLOGIES = ['screening', 'proteomics', 'illumina-ngs', 'eln-lims', 'microscopy', 'flow']
ALL_TECHNOLOGIES = ['screening', 'proteomics', 'illumina-ngs', 'microscopy', 'flow']
DATA_SOURCE_AS_PORT = '9000'
DATA_SOURCE_DSS_PORT = '9001'
HARVESTER_AS_PORT = '9002'
HARVESTER_DSS_PORT = '9003'
TYPE_PREFIX = 'DS1_'


class TestCase(systemtest.testcase.TestCase):

    def execute(self):
        openbis_data_source = self._setupOpenbisDataSource()
        openbis_data_source.allUp()
        self._drop_test_examples(openbis_data_source)

        openbis_harvester = self._setupOpenbisHarvester()
        openbis_harvester.allUp()

        self._waitUntilSyncIsFinished(openbis_harvester)
        
        self._checkData(openbis_data_source, openbis_harvester)

    def executeInDevMode(self):
        openbis_data_source = self.createOpenbisController('data_source', port=DATA_SOURCE_AS_PORT, dropDatabases=False)
#        self._drop_test_examples(openbis_data_source)
        openbis_harvester = self.createOpenbisController('harvester', port=HARVESTER_AS_PORT, dropDatabases=False)
#        openbis_harvester = self._setupOpenbisHarvester()
#        openbis_harvester.allUp()
        
#        self._waitUntilSyncIsFinished(openbis_harvester)
        self._checkData(openbis_data_source, openbis_harvester)

    def _drop_test_examples(self, openbis):
        incoming = "%s/data/incoming-test" % openbis.installPath
        if not os.path.exists(incoming):
            os.makedirs(incoming)
        example_files = []
        example_files.append(self._drop_big_file(incoming))
        example_files.append(self._drop_big_folder_with_many_files(incoming))
        for example_file in example_files:
            with open("%s/.MARKER_is_finished_%s" % (incoming, example_file), 'w') as f:
                pass
        openbis.waitUntilDataSetRegistrationFinished(2, timeOutInMinutes = 10)
        
    def _drop_big_file(self, incoming):
        big_file = self.artifactRepository.getPathToArtifact(OPENBIS_STANDARD_TECHNOLOGIES_PROJECT, 'openBIS-installation')
        big_file_folder, big_file_name = os.path.split(big_file)
        util.copyFromTo(big_file_folder, incoming, big_file_name)
        return big_file_name

    def _drop_big_folder_with_many_files(self, incoming):
        next_words = self._get_next_words_dictionary('Pride_and_Prejudice.txt')
        number_of_text_files = 20000
        minimum_size = 50000
        texts_folder = "%s/%s" % (incoming, "texts")
        if not os.path.exists(texts_folder):
            os.makedirs(texts_folder)
        for i in range(number_of_text_files):
            with open("%s/text-%05d.txt" % (texts_folder, i + 1), 'w') as f:
                f.write(self._create_random_text(next_words, minimum_size))
        util.printAndFlush("%s text files (minimum size %s each) created in %s" % (number_of_text_files, minimum_size, texts_folder))
        return "texts"

    def _create_random_text(self, next_words, minimum_size):
        text = ''
        word = self._choose_random_word(next_words)
        line = word
        while len(text) < minimum_size:
            word = self._choose_next_word(next_words, word)
            if len(line) > 60:
                text += '\n' + line
                line = word
            else:
                line += ' ' + word
        return text + '\n'

    def _choose_random_word(self, next_words):
        keys = list(next_words)
        return keys[randint(0, len(keys) - 1)]

    def _choose_next_word(self, next_words, word):
        if word in next_words:
            words = next_words[word]
            return words[randint(0, len(words) - 1)]
        return word

    def _get_next_words_dictionary(self, text_file):
        next_words = {}
        previous_word = None
        with open("%s/%s" % (self.getTemplatesFolder(), text_file), 'r') as f:
            for line in f:
                for word in line.split():
                    if previous_word is not None:
                        words = []
                        if previous_word not in next_words:
                            next_words[previous_word] = words
                        else:
                            words = next_words[previous_word]
                        words.append(word)
                    previous_word = word
        return next_words
    
    def _checkData(self, openbis_data_source, openbis_harvester):
        self._compareDataBases("Vocabularies", openbis_data_source, openbis_harvester, "openbis",
                               "select '{0}' || code as code, description, source_uri, is_managed_internally, "
                               + "  is_internal_namespace, is_chosen_from_list "
                               + "from controlled_vocabularies where code like '{1}%' order by code")
        self._compareDataBases("Property types", openbis_data_source, openbis_harvester, "openbis", 
                               "select '{0}' || t.code as code,dt.code as data_type, '{0}' || v.code as vocabulary, "
                               + "  '{0}' || mt.code as material, t.label, t.description, "
                               + "  t.is_managed_internally, t.is_internal_namespace, t.schema, t.transformation "
                               + "from property_types t join data_types dt on t.daty_id = dt.id "
                               + "left join controlled_vocabularies v on t.covo_id = v.id "
                               + "left join material_types mt on t.maty_prop_id = mt.id "
                               + "where t.code like '{1}%' order by t.code")
        self._compareDataBases("Material types", openbis_data_source, openbis_harvester, "openbis", 
                               "select '{0}' || t.code as code, t.description, '{0}' || s.name as validation_script "
                               + "from material_types t left join scripts s on t.validation_script_id = s.id "
                               + "where t.code like '{1}%' order by t.code")
        self._compareDataBases("Experiment types", openbis_data_source, openbis_harvester, "openbis", 
                               "select '{0}' || t.code as code, t.description, '{0}' || s.name as validation_script "
                               + "from experiment_types t left join scripts s on t.validation_script_id = s.id "
                               + "where t.code like '{1}%' order by t.code")
        self._compareDataBases("Sample types", openbis_data_source, openbis_harvester, "openbis", 
                               "select '{0}' || code as code, t.description, is_listable, generated_from_depth, part_of_depth "
                               + "  is_auto_generated_code, generated_code_prefix, is_subcode_unique, inherit_properties, "
                               + "  show_parent_metadata, '{0}' || s.name as validation_script "
                               + "from sample_types t left join scripts s on t.validation_script_id = s.id "
                               + "where code like '{1}%' order by code")
        self._compareDataBases("Data set types", openbis_data_source, openbis_harvester, "openbis", 
                               "select '{0}' || code as code, t.description, main_ds_pattern, main_ds_path, "
                               + "  deletion_disallow, '{0}' || s.name as validation_script "
                               + "from data_set_types t left join scripts s on t.validation_script_id = s.id "
                               + "where code like '{1}%' order by code")
        self._compareDataBases("Plugins", openbis_data_source, openbis_harvester, "openbis", 
                               "select '{0}' || name as name, description, script_type, plugin_type, entity_kind, is_available, "
                               + "  length(script) as script_length, md5(script) as script_hash "
                               + "from scripts where name like '{1}%' order by name")
        self._compareDataBases("Material properties", openbis_data_source, openbis_harvester, "openbis", 
                               "select '{0}' || m.code as material, '{0}' || t.code as type, '{0}' || pt.code as property, "
                               + "  concat(mp.value, cvt.code, '{0}' || m2.code) as value "
                               + "from materials m join material_properties mp on mp.mate_id = m.id "
                               + "left join controlled_vocabulary_terms cvt on mp.cvte_id = cvt.id "
                               + "left join materials m2 on mp.mate_prop_id = m2.id "
                               + "join material_type_property_types etpt on mp.mtpt_id = etpt.id "
                               + "join material_types t on etpt.maty_id = t.id "
                               + "join property_types pt on etpt.prty_id = pt.id "
                               + "order by m.code, pt.code")
        self._compareDataBases("Experiment properties", openbis_data_source, openbis_harvester, "openbis", 
                               "select e.code as experiment, '{0}' || t.code as type, '{0}' || pt.code as property, "
                               + "  concat(ep.value, cvt.code, m.code) as value "
                               + "from experiments e join experiment_properties ep on ep.expe_id = e.id "
                               + "left join controlled_vocabulary_terms cvt on ep.cvte_id = cvt.id "
                               + "left join materials m on ep.mate_prop_id = m.id "
                               + "join experiment_type_property_types etpt on ep.etpt_id = etpt.id "
                               + "join experiment_types t on etpt.exty_id = t.id "
                               + "join property_types pt on etpt.prty_id = pt.id "
                               + "order by e.code, pt.code")
        self._compareDataBases("Number of samples per experiment", openbis_data_source, openbis_harvester, "openbis",
                               "select p.code as project, e.code as experiment, count(*) as number_of_samples "
                               + "from experiments e join projects p on e.proj_id = p.id "
                               + "join samples s on s.expe_id = e.id "
                               + "where s.code != 'DEFAULT' "
                               + "group by p.code, e.code order by p.code, e.code")
        self._compareDataBases("Attachments", openbis_data_source, openbis_harvester, "openbis",
                               "select e.code as experiment, p.code as project, s.code as sample, "
                               + "  a.file_name, a.version, a.title, a.description, length(value) as attachment_length, "
                               + "  md5(value) as attachment_hash "
                               + "from attachments a join attachment_contents c on a.exac_id = c.id "
                               + "left join experiments e on a.expe_id = e.id "
                               + "left join projects p on a.proj_id = p.id "
                               + "left join samples s on a.samp_id = s.id order by a.file_name, a.version")
        self._compareDataBases("Samples which are not of type BENCHMARK_OBJECT", openbis_data_source, openbis_harvester, "openbis",
                               "select s.code, '{0}' || t.code as type, s.perm_id, '{0}' || sp.code as space, "
                               + "  p.code as project, e.code as experiment, sc.code as container "
                               + "from samples s join sample_types t on s.saty_id = t.id "
                               + "left join spaces sp on s.space_id = sp.id "
                               + "left join projects p on s.proj_id = p.id "
                               + "left join experiments e on s.expe_id = e.id "
                               + "left join samples sc on s.samp_id_part_of = sc.id "
                               + "where t.code <> '{1}BENCHMARK_OBJECT' and s.code != 'DEFAULT' order by s.code, s.perm_id")
        self._compareDataBases("Sample properties", openbis_data_source, openbis_harvester, "openbis",
                               "select s.code as sample, '{0}' || t.code as type, '{0}' || pt.code as property, "
                               + "  concat(sp.value, cvt.code, m.code) as value "
                               + "from samples s join sample_properties sp on sp.samp_id = s.id "
                               + "left join controlled_vocabulary_terms cvt on sp.cvte_id = cvt.id "
                               + "left join materials m on sp.mate_prop_id = m.id "
                               + "join sample_type_property_types stpt on sp.stpt_id = stpt.id "
                               + "join sample_types t on stpt.saty_id = t.id "
                               + "join property_types pt on stpt.prty_id = pt.id "
                               + "where expe_id in (select id from experiments where code = 'TEST-EXPERIMENT') "
                               + "order by s.code, pt.code")
        self._compareDataBases("Sample relationships", openbis_data_source, openbis_harvester, "openbis",
                               "select p.code as parent, c.code as child, t.code as relationship "
                               + "from sample_relationships r join relationship_types t on r.relationship_id = t.id "
                               + "join samples p on r.sample_id_parent = p.id "
                               + "join samples c on r.sample_id_child = c.id order by p.code, c.code")
        self._compareDataBases("Data sets", openbis_data_source, openbis_harvester, "openbis",
                               "select d.code as data_set, s.code as sample, e.code as experiment " 
                               + "from data d left join samples s on d.samp_id = s.id "
                               + "left join experiments e on d.expe_id=e.id order by d.code")
        self._compareDataBases("Data set properties", openbis_data_source, openbis_harvester, "openbis", 
                               "select d.code as data_set, '{0}' || t.code as type, '{0}' || pt.code as property, "
                               + "  concat(dp.value, cvt.code, m.code) as value "
                               + "from data d join data_set_properties dp on dp.ds_id = d.id "
                               + "left join controlled_vocabulary_terms cvt on dp.cvte_id = cvt.id "
                               + "left join materials m on dp.mate_prop_id = m.id "
                               + "join data_set_type_property_types dtpt on dp.dstpt_id = dtpt.id "
                               + "join data_set_types t on dtpt.dsty_id = t.id "
                               + "join property_types pt on dtpt.prty_id = pt.id "
                               + "order by d.code, pt.code")
        self._compareDataBases("Data set sizes", openbis_data_source, openbis_harvester, "pathinfo",
                               "select d.code as data_set, file_name, size_in_bytes "
                               + "from data_set_files f join data_sets d on f.dase_id=d.id where parent_id is null "
                               + "order by d.code")
        self._compareDataBases("Data set relationships", openbis_data_source, openbis_harvester, "openbis",
                                "select p.code as parent, c.code as child, t.code as relationship_type "
                                + "from data_set_relationships r join data p on r.data_id_parent = p.id "
                                + "join data c on r.data_id_child = c.id "
                                + "join relationship_types t on r.relationship_id = t.id order by p.code, c.code")

    def _compareDataBases(self, name, openbis_data_source, openbis_harvester, databaseType, sql):
        expectedContent = openbis_data_source.queryDatabase(databaseType, sql.format(TYPE_PREFIX, ""), showHeaders = True)
        synchedContent = openbis_harvester.queryDatabase(databaseType, sql.format("", TYPE_PREFIX), showHeaders = True)
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
        monitor.waitUntilEvent(util.RegexCondition('OPERATION.EntitySynchronizer.DS1 - Saving the timestamp of sync start to file'),
                               delay = 60)
        time.sleep(60)


TestCase(settings, __file__).runTest()
