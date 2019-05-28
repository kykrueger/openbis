import _ from 'lodash'
import React from 'react'
import TextField from '@material-ui/core/TextField'
import logger from '../../../common/logger.js'

class ObjectTypePropertyType extends React.Component {

  constructor(props){
    super(props)
    this.handleClick = this.handleClick.bind(this)
    this.handleChange = this.handleChange.bind(this)
  }

  handleClick(event){
    event.stopPropagation()
  }

  handleChange(event){
    event.stopPropagation()
    let propertyType = _.find(this.props.propertyTypes, propertyType => {
      return propertyType.code === event.target.value
    })
    this.props.onChange(this.props.property.id, 'propertyType', propertyType)
  }

  render(){
    logger.log(logger.DEBUG, 'ObjectTypePropertyType.render')

    const {property, propertyTypes} = this.props
    const {propertyType, errors} = property

    return (
      <TextField
        select
        SelectProps={{
          native: true,
        }}
        value={propertyType ? propertyType.code : ''}
        onClick={this.handleClick}
        onChange={this.handleChange}
        fullWidth={true}
        error={errors['propertyType'] ? true : false}
        helperText={errors['propertyType']}
      >
        <option value=""></option>
        {propertyTypes && propertyTypes.map(propertyType => (
          <option key={propertyType.code} value={propertyType.code}>{propertyType.code}</option>
        ))}
      </TextField>
    )
  }

}

export default ObjectTypePropertyType
