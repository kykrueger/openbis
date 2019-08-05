import {call, put, putAndWait, takeEvery, select} from './effects.js'
import {facade, dto} from '../../services/openbis.js'
import * as selectors from '../selectors/selectors.js'
import * as actions from '../actions/actions.js'
import * as objectTypes from '../../common/consts/objectType.js'
import routes from '../../common/consts/routes.js'

export default function* appSaga() {
  yield takeEvery(actions.INIT, init)
  yield takeEvery(actions.LOGIN, login)
  yield takeEvery(actions.LOGOUT, logout)
  yield takeEvery(actions.SEARCH, search)
  yield takeEvery(actions.CURRENT_PAGE_CHANGE, currentPageChange)
  yield takeEvery(actions.SEARCH_CHANGE, searchChange)
  yield takeEvery(actions.ERROR_CHANGE, errorChange)
  yield takeEvery(actions.ROUTE_CHANGE, routeChange)
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
      yield put(actions.setSession({
        sessionToken: loginResponse.payload.result,
        userName: username
      }))

      let path = yield select(selectors.getRoute)
      let route = routes.parse(path)
      yield put(actions.routeChange(route.path))
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
    yield put(actions.routeChange('/'))
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
    yield put(actions.routeChange(route))
  }else{
    route = routes.format({ page })
    yield put(actions.routeChange(route))
  }
}

function* searchChange(action){
  yield put(actions.setSearch(action.payload.search))
}

function* errorChange(action){
  yield put(actions.setError(action.payload.error))
}

function* routeChange(action){
  const route = action.payload.route
  yield put(actions.setRoute(route))
}
