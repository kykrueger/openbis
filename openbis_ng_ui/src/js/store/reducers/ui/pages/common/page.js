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

const openObjects = (state = [], action) => {
  let newState = null

  switch (action.type) {
    case actions.ADD_OPEN_OBJECT: {
      newState = _.unionWith(
        state,
        [{ type: action.payload.type, id: action.payload.id }],
        _.isEqual
      )
      break
    }
    case actions.REMOVE_OPEN_OBJECT: {
      newState = _.differenceWith(
        state,
        [{ type: action.payload.type, id: action.payload.id }],
        _.isEqual
      )
      break
    }
    case actions.REPLACE_OPEN_OBJECT: {
      const index = _.findIndex(
        state,
        {
          type: action.payload.oldType,
          id: action.payload.oldId
        },
        _.isEqual
      )
      if (index !== -1) {
        newState = [...state]
        newState[index] = {
          type: action.payload.newType,
          id: action.payload.newId
        }
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

const changedObjects = (state = [], action) => {
  let newState = null

  switch (action.type) {
    case actions.ADD_CHANGED_OBJECT: {
      newState = _.unionWith(
        state,
        [{ type: action.payload.type, id: action.payload.id }],
        _.isEqual
      )
      break
    }
    case actions.REMOVE_CHANGED_OBJECT: {
      newState = _.differenceWith(
        state,
        [{ type: action.payload.type, id: action.payload.id }],
        _.isEqual
      )
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
  openObjects,
  changedObjects
}
