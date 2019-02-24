import 'regenerator-runtime/runtime'
import React from 'react'
import ReactDOM from 'react-dom'
import { Provider } from 'react-redux'


const render = () => {
  const App = require('./components/App.jsx').default
  const WithLogin = require('./components/WithLogin.jsx').default
  const WithLoader = require('./components/WithLoader.jsx').default
  const WithError = require('./components/WithError.jsx').default
  const store = require('./store/store.js').default.store
  ReactDOM.render(
    <Provider store = { store }>
      <WithLoader>
        <WithError>
          <WithLogin>
            <App />
          </WithLogin>
        </WithError>
      </WithLoader>
    </Provider>,
    document.getElementById('app')
  )
}

if (module.hot) {
  module.hot.accept('./components/App.jsx', () => setTimeout(render))
  module.hot.accept('./store/reducers/reducer.js', () => {
    const nextRootReducer = require('./store/reducers/reducer.js').default
    store.replaceReducer(nextRootReducer)
  })
}
render()
