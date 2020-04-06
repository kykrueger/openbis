import _ from 'lodash'
import openbis from '@src/js/services/openbis.js'
import actions from '@src/js/store/actions/actions.js'
import pages from '@src/js/common/consts/pages.js'
import objectType from '@src/js/common/consts/objectType.js'
import objectOperation from '@src/js/common/consts/objectOperation.js'
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
          children: objectTypeNodes,
          childrenType: objectType.NEW_OBJECT_TYPE
        },
        {
          id: 'collectionTypes',
          text: 'Collection Types',
          children: collectionTypeNodes,
          childrenType: objectType.NEW_COLLECTION_TYPE
        },
        {
          id: 'dataSetTypes',
          text: 'Data Set Types',
          children: dataSetTypeNodes,
          childrenType: objectType.NEW_DATA_SET_TYPE
        },
        {
          id: 'materialTypes',
          text: 'Material Types',
          children: materialTypeNodes,
          childrenType: objectType.NEW_MATERIAL_TYPE
        }
      ]

      return nodes
    })
  }

  nodeAdd() {
    if (!this.isAddEnabled()) {
      return
    }

    const selectedNode = this.getSelectedNode()

    if (selectedNode && selectedNode.childrenType) {
      this.context.dispatch(
        actions.objectNew(this.getPage(), selectedNode.childrenType)
      )
    }
  }

  nodeRemove() {
    if (!this.isRemoveEnabled()) {
      return
    }

    const selectedNode = this.getSelectedNode()

    if (selectedNode && selectedNode.object) {
      alert('Object remove')
    }
  }

  getObservedModifications() {
    return {
      [objectType.OBJECT_TYPE]: [objectOperation.CREATE, objectOperation.DELETE]
    }
  }
}
