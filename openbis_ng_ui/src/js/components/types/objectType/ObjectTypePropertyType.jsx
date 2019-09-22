import _ from 'lodash'
import React from 'react'
import TextField from '@material-ui/core/TextField'
import FormHelperText from '@material-ui/core/FormHelperText'
import EditableField from '../../common/form/EditableField.jsx'
import logger from '../../../common/logger.js'

class ObjectTypePropertyType extends React.Component {
  constructor(props) {
    super(props)
    this.renderField = this.renderField.bind(this)
    this.handleChange = this.handleChange.bind(this)
  }

  handleChange(event) {
    event.stopPropagation()
    let propertyType = _.find(this.props.propertyTypes, propertyType => {
      return propertyType.code === event.target.value
    })
    this.props.onChange(this.props.property.id, 'propertyType', propertyType)
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectTypePropertyType.render')

    return <EditableField renderField={this.renderField} />
  }

  renderField({ ref, edited, handleBlur }) {
    const { property, propertyTypes } = this.props
    const { propertyType } = property

    if (edited) {
      return (
        <TextField
          inputRef={ref}
          select
          SelectProps={{
            native: true
          }}
          value={propertyType ? propertyType.code : ''}
          onChange={this.handleChange}
          onBlur={handleBlur}
          fullWidth={true}
          error={this.hasError()}
          helperText={this.getError()}
        >
          <option value=''></option>
          {propertyTypes &&
            propertyTypes.map(propertyType => (
              <option key={propertyType.code} value={propertyType.code}>
                {propertyType.code}
              </option>
            ))}
        </TextField>
      )
    } else {
      return (
        <div>
          <div>{propertyType ? propertyType.code : ''}</div>
          {this.hasError() && (
            <FormHelperText error={true}>{this.getError()}</FormHelperText>
          )}
        </div>
      )
    }
  }

  hasError() {
    return this.getError() ? true : false
  }

  getError() {
    return this.props.property.errors['propertyType']
  }
}

export default ObjectTypePropertyType
