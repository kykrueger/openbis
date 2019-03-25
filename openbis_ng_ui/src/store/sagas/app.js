import {put, putAndWait, takeEvery} from './effects.js'
import * as actions from '../actions/actions.js'
import * as pages from '../consts/pages.js'

export default function* app() {
  yield takeEvery(actions.INIT, init)
  yield takeEvery(actions.LOGIN, login)
  yield takeEvery(actions.LOGOUT, logout)
  yield takeEvery(actions.CURRENT_PAGE_CHANGED, currentPageChanged)
  yield takeEvery(actions.ERROR_CHANGED, errorChanged)
}

function* init() {
  try{
    yield putAndWait(actions.apiRequest({method: 'init'}))
    yield put(actions.setInitialized(true))
  }catch(e){
    yield put(actions.setError(e))
  }
}

function* login(action) {
  try{
    let loginResponse = yield putAndWait(actions.apiRequest({method: 'login', params: [action.payload.username, action.payload.password]}))

    if(loginResponse.payload.result){
      yield put(actions.setCurrentPage(pages.USERS))
      yield put(actions.setSession(loginResponse.payload.result))
    }else{
      throw { message: 'Incorrect used or password' }
    }
  }catch(e){
    yield put(actions.setError(e))
  }
}

function* logout() {
  try{
    yield putAndWait(actions.apiRequest({method: 'logout'}))
    yield put(actions.init())
  }catch(e){
    yield put(actions.setError(e))
  }
}

function* currentPageChanged(action){
  yield put(actions.setCurrentPage(action.payload.currentPage))
}

function* errorChanged(action){
  yield put(actions.setError(action.payload.error))
}
