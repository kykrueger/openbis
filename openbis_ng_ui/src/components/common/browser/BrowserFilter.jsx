import React from 'react'
import {withStyles} from '@material-ui/core/styles'
import InputAdornment from '@material-ui/core/InputAdornment'
import TextField from '@material-ui/core/TextField'
import IconButton from '@material-ui/core/IconButton'
import FilterIcon from '@material-ui/icons/FilterList'
import CloseIcon from '@material-ui/icons/Close'
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

  constructor(props){
    super(props)
    this.handleFilterChange = this.handleFilterChange.bind(this)
    this.handleFilterClear = this.handleFilterClear.bind(this)
  }

  handleFilterChange(event){
    this.props.filterChange(event.target.value)
  }

  handleFilterClear(){
    this.props.filterChange('')
  }

  render() {
    logger.log(logger.DEBUG, 'BrowserFilter.render')

    const classes = this.props.classes

    return (
      <TextField
        className={classes.field}
        placeholder="Filter"
        value={this.props.filter}
        onChange={this.handleFilterChange}
        InputProps={{
          startAdornment: (
            <InputAdornment position="start" classes={{
              root: classes.adornment
            }}>
              <FilterIcon />
            </InputAdornment>
          ),
          endAdornment: (
            <InputAdornment position="end" classes={{
              root: classes.adornment
            }}>
              <IconButton onClick={this.handleFilterClear}>
                <CloseIcon />
              </IconButton>
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
