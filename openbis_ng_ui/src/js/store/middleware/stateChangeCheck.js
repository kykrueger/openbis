import _ from 'lodash'
import diff from '@src/js/common/diff.js'
import logger from '@src/js/common/logger.js'

export default store => next => action => {
  if (logger.isLevelEnabled(logger.DEBUG)) {
    logger.group(logger.DEBUG, 'Action ' + action.type, action)

    let beforeState = store.getState()
    let beforeStateClone = _.cloneDeep(beforeState)
    next(action)
    let afterState = store.getState()

    logger.group(logger.DEBUG, 'State diff')
    let unmodifiedNewObjects = diff(beforeState, afterState, 'root')
    let modifiedPreviousState = !_.isEqual(beforeState, beforeStateClone)
    logger.groupEnd(logger.DEBUG)

    if (unmodifiedNewObjects || modifiedPreviousState) {
      if (unmodifiedNewObjects) {
        logger.log(
          logger.ERROR,
          'ERROR state changed incorrectly - returned new objects without changes',
          beforeState,
          afterState
        )
      }
      if (modifiedPreviousState) {
        logger.log(
          logger.ERROR,
          'ERROR state changed incorrectly - reducer modified previous state object',
          beforeStateClone,
          beforeState,
          afterState
        )
      }
    } else {
      if (_.isEqual(beforeState, afterState)) {
        logger.log(logger.DEBUG, 'OK state not changed', afterState)
      } else {
        logger.log(
          logger.DEBUG,
          'OK state changed correctly',
          beforeState,
          afterState
        )
      }
    }

    logger.groupEnd(logger.DEBUG)
  } else {
    next(action)
  }
}
