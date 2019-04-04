import * as actions from '../actions/actions.js'

const MIN_LOADING_TIME = 500
let startLoadingTimestamp = null

export default () => (next) => (action) => {
  if(action.type === actions.SET_LOADING){
    if(action.payload.loading){
      if(startLoadingTimestamp === null){
        startLoadingTimestamp = new Date().getTime()
      }
      next(action)
    }else{
      let timeToWait = startLoadingTimestamp + MIN_LOADING_TIME - new Date().getTime()
      if(timeToWait > 0){
        setTimeout(() => {
          startLoadingTimestamp = null
          next(action)
        }, timeToWait)
      }else{
        startLoadingTimestamp = null
        next(action)
      }
    }
  }else{
    next(action)
  }
}
