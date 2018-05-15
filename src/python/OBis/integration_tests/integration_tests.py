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
        except Exception:
            print(output_buffer)
            raise
    return wrapper

@decorator_print
def test_obis(tmpdir):
    global output_buffer

    o = Openbis('https://obisserver:8443', verify_certificates=False)
    o.login('admin', 'admin', save_token=True)

    output_buffer = '=================== 1. Global settings ===================\n'
    if os.path.exists('~/.obis'):
        os.rmdir('~/.obis')
    cmd('obis config -g set openbis_url=https://obisserver:8443')
    cmd('obis config -g set user=admin')
    cmd('obis config -g set verify_certificates=false')
    cmd('obis config -g set hostname=' + socket.gethostname())
    cmd('obis data_set -g set type=UNKNOWN')
    settings = get_settings_global()
    assert settings['config']['openbis_url'] == 'https://obisserver:8443'
    assert settings['config']['user'] == 'admin'
    assert settings['config']['verify_certificates'] == False
    assert settings['config']['hostname'] == socket.gethostname()
    assert settings['data_set']['type'] == 'UNKNOWN'

    with cd(tmpdir): cmd('mkdir obis_data')
    with cd(tmpdir + '/obis_data'):

        output_buffer = '=================== 2. First commit ===================\n'
        cmd('obis init data1')
        with cd('data1'):
            cmd('touch file')
            result = cmd('obis status')
            assert '?? .obis/config.json' in result
            assert '?? file' in result
            cmd('obis object set object_id=/DEFAULT/DEFAULT')
            result = cmd('obis commit -m \'commit-message\'')
            settings = get_settings()
            assert settings['repository']['external_dms_id'].startswith('ADMIN-' + socket.gethostname().upper())
            assert len(settings['repository']['repository_id']) == 36
            assert "Created data set {}.".format(settings['repository']['data_set_id']) in result
            data_set = o.get_dataset(settings['repository']['data_set_id']).data
            assert_matching(settings, data_set, tmpdir, 'obis_data/data1')

        output_buffer = '=================== 3. Second commit ===================\n'
        with cd('data1'):
            settings_before = get_settings()
            cmd('dd if=/dev/zero of=big_file bs=1000000 count=1')
            result = cmd('obis commit -m \'commit-message\'')
            settings = get_settings()
            assert settings['repository']['data_set_id'] != settings_before['repository']['data_set_id']
            assert settings['repository']['external_dms_id'].startswith('ADMIN-' + socket.gethostname().upper())
            assert settings['repository']['external_dms_id'] == settings_before['repository']['external_dms_id']
            assert settings['repository']['repository_id'] == settings_before['repository']['repository_id']
            assert "Created data set {}.".format(settings['repository']['data_set_id']) in result
            result = cmd('git annex info big_file')
            assert 'file: big_file' in result
            assert 'key: MD5-s1000000--879f4bba57ed37c9ec5e5aedf9864698' in result
            assert 'present: true' in result
            data_set = o.get_dataset(settings['repository']['data_set_id']).data
            assert_matching(settings, data_set, tmpdir, 'obis_data/data1')
            assert data_set['parents'][0]['code'] == settings_before['repository']['data_set_id']

        output_buffer = '=================== 4. Second repository ===================\n'
        cmd('obis init data2')
        with cd('data2'):
            cmd('obis object set object_id=/DEFAULT/DEFAULT')
            cmd('touch file')
            result = cmd('obis commit -m \'commit-message\'')
            with cd('../data1'): settings_data1 = get_settings()
            settings = get_settings()
            assert settings['repository']['external_dms_id'].startswith('ADMIN-' + socket.gethostname().upper())
            assert settings['repository']['external_dms_id'] == settings_data1['repository']['external_dms_id']
            assert len(settings['repository']['repository_id']) == 36
            assert settings['repository']['repository_id'] != settings_data1['repository']['repository_id']
            assert "Created data set {}.".format(settings['repository']['data_set_id']) in result
            data_set = o.get_dataset(settings['repository']['data_set_id']).data
            assert_matching(settings, data_set, tmpdir, 'obis_data/data2')

    output_buffer = '=================== 5. Second external dms ===================\n'
    with cd(tmpdir): cmd('mkdir obis_data_b')
    with cd(tmpdir + '/obis_data_b'):
        cmd('obis init data3')
        with cd('data3'):
            cmd('obis object set object_id=/DEFAULT/DEFAULT')
            cmd('touch file')
            result = cmd('obis commit -m \'commit-message\'')
            with cd('../../obis_data/data1'): settings_data1 = get_settings()
            settings = get_settings()
            assert settings['repository']['external_dms_id'].startswith('ADMIN-' + socket.gethostname().upper())
            assert settings['repository']['external_dms_id'] != settings_data1['repository']['external_dms_id']
            assert len(settings['repository']['repository_id']) == 36
            assert settings['repository']['repository_id'] != settings_data1['repository']['repository_id']
            assert "Created data set {}.".format(settings['repository']['data_set_id']) in result
            data_set = o.get_dataset(settings['repository']['data_set_id']).data
            assert_matching(settings, data_set, tmpdir, 'obis_data_b/data3')

    output_buffer = '=================== 6. Error on first commit ===================\n'
    with cd(tmpdir + '/obis_data'):
        cmd('obis init data4')
        with cd('data4'):
            cmd('touch file')
            result = cmd('obis commit -m \'commit-message\'')
            assert 'Missing configuration settings for [\'object_id\', \'collection_id\'].' in result
            result = cmd('obis status')
            assert '?? file' in result
            cmd('obis object set object_id=/DEFAULT/DEFAULT')
            result = cmd('obis commit -m \'commit-message\'')
            settings = get_settings()
            assert "Created data set {}.".format(settings['repository']['data_set_id']) in result
            data_set = o.get_dataset(settings['repository']['data_set_id']).data
            assert_matching(settings, data_set, tmpdir, 'obis_data/data4')

        output_buffer = '=================== 7. Attach data set to a collection ===================\n'
        cmd('obis init data5')
        with cd('data5'):
            cmd('touch file')
            cmd('obis collection set collection_id=/DEFAULT/DEFAULT/DEFAULT')
            result = cmd('obis commit -m \'commit-message\'')
            settings = get_settings()
            assert settings['repository']['external_dms_id'].startswith('ADMIN-' + socket.gethostname().upper())
            assert len(settings['repository']['repository_id']) == 36
            assert "Created data set {}.".format(settings['repository']['data_set_id']) in result
            data_set = o.get_dataset(settings['repository']['data_set_id']).data
            assert_matching(settings, data_set, tmpdir, 'obis_data/data5')

        output_buffer = '=================== 8. Addref ===================\n'
        cmd('cp -r data1 data6')
        cmd('obis addref data6')
        with cd('data1'): settings_data1 = get_settings()
        with cd('data6'): settings_data6 = get_settings()
        assert settings_data6 == settings_data1
        result = cmd('obis addref data6')
        assert 'DataSet already exists in the database' in result
        result = cmd('obis addref data7')
        assert 'Invalid value' in result
        data_set = o.get_dataset(settings_data6['repository']['data_set_id']).data
        with cd('data6'): assert_matching(settings_data6, data_set, tmpdir, 'obis_data/data6')

        output_buffer = '=================== 9. Local clone ===================\n'
        with cd('data2'): settings_data2 = get_settings()
        with cd('../obis_data_b'):
            cmd('obis clone ' + settings_data2['repository']['data_set_id'])
            with cd('data2'):
                settings_data2_clone = get_settings()
                assert settings_data2_clone['repository']['external_dms_id'].startswith('ADMIN-' + socket.gethostname().upper())
                assert settings_data2_clone['repository']['external_dms_id'] != settings_data2['repository']['external_dms_id']
                data_set = o.get_dataset(settings_data2_clone['repository']['data_set_id']).data
                assert_matching(settings_data2_clone, data_set, tmpdir, 'obis_data_b/data2')
                del settings_data2['repository']['external_dms_id']
                del settings_data2_clone['repository']['external_dms_id']
                assert settings_data2_clone == settings_data2

        output_buffer = '=================== 11. Init analysis ===================\n'
        cmd('obis init_analysis -p data1 analysis1')
        with cd('analysis1'):
            cmd('obis object set object_id=/DEFAULT/DEFAULT')
            cmd('touch file')
            result = cmd('obis commit -m \'commit-message\'')
        with cd('data1'): settings_data1 = get_settings()
        with cd('analysis1'):
            settings_analysis1 = get_settings()
            assert "Created data set {}.".format(settings_analysis1['repository']['data_set_id']) in result
            assert len(settings_analysis1['repository']['repository_id']) == 36
            assert settings_analysis1['repository']['repository_id'] != settings_data1['repository']['repository_id']
            assert settings_analysis1['repository']['data_set_id'] != settings_data1['repository']['data_set_id']
            data_set = o.get_dataset(settings_analysis1['repository']['data_set_id']).data
            assert_matching(settings_analysis1, data_set, tmpdir, 'obis_data/analysis1')
            assert data_set['parents'][0]['code'] == settings_data1['repository']['data_set_id']
        with cd('data1'):
            cmd('obis init_analysis analysis2')
            with cd('analysis2'):
                cmd('obis object set object_id=/DEFAULT/DEFAULT')
                cmd('touch file')
                result = cmd('obis commit -m \'commit-message\'')
                settings_analysis2 = get_settings()
                assert "Created data set {}.".format(settings_analysis2['repository']['data_set_id']) in result
                assert len(settings_analysis2['repository']['repository_id']) == 36
                assert settings_analysis2['repository']['repository_id'] != settings_data1['repository']['repository_id']
                assert settings_analysis2['repository']['data_set_id'] != settings_data1['repository']['data_set_id']
                data_set = o.get_dataset(settings_analysis2['repository']['data_set_id']).data
                assert_matching(settings_analysis2, data_set, tmpdir, 'obis_data/data1/analysis2')
                assert data_set['parents'][0]['code'] == settings_data1['repository']['data_set_id']
            result = cmd('git check-ignore analysis2')
            assert 'analysis2' in result

        output_buffer = '=================== 12. Metadata only commit ===================\n'
        cmd('obis init data7')
        with cd('data7'):
            cmd('obis object set object_id=/DEFAULT/DEFAULT')
            cmd('touch file')
            result = cmd('obis commit -m \'commit-message\'')
            settings = get_settings()
            assert "Created data set {}.".format(settings['repository']['data_set_id']) in result
            data_set = o.get_dataset(settings['repository']['data_set_id']).data
            assert_matching(settings, data_set, tmpdir, 'obis_data/data7')
            cmd('obis collection set collection_id=/DEFAULT/DEFAULT/DEFAULT')
            result = cmd('obis commit -m \'commit-message\'')
            settings = get_settings()
            assert "Created data set {}.".format(settings['repository']['data_set_id']) in result
            data_set = o.get_dataset(settings['repository']['data_set_id']).data
            assert_matching(settings, data_set, tmpdir, 'obis_data/data7')

        output_buffer = '=================== 13. obis sync ===================\n'
        with cd('data7'):
            cmd('touch file2')
            cmd('git add file2')
            cmd('git commit -m \'msg\'')
            result = cmd('obis sync')
            settings = get_settings()
            assert "Created data set {}.".format(settings['repository']['data_set_id']) in result
            data_set = o.get_dataset(settings['repository']['data_set_id']).data
            assert_matching(settings, data_set, tmpdir, 'obis_data/data7')
            result = cmd('obis sync')
            assert 'Nothing to sync' in result

        output_buffer = '=================== 14. Set data set properties ===================\n'
        cmd('obis init data8')
        with cd('data8'):
            result = cmd('obis data_set -p set a=0')
            settings = get_settings()
            assert settings['data_set']['properties'] == { 'A': '0' }
            cmd('obis data_set set properties={"a":"0","b":"1","c":"2"}')
            cmd('obis data_set -p set c=3')
            settings = get_settings()
            assert settings['data_set']['properties'] == { 'A': '0', 'B': '1', 'C': '3' }
            result = cmd('obis data_set set properties={"a":"0","A":"1"}')
            assert 'Duplicate key after capitalizing JSON config: A' in result

        output_buffer = '=================== 15. Removeref ===================\n'
        with cd('data6'): settings = get_settings()
        content_copies = get_data_set(o, settings)['linkedData']['contentCopies']
        assert len(content_copies) == 2
        cmd('obis removeref data6')
        content_copies = get_data_set(o, settings)['linkedData']['contentCopies']
        assert len(content_copies) == 1
        assert content_copies[0]['path'].endswith('data1')
        cmd('obis addref data6')
        cmd('obis removeref data1')
        content_copies = get_data_set(o, settings)['linkedData']['contentCopies']
        assert len(content_copies) == 1
        assert content_copies[0]['path'].endswith('data6')
        result = cmd('obis removeref data1')
        assert 'Matching content copy not fount in data set' in result
        cmd('obis addref data1')

        output_buffer = '=================== 18. Use git-annex hashes as checksums ===================\n'
        cmd('obis init data10')
        with cd('data10'):
            cmd('touch file')
            cmd('obis object set object_id=/DEFAULT/DEFAULT')
            # use MD5 form git annex by default
            result = cmd('obis commit -m \'commit-message\'')
            settings = get_settings()
            search_result = o.search_files(settings['repository']['data_set_id'])
            files = list(filter(lambda file: file['fileLength'] > 0, search_result['objects']))
            assert len(files) == 5
            for file in files:
                assert file['checksumType'] == "MD5"
                assert len(file['checksum']) == 32
            # don't use git annex hash - use default CRC32
            cmd('obis config set git_annex_hash_as_checksum=false')
            result = cmd('obis commit -m \'commit-message\'')
            settings = get_settings()
            search_result = o.search_files(settings['repository']['data_set_id'])
            files = list(filter(lambda file: file['fileLength'] > 0, search_result['objects']))
            assert len(files) == 5
            for file in files:
                assert file['checksumType'] is None
                assert file['checksum'] is None
                assert file['checksumCRC32'] != 0

        output_buffer = '=================== 16. User switch ===================\n'
        cmd('obis init data9')
        with cd('data9'):
            cmd('touch file')
            cmd('obis object set object_id=/DEFAULT/DEFAULT')
            result = cmd('obis commit -m \'commit-message\'')
            settings = get_settings()
            assert "Created data set {}.".format(settings['repository']['data_set_id']) in result
            cmd('touch file2')
            cmd('obis config set user=watney')
            # expect timeout because obis is asking for the password of the new user
            try:
                timeout = False
                result = cmd('obis commit -m \'commit-message\'', timeout=3)
            except SubprocessError:
                timeout = True
            assert timeout == True


def get_settings():
    return json.loads(cmd('obis settings get'))

def get_settings_global():
    return json.loads(cmd('obis settings -g get'))

def get_data_set(o, settings):
    return o.get_dataset(settings['repository']['data_set_id']).data

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

def assert_matching(settings, data_set, tmpdir, path):
    content_copies = data_set['linkedData']['contentCopies']
    content_copy = list(filter(lambda cc: cc['path'].endswith(path) == 1, content_copies))[0]
    assert data_set['type']['code'] == settings['data_set']['type']
    assert content_copy['externalDms']['code'] == settings['repository']['external_dms_id']
    assert content_copy['gitCommitHash'] == cmd('git rev-parse --short HEAD')
    assert content_copy['gitRepositoryId'] == settings['repository']['repository_id']
    if settings['object']['object_id'] is not None:
        assert data_set['sample']['identifier']['identifier'] == settings['object']['object_id']
    if settings['collection']['collection_id'] is not None:
        assert data_set['experiment']['identifier']['identifier'] == settings['collection']['collection_id']
