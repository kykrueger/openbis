import React from 'react'
import {withStyles} from '@material-ui/core/styles'
import InputAdornment from '@material-ui/core/InputAdornment'
import TextField from '@material-ui/core/TextField'
import FilterIcon from '@material-ui/icons/FilterList'
import logger from '../../../common/logger.js'

const styles = () => ({
  browserFilter: {
    width: '100%'
  }
})

class BrowserFilter extends React.Component {

  render() {
    logger.log(logger.DEBUG, 'BrowserFilter.render')

    const classes = this.props.classes

    return (
      <TextField
        className={classes.browserFilter}
        placeholder="Filter"
        value={this.props.filter}
        onChange={this.props.filterChange}
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

export default withStyles(styles)(BrowserFilter)
