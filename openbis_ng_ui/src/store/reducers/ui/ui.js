import { combineReducers } from 'redux'
import * as actions from '../../actions/actions.js'
import pages from './pages/pages.js'

export default combineReducers({
  initialized,
  loading,
  currentPage,
  pages,
  error
})

function initialized(state = false, action){
  switch(action.type){
    case actions.SET_INITIALIZED:
      return action.payload.initialized
    default:
      return state
  }
}

function loading(state = false, action){
  switch (action.type) {
    case actions.SET_LOADING: {
      return action.payload.loading
    }
    default: {
      return state
    }
  }
}

function currentPage(state = null, action){
  switch(action.type){
    case actions.SET_CURRENT_PAGE:
      return action.payload.currentPage
    default:
      return state
  }
}

function error(state = null, action){
  switch (action.type) {
    case actions.SET_ERROR: {
      return action.payload.error
    }
    default: {
      return state
    }
  }
}
