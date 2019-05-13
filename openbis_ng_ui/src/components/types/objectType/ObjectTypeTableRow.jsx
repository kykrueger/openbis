import _ from 'lodash'
import React from 'react'
import { DragSource } from 'react-dnd'
import { DropTarget } from 'react-dnd'
import MenuItem from '@material-ui/core/MenuItem'
import TableCell from '@material-ui/core/TableCell'
import TableRow from '@material-ui/core/TableRow'
import Select from '@material-ui/core/Select'
import TextField from '@material-ui/core/TextField'
import Checkbox from '@material-ui/core/Checkbox'
import RootRef from '@material-ui/core/RootRef'
import {withStyles} from '@material-ui/core/styles'
import logger from '../../../common/logger.js'

const styles = (theme) => ({
  row: {
    backgroundColor: theme.palette.background.paper
  }
})

const source = {
  beginDrag(props) {
    return { sourceIndex: props.index }
  },
  endDrag(props, monitor) {
    const { sourceIndex } = monitor.getItem()
    const { targetIndex } = monitor.getDropResult()
    props.onReorder(sourceIndex, targetIndex)
  }
}

function sourceCollect(connect, monitor) {
  return {
    connectDragSource: connect.dragSource(),
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
    this.sourceRef = React.createRef()
  }

  componentDidMount(){
    this.props.connectDragSource(this.sourceRef.current)
    this.props.connectDropTarget(this.sourceRef.current)
  }

  handleSelect(property){
    return () => {
      this.props.onSelect(property)
    }
  }

  handleChange(path){
    return event => {
      let value = _.has(event.target, 'checked') ? event.target.checked : event.target.value
      this.props.onChange(path, value)
    }
  }

  render(){
    logger.log(logger.DEBUG, 'ObjectTypeTableRow.render')

    const {classes, property, index} = this.props

    return (
      <RootRef rootRef={this.sourceRef}>
        <TableRow key={property.code} classes={{ root: classes.row }} selected={property.selected} onClick={this.handleSelect(property.code)}>
          <TableCell>
            <TextField value={property.code} onChange={this.handleChange('properties[' + index + '].code')} />
          </TableCell>
          <TableCell>
            <TextField value={property.label} onChange={this.handleChange('properties[' + index + '].label')} />
          </TableCell>
          <TableCell>
            <TextField value={property.description} onChange={this.handleChange('properties[' + index + '].description')} />
          </TableCell>
          <TableCell>
            <Select
              value={property.dataType ? property.dataType : 'VARCHAR'}
              onChange={this.handleChange('properties[' + index + '].dataType')}
            >
              <MenuItem value={'VARCHAR'}>VARCHAR</MenuItem>
              <MenuItem value={'INTEGER'}>INTEGER</MenuItem>
              <MenuItem value={'REAL'}>REAL</MenuItem>
              <MenuItem value={'BOOLEAN'}>BOOLEAN</MenuItem>
            </Select>
          </TableCell>
          <TableCell>
            <Checkbox checked={property.mandatory} value='mandatory' onChange={this.handleChange('properties[' + index + '].mandatory')} />
          </TableCell>
        </TableRow>
      </RootRef>
    )
  }
}

export default _.flow(
  DragSource('property', source, sourceCollect),
  DropTarget('property', target, targetCollect),
  withStyles(styles)
)(ObjectTypeTableRow)
