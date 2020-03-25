import _ from 'lodash'
import autoBind from 'auto-bind'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'

export default class BrowserController {
  constructor(getState, setState) {
    this.getState = getState
    this.setState = setState
    autoBind(this)
  }

  init() {
    this.setState({
      loaded: false,
      loadedNodes: {},
      selectedNodes: {},
      expandedNodes: {},
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

      this.setState({
        loaded: true,
        loadedNodes: nodes
      })
    })
  }

  filterChange(filter) {
    this.setState({
      filter
    })
  }

  nodeExpand(nodeId) {
    const { expandedNodes } = this.getState()

    const newExpandedNodes = { ...expandedNodes }
    newExpandedNodes[nodeId] = nodeId

    this.setState({
      expandedNodes: newExpandedNodes
    })
  }

  nodeCollapse(nodeId) {
    const { expandedNodes } = this.getState()

    const newExpandedNodes = { ...expandedNodes }
    delete newExpandedNodes[nodeId]

    this.setState({
      expandedNodes: newExpandedNodes
    })
  }

  nodeSelect(nodeId) {
    const { loadedNodes } = this.getState()

    const _findNodes = (nodes, matchFn, matches = []) => {
      if (nodes) {
        for (let i = 0; i < nodes.length; i++) {
          const node = nodes[i]

          if (matchFn(node)) {
            matches.push(node)
          } else {
            _findNodes(node.children, matchFn, matches)
          }
        }
      }
      return matches
    }

    const nodesWithNodeId = _findNodes(loadedNodes, node => {
      return node.id === nodeId
    })

    if (nodesWithNodeId.length > 0) {
      const nodeWithNodeId = nodesWithNodeId[0]
      const newSelectedNodes = {}

      if (nodeWithNodeId.object) {
        const nodesWithObject = _findNodes(loadedNodes, node => {
          return node.object && _.isEqual(node.object, nodeWithNodeId.object)
        })
        nodesWithObject.forEach(node => {
          newSelectedNodes[node.id] = node.id
        })
      } else {
        newSelectedNodes[nodeId] = nodeId
      }

      this.setState({
        selectedNodes: newSelectedNodes
      })
    } else {
      this.setState({
        selectedNodes: {}
      })
    }
  }

  getFilter() {
    const { filter } = this.getState()
    return filter
  }

  getNodes() {
    const {
      loadedNodes,
      expandedNodes,
      selectedNodes,
      filter
    } = this.getState()

    const _createNodes = nodes => {
      if (!nodes) {
        return []
      }
      return _.map(nodes, node => {
        return {
          ...node,
          expanded: !!expandedNodes[node.id],
          selected: !!selectedNodes[node.id],
          children: _createNodes(node.children)
        }
      })
    }

    const _filterNodes = (nodes, filter, ascendantMatches = false) => {
      if (!nodes || filter.trim().length === 0) {
        return nodes
      }

      let matchingNodes = []

      nodes.forEach(node => {
        const nodeMatches =
          node.text &&
          node.text.toLowerCase().indexOf(filter.trim().toLowerCase()) !== -1

        node.children = _filterNodes(
          node.children,
          filter,
          ascendantMatches || nodeMatches
        )

        if (ascendantMatches || nodeMatches || node.children.length > 0) {
          node.expanded = true
          matchingNodes.push(node)
        }
      })

      return matchingNodes
    }

    const _sortNodes = (nodes, level = 0) => {
      if (!nodes) {
        return
      }
      if (level > 0) {
        nodes.sort((n1, n2) => {
          return n1.text.localeCompare(n2.text)
        })
      }
      nodes.forEach(node => {
        _sortNodes(node.children, level + 1)
      })
    }

    let nodes = _createNodes(loadedNodes)
    nodes = _filterNodes(nodes, filter)
    _sortNodes(nodes)
    return nodes
  }
}
