import _ from 'lodash'
import React from 'react'
import { DragSource } from 'react-dnd'
import { DropTarget } from 'react-dnd'
import MenuItem from '@material-ui/core/MenuItem'
import TableCell from '@material-ui/core/TableCell'
import TableRow from '@material-ui/core/TableRow'
import FormControl from '@material-ui/core/FormControl'
import FormHelperText from '@material-ui/core/FormHelperText'
import Select from '@material-ui/core/Select'
import TextField from '@material-ui/core/TextField'
import Checkbox from '@material-ui/core/Checkbox'
import DragHandleIcon from '@material-ui/icons/DragHandle'
import RootRef from '@material-ui/core/RootRef'
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
  }

  componentDidMount(){
    this.props.connectDragSource(this.handleRef.current)
    this.props.connectDragPreview(this.rowRef.current)
    this.props.connectDropTarget(this.rowRef.current)
  }

  handleSelect(property){
    return () => {
      this.props.onSelect(property)
    }
  }

  handleChange(index, key){
    return event => {
      let value = _.has(event.target, 'checked') ? event.target.checked : event.target.value
      this.props.onChange(index, key, value)
    }
  }

  render(){
    logger.log(logger.DEBUG, 'ObjectTypeTableRow.render')

    const {classes, property, index} = this.props

    return (
      <RootRef rootRef={this.rowRef}>
        <TableRow key={property.code} classes={{ root: classes.row, selected: classes.selected }} selected={property.selected} onClick={this.handleSelect(property.code)}>
          <RootRef rootRef={this.handleRef}>
            <TableCell classes={{ root: classes.drag }}>
              <DragHandleIcon />
            </TableCell>
          </RootRef>
          <TableCell>
            <TextField
              value={property.code}
              error={property.errors['code'] ? true : false}
              helperText={property.errors['code']}
              onChange={this.handleChange(index, 'code')}
            />
          </TableCell>
          <TableCell>
            <TextField
              value={property.label}
              error={property.errors['label'] ? true : false}
              helperText={property.errors['label']}
              onChange={this.handleChange(index, 'label')}
            />
          </TableCell>
          <TableCell>
            <TextField
              value={property.description}
              error={property.errors['description'] ? true : false}
              helperText={property.errors['description']}
              onChange={this.handleChange(index, 'description')}
            />
          </TableCell>
          <TableCell>
            <FormControl error={property.errors['dataType'] ? true : false}>
              <Select
                value={property.dataType ? property.dataType : ''}
                onChange={this.handleChange(index, 'dataType')}
              >
                <MenuItem value=""></MenuItem>
                <MenuItem value={'VARCHAR'}>VARCHAR</MenuItem>
                <MenuItem value={'INTEGER'}>INTEGER</MenuItem>
                <MenuItem value={'REAL'}>REAL</MenuItem>
                <MenuItem value={'BOOLEAN'}>BOOLEAN</MenuItem>
              </Select>
              { property.errors['dataType'] &&
                <FormHelperText>{property.errors['dataType']}</FormHelperText>
              }
            </FormControl>
          </TableCell>
          <TableCell>
            <Checkbox checked={property.mandatory} value='mandatory' onChange={this.handleChange(index, 'mandatory')} />
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
