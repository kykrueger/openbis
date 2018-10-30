/* eslint-disable */
import "regenerator-runtime/runtime"
import React from 'react'
import ReactDOM from 'react-dom'
import { createStore, applyMiddleware, compose } from 'redux'
import { Provider } from 'react-redux'
import createSagaMiddleware from 'redux-saga'
import reducer from './reducer/reducer.js'
import { watchInit, watchSetSpaces, watchSaveEntity, watchExpandNode } from './reducer/sagas'

const sagaMiddleware = createSagaMiddleware()
const composeEnhancers = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose;
export const store = createStore(reducer, composeEnhancers(applyMiddleware(sagaMiddleware)))

sagaMiddleware.run(watchInit)
sagaMiddleware.run(watchSetSpaces)
sagaMiddleware.run(watchSaveEntity)
sagaMiddleware.run(watchExpandNode)

const render = () => {
  const App = require('./components/App.jsx').default
  ReactDOM.render(
    <Provider store = { store }>
      <App />
    </Provider>,
    document.getElementById("app")
  )
}

if (module.hot) {
  module.hot.accept('./components/App.jsx', () => setTimeout(render))  
  module.hot.accept('./reducer/reducer.js', () => {
    const nextRootReducer = require('./reducer/reducer.js').default
    store.replaceReducer(nextRootReducer)
  });
}
render()