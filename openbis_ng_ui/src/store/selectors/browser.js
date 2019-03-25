import _ from 'lodash'
import { createSelector } from 'reselect'
import * as common from './../common/browser.js'

function getBrowser(state, page){
  return state.ui.pages[page].browser
}

export const getBrowserInitialized = (state, page) => {
  return getBrowser(state, page).initialized
}

export const getBrowserFilter = (state, page) => {
  return getBrowser(state, page).filter
}

export const getAllBrowserNodes = (state, page) => {
  return getBrowser(state, page).nodes
}

export const getBrowserNodes = createSelector(
  [getBrowser],
  browser => {
    let selectedNode = browser.selectedNode
    let visibleNodes = new Set(browser.visibleNodes)
    let expandedNodes = new Set(browser.expandedNodes)
    let nodes = browser.nodes

    nodes = common.mapNodes(null, nodes, (parent, node) => {
      if(visibleNodes.has(node.id)){
        let selected = selectedNode === node.id
        let expanded = expandedNodes.has(node.id)
        return Object.assign({ selected, expanded }, node)
      }else{
        return null
      }
    })

    nodes = common.mapNodes(null, nodes, (parent, node) => {
      if(_.size(node.children) === 0){
        delete node['children']
      }
      return node
    })

    return nodes
  }
)
