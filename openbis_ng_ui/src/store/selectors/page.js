import { createSelector } from 'reselect'
import logger from './../../common/logger.js'
import routes from '../../common/consts/routes.js'

export const getCurrentRoute = (state, page) => {
  logger.log(logger.DEBUG, 'pageSelector.getCurrentRoute')
  return state.ui.pages[page].currentRoute
}

export const getOpenObjects = (state, page) => {
  logger.log(logger.DEBUG, 'pageSelector.getOpenObjects')
  return state.ui.pages[page].openObjects
}

export const getChangedObjects = (state, page) => {
  logger.log(logger.DEBUG, 'pageSelector.getChangedObjects')
  return state.ui.pages[page].changedObjects
}

export const createGetSelectedObject = () => {
  return createSelector(
    [getCurrentRoute],
    path => {
      logger.log(logger.DEBUG, 'pageSelector.getSelectedObject')
      if (path) {
        let route = routes.parse(path)
        if (route && route.type && route.id) {
          return { type: route.type, id: route.id }
        }
      }
      return null
    }
  )
}
