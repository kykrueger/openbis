import 'regenerator-runtime/runtime'
import React from 'react'
import ReactDOM from 'react-dom'
import { Provider } from 'react-redux'

const render = () => {
  const App = require('./components/App.jsx').default
  const store = require('./store/store.js').default
  ReactDOM.render(
    <Provider store = { store }>
      <App />
    </Provider>,
    document.getElementById('app')
  )
}

/* eslint-disable no-undef */
if (module.hot) {
  module.hot.accept('./components/App.jsx', () => setTimeout(render))
  module.hot.accept('./store/reducers/reducers.js', () => {
    const nextRootReducer = require('./store/reducers/reducers.js').default
    store.replaceReducer(nextRootReducer)
  })
}

render()
