#!/usr/bin/env python
# -*- coding: utf-8 -*-

# can be run on vagrant like this:
# vagrant ssh obisserver -c 'cd /vagrant_python/OBis/integration_tests && pytest ./integration_tests.py'

import json
import os
import socket
import subprocess
from contextlib import contextmanager
from pybis import Openbis


def test_obis(tmpdir):
    # 0. pybis login
    o = Openbis('https://localhost:8443', verify_certificates=False)
    o.login('admin', 'admin', save_token=True)

    # 1. Global configuration
    result = run('./01_global_config.sh', tmpdir)
    config = json.loads(run('./00_get_config_global.sh'))
    assert config['openbis_url'] == 'https://localhost:8443'
    assert config['user'] == 'admin'
    assert config['data_set_type'] == 'UNKNOWN'
    assert config['verify_certificates'] == False

    # 2. First commit
    result = run('./02_first_commit_1_create_repository.sh', tmpdir)
    assert '?? .obis/config.json' in result
    assert '?? file' in result
    result = run('./02_first_commit_2_commit.sh', tmpdir)
    config = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data1'))
    assert config['external_dms_id'].startswith('ADMIN-' + socket.gethostname().upper())
    assert len(config['repository_id']) == 36
    assert "Created data set {}.".format(config['data_set_id']) in result
    data_set = o.get_dataset(config['data_set_id']).data
    assert_matching(config, data_set, tmpdir, 'obis_data/data1')

    # 3. Second commit
    config_before = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data1'))
    result = run('./03_second_commit_1_commit.sh', tmpdir)
    config = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data1'))
    assert config['data_set_id'] != config_before['data_set_id']
    assert config['external_dms_id'].startswith('ADMIN-' + socket.gethostname().upper())
    assert config['external_dms_id'] == config_before['external_dms_id']
    assert config['repository_id'] == config_before['repository_id']
    assert "Created data set {}.".format(config['data_set_id']) in result
    result = run('./03_second_commit_2_git_annex_info.sh', tmpdir)
    assert 'file: big_file' in result
    assert 'key: SHA256E-s1000000--d29751f2649b32ff572b5e0a9f541ea660a50f94ff0beedfb0b692b924cc8025' in result
    assert 'present: true' in result
    data_set = o.get_dataset(config['data_set_id']).data
    assert_matching(config, data_set, tmpdir, 'obis_data/data1')
    assert data_set['parents'][0]['code'] == config_before['data_set_id']

    # 4. Second repository
    result = run('./04_second_repository.sh', tmpdir)
    config_data1 = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data1'))
    config = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data2'))
    assert config['external_dms_id'].startswith('ADMIN-' + socket.gethostname().upper())
    assert config['external_dms_id'] == config_data1['external_dms_id']
    assert len(config['repository_id']) == 36
    assert config['repository_id'] != config_data1['repository_id']
    assert "Created data set {}.".format(config['data_set_id']) in result
    data_set = o.get_dataset(config['data_set_id']).data
    assert_matching(config, data_set, tmpdir, 'obis_data/data2')

    # 5. Second external dms
    result = run('./05_second_external_dms.sh', tmpdir)
    config_data1 = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data1'))
    config = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data_b/data3'))
    assert config['external_dms_id'].startswith('ADMIN-' + socket.gethostname().upper())
    assert config['external_dms_id'] != config_data1['external_dms_id']
    assert len(config['repository_id']) == 36
    assert config['repository_id'] != config_data1['repository_id']
    assert "Created data set {}.".format(config['data_set_id']) in result
    data_set = o.get_dataset(config['data_set_id']).data
    assert_matching(config, data_set, tmpdir, 'obis_data_b/data3')

    # 6. Error on first commit
    result = run('./06_error_on_first_commit_1_error.sh', tmpdir)
    assert 'Missing configuration settings for [\'object_id\', \'collection_id\'].' in result
    result = run('./06_error_on_first_commit_2_status.sh', tmpdir)
    assert '?? file' in result
    result = run('./06_error_on_first_commit_3_commit.sh', tmpdir)
    config = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data4'))
    assert "Created data set {}.".format(config['data_set_id']) in result
    data_set = o.get_dataset(config['data_set_id']).data
    assert_matching(config, data_set, tmpdir, 'obis_data/data4')

    # 7. Attach data set to a collection
    result = run('./07_attach_to_collection.sh', tmpdir)
    config = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data5'))
    assert config['external_dms_id'].startswith('ADMIN-' + socket.gethostname().upper())
    assert len(config['repository_id']) == 36
    assert "Created data set {}.".format(config['data_set_id']) in result
    data_set = o.get_dataset(config['data_set_id']).data
    assert_matching(config, data_set, tmpdir, 'obis_data/data5')

    # 8. Addref
    result = run('./08_addref_1_success.sh', tmpdir)
    config_data1 = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data1'))
    config_data6 = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data6'))
    assert config_data6 == config_data1
    result = run('./08_addref_2_duplicate.sh', tmpdir)
    assert 'DataSet already exists in the database' in result
    result = run('./08_addref_3_non-existent.sh', tmpdir)
    assert 'Invalid value' in result
    data_set = o.get_dataset(config_data6['data_set_id']).data
    assert_matching(config_data6, data_set, tmpdir, 'obis_data/data6')

    # 9. Local clone
    config_data2 = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data2'))
    result = run('./09_local_clone.sh', tmpdir, [config_data2['data_set_id']])
    config_data2_clone = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data_b/data2'))
    assert config_data2_clone['external_dms_id'].startswith('ADMIN-' + socket.gethostname().upper())
    assert config_data2_clone['external_dms_id'] != config_data2['external_dms_id']
    data_set = o.get_dataset(config_data2_clone['data_set_id']).data
    assert_matching(config_data2_clone, data_set, tmpdir, 'obis_data_b/data2')
    del config_data2['external_dms_id']
    del config_data2_clone['external_dms_id']
    assert config_data2_clone == config_data2

    # 11. Init analysis
    result = run('./11_init_analysis_1_external.sh', tmpdir, [config_data2['data_set_id']])
    config_data1 = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data1'))
    config_analysis1 = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/analysis1'))
    assert "Created data set {}.".format(config_analysis1['data_set_id']) in result
    assert len(config_analysis1['repository_id']) == 36
    assert config_analysis1['repository_id'] != config_data1['repository_id']
    assert config_analysis1['data_set_id'] != config_data1['data_set_id']
    data_set = o.get_dataset(config_analysis1['data_set_id']).data
    assert_matching(config_analysis1, data_set, tmpdir, 'obis_data/analysis1')
    assert data_set['parents'][0]['code'] == config_data1['data_set_id']
    result = run('./11_init_analysis_2_internal.sh', tmpdir)
    config_analysis2 = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data1/analysis2'))
    assert "Created data set {}.".format(config_analysis2['data_set_id']) in result
    assert len(config_analysis2['repository_id']) == 36
    assert config_analysis2['repository_id'] != config_data1['repository_id']
    assert config_analysis2['data_set_id'] != config_data1['data_set_id']
    result = run('./11_init_analysis_3_git_check_ignore.sh', tmpdir)
    assert 'analysis2' in result
    data_set = o.get_dataset(config_analysis2['data_set_id']).data
    assert_matching(config_analysis2, data_set, tmpdir, 'obis_data/data1/analysis2')
    assert data_set['parents'][0]['code'] == config_data1['data_set_id']

    # 12. Metadata only commit
    result = run('./12_metadata_only_1_commit.sh', tmpdir)
    config = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data7'))
    assert "Created data set {}.".format(config['data_set_id']) in result
    data_set = o.get_dataset(config['data_set_id']).data
    assert_matching(config, data_set, tmpdir, 'obis_data/data7')
    result = run('./12_metadata_only_2_metadata_commit.sh', tmpdir)
    config = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data7'))
    assert "Created data set {}.".format(config['data_set_id']) in result
    data_set = o.get_dataset(config['data_set_id']).data
    assert_matching(config, data_set, tmpdir, 'obis_data/data7')

    # 13. obis sync
    result = run('./13_sync_1_git_commit_and_sync.sh', tmpdir)
    config = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data7'))
    assert "Created data set {}.".format(config['data_set_id']) in result
    data_set = o.get_dataset(config['data_set_id']).data
    assert_matching(config, data_set, tmpdir, 'obis_data/data7')
    result = run('./13_sync_2_only_sync.sh', tmpdir)
    assert 'Nothing to sync' in result

    # 14. Configure data set properties
    result = run('./14_config_data_set_properties_1.sh', tmpdir)
    config = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data8'))
    assert config['data_set_properties'] == { 'A': '0' }
    result = run('./14_config_data_set_properties_2.sh', tmpdir)
    config = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data8'))
    assert config['data_set_properties'] == { 'A': '0', 'B': '1', 'C': '3' }
    result = run('./14_config_data_set_properties_3.sh', tmpdir)
    assert 'Duplicate key after capitalizing JSON config: A' in result

    # 15. Removeref
    with cd(tmpdir + '/obis_data'):
        config = get_config('data6')
        content_copies = get_data_set(o, config)['linkedData']['contentCopies']
        assert len(content_copies) == 2
        cmd('obis removeref data6')
        content_copies = get_data_set(o, config)['linkedData']['contentCopies']
        assert len(content_copies) == 1
        assert content_copies[0]['path'].endswith('data1')
        cmd('obis addref data6')
        cmd('obis removeref data1')
        content_copies = get_data_set(o, config)['linkedData']['contentCopies']
        assert len(content_copies) == 1
        assert content_copies[0]['path'].endswith('data6')
        result = cmd('obis removeref data1')
        assert 'Matching content copy not fount in data set' in result
        cmd('obis addref data1')


