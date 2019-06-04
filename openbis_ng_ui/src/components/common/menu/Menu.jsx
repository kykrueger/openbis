import React from 'react'
import AppBar from '@material-ui/core/AppBar'
import Toolbar from '@material-ui/core/Toolbar'
import Tabs from '@material-ui/core/Tabs'
import Tab from '@material-ui/core/Tab'
import TextField from '@material-ui/core/TextField'
import InputAdornment from '@material-ui/core/InputAdornment'
import Button from '@material-ui/core/Button'
import SearchIcon from '@material-ui/icons/Search'
import CloseIcon from '@material-ui/icons/Close'
import LogoutIcon from '@material-ui/icons/PowerSettingsNew'
import { fade } from '@material-ui/core/styles/colorManipulator'
import {connect} from 'react-redux'
import {withStyles} from '@material-ui/core/styles'
import logger from '../../../common/logger.js'
import * as actions from '../../../store/actions/actions.js'
import * as selectors from '../../../store/selectors/selectors.js'
import * as pages from '../../../store/consts/pages.js'

const styles = (theme) => ({
  tabs: {
    flexGrow: 1,
  },
  search: {
    color: theme.palette.common.white,
    backgroundColor: fade(theme.palette.common.white, 0.15),
    '&:hover': {
      backgroundColor: fade(theme.palette.common.white, 0.25),
    },
    borderRadius: theme.shape.borderRadius,
    paddingLeft: theme.spacing.unit,
    paddingRight: theme.spacing.unit,
    marginRight: theme.spacing.unit * 2,
  },
  searchInput: {
    transition: theme.transitions.create('width'),
    width: '200px',
    '&:focus': {
      width: '300px',
    },
  },
  searchIcon: {
    paddingLeft: theme.spacing.unit / 2,
    paddingRight: theme.spacing.unit,
    cursor: 'default'
  },
  searchClear: {
    cursor: 'pointer'
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
          <TextField
            placeholder="Search..."
            InputProps={{
              disableUnderline: true,
              startAdornment: (
                <InputAdornment>
                  <SearchIcon classes={{ root: classes.searchIcon }} />
                </InputAdornment>
              ),
              endAdornment: (
                <InputAdornment>
                  <CloseIcon classes={{ root: classes.searchClear }}/>
                </InputAdornment>
              ),
              classes: {
                root: classes.search,
                input: classes.searchInput
              }
            }}/>
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
