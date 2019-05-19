import _ from 'lodash'
import React from 'react'
import FormControlLabel from '@material-ui/core/FormControlLabel'
import TextField from '@material-ui/core/TextField'
import MenuItem from '@material-ui/core/MenuItem'
import Checkbox from '@material-ui/core/Checkbox'
import InfoIcon from '@material-ui/icons/InfoOutlined'
import Tooltip from '@material-ui/core/Tooltip'
import {withStyles} from '@material-ui/core/styles'
import logger from '../../../common/logger.js'

const styles = () => ({
  container: {
    display: 'flex',
    flexDirection: 'row',
    alignItems: 'center'
  },
  boolean: {
    width: '100%'
  }
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

    const {classes} = this.props

    return (
      <div className={classes.container}>
        {this.renderField()}
        <Tooltip title={this.getDescription()}>
          <InfoIcon />
        </Tooltip>
      </div>
    )
  }

  renderField(){
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
    const {classes} = this.props
    return (
      <FormControlLabel classes={{ root: classes.boolean }}
        control={<Checkbox />}
        label={this.getLabel()}
      />
    )
  }

  renderVarchar(){
    return (
      <TextField
        label={this.getLabel()}
        fullWidth={true}
        variant="filled"
      />
    )
  }

  renderMultilineVarchar(){
    return (
      <TextField
        label={this.getLabel()}
        multiline={true}
        fullWidth={true}
        variant="filled"
      />
    )
  }

  renderNumber(){
    return (
      <TextField
        label={this.getLabel()}
        type="number"
        fullWidth={true}
        variant="filled"
      />
    )
  }

  renderVocabulary(){
    return (
      <TextField
        select
        label={this.getLabel()}
        value={this.getValue('vocabulary')}
        onChange={this.handleChange('vocabulary')}
        fullWidth={true}
        variant="filled"
      >
        <MenuItem value=""></MenuItem>
        {this.getTerms().map(term => (
          <MenuItem key={term.code} value={term.code}>{term.label || term.code}</MenuItem>
        ))}
      </TextField>
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
