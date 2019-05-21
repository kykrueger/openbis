import { combineReducers } from 'redux'
import * as pages from '../../../../consts/pages.js'
import * as page from '../common/page.js'

export default function types(state = {}, action){
  if(page.isPageAction(pages.USERS, action)){
    return combineReducers({
      browser: page.browser,
      openObjects: page.openObjects,
      selectedObject: page.selectedObject
    })(state, action)
  }else{
    return state
  }
}
