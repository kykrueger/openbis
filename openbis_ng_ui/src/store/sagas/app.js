import {call, put, putAndWait, takeEvery} from './effects.js'
import {facade, dto} from '../../services/openbis.js'
import * as actions from '../actions/actions.js'
import * as pages from '../consts/pages.js'

export default function* app() {
  yield takeEvery(actions.INIT, init)
  yield takeEvery(actions.LOGIN, login)
  yield takeEvery(actions.LOGOUT, logout)
  yield takeEvery(actions.CURRENT_PAGE_CHANGE, currentPageChange)
  yield takeEvery(actions.ERROR_CHANGE, errorChange)
}

function* init() {
  try{
    yield put(actions.setLoading(true))
    yield call([dto, dto.init])
    yield call([facade, facade.init])
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
      yield put(actions.setCurrentPage(pages.TYPES))
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

function* currentPageChange(action){
  yield put(actions.setCurrentPage(action.payload.currentPage))
}

function* errorChange(action){
  yield put(actions.setError(action.payload.error))
}
