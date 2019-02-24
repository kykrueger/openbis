import {all} from 'redux-saga/effects'

import {watchActions as login} from './login.js'
import {watchActions as page} from './page.js'

export default function* rootSaga() {
  yield all([
    login(),
    page()
  ])
}
