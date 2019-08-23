def openbis_definitions(entity):
    """
    attrs_new: Attributes, that can appear when creating new entities
    attrs_up: Attributes that can be updated
    attrs: Attributes that are displayed when fetched
    multi: multivalue-elements which appear in an entity. E.g. parents or children in a Sample.
    identifier: to update entities, the identifier must be specified. Usually identityName + "Id"
    (Entity-Name in camel-case, starting with lowercase letter, with Id added)
    """
    entities = {
        "space": {
            "attrs_new": "code description".split(),
            "attrs_up": "description freeze freezeForProjects freezeForSamples".split(),
            "attrs": "code permId description frozen frozenForProjects frozenForSamples registrator registrationDate modificationDate".split(),
            "multi": "".split(),
            "identifier": "spaceId",
            "create": { "@type": "as.dto.space.create.SpaceCreation"},
            "update": { "@type": "as.dto.space.update.SpaceUpdate"},
            "delete": { "@type": "as.dto.space.delete.SpaceDeletionOptions"},
            "fetch":  { "@type": "as.dto.space.fetchoptions.SpaceFetchOptions"},
        },
        "project": {
            "attrs_new": "code description space attachments".split(),
            "attrs_up": "description space attachments freeze freezeForExperiments freezeForSamples".split(),
            "attrs": "code description permId identifier space leader registrator registrationDate modifier modificationDate attachments frozen frozenForExperiments frozenForSamples".split(),
            "multi": "".split(),
            "identifier": "projectId",
            "create": { "@type": "as.dto.project.create.ProjectCreation"},
            "update": { "@type": "as.dto.project.update.ProjectUpdate"},
        },
        "experiment": {
            "attrs_new": "code type project tags attachments".split(),
            "attrs_up": "project tags attachments freeze freezeForDataSets freezeForSamples".split(),
            "attrs": "code permId identifier type project tags registrator registrationDate modifier modificationDate attachments frozen frozenForDataSets frozenForSamples".split(),
            "multi": "tags attachments".split(),
            "identifier": "experimentId",
            "create": { "@type": "as.dto.experiment.create.ExperimentCreation"},
            "update": { "@type": "as.dto.experiment.update.ExperimentUpdate"},
        },
        "sample": {
            "attrs_new": "code type project parents children container components space experiment tags attachments".split(),
            "attrs_up": "project parents children container components space experiment tags attachments freeze freezeForComponents freezeForChildren freezeForParents freezeForDataSets".split(),
            "attrs": "code permId identifier type project parents children components space experiment tags registrator registrationDate modifier modificationDate attachments container frozen frozenForComponents frozenForChildren frozenForParents frozenForDataSets".split(),
            "ids2type": {
                'parentIds': {'permId': {'@type': 'as.dto.sample.id.SamplePermId'}},
                'childIds': {'permId': {'@type': 'as.dto.sample.id.SamplePermId'}},
                'componentIds': {'permId': {'@type': 'as.dto.sample.id.SamplePermId'}},
            },
            "identifier": "sampleId",
            "create": { "@type": "as.dto.sample.create.SampleCreation"},
            "update": { "@type": "as.dto.sample.update.SampleUpdate"},
            "delete": { "@type": "as.dto.sample.delete.SampleDeletionOptions"},
            "cre_type": "as.dto.sample.create.SampleCreation",
            "multi": "parents children components tags attachments".split(),
        },
        "sampleType": {
            "attrs_new": "code description autoGeneratedCode generatedCodePrefix subcodeUnique listable showContainer showParents showParentMetadata validationPlugin".split(),
            "attrs_up": "description autoGeneratedCode generatedCodePrefix subcodeUnique listable showContainer showParents showParentMetadata, validationPlugin".split(),
            "attrs": "permId code description autoGeneratedCode generatedCodePrefix subcodeUnique listable showContainer showParents showParentMetadata modificationDate validationPlugin".split(),
            "default_attribute_values": {
                "autoGeneratedCode"   : False,
                "generatedCodePrefix" : "S",
                "subcodeUnique"       : False,
                "description"         : "",
                "listable"            : True,
                "showContainer"       : False,
                "showParents"         : True,
                "showParentMetadata"  : False
            },
            "search": {
                "@type": "as.dto.sample.search.SampleTypeSearchCriteria"
            },
            "create": {
                "@type": "as.dto.sample.create.SampleTypeCreation"
            },
            "update": {
                "@type": "as.dto.sample.update.SampleTypeUpdate"
            },
            "delete": {
                "@type": "as.dto.sample.delete.SampleTypeDeletionOptions"
            },
            "identifier": "typeId",
        },
        "materialType": {
            "attrs_new":    "code description validationPluginId".split(),
            "attrs_up":          "description validationPluginId".split(),
            "attrs": "permId code description validationPlugin".split(),
            "search": {
                "@type": "as.dto.material.search.MaterialTypeSearchCriteria"
            },
            "fetch": {
                "@type": "as.dto.material.fetchoptions.MaterialTypeFetchOptions"
            },
            "create": {
                "@type": "as.dto.material.create.MaterialTypeCreation"
            },
            "update": {
                "@type": "as.dto.material.update.MaterialTypeUpdate"
            },
            "delete": {
                "@type": "as.dto.material.delete.MaterialTypeDeletionOptions"
            },
            "identifier": "typeId",
        },
        "dataSetType": {
            "attrs_new":    "code description mainDataSetPattern mainDataSetPath disallowDeletion validationPluginId".split(),
            "attrs_up":          "description mainDataSetPattern mainDataSetPath disallowDeletion validationPluginId".split(),
            "attrs": "permId code description mainDataSetPattern mainDataSetPath disallowDeletion modificationDate validationPlugin".split(),
            "search": {
                "@type": "as.dto.dataset.search.DataSetTypeSearchCriteria"
            },
            "fetch": {
                "@type": "as.dto.dataset.fetchoptions.DataSetTypeFetchOptions"
            },
            "create": {
                "@type": "as.dto.dataset.create.DataSetTypeCreation"
            },
            "update": {
                "@type": "as.dto.dataset.update.DataSetTypeUpdate"
            },
            "delete": {
                "@type": "as.dto.dataset.delete.DataSetTypeDeletionOptions"
            },
            "identifier": "typeId",
        },
        "experimentType": {
            "attrs_new":        "code description modificationDate validationPluginId".split(),
            "attrs_up":              "description modificationDate validationPluginId".split(),
            "attrs":     "permId code description modificationDate validationPlugin".split(),
            "search": {
                "@type": "as.dto.experiment.search.ExperimentTypeSearchCriteria"
            },
            "fetch": {
                "@type": "as.dto.experiment.fetchoptions.ExperimentTypeFetchOptions"
            },
            "create": {
                "@type": "as.dto.experiment.create.ExperimentTypeCreation"
            },
            "update": {
                "@type": "as.dto.experiment.create.ExperimentTypeUpdate"
            },
            "delete": {
                "@type": "as.dto.experiment.delete.ExperimentTypeDeletionOptions"
            },
            "identifier": "typeId",
        },
        "propertyType": {
            "attrs": "code label description managedInternally internalNameSpace dataType vocabulary materialType schema transformation semanticAnnotations registrator registrationDate".split(),
            "attrs_new": "code label description managedInternally internalNameSpace dataType vocabularyId materialTypeId schema transformation metaData".split(),
            "attrs_up": "label description schema transformation metaData".split(),
            "search": {
                "@type": "as.dto.property.search.PropertyTypeSearchCriteria"
            },
            "create": { 
                "@type": "as.dto.property.create.PropertyTypeCreation"
            },
            "update": { 
                "@type": "as.dto.property.update.PropertyTypeUpdate"
            },
            "delete": {
                "@type": "as.dto.property.delete.PropertyTypeDeletionOptions"
            },
            "dataType": [
                "INTEGER", "VARCHAR", "MULTILINE_VARCHAR", 
                "REAL", "TIMESTAMP", "BOOLEAN", "CONTROLLEDVOCABULARY",
                "MATERIAL", "HYPERLINK", "XML"
            ],
            "identifier": "typeId",
        },
        "SemanticAnnotation": {
            "attrs_new": "permId entityType propertyType predicateOntologyId predicateOntologyVersion predicateAccessionId descriptorOntologyId descriptorOntologyVersion descriptorAccessionId".split(),
            "attrs_up": "entityType propertyType predicateOntologyId predicateOntologyVersion predicateAccessionId descriptorOntologyId descriptorOntologyVersion descriptorAccessionId ".split(),
            "attrs": "permId entityType propertyType predicateOntologyId predicateOntologyVersion predicateAccessionId descriptorOntologyId descriptorOntologyVersion descriptorAccessionId creationDate".split(),
            "ids2type": {
                "propertyTypeId": { 
                    "permId": "as.dto.property.id.PropertyTypePermId"
                },
                "entityTypeId": { 
                    "permId": "as.dto.entity.id.EntityTypePermId"
                },
            },
            "identifier": "permId",
            "cre_type": "as.dto.sample.create.SampleCreation",
            "multi": "parents children components tags attachments".split(),
        },
        "dataSet": {
            "attrs_new": "type code kind experiment sample parents children components containers tags".split(),
            "attrs_up": "parents children experiment sample components containers tags freeze freezeForChildren freezeForParents freezeForComponents freezeForContainers".split(),
            "attrs": "code permId type kind experiment sample parents children components containers tags accessDate dataProducer dataProductionDate registrator registrationDate modifier modificationDate dataStore measured frozen frozenForChildren frozenForParents frozenForComponents frozenForContainers".split(),

            "ids2type": {
                'parentIds': {'permId': {'@type': 'as.dto.dataset.id.DataSetPermId'}},
                'childIds': {'permId': {'@type': 'as.dto.dataset.id.DataSetPermId'}},
                'componentIds': {'permId': {'@type': 'as.dto.dataset.id.DataSetPermId'}},
                'containerIds': {'permId': {'@type': 'as.dto.dataset.id.DataSetPermId'}},
            },
            "multi": "parents children containers components".split(),
            "identifier": "dataSetId",
        },
        "Material": {
            "attrs_new": "code description type creation tags".split(),
            "attrs_up": "description type creation tags".split(),
            "attrs": "code description type creation registrator registrationDate modifier modificationDate tags".split(),
            "multi": "".split(),
            "identifier": "materialId",
        },
        "tag": {
            "attrs_new": "code description".split(),
            "attrs_up": "description".split(),
            "attrs": "permId code description registrationDate owner".split(),
            "multi": "".split(),
            "identifier": "tagId",
        },
        "vocabulary": {
            "attrs_new": "code description managedInternally internalNameSpace chosenFromList urlTemplate terms".split(),
            "attrs_up": "description managedInternally internalNameSpace chosenFromList urlTemplate".split(),
            "attrs": "code description managedInternally internalNameSpace chosenFromList urlTemplate registrator registrationDate modifier modificationDate".split(),
            "multi": "".split(),
            "identifier": "vocabularyId",
            "search": { "@type": "as.dto.vocabulary.search.VocabularySearchCriteria" },
            "create": { "@type": "as.dto.vocabulary.create.VocabularyCreation"}, 
            "update": { "@type": "as.dto.vocabulary.upate.VocabularyUpdate"},
            "delete": { "@type": "as.dto.vocabulary.delete.VocabularyDeletionOptions"},
            "fetch":  { "@type": "as.dto.vocabulary.fetchoptions.VocabularyFetchOptions"},
        },
        "VocabularyTerm": {
            "attrs_new": "code vocabularyCode label description official ordinal".split(),
            "attrs_up": "label description official previousTermId".split(),
            "attrs": "code vocabularyCode label description official ordinal registrator registrationDate modifier modificationDate".split(),
            "multi": "".split(),
            "identifier": "vocabularyTermId",
            "create": { "@type": "as.dto.vocabulary.create.VocabularyTermCreation"},
            "update": { "@type": "as.dto.vocabulary.upate.VocabularyTermUpdate"},
            "delete": { "@type": "as.dto.vocabulary.delete.VocabularyTermDeletionOptions"},
            "fetch":  { "@type": "as.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions"},
        },
        "plugin": {
            "attrs_new": "name description pluginType script available entityKind".split(),
            "attrs_up": "description script available".split(),
            "attrs": "permId name description registrator registrationDate pluginKind entityKinds pluginType script available".split(),
            "multi": "".split(),
            "identifier": "pluginId",
            "pluginType": ['DYNAMIC_PROPERTY', 'MANAGED_PROPERTY', 'ENTITY_VALIDATION'],
            "entityKind": ['MATERIAL', 'EXPERIMENT', 'SAMPLE', 'DATA_SET'],
        },
        "person": {
            "attrs_new": "userId space".split(),
            "attrs_up": "space".split(),
            "attrs": "permId userId firstName lastName email space registrationDate ".split(),
            "multi": "".split(),
            "identifier": "userId",
        },
        "authorizationGroup" : {
            "attrs_new": "code description userIds".split(),
            "attrs_up": "code description userIds".split(),
            "attrs": "permId code description registrator registrationDate modificationDate users".split(),
            "multi": "users".split(),
            "identifier": "groupId",
        },
        "roleAssignment" : {
            "attrs": "id user authorizationGroup role roleLevel space project registrator registrationDate".split(),
            "attrs_new": "role roleLevel user authorizationGroup role space project".split(),
            "attrs_up": "role roleLevel user authorizationGroup role space project".split(),
            "role": ['POWER_USER', 'OBSERVER', 'USER', 'DISABLED', 'ADMIN', 'ETL_SERVER']
        },
        "attr2ids": {
            "space": "spaceId",
            "project": "projectId",
            "sample": "sampleId",
            "samples": "sampleIds",
            "dataSet": "dataSetId",
            "dataSets": "dataSetIds",
            "experiment": "experimentId",
            "experiments": "experimentIds",
            "material": "materialId",
            "materials": "materialIds",
            "container": "containerId",
            "containers": "containerIds",
            "component": "componentId",
            "components": "componentIds",
            "parents": "parentIds",
            "children": "childIds",
            "tags": "tagIds",
            "userId": "userId",
            "users": "userIds",
            "description": "description",
            "vocabulary": "vocabularyId",
            "validationPlugin": "validationPluginId",
        },
        "ids2type": {
            'spaceId': {'permId': {'@type': 'as.dto.space.id.SpacePermId'}},
            'projectId': {'permId': {'@type': 'as.dto.project.id.ProjectPermId'}},
            'experimentId': {'permId': {'@type': 'as.dto.experiment.id.ExperimentPermId'}},
            'tagIds': {'code': {'@type': 'as.dto.tag.id.TagCode'}},
        },
    }
    return entities[entity]

