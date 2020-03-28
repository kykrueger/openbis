import _ from 'lodash'
import autoBind from 'auto-bind'
import actions from '@src/js/store/actions/actions.js'

export default class BrowserController {
  constructor() {
    autoBind(this)
  }

  init(getState, setState, dispatch) {
    this.getState = getState
    this.setState = setState
    this.dispatch = dispatch

    return {
      loaded: false,
      loadedNodes: [],
      visibleIds: {},
      selectedIds: {},
      expandedIds: {},
      filter: ''
    }
  }

  getPage() {
    throw 'Method not implemented'
  }

  loadNodes() {
    throw 'Method not implemented'
  }

  load() {
    return this.loadNodes().then(nodes => {
      this._sortNodes(nodes)

      this._visitNodes(nodes, node => {
        if (node.children && node.children.length === 0) {
          delete node.children
        }
      })

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

    const filteredIds = {}
    const expandedIds = {}

    this._visitNodes(filteredNodes, node => {
      filteredIds[node.id] = node.id
      if (node.children) {
        expandedIds[node.id] = node.id
      }
    })

    this.setState({
      filter,
      visibleIds: filteredIds,
      expandedIds: expandedIds
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
            this.getPage(),
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

      const newNodes = []

      for (let i = 0; i < nodes.length; i++) {
        let node = nodes[i]

        if (visibleIds[node.id]) {
          const newNode = {
            ...node,
            expanded: !!expandedIds[node.id],
            selected: !!selectedIds[node.id]
          }
          if (node.children) {
            newNode.children = _createNodes(node.children)
          }
          newNodes.push(newNode)
        }
      }

      return newNodes
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
