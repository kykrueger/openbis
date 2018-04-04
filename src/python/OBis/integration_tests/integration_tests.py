#!/usr/bin/env python
# -*- coding: utf-8 -*-

# can be run on vagrant like this:
# vagrant ssh obisserver -c 'cd /vagrant_python/OBis/integration_tests && pytest ./integration_tests.py'

import json
import os
import socket
import subprocess
from subprocess import PIPE
from subprocess import SubprocessError
from contextlib import contextmanager
from pybis import Openbis


output_buffer = ''

def decorator_print(func):
    def wrapper(tmpdir, *args, **kwargs):
        try:
            func(tmpdir, *args, **kwargs)
        except AssertionError:
            print(output_buffer)
            raise
    return wrapper

@decorator_print
def test_obis(tmpdir):
    global output_buffer

    o = Openbis('https://obisserver:8443', verify_certificates=False)
    o.login('admin', 'admin', save_token=True)

    output_buffer = '=================== 1. Global configuration ===================\n'
    if os.path.exists('~/.obis'):
        os.rmdir('~/.obis')
    cmd('obis config -g openbis_url https://obisserver:8443')
    cmd('obis config -g user admin')
    cmd('obis config -g data_set_type UNKNOWN')
    cmd('obis config -g verify_certificates false')
    cmd('obis config -g hostname ' + socket.gethostname())
    config = get_config_global()
    assert config['openbis_url'] == 'https://obisserver:8443'
    assert config['user'] == 'admin'
    assert config['data_set_type'] == 'UNKNOWN'
    assert config['verify_certificates'] == False
    assert config['hostname'] == socket.gethostname()

    with cd(tmpdir): cmd('mkdir obis_data')
    with cd(tmpdir + '/obis_data'):

        output_buffer = '=================== 2. First commit ===================\n'
        cmd('obis init data1')
        with cd('data1'):
            cmd('touch file')
            result = cmd('obis status')
            assert '?? .obis/config.json' in result
            assert '?? file' in result
            cmd('obis config object_id /DEFAULT/DEFAULT')
            result = cmd('obis commit -m \'commit-message\'')
            config = get_config()
            assert config['external_dms_id'].startswith('ADMIN-' + socket.gethostname().upper())
            assert len(config['repository_id']) == 36
            assert "Created data set {}.".format(config['data_set_id']) in result
            data_set = o.get_dataset(config['data_set_id']).data
            assert_matching(config, data_set, tmpdir, 'obis_data/data1')

        output_buffer = '=================== 3. Second commit ===================\n'
        with cd('data1'):
            config_before = get_config()
            cmd('dd if=/dev/zero of=big_file bs=1000000 count=1')
            result = cmd('obis commit -m \'commit-message\'')
            config = get_config()
            assert config['data_set_id'] != config_before['data_set_id']
            assert config['external_dms_id'].startswith('ADMIN-' + socket.gethostname().upper())
            assert config['external_dms_id'] == config_before['external_dms_id']
            assert config['repository_id'] == config_before['repository_id']
            assert "Created data set {}.".format(config['data_set_id']) in result
            result = cmd('git annex info big_file')
            assert 'file: big_file' in result
            assert 'key: SHA256E-s1000000--d29751f2649b32ff572b5e0a9f541ea660a50f94ff0beedfb0b692b924cc8025' in result
            assert 'present: true' in result
            data_set = o.get_dataset(config['data_set_id']).data
            assert_matching(config, data_set, tmpdir, 'obis_data/data1')
            assert data_set['parents'][0]['code'] == config_before['data_set_id']

        output_buffer = '=================== 4. Second repository ===================\n'
        cmd('obis init data2')
        with cd('data2'):
            cmd('obis config object_id /DEFAULT/DEFAULT')
            cmd('touch file')
            result = cmd('obis commit -m \'commit-message\'')
            with cd('../data1'): config_data1 = get_config()
            config = get_config()
            assert config['external_dms_id'].startswith('ADMIN-' + socket.gethostname().upper())
            assert config['external_dms_id'] == config_data1['external_dms_id']
            assert len(config['repository_id']) == 36
            assert config['repository_id'] != config_data1['repository_id']
            assert "Created data set {}.".format(config['data_set_id']) in result
            data_set = o.get_dataset(config['data_set_id']).data
            assert_matching(config, data_set, tmpdir, 'obis_data/data2')

    output_buffer = '=================== 5. Second external dms ===================\n'
    with cd(tmpdir): cmd('mkdir obis_data_b')
    with cd(tmpdir + '/obis_data_b'):
        cmd('obis init data3')
        with cd('data3'):
            cmd('obis config object_id /DEFAULT/DEFAULT')
            cmd('touch file')
            result = cmd('obis commit -m \'commit-message\'')
            with cd('../../obis_data/data1'): config_data1 = get_config()
            config = get_config()
            assert config['external_dms_id'].startswith('ADMIN-' + socket.gethostname().upper())
            assert config['external_dms_id'] != config_data1['external_dms_id']
            assert len(config['repository_id']) == 36
            assert config['repository_id'] != config_data1['repository_id']
            assert "Created data set {}.".format(config['data_set_id']) in result
            data_set = o.get_dataset(config['data_set_id']).data
            assert_matching(config, data_set, tmpdir, 'obis_data_b/data3')

    output_buffer = '=================== 6. Error on first commit ===================\n'
    with cd(tmpdir + '/obis_data'):
        cmd('obis init data4')
        with cd('data4'):
            cmd('touch file')
            result = cmd('obis commit -m \'commit-message\'')
            assert 'Missing configuration settings for [\'object_id\', \'collection_id\'].' in result
            result = cmd('obis status')
            assert '?? file' in result
            cmd('obis config object_id /DEFAULT/DEFAULT')
            result = cmd('obis commit -m \'commit-message\'')
            config = get_config()
            assert "Created data set {}.".format(config['data_set_id']) in result
            data_set = o.get_dataset(config['data_set_id']).data
            assert_matching(config, data_set, tmpdir, 'obis_data/data4')

        output_buffer = '=================== 7. Attach data set to a collection ===================\n'
        cmd('obis init data5')
        with cd('data5'):
            cmd('touch file')
            cmd('obis config collection_id /DEFAULT/DEFAULT/DEFAULT')
            result = cmd('obis commit -m \'commit-message\'')
            config = get_config()
            assert config['external_dms_id'].startswith('ADMIN-' + socket.gethostname().upper())
            assert len(config['repository_id']) == 36
            assert "Created data set {}.".format(config['data_set_id']) in result
            data_set = o.get_dataset(config['data_set_id']).data
            assert_matching(config, data_set, tmpdir, 'obis_data/data5')

        output_buffer = '=================== 8. Addref ===================\n'
        cmd('cp -r data1 data6')
        cmd('obis addref data6')
        with cd('data1'): config_data1 = get_config()
        with cd('data6'): config_data6 = get_config()
        assert config_data6 == config_data1
        result = cmd('obis addref data6')
        assert 'DataSet already exists in the database' in result
        result = cmd('obis addref data7')
        assert 'Invalid value' in result
        data_set = o.get_dataset(config_data6['data_set_id']).data
        with cd('data6'): assert_matching(config_data6, data_set, tmpdir, 'obis_data/data6')

        output_buffer = '=================== 9. Local clone ===================\n'
        with cd('data2'): config_data2 = get_config()
        with cd('../obis_data_b'):
            cmd('obis clone ' + config_data2['data_set_id'])
            with cd('data2'):
                config_data2_clone = get_config()
                assert config_data2_clone['external_dms_id'].startswith('ADMIN-' + socket.gethostname().upper())
                assert config_data2_clone['external_dms_id'] != config_data2['external_dms_id']
                data_set = o.get_dataset(config_data2_clone['data_set_id']).data
                assert_matching(config_data2_clone, data_set, tmpdir, 'obis_data_b/data2')
                del config_data2['external_dms_id']
                del config_data2_clone['external_dms_id']
                assert config_data2_clone == config_data2

        output_buffer = '=================== 11. Init analysis ===================\n'
        cmd('obis init_analysis -p data1 analysis1')
        with cd('analysis1'):
            cmd('obis config object_id /DEFAULT/DEFAULT')
            cmd('touch file')
            result = cmd('obis commit -m \'commit-message\'')
        with cd('data1'): config_data1 = get_config()
        with cd('analysis1'):
            config_analysis1 = get_config()
            assert "Created data set {}.".format(config_analysis1['data_set_id']) in result
            assert len(config_analysis1['repository_id']) == 36
            assert config_analysis1['repository_id'] != config_data1['repository_id']
            assert config_analysis1['data_set_id'] != config_data1['data_set_id']
            data_set = o.get_dataset(config_analysis1['data_set_id']).data
            assert_matching(config_analysis1, data_set, tmpdir, 'obis_data/analysis1')
            assert data_set['parents'][0]['code'] == config_data1['data_set_id']
        with cd('data1'):
            cmd('obis init_analysis analysis2')
            with cd('analysis2'):
                cmd('obis config object_id /DEFAULT/DEFAULT')
                cmd('touch file')
                result = cmd('obis commit -m \'commit-message\'')
                config_analysis2 = get_config()
                assert "Created data set {}.".format(config_analysis2['data_set_id']) in result
                assert len(config_analysis2['repository_id']) == 36
                assert config_analysis2['repository_id'] != config_data1['repository_id']
                assert config_analysis2['data_set_id'] != config_data1['data_set_id']
                data_set = o.get_dataset(config_analysis2['data_set_id']).data
                assert_matching(config_analysis2, data_set, tmpdir, 'obis_data/data1/analysis2')
                assert data_set['parents'][0]['code'] == config_data1['data_set_id']
            result = cmd('git check-ignore analysis2')
            assert 'analysis2' in result

        output_buffer = '=================== 12. Metadata only commit ===================\n'
        cmd('obis init data7')
        with cd('data7'):
            cmd('obis config object_id /DEFAULT/DEFAULT')
            cmd('touch file')
            result = cmd('obis commit -m \'commit-message\'')
            config = get_config()
            assert "Created data set {}.".format(config['data_set_id']) in result
            data_set = o.get_dataset(config['data_set_id']).data
            assert_matching(config, data_set, tmpdir, 'obis_data/data7')
            cmd('obis config collection_id /DEFAULT/DEFAULT/DEFAULT')
            result = cmd('obis commit -m \'commit-message\'')
            config = get_config()
            assert "Created data set {}.".format(config['data_set_id']) in result
            data_set = o.get_dataset(config['data_set_id']).data
            assert_matching(config, data_set, tmpdir, 'obis_data/data7')

        output_buffer = '=================== 13. obis sync ===================\n'
        with cd('data7'):
            cmd('touch file2')
            cmd('git add file2')
            cmd('git commit -m \'msg\'')
            result = cmd('obis sync')
            config = get_config()
            assert "Created data set {}.".format(config['data_set_id']) in result
            data_set = o.get_dataset(config['data_set_id']).data
            assert_matching(config, data_set, tmpdir, 'obis_data/data7')
            result = cmd('obis sync')
            assert 'Nothing to sync' in result

        output_buffer = '=================== 14. Configure data set properties ===================\n'
        cmd('obis init data8')
        with cd('data8'):
            result = cmd('obis config -p a 0')
            config = get_config()
            assert config['data_set_properties'] == { 'A': '0' }
            cmd('obis config data_set_properties {"a":"0","b":"1","c":"2"}')
            cmd('obis config -p c 3')
            config = get_config()
            assert config['data_set_properties'] == { 'A': '0', 'B': '1', 'C': '3' }
            result = cmd('obis config data_set_properties {"a":"0","A":"1"}')
            assert 'Duplicate key after capitalizing JSON config: A' in result

        output_buffer = '=================== 15. Removeref ===================\n'
        with cd('data6'): config = get_config()
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

        output_buffer = '=================== 16. User switch ===================\n'
        cmd('obis init data9')
        with cd('data9'):
            cmd('touch file')
            cmd('obis config object_id /DEFAULT/DEFAULT')
            result = cmd('obis commit -m \'commit-message\'')
            config = get_config()
            assert "Created data set {}.".format(config['data_set_id']) in result
            cmd('touch file2')
            cmd('obis config user watney')
            # expect timeout because obis is asking for the password of the new user
            try:
                timeout = False
                result = cmd('obis commit -m \'commit-message\'', timeout=3)
            except SubprocessError:
                timeout = True
            assert timeout == True


def get_config():
    return json.loads(cmd('obis config'))

def get_config_global():
    return json.loads(cmd('obis config -g'))

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

def cmd(cmd, timeout=None):
    global output_buffer
    output_buffer += '==== running: ' + cmd + '\n'
    output_buffer += '====          with: ' + str(cmd.split(' ')) + '\n'
    completed_process = subprocess.run(cmd.split(' '), stdout=PIPE, stderr=PIPE, timeout=timeout)
    result = get_cmd_result(completed_process)
    output_buffer += result + '\n'
    return result

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
    assert content_copy['gitCommitHash'] == cmd('git rev-parse --short HEAD')
    assert content_copy['gitRepositoryId'] == config['repository_id']
    if config['object_id'] is not None:
        assert data_set['sample']['identifier']['identifier'] == config['object_id']
    if config['collection_id'] is not None:
        assert data_set['experiment']['identifier']['identifier'] == config['collection_id']