get_definition_for_entity = openbis_definitions   # Alias


fetch_option = {
    "space": {"@type": "as.dto.space.fetchoptions.SpaceFetchOptions"},
    "project": {"@type": "as.dto.project.fetchoptions.ProjectFetchOptions"},
    "person": {"@type": "as.dto.person.fetchoptions.PersonFetchOptions"},
    "users": {"@type": "as.dto.person.fetchoptions.PersonFetchOptions" },
    "user": {"@type": "as.dto.person.fetchoptions.PersonFetchOptions" },
    "owner": {"@type": "as.dto.person.fetchoptions.PersonFetchOptions" },
    "registrator": {"@type": "as.dto.person.fetchoptions.PersonFetchOptions"},
    "modifier": {"@type": "as.dto.person.fetchoptions.PersonFetchOptions"},
    "leader": {"@type": "as.dto.person.fetchoptions.PersonFetchOptions"},
    "authorizationGroup": {"@type": "as.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions"},
    "experiment": {
        "@type": "as.dto.experiment.fetchoptions.ExperimentFetchOptions",
        "type": {"@type": "as.dto.experiment.fetchoptions.ExperimentTypeFetchOptions"}
    },
    "sample": {
        "@type": "as.dto.sample.fetchoptions.SampleFetchOptions",
        "type": {"@type": "as.dto.sample.fetchoptions.SampleTypeFetchOptions"}
    },
    "samples": {"@type": "as.dto.sample.fetchoptions.SampleFetchOptions"},
    "sampleType": {
        "@type": "as.dto.sample.fetchoptions.SampleTypeFetchOptions",
        "propertyAssignments": {
            "@type": "as.dto.property.fetchoptions.PropertyAssignmentFetchOptions",
            "propertyType": {
                "@type": "as.dto.property.fetchoptions.PropertyTypeFetchOptions"
            },
            "plugin": {
                "@type": "as.dto.plugin.fetchoptions.PluginFetchOptions"
            }
        },
        "validationPlugin": {
            "@type": "as.dto.plugin.fetchoptions.PluginFetchOptions",
        }
    },
    "materialType": {
        "@type": "as.dto.material.fetchoptions.MaterialTypeFetchOptions",
        "propertyAssignments": {
            "@type": "as.dto.property.fetchoptions.PropertyAssignmentFetchOptions",
            "propertyType": {
                "@type": "as.dto.property.fetchoptions.PropertyTypeFetchOptions",
            },
            "plugin": {
                "@type": "as.dto.plugin.fetchoptions.PluginFetchOptions"
            }
        },
        "validationPlugin": {
            "@type": "as.dto.plugin.fetchoptions.PluginFetchOptions",
        }
    },
    "dataSetType": {
        "@type": "as.dto.dataset.fetchoptions.DataSetTypeFetchOptions",
        "propertyAssignments": {
            "@type": "as.dto.property.fetchoptions.PropertyAssignmentFetchOptions",
            "propertyType": {
                "@type": "as.dto.property.fetchoptions.PropertyTypeFetchOptions",
            },
            "plugin": {
                "@type": "as.dto.plugin.fetchoptions.PluginFetchOptions"
            }
        },
        "validationPlugin": {
            "@type": "as.dto.plugin.fetchoptions.PluginFetchOptions",
        }
    },
    "experimentType": {
        "@type": "as.dto.experiment.fetchoptions.ExperimentTypeFetchOptions",
        "propertyAssignments": {
            "@type": "as.dto.property.fetchoptions.PropertyAssignmentFetchOptions",
            "propertyType": {
                "@type": "as.dto.property.fetchoptions.PropertyTypeFetchOptions",
            },
            "plugin": {
                "@type": "as.dto.plugin.fetchoptions.PluginFetchOptions"
            }
        }, 
        "validationPlugin": {
            "@type": "as.dto.plugin.fetchoptions.PluginFetchOptions",
        }
    },
    "dataSet": {
        "@type": "as.dto.dataset.fetchoptions.DataSetFetchOptions",
        "type":       {"@type": "as.dto.dataset.fetchoptions.DataSetTypeFetchOptions"},
        "parents":    {"@type": "as.dto.dataset.fetchoptions.DataSetFetchOptions"},
        "children":   {"@type": "as.dto.dataset.fetchoptions.DataSetFetchOptions"},
        "containers": {"@type": "as.dto.dataset.fetchoptions.DataSetFetchOptions"},
        "components": {"@type": "as.dto.dataset.fetchoptions.DataSetFetchOptions"},
    },
    "dataSets": {
        "@type": "as.dto.dataset.fetchoptions.DataSetFetchOptions",
        "properties": {"@type": "as.dto.property.fetchoptions.PropertyFetchOptions"},
        "type": {"@type": "as.dto.dataset.fetchoptions.DataSetTypeFetchOptions"},
    },
    "physicalData": {"@type": "as.dto.dataset.fetchoptions.PhysicalDataFetchOptions"},
    "linkedData": {
        "externalDms": {"@type": "as.dto.externaldms.fetchoptions.ExternalDmsFetchOptions"},
        "@type": "as.dto.dataset.fetchoptions.LinkedDataFetchOptions"
    },
    "roleAssignment": {
        "@type": "as.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions",
    },
    "roleAssignments": {
        "@type": "as.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions",
    },
    "properties": {"@type": "as.dto.property.fetchoptions.PropertyFetchOptions"},
    "propertyAssignments": {
        "@type": "as.dto.property.fetchoptions.PropertyAssignmentFetchOptions",
        "propertyType": {
            "@type": "as.dto.property.fetchoptions.PropertyTypeFetchOptions",
            "vocabulary": {
                "@type": "as.dto.vocabulary.fetchoptions.VocabularyFetchOptions",
            }
        }
    },
    "propertyType": {
        "@type": "as.dto.property.fetchoptions.PropertyTypeFetchOptions",
        "vocabulary": {
            "@type": "as.dto.vocabulary.fetchoptions.VocabularyFetchOptions"
        },
        "materialType": { 
            "@type": "as.dto.material.fetchoptions.MaterialTypeFetchOptions" 
        },
        "semanticAnnotations": {
            "@type": "as.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions"
        },
        "registrator": {
            "@type": "as.dto.person.fetchoptions.PersonFetchOptions"
        },
    },
    "semanticAnnotations": {
        "@type": "as.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions"
    },
    "tags": {"@type": "as.dto.tag.fetchoptions.TagFetchOptions"},
    "tag": {"@type": "as.dto.tag.fetchoptions.TagFetchOptions"},
    "attachments": {"@type": "as.dto.attachment.fetchoptions.AttachmentFetchOptions"},
    "attachmentsWithContent": {
        "@type": "as.dto.attachment.fetchoptions.AttachmentFetchOptions",
        "content": {
            "@type": "as.dto.common.fetchoptions.EmptyFetchOptions"
        },
    },
    "script": {
        "@type": "as.dto.common.fetchoptions.EmptyFetchOptions",
    },
    "history": {"@type": "as.dto.history.fetchoptions.HistoryEntryFetchOptions"},
    "dataStore": {"@type": "as.dto.datastore.fetchoptions.DataStoreFetchOptions"},
    "plugin": {"@type": "as.dto.plugin.fetchoptions.PluginFetchOptions"},
    "vocabulary": {
        "@type": "as.dto.vocabulary.fetchoptions.VocabularyFetchOptions",
    },
    "vocabularyTerm": {"@type": "as.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions"},
    "deletedObjects": { "@type": "as.dto.deletion.fetchoptions.DeletedObjectFetchOptions" },
    "deletion": { "@type": "as.dto.deletion.fetchoptions.DeletionFetchOptions" },
}

def get_fetchoption_for_entity(entity):
    entity = entity[0].lower() + entity[1:]   # make first character lowercase
    try:
        return fetch_option[entity]
    except KeyError as e:
        return {}

def get_type_for_entity(entity, action):
    if action not in "create update delete search".split():
        raise ValueError('unknown action: {}'.format(action))

    definition = openbis_definitions(entity)
    if action in definition:
        return definition[action]
    else:
        # try to guess type, according to the naming scheme
        cap_entity = entity[:1].upper() + entity[1:]
        noun = {
            "create": "Creation",
            "update": "Update",
            "delete": "DeletionOptions",
            "search": "SearchCriteria",
        }
        
        return {
            "@type": "as.dto.{}.{}.{}{}"
            .format(entity.lower(), action, cap_entity, noun[action])
        }


def get_method_for_entity(entity, action):
    action = action.lower()

    if entity == "vocabulary":
        return "{}Vocabularies".format(action)
    
    cap_entity = entity[:1].upper() + entity[1:]

    return "{}{}s".format(action, cap_entity)

