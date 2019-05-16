import _ from 'lodash'
import React from 'react'
import {withStyles} from '@material-ui/core/styles'
import ObjectTypeForm from './ObjectTypeForm.jsx'
import ObjectTypeFooter from './ObjectTypeFooter.jsx'
import logger from '../../../common/logger.js'
import {facade, dto} from '../../../services/openbis.js'

const styles = (theme) => ({
  container: {
    height: '100%',
    display: 'flex',
    flexDirection: 'column'
  },
  form: {
    flex: '1 1 0',
    overflow: 'auto',
    padding: theme.spacing.unit * 2
  },
  footer: {
    flex: '0 0',
    padding: theme.spacing.unit * 2
  }
})

class ObjectType extends React.Component {

  constructor(props){
    super(props)
    this.state = {
      loaded: false,
    }
    this.handleChange = this.handleChange.bind(this)
    this.handleAdd = this.handleAdd.bind(this)
    this.handleSelect = this.handleSelect.bind(this)
    this.handleReorder = this.handleReorder.bind(this)
    this.handleRemove = this.handleRemove.bind(this)
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
            object: {
              code: objectType.code,
              properties: objectType.propertyAssignments.map(assignment => ({
                permId: assignment.permId,
                code: assignment.propertyType.code,
                label: assignment.propertyType.label,
                description: assignment.propertyType.description,
                dataType: assignment.propertyType.dataType,
                ordinal: assignment.ordinal,
                mandatory: assignment.mandatory,
                selected: false,
                errors: {}
              }))
            }
          }
        })
      }
    })
  }

  handleChange(index, key, value){
    this.setState((prevState) => {
      let newProperties = prevState.object.properties.map((property, i) => {
        if(i === index){
          return {
            ...property,
            [key]: value
          }
        }else{
          return property
        }
      })
      return {
        ...prevState,
        object: {
          ...prevState.object,
          properties: newProperties
        }
      }
    }, () => {
      if(this.state.validated){
        this.validate()
      }
    })
  }

  handleRemove(){
    this.setState((prevState) => {
      let newProperties = prevState.object.properties.reduce((array, property) => {
        if(!property.selected){
          array.push(property)
        }
        return array
      }, [])

      return {
        ...prevState,
        object: {
          ...prevState.object,
          properties: newProperties
        }
      }
    })
  }

  handleAdd(){
    this.setState((prevState) => {
      let newProperties = prevState.object.properties.map(property => ({
        ...property,
        selected: false
      }))
      newProperties.push({
        code: 'PROPERTY_' + prevState.object.properties.length,
        label: '',
        description: '',
        dataType: '',
        mandatory: false,
        selected: true,
        errors: {}
      })

      return {
        ...prevState,
        object: {
          ...prevState.object,
          properties: newProperties
        }
      }
    })
  }

  handleSelect(propertyCode){
    this.setState((prevState) => ({
      ...prevState,
      object: {
        ...prevState.object,
        properties: prevState.object.properties.map(property => {
          return {
            ...property,
            selected: property.code === propertyCode ? !property.selected : false
          }
        })
      }
    }))
  }

  handleReorder(oldPropertyIndex, newPropertyIndex){
    let oldProperties = this.state.object.properties
    let newProperties = oldProperties.map(property => {
      if(property.selected){
        return {
          ...property,
          selected: false
        }
      }else{
        return property
      }
    })

    let [ property ] = newProperties.splice(oldPropertyIndex, 1)
    newProperties.splice(newPropertyIndex, 0, {
      ...property,
      selected: true
    })

    this.setState((prevState) => ({
      ...prevState,
      object: {
        ...prevState.object,
        properties: newProperties
      }
    }))
  }

  validate(){
    let valid = true

    let newProperties = this.state.object.properties.map(property => {
      let errors = {}

      if(!_.trim(property.code)){
        errors['code'] = 'Cannot be empty'
      }
      if(!_.trim(property.label)){
        errors['label'] = 'Cannot be empty'
      }
      if(!_.trim(property.description)){
        errors['description'] = 'Cannot be empty'
      }
      if(!_.trim(property.dataType)){
        errors['dataType'] = 'Cannot be empty'
      }

      if(_.size(errors) > 0){
        valid = false
      }

      return {
        ...property,
        errors: errors
      }
    })

    this.setState((prevState) => ({
      ...prevState,
      validated: true,
      object: {
        ...prevState.object,
        properties: newProperties
      }
    }))

    return valid
  }

  handleSave(){
    if(this.validate()){
      let update = new dto.SampleTypeUpdate()
      update.setTypeId(new dto.EntityTypePermId(this.props.objectId))
      facade.updateSampleTypes([update])
    }
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectType.render')

    if(!this.state.loaded){
      return <div></div>
    }

    let classes = this.props.classes

    return (
      <div className={classes.container}>
        <div className={classes.form}>
          <ObjectTypeForm
            object={this.state.object}
            onSelect={this.handleSelect}
            onReorder={this.handleReorder}
            onChange={this.handleChange}
          />
        </div>
        <div className={classes.footer}>
          <ObjectTypeFooter
            onAdd={this.handleAdd}
            onRemove={this.handleRemove}
            onSave={this.handleSave}
          />
        </div>
      </div>
    )
  }

}

export default withStyles(styles)(ObjectType)
