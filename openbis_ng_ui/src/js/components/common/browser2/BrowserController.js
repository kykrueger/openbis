import _ from 'lodash'
import autoBind from 'auto-bind'
import openbis from '@src/js/services/openbis.js'
import pages from '@src/js/common/consts/pages.js'
import objectType from '@src/js/common/consts/objectType.js'
import actions from '@src/js/store/actions/actions.js'

export default class BrowserController {
  constructor(getState, setState, dispatch) {
    this.getState = getState
    this.setState = setState
    this.dispatch = dispatch
    autoBind(this)
  }

  init() {
    this.setState({
      loaded: false,
      loadedNodes: [],
      visibleIds: {},
      selectedIds: {},
      expandedIds: {},
      filter: ''
    })
  }

  load() {
    Promise.all([
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

      this._sortNodes(nodes)

      const filteredNodes = this._filterNodes(nodes, this.getFilter())
      const filteredIds = this._visitNodes(
        filteredNodes,
        (node, result) => {
          result[node.id] = node.id
        },
        {}
      )

      this.setState({
        loaded: true,
        loadedNodes: nodes,
        visibleIds: filteredIds
      })
    })
  }

  filterChange(filter) {
    const { loadedNodes } = this.getState()

    const filteredNodes = this._filterNodes(loadedNodes, filter)
    const filteredIds = this._visitNodes(
      filteredNodes,
      (node, result) => {
        result[node.id] = node.id
      },
      {}
    )

    this.setState({
      filter,
      visibleIds: filteredIds,
      expandedIds: filteredIds
    })
  }

  nodeExpand(nodeId) {
    const { expandedIds } = this.getState()

    const newExpandedIds = { ...expandedIds }
    newExpandedIds[nodeId] = nodeId

    this.setState({
      expandedIds: newExpandedIds
    })
  }

  nodeCollapse(nodeId) {
    const { expandedIds } = this.getState()

    const newExpandedIds = { ...expandedIds }
    delete newExpandedIds[nodeId]

    this.setState({
      expandedIds: newExpandedIds
    })
  }

  nodeSelect(nodeId) {
    const { loadedNodes } = this.getState()

    const nodesWithNodeId = this._visitNodes(loadedNodes, (node, results) => {
      if (node.id === nodeId) {
        results.push(node)
      }
    })

    if (nodesWithNodeId.length > 0) {
      const nodeWithNodeId = nodesWithNodeId[0]
      const newSelectedIds = {}

      if (nodeWithNodeId.object) {
        this._visitNodes(loadedNodes, node => {
          if (node.object && _.isEqual(node.object, nodeWithNodeId.object)) {
            newSelectedIds[node.id] = node.id
          }
        })
        this.dispatch(
          actions.objectOpen(
            pages.TYPES,
            nodeWithNodeId.object.type,
            nodeWithNodeId.object.id
          )
        )
      } else {
        newSelectedIds[nodeId] = nodeId
      }

      this.setState({
        selectedIds: newSelectedIds
      })
    } else {
      this.setState({
        selectedIds: {}
      })
    }
  }

  objectSelect(object) {
    const { loadedNodes } = this.getState()

    const newSelectedIds = {}

    this._visitNodes(loadedNodes, node => {
      if (node.object && _.isEqual(node.object, object)) {
        newSelectedIds[node.id] = node.id
      }
    })

    this.setState({
      selectedIds: newSelectedIds
    })
  }

  getLoaded() {
    const { loaded } = this.getState()
    return loaded
  }

  getFilter() {
    const { filter } = this.getState()
    return filter
  }

  getNodes() {
    const {
      loadedNodes,
      visibleIds,
      expandedIds,
      selectedIds
    } = this.getState()

    const _createNodes = nodes => {
      if (!nodes) {
        return []
      }

      const result = []

      for (let i = 0; i < nodes.length; i++) {
        let node = nodes[i]

        if (visibleIds[node.id]) {
          result.push({
            ...node,
            expanded: !!expandedIds[node.id],
            selected: !!selectedIds[node.id],
            children: _createNodes(node.children)
          })
        }
      }

      return result
    }

    return _createNodes(loadedNodes)
  }

  _sortNodes = (nodes, level = 0) => {
    if (!nodes) {
      return
    }
    if (level > 0) {
      nodes.sort((n1, n2) => {
        return n1.text.localeCompare(n2.text)
      })
    }
    nodes.forEach(node => {
      this._sortNodes(node.children, level + 1)
    })
  }

  _visitNodes = (nodes, visitFn, results = []) => {
    if (nodes) {
      for (let i = 0; i < nodes.length; i++) {
        const node = nodes[i]

        visitFn(node, results)

        this._visitNodes(node.children, visitFn, results)
      }
    }
    return results
  }

  _filterNodes = (nodes, filter, ascendantMatches = false) => {
    if (!nodes || filter.trim().length === 0) {
      return nodes
    }

    let newNodes = []

    nodes.forEach(node => {
      const nodeMatches =
        node.text &&
        node.text.toLowerCase().indexOf(filter.trim().toLowerCase()) !== -1

      const newNode = {
        ...node
      }

      newNode.children = this._filterNodes(
        node.children,
        filter,
        ascendantMatches || nodeMatches
      )

      if (
        ascendantMatches ||
        nodeMatches ||
        (newNode.children && newNode.children.length > 0)
      ) {
        newNodes.push(newNode)
      }
    })

    return newNodes
  }
}
