import { all } from 'redux-saga/effects'
import app from './app.js'
import api from './api.js'
import page from './page.js'

export default function* root() {
  yield all([api(), app(), page()])
}
