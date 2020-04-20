import _ from 'lodash'
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

function diff(beforeState, afterState, path) {
  let unmodifiedNewObjects = false

  if (_.isObject(beforeState) && _.isObject(afterState)) {
    if (beforeState === afterState) {
      logger.log(logger.TRACE, 'OK - ' + path + ' - same object')
    } else {
      if (_.isEqual(beforeState, afterState)) {
        logger.log(
          logger.DEBUG,
          'ERROR - ' + path + ' - new object without changes',
          afterState
        )
        unmodifiedNewObjects = true
      } else {
        logger.log(
          logger.DEBUG,
          'OK* - ' + path + ' - new object with changes',
          beforeState,
          afterState
        )
      }

      let props = _.union(_.keys(beforeState), _.keys(afterState))
      props.forEach(prop => {
        let beforeProp = beforeState ? beforeState[prop] : undefined
        let afterProp = afterState ? afterState[prop] : undefined
        unmodifiedNewObjects =
          unmodifiedNewObjects | diff(beforeProp, afterProp, path + '/' + prop)
      })
    }
  } else {
    if (_.isEqual(beforeState, afterState)) {
      logger.log(logger.TRACE, 'OK - ' + path + ' - same value', afterState)
    } else {
      logger.log(
        logger.DEBUG,
        'OK* - ' + path + ' - changed value',
        beforeState,
        afterState
      )
    }
  }

  return unmodifiedNewObjects
}
