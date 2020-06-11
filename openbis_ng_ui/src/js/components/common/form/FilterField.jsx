import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import InputAdornment from '@material-ui/core/InputAdornment'
import TextField from '@material-ui/core/TextField'
import IconButton from '@material-ui/core/IconButton'
import FilterIcon from '@material-ui/icons/FilterList'
import CloseIcon from '@material-ui/icons/Close'
import logger from '@src/js/common/logger.js'

const styles = () => ({
  field: {
    width: '100%'
  },
  input: {
    height: '24px',
    fontSize: '0.875rem'
  },
  adornment: {
    margin: '8px',
    marginLeft: '10px'
  },
  adornmentButton: {
    padding: '4px'
  },
  adornmentSpacer: {
    width: '64px'
  }
})

class FilterField extends React.Component {
  constructor(props) {
    super(props)
    this.filterRef = React.createRef()
    this.handleFilterChange = this.handleFilterChange.bind(this)
    this.handleFilterClear = this.handleFilterClear.bind(this)
  }

  handleFilterChange(event) {
    this.props.filterChange(event.target.value)
  }

  handleFilterClear(event) {
    event.preventDefault()
    this.props.filterChange('')
    this.filterRef.current.focus()
  }

  render() {
    logger.log(logger.DEBUG, 'BrowserFilter.render')

    const classes = this.props.classes

    return (
      <TextField
        className={classes.field}
        placeholder='Filter'
        value={this.props.filter}
        onChange={this.handleFilterChange}
        InputProps={{
          inputRef: this.filterRef,
          startAdornment: this.renderFilterIcon(),
          endAdornment: this.renderFilterClearIcon(),
          classes: {
            input: classes.input
          }
        }}
        margin='none'
      />
    )
  }

  renderFilterIcon() {
    const classes = this.props.classes
    return (
      <InputAdornment
        position='start'
        classes={{
          root: classes.adornment
        }}
      >
        <FilterIcon />
      </InputAdornment>
    )
  }

  renderFilterClearIcon() {
    const classes = this.props.classes

    if (this.props.filter) {
      return (
        <InputAdornment
          position='end'
          classes={{
            root: classes.adornment
          }}
        >
          <IconButton
            onMouseDown={this.handleFilterClear}
            classes={{ root: classes.adornmentButton }}
          >
            <CloseIcon />
          </IconButton>
        </InputAdornment>
      )
    } else {
      return (
        <React.Fragment>
          <div className={classes.adornmentSpacer}></div>
        </React.Fragment>
      )
    }
  }
}

export default withStyles(styles)(FilterField)
