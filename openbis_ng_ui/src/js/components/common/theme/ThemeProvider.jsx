import React from 'react'
import { MuiThemeProvider, createMuiTheme } from '@material-ui/core/styles'
import indigo from '@material-ui/core/colors/indigo'
import lightBlue from '@material-ui/core/colors/lightBlue'

const theme = createMuiTheme({
  typography: {
    useNextVariants: true
  },
  palette: {
    grey: {
      main: '#757575'
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

export default class ThemeProvider extends React.Component {
  render() {
    return (
      <MuiThemeProvider theme={theme}>{this.props.children}</MuiThemeProvider>
    )
  }
}
