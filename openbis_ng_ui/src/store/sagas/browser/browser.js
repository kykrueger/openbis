import _ from 'lodash'
import {put, takeEvery, select} from './../effects.js'

import * as selectors from '../../selectors/selectors.js'
import * as actions from '../../actions/actions.js'
import * as pages from '../../consts/pages.js'
import * as common from '../../common/browser.js'

import * as typesBrowser from './types.js'
import * as usersBrowser from './users.js'

export default function* browser() {
  yield takeEvery(actions.BROWSER_INIT, browserInit)
  yield takeEvery(actions.BROWSER_RELEASE, browserRelease)
  yield takeEvery(actions.BROWSER_FILTER_CHANGED, browserFilterChanged)
  yield takeEvery(actions.BROWSER_NODE_SELECTED, browserNodeSelected)
  yield takeEvery(actions.BROWSER_NODE_EXPANDED, browserNodeExpanded)
  yield takeEvery(actions.BROWSER_NODE_COLLAPSED, browserNodeCollapsed)
}

function* browserInit(action) {
  let page = action.payload.page
  let impl = yield getBrowserImpl(page)

  let nodes = yield impl.createNodes()
  let filter = yield select(selectors.getBrowserFilter, page)
  let filteredNodes = browserFilter(nodes, filter)

  yield put(actions.browserSetNodes(page, nodes))
  yield put(actions.browserSetVisibleNodes(page, filteredNodes))
  yield put(actions.browserSetInitialized(page, true))
}

function* browserRelease(action){
  let page = action.payload.page
  yield put(actions.browserSetNodes(page, []))
  yield put(actions.browserSetInitialized(page, false))
}

function* browserFilterChanged(action){
  let page = action.payload.page
  let filter = action.payload.filter

  let allNodes = yield select(selectors.getAllBrowserNodes, page)
  let filteredNodes = new Set(browserFilter(allNodes, filter))
  let expandedNodes = []

  if(filter && filter.trim()){
    common.mapNodes(null, allNodes, (parent, node) => {
      if(_.size(node.children) > 0 && _.some(node.children, child => {
        return filteredNodes.has(child.id)
      })){
        expandedNodes.push(node.id)
      }
      return node
    })
  }

  if(_.size(expandedNodes) > 0){
    yield put(actions.browserAddExpandedNodes(page, expandedNodes))
  }
  yield put(actions.browserSetVisibleNodes(page, filteredNodes))
  yield put(actions.browserSetFilter(page, action.payload.filter))
}

function* browserNodeSelected(action){
  yield put(actions.browserSetSelectedNode(action.payload.page, action.payload.id))
}

function* browserNodeExpanded(action){
  yield put(actions.browserAddExpandedNodes(action.payload.page, [action.payload.id]))
}

function* browserNodeCollapsed(action){
  yield put(actions.browserRemoveExpandedNodes(action.payload.page, [action.payload.id]))
}

function browserFilter(nodes, filter){
  filter = filter && filter.trim().toLowerCase() || ''
  return common.getMatchingNodes(nodes, node => {
    return node.text && node.text.toLowerCase().indexOf(filter) !== -1
  })
}

function getBrowserImpl(page){
  switch(page){
    case pages.TYPES :
      return typesBrowser
    case pages.USERS:
      return usersBrowser
  }
}
