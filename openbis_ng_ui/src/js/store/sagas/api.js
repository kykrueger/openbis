import { put, takeEvery, apply } from 'redux-saga/effects'
import openbis from '@src/js/services/openbis.js'
import actions from '@src/js/store/actions/actions.js'

export default function* apiSaga() {
  yield takeEvery(actions.API_REQUEST, apiRequest)
}

function* apiRequest(action) {
  const { method, params } = action.payload

  try {
    let result = yield apply(openbis, openbis[method], params || [])
    yield put(
      actions.apiSuccess({
        result,
        meta: Object.assign({}, action.meta, { method, params })
      })
    )
  } catch (error) {
    yield put(
      actions.apiError({
        error,
        meta: Object.assign({}, action.meta, { method, params })
      })
    )
  }
}
