import 'regenerator-runtime/runtime'
import React from 'react'
import ReactDOM from 'react-dom'
import store from './store/store.js'
import { Provider } from 'react-redux'
import ErrorBoundary from './components/common/error/ErrorBoundary.jsx'
import ThemeProvider from './components/common/theme/ThemeProvider.jsx'

const render = () => {
  let App = require('./components/App.jsx').default

  ReactDOM.render(
    <Provider store={store}>
      <ThemeProvider>
        <ErrorBoundary>
          <App />
        </ErrorBoundary>
      </ThemeProvider>
    </Provider>,
    document.getElementById('app')
  )
}

/* eslint-disable no-undef */
if (module.hot) {
  module.hot.accept('./components/App.jsx', () => setTimeout(render))
}

render()
