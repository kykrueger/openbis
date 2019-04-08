import logger from './../../common/logger.js'

export const getOpenObjects = (state, page) => {
  logger.log(logger.DEBUG, 'pageSelector.getOpenObjects')
  return state.ui.pages[page].openObjects
}

export const getSelectedObject = (state, page) => {
  logger.log(logger.DEBUG, 'pageSelector.getSelectedObject')
  return state.ui.pages[page].selectedObject
}
