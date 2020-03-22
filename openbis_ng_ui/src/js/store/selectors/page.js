import { createSelector } from 'reselect'
import logger from '@src/js/common/logger.js'
import routes from '@src/js/common/consts/routes.js'

const getCurrentRoute = (state, page) => {
  logger.log(logger.DEBUG, 'pageSelector.getCurrentRoute')
  return state.ui.pages[page].currentRoute
}

const getOpenObjects = (state, page) => {
  logger.log(logger.DEBUG, 'pageSelector.getOpenObjects')
  return state.ui.pages[page].openObjects
}

const getChangedObjects = (state, page) => {
  logger.log(logger.DEBUG, 'pageSelector.getChangedObjects')
  return state.ui.pages[page].changedObjects
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
  getOpenObjects,
  getChangedObjects,
  createGetSelectedObject
}
