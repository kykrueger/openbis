import 'regenerator-runtime/runtime'
import React from 'react'
import ReactDOM from 'react-dom'
import store from './store/store.js'
import { Provider } from 'react-redux'
import { MuiThemeProvider, createMuiTheme } from '@material-ui/core/styles'
import indigo from '@material-ui/core/colors/indigo'
import lightBlue from '@material-ui/core/colors/lightBlue'

const theme = createMuiTheme({
  typography: {
    useNextVariants: true
  },
  palette: {
    grey: {
      main: '#5d5d5d'
    },
    primary: {
      main: indigo[700]
    },
    secondary: {
      main: lightBlue[600]
    },
    warning: {
      main: '#ff9609'
    },
    background: {
      primary: '#ebebeb',
      secondary: '#dbdbdb'
    }
  }
})

const render = () => {
  let App = require('./components/App.jsx').default

  ReactDOM.render(
    <Provider store={store}>
      <MuiThemeProvider theme={theme}>
        <App />
      </MuiThemeProvider>
    </Provider>,
    document.getElementById('app')
  )
}

/* eslint-disable no-undef */
if (module.hot) {
  module.hot.accept('./components/App.jsx', () => setTimeout(render))
}

render()
