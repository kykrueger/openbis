import { combineReducers } from 'redux'
import * as pages from '../../../../../common/consts/pages.js'
import * as page from '../common/page.js'

export default function types(state = {}, action){
  if(page.isPageAction(pages.USERS, action)){
    return combineReducers({
      currentRoute: page.currentRoute,
      browser: page.browser,
      openObjects: page.openObjects,
      changedObjects: page.changedObjects
    })(state, action)
  }else{
    return state
  }
}
