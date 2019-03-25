import { createStore, applyMiddleware, compose } from 'redux'
import createSagaMiddleware from 'redux-saga'
import rootReducer from './reducers/reducers.js'
import rootSaga from './sagas/sagas.js'

function createStoreWithMiddleware(){
  const sagaMiddleware = createSagaMiddleware()
  const composeEnhancers = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose
  let store = createStore(rootReducer, composeEnhancers(applyMiddleware(sagaMiddleware)))
  sagaMiddleware.run(rootSaga)
  return store
}

let store = createStoreWithMiddleware()

export { createStoreWithMiddleware as createStore }
export default store
