import React from 'react'
import AppBar from '@material-ui/core/AppBar'
import Toolbar from '@material-ui/core/Toolbar'
import Tabs from '@material-ui/core/Tabs'
import Tab from '@material-ui/core/Tab'
import Button from '@material-ui/core/Button'
import LogoutIcon from '@material-ui/icons/PowerSettingsNew'
import {connect} from 'react-redux'
import {withStyles} from '@material-ui/core/styles'
import logger from '../../../common/logger.js'
import * as actions from '../../../store/actions/actions.js'
import * as selectors from '../../../store/selectors/selectors.js'
import * as pages from '../../../store/consts/pages.js'

const styles = () => ({
  tabs: {
    'flex-grow': '8',
  }
})

function mapStateToProps(state){
  return {
    currentPage: selectors.getCurrentPage(state)
  }
}

function mapDispatchToProps(dispatch){
  return {
    currentPageChange: (event, value) => dispatch(actions.currentPageChange(value)),
    logout: () => dispatch(actions.logout())
  }
}

class Menu extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'Menu.render')

    const classes = this.props.classes

    return (
      <AppBar position="static">
        <Toolbar>
          <Tabs value={this.props.currentPage}
            onChange={this.props.currentPageChange}
            classes={{root: classes.tabs}}>
            <Tab value={pages.TYPES} label="Types"/>
            <Tab value={pages.USERS} label="Users"/>
          </Tabs>
          <Button
            variant="contained"
            color="primary"
            onClick={this.props.logout}>
            <LogoutIcon/>
          </Button>
        </Toolbar>
      </AppBar>
    )
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(withStyles(styles)(Menu))
