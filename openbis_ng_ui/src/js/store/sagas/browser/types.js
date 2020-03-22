import _ from 'lodash'
import { putAndWait } from '@src/js/store/sagas/effects.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'
import actions from '@src/js/store/actions/actions.js'
import common from '@src/js/store/common/browser.js'

function* createNodes() {
  let {
    objectTypes,
    collectionTypes,
    dataSetTypes,
    materialTypes
  } = yield getTypes()

  let convert = function(types, typeName) {
    return _.map(types, type => {
      return {
        id: `${typeName}s/${type.code}`,
        text: type.code,
        object: { type: typeName, id: type.code }
      }
    })
  }

  let objectTypeNodes = convert(objectTypes, objectType.OBJECT_TYPE)
  let collectionTypeNodes = convert(collectionTypes, objectType.COLLECTION_TYPE)
  let dataSetTypeNodes = convert(dataSetTypes, objectType.DATA_SET_TYPE)
  let materialTypeNodes = convert(materialTypes, objectType.MATERIAL_TYPE)

  common.sortNodes(objectTypeNodes)
  common.sortNodes(collectionTypeNodes)
  common.sortNodes(dataSetTypeNodes)
  common.sortNodes(materialTypeNodes)

  let nodes = [
    {
      id: 'objectTypes',
      text: 'Object Types',
      children: objectTypeNodes
    },
    {
      id: 'collectionTypes',
      text: 'Collection Types',
      children: collectionTypeNodes
    },
    {
      id: 'dataSetTypes',
      text: 'Data Set Types',
      children: dataSetTypeNodes
    },
    {
      id: 'materialTypes',
      text: 'Material Types',
      children: materialTypeNodes
    }
  ]

  return nodes
}

function* getTypes() {
  let responses = yield putAndWait({
    objectTypes: actions.apiRequest({
      method: 'searchSampleTypes',
      params: [
        new openbis.SampleTypeSearchCriteria(),
        new openbis.SampleTypeFetchOptions()
      ]
    }),
    collectionTypes: actions.apiRequest({
      method: 'searchExperimentTypes',
      params: [
        new openbis.ExperimentTypeSearchCriteria(),
        new openbis.ExperimentTypeFetchOptions()
      ]
    }),
    dataSetTypes: actions.apiRequest({
      method: 'searchDataSetTypes',
      params: [
        new openbis.DataSetTypeSearchCriteria(),
        new openbis.DataSetTypeFetchOptions()
      ]
    }),
    materialTypes: actions.apiRequest({
      method: 'searchMaterialTypes',
      params: [
        new openbis.MaterialTypeSearchCriteria(),
        new openbis.MaterialTypeFetchOptions()
      ]
    })
  })

  let convert = function(response) {
    return response.payload.result.getObjects().map(type => {
      return {
        code: type.getCode(),
        description: type.getDescription()
      }
    })
  }

  return {
    objectTypes: convert(responses.objectTypes),
    collectionTypes: convert(responses.collectionTypes),
    dataSetTypes: convert(responses.dataSetTypes),
    materialTypes: convert(responses.materialTypes)
  }
}

export default {
  createNodes
}
