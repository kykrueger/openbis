import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import InputAdornment from '@material-ui/core/InputAdornment'
import TextField from '@material-ui/core/TextField'
import FilterIcon from '@material-ui/icons/FilterList'

/*eslint-disable-next-line no-unused-vars*/
const styles = theme => ({
  browserFilter: {
    width: '100%'
  }
})

class BrowserFilter extends React.Component {

  render() {
    const classes = this.props.classes

    return (
      <TextField
        className = { classes.browserFilter }
        placeholder = "Filter"
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <FilterIcon />
            </InputAdornment>
          ),
        }}/>
    )
  }
}

export default withStyles(styles)(BrowserFilter)
