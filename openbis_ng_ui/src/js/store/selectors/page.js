import _ from 'lodash'
import { createSelector } from 'reselect'
import logger from '@src/js/common/logger.js'
import routes from '@src/js/common/consts/routes.js'

const getCurrentRoute = (state, page) => {
  logger.log(logger.DEBUG, 'pageSelector.getCurrentRoute')
  return state.ui.pages[page].currentRoute
}

const getOpenTabs = (state, page) => {
  logger.log(logger.DEBUG, 'pageSelector.getOpenTabs')
  return state.ui.pages[page].openTabs
}

const getOpenObjects = createSelector(getOpenTabs, openTabs => {
  logger.log(logger.DEBUG, 'pageSelector.getOpenObjects')
  return openTabs.map(openTab => {
    return openTab.object
  })
})

const getSelectedTab = (state, page) => {
  logger.log(logger.DEBUG, 'pageSelector.getSelectedTab')
  const selectedObject = createGetSelectedObject()(state, page)
  if (selectedObject) {
    const openTabs = getOpenTabs(state, page)
    return _.find(openTabs, { object: selectedObject })
  } else {
    return null
  }
}

const createGetSelectedObject = () => {
  return createSelector([getCurrentRoute], path => {
    logger.log(logger.DEBUG, 'pageSelector.getSelectedObject')
    if (path) {
      let route = routes.parse(path)
      if (route && route.type && route.id) {
        return { type: route.type, id: route.id }
      }
    }
    return null
  })
}

export default {
  getCurrentRoute,
  getOpenTabs,
  getOpenObjects,
  getSelectedTab,
  createGetSelectedObject
}
