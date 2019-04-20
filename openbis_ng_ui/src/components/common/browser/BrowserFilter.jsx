import React from 'react'
import {withStyles} from '@material-ui/core/styles'
import InputAdornment from '@material-ui/core/InputAdornment'
import TextField from '@material-ui/core/TextField'
import FilterIcon from '@material-ui/icons/FilterList'
import logger from '../../../common/logger.js'

const styles = () => ({
  field: {
    width: '100%'
  },
  input: {
    height: '35px'
  },
  adornment: {
    margin: '8px'
  }
})

class BrowserFilter extends React.Component {

  render() {
    logger.log(logger.DEBUG, 'BrowserFilter.render')

    const classes = this.props.classes

    return (
      <TextField
        className={classes.field}
        placeholder="Filter"
        value={this.props.filter}
        onChange={this.props.filterChange}
        InputProps={{
          startAdornment: (
            <InputAdornment position="start" classes={{
              root: classes.adornment
            }}>
              <FilterIcon />
            </InputAdornment>
          ),
          classes: {
            input: classes.input
          }
        }}/>
    )
  }
}

export default withStyles(styles)(BrowserFilter)
