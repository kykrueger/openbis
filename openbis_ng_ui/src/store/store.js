import { createStore, applyMiddleware, compose } from 'redux'
import createSagaMiddleware from 'redux-saga'
import middlewares from './middleware/middlewares.js'
import rootReducer from './reducers/reducers.js'
import rootSaga from './sagas/sagas.js'
import history from './history.js'

function createStoreWithMiddleware(){
  const sagaMiddleware = createSagaMiddleware()
  const composeEnhancers = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose

  let store = createStore(rootReducer, composeEnhancers(applyMiddleware(...middlewares, sagaMiddleware)))
  sagaMiddleware.run(rootSaga)

  history.configure(store)

  return store
}

let store = createStoreWithMiddleware()

export { createStoreWithMiddleware as createStore }
export default store
