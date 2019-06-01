import logger from './../../common/logger.js'

export const getOpenObjects = (state, page) => {
  logger.log(logger.DEBUG, 'pageSelector.getOpenObjects')
  return state.ui.pages[page].openObjects
}

export const getChangedObjects = (state, page) => {
  logger.log(logger.DEBUG, 'pageSelector.getChangedObjects')
  return state.ui.pages[page].changedObjects
}

export const getSelectedObject = (state, page) => {
  logger.log(logger.DEBUG, 'pageSelector.getSelectedObject')
  return state.ui.pages[page].selectedObject
}
