import { combineReducers } from 'redux'
import * as pages from '../../../../consts/pages.js'
import { browser, isBrowserAction } from '../common/browser.js'

export default function types(state = {}, action){
  if(isBrowserAction(pages.TYPES, action)){
    return combineReducers({browser})(state, action)
  }else{
    return state
  }
}
