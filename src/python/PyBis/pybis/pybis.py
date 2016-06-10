#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
pybis.py

A class with methods for interacting with openBIS.

Created by Chandrasekhar Ramakrishnan on 2016-05-10.
Copyright (c) 2016 ETH Zuerich. All rights reserved.
"""

import os
import requests
import json
import re
from urllib.parse import urlparse

import threading
from threading import Thread
from queue import Queue


class OpenbisCredentials:
    """Credentials for communicating with openBIS."""

    def __init__(self, token=None, uname_and_pass=None):
        """A connection can be authenticated either by a token or a username and password combination
        :param token: An authentication token for openBIS, can be None.
        :param uname_and_pass: A tuple with username and password, in that order.
        """
        self.token = token
        self.uname_and_pass = uname_and_pass

    def has_token(self):
        return self.token is not None

    def has_username_and_password(self):
        return self.uname_and_pass is not None

    @property
    def username(self):
        return self.uname_and_pass[0]

    @property
    def password(self):
        return self.uname_and_pass[1]


class OpenbisCredentialStore:
    """Cache login tokens for reuse."""

    def __init__(self, store_folder):
        """Cache credentials on the file system at store_path.
        If the store_folder does not exist, it will be created with the umask inherited from the shell.
        :param store_folder: The folder to write the credentials to. It will be created if necessary.
        """
        self.store_folder = store_folder

    @property
    def store_path(self):
        return os.path.join(self.store_folder, "bis_token.txt")

    def read(self):
        """Read the cached credentials and return a credentials object.
        :return: A credentials object with a token, or an empty credentials object if no store was found.
        """
        if not os.path.exists(self.store_path):
            return OpenbisCredentials()
        with open(self.store_path, "r") as f:
            token = f.read()
        return OpenbisCredentials(token)

    def write(self, credentials):
        """Write a credentials object to the store, overwriting any previous information.
        :param credentials: The credentials with a token to write. If it has no token, nothing is written.
        """
        if not credentials.has_token():
            return
        token = credentials.token
        if not os.path.exists(self.store_folder):
            os.makedirs(self.store_folder)
        with open(self.store_path, "w") as f:
            f.write(token)


class Openbis:
    """Interface for communicating with openBIS."""

    def __init__(self, url):
        """Initialize an interface to openBIS with information necessary to connect to the server.
        :param host:
        """

        url_obj = urlparse(url)
        if  url_obj.netloc is None:
            raise ValueError("please provide the url in this format: https://openbis.host.ch:8443")

        self.url_obj = url_obj
        self.url     = url_obj.geturl()
        self.port    = url_obj.port
        self.hostname = url_obj.hostname
        self.v3_as = '/openbis/openbis/rmi-application-server-v3.json'
        self.v1_as = '/openbis/openbis/rmi-general-information-v1.json'

        self.token_filename = os.path.join(os.path.expanduser("~"), '.pybis', self.hostname + '.token')
        try: 
            with open(self.token_filename) as f:
                self.token = f.read()
                if not self.is_token_valid():
                    self.token = None
                    os.remove(self.token_filename)
        except FileNotFoundError:
            self.token = None


    def token(self):
        if self.token is None:
            raise ValueError('no valid session available')


    def save_token(self):
        os.makedirs(os.path.dirname(self.token_filename), exist_ok=True)
        with open(self.token_filename, 'w') as f:
            f.write(self.token)


    def post_request(self, resource, data):
        resp = requests.post(self.url + resource, json.dumps(data))

        if resp.ok:
            if 'error' in resp.json():
                raise ValueError('an error has occured: ' + resp.json()['error'] )
            elif 'result' in resp.json():
                return resp.json()['result']
            else:
                raise ValueError('request did not return either result nor error')
        else:
            raise ValueError('general error while performing post request')


    def logout(self):

        logout_request = {
            "method":"logout",
            "params":[self.token],
            "id":"1",
            "jsonrpc":"2.0"
        }
        resp = self.post_request(self.v3_as, logout_request)
        return resp


    def login(self, username='openbis_test_js', password='password', store_credentials=False):
        """Log into openBIS.
        Expects a username and a password and updates the token (session-ID).
        The token is then used for every request.
        Clients may want to store the credentials object in a credentials store after successful login.
        Throw a ValueError with the error message if login failed.
        """

        login_request = {
            "method":"login",
            "params":[username, password],
            "id":"1",
            "jsonrpc":"2.0"
        }
        result = self.post_request(self.v3_as, login_request)
        self.token = result
        

    def is_token_valid(self):
        """Check if the connection to openBIS is valid.
        This method is useful to check if a token is still valid or if it has timed out,
        requiring the user to login again.
        :return: Return True if the token is valid, False if it is not valid.
        """
        if self.token is None:
            return False

        request = {
            "method": "isSessionActive",
            "params": [ self.token ],
            "id": "1",
            "jsonrpc": "2.0"
        }
        resp = self.post_request(self.v1_as, request)
        return resp


    def get_dataset(self, permid):

        dataset_request = {
            "method": "getDataSets",
            "params": [
                self.token,
                [
                    {
                        "permId": permid,
                        "@type": "as.dto.dataset.id.DataSetPermId"
                    }
                ],
                {
                "parents": {
                    "@type": "as.dto.dataset.fetchoptions.DataSetFetchOptions"
                },
                "children": {
                  "@type": "as.dto.dataset.fetchoptions.DataSetFetchOptions"
                },
                "containers": {
                    "@type": "as.dto.dataset.fetchoptions.DataSetFetchOptions"
                },
                "physicalData": {
                    "@type": "as.dto.dataset.fetchoptions.PhysicalDataFetchOptions"
                },
                "linkedData": {
                    "@type": "as.dto.dataset.fetchoptions.LinkedDataFetchOptions",
                },
                "dataStore": {
                    "@type": "as.dto.datastore.fetchoptions.DataStoreFetchOptions",
                },
                "sample": {
                    "@type": "as.dto.sample.fetchoptions.SampleFetchOptions"
                },
                "properties": {
                    "@type": "as.dto.property.fetchoptions.PropertyFetchOptions"
                },
                "@type": "as.dto.dataset.fetchoptions.DataSetFetchOptions"
                }
            ],
            "id": permid,
            "jsonrpc": "2.0"
        }

        resp = self.post_request(self.v3_as, dataset_request)
        if resp is not None:
            for permid in resp:
                return DataSet(self, permid, resp[permid])


    def get_sample(self, sample_ident):
        """Retrieve metadata for the sample.
        Get metadata for the sample and any directly connected parents of the sample to allow access
        to the same information visible in the ELN UI. The metadata will be on the file system.
        :param sample_identifiers: A list of sample identifiers to retrieve.
        """

        search_request = None

        # assume we got a sample identifier e.g. /TEST/TEST-SAMPLE
        match = re.match('/', sample_ident)
        if match:
            search_request = {
                "identifier": sample_ident,
                "@type": "as.dto.sample.id.SampleIdentifier"
            }
        else:
            # look if we got a PermID eg. 234567654345-123
            match = re.match('\d+\-\d+', sample_ident)
            if match:
                search_request = {
                    "permId": sample_ident,
                    "@type": "as.dto.sample.id.SamplePermId"
                }
            else:
                raise ValueError(
                    '"' + sample_ident + '" is neither a Sample Identifier nor a PermID'
                )

        sample_request = {
            "method": "getSamples",
            "params": [
                self.token,
                [
                    search_request, 
                ],
                {
                    "type": {
                        "@type": "as.dto.sample.fetchoptions.SampleTypeFetchOptions"
                    },
                    "properties": {
                        "@type": "as.dto.property.fetchoptions.PropertyFetchOptions"
                    },
                    "parents": {
                        "@type": "as.dto.sample.fetchoptions.SampleFetchOptions",
                        "properties": {
                            "@type": "as.dto.property.fetchoptions.PropertyFetchOptions"
                        }
                    },
                    "children": {
                        "@type": "as.dto.sample.fetchoptions.SampleFetchOptions",
                        "properties": {
                            "@type": "as.dto.property.fetchoptions.PropertyFetchOptions"
                        }
                    },
                    "dataSets": {
                        "@type": "as.dto.dataset.fetchoptions.DataSetFetchOptions",
                        "properties": {
                            "@type": "as.dto.property.fetchoptions.PropertyFetchOptions"
                        }
                    }
                }
            ],
            "id": sample_ident,
            "jsonrpc": "2.0"
        }
        resp = self.post_request(self.v3_as, sample_request)
        if resp is not None:
            for sample_ident in resp:
                return Sample(self, sample_ident, resp[sample_ident])

        

    def get_samples_with_data(self, sample_identifiers):
        """Retrieve metadata for the sample, like get_sample_metadata, but retrieve any data sets as well,
        like get_data_set.
        :param sample_identifiers: A list of sample identifiers to retrieve.
        """
        pass
        # TODO Implement the logic of this method


    def get_data_sets(self, data_set_identifiers):
        """Retrieve data set metadata and content.
        The metadata will be on the file system. The file will also include the location of the data.
        """
        pass
        # TODO Implement the logic of this method


    def create_data_set_from_notebook(self, path_to_notebook, owner_identifier, paths_to_files,
                                      parent_identifiers):
        """Register a new data set with openBIS.
        :param path_to_notebook: The path to the Jupyter notebook that created this data set
        :param owner_identifier: The identifier of the sample that owns this data set.
        :param paths_to_files: A list of paths to files that should be in the data set.
        :param parent_identifiers: A list of parents for the data set.
        :return:
        """
        pass
        # TODO Implement the logic of this method


class DataSetDownloadQueue:
    
    def __init__(self, workers=20):
        # maximum files to be downloaded at once
        self.download_queue = Queue()

        # define number of threads
        for t in range(workers):
            t = Thread(target=self.download_file)
            t.daemon = True
            t.start()


    def put(self, things):
        """ expects a list [url, filename] which is put into the download queue
        """
        self.download_queue.put(things)

    def join(self):
        """ needs to be called if you want to wait for all downloads to be finished
        """
        self.download_queue.join()


    def download_file(self):
        while True:
            url, filename = self.download_queue.get()
            # create the necessary directory structure if they don't exist yet
            os.makedirs(os.path.dirname(filename), exist_ok=True)

            # request the file in streaming mode
            r = requests.get(url, stream=True)
            with open(filename, 'wb') as f:
                for chunk in r.iter_content(chunk_size=1024): 
                    if chunk: # filter out keep-alive new chunks
                        f.write(chunk)

            self.download_queue.task_done()


class DataSet(Openbis):
    """objects which contain datasets"""

    def __init__(self, openbis_obj, permid, data):
        self.openbis = openbis_obj
        self.permid  = permid
        self.data    = data
        self.v1_ds = '/datastore_server/rmi-dss-api-v1.json'
        self.downloadUrl = self.data['dataStore']['downloadUrl']


    @staticmethod
    def ensure_folder_exists(folder): 
        if not os.path.exists(folder):
            os.makedirs(folder)


    def download(self, wait_until_finished=False, workers=10):
        """ download the actual files and put them in the following folder:
        __current_dir__/hostname/dataset_permid/
        """

        base_url = self.downloadUrl + '/datastore_server/' + self.permid + '/'

        queue = DataSetDownloadQueue(workers=workers)

        # get file list and start download
        for file in self.get_file_list(recursive=True):
            if file['isDirectory']:
                folder = os.path.join(self.openbis.hostname, self.permid)
                DataSet.ensure_folder_exists(folder)
            else:
                download_url = base_url + file['pathInDataSet'] + '?sessionID=' + self.openbis.token 
                filename = os.path.join(self.openbis.hostname, self.permid, file['pathInDataSet'])
                queue.put([download_url, filename])

        # wait until all files have downloaded
        if wait_until_finished:
            queue.join()


    def get_parents(self):
        parents = []
        for item in self.data['parents']:
            parent = self.openbis.get_dataset(item['code'])
            if parent is not None:
                parents.append(parent)
        return parents


    def get_children(self):
        children = []
        for item in self.data['children']:
            child = self.openbis.get_dataset(item['code'])
            if child is not None:
                children.append(child)
        return children
        

    def get_file_list(self, recursive=True, start_folder="/"):
        request = {
            "method" : "listFilesForDataSet",
            "params" : [ 
                self.openbis.token,
                self.permid, 
                start_folder,
                recursive,
            ],
            "id":"1"
        }

        resp = requests.post(self.downloadUrl + self.v1_ds, json.dumps(request))

        if resp.ok:
            if 'error' in resp.json():
                raise ValueError('an error has occured: ' + resp.json()['error'] )
            elif 'result' in resp.json():
                return resp.json()['result']
            else:
                raise ValueError('request did not return either result nor error')
        else:
            raise ValueError('general error while performing post request')


class Sample(Openbis):
    """objects which contain samples"""

    def __init__(self, openbis_obj, ident, data):
        self.openbis = openbis_obj
        self.ident  =  ident
        self.permid = data['permId']['permId']
        self.ident  = data['identifier']['identifier']
        self.data    = data


    def get_parents(self):
        parents = []
        for item in self.data['parents']:
            parent = self.openbis.get_sample(item['permId']['permId'])
            if parent is not None:
                parents.append(parent)
        return parents


    def get_children(self):
        children = []
        for item in self.data['children']:
            child = self.openbis.get_sample(item['permId']['permId'])
            if child is not None:
                children.append(child)
        return children
