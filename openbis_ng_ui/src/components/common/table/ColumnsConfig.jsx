import _ from 'lodash'
import React from 'react'
import Popover from '@material-ui/core/Popover'
import Button from '@material-ui/core/Button'
import FormControlLabel from '@material-ui/core/FormControlLabel'
import Checkbox from '@material-ui/core/Checkbox'
import logger from '../../../common/logger.js'

class ColumnsConfig extends React.Component {

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
    logger.log(logger.DEBUG, 'ColumnsConfig.render')

    const { allColumns, visibleColumns } = this.props
    const { el } = this.state

    return (
      <React.Fragment>
        <Button
          variant="contained"
          onClick={this.handleOpen}
        >
        Columns
        </Button>
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
          {allColumns.map(column => (
            <FormControlLabel key={column}
              control={
                <Checkbox
                  value={column}
                  checked={visibleColumns.includes(column)}
                  onChange={this.handleChange}
                />
              }
              label={column}
            />
          ))}
        </Popover>
      </React.Fragment>
    )
  }

}

export default ColumnsConfig
