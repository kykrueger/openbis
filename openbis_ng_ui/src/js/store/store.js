import { createStore, applyMiddleware, compose } from 'redux'
import createSagaMiddleware from 'redux-saga'
import middlewares from '@src/js/store/middleware/middlewares.js'
import rootReducer from '@src/js/store/reducers/reducers.js'
import rootSaga from '@src/js/store/sagas/sagas.js'
import history from '@src/js/store/history.js'

function createStoreWithMiddleware() {
  const sagaMiddleware = createSagaMiddleware()
  const composeEnhancers =
    (window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ &&
      window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__({
        trace: true,
        traceLimit: 25
      })) ||
    compose

  let store = createStore(
    rootReducer,
    composeEnhancers(applyMiddleware(...middlewares, sagaMiddleware))
  )
  sagaMiddleware.run(rootSaga)

  history.configure(store)

  return store
}

let store = createStoreWithMiddleware()

/* eslint-disable no-undef */
if (module.hot) {
  module.hot.accept('./reducers/reducers.js', () => {
    const nextRootReducer = require('./reducers/reducers.js').default
    store.replaceReducer(nextRootReducer)
  })
}

export { createStoreWithMiddleware as createStore }
export default store
