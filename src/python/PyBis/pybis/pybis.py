#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
pybis.py

Work with openBIS from Python.

"""

import os
import requests
from requests.packages.urllib3.exceptions import InsecureRequestWarning
requests.packages.urllib3.disable_warnings(InsecureRequestWarning)

import time
import json
import re
from urllib.parse import urlparse
import zlib
import base64
from collections import namedtuple
from pybis.utils import parse_jackson, check_datatype, split_identifier, format_timestamp, is_identifier, is_permid
from pybis.property import PropertyHolder, PropertyAssignments
from pybis.masterdata import Vocabulary


import pandas as pd
from pandas import DataFrame, Series

import threading
from threading import Thread
from queue import Queue
DROPBOX_PLUGIN = "jupyter-uploader-api"


def _definitions(entity):
    entities = {
        "Space": {
            "attrs_new": "code description".split(),
            "attrs_up": "description".split(),
            "attrs": "code permId description registrator registrationDate modificationDate".split(), "identifier": "spaceId",
        },
        "Project": {
            "attrs_new": "code description space attachments".split(),
            "attrs_up": "description space attachments".split(),
            "attrs": "code description permId identifier space leader registrator registrationDate modifier modificationDate attachments".split(),
            "multi": "".split(),
            "identifier": "projectId",
        },
        "Experiment": {
            "attrs_new": "code type project tags attachments".split(),
            "attrs_up": "project tags attachments".split(),
            "attrs": "code permId identifier type project tags attachments".split(),
            "multi": "tags attachments".split(),
            "identifier": "experimentId",
        },
        "Sample": {
            "attrs_new": "code type space project experiment tags attachments".split(),
            "attrs_up": "space project experiment tags attachments".split(),
            "attrs": "code permId identifier type space project experiment tags attachments".split(),
            "ids2type": {
                'parentIds': { 'permId': { '@type': 'as.dto.sample.id.SamplePermId' } },
                'childIds':  { 'permId': { '@type': 'as.dto.sample.id.SamplePermId' } },
                'componentIds': { 'permId': {'@type': 'as.dto.sample.id.SamplePermId' } },
            },
            "identifier": "sampleId",
            "cre_type": "as.dto.sample.create.SampleCreation",
            "multi": "parents children components tags".split(),
        },
        "DataSet": {
            "attrs_new": "type experiment sample parents children container components tags".split(),
            "attrs_up": "experiment sample parents children container components tags".split(),
            "attrs": "code permId type experiment sample parents children container components tags".split(),
            
            "ids2type": {
                'parentIds':     { 'permId': { '@type': 'as.dto.dataset.id.DataSetPermId' } },
                'childIds':      { 'permId': { '@type': 'as.dto.dataset.id.DataSetPermId' } }, 
                'componentIds':  { 'permId': { '@type': 'as.dto.dataset.id.DataSetPermId' } }, 
                'containerIds':  { 'permId': { '@type': 'as.dto.dataset.id.DataSetPermId' } }, 
            },
            "multi": "".split(),
            "identifier": "dataSetId",
        },
        "Material": {
            "attrs_new": "code description type creation tags".split(),
            "attrs" : "code description type creation registrator tags".split()
        },
        "Tag": {
            "attrs_new": "code description experiments samples dataSets materials".split(),
            "attrs": "code description experiments samples dataSets materials registrationDate".split(),
        },
        "attr2ids": {
            "space"      : "spaceId",
            "project"    : "projectId",
            "sample"     : "sampleId",
            "samples"    : "sampleIds",
            "dataSet"    : "dataSetId",
            "dataSets"   : "dataSetIds",
            "experiment" : "experimentId",
            "experiments": "experimentIds",
            "material"   : "materialId",
            "materials"  : "materialIds",
            "container"  : "containerId",
            "component"  : "componentId",
            "components" : "componentIds",
            "parents"    : "parentIds",
            "children"   : "childIds",
            "tags"       : "tagIds",
        },
        "ids2type": {
            'spaceId': { 'permId': { '@type': 'as.dto.space.id.SpacePermId' } },
            'projectId': { 'permId': { '@type': 'as.dto.project.id.ProjectPermId' } },
            'experimentId': { 'permId': { '@type': 'as.dto.experiment.id.ExperimentPermId' } },
            'tagIds': { 'code': { '@type': 'as.dto.tag.id.TagCode' } },
        },
    }
    return entities[entity]


search_criteria = {
    "space":      "as.dto.space.search.SpaceSearchCriteria",
    "project":    "as.dto.project.search.ProjectSearchCriteria",
    "experiment": "as.dto.experiment.search.ExperimentSearchCriteria",
    "sample":     "as.dto.sample.search.SampleSearchCriteria",
    "dataset":    "as.dto.dataset.search.DataSetSearchCriteria",
    "code":       "as.dto.common.search.CodeSearchCriteria",
    "sample_type":"as.dto.sample.search.SampleTypeSearchCriteria",
}

fetch_option = {
    "space":        { "@type": "as.dto.space.fetchoptions.SpaceFetchOptions" },
    "project":      { "@type": "as.dto.project.fetchoptions.ProjectFetchOptions" },
    "experiment":   { "@type": "as.dto.experiment.fetchoptions.ExperimentFetchOptions" },
    "sample":       { "@type": "as.dto.sample.fetchoptions.SampleFetchOptions" },
    "samples":       { "@type": "as.dto.sample.fetchoptions.SampleFetchOptions" },
    "dataSets":    {
        "@type": "as.dto.dataset.fetchoptions.DataSetFetchOptions",
        "properties": { "@type": "as.dto.property.fetchoptions.PropertyFetchOptions" },
        "type": { "@type": "as.dto.dataset.fetchoptions.DataSetTypeFetchOptions" },
    },
    "physicalData": { "@type": "as.dto.dataset.fetchoptions.PhysicalDataFetchOptions" },
    "linkedData":   { "@type": "as.dto.dataset.fetchoptions.LinkedDataFetchOptions" },


    "properties":   { "@type": "as.dto.property.fetchoptions.PropertyFetchOptions" },
    "propertyAssignments" : {
        "@type" : "as.dto.property.fetchoptions.PropertyAssignmentFetchOptions",
        "propertyType": {
            "@type": "as.dto.property.fetchoptions.PropertyTypeFetchOptions"
        }
    },
    "tags":         { "@type": "as.dto.tag.fetchoptions.TagFetchOptions" },

    "registrator":  { "@type": "as.dto.person.fetchoptions.PersonFetchOptions" },
    "modifier":     { "@type": "as.dto.person.fetchoptions.PersonFetchOptions" },
    "leader":       { "@type": "as.dto.person.fetchoptions.PersonFetchOptions" },

    "attachments":  { "@type": "as.dto.attachment.fetchoptions.AttachmentFetchOptions" },
    "attachmentsWithContent": {
        "@type": "as.dto.attachment.fetchoptions.AttachmentFetchOptions",
        "content": {
            "@type": "as.dto.common.fetchoptions.EmptyFetchOptions"
        },
    },
    "history": { "@type": "as.dto.history.fetchoptions.HistoryEntryFetchOptions" },
    "dataStore": { "@type": "as.dto.datastore.fetchoptions.DataStoreFetchOptions" },
}

def search_request_for_identifier(ident, entity):
    search_request = {}

    if is_identifier(ident):
        search_request = {
            "identifier": ident.upper(),
            "@type": "as.dto.{}.id.{}Identifier".format(entity.lower(), entity.capitalize())
        }
    else:
        search_request = {
            "permId": ident,
            "@type": "as.dto.{}.id.{}PermId".format(entity.lower(), entity.capitalize())
        }
    return search_request

def extract_code(obj):
    if not isinstance(obj, dict):
        return str(obj)
    return obj['code']

def extract_deletion(obj):
    del_objs = []
    for deleted_object in obj['deletedObjects']:
        del_objs.append({
            "reason": obj['reason'],
            "permId": deleted_object["id"]["permId"],
            "type": deleted_object["id"]["@type"]
        })
    return del_objs

def extract_identifier(ident):
    if not isinstance(ident, dict): 
        return str(ident)
    return ident['identifier']

def extract_nested_identifier(ident):
    if not isinstance(ident, dict): 
        return str(ident)
    return ident['identifier']['identifier']

def extract_permid(permid):
    if not isinstance(permid, dict):
        return str(permid)
    return permid['permId']

def extract_nested_permid(permid):
    if not isinstance(permid, dict):
        return str(permid)
    return permid['permId']['permId']

def extract_property_assignments(pas):
    pa_strings = []
    for pa in pas:
        if not isinstance(pa['propertyType'], dict):
            pa_strings.append(pa['propertyType'])
        else:
            pa_strings.append(pa['propertyType']['label'])
    return pa_strings

def extract_person(person):
    if not isinstance(person, dict):
        return str(person)
    return person['userId']

def crc32(fileName):
    """since Python3 the zlib module returns unsigned integers (2.7: signed int)
    """
    prev = 0
    for eachLine in open(fileName,"rb"):
        prev = zlib.crc32(eachLine, prev)
    # return as hex
    return "%x"%(prev & 0xFFFFFFFF)

def _create_tagIds(tags=None):
    if tags is None:
        return None
    if not isinstance(tags, list):
        tags = [tags]
    tagIds = []
    for tag in tags:
        tagIds.append({ "code": tag, "@type": "as.dto.tag.id.TagCode" })
    return tagIds

def _tagIds_for_tags(tags=None, action='Add'):
    """creates an action item to add or remove tags. Action is either 'Add', 'Remove' or 'Set'
    """
    if tags is None:
        return
    if not isinstance(tags, list):
        tags = [tags]

    items = []
    for tag in tags:
        items.append({
            "code": tag,
            "@type": "as.dto.tag.id.TagCode"
        })

    tagIds = {
        "actions": [
            {
                "items": items,
                "@type": "as.dto.common.update.ListUpdateAction{}".format(action.capitalize())
            }
        ],
        "@type": "as.dto.common.update.IdListUpdateValue"
    }
    return tagIds

def _list_update(ids=None, entity=None, action='Add'):
    """creates an action item to add, set or remove ids. 
    """
    if ids is None:
        return
    if not isinstance(ids, list):
        ids = [ids]

    items = []
    for ids in ids:
        items.append({
            "code": ids,
            "@type": "as.dto.{}.id.{}Code".format(entity.lower(), entity)
        })

    list_update = {
        "actions": [
            {
                "items": items,
                "@type": "as.dto.common.update.ListUpdateAction{}".format(action.capitalize())
            }
        ],
        "@type": "as.dto.common.update.IdListUpdateValue"
    }
    return list_update 

def _create_typeId(type):
    return {
        "permId": type.upper(),
        "@type": "as.dto.entitytype.id.EntityTypePermId"
    }


def _create_projectId(ident):
    match = re.match('/', ident)
    if match:
        return {
            "identifier": ident,
            "@type": "as.dto.project.id.ProjectIdentifier"
        }
    else:
        return { 
            "permId": ident,
            "@type": "as.dto.project.id.ProjectPermId"
        }

def _common_search(search_type, value, comparison="StringEqualToValue"):
    sreq = {
        "@type": search_type,                                                                                                       
        "fieldValue": {
            "value": value,
            "@type": "as.dto.common.search.{}".format(comparison)                                      
        }   
    }   
    return sreq

def _criteria_for_code(code):
    return {
        "fieldValue": {
            "value": code.upper(),
            "@type": "as.dto.common.search.StringEqualToValue"
        },
        "@type": "as.dto.common.search.CodeSearchCriteria"
    }

def _subcriteria_for_type(code, entity):

    return {
        "@type": "as.dto.{}.search.{}TypeSearchCriteria".format(entity.lower(), entity),
          "criteria": [
            {
              "@type": "as.dto.common.search.CodeSearchCriteria",
              "fieldValue": {
                "value": code.upper(),
                "@type": "as.dto.common.search.StringEqualToValue"
              }
            }
          ]
    }


def _gen_search_criteria(req):
    sreq = {}
    for key, val in req.items():
        if key == "criteria":
            items = []
            for item in req['criteria']:
                items.append(_gen_search_criteria(item))
            sreq['criteria'] = items
        elif key == "code":
            sreq["criteria"] = [_common_search(
                "as.dto.common.search.CodeSearchCriteria", val.upper()
            )]
        elif key == "permid":
            sreq["criteria"] = [_common_search(
                "as.dto.common.search.PermIdSearchCriteria", val
            )]
        elif key == "identifier":
            si = split_identifier(val)
            sreq["criteria"] = []
            if "space" in si:
                sreq["criteria"].append(
                    _gen_search_criteria({ "space": "Space", "code": si["space"] })
                )
            if "experiment" in si:
                pass

            if "code" in si:
                sreq["criteria"].append(
                    _common_search(
                        "as.dto.common.search.CodeSearchCriteria", si["code"].upper()
                    ) 
                )

        elif key == "operator":
           sreq["operator"] = val.upper() 
        else:
            sreq["@type"] = "as.dto.{}.search.{}SearchCriteria".format(key, val)
    return sreq

def _subcriteria_for_tags(tags):
    if not isinstance(tags, list):
        tags = [tags]

    criterias = []
    for tag in tags:
        criterias.append({
            "fieldName": "code",
            "fieldType": "ATTRIBUTE",
            "fieldValue": {
                "value": tag,
                "@type": "as.dto.common.search.StringEqualToValue"
            },
            "@type": "as.dto.common.search.CodeSearchCriteria"
        })

    return {
        "@type": "as.dto.tag.search.TagSearchCriteria",
        "operator": "AND",
        "criteria": criterias
    }

def _subcriteria_for_is_finished(is_finished):
    return {
        "@type": "as.dto.common.search.StringPropertySearchCriteria",
        "fieldName": "FINISHED_FLAG",
        "fieldType": "PROPERTY",
        "fieldValue": {
            "value": is_finished,
            "@type": "as.dto.common.search.StringEqualToValue"
        }
    }

def _subcriteria_for_properties(prop, val):
    return {
        "@type": "as.dto.common.search.StringPropertySearchCriteria",
        "fieldName": prop.upper(),
        "fieldType": "PROPERTY",
        "fieldValue": {
            "value": val,
            "@type": "as.dto.common.search.StringEqualToValue"
        }
    }

def _subcriteria_for_permid(permids, entity, parents_or_children=''):

    if not isinstance(permids, list):
        permids = [permids]

    criterias = []
    for permid in permids:
        criterias.append( {
            "@type": "as.dto.common.search.PermIdSearchCriteria",
            "fieldValue": {
                "value": permid,
                "@type": "as.dto.common.search.StringEqualToValue"
            },
            "fieldType": "ATTRIBUTE",
            "fieldName": "code"
        } )

    criteria = {
        "criteria": criterias,
        "@type": "as.dto.{}.search.{}{}SearchCriteria".format(
            entity.lower(), entity, parents_or_children
        ),
        "operator": "OR"
    }
    return criteria

def _subcriteria_for_code(code, object_type):
    if code is not None:
        if is_permid(code):
            fieldname = "permId"
            fieldtype = "as.dto.common.search.PermIdSearchCriteria"
        else:
            fieldname = "code"
            fieldtype = "as.dto.common.search.CodeSearchCriteria"

        criteria = {
            "criteria": [
                {
                    "fieldName": fieldname,
                    "fieldType": "ATTRIBUTE",
                    "fieldValue": {
                        "value": code.upper(),
                        "@type": "as.dto.common.search.StringEqualToValue"
                    },
                    "@type": fieldtype 
                }
            ],
            "@type": search_criteria[object_type.lower()],
            "operator": "AND"
        }
        return criteria
    else:
        criteria = { "@type": search_criteria[object_type.lower()] }
        return criteria


class Openbis:
    """Interface for communicating with openBIS. A current version of openBIS is needed.
    (minimum version 16.05).
    """

    def __init__(self, url='https://localhost:8443', verify_certificates=True, token=None):
        """Initialize a new connection to an openBIS server.

        :param host:
        """

        url_obj = urlparse(url)
        if  url_obj.netloc is None:
            raise ValueError("please provide the url in this format: https://openbis.host.ch:8443")

        self.url_obj = url_obj
        self.url     = url_obj.geturl()
        self.port    = url_obj.port
        self.hostname = url_obj.hostname
        self.as_v3 = '/openbis/openbis/rmi-application-server-v3.json'
        self.as_v1 = '/openbis/openbis/rmi-general-information-v1.json'
        self.reg_v1 = '/openbis/openbis/rmi-query-v1.json'
        self.verify_certificates = verify_certificates
        self.token = token
        self.datastores = []

        self.dataset_types = None
        self.sample_types = None
        self.files_in_wsp = []
        self.token_path = None

        # use an existing token, if available
        if self.token is None:
            self.token = self._get_cached_token()

    @property
    def spaces(self):
        return self.get_spaces()


    @property
    def projects(self):
        return self.get_projects()


    def _get_cached_token(self):
        """Read the token from the cache, and set the token ivar to it, if there, otherwise None.
        If the token is not valid anymore, delete it. 
        """
        token_path = self.gen_token_path()
        if not os.path.exists(token_path):
            return None
        try:
            with open(token_path) as f:
                token = f.read()
                if not self.is_token_valid(token):
                    os.remove(token_path)
                    return None
                else:
                    return token
        except FileNotFoundError:
            return None


    def gen_token_path(self, parent_folder=None):
        """generates a path to the token file.
        The token is usually saved in a file called
        ~/.pybis/hostname.token
        """
        if parent_folder is None:
            # save token under ~/.pybis folder
            parent_folder = os.path.join(
                os.path.expanduser("~"),
                '.pybis'
            )
        path = os.path.join(parent_folder, self.hostname + '.token')
        return path


    def save_token(self, token=None, parent_folder=None):
        """ saves the session token to the disk, usually here: ~/.pybis/hostname.token. When a new Openbis instance is created, it tries to read this saved token by default.
        """
        if token is None:
            token = self.token

        token_path = None;
        if parent_folder is None:
            token_path = self.gen_token_path()
        else:
            token_path = self.gen_token_path(parent_folder)

        # create the necessary directories, if they don't exist yet
        os.makedirs(os.path.dirname(token_path), exist_ok=True)
        with open(token_path, 'w') as f:
            f.write(token)
            self.token_path = token_path


    def delete_token(self, token_path=None):
        """ deletes a stored session token.
        """
        if token_path is None:
            token_path = self.token_path
        os.remove(token_path)


    def _post_request(self, resource, request):
        """ internal method, used to handle all post requests and serializing / deserializing
        data
        """
        if "id" not in request:
            request["id"] = "1"
        if "jsonrpc" not in request:
            request["jsonrpc"] = "2.0"
        if request["params"][0] is None:
            raise ValueError("Your session expired, please log in again")
        resp = requests.post(
            self.url + resource, 
            json.dumps(request), 
            verify=self.verify_certificates
        )

        if resp.ok:
            resp = resp.json()
            if 'error' in resp:
                print(json.dumps(request))
                raise ValueError('an error has occured: ' + resp['error']['message'] )
            elif 'result' in resp:
                return resp['result']
            else:
                raise ValueError('request did not return either result nor error')
        else:
            raise ValueError('general error while performing post request')


    def logout(self):
        """ Log out of openBIS. After logout, the session token is no longer valid.
        """
        if self.token is None:
            return

        logout_request = {
            "method":"logout",
            "params":[self.token],
        }
        resp = self._post_request(self.as_v3, logout_request)
        self.token = None
        self.token_path = None
        return resp


    def login(self, username=None, password=None, save_token=False):
        """Log into openBIS.
        Expects a username and a password and updates the token (session-ID).
        The token is then used for every request.
        Clients may want to store the credentials object in a credentials store after successful login.
        Throw a ValueError with the error message if login failed.
        """

        login_request = {
            "method":"login",
            "params":[username, password],
        }
        result = self._post_request(self.as_v3, login_request)
        if result is None:
            raise ValueError("login to openBIS failed")
        else:
            self.token = result
            if save_token:
                self.save_token()
            return self.token


    def get_datastores(self):
        """ Get a list of all available datastores. Usually there is only one, but in some cases
        there might be more. If you upload a file, you need to specifiy the datastore you want
        the file uploaded to.
        """
        if len(self.datastores) == 0: 
            request = {
                "method": "listDataStores",
                "params": [ self.token ],
            }
            resp = self._post_request(self.as_v1, request)
            if resp is not None:
                self.datastores = DataFrame(resp)[['code','downloadUrl', 'hostUrl']]
                return self.datastores
            else:
                raise ValueError("No datastore found!")
        else:
            return self.datastores


    def get_spaces(self, code=None):
        """ Get a list of all available spaces (DataFrame object). To create a sample or a
        dataset, you need to specify in which space it should live.
        """
     
        criteria = {}
        options = {}
        request = {
            "method": "searchSpaces",
            "params": [ self.token, 
                criteria,
                options,
            ],
        }
        resp = self._post_request(self.as_v3, request)
        if resp is not None:
            spaces = DataFrame(resp['objects'])
            spaces['registrationDate']= spaces['registrationDate'].map(format_timestamp)
            spaces['modificationDate']= spaces['modificationDate'].map(format_timestamp)
            sp = Things(
                self,
                'space',
                spaces[['code', 'description', 'registrationDate', 'modificationDate']]
            )
            return sp
        else:
            raise ValueError("No spaces found!")


    def get_space(self, spaceId):
        """ Returns a Space object for a given identifier (spaceId).
        """

        spaceId = str(spaceId).upper()
        fetchopts = { "@type": "as.dto.space.fetchoptions.SpaceFetchOptions" }
        for option in ['registrator']:
            fetchopts[option] = fetch_option[option]

        request = {
        "method": "getSpaces",
            "params": [ 
            self.token,
            [{ 
                "permId": spaceId,
                "@type": "as.dto.space.id.SpacePermId"
            }],
            fetchopts
            ],
        } 
        resp = self._post_request(self.as_v3, request)
        if len(resp) == 0:
            raise ValueError("No such space: %s" % spaceId)
        return Space(self, None, resp[spaceId])


    def get_samples(self, code=None, permId=None, space=None, project=None, experiment=None, type=None,
                    withParents=None, withChildren=None, tags=None, **properties):
        """ Get a list of all samples for a given space/project/experiment (or any combination)
        """

        sub_criteria = []
        if space:
            sub_criteria.append(_gen_search_criteria({
                "space": "Space",
                "operator": "AND",
                "code": space
              })
            )
        if project:
            exp_crit = _subcriteria_for_code(experiment, 'experiment')
            proj_crit = _subcriteria_for_code(project, 'project')
            exp_crit['criteria'] = []
            exp_crit['criteria'].append(proj_crit)
            sub_criteria.append(exp_crit)
        if experiment:
            sub_criteria.append(_subcriteria_for_code(experiment, 'experiment'))
        if properties is not None:
            for prop in properties:
                sub_criteria.append(_subcriteria_for_properties(prop, properties[prop]))
        if type:
            sub_criteria.append(_subcriteria_for_code(type, 'sample_type'))
        if tags:
            sub_criteria.append(_subcriteria_for_tags(tags))
        if code:
            sub_criteria.append(_criteria_for_code(code))
        if permId:
            sub_criteria.append(_common_search("as.dto.common.search.PermIdSearchCriteria",permId))
        if withParents:
            if not isinstance(withParents, list):
                withParents = [withParents]
            for parent in withParents:
                sub_criteria.append(
                        _gen_search_criteria({
                        "sample": "SampleParents",
                        "identifier": parent
                    })
                )
        if withChildren:
            if not isinstance(withChildren, list):
                withChildren = [withChildren]
            for child in withChildren:
                sub_criteria.append(
                        _gen_search_criteria({
                        "sample": "SampleChildren",
                        "identifier": child
                    })
                )

        criteria = {
            "criteria": sub_criteria,
            "@type": "as.dto.sample.search.SampleSearchCriteria",
            "operator": "AND"
        }

        options = {
            "properties": { "@type": "as.dto.property.fetchoptions.PropertyFetchOptions" },
            "tags": { "@type": "as.dto.tag.fetchoptions.TagFetchOptions" },
            "registrator": { "@type": "as.dto.person.fetchoptions.PersonFetchOptions" },
            "modifier": { "@type": "as.dto.person.fetchoptions.PersonFetchOptions" },
            "experiment": { "@type": "as.dto.experiment.fetchoptions.ExperimentFetchOptions" },
            "type": { "@type": "as.dto.sample.fetchoptions.SampleTypeFetchOptions" },
            "@type": "as.dto.sample.fetchoptions.SampleFetchOptions",
        }

        request = {
            "method": "searchSamples",
            "params": [ self.token, 
                criteria,
                options,
            ],
        }

        resp = self._post_request(self.as_v3, request)
        if resp is not None:
            objects = resp['objects']
            parse_jackson(objects)

            samples = DataFrame(objects)
            if len(samples) is 0:
                raise ValueError("No samples found!")

            samples['registrationDate']= samples['registrationDate'].map(format_timestamp)
            samples['modificationDate']= samples['modificationDate'].map(format_timestamp)
            samples['registrator'] = samples['registrator'].map(extract_person)
            samples['modifier'] = samples['modifier'].map(extract_person)
            samples['identifier'] = samples['identifier'].map(extract_identifier)
            samples['permId'] = samples['permId'].map(extract_permid)
            samples['experiment'] = samples['experiment'].map(extract_nested_identifier)
            samples['sample_type'] = samples['type'].map(extract_nested_permid)

            ss = samples[['identifier', 'permId', 'experiment', 'sample_type', 'registrator', 'registrationDate', 'modifier', 'modificationDate']]
            return Things(self, 'sample', ss, 'identifier')
        else:
            raise ValueError("No samples found!")

    def get_experiments(self, code=None, type=None, space=None, project=None, tags=None, is_finished=None, **properties):
        """ Get a list of all experiment for a given space or project (or any combination)
        """

        sub_criteria = []
        if space:
            sub_criteria.append(_subcriteria_for_code(space, 'space'))
        if project:
            sub_criteria.append(_subcriteria_for_code(project, 'project'))
        if code:
            sub_criteria.append(_criteria_for_code(code))
        if type:
            sub_criteria.append(_subcriteria_for_type(type, 'Experiment'))
        if tags:
            sub_criteria.append(_subcriteria_for_tags(tags))
        if is_finished is not None:
            sub_criteria.append(_subcriteria_for_is_finished(is_finished))
        if properties is not None:
            for prop in properties:
                sub_criteria.append(_subcriteria_for_properties(prop, properties[prop]))

        criteria = {
            "criteria": sub_criteria,
            "@type": "as.dto.experiment.search.ExperimentSearchCriteria",
            "operator": "AND"
        }
        fetchopts = { "@type": "as.dto.experiment.fetchoptions.ExperimentFetchOptions" }
        for option in ['tags', 'properties', 'registrator', 'modifier', 'project']:
            fetchopts[option] = fetch_option[option]

        request = {
            "method": "searchExperiments",
            "params": [ self.token, 
                criteria,
                fetchopts,
            ],
        }
        resp = self._post_request(self.as_v3, request)
        if len(resp['objects']) == 0:
            raise ValueError("No experiments found!")

        objects = resp['objects']
        parse_jackson(objects)

        experiments = DataFrame(objects)
        experiments['registrationDate']= experiments['registrationDate'].map(format_timestamp)
        experiments['modificationDate']= experiments['modificationDate'].map(format_timestamp)
        experiments['project']= experiments['project'].map(extract_code)
        experiments['registrator'] = experiments['registrator'].map(extract_person)
        experiments['modifier'] = experiments['modifier'].map(extract_person)
        experiments['identifier'] = experiments['identifier'].map(extract_identifier)
        experiments['permId'] = experiments['permId'].map(extract_permid)
        experiments['type'] = experiments['type'].map(extract_code)

        exps = experiments[['identifier', 'permId', 'project', 'type', 'registrator', 
            'registrationDate', 'modifier', 'modificationDate']]
        return Things(self, 'experiment', exps, 'identifier')


    def get_datasets(self, 
        code=None, type=None, withParents=None, withChildren=None,
        sample=None, experiment=None, project=None, tags=None
    ):

        sub_criteria = []

        if code:
            sub_criteria.append(_criteria_for_code(code))
        if type:
            sub_criteria.append(_subcriteria_for_type(type, 'DataSet'))
        if withParents:
            sub_criteria.append(_subcriteria_for_permid(withParents, 'DataSet', 'Parents'))
        if withChildren:
            sub_criteria.append(_subcriteria_for_permid(withChildren, 'DataSet', 'Children'))

        if sample:
            sub_criteria.append(_subcriteria_for_code(sample, 'Sample'))
        if experiment:
            sub_criteria.append(_subcriteria_for_code(experiment, 'Experiment'))
        if project:
            exp_crit = _subcriteria_for_code(experiment, 'Experiment')
            proj_crit = _subcriteria_for_code(project, 'Project')
            exp_crit['criteria'] = []
            exp_crit['criteria'].append(proj_crit)
            sub_criteria.append(exp_crit)
        if tags:
            sub_criteria.append(_subcriteria_for_tags(tags))

        criteria = {
            "criteria": sub_criteria,
            "@type": "as.dto.dataset.search.DataSetSearchCriteria",
            "operator": "AND"
        }

        fetchopts = {
            "containers":   { "@type": "as.dto.dataset.fetchoptions.DataSetFetchOptions" },
            "type":         { "@type": "as.dto.dataset.fetchoptions.DataSetTypeFetchOptions" }
        }

        for option in ['tags', 'properties', 'sample', 'experiment']:
            fetchopts[option] = fetch_option[option]

        request = {
            "method": "searchDataSets",
            "params": [ self.token, 
                criteria,
                fetchopts,
            ],
        }
        resp = self._post_request(self.as_v3, request)
        objects = resp['objects']
        if len(objects) == 0:
            raise ValueError("no datasets found!")
        else:
            parse_jackson(objects)
            datasets = DataFrame(objects)
            datasets['registrationDate']= datasets['registrationDate'].map(format_timestamp)
            datasets['modificationDate']= datasets['modificationDate'].map(format_timestamp)
            datasets['experiment']= datasets['experiment'].map(extract_nested_identifier)
            datasets['sample']= datasets['sample'].map(extract_nested_identifier)
            datasets['type']= datasets['type'].map(extract_code)
            datasets['permId'] = datasets['code']
            ds = Things(
                self,
                'dataset',
                datasets[['permId', 'properties', 'type', 'experiment', 'sample', 'registrationDate', 'modificationDate']],
                'permId'
            )
            return ds


    def get_experiment(self, expId, withAttachments=False):
        """ Returns an experiment object for a given identifier (expId).
        """

        fetchopts = {
            "@type": "as.dto.experiment.fetchoptions.ExperimentFetchOptions",
            "type": {
                "@type": "as.dto.experiment.fetchoptions.ExperimentTypeFetchOptions",
            },
        }

        search_request = search_request_for_identifier(expId, 'experiment')
        for option in ['tags', 'properties', 'attachments', 'project', 'samples']:
            fetchopts[option] = fetch_option[option]

        if withAttachments:
            fetchopts['attachments'] = fetch_option['attachmentsWithContent']

        request = {
        "method": "getExperiments",
            "params": [ 
                self.token,
                [ search_request ],
                fetchopts
            ],
        } 
        resp = self._post_request(self.as_v3, request)
        if len(resp) == 0:
            raise ValueError("No such experiment: %s" % expId)
        return Experiment(self, 
            self.get_experiment_type(resp[expId]["type"]["code"]), 
            resp[expId]
        )


    def new_experiment(self, type, **kwargs):
        """ Creates a new experiment of a given experiment type.
        """
        return Experiment(self, self.get_experiment_type(type), None, **kwargs)


    def update_experiment(self, experimentId, properties=None, tagIds=None, attachments=None):
        params = {
            "experimentId": {
                "permId": experimentId,
                "@type": "as.dto.experiment.id.ExperimentPermId"
            },
            "@type": "as.dto.experiment.update.ExperimentUpdate"
        }
        if properties is not None:
            params["properties"]= properties
        if tagIds is not None:
            params["tagIds"] = tagIds
        if attachments is not None:
            params["attachments"] = attachments

        request = {
            "method": "updateExperiments",
            "params": [
                self.token,
                [ params ]
            ]
        }
        self._post_request(self.as_v3, request)


    def create_sample(self, space_ident, code, type, 
        project_ident=None, experiment_ident=None, properties=None, attachments=None, tags=None):

        tagIds = _create_tagIds(tags)
        typeId = _create_typeId(type)
        projectId = _create_projectId(project_ident)
        experimentId = _create_experimentId(experiment_ident)

        if properties is None:
            properties = {}
        
        request = {
            "method": "createSamples",
            "params": [
                self.token,
                [
                    {
                        "properties": properties,
                        "code": code,
                        "typeId" : typeId,
                        "projectId": projectId,
                        "experimentId": experimentId,
                        "tagIds": tagIds,
                        "attachments": attachments,
                        "@type": "as.dto.sample.create.SampleCreation",
                    }
                ]
            ],
        }
        resp = self._post_request(self.as_v3, request)
        return self.get_sample(resp[0]['permId'])


    def update_sample(self, sampleId, space=None, project=None, experiment=None,
        parents=None, children=None, components=None, properties=None, tagIds=None, attachments=None):
        params = {
            "sampleId": {
                "permId": sampleId,
                "@type": "as.dto.sample.id.SamplePermId"
            },
            "@type": "as.dto.sample.update.SampleUpdate"
        }
        if space is not None:
            params['spaceId'] = space
        if project is not None:
            params['projectId'] = project
        if properties is not None:
            params["properties"]= properties
        if tagIds is not None:
            params["tagIds"] = tagIds
        if attachments is not None:
            params["attachments"] = attachments

        request = {
            "method": "updateSamples",
            "params": [
                self.token,
                [ params ]
            ]
        }
        self._post_request(self.as_v3, request)


    def delete_entity(self, entity, permid, reason):
        """Deletes Spaces, Projects, Experiments, Samples and DataSets
        """

        entity_type = "as.dto.{}.id.{}PermId".format(entity.lower(), entity.capitalize())
        request = {
            "method": "delete" + entity.capitalize()  + 's',
            "params": [
                self.token,
                [
                    {
                        "permId": permid,
                        "@type": entity_type
                    }
                ],
                {
                    "reason": reason,
                    "@type": "as.dto.{}.delete.{}DeletionOptions".format(entity.lower(), entity.capitalize())
                }
            ]
        }
        self._post_request(self.as_v3, request)


    def get_deletions(self):
        request = {
            "method": "searchDeletions",
            "params": [
                self.token,
                {},
                {
                    "deletedObjects": {
                        "@type": "as.dto.deletion.fetchoptions.DeletedObjectFetchOptions"
                    }
                }
            ]
        }
        resp = self._post_request(self.as_v3, request)
        objects = resp['objects']
        parse_jackson(objects)

        new_objs = [] 
        for value in objects:
            del_objs = extract_deletion(value)
            if len(del_objs) > 0:
                new_objs.append(*del_objs)

        return DataFrame(new_objs)


    def new_project(self, space, code, description=None, **kwargs):
        return Project(self, None, space=space, code=code, description=description, **kwargs)
        #request = {
        #    "method": "createProjects",
        #    "params": [
        #        self.token,
        #        [
        #            {
        #                "code": code,
        #                "spaceId": {
        #                    "permId": space_code,
        #                    "@type": "as.dto.space.id.SpacePermId"
        #                },
        #                "@type": "as.dto.project.create.ProjectCreation",
        #                "description": description,
        #                "attachments": None
        #            }
        #        ]
        #    ],
        #}
        #resp = self._post_request(self.as_v3, request)
        #return resp

    def _gen_fetchoptions(self, options):
        fo = {}
        for option in options:
            fo[option] = fetch_option[option]
        return fo


    def get_project(self, projectId):
        options = ['space', 'registrator', 'modifier', 'attachments']
        if is_identifier(projectId):
            request = self._create_get_request(
                'getProjects', 'project', projectId, options
            )
            resp = self._post_request(self.as_v3, request)
            return Project(self, resp[projectId])

        else:
            search_criteria = _gen_search_criteria({
                'project': 'Project',
                'operator': 'AND',
                'code': projectId
            })
            fo = self._gen_fetchoptions(options)
            request = {
                "method": "searchProjects",
                "params": [self.token, search_criteria, fo]
            }
            resp = self._post_request(self.as_v3, request)
            return Project(self, resp['objects'][0])
            

    def get_projects(self, space=None):
        """ Get a list of all available projects (DataFrame object).
        """

        sub_criteria = []
        if space:
            sub_criteria.append(_subcriteria_for_code(space, 'space'))

        criteria = {
            "criteria": sub_criteria,
            "@type": "as.dto.project.search.ProjectSearchCriteria",
            "operator": "AND"
        }

        fetchopts = { "@type": "as.dto.project.fetchoptions.ProjectFetchOptions" }
        for option in ['registrator', 'modifier', 'leader' ]:
            fetchopts[option] = fetch_option[option]

        request = {
            "method": "searchProjects",
            "params": [ self.token, 
                criteria,
                fetchopts,
            ],
        }

        resp = self._post_request(self.as_v3, request)
        if resp is not None:
            objects = resp['objects']
            parse_jackson(objects)

            projects = DataFrame(objects)
            if len(projects) is 0:
                raise ValueError("No projects found!")

            projects['registrationDate']= projects['registrationDate'].map(format_timestamp)
            projects['modificationDate']= projects['modificationDate'].map(format_timestamp)
            projects['leader'] = projects['leader'].map(extract_person)
            projects['registrator'] = projects['registrator'].map(extract_person)
            projects['modifier'] = projects['modifier'].map(extract_person)
            projects['permId'] = projects['permId'].map(extract_permid)
            projects['identifier'] = projects['identifier'].map(extract_identifier)

            pros=projects[['identifier', 'permId', 'leader', 'registrator', 'registrationDate', 
                            'modifier', 'modificationDate']]
            return Things(self, 'project', pros, 'identifier')
        else:
            raise ValueError("No projects found!")


    def _create_get_request(self, method_name, entity, permids, options):

        if not isinstance(permids, list):
            permids = [permids]

        type = "as.dto.{}.id.{}".format(entity.lower(), entity.capitalize())
        search_params = []
        for permid in permids:
            # decide if we got a permId or an identifier
            match = re.match('/', permid)
            if match:
                search_params.append(
                    { "identifier" : permid, "@type" : type + 'Identifier' }
                )
            else: 
                search_params.append(
                    { "permId" : permid, "@type": type + 'PermId' }
                )

        fo = {}
        for option in options:
            fo[option] = fetch_option[option]

        request = {
            "method": method_name,
            "params": [
                self.token,
                search_params,
                fo
            ],
        }
        return request


    def get_terms(self, vocabulary=None):
        """ Returns information about vocabulary, including its controlled vocabulary
        """

        search_request = {}
        if vocabulary is not None:
            search_request = _gen_search_criteria( { 
                "vocabulary": "VocabularyTerm", 
                "criteria" : [{
                    "vocabulary": "Vocabulary",
                    "code": vocabulary
                }]
            })
    
        fetch_options = {
            "vocabulary" : { "@type" : "as.dto.vocabulary.fetchoptions.VocabularyFetchOptions" },
            "@type": "as.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions"
        }

        request = {
            "method": "searchVocabularyTerms",
            "params": [ self.token, search_request, fetch_options ]
        }
        resp = self._post_request(self.as_v3, request)
        parse_jackson(resp)
        return Vocabulary(resp)

    def get_tags(self):
        """ Returns a DataFrame of all 
        """
        request = {
            "method": "searchTags",
            "params": [ self.token, {}, {} ]
        }
        resp = self._post_request(self.as_v3, request)
        parse_jackson(resp)
        objects = DataFrame(resp['objects'])
        objects['registrationDate'] = objects['registrationDate'].map(format_timestamp)
        return objects[['code', 'registrationDate']]


    def get_sample_types(self, type=None):
        """ Returns a list of all available sample types
        """
        return self._get_types_of(
            "searchSampleTypes",
            "Sample",
            type, 
            ["generatedCodePrefix"]
        )

    def get_sample_type(self, type):
        try:
            return self._get_types_of(
                "searchSampleTypes", 
                "Sample",
                type,
                ["generatedCodePrefix"]
            )
        except Exception:
            raise ValueError("no such sample type: {}".format(type))


    def get_experiment_types(self, type=None):
        """ Returns a list of all available experiment types
        """
        return self._get_types_of(
            "searchExperimentTypes", 
            "Experiment", 
            type
        )

    def get_experiment_type(self, type):
        try:    
            return self._get_types_of(
                "searchExperimentTypes", 
                "Experiment", 
                type
            )
        except Exception:
            raise ValueError("No such experiment type: {}".format(type))


    def get_material_types(self, type=None):
        """ Returns a list of all available material types
        """
        return self._get_types_of("searchMaterialTypes", "Material", type)

    def get_material_type(self, type):
        try:
            return self._get_types_of("searchMaterialTypes", "Material", type)
        except Exception:
            raise ValueError("No such material type: {}".format(type))


    def get_dataset_types(self, type=None):
        """ Returns a list (DataFrame object) of all currently available dataset types
        """
        return self._get_types_of("searchDataSetTypes", "DataSet", type, ['kind'] )

    def get_dataset_type(self, type):
        try:
            return self._get_types_of("searchDataSetTypes", "DataSet", type, ['kind'])
        except Exception:
            raise ValueError("No such dataSet type: {}".format(type))


    def _get_types_of(self, method_name, entity, type_name=None, additional_attributes=None):
        """ Returns a list of all available types of an entity.
        If the name of the entity-type is given, it returns a PropertyAssignments object
        """
        if additional_attributes is None:
            additional_attributes = []

        attributes = ['code', 'description', *additional_attributes, 'modificationDate']

        search_request = {}
        fetch_options = {}

        if type_name is not None:
            search_request = _gen_search_criteria({
                entity.lower(): entity + "Type",
                "operator": "AND",
                "code": type_name
            })

            fetch_options = {
                "@type": "as.dto.{}.fetchoptions.{}TypeFetchOptions".format(
                    entity.lower(), entity
                )
            }
            fetch_options['propertyAssignments'] = fetch_option['propertyAssignments']
            attributes.append('propertyAssignments')
        
        request = {
            "method": method_name,
            "params": [ self.token, search_request, fetch_options ],
        }
        resp = self._post_request(self.as_v3, request)
        parse_jackson(resp)

        if type_name is not None and len(resp['objects']) == 1:
            return PropertyAssignments(self, resp['objects'][0])
        if len(resp['objects']) >= 1:
            types = DataFrame(resp['objects'])
            types['modificationDate'] = types['modificationDate'].map(format_timestamp)
            return Things(self, entity.lower()+'_type', types[attributes])
            return types[attributes]
            
        else:
            raise ValueError("Nothing found!")


    def is_session_active(self):
        """ checks whether a session is still active. Returns true or false.
        """
        return self.is_token_valid(self.token)


    def is_token_valid(self, token=None):
        """Check if the connection to openBIS is valid.
        This method is useful to check if a token is still valid or if it has timed out,
        requiring the user to login again.
        :return: Return True if the token is valid, False if it is not valid.
        """
        if token is None:
            token = self.token
        
        if token is None:
            return False

        request = {
            "method": "isSessionActive",
            "params": [ token ],
        }
        resp = self._post_request(self.as_v1, request)
        return resp


    def get_dataset(self, permid):
        """fetch a dataset and some metadata attached to it:
        - properties
        - sample
        - parents
        - children
        - containers
        - dataStore
        - physicalData
        - linkedData
        :return: a DataSet object
        """

        criteria = [{
            "permId": permid,
            "@type": "as.dto.dataset.id.DataSetPermId"
        }]

        fetchopts = {
            "parents":      { "@type": "as.dto.dataset.fetchoptions.DataSetFetchOptions" },
            "children":     { "@type": "as.dto.dataset.fetchoptions.DataSetFetchOptions" },
            "containers":   { "@type": "as.dto.dataset.fetchoptions.DataSetFetchOptions" },
            "type":         { "@type": "as.dto.dataset.fetchoptions.DataSetTypeFetchOptions" },
        }

        for option in ['tags', 'properties', 'dataStore', 'physicalData', 'linkedData', 
                       'experiment', 'sample']:
            fetchopts[option] = fetch_option[option]

        request = {
            "method": "getDataSets",
            "params": [ self.token, 
                criteria,
                fetchopts,
            ],
        }

        resp = self._post_request(self.as_v3, request)
        if resp is None or len(resp) == 0:
            raise ValueError('no such dataset found: '+permid)
        if resp is not None:
            for permid in resp:
                #return resp[permid]
                return DataSet(self, self.get_dataset_type(resp[permid]["type"]["code"]), resp[permid])


    def get_sample(self, sample_ident, only_data=False, withAttachments=False):
        """Retrieve metadata for the sample.
        Get metadata for the sample and any directly connected parents of the sample to allow access
        to the same information visible in the ELN UI. The metadata will be on the file system.
        :param sample_identifiers: A list of sample identifiers to retrieve.
        """

        fetchopts = { "type": { "@type": "as.dto.sample.fetchoptions.SampleTypeFetchOptions" } }

        search_request = search_request_for_identifier(sample_ident, 'sample')

        for option in ['tags', 'properties', 'attachments', 'space', 'experiment', 'registrator', 'dataSets']:
            fetchopts[option] = fetch_option[option]

        if withAttachments:
            fetchopts['attachments'] = fetch_option['attachmentsWithContent']

        #fetchopts["parents"]  = { "@type": "as.dto.sample.fetchoptions.SampleFetchOptions" }
        #fetchopts["children"] = { "@type": "as.dto.sample.fetchoptions.SampleFetchOptions" }

        sample_request = {
            "method": "getSamples",
            "params": [
                self.token,
                [ search_request ],
                fetchopts
            ],
        }

        resp = self._post_request(self.as_v3, sample_request)
        parse_jackson(resp)

        if resp is None or len(resp) == 0:
            raise ValueError('no such sample found: '+sample_ident)
        else:
            for sample_ident in resp:
                if only_data:
                    return resp[sample_ident]
                else:
                    return Sample(self, self.get_sample_type(resp[sample_ident]["type"]["code"]), resp[sample_ident])


    def new_space(self, name, description=None):
        """ Creates a new space in the openBIS instance.
        """
        request = {
            "method": "createSpaces",
            "params": [
                self.token,
                [ {
                    "code": name,
                    "description": description,
                    "@type": "as.dto.space.create.SpaceCreation"
                } ]
            ],
        }
        resp = self._post_request(self.as_v3, request)
        return self.get_space(name)


    def new_analysis(self, name, description=None, sample=None, dss_code=None, result_files=None,
    notebook_files=None, parents=None):

        """ An analysis contains the Jupyter notebook file(s) and some result files.
            Technically this method involves uploading files to the session workspace
            and activating the dropbox aka dataset ingestion service "jupyter-uploader-api"
        """

        if dss_code is None:
            dss_code = self.get_datastores()['code'][0]

        # if a sample identifier was given, use it as a string.
        # if a sample object was given, take its identifier
        sampleId = None
        if isinstance(sample, str):
            if (is_identifier(sample)):
                sampleId = { 
                    "identifier": sample,
                    "@type": "as.dto.sample.id.SampleIdentifier"
                }
            else:
                sampleId = { 
                    "permId": sample,
                    "@type": "as.dto.sample.id.SamplePermId"
                }
        else:
            sampleId = { 
                "identifier": sample.identifier,
                "@type": "as.dto.sample.id.SampleIdentifier"
            }

        parentIds = []
        if parents is not None:
            if not isinstance(parents, list):
                parants = [parents]
            for parent in parents:
                parentIds.append(parent.permId)
        
        datastore_url = self._get_dss_url(dss_code)
        folder = time.strftime('%Y-%m-%d_%H-%M-%S')

        # upload the files
        data_sets = []
        if notebook_files is not None:
            notebooks_folder = os.path.join(folder, 'notebook_files')
            self.upload_files(
                datastore_url = datastore_url,
                files=notebook_files,
                folder= notebooks_folder, 
                wait_until_finished=True
            )
            data_sets.append({
                "dataSetType" : "JUPYTER_NOTEBOOk",
                "sessionWorkspaceFolder": notebooks_folder,
                "fileNames" : notebook_files,
                "properties" : {}
            })
        if result_files is not None:
            results_folder = os.path.join(folder, 'result_files')
            self.upload_files(
                datastore_url = datastore_url,
                files=result_files,
                folder=results_folder,
                wait_until_finished=True
            )
            data_sets.append({
                "dataSetType" : "JUPYTER_RESULT",
                "sessionWorkspaceFolder" : results_folder,
                "fileNames" : result_files,
                "properties" : {}
            })

        # register the files in openBIS
        request = {
          "method": "createReportFromAggregationService",
          "params": [
            self.token,
            dss_code,
            DROPBOX_PLUGIN,
            { 
                "sample" : { "identifier" : sampleId['identifier'] },
                "sampleId": sampleId,
                "parentIds": parentIds,
                "containers" : [ {
                    "dataSetType" : "JUPYTER_CONTAINER",
                    "properties" : {
                        "NAME" : name,
                        "DESCRIPTION" : description
                    }
                } ],
                "dataSets" : data_sets,
            }
          ],
        }
        
        resp = self._post_request(self.reg_v1, request)
        try:
            if resp['rows'][0][0]['value'] == 'OK':
                return resp['rows'][0][1]['value']
        except:
            return resp


    def new_sample(self, type, **kwargs):
        """ Creates a new sample of a given sample type.
        """
        return Sample(self, self.get_sample_type(type), None, **kwargs)


    def new_dataset(self, type, **kwargs):
        """ Creates a new dataset of a given sample type.
        """
        return DataSet(self, self.get_dataset_type(type.upper()), None, **kwargs)


    def _get_dss_url(self, dss_code=None):
        """ internal method to get the downloadURL of a datastore.
        """
        dss = self.get_datastores()
        if dss_code is None:
            return dss['downloadUrl'][0]
        else:
            return dss[dss['code'] == dss_code]['downloadUrl'][0]
        


    def upload_files(self, datastore_url=None, files=None, folder=None, wait_until_finished=False):

        if datastore_url is None:
            datastore_url = self._get_dss_url()

        if files is None:
            raise ValueError("Please provide a filename.")

        if folder is None:
            # create a unique foldername
            folder = time.strftime('%Y-%m-%d_%H-%M-%S')

        if isinstance(files, str):
            files = [files]

        self.files = files
        self.startByte = 0
        self.endByte   = 0
    
        # define a queue to handle the upload threads
        queue = DataSetUploadQueue()

        real_files = []
        for filename in files:
            if os.path.isdir(filename):
                real_files.extend([os.path.join(dp, f) for dp, dn, fn in os.walk(os.path.expanduser(filename)) for f in fn])
            else:
                real_files.append(os.path.join(filename))

        # compose the upload-URL and put URL and filename in the upload queue 
        for filename in real_files:
            file_in_wsp = os.path.join(folder, filename)
            self.files_in_wsp.append(file_in_wsp)
            upload_url = (
                datastore_url + '/session_workspace_file_upload'
                + '?filename=' + os.path.join(folder,filename)
                + '&id=1'
                + '&startByte=0&endByte=0'
                + '&sessionID=' + self.token
            )
            queue.put([upload_url, filename, self.verify_certificates])

        # wait until all files have uploaded
        if wait_until_finished:
            queue.join()

        # return files with full path in session workspace
        return self.files_in_wsp


class DataSetUploadQueue():
   
    def __init__(self, workers=20):
        # maximum files to be uploaded at once
        self.upload_queue = Queue()

        # define number of threads and start them
        for t in range(workers):
            t = Thread(target=self.upload_file)
            t.daemon = True
            t.start()


    def put(self, things):
        """ expects a list [url, filename] which is put into the upload queue
        """
        self.upload_queue.put(things)


    def join(self):
        """ needs to be called if you want to wait for all uploads to be finished
        """
        self.upload_queue.join()


    def upload_file(self):
        while True:
            # get the next item in the queue
            upload_url, filename, verify_certificates = self.upload_queue.get()

            filesize = os.path.getsize(filename)

            # upload the file to our DSS session workspace
            with open(filename, 'rb') as f:
                resp = requests.post(upload_url, data=f, verify=verify_certificates)
                resp.raise_for_status()
                data = resp.json()
                assert filesize == int(data['size'])

            # Tell the queue that we are done
            self.upload_queue.task_done()


class DataSetDownloadQueue():
    
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
            url, filename, file_size, verify_certificates = self.download_queue.get()
            # create the necessary directory structure if they don't exist yet
            os.makedirs(os.path.dirname(filename), exist_ok=True)

            # request the file in streaming mode
            r = requests.get(url, stream=True, verify=verify_certificates)
            with open(filename, 'wb') as f:
                for chunk in r.iter_content(chunk_size=1024): 
                    if chunk: # filter out keep-alive new chunks
                        f.write(chunk)

            assert os.path.getsize(filename) == int(file_size)
            self.download_queue.task_done()


class OpenBisObject():

    def __init__(self, openbis_obj, type, data=None, **kwargs):
        self.__dict__['openbis'] = openbis_obj
        self.__dict__['type'] = type
        self.__dict__['p'] = PropertyHolder(openbis_obj, type)
        self.__dict__['a'] = AttrHolder(openbis_obj, 'DataSet', type)

        # existing OpenBIS object
        if data is not None:
            self._set_data(data)

        if kwargs is not None:
            for key in kwargs:
                setattr(self, key, kwargs[key])

    def __eq__(self, other):
        return str(self) == str(other)

    def __ne__(self, other):
        return str(self) != str(other)

    def _set_data(self, data):
            # assign the attribute data to self.a by calling it 
            # (invoking the AttrHolder.__call__ function)
            self.a(data)
            self.__dict__['data'] = data

            # put the properties in the self.p namespace (without checking them)
            for key, value in data['properties'].items():
                self.p.__dict__[key.lower()] = value

    @property
    def space(self):
        try: 
            return self.openbis.get_space(self._space['permId'])
        except Exception:
            pass

    @property
    def project(self):
        try: 
            return self.openbis.get_project(self._project['identifier'])
        except Exception:
            pass

    @property
    def experiment(self):
        try: 
            return self.openbis.get_experiment(self._experiment['identifier'])
        except Exception:
            pass

    @property
    def sample(self):
        try:
            return self.openbis.get_sample(self._sample['permId']['permId'])
        except Exception:
            pass

    def __getattr__(self, name):
        return getattr(self.__dict__['a'], name)

    def __setattr__(self, name, value):
        if name in ['set_properties', 'set_tags', 'add_tags']:
            raise ValueError("These are methods which should not be overwritten")

        setattr(self.__dict__['a'], name, value)

    def _repr_html_(self):
        """Print all the assigned attributes (identifier, tags, etc.) in a nicely formatted table. See
        AttributeHolder class.
        """
        html = self.a._repr_html_()
        return html


class DataSet(OpenBisObject):
    """ DataSet are openBIS objects that contain the actual files.
    """

    def __init__(self, openbis_obj, type, data=None, **kwargs):
        super(DataSet, self).__init__(openbis_obj, type, data, **kwargs)

        # existing DataSet
        if data is not None:
            if data['physicalData'] is None:
                self.__dict__['shareId'] = None
                self.__dict__['location'] = None
            else:
                self.__dict__['shareId'] = data['physicalData']['shareId']
                self.__dict__['location'] = data['physicalData']['location']

    def __str__(self):
        return self.data['code']

    def __dir__(self):
        return [
            'props', 'get_parents()', 'get_children()',
            'sample', 'experiment', 
            'tags', 'set_tags()', 'add_tags()', 'del_tags()',
            'add_attachment()', 'get_attachments()', 'download_attachments()',
            'data'
        ]

    @property
    def type(self):
        return self.__dict__['type']

    @type.setter
    def type(self, type_name):
            dataset_type = self.openbis.get_dataset_type(type_name.upper())
            self.p.__dict__['_type'] = dataset_type
            self.a.__dict__['_type'] = dataset_type


    def set_properties(self, properties):
        self.openbis.update_dataset(self.permId, properties=properties)

    def download(self, files=None, wait_until_finished=True, workers=10):
        """ download the actual files and put them by default in the following folder:
        __current_dir__/hostname/dataset_permId/
        If no files are specified, all files of a given dataset are downloaded.
        Files are usually downloaded in parallel, using 10 workers by default. If you want to wait until
        all the files are downloaded, set the wait_until_finished option to True.
        """

        if files == None:
            files = self.file_list()
        elif isinstance(files, str):
            files = [files]

        base_url = self.data['dataStore']['downloadUrl'] + '/datastore_server/' + self.permId + '/'

        queue = DataSetDownloadQueue(workers=workers)

        # get file list and start download
        for filename in files:
            file_info = self.get_file_list(start_folder=filename)
            file_size = file_info[0]['fileSize']
            download_url = base_url + filename + '?sessionID=' + self.openbis.token 
            filename = os.path.join(self.openbis.hostname, self.permId, filename)
            queue.put([download_url, filename, file_size, self.openbis.verify_certificates])

        # wait until all files have downloaded
        if wait_until_finished:
            queue.join()


        print("Files downloaded to: %s" % os.path.join(self.openbis.hostname, self.permId))


    def get_parents(self):
        return self.openbis.get_datasets(withChildren=self.permId)

    def get_children(self):
        return self.openbis.get_datasets(withParents=self.permId)


    def file_list(self):
        """returns the list of files including their directories as an array of strings. Just folders are not
        listed.
        """
        files = []
        for file in self.get_file_list(recursive=True):
            if file['isDirectory']:
                pass
            else:
                files.append(file['pathInDataSet'])
        return files


    def get_files(self, start_folder='/'):
        """Returns a DataFrame of all files in this dataset
        """

        def createRelativePath(pathInDataSet):
            if self.shareId is None:
                return ''
            else:
                return os.path.join(self.shareId, self.location, pathInDataSet)

        def signed_to_unsigned(sig_int):
            """openBIS delivers crc32 checksums as signed integers.
            If the number is negative, we just have to add 2**32
            We display the hex number to match with the classic UI
            """
            if sig_int < 0:
                sig_int += 2**32
            return "%x"%(sig_int & 0xFFFFFFFF)
            
        files = self.get_file_list(start_folder=start_folder)
        df = DataFrame(files)
        df['relativePath'] = df['pathInDataSet'].map(createRelativePath)
        df['crc32Checksum'] = df['crc32Checksum'].fillna(0.0).astype(int).map(signed_to_unsigned)
        return df[['isDirectory', 'pathInDataSet', 'fileSize', 'crc32Checksum']]
        

    def get_file_list(self, recursive=True, start_folder="/"):
        """Lists all files of a given dataset. You can specifiy a start_folder other than "/".
        By default, all directories and their containing files are listed recursively. You can
        turn off this option by setting recursive=False.
        """
        request = {
            "method" : "listFilesForDataSet",
            "params" : [ 
                self.openbis.token,
                self.permId, 
                start_folder,
                recursive,
            ],
           "id":"1"
        }

        resp = requests.post(
            self.data["dataStore"]["downloadUrl"] + '/datastore_server/rmi-dss-api-v1.json',
            json.dumps(request), 
            verify=self.openbis.verify_certificates
        )

        if resp.ok:
            data = resp.json()
            if 'error' in data:
                raise ValueError('Error from openBIS: ' + data['error'] )
            elif 'result' in data:
                return data['result']
            else:
                raise ValueError('request to openBIS did not return either result nor error')
        else:
            raise ValueError('internal error while performing post request')


class AttrHolder():
    """ General class for both samples and experiments that hold all common attributes, such as:
    - space
    - project
    - experiment (sample)
    - samples (experiment)
    - dataset
    - parents (sample, dataset)
    - children (sample, dataset)
    - tags
    """

    def __init__(self, openbis_obj, entity, type=None):
        self.__dict__['_openbis'] = openbis_obj
        self.__dict__['_entity'] = entity

        if type is not None:
            self.__dict__['_type'] = type.data

        self.__dict__['_allowed_attrs'] = _definitions(entity)['attrs']
        self.__dict__['_identifier'] = None
        self.__dict__['_is_new'] = True


    def __call__(self, data):
        self.__dict__['_is_new'] = False
        for attr in self._allowed_attrs:
            if attr in ["code","permId","identifier",
                    "type", "container","components"]:
                self.__dict__['_'+attr] = data.get(attr, None)

            elif attr in ["space"]:
                d =  data.get(attr, None)
                if d is not None:
                    d = d['permId']
                self.__dict__['_'+attr] = d

            elif attr in ["sample", "experiment", "project"]:
                d =  data.get(attr, None)
                if d is not None:
                    d = d['identifier']
                self.__dict__['_'+attr] = d

            elif attr in ["parents","children","samples"]:
                self.__dict__['_'+attr] = []
                for item in data[attr]:
                    if 'identifier' in item:
                        self.__dict__['_'+attr].append(item['identifier'])
                    elif 'permId' in item:
                        self.__dict__['_'+attr].append(item['permId'])

            elif attr in ["tags"]:
                tags = []
                for item in data[attr]:
                    tags.append({
                        "code": item['code'],
                        "@type": "as.dto.tag.id.TagCode"
                    })
                self.__dict__['_tags'] = tags
                import copy
                self.__dict__['_prev_tags'] = copy.deepcopy(tags)
            else:
                self.__dict__['_'+attr] = data.get(attr, None)


    def _new_attrs(self):
        defs = _definitions(self.entity)
        attr2ids = _definitions('attr2ids')

        new_obj = {
           "@type" : "as.dto.{}.create.{}Creation".format(self.entity.lower(), self.entity)
        } 

        for attr in defs['attrs_new']:
            items = None

            if attr == 'type':
                new_obj['typeId'] = self._type['permId']
                continue

            elif attr == 'attachments':
                attachments = getattr(self,'_new_attachments')
                if attachments is None:
                    continue
                atts_data = [ attachment.get_data() for attachment in attachments ]
                items = atts_data

            elif attr in defs['multi']:
                items = getattr(self, '_'+attr)
                if items is None:
                    items = []
            else:
                items = getattr(self, '_'+attr)

            key = None
            if attr in attr2ids:
                key = attr2ids[attr]
            else:
                key = attr
            
            new_obj[key] = items


        # create a new entity
        request = { 
            "method": "create{}s".format(self.entity),
            "params": [
                self.openbis.token,
                [ new_obj ]
            ]
        }
        return request


    def _up_attrs(self):
        defs = _definitions(self._entity)
        attr2ids = _definitions('attr2ids')

        up_obj = {
           "@type" : "as.dto.{}.update.{}Update".format(self.entity.lower(), self.entity),
           defs["identifier"]: self._permId
        } 

        # look at all attributes available for that entity
        # that can be updated
        for attr in defs['attrs_up']:
            items = None

            if attr == 'attachments':
                # v3 API currently only supports adding attachments
                attachments = self.__dict__.get('_new_attachments', None)
                if attachments is None:
                    continue
                atts_data = [ attachment.get_data() for attachment in attachments ]

                if self._is_new:
                    up_obj['attachments'] = atts_data
                else:
                    up_obj['attachments'] = {
                        "actions": [ {
                            "items": atts_data,
                            "@type": "as.dto.common.update.ListUpdateActionAdd"
                  } ],
                        "@type": "as.dto.attachment.update.AttachmentListUpdateValue"
                    } 

            elif attr == 'tags':
                # look which tags have been added or removed and update them
                if getattr(self,'_prev_tags') is None:
                    self.__dict__['_prev_tags'] = []
                actions = []
                for tagId in self._prev_tags:
                    if tagId not in self._tags:
                        actions.append({
                            "items": [ tagId ],
                            "@type": "as.dto.common.update.ListUpdateActionRemove"
                        })

                for tagId in self._tags:
                    if tagId not in self._prev_tags:
                        actions.append({
                            "items": [ tagId ],
                            "@type": "as.dto.common.update.ListUpdateActionAdd"
                        })
                    
                up_obj['tagIds'] = {
                    "@type": "as.dto.common.update.IdListUpdateValue",
                    "actions": actions
                }

            elif '_'+attr in self.__dict__:
                # handle multivalue attributes (parents, children, tags etc.)
                # we only cover the Set mechanism, which means we always update 
                # all items in a list
                if attr in defs['multi']:
                    items = self.__dict__.get('_'+attr, [])
                    if items == None:
                        items = []
                    up_obj[attr2ids[attr]] = {
                        "actions": [
                            {
                                "items": items,
                                "@type": "as.dto.common.update.ListUpdateActionSet",
                            }
                        ],
                        "@type": "as.dto.common.update.IdListUpdateValue"
                    }
                else:
                    # handle single attributes (space, experiment, project, container, etc.)
                    value =  self.__dict__.get('_'+attr, {})
                    if value is None:
                        pass
                    else:
                        isModified=False
                        if 'isModified' in value:
                            isModified=True
                            del value['isModified']
                        
                        up_obj[attr2ids[attr]] = {
                           "@type": "as.dto.common.update.FieldUpdateValue",
                           "isModified": isModified,
                           "value": value,
                        }

        # update a new entity
        request = { 
            "method": "update{}s".format(self.entity),
            "params": [
                self.openbis.token,
                [ up_obj ]
            ]
        }
        return request


    def __getattr__(self, name):
        """ handles all attribute requests dynamically. Values are returned in a sensible way,
            for example the identifiers of parents, children and components are returned
            as an array of values.
        """
                
        int_name = '_'+name
        if int_name in self.__dict__: 
            if int_name in ['_attachments']:
                return [
                    { 
                        "fileName": x['fileName'],
                        "title": x['title'],
                        "description": x['description']
                    } for x in self._attachments 
                ]
            if int_name in ['_registrator','_modifier']:
                return self.__dict__[int_name].get('userId', None)
            elif int_name in ['_registrationDate', '_modificationDate']:
                return format_timestamp(self.__dict__[int_name])
            # if the attribute contains a list, 
            # return a list of either identifiers, codes or
            # permIds (whatever is available first)
            elif isinstance(self.__dict__[int_name], list):
                values = []
                for item in self.__dict__[int_name]:
                    if "identifier" in item:
                        values.append(item['identifier'])
                    elif "code" in item:
                        values.append(item['code'])
                    elif "permId" in item:
                        values.append(item['permId'])
                    else:
                        pass
                return values
            # attribute contains a dictionary: same procedure as above.
            elif isinstance(self.__dict__[int_name], dict):
                if "identifier" in self.__dict__[int_name]:
                    return self.__dict__[int_name]['identifier']
                elif "code" in self.__dict__[int_name]:
                    return self.__dict__[int_name]['code']
                elif "permId" in self.__dict__[int_name]:
                    return self.__dict__[int_name]['permId']
            else:
                return self.__dict__[int_name]
        else:
            return None


    def __setattr__(self, name, value):
        if name in ["parents", "children", "components"]:
            if not isinstance(value, list):
                value = [value]
            objs = []
            for val in value:
                # fetch objects in openBIS, make sure they actually exists
                obj = getattr(self._openbis, 'get_'+self._entity.lower())(val)
                objs.append(obj)
            self.__dict__['_'+name] = {
                "@type": "as.dto.common.update.IdListUpdateValue",
                "actions": [{
                    "@type": "as.dto.common.update.ListUpdateActionSet",
                    "items": [item._permId for item in objs]
                }]
            }
        elif name in ["tags"]:
            self.set_tags(value)
        
        elif name in ["attachments"]:
            if isinstance(value, list):
                for item in value:
                    if isinstance(item, dict):
                        self.add_attachment(**item)
                    else:
                        self.add_attachment(item)

            else:
                self.add_attachment(value)

        elif name in ["sample", "experiment", "space", "project"]:
            obj = None
            if isinstance(value, str):
                # fetch object in openBIS, make sure it actually exists
                obj = getattr(self._openbis, "get_"+name)(value)
            else:
                obj = value

            self.__dict__['_'+name] = obj._identifier

            # mark attribute as modified, if it's an existing entity
            if self.is_new:
                pass
            else:
                self.__dict__['_'+name]['isModified'] = True

        elif name in ["identifier"]:
            raise KeyError("you can not modify the {}".format(name))
        elif name == "code":
            try:
                if self._type.data['autoGeneratedCode']:
                    raise KeyError("for this {}Type you can not set a code".format(self.entity))
            except AttributeError:
                pass
                
            self.__dict__['_code'] = value

        elif name == "description":
            self.__dict__['_description'] = value
        else:
            raise KeyError("no such attribute: {}".format(name))

    def get_type(self):
        return self._type

    def get_parents(self):
        # e.g. self._openbis.get_samples(withChildren=self.identifier)
        return getattr(self._openbis, 'get_'+self._entity.lower()+'s')(withChildren=self.identifier)

    def get_children(self):
        # e.g. self._openbis.get_samples(withParents=self.identifier)
        return getattr(self._openbis, 'get_'+self._entity.lower()+'s')(withParents=self.identifier)

    @property
    def tags(self):
        if getattr(self, '_tags') is not None:
            return [ x['code'] for x in self._tags ]

    def set_tags(self, tags):
        if getattr(self, '_tags') is None:
            self.__dict__['_tags'] = []

        tagIds = _create_tagIds(tags)

        # remove tags that are not in the new tags list
        for tagId in self.__dict__['_tags']:
            if tagId not in tagIds:
                self.__dict__['_tags'].remove(tagId)

        # add all new tags that are not in the list yet
        for tagId in tagIds:
            if tagId not in self.__dict__['_tags']:
                self.__dict__['_tags'].append(tagId)        

    def add_tags(self, tags):
        if getattr(self, '_tags') is None:
            self.__dict__['_tags'] = []

        # add the new tags to the _tags and _new_tags list,
        # if not listed yet
        tagIds = _create_tagIds(tags)
        for tagId in tagIds:
            if not tagId in self.__dict__['_tags']:
                self.__dict__['_tags'].append(tagId)        

    def del_tags(self, tags):
        if getattr(self, '_tags') is None:
            self.__dict__['_tags'] = []

        # remove the tags from the _tags and _del_tags list,
        # if listed there
        tagIds = _create_tagIds(tags)
        for tagId in tagIds:
            if tagId in self.__dict__['_tags']:
                self.__dict__['_tags'].remove(tagId)        

    def get_attachments(self):
        if getattr(self, '_attachments') is None:
            return None
        else:
            return DataFrame(self._attachments)[['fileName','title','description','version']]

    def add_attachment(self, fileName, title=None, description=None):
        att = Attachment(filename=fileName, title=title, description=description)
        if getattr(self, '_attachments') is None:
            self.__dict__['_attachments'] = []
        self._attachments.append(att.get_data_short())

        if getattr(self, '_new_attachments') is None:
            self.__dict__['_new_attachments'] = []
        self._new_attachments.append(att)

    def download_attachments(self):
        method = 'get'+self.entity+'s'
        entity = self.entity.lower()
        request = {
            "method": method,
            "params": [ self._openbis.token,
                [ self._permId ],
                {
                    "attachments": fetch_option['attachmentsWithContent'],
                    **fetch_option[entity]
                }
            ]
        }
        resp = self._openbis._post_request(self._openbis.as_v3, request)
        attachments = resp[self.permId]['attachments']
        file_list = []
        for attachment in attachments:
            filename = os.path.join(
                self._openbis.hostname, 
                self.permId,
                attachment['fileName']
            )
            os.makedirs(os.path.dirname(filename), exist_ok=True)
            with open(filename, 'wb') as att:
                content = base64.b64decode(attachment['content'])
                att.write(content)    
            file_list.append(filename)
        return file_list


    def _repr_html_(self):
        def nvl(val, string=''):
            if val is None:
                return string
            return val

        html = """
            <table border="1" class="dataframe">
            <thead>
                <tr style="text-align: right;">
                <th>attribute</th>
                <th>value</th>
                </tr>
            </thead>
            <tbody>
        """
            
        for attr in self._allowed_attrs:
            if attr == 'attachments':
                continue
            html += "<tr> <td>{}</td> <td>{}</td> </tr>".format(
                attr, nvl(getattr(self, attr, ''),'') 
            )
        if getattr(self, '_attachments') is not None:
            html += "<tr><td>attachments</td><td>"
            html += "<br/>".join(att['fileName'] for att in self._attachments)
            html += "</td></tr>"

        html += """
            </tbody>
            </table>
        """
        return html


class Sample():
    """ A Sample is one of the most commonly used objects in openBIS.
    """

    def __init__(self, openbis_obj, type, data=None, **kwargs):
        self.__dict__['openbis'] = openbis_obj
        self.__dict__['type'] = type
        self.__dict__['p'] = PropertyHolder(openbis_obj, type)
        self.__dict__['a'] = AttrHolder(openbis_obj, 'Sample', type)

        if data is not None:
            self._set_data(data)

        if kwargs is not None:
            for key in kwargs:
                setattr(self, key, kwargs[key])

    def _set_data(self, data):
            # assign the attribute data to self.a by calling it 
            # (invoking the AttrHolder.__call__ function)
            self.a(data)
            self.__dict__['data'] = data

            # put the properties in the self.p namespace (without checking them)
            for key, value in data['properties'].items():
                self.p.__dict__[key.lower()] = value

    def __dir__(self):
        return [
            'props', 'get_parents()', 'get_children()',
            'get_datasets()', 'get_experiment()',
            'space', 'project', 'experiment', 'project', 'tags', 
            'set_tags()', 'add_tags()', 'del_tags()',
            'add_attachment()', 'get_attachments()', 'download_attachments()'
        ]

    @property
    def props(self):
        return self.__dict__['p']

    @property
    def type(self):
        return self.__dict__['type']

    @type.setter
    def type(self, type_name):
            sample_type = self.openbis.get_sample_type(type_name)
            self.p.__dict__['_type'] = sample_type
            self.a.__dict__['_type'] = sample_type

    def __getattr__(self, name):
        return getattr(self.__dict__['a'], name)

    def __setattr__(self, name, value):
        if name in ['set_properties', 'set_tags', 'add_tags']:
            raise ValueError("These are methods which should not be overwritten")

        setattr(self.__dict__['a'], name, value)

    def _repr_html_(self):
        html = self.a._repr_html_()
        return html

    def set_properties(self, properties):
        self.openbis.update_sample(self.permId, properties=properties)

    def save(self):
        props = self.p._all_props()
        attrs = self.a._up_attrs()
        attrs["properties"] = props

        if self.identifier is None:
            # create a new sample
            attrs["@type"] = "as.dto.sample.create.SampleCreation"
            attrs["typeId"] = self.__dict__['type'].data['permId']
            request = {
                "method": "createSamples",
                "params": [ self.openbis.token,
                    [ attrs ]
                ]
            }
            resp = self.openbis._post_request(self.openbis.as_v3, request)
            new_sample_data = self.openbis.get_sample(resp[0]['permId'], only_data=True)
            self._set_data(new_sample_data)
            return self
            
        else:
            attrs["@type"] = "as.dto.sample.update.SampleUpdate"
            attrs["sampleId"] = {
                "permId": self.permId,
                "@type": "as.dto.sample.id.SamplePermId"
            }
            request = {
                "method": "updateSamples",
                "params": [ self.openbis.token,
                    [ attrs ]
                ]
            }
            resp = self.openbis._post_request(self.openbis.as_v3, request)
            print('Sample successfully updated')

    def delete(self, reason):
        self.openbis.delete_entity('sample', self.permId, reason)

    def get_datasets(self):
        return self.openbis.get_datasets(sample=self.permId)

    def get_projects(self):
        return self.openbis.get_project(withSamples=[self.permId])

    def get_experiment(self):
        try: 
            return self.openbis.get_experiment(self._experiment['identifier'])
        except Exception:
            pass

    @property
    def experiment(self):
        try: 
            return self.openbis.get_experiment(self._experiment['identifier'])
        except Exception:
            pass
        

class Space(OpenBisObject):
    """ managing openBIS spaces
    """

    def __init__(self, openbis_obj, type=None, data=None, **kwargs):
        self.__dict__['openbis'] = openbis_obj
        self.__dict__['a'] = AttrHolder(openbis_obj, 'Space', type)

        if data is not None:
            self.a(data)
            self.__dict__['data'] = data

        if kwargs is not None:
            for key in kwargs:
                setattr(self, key, kwargs[key])

    def __dir__(self):
        """all the available methods and attributes that should be displayed
        when using the autocompletion feature (TAB) in Jupyter
        """
        return['code','permId', 'description', 'registrator', 'registrationDate',
        'modificationDate', 'get_projects()', 'new_project()', 'get_samples()', 'delete()']

    def __str__(self):
        return self.data.get('code', None)

    def get_samples(self):
        return self.openbis.get_samples(space=self.code)

    def get_projects(self):
        return self.openbis.get_projects(space=self.code)

    def new_project(self, code, description=None, **kwargs):
        return self.openbis.new_project(self.code, code, description, **kwargs)

    def delete(self, reason):
        self.openbis.delete_entity('Space', self.permId, reason)


class Things():
    """An object that contains a DataFrame object about an entity  available in openBIS.
       
    """

    def __init__(self, openbis_obj, entity, df, identifier_name='code'):
        self.openbis = openbis_obj
        self.entity = entity
        self.df = df
        self.identifier_name = identifier_name

    def _repr_html_(self):
        return self.df._repr_html_()

    def __getitem__(self, key):
        if self.df is not None and len(self.df) > 0:
            row = None
            if isinstance(key, int):
                # get thing by rowid
                row = self.df.loc[[key]]
            elif isinstance(key, list):
                # treat it as a normal dataframe
                return self.df[key]
            else:
                # get thing by code
                row = self.df[self.df[self.identifier_name]==key.upper()]

            if row is not None:
                # invoke the openbis.get_entity() method
                return getattr(self.openbis, 'get_'+self.entity)(row[self.identifier_name].values[0])


class Experiment(OpenBisObject):
    """ 
    """

    def __init__(self, openbis_obj, type, data=None, **kwargs):
        self.__dict__['openbis'] = openbis_obj
        self.__dict__['type'] = type
        self.__dict__['p'] = PropertyHolder(openbis_obj, type)
        self.__dict__['a'] = AttrHolder(openbis_obj, 'Experiment', type)

        if data is not None:
            self._set_data(data)

        if kwargs is not None:
            for key in kwargs:
                setattr(self, key, kwargs[key])

    def _set_data(self, data):
            # assign the attribute data to self.a by calling it 
            # (invoking the AttrHolder.__call__ function)
            self.a(data)
            self.__dict__['data'] = data

            # put the properties in the self.p namespace (without checking them)
            for key, value in data['properties'].items():
                self.p.__dict__[key.lower()] = value

    def __str__(self):
        return self.data['code']

    def __dir__(self):
        # the list of possible methods/attributes displayed
        # when invoking TAB-completition
        return [
            'props', 'space', 'project', 
            'project','tags', 'attachments', 'data',
            'get_datasets()', 'get_samples()', 
            'set_tags()', 'add_tags()', 'del_tags()',
            'add_attachment()', 'get_attachments()', 'download_attachments()'
        ]

    @property
    def props(self):
        return self.__dict__['p']

    @property
    def type(self):
        return self.__dict__['type']

    @type.setter
    def type(self, type_name):
            experiment_type = self.openbis.get_experiment_type(type_name)
            self.p.__dict__['_type'] = experiment_type
            self.a.__dict__['_type'] = experiment_type

    def __getattr__(self, name):
        return getattr(self.__dict__['a'], name)

    def __setattr__(self, name, value):
        if name in ['set_properties', 'add_tags()', 'del_tags()', 'set_tags()']:
            raise ValueError("These are methods which should not be overwritten")

        setattr(self.__dict__['a'], name, value)

    def _repr_html_(self):
        html = self.a._repr_html_()
        return html

    def set_properties(self, properties):
        self.openbis.update_experiment(self.permId, properties=properties)

    def save(self):
        if self.is_new:
            request = self._new_attrs()
            props = self.p._all_props()
            request["properties"] = props
            self.openbis._post_request(self.openbis.as_v3, request)
            self.a.__dict__['_is_new'] = False
            print("Experiment successfully created.")
        else:
            request = self._up_attrs()
            props = self.p._all_props()
            request["properties"] = props
            self.openbis._post_request(self.openbis.as_v3, request)
            print("Experiment successfully updated.")

    def delete(self, reason):
        self.openbis.delete_entity('experiment', self.permId, reason)

    def get_datasets(self):
        return self.openbis.get_datasets(experiment=self.permId)

    def get_projects(self):
        return self.openbis.get_project(experiment=self.permId)

    def get_samples(self):
        return self.openbis.get_samples(experiment=self.permId)


class Attachment():

    def __init__(self, filename, title=None, description=None):
        if not os.path.exists(filename):
            raise ValueError("File not found: {}".format(filename))
        self.fileName = filename
        self.title = title
        self.description = description

    def get_data_short(self):
        return {
            "fileName"    : self.fileName,
            "title"       : self.title,
            "description" : self.description,
        }

    def get_data(self):
        with open(self.fileName, 'rb') as att:
            content = att.read()
            contentb64 = base64.b64encode(content).decode()
        return {
            "fileName"    : self.fileName,
            "title"       : self.title,
            "description" : self.description,
            "content"     : contentb64,
            "@type"       : "as.dto.attachment.create.AttachmentCreation",
        }


class Project(OpenBisObject):

    def __init__(self, openbis_obj, data=None, **kwargs):
        self.__dict__['openbis'] = openbis_obj
        self.__dict__['a'] = AttrHolder(openbis_obj, 'Project')

        if data is not None:
            self.a(data)
            self.__dict__['data'] = data

        if kwargs is not None:
            for key in kwargs:
                setattr(self, key, kwargs[key])

    def _modifiable_attrs(self):
        return
 
    def __dir__(self):
        """all the available methods and attributes that should be displayed
        when using the autocompletion feature (TAB) in Jupyter
        """
        return['code','permId', 'identifier', 'description', 'space', 'registrator',
        'registrationDate', 'modifier', 'modificationDate', 'add_attachment()',
        'get_attachments()', 'download_attachments()',
        'get_experiments()', 'get_samples()', 'get_datasets()',
        'delete()'
        ]

    def get_samples(self):
        return self.openbis.get_samples(project=self.permId)

    def get_experiments(self):
        return self.openbis.get_experiments(project=self.permId)

    def get_datasets(self):
        return self.openbis.get_datasets(project=self.permId)

    def delete(self, reason):
        self.openbis.delete_entity('project', self.permId, reason) 

    def save(self):
        if self.is_new:
            request = self._new_attrs()
            self.openbis._post_request(self.openbis.as_v3, request)
            self.a.__dict__['_is_new'] = False
            print("Project successfully created.")
        else:
            request = self._up_attrs()
            self.openbis._post_request(self.openbis.as_v3, request)
            print("Project successfully updated.")

