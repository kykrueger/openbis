import _ from 'lodash'
import openbis from '@src/js/services/openbis.js'
import pages from '@src/js/common/consts/pages.js'
import objectType from '@src/js/common/consts/objectType.js'
import BrowserController from '@src/js/components/common/browser/BrowserController.js'

export default class TypesBrowserController extends BrowserController {
  getPage() {
    return pages.TYPES
  }

  async loadNodes() {
    return Promise.all([
      openbis.searchSampleTypes(
        new openbis.SampleTypeSearchCriteria(),
        new openbis.SampleTypeFetchOptions()
      ),
      openbis.searchExperimentTypes(
        new openbis.ExperimentTypeSearchCriteria(),
        new openbis.ExperimentTypeFetchOptions()
      ),
      openbis.searchDataSetTypes(
        new openbis.DataSetTypeSearchCriteria(),
        new openbis.DataSetTypeFetchOptions()
      ),
      openbis.searchMaterialTypes(
        new openbis.MaterialTypeSearchCriteria(),
        new openbis.MaterialTypeFetchOptions()
      )
    ]).then(([objectTypes, collectionTypes, dataSetTypes, materialTypes]) => {
      const _createNodes = (types, typeName) => {
        return _.map(types, type => {
          return {
            id: `${typeName}s/${type.code}`,
            text: type.code,
            object: { type: typeName, id: type.code }
          }
        })
      }

      let objectTypeNodes = _createNodes(
        objectTypes.getObjects(),
        objectType.OBJECT_TYPE
      )
      let collectionTypeNodes = _createNodes(
        collectionTypes.getObjects(),
        objectType.COLLECTION_TYPE
      )
      let dataSetTypeNodes = _createNodes(
        dataSetTypes.getObjects(),
        objectType.DATA_SET_TYPE
      )
      let materialTypeNodes = _createNodes(
        materialTypes.getObjects(),
        objectType.MATERIAL_TYPE
      )

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
    })
  }
}
