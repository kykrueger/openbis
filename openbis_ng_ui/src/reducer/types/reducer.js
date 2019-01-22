import initialState from '../initialstate.js'
import _ from 'lodash'

export default function types(types = initialState.types, action) {
    return {
        browser: browser(types.browser, action),
    }
}

function browser(browser = initialState.types.browser, action) {
    switch (action.type) {
        case 'SET-MODE-DONE':
            return browser_SetModeDone(browser, action)
        case 'EXPAND-NODE':
            return browser_ExpandNode(browser, action)
        case 'COLLAPSE-NODE':
            return browser_CollapseNode(browser, action)
        default:
            return browser
    }
}

function browser_SetModeDone(browser, action) {
    return {
        selectedNodeId: browser.selectedNodeId,
        nodes: [
            browser_SetModeDone_TypeNodes('Object Types', action.data.objectTypes),
            browser_SetModeDone_TypeNodes('Collection Types', action.data.collectionTypes),
            browser_SetModeDone_TypeNodes('Data Set Types', action.data.dataSetTypes),
            browser_SetModeDone_TypeNodes('Material Types', action.data.materialTypes)
        ]
    }
}

function browser_SetModeDone_TypeNodes(groupId, types) {
    let typeNodes = []

    types.forEach(type => {
        typeNodes.push({
            id: type.getPermId().getPermId(),
            expanded: false,
            loading: false,
            loaded: true,
            children: []
        })
    })

    _sortById(typeNodes)

    return {
        id: groupId,
        expanded: false,
        loading: false,
        loaded: true,
        children: typeNodes
    }
}

function browser_ExpandNode(browser, action) {
    let newBrowser = _.cloneDeep(browser)
    _visitNodes(newBrowser.nodes, node => {
        if (node.id === action.node.id) {
            node.expanded = true
        }
    })
    return newBrowser
}

function browser_CollapseNode(browser, action) {
    let newBrowser = _.cloneDeep(browser)
    _visitNodes(newBrowser.nodes, node => {
        if (node.id === action.node.id) {
            node.expanded = false
        }
    })
    return newBrowser
}

const _visitNodes = (nodes, visitor) => {
    let toVisit = []
    let visited = {}

    toVisit.push(...nodes)

    while (toVisit.length > 0) {
        let node = toVisit.shift()

        if (!visited[node.id]) {
            visited[node.id] = true
            let result = visitor(node)
            if (result) {
                return result
            }
        }

        if (node.children !== undefined) {
            node.children.forEach((child) => {
                toVisit.push(child)
            })
        }
    }
}


const _sortById = (arr) => {
    arr.sort((i1, i2) => {
        return i1.id.localeCompare(i2.id)
    })
}