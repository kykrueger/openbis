import { push, LOCATION_CHANGE } from 'connected-react-router'
import {call, put, putAndWait, takeEvery, select} from './effects.js'
import {facade, dto} from '../../services/openbis.js'
import * as selectors from '../selectors/selectors.js'
import * as actions from '../actions/actions.js'
import * as pages from '../../common/consts/pages.js'
import routes from '../../common/consts/routes.js'
import * as objectTypes from '../../common/consts/objectType.js'

export default function* appSaga() {
  yield takeEvery(LOCATION_CHANGE, locationChange)
  yield takeEvery(actions.INIT, init)
  yield takeEvery(actions.LOGIN, login)
  yield takeEvery(actions.LOGOUT, logout)
  yield takeEvery(actions.SEARCH, search)
  yield takeEvery(actions.CURRENT_PAGE_CHANGE, currentPageChange)
  yield takeEvery(actions.SEARCH_CHANGE, searchChange)
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

    let { username, password } = action.payload
    let loginResponse = yield putAndWait(actions.apiRequest({method: 'login', params: [ username, password ]}))

    if(loginResponse.payload.result){
      yield put(actions.currentPageChange(pages.TYPES))
      yield put(actions.setSession({
        sessionToken: loginResponse.payload.result,
        userName: username
      }))
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

function* search(action) {
  const {page, text} = action.payload
  yield put(actions.objectOpen(page, objectTypes.SEARCH, text.trim()))
  yield put(actions.setSearch(''))
}

function* currentPageChange(action){
  let page = action.payload.currentPage
  let route = yield select(selectors.getCurrentRoute, page)

  if(route){
    yield put(push(route))
  }else{
    route = routes.format({ page })
    yield put(push(route))
  }
}

function* searchChange(action){
  yield put(actions.setSearch(action.payload.search))
}

function* errorChange(action){
  yield put(actions.setError(action.payload.error))
}

function* locationChange(action){
  let route = routes.parse(action.payload.location.pathname)

  if(route){
    yield put(actions.routeChange(route))
  }
}
