import _ from 'lodash'
import { put, take } from 'redux-saga/effects'

function generateCorrelationId() {
  let date = new Date()
  let timestamp =
    date.getUTCHours() + ':' + date.getUTCMinutes() + ':' + date.getUTCSeconds()
  let random = Math.floor(Math.random() * 1000000)
  return timestamp + '-' + random
}

export function* putAndWait(actionOrActionsMap) {
  let actionsMap = _.isString(actionOrActionsMap.type)
    ? { action: actionOrActionsMap }
    : actionOrActionsMap

  let correlationId = generateCorrelationId()
  let correlationKeys = []
  let actions = []

  _.forEach(actionsMap, (action, correlationKey) => {
    correlationKeys.push(correlationKey)
    actions.push(action)
  })

  for (let i = 0; i < actions.length; i++) {
    let actionWithCorrelation = Object.assign(actions[i], {
      meta: { correlation: { id: correlationId, key: correlationKeys[i] } }
    })
    yield put(actionWithCorrelation)
  }

  let responsesMap = {}

  while (_.size(responsesMap) < correlationKeys.length) {
    let potentialResponse = yield take('*')
    if (
      potentialResponse.meta &&
      potentialResponse.meta.correlation &&
      potentialResponse.meta.correlation.id === correlationId
    ) {
      if (potentialResponse.payload.error) {
        throw potentialResponse.payload.error
      }
      let correlationKey = potentialResponse.meta.correlation.key
      responsesMap[correlationKey] = potentialResponse
    }
  }

  return _.isString(actionOrActionsMap.type)
    ? responsesMap.action
    : responsesMap
}

export * from 'redux-saga/effects'
