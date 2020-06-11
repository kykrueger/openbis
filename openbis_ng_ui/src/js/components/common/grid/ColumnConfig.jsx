import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import { DragDropContext, Droppable } from 'react-beautiful-dnd'
import IconButton from '@material-ui/core/IconButton'
import SettingsIcon from '@material-ui/icons/Settings'
import Popover from '@material-ui/core/Popover'
import logger from '@src/js/common/logger.js'

import ColumnConfigRow from './ColumnConfigRow.jsx'

const styles = theme => ({
  container: {
    display: 'flex',
    alignItems: 'center'
  },
  columns: {
    listStyle: 'none',
    padding: `${theme.spacing(1)}px ${theme.spacing(2)}px`,
    paddingBottom: 0,
    margin: 0
  }
})

class ColumnConfig extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      el: null
    }
    this.handleOpen = this.handleOpen.bind(this)
    this.handleClose = this.handleClose.bind(this)
    this.handleDragEnd = this.handleDragEnd.bind(this)
  }

  handleOpen(event) {
    this.setState({
      el: event.currentTarget
    })
  }

  handleClose() {
    this.setState({
      el: null
    })
  }

  handleDragEnd(result) {
    if (!result.destination) {
      return
    }
    this.props.onOrderChange(result.source.index, result.destination.index)
  }

  render() {
    logger.log(logger.DEBUG, 'ColumnConfig.render')

    const { classes, columns, onVisibleChange } = this.props
    const { el } = this.state

    return (
      <div className={classes.container}>
        <IconButton onClick={this.handleOpen}>
          <SettingsIcon fontSize='small' />
        </IconButton>
        <Popover
          open={Boolean(el)}
          anchorEl={el}
          onClose={this.handleClose}
          anchorOrigin={{
            vertical: 'top',
            horizontal: 'center'
          }}
          transformOrigin={{
            vertical: 'bottom',
            horizontal: 'center'
          }}
        >
          <DragDropContext onDragEnd={this.handleDragEnd}>
            <Droppable droppableId='root'>
              {provided => (
                <ol
                  ref={provided.innerRef}
                  {...provided.droppableProps}
                  className={classes.columns}
                >
                  {columns.map((column, index) => (
                    <ColumnConfigRow
                      key={column.field}
                      column={column}
                      index={index}
                      onVisibleChange={onVisibleChange}
                    />
                  ))}
                  {provided.placeholder}
                </ol>
              )}
            </Droppable>
          </DragDropContext>
        </Popover>
      </div>
    )
  }
}

export default _.flow(withStyles(styles))(ColumnConfig)
