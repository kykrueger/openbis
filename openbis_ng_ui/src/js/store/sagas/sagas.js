import { all } from 'redux-saga/effects'
import app from '@src/js/store/sagas/app.js'
import api from '@src/js/store/sagas/api.js'
import page from '@src/js/store/sagas/page.js'

export default function* root() {
  yield all([api(), app(), page()])
}
