import _ from 'lodash'
import * as actions from '../../../../actions/actions.js'

export * from './browser.js'

export function isPageAction(page, action){
  return action.type === actions.INIT || page === (action.payload && action.payload.page)
}

export const openObjects = (state = [], action) => {
  let newState = null

  switch(action.type){
    case actions.ADD_OPEN_OBJECT:{
      newState = _.unionWith(state, [{type: action.payload.type, id: action.payload.id}], _.isEqual)
      break
    }
    case actions.REMOVE_OPEN_OBJECT:{
      newState = _.differenceWith(state, [{type: action.payload.type, id: action.payload.id}], _.isEqual)
      break
    }
    default:{
      return state
    }
  }

  if(_.isEqual(state, newState)){
    return state
  } else {
    return newState
  }
}

export const changedObjects = (state = [], action) => {
  let newState = null

  switch(action.type){
    case actions.ADD_CHANGED_OBJECT:{
      newState = _.unionWith(state, [{type: action.payload.type, id: action.payload.id}], _.isEqual)
      break
    }
    case actions.REMOVE_CHANGED_OBJECT:{
      newState = _.differenceWith(state, [{type: action.payload.type, id: action.payload.id}], _.isEqual)
      break
    }
    default:{
      return state
    }
  }

  if(_.isEqual(state, newState)){
    return state
  } else {
    return newState
  }
}

export const selectedObject = (state = null, action) => {
  switch(action.type){
    case actions.SET_SELECTED_OBJECT: {
      let newState = action.payload.type && action.payload.id ? { type: action.payload.type, id: action.payload.id } : null

      if(_.isEqual(state, newState)){
        return state
      }else{
        return newState
      }
    }
    default:{
      return state
    }
  }
}
