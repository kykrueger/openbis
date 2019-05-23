import _ from 'lodash'
import React from 'react'
import FormControlLabel from '@material-ui/core/FormControlLabel'
import TextField from '@material-ui/core/TextField'
import MenuItem from '@material-ui/core/MenuItem'
import Checkbox from '@material-ui/core/Checkbox'
import InfoIcon from '@material-ui/icons/InfoOutlined'
import Tooltip from '@material-ui/core/Tooltip'
import {withStyles} from '@material-ui/core/styles'
import {facade, dto} from '../../../services/openbis.js'
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
      vocabularyValue: '',
      materialValue: '',
    }
    this.setMaterial = this.setMaterial.bind(this)
    this.setVocabulary = this.setVocabulary.bind(this)
  }

  static getDerivedStateFromProps(props, state) {
    if(!state.property || state.property.propertyType !== props.property.propertyType){
      return {
        loaded: false,
        property: props.property,
        vocabularyTerms: [],
        materials: []
      }
    }else{
      return null
    }
  }

  componentDidMount(){
    this.load()
  }

  componentDidUpdate(){
    this.load()
  }

  load(){
    if(this.state.loaded){
      return
    }

    const {propertyType} = this.state.property

    switch(propertyType.dataType){
      case 'CONTROLLEDVOCABULARY':
        this.loadVocabulary()
        return
      case 'MATERIAL':
        this.loadMaterial()
        return
      default:
        this.setState(() => ({
          loaded: true
        }))
        return
    }
  }

  render(){
    logger.log(logger.DEBUG, 'ObjectTypePropertyPreview.render')

    const {classes} = this.props

    return (
      <div className={classes.container} onClick={(event) => {event.stopPropagation()}}>
        {this.renderField()}
        <Tooltip title={this.getDescription()}>
          <InfoIcon />
        </Tooltip>
      </div>
    )
  }

  renderField(){
    const {propertyType} = this.state.property

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
      case 'MATERIAL':
        return this.renderMaterial()
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

  loadVocabulary(){
    let criteria = new dto.VocabularyTermSearchCriteria()
    let fo = new dto.VocabularyTermFetchOptions()

    criteria.withVocabulary().withCode().thatEquals(this.state.property.propertyType.vocabulary.code)

    return facade.searchVocabularyTerms(criteria, fo).then(result => {
      this.setState(() => ({
        loaded: true,
        vocabularyTerms: result.objects
      }))
    })
  }

  getVocabulary(){
    return this.state.vocabularyValue
  }

  setVocabulary(event){
    this.setState(() => ({
      vocabularyValue: event.target.value
    }))
  }

  renderVocabulary(){
    return (
      <TextField
        select
        label={this.getLabel()}
        value={this.getVocabulary()}
        onChange={this.setVocabulary}
        fullWidth={true}
        variant="filled"
      >
        <MenuItem value=""></MenuItem>
        {this.state.vocabularyTerms.map(term => (
          <MenuItem key={term.code} value={term.code}>{term.label || term.code}</MenuItem>
        ))}
      </TextField>
    )
  }

  loadMaterial(){
    let criteria = new dto.MaterialSearchCriteria()
    let fo = new dto.MaterialFetchOptions()

    let materialType = this.state.property.propertyType.materialType
    if(materialType){
      criteria.withType().withId().thatEquals(materialType.permId)
    }

    return facade.searchMaterials(criteria, fo).then(result => {
      this.setState(() => ({
        loaded: true,
        materials: result.objects
      }))
    })
  }

  getMaterial(){
    return this.state.materialValue
  }

  setMaterial(event){
    this.setState(() => ({
      materialValue: event.target.value
    }))
  }

  renderMaterial(){
    return (
      <TextField
        select
        label={this.getLabel()}
        value={this.getMaterial()}
        onChange={this.setMaterial}
        fullWidth={true}
        variant="filled"
      >
        <MenuItem value=""></MenuItem>
        {this.state.materials.map(material => (
          <MenuItem key={material.code} value={material.code}>{material.code}</MenuItem>
        ))}
      </TextField>
    )
  }

  renderUnsupported(){
    return (<div>unsupported</div>)
  }

  getLabel(){
    let mandatory = this.state.property.mandatory
    let label = this.state.property.propertyType.label
    return mandatory ? label + '*' : label
  }

  getDescription(){
    return this.state.property.propertyType.description
  }

}

export default _.flow(
  withStyles(styles)
)(ObjectTypePropertyPreview)
