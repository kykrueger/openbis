import { combineReducers } from 'redux'
import actions from '@src/js/store/actions/actions.js'
import pages from '@src/js/store/reducers/ui/pages/pages.js'

export default combineReducers({
  loading,
  search,
  pages,
  error
})

function loading(state = false, action) {
  switch (action.type) {
    case actions.SET_LOADING: {
      return action.payload.loading
    }
    default: {
      return state
    }
  }
}

function search(state = '', action) {
  switch (action.type) {
    case actions.SET_SEARCH: {
      return action.payload.search
    }
    default: {
      return state
    }
  }
}

function error(state = null, action) {
  switch (action.type) {
    case actions.SET_ERROR: {
      return action.payload.error
    }
    default: {
      return state
    }
  }
}
