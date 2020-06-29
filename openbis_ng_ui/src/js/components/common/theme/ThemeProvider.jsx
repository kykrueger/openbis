import React from 'react'
import { MuiThemeProvider, createMuiTheme } from '@material-ui/core/styles'
import indigo from '@material-ui/core/colors/indigo'
import lightBlue from '@material-ui/core/colors/lightBlue'

const theme = createMuiTheme({
  typography: {
    useNextVariants: true,
    label: {
      fontSize: '0.7rem',
      color: '#0000008a'
    }
  },
  palette: {
    primary: {
      main: indigo[700]
    },
    secondary: {
      main: lightBlue[600]
    },
    info: {
      main: lightBlue[600]
    },
    warning: {
      main: '#ff9609'
    },
    background: {
      primary: '#ebebeb',
      secondary: '#f5f5f5'
    },
    border: {
      primary: '#dbdbdb',
      secondary: '#ebebeb'
    }
  }
})

export default class ThemeProvider extends React.Component {
  render() {
    return (
      <MuiThemeProvider theme={theme}>{this.props.children}</MuiThemeProvider>
    )
  }
}
