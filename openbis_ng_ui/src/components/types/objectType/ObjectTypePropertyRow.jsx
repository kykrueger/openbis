import _ from 'lodash'
import React from 'react'
import { DragSource } from 'react-dnd'
import { DropTarget } from 'react-dnd'
import MenuItem from '@material-ui/core/MenuItem'
import TableCell from '@material-ui/core/TableCell'
import TableRow from '@material-ui/core/TableRow'
import TextField from '@material-ui/core/TextField'
import Checkbox from '@material-ui/core/Checkbox'
import DragHandleIcon from '@material-ui/icons/DragHandle'
import RootRef from '@material-ui/core/RootRef'
import ObjectTypePropertyPreview from './ObjectTypePropertyPreview.jsx'
import {withStyles} from '@material-ui/core/styles'
import logger from '../../../common/logger.js'

const styles = (theme) => ({
  row: {
    backgroundColor: theme.palette.background.paper,
    '&:hover': {
      backgroundColor: 'rgba(0, 0, 0, 0.08)',
      cursor: 'pointer'
    },
    '&$selected': {
      backgroundColor: 'rgba(0, 0, 0, 0.14)'
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
    if(monitor.getItem() && monitor.getDropResult()){
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

class ObjectTypeTableRow extends React.Component {

  constructor(props){
    super(props)
    this.handleRef = React.createRef()
    this.rowRef = React.createRef()
    this.handleSelect = this.handleSelect.bind(this)
    this.handleChangePropertyType = this.handleChangePropertyType.bind(this)
    this.handleChangeMandatory = this.handleChangeMandatory.bind(this)
  }

  componentDidMount(){
    this.props.connectDragSource(this.handleRef.current)
    this.props.connectDragPreview(this.rowRef.current)
    this.props.connectDropTarget(this.rowRef.current)
  }

  handleSelect(){
    this.props.onSelect(this.props.property.ordinal)
  }

  handleChangePropertyType(event){
    event.stopPropagation()
    let propertyType = _.find(this.props.propertyTypes, propertyType => {
      return propertyType.code === event.target.value
    })
    this.props.onChange(this.props.property.ordinal, 'propertyType', propertyType)
  }

  handleChangeMandatory(event){
    event.stopPropagation()
    this.props.onChange(this.props.property.ordinal, 'mandatory', event.target.checked)
  }

  render(){
    logger.log(logger.DEBUG, 'ObjectTypePropertyRow.render')

    const {classes, property} = this.props

    return (
      <RootRef rootRef={this.rowRef}>
        <TableRow
          classes={{ root: classes.row, selected: classes.selected }}
          selected={property.selected}
          onClick={this.handleSelect}
        >
          <RootRef rootRef={this.handleRef}>
            <TableCell classes={{ root: classes.drag }}>
              <DragHandleIcon />
            </TableCell>
          </RootRef>
          <TableCell>
            {this.renderPreview()}
          </TableCell>
          <TableCell>
            {this.renderPropertyType()}
          </TableCell>
          <TableCell>
            {this.renderMandatory()}
          </TableCell>
        </TableRow>
      </RootRef>
    )
  }

  renderPreview(){
    const {property, propertyTypes} = this.props

    const propertyType = _.find(propertyTypes, propertyType => {
      return propertyType.code === property.propertyType.code
    })

    return (
      <ObjectTypePropertyPreview property={property} propertyType={propertyType} />
    )
  }

  renderPropertyType(){
    const {property, propertyTypes} = this.props

    return (
      <TextField
        select
        value={property.propertyType ? property.propertyType.code : ''}
        onClick={event => {event.stopPropagation()}}
        onChange={this.handleChangePropertyType}
        fullWidth={true}
        error={property.errors['propertyType'] ? true : false}
        helperText={property.errors['propertyType']}
      >
        <MenuItem value=""></MenuItem>
        {propertyTypes && propertyTypes.map(propertyType => (
          <MenuItem key={propertyType.code} value={propertyType.code}>{propertyType.code}</MenuItem>
        ))}
      </TextField>
    )
  }

  renderMandatory(){
    const {property} = this.props

    return (
      <Checkbox
        checked={property.mandatory}
        value='mandatory'
        onClick={event => {event.stopPropagation()}}
        onChange={this.handleChangeMandatory}
      />
    )
  }
}

export default _.flow(
  DragSource('property', source, sourceCollect),
  DropTarget('property', target, targetCollect),
  withStyles(styles)
)(ObjectTypeTableRow)
