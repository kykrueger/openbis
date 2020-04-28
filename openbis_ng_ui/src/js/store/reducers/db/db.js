import { combineReducers } from 'redux'
import actions from '@src/js/store/actions/actions.js'

export default combineReducers({
  lastObjectModifications
})

function lastObjectModifications(state = {}, action) {
  switch (action.type) {
    case actions.SET_LAST_OBJECT_MODIFICATION: {
      const { type, operation, timestamp } = action.payload
      if (
        !state[type] ||
        !state[type][operation] ||
        state[type][operation] < timestamp
      ) {
        return {
          ...state,
          [type]: { ...state[type], [operation]: timestamp }
        }
      } else {
        return state
      }
    }
    default: {
      return state
    }
  }
}
