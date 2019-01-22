import React from 'react'
import {connect} from 'react-redux'
import AppBar from '@material-ui/core/AppBar'
import Toolbar from '@material-ui/core/Toolbar'
import Tabs from '@material-ui/core/Tabs'
import Tab from '@material-ui/core/Tab'
import {withStyles} from '@material-ui/core/styles'
import actions from '../reducer/actions.js'

/*eslint-disable-next-line no-unused-vars*/
const styles = theme => ({
  browserTabs: {
    width: '100%'
  },
  browserTab: {
    minWidth: '50px'
  }
})

function mapStateToProps(state) {
  return {
    mode: state.mode
  }
}

function mapDispatchToProps(dispatch) {
  return {
    setMode: (event, value) => {
      dispatch(actions.setMode(value))
    }
  }
}

class ModeBar extends React.Component {

  render() {
    const classes = this.props.classes

    return (
      <AppBar position="static">
        <Toolbar>
          <Tabs value={this.props.mode} onChange={this.props.setMode} fullWidth className={classes.browserTabs}>
            <Tab value="DATABASE" className={classes.browserTab} label="Database"/>
            <Tab value="TYPES" className={classes.browserTab} label="Types"/>
            <Tab value="USERS" className={classes.browserTab} label="Users"/>
            <Tab value="FAVOURITES" className={classes.browserTab} label="Favourites"/>
            <Tab value="TOOLS" className={classes.browserTab} label="Tools"/>
          </Tabs>
        </Toolbar>
      </AppBar>
    )
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(withStyles(styles)(ModeBar))
