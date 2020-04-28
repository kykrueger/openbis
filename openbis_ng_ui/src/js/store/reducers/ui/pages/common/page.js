import _ from 'lodash'
import actions from '@src/js/store/actions/actions.js'

const isPageAction = (page, action) => {
  return (
    action.type === actions.INIT ||
    action.type === actions.CLEAR ||
    page === (action.payload && action.payload.page)
  )
}

const currentRoute = (state = null, action) => {
  switch (action.type) {
    case actions.SET_CURRENT_ROUTE: {
      return action.payload.currentRoute
    }
    default: {
      return state
    }
  }
}

const openTabs = (state = [], action) => {
  let newState = null

  switch (action.type) {
    case actions.ADD_OPEN_TAB: {
      const index = _.findIndex(state, { id: action.payload.id }, _.isMatch)
      if (index !== -1) {
        return state
      } else {
        newState = Array.from(state)
        newState.push(action.payload.tab)
      }
      break
    }
    case actions.REMOVE_OPEN_TAB: {
      const index = _.findIndex(state, { id: action.payload.id }, _.isMatch)
      if (index !== -1) {
        newState = Array.from(state)
        newState.splice(index, 1)
      } else {
        return state
      }
      break
    }
    case actions.REPLACE_OPEN_TAB: {
      const index = _.findIndex(state, { id: action.payload.id }, _.isMatch)
      if (index !== -1) {
        newState = Array.from(state)
        newState[index] = action.payload.tab
      } else {
        return state
      }
      break
    }
    default: {
      return state
    }
  }

  if (_.isEqual(state, newState)) {
    return state
  } else {
    return newState
  }
}

export default {
  isPageAction,
  currentRoute,
  openTabs
}
