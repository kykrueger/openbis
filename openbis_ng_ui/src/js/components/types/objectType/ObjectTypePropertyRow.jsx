import _ from 'lodash'
import React from 'react'
import {DragSource, DropTarget} from 'react-dnd'
import TableRow from '@material-ui/core/TableRow'
import DragHandleIcon from '@material-ui/icons/DragHandle'
import RootRef from '@material-ui/core/RootRef'
import ObjectTypePropertyCell from './ObjectTypePropertyCell.jsx'
import ObjectTypePropertyPreview from './ObjectTypePropertyPreview.jsx'
import ObjectTypePropertyType from './ObjectTypePropertyType.jsx'
import ObjectTypePropertyMandatory from './ObjectTypePropertyMandatory.jsx'
import {withStyles} from '@material-ui/core/styles'
import logger from '../../../common/logger.js'

const styles = theme => ({
  row: {
    backgroundColor: theme.palette.background.paper,
    '&:hover': {
      backgroundColor: theme.palette.action.hover,
      cursor: 'pointer'
    },
    '&$selected': {
      backgroundColor: theme.palette.action.selected
    }
  },
  selected: {},
  drag: {
    cursor: 'grab'
  }
})

const source = {
  beginDrag(props) {
    return { sourceIndex: props.index }
  },
  endDrag(props, monitor) {
    if (monitor.getItem() && monitor.getDropResult()) {
      const { sourceIndex } = monitor.getItem()
      const { targetIndex } = monitor.getDropResult()
      props.onReorder(sourceIndex, targetIndex)
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
    return { targetIndex: props.index }
  }
}

function targetCollect(connect, monitor) {
  return {
    connectDropTarget: connect.dropTarget(),
    isDragging: monitor.getItem() !== null
  }
}

class ObjectTypePropertyRow extends React.Component {
  constructor(props) {
    super(props)
    this.handleRef = React.createRef()
    this.rowRef = React.createRef()
    this.handleSelect = this.handleSelect.bind(this)
  }

  componentDidMount() {
    this.props.connectDragSource(this.handleRef.current)
    this.props.connectDragPreview(this.rowRef.current)
    this.props.connectDropTarget(this.rowRef.current)
  }

  handleSelect() {
    this.props.onSelect(this.props.property.id)
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectTypePropertyRow.render')

    const { classes, property } = this.props

    return (
      <RootRef rootRef={this.rowRef}>
        <TableRow
          classes={{ root: classes.row, selected: classes.selected }}
          selected={property.selected}
          onClick={this.handleSelect}
        >
          <ObjectTypePropertyCell>
            <RootRef rootRef={this.handleRef}>
              <div className={classes.drag}>
                <DragHandleIcon />
              </div>
            </RootRef>
          </ObjectTypePropertyCell>
          <ObjectTypePropertyCell>
            {this.renderPreview()}
          </ObjectTypePropertyCell>
          <ObjectTypePropertyCell>
            {this.renderPropertyType()}
          </ObjectTypePropertyCell>
          <ObjectTypePropertyCell>
            {this.renderMandatory()}
          </ObjectTypePropertyCell>
        </TableRow>
      </RootRef>
    )
  }

  renderPreview() {
    const { property } = this.props

    if (property.propertyType) {
      return <ObjectTypePropertyPreview property={property} />
    } else {
      return <div></div>
    }
  }

  renderPropertyType() {
    const { property, propertyTypes, onChange } = this.props

    return (
      <ObjectTypePropertyType
        property={property}
        propertyTypes={propertyTypes}
        onChange={onChange}
      />
    )
  }

  renderMandatory() {
    const { property, onChange } = this.props
    return (
      <ObjectTypePropertyMandatory property={property} onChange={onChange} />
    )
  }
}

export default _.flow(
  DragSource('property', source, sourceCollect),
  DropTarget('property', target, targetCollect),
  withStyles(styles)
)(ObjectTypePropertyRow)
