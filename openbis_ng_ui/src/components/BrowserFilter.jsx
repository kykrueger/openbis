import React from 'react'
import {connect} from 'react-redux'
import {withStyles} from '@material-ui/core/styles'
import InputAdornment from '@material-ui/core/InputAdornment'
import TextField from '@material-ui/core/TextField'
import FilterIcon from '@material-ui/icons/FilterList'
import actions from '../reducer/actions'
import {getTabState} from '../reducer/selectors'

/*eslint-disable-next-line no-unused-vars*/
const styles = theme => ({
  browserFilter: {
    width: '100%'
  }
})

function mapDispatchToProps(dispatch) {
  return {
    setFilter: filter => {
      dispatch(actions.setFilter(filter))
    }
  }
}

function mapStateToProps(state) {
  let tabState = getTabState(state)
  return {
    filter: tabState.browser.filter
  }
}

class BrowserFilter extends React.Component {

  render() {
    const classes = this.props.classes

    return (
      <TextField
        className={classes.browserFilter}
        placeholder="Filter"
        value={this.props.filter}
        onChange={e => this.props.setFilter(e.target.value)}
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <FilterIcon/>
            </InputAdornment>
          ),
        }}/>
    )
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(withStyles(styles)(BrowserFilter))
