import _ from 'lodash'
import {putAndWait} from './../effects.js'
import {dto} from '../../../services/openbis.js'
import * as objectType from '../../consts/objectType.js'
import * as actions from '../../actions/actions.js'
import * as common from '../../common/browser.js'

export function* createNodes() {
  let {objectTypes, collectionTypes, dataSetTypes, materialTypes} = yield getTypes()

  let convert = function(types, typeName){
    return _.map(types, type => {
      return {
        id: `${typeName}s/${type.code}`,
        text: type.code,
        object: {type: typeName, id: type.code}
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

  let nodes = [{
    id: 'objectTypes',
    text: 'Object Types',
    children: objectTypeNodes
  }, {
    id: 'collectionTypes',
    text: 'Collection Types',
    children: collectionTypeNodes
  },{
    id: 'dataSetTypes',
    text: 'Data Set Types',
    children: dataSetTypeNodes
  },{
    id: 'materialTypes',
    text: 'Material Types',
    children: materialTypeNodes
  }]

  return nodes
}

function* getTypes(){
  let responses = yield putAndWait({
    objectTypes: actions.apiRequest({
      method: 'searchSampleTypes',
      params: [new dto.SampleTypeSearchCriteria(), new dto.SampleTypeFetchOptions()]
    }),
    collectionTypes: actions.apiRequest({
      method: 'searchExperimentTypes',
      params: [new dto.ExperimentTypeSearchCriteria(), new dto.ExperimentTypeFetchOptions()]
    }),
    dataSetTypes: actions.apiRequest({
      method: 'searchDataSetTypes',
      params: [new dto.DataSetTypeSearchCriteria(), new dto.DataSetTypeFetchOptions()]
    }),
    materialTypes: actions.apiRequest({
      method: 'searchMaterialTypes',
      params: [new dto.MaterialTypeSearchCriteria(), new dto.MaterialTypeFetchOptions()]
    })
  })

  let convert = function(response){
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
