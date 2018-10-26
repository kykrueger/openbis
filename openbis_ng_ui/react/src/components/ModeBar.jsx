import React from 'react'
import AppBar from '@material-ui/core/AppBar'
import Toolbar from '@material-ui/core/Toolbar'
import Tabs from '@material-ui/core/Tabs'
import Tab from '@material-ui/core/Tab'
import { withStyles } from '@material-ui/core/styles'

/*eslint-disable-next-line no-unused-vars*/
const styles = theme => ({
  browserTabs: {
    width: '100%'
  },
  browserTab: {
    minWidth: '50px'
  }
})

class ModeBar extends React.Component {

  render() {
    const classes = this.props.classes

    return (
      <AppBar position="static">
        <Toolbar>
          <Tabs value={0} fullWidth className = { classes.browserTabs }>
            <Tab className = { classes.browserTab } label="Database" />
            <Tab className = { classes.browserTab } label="Types" />
            <Tab className = { classes.browserTab } label="Users" />
            <Tab className = { classes.browserTab } label="Favourites" />
            <Tab className = { classes.browserTab } label="Tools" />
          </Tabs>
        </Toolbar>
      </AppBar>
    )
  }
}

export default withStyles(styles)(ModeBar)
