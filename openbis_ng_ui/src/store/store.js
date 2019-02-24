import { createStore, applyMiddleware, compose } from 'redux'
import createSagaMiddleware from 'redux-saga'
import reducer from './reducers/reducer.js'
import * as pageActions from './actions/page.js'
import { watchActions } from './sagas/sagas.js'

const sagaMiddleware = createSagaMiddleware()
const composeEnhancers = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose
export const store = createStore(reducer, composeEnhancers(applyMiddleware(sagaMiddleware)))

sagaMiddleware.run(watchActions)
store.dispatch({type: pageActions.INIT})

export default {
  store: store
}
