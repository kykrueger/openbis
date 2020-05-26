import _ from 'lodash'
import openbis from '@src/js/services/openbis.js'
import actions from '@src/js/store/actions/actions.js'
import pages from '@src/js/common/consts/pages.js'
import objectType from '@src/js/common/consts/objectType.js'
import objectOperation from '@src/js/common/consts/objectOperation.js'
import BrowserController from '@src/js/components/common/browser/BrowserController.js'

export default class TypeBrowserController extends BrowserController {
  doGetPage() {
    return pages.TYPES
  }

  async doLoadNodes() {
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

  doNodeAdd(node) {
    if (node && node.childrenType) {
      this.context.dispatch(
        actions.objectNew(this.getPage(), node.childrenType)
      )
    }
  }

  doNodeRemove(node) {
    if (!node.object) {
      return Promise.resolve()
    }

    const { type, id } = node.object
    const reason = 'deleted via ng_ui'
    let promise = null

    if (type === objectType.OBJECT_TYPE) {
      const options = new openbis.SampleTypeDeletionOptions()
      options.setReason(reason)
      promise = openbis.deleteSampleTypes(
        [new openbis.EntityTypePermId(id)],
        options
      )
    } else if (type === objectType.COLLECTION_TYPE) {
      const options = new openbis.ExperimentTypeDeletionOptions()
      options.setReason(reason)
      promise = openbis.deleteExperimentTypes(
        [new openbis.EntityTypePermId(id)],
        options
      )
    } else if (type === objectType.DATA_SET_TYPE) {
      const options = new openbis.DataSetTypeDeletionOptions()
      options.setReason(reason)
      promise = openbis.deleteDataSetTypes(
        [new openbis.EntityTypePermId(id)],
        options
      )
    } else if (type === objectType.MATERIAL_TYPE) {
      const options = new openbis.MaterialTypeDeletionOptions()
      options.setReason(reason)
      promise = openbis.deleteMaterialTypes(
        [new openbis.EntityTypePermId(id)],
        options
      )
    }

    return promise
      .then(() => {
        this.context.dispatch(actions.objectDelete(this.getPage(), type, id))
      })
      .catch(error => {
        this.context.dispatch(actions.errorChange(error))
      })
  }

  doGetObservedModifications() {
    return {
      [objectType.OBJECT_TYPE]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ],
      [objectType.COLLECTION_TYPE]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ],
      [objectType.DATA_SET_TYPE]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ],
      [objectType.MATERIAL_TYPE]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ]
    }
  }
}
