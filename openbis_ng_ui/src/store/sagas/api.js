import {put, takeEvery, apply} from 'redux-saga/effects'
import openbis from '../../services/openbis.js'
import * as actions from '../actions/actions.js'

export default function* api() {
  yield takeEvery(actions.API_REQUEST, apiRequest)
}

function* apiRequest(action){
  const { method, params } = action.payload

  try{
    let result = yield apply(openbis, openbis[method], params || [])
    yield put(actions.apiSuccess({result, meta: Object.assign({}, action.meta, {method, params})}))
  }catch(error){
    yield put(actions.apiError({error, meta: Object.assign({}, action.meta, {method, params})}))
  }
}
