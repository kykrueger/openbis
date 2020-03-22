import _ from 'lodash'
import { createSelector } from 'reselect'
import common from '@src/js/store/common/browser.js'
import logger from '@src/js/common/logger.js'

const getBrowser = (state, page) => {
  logger.log(logger.DEBUG, 'browserSelector.getBrowser')
  return state.ui.pages[page].browser
}

const getBrowserFilter = (state, page) => {
  logger.log(logger.DEBUG, 'browserSelector.getBrowserFilter')
  return getBrowser(state, page).filter
}

const getAllBrowserNodes = (state, page) => {
  logger.log(logger.DEBUG, 'browserSelector.getAllBrowserNodes')
  return getBrowser(state, page).nodes
}

const getBrowserSelectedNode = (state, page) => {
  logger.log(logger.DEBUG, 'browserSelector.getBrowserSelectedNode')
  return getBrowser(state, page).selectedNode
}

const createGetBrowserNodes = () => {
  return createSelector([getBrowser], browser => {
    logger.log(logger.DEBUG, 'browserSelector.getBrowserNodes')

    let selectedNodes = new Set(browser.selectedNodes)
    let visibleNodes = new Set(browser.visibleNodes)
    let expandedNodes = new Set(browser.expandedNodes)
    let nodes = browser.nodes

    nodes = common.mapNodes(null, nodes, (parent, node) => {
      if (visibleNodes.has(node.id)) {
        let selected = selectedNodes.has(node.id)
        let expanded = expandedNodes.has(node.id)
        return Object.assign({ selected, expanded }, node)
      } else {
        return null
      }
    })

    nodes = common.mapNodes(null, nodes, (parent, node) => {
      if (_.size(node.children) === 0) {
        delete node['children']
      }
      return node
    })

    return nodes
  })
}

export default {
  getBrowser,
  getBrowserFilter,
  getAllBrowserNodes,
  getBrowserSelectedNode,
  createGetBrowserNodes
}
