import { combineReducers } from 'redux'
import * as actions from '../../actions/actions.js'
import pages from './pages/pages.js'

export default combineReducers({
  loading,
  search,
  currentPage,
  pages,
  error
})

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

function search(state = '', action){
  switch (action.type) {
    case actions.SET_SEARCH: {
      return action.payload.search
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
