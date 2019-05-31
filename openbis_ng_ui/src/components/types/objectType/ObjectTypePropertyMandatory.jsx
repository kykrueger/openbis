import _ from 'lodash'
import React from 'react'
import Checkbox from '@material-ui/core/Checkbox'
import EditableField from '../../common/form/EditableField.jsx'
import {withStyles} from '@material-ui/core/styles'
import logger from '../../../common/logger.js'

const styles = () => ({
  checkbox: {
    padding: '0px'
  },
})

class ObjectTypePropertyMandatory extends React.Component {

  constructor(props){
    super(props)
    this.renderField = this.renderField.bind(this)
    this.handleChange = this.handleChange.bind(this)
  }

  handleChange(event){
    event.stopPropagation()
    this.props.onChange(this.props.property.id, 'mandatory', event.target.checked)
  }

  render(){
    logger.log(logger.DEBUG, 'ObjectTypePropertyMandatory.render')

    return <EditableField renderField={this.renderField} />
  }

  renderField({ref, edited, handleBlur}){
    const {classes, property} = this.props

    if(edited){
      return (
        <Checkbox
          checked={property.mandatory}
          value='mandatory'
          onChange={this.handleChange}
          onBlur={handleBlur}
          inputRef={ref}
          classes={{ root: classes.checkbox }}
        />
      )
    }else{
      return (
        <span>{property.mandatory ? 'true' : 'false'}</span>
      )
    }
  }

}

export default _.flow(
  withStyles(styles),
)(ObjectTypePropertyMandatory)
