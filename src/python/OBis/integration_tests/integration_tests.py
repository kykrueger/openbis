#!/usr/bin/env python
# -*- coding: utf-8 -*-

import json
import subprocess
import socket
from pybis import Openbis


def run(cmd, tmpdir="", params=[]):
    completed_process = subprocess.run([cmd, tmpdir] + params, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    result = ''
    if completed_process.stderr:
        result += completed_process.stderr.decode('utf-8').strip()
    if completed_process.stdout:
        result += completed_process.stdout.decode('utf-8').strip()
    print('-------------------' + cmd + '------------------- ' + str(tmpdir))
    print(result)
    return result


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

    # 4. Second repository
    result = run('./04_second_repository.sh', tmpdir)
    config_data1 = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data1'))
    config = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data2'))
    assert config['external_dms_id'].startswith('ADMIN-' + socket.gethostname().upper())
    assert config['external_dms_id'] == config_data1['external_dms_id']
    assert len(config['repository_id']) == 36
    assert config['repository_id'] != config_data1['repository_id']
    assert "Created data set {}.".format(config['data_set_id']) in result

    # 5. Second external dms
    result = run('./05_second_external_dms.sh', tmpdir)
    config_data1 = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data1'))
    config = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data_b/data3'))
    assert config['external_dms_id'].startswith('ADMIN-' + socket.gethostname().upper())
    assert config['external_dms_id'] != config_data1['external_dms_id']
    assert len(config['repository_id']) == 36
    assert config['repository_id'] != config_data1['repository_id']
    assert "Created data set {}.".format(config['data_set_id']) in result

    # 6. Error on first commit
    result = run('./06_error_on_first_commit_1_error.sh', tmpdir)
    assert 'Missing configuration settings for [\'object_id\', \'collection_id\'].' in result
    result = run('./06_error_on_first_commit_2_status.sh', tmpdir)
    assert '?? file' in result
    result = run('./06_error_on_first_commit_3_commit.sh', tmpdir)
    config = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data4'))
    assert "Created data set {}.".format(config['data_set_id']) in result

    # 7. Attach data set to a collection
    result = run('./07_attach_to_collection.sh', tmpdir)
    config = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data5'))
    assert config['external_dms_id'].startswith('ADMIN-' + socket.gethostname().upper())
    assert len(config['repository_id']) == 36
    assert "Created data set {}.".format(config['data_set_id']) in result

    # 8. Addref
    result = run('./08_addref_1_success.sh', tmpdir)
    config_data1 = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data1'))
    config_data6 = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data6'))
    assert config_data6 == config_data1
    result = run('./08_addref_2_duplicate.sh', tmpdir)
    assert 'DataSet already exists in the database' in result
    result = run('./08_addref_3_non-existent.sh', tmpdir)
    assert 'Invalid value' in result

    # 9. Local clone
    config_data2 = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data2'))
    result = run('./09_local_clone.sh', tmpdir, [config_data2['data_set_id']])
    config_data2_clone = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data_b/data2'))
    assert config_data2_clone['external_dms_id'].startswith('ADMIN-' + socket.gethostname().upper())
    assert config_data2_clone['external_dms_id'] != config_data2['external_dms_id']
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
    result = run('./11_init_analysis_2_internal.sh', tmpdir)
    config_analysis2 = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data1/analysis2'))
    assert "Created data set {}.".format(config_analysis2['data_set_id']) in result
    assert len(config_analysis2['repository_id']) == 36
    assert config_analysis2['repository_id'] != config_data1['repository_id']
    assert config_analysis2['data_set_id'] != config_data1['data_set_id']
    result = run('./11_init_analysis_3_git_check_ignore.sh', tmpdir)
    assert 'analysis2' in result

    # 12. Metadata only commit
    result = run('./12_metadata_only_1_commit.sh', tmpdir)
    config = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data7'))
    assert "Created data set {}.".format(config['data_set_id']) in result
    result = run('./12_metadata_only_2_metadata_commit.sh', tmpdir)
    config = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data7'))
    assert "Created data set {}.".format(config['data_set_id']) in result

    # 13. obis sync
    result = run('./13_sync_1_git_commit_and_sync.sh', tmpdir)
    config = json.loads(run('./00_get_config.sh', tmpdir + '/obis_data/data7'))
    assert "Created data set {}.".format(config['data_set_id']) in result
    result = run('./13_sync_2_only_sync.sh', tmpdir)
    assert 'Nothing to sync' in result
