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
    yield put(actions.setLoading(true))
    yield putAndWait(actions.apiRequest({method: 'init'}))
  }catch(e){
    yield put(actions.setError(e))
  }finally{
    yield put(actions.setLoading(false))
  }
}

function* login(action) {
  try{
    yield put(actions.setLoading(true))

    let loginResponse = yield putAndWait(actions.apiRequest({method: 'login', params: [action.payload.username, action.payload.password]}))

    if(loginResponse.payload.result){
      yield put(actions.setCurrentPage(pages.USERS))
      yield put(actions.setSession(loginResponse.payload.result))
    }else{
      throw { message: 'Incorrect used or password' }
    }
  }catch(e){
    yield put(actions.setError(e))
  }finally{
    yield put(actions.setLoading(false))
  }
}

function* logout() {
  try{
    yield put(actions.setLoading(true))
    yield putAndWait(actions.apiRequest({method: 'logout'}))
    yield put(actions.init())
  }catch(e){
    yield put(actions.setError(e))
  }finally{
    yield put(actions.setLoading(false))
  }
}

function* currentPageChanged(action){
  yield put(actions.setCurrentPage(action.payload.currentPage))
}

function* errorChanged(action){
  yield put(actions.setError(action.payload.error))
}
