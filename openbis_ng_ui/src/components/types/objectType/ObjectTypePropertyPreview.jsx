import _ from 'lodash'
import React from 'react'
import FormControl from '@material-ui/core/FormControl'
import FormControlLabel from '@material-ui/core/FormControlLabel'
import FormHelperText from '@material-ui/core/FormHelperText'
import TextField from '@material-ui/core/TextField'
import Select from '@material-ui/core/Select'
import MenuItem from '@material-ui/core/MenuItem'
import Checkbox from '@material-ui/core/Checkbox'
import {withStyles} from '@material-ui/core/styles'
import logger from '../../../common/logger.js'

const styles = () => ({
})

class ObjectTypePropertyPreview extends React.Component {

  constructor(props){
    super(props)
    this.state = {
      values: {
        vocabulary: '',
      }
    }
  }

  handleChange(name){
    return (event) => {
      this.setState((prevState) => ({
        ...prevState,
        values: {
          ...prevState.values,
          [name]: _.has(event.target, 'checked') ? event.target.checked : event.target.value
        }
      }))
    }
  }

  render(){
    logger.log(logger.DEBUG, 'ObjectTypePropertyPreview.render')

    const {propertyType} = this.props

    switch(propertyType.dataType){
      case 'BOOLEAN':
        return this.renderBoolean()
      case 'VARCHAR':
        return this.renderVarchar()
      case 'MULTILINE_VARCHAR':
        return this.renderMultilineVarchar()
      case 'INTEGER':
      case 'REAL':
        return this.renderNumber()
      case 'CONTROLLEDVOCABULARY':
        return this.renderVocabulary()
      default:
        return this.renderUnsupported()
    }
  }

  renderBoolean(){
    return (
      <FormControl>
        <FormControlLabel
          control={<Checkbox />}
          label={this.getLabel()}
        />
        <FormHelperText>{this.getDescription()}</FormHelperText>
      </FormControl>
    )
  }

  renderVarchar(){
    return (
      <TextField
        label={this.getLabel()}
        helperText={this.getDescription()}
        variant="filled"
      />
    )
  }

  renderMultilineVarchar(){
    return (
      <TextField
        label={this.getLabel()}
        helperText={this.getDescription()}
        multiline={true}
        variant="filled"
      />
    )
  }

  renderNumber(){
    return (
      <TextField
        label={this.getLabel()}
        helperText={this.getDescription()}
        type="number"
        variant="filled"
      />
    )
  }

  renderVocabulary(){
    return (
      <FormControl>
        <FormControlLabel
          control={
            <Select value={this.getValue('vocabulary')} onChange={this.handleChange('vocabulary')}>
              <MenuItem value=""></MenuItem>
              {this.getTerms().map(term => (
                <MenuItem key={term.code} value={term.code}>{term.label || term.code}</MenuItem>
              ))}
            </Select>
          }
          label={this.getLabel()}
          labelPlacement="top"
        />
        <FormHelperText>{this.getDescription()}</FormHelperText>
      </FormControl>
    )
  }

  renderUnsupported(){
    return (<div>unsupported</div>)
  }

  getValue(field){
    return this.state.values[field]
  }

  getLabel(){
    let mandatory = this.props.property.mandatory
    let label = this.props.propertyType.label
    return mandatory ? label + '*' : label
  }

  getDescription(){
    return this.props.propertyType.description
  }

  getTerms(){
    return this.props.propertyType.vocabulary.terms
  }

}

export default _.flow(
  withStyles(styles)
)(ObjectTypePropertyPreview)
