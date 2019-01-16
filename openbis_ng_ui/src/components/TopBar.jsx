import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import { connect } from 'react-redux'
import flow from 'lodash/flow'

import Grid from '@material-ui/core/Grid'
import Button from '@material-ui/core/Button'
import Hidden from '@material-ui/core/Hidden'
import LogoutIcon from '@material-ui/icons/PowerSettingsNew'
import AppBar from '@material-ui/core/AppBar'
import Toolbar from '@material-ui/core/Toolbar'
import InputAdornment from '@material-ui/core/InputAdornment'
import TextField from '@material-ui/core/TextField'
import SearchIcon from '@material-ui/icons/Search'

import actions from '../reducer/actions.js'


const styles = theme => ({
  searchField: {
    backgroundColor: 'white',
    width: '100%'
  },
  leftIcon: {
    marginRight: theme.spacing.unit,
  },
  grid: {
    minWidth: 0
  }
})


function mapDispatchToProps(dispatch) {
  return {
    logout: () => dispatch(actions.logout()),
  }
}


class TopBar extends React.Component {

  render() {
    const classes = this.props.classes

    return (
      <AppBar position='static'>
        <Toolbar>
          <Grid container alignItems='center' spacing={8}>

            <Grid item xs className={ classes.grid }>
              <TextField
                className = { classes.searchField }
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon />
                    </InputAdornment>
                  ),
                }}/>
            </Grid> 

            <Grid item>
              <Button
                variant="contained" 
                color="primary">
                <SearchIcon className={ classes.leftIcon } />
                <Hidden mdUp>
                  Adv.
                </Hidden>
                <Hidden smDown>
                  Adv. search
                </Hidden>
              </Button>
            </Grid>

            <Grid item>
              <Button
                variant="contained" 
                color="primary"
                onClick={ this.props.logout }>
                <LogoutIcon />
              </Button>
            </Grid>
  
          </Grid>
        </Toolbar>
      </AppBar>
    )
  }
}

export default flow(
  connect(null, mapDispatchToProps),
  withStyles(styles)
)(TopBar)
