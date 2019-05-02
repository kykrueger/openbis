import _ from 'lodash'
import React from 'react'
import TextField from '@material-ui/core/TextField'
import Checkbox from '@material-ui/core/Checkbox'
import Button from '@material-ui/core/Button'
import Table from '@material-ui/core/Table'
import TableBody from '@material-ui/core/TableBody'
import TableCell from '@material-ui/core/TableCell'
import TableHead from '@material-ui/core/TableHead'
import TableRow from '@material-ui/core/TableRow'
import FormControlLabel from '@material-ui/core/FormControlLabel'
import logger from '../../../common/logger.js'
import {facade, dto} from '../../../services/openbis.js'

class ObjectType extends React.Component {

  constructor(props){
    super(props)
    this.state = {
      loaded: false,
    }
    this.handleSave = this.handleSave.bind(this)
  }

  componentDidMount(){
    let id = new dto.EntityTypePermId(this.props.objectId)
    let fo = new dto.SampleTypeFetchOptions()
    fo.withPropertyAssignments().withPropertyType()
    fo.withPropertyAssignments().sortBy().code()

    facade.getSampleTypes([id], fo).then(map => {
      let objectType = map[this.props.objectId]
      if(objectType){
        this.setState(() => {
          return {
            loaded: true,
            data: {
              code: objectType.code,
              description: objectType.description || '',
              listable: objectType.listable,
              properties: objectType.propertyAssignments.map(assignment => ({
                permId: assignment.permId,
                code: assignment.propertyType.code,
                label: assignment.propertyType.label,
                description: assignment.propertyType.description,
                ordinal: assignment.ordinal,
                mandatory: assignment.mandatory
              }))
            }
          }
        })
      }
    })
  }

  handleChange(name){
    return event => {
      let value = _.has(event.target, 'checked') ? event.target.checked : event.target.value
      this.setState((prevState) => ({
        ...prevState,
        data: {
          ...prevState.data,
          [name]: value
        }
      }))
    }
  }

  handleSave(){
    let {description, listable} = this.state.data
    let update = new dto.SampleTypeUpdate()
    update.setTypeId(new dto.EntityTypePermId(this.props.objectId))
    update.setDescription(description)
    update.setListable(listable)
    facade.updateSampleTypes([update])
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectType.render')

    if(!this.state.loaded){
      return <div></div>
    }

    let { code, description, listable, properties } = this.state.data

    return (
      <div>
        <h2>{code}</h2>
        <form>
          <FormControlLabel
            label="Description"
            labelPlacement="top"
            control={
              <TextField value={description} onChange={this.handleChange('description')} />
            }
          />
          <FormControlLabel
            label="Listable"
            labelPlacement="top"
            control={
              <Checkbox checked={listable} value='listable' onChange={this.handleChange('listable')} />
            }
          />
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Code</TableCell>
                <TableCell>Label</TableCell>
                <TableCell>Description</TableCell>
                <TableCell>Mandatory</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {properties.map(property => (
                <TableRow key={property.permId}>
                  <TableCell>{property.code}</TableCell>
                  <TableCell>{property.label}</TableCell>
                  <TableCell>{property.description}</TableCell>
                  <TableCell>{property.mandatory ? 'true' : 'false'}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          <div>
            <Button variant='contained' color='primary' onClick={this.handleSave}>Save</Button>
          </div>
        </form>
      </div>
    )
  }

}

export default ObjectType