def get_config(repository_folder):
    with cd(repository_folder):
        return json.loads(cmd('obis config'))

def get_data_set(o, config):
    return o.get_dataset(config['data_set_id']).data

@contextmanager
def cd(newdir):
    """Safe cd -- return to original dir after execution, even if an exception is raised."""
    prevdir = os.getcwd()
    os.chdir(os.path.expanduser(newdir))
    try:
        yield
    finally:
        os.chdir(prevdir)

def run(cmd, tmpdir="", params=[]):
    completed_process = subprocess.run([cmd, tmpdir] + params, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    return get_cmd_result(completed_process, tmpdir)

def cmd(cmd):
    cmd_split = cmd.split(' ')
    completed_process = subprocess.run(cmd_split, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    return get_cmd_result(completed_process)

def get_cmd_result(completed_process, tmpdir=''):
    result = ''
    if completed_process.stderr:
        result += completed_process.stderr.decode('utf-8').strip()
    if completed_process.stdout:
        result += completed_process.stdout.decode('utf-8').strip()
    return result

def assert_matching(config, data_set, tmpdir, path):
    content_copies = data_set['linkedData']['contentCopies']
    content_copy = list(filter(lambda cc: cc['path'].endswith(path) == 1, content_copies))[0]
    assert data_set['type']['code'] == config['data_set_type']
    assert content_copy['externalDms']['code'] == config['external_dms_id']
    assert content_copy['gitCommitHash'] == run('./00_get_commit_hash.sh', str(tmpdir) + '/' + path)
    assert content_copy['gitRepositoryId'] == config['repository_id']
    if config['object_id'] is not None:
        assert data_set['sample']['identifier']['identifier'] == config['object_id']
    if config['collection_id'] is not None:
        assert data_set['experiment']['identifier']['identifier'] == config['collection_id']
