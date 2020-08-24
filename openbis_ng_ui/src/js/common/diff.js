import _ from 'lodash'
import logger from '@src/js/common/logger.js'

export default function diff(beforeState, afterState, path) {
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
