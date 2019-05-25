import React from 'react'
import Checkbox from '@material-ui/core/Checkbox'
import logger from '../../../common/logger.js'

class ObjectTypePropertyMandatory extends React.Component {

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
    this.props.onChange(this.props.property.ordinal, 'mandatory', event.target.checked)
  }

  render(){
    logger.log(logger.DEBUG, 'ObjectTypePropertyMandatory.render')

    const {property} = this.props

    return (
      <Checkbox
        checked={property.mandatory}
        value='mandatory'
        onClick={this.handleClick}
        onChange={this.handleChange}
      />
    )
  }

}

export default ObjectTypePropertyMandatory
