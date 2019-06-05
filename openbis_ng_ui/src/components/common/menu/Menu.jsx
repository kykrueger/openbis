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
    transition: theme.transitions.create('width'),
    width: '200px',
    '&:focus-within': {
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
    currentPage: selectors.getCurrentPage(state),
    search: selectors.getSearch(state)
  }
}

function mapDispatchToProps(dispatch){
  return {
    currentPageChange: (event, value) => dispatch(actions.currentPageChange(value)),
    searchChange: (value) => dispatch(actions.searchChange(value)),
    logout: () => dispatch(actions.logout())
  }
}

class Menu extends React.Component {

  constructor(props){
    super(props)
    this.searchRef = React.createRef()
    this.handleSearchChange = this.handleSearchChange.bind(this)
    this.handleSearchClear = this.handleSearchClear.bind(this)
  }

  handleSearchChange(event){
    this.props.searchChange(event.target.value)
  }

  handleSearchClear(event){
    event.preventDefault()
    this.props.searchChange('')
    this.searchRef.current.focus()
  }

  render() {
    logger.log(logger.DEBUG, 'Menu.render')

    const {classes, search} = this.props

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
            value={search}
            onChange={this.handleSearchChange}
            InputProps={{
              inputRef: this.searchRef,
              disableUnderline: true,
              startAdornment: this.renderSearchIcon(),
              endAdornment: this.renderSearchClearIcon(),
              classes: {
                root: classes.search,
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

  renderSearchIcon(){
    const {classes} = this.props
    return (
      <InputAdornment>
        <SearchIcon classes={{ root: classes.searchIcon }} />
      </InputAdornment>
    )
  }

  renderSearchClearIcon(){
    const {classes, search} = this.props
    if(search){
      return (
        <InputAdornment>
          <CloseIcon
            classes={{ root: classes.searchClear }}
            onMouseDown={this.handleSearchClear}
          />
        </InputAdornment>
      )
    }else{
      return (<React.Fragment></React.Fragment>)
    }
  }

}

export default connect(mapStateToProps, mapDispatchToProps)(withStyles(styles)(Menu))
