import _ from 'lodash'
import React from 'react'
import { DragSource } from 'react-dnd'
import { DropTarget } from 'react-dnd'
import FormControlLabel from '@material-ui/core/FormControlLabel'
import Checkbox from '@material-ui/core/Checkbox'
import DragHandleIcon from '@material-ui/icons/DragHandle'
import RootRef from '@material-ui/core/RootRef'
import { withStyles } from '@material-ui/core/styles'
import logger from '../../../common/logger.js'

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

const source = {
  beginDrag(props) {
    return { column: props.column.field }
  },
  endDrag(props, monitor) {
    if (monitor.getItem() && monitor.getDropResult()) {
      const { column: sourceColumn } = monitor.getItem()
      const { column: targetColumn } = monitor.getDropResult()
      props.onOrderChange(sourceColumn, targetColumn)
    }
  }
}

function sourceCollect(connect, monitor) {
  return {
    connectDragSource: connect.dragSource(),
    connectDragPreview: connect.dragPreview(),
    isDragging: monitor.isDragging()
  }
}

const target = {
  drop(props) {
    return { column: props.column.field }
  }
}

function targetCollect(connect, monitor) {
  return {
    connectDropTarget: connect.dropTarget(),
    isDragging: monitor.getItem() !== null
  }
}

class ColumnConfigRow extends React.Component {
  constructor(props) {
    super(props)
    this.handleRef = React.createRef()
    this.rowRef = React.createRef()
    this.handleVisibleChange = this.handleVisibleChange.bind(this)
  }

  componentDidMount() {
    this.props.connectDragSource(this.handleRef.current)
    this.props.connectDragPreview(this.rowRef.current)
    this.props.connectDropTarget(this.rowRef.current)
  }

  handleVisibleChange() {
    this.props.onVisibleChange(this.props.column.field)
  }

  render() {
    logger.log(logger.DEBUG, 'ColumnConfigRow.render')

    const { classes, column } = this.props

    return (
      <RootRef rootRef={this.rowRef}>
        <div className={classes.row}>
          <RootRef rootRef={this.handleRef}>
            <div className={classes.drag}>
              <DragHandleIcon />
            </div>
          </RootRef>
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
      </RootRef>
    )
  }
}

export default _.flow(
  DragSource('column', source, sourceCollect),
  DropTarget('column', target, targetCollect),
  withStyles(styles)
)(ColumnConfigRow)
