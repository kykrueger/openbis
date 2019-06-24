import _ from 'lodash'
import React from 'react'
import {withStyles} from '@material-ui/core/styles'
import IconButton from '@material-ui/core/IconButton'
import SettingsIcon from '@material-ui/icons/Settings'
import Popover from '@material-ui/core/Popover'
import FormControlLabel from '@material-ui/core/FormControlLabel'
import Checkbox from '@material-ui/core/Checkbox'
import logger from '../../../common/logger.js'

const styles = () => ({
  container: {
    display: 'flex',
    alignItems: 'center'
  },
  columns: {
    listStyle: 'none',
    padding: '10px 20px',
    margin: 0
  }
})

class ColumnConfig extends React.Component {

  constructor(props){
    super(props)
    this.state = {
      el: null
    }
    this.handleOpen = this.handleOpen.bind(this)
    this.handleClose = this.handleClose.bind(this)
    this.handleChange = this.handleChange.bind(this)
  }

  handleOpen(event){
    this.setState({
      el: event.currentTarget
    })
  }

  handleClose(){
    this.setState({
      el: null
    })
  }

  handleChange(event){
    let columns = [...this.props.visibleColumns]
    let column = event.target.value

    if(columns.includes(column)){
      _.pull(columns, column)
    }else{
      columns.push(column)
    }

    this.props.onColumnsChange(columns)
  }

  render() {
    logger.log(logger.DEBUG, 'ColumnConfig.render')

    const { classes, allColumns, visibleColumns } = this.props
    const { el } = this.state

    return (
      <div className={classes.container}>
        <IconButton onClick={this.handleOpen}>
          <SettingsIcon />
        </IconButton>
        <Popover
          open={Boolean(el)}
          anchorEl={el}
          onClose={this.handleClose}
          anchorOrigin={{
            vertical: 'top',
            horizontal: 'center',
          }}
          transformOrigin={{
            vertical: 'bottom',
            horizontal: 'center',
          }}
        >
          <ol className={classes.columns}>
            {allColumns.map(column => (
              <li key={column.field}>
                <FormControlLabel
                  control={
                    <Checkbox
                      value={column.field}
                      checked={visibleColumns.includes(column.field)}
                      onChange={this.handleChange}
                    />
                  }
                  label={column.label || column.field}
                />
              </li>
            ))}
          </ol>
        </Popover>
      </div>
    )
  }

}

export default _.flow(
  withStyles(styles)
)(ColumnConfig)
