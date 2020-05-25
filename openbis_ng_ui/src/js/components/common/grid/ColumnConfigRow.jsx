import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import { Draggable } from 'react-beautiful-dnd'
import FormControlLabel from '@material-ui/core/FormControlLabel'
import Checkbox from '@material-ui/core/Checkbox'
import DragHandleIcon from '@material-ui/icons/DragHandle'
import logger from '@src/js/common/logger.js'

const styles = () => ({
  row: {
    display: 'flex',
    alignItems: 'center'
  },
  label: {
    marginLeft: 0
  },
  drag: {
    display: 'flex',
    cursor: 'grab'
  }
})

class ColumnConfigRow extends React.Component {
  constructor(props) {
    super(props)
    this.handleVisibleChange = this.handleVisibleChange.bind(this)
  }

  handleVisibleChange() {
    this.props.onVisibleChange(this.props.column.field)
  }

  render() {
    logger.log(logger.DEBUG, 'ColumnConfigRow.render')

    const { classes, column, index } = this.props

    return (
      <Draggable draggableId={column.field} index={index}>
        {provided => (
          <div
            ref={provided.innerRef}
            {...provided.draggableProps}
            className={classes.row}
          >
            <div {...provided.dragHandleProps} className={classes.drag}>
              <DragHandleIcon />
            </div>
            <FormControlLabel
              classes={{ root: classes.label }}
              control={
                <Checkbox
                  checked={column.visible}
                  onChange={this.handleVisibleChange}
                />
              }
              label={column.label || column.field}
            />
          </div>
        )}
      </Draggable>
    )
  }
}

export default withStyles(styles)(ColumnConfigRow)
