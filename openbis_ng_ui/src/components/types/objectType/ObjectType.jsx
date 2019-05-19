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
    Promise.all([
      this.loadObjectType(this.props.objectId),
      this.loadPropertyTypes()
    ]).then(([objectType, propertyTypes]) => {
      this.setState(() => ({
        loaded: true,
        objectType: objectType,
        propertyTypes: propertyTypes
      }))
    })
  }

  loadObjectType(objectTypeId){
    let id = new dto.EntityTypePermId(objectTypeId)
    let fo = new dto.SampleTypeFetchOptions()
    fo.withPropertyAssignments().withPropertyType()
    fo.withPropertyAssignments().sortBy().code()

    return facade.getSampleTypes([id], fo).then(map => {
      let objectType = map[objectTypeId]
      if(objectType){
        return {
          code: objectType.code,
          properties: objectType.propertyAssignments.map(assignment => ({
            permId: assignment.permId,
            propertyType: assignment.propertyType,
            ordinal: assignment.ordinal,
            mandatory: assignment.mandatory,
            selected: false,
            errors: {}
          }))
        }
      }else{
        return null
      }
    })
  }

  loadPropertyTypes(){
    let criteria = new dto.PropertyTypeSearchCriteria()
    let fo = new dto.PropertyTypeFetchOptions()
    fo.withVocabulary().withTerms()

    return facade.searchPropertyTypes(criteria, fo).then(result => {
      return result.objects
    })
  }

  handleChange(ordinal, key, value){
    this.setState((prevState) => {
      let newProperties = prevState.objectType.properties.map((property) => {
        if(property.ordinal === ordinal){
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
        objectType: {
          ...prevState.objectType,
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
      let newProperties = prevState.objectType.properties.reduce((array, property) => {
        if(!property.selected){
          array.push(property)
        }
        return array
      }, [])

      return {
        ...prevState,
        objectType: {
          ...prevState.objectType,
          properties: newProperties
        }
      }
    })
  }

  handleAdd(){
    this.setState((prevState) => {
      let newOrdinal = 0
      let newProperties = prevState.objectType.properties.map(property => {
        if(newOrdinal <= property.ordinal){
          newOrdinal = property.ordinal + 1
        }
        return {
          ...property,
          selected: false
        }
      })
      newProperties.push({
        permId: null,
        propertyType: null,
        mandatory: false,
        selected: true,
        ordinal: newOrdinal,
        errors: {}
      })

      return {
        ...prevState,
        objectType: {
          ...prevState.objectType,
          properties: newProperties
        }
      }
    })
  }

  handleSelect(ordinal){
    this.setState((prevState) => ({
      ...prevState,
      objectType: {
        ...prevState.objectType,
        properties: prevState.objectType.properties.map(property => {
          return {
            ...property,
            selected: property.ordinal === ordinal ? !property.selected : false
          }
        })
      }
    }))
  }

  handleReorder(oldPropertyIndex, newPropertyIndex){
    let oldProperties = this.state.objectType.properties
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
      objectType: {
        ...prevState.objectType,
        properties: newProperties
      }
    }))
  }

  validate(){
    let valid = true

    let newProperties = this.state.objectType.properties.map(property => {
      let errors = {}

      if(!property.propertyType){
        errors['propertyType'] = 'Cannot be empty'
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
      objectType: {
        ...prevState.objectType,
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
            objectType={this.state.objectType}
            propertyTypes={this.state.propertyTypes}
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
