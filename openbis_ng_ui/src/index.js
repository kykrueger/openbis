import 'regenerator-runtime/runtime'
import React from 'react'
import ReactDOM from 'react-dom'
import { Provider } from 'react-redux'
import { MuiThemeProvider, createMuiTheme } from '@material-ui/core/styles'
import DragAndDropProvider from './components/common/dnd/DragAndDropProvider.jsx'
import indigo from '@material-ui/core/colors/indigo'
import lightBlue from '@material-ui/core/colors/lightBlue'

const theme = createMuiTheme({
  typography: {
    useNextVariants: true
  },
  palette: {
    primary: {
      main: indigo[700]
    },
    secondary: {
      main: lightBlue[600]
    }
  }
})

const render = () => {
  const App = require('./components/App.jsx').default
  const store = require('./store/store.js').default

  ReactDOM.render(
    <Provider store = { store }>
      <MuiThemeProvider theme={ theme }>
        <DragAndDropProvider>
            <App />
        </DragAndDropProvider>
      </MuiThemeProvider>
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
