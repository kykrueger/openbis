import hashlib
import json
import os
from abc import ABC, abstractmethod
from .utils import run_shell
from .command_result import CommandResult, CommandException


def get_checksum_generator(checksum_type, default=None):
    if checksum_type == "SHA256":
        return ChecksumGeneratorSha256()
    elif checksum_type == "MD5":
        return ChecksumGeneratorMd5()
    elif checksum_type == "WORM":
        return ChecksumGeneratorWORM()
    elif default is not None:
        return default
    else:
        return None


def validate_checksum(openbis, files, data_set_id, folder):
    invalid_files = []
    dataset_files = openbis.search_files(data_set_id)['objects']
    dataset_files_by_path = {}
    for dataset_file in dataset_files:
        dataset_files_by_path[dataset_file['path']] = dataset_file
    for filename in files:
        dataset_file = dataset_files_by_path[filename]
        filename_dest = os.path.join(folder, filename)
        checksum_generator = None
        if dataset_file['checksumCRC32'] is not None and dataset_file['checksumCRC32'] > 0:
            checksum_generator = ChecksumGeneratorCrc32()
            expected_checksum = dataset_file['checksumCRC32']
        elif dataset_file['checksumType'] is not None:
            checksum_generator = get_checksum_generator(dataset_file['checksumType'])
            expected_checksum = dataset_file['checksum']
        if checksum_generator is not None:
            checksum = checksum_generator.get_checksum(filename_dest)['checksum']
            if checksum != expected_checksum:
                invalid_files.append(filename)
    return invalid_files


class ChecksumGeneratorCrc32(object):
    def get_checksum(self, file):
        result = run_shell(['cksum', file])
        if result.failure():
            raise CommandException(result)
        fields = result.output.split(" ")
        return {
            'crc32': int(fields[0]),
            'fileLength': int(fields[1]),
            'path': file
        }


class ChecksumGeneratorHashlib(ABC):
    @abstractmethod
    def hash_function(self):
        pass
    @abstractmethod
    def hash_type(self):
        pass

    def get_checksum(self, file):
        return {
            'checksum': self._checksum(file),
            'checksumType': self.hash_type(),
            'fileLength': os.path.getsize(file),
            'path': file
        }

    def _checksum(self, file):
        hash_function = self.hash_function()
        with open(file, "rb") as f:
            for chunk in iter(lambda: f.read(4096), b""):
                hash_function.update(chunk)
        return hash_function.hexdigest()


class ChecksumGeneratorSha256(ChecksumGeneratorHashlib):
    def hash_function(self):
        return hashlib.sha256()
    def hash_type(self):
        return 'SHA256'


class ChecksumGeneratorMd5(ChecksumGeneratorHashlib):
    def hash_function(self):
        return hashlib.md5()
    def hash_type(self):
        return "MD5"


class ChecksumGeneratorWORM(object):
    def get_checksum(self, file):
        return {
            'checksum': self.worm(file),
            'checksumType': 'WORM',
            'fileLength': os.path.getsize(file),
            'path': file
        }        
    def worm(self, file):
        modification_time = int(os.path.getmtime(file))
        size = os.path.getsize(file)
        return "s{}-m{}--{}".format(size, modification_time, file)


class ChecksumGeneratorGitAnnex(object):

    def __init__(self):
        self.backend = self._get_annex_backend()
        self.checksum_generator_replacement = ChecksumGeneratorCrc32() if self.backend is None else None
        # define which generator to use for files which are not handled by annex
        self.checksum_generator_supplement = get_checksum_generator(self.backend, default=ChecksumGeneratorCrc32())

    def get_checksum(self, file):
        if self.checksum_generator_replacement is not None:
            return self.checksum_generator_replacement.get_checksum(file)
        return self._get_checksum(file)

    def _get_checksum(self, file):
        annex_result = run_shell(['git', 'annex', 'info', '-j', file], raise_exception_on_failure=True)
        if 'Not a valid object name' in annex_result.output:
            return self.checksum_generator_supplement.get_checksum(file)
        annex_info = json.loads(annex_result.output)
        # TODO annex_info will not have 'present' if there is a git repository within the obis repository
        if annex_info['present'] != True:
            return self.checksum_generator_supplement.get_checksum(file)
        return {
            'checksum': self._get_checksum_from_annex_info(annex_info),
            'checksumType': self.backend,
            'fileLength': os.path.getsize(file),
            'path': file
        }

    def _get_checksum_from_annex_info(self, annex_info):
        if self.backend in ['MD5', 'SHA256']:
            return annex_info['key'].split('--')[1].split('.')[0]
        elif self.backend == 'WORM':
            return annex_info['key'][5:]
        else:
            raise ValueError("Git annex backend not supported: " + self.backend)

    def _get_annex_backend(self):
        with open('.gitattributes') as gitattributes:
            for line in gitattributes.readlines():
                if 'annex.backend' in line:
                    backend = line.split('=')[1].strip()
                    if backend == 'SHA256E':
                        backend = 'SHA256'
                    return backend
        return None
