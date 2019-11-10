import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Typography from '@material-ui/core/Typography'
import CheckboxField from '../../common/form/CheckboxField.jsx'
import TextField from '../../common/form/TextField.jsx'
import SelectField from '../../common/form/SelectField.jsx'
import { facade, dto } from '../../../services/openbis.js'
import logger from '../../../common/logger.js'

const styles = theme => ({
  container: {
    padding: theme.spacing(2)
  },
  header: {
    paddingBottom: theme.spacing(2)
  },
  field: {
    paddingBottom: theme.spacing(2)
  }
})

class ObjectTypeParametersProperty extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {}
    this.references = {
      code: React.createRef(),
      label: React.createRef(),
      description: React.createRef(),
      dataType: React.createRef(),
      mandatory: React.createRef()
    }
    this.handleChange = this.handleChange.bind(this)
    this.handleFocus = this.handleFocus.bind(this)
  }

  componentDidMount() {
    this.load()
    this.focus()
  }

  componentDidUpdate(prevProps) {
    const prevProperty = this.getProperty(prevProps)
    const property = this.getProperty(this.props)

    const prevDataType = prevProperty ? prevProperty.dataType : null
    const dataType = property ? property.dataType : null

    if (prevDataType !== dataType) {
      this.load()
    }

    const prevSelection = prevProps.selection
    const selection = this.props.selection

    if (prevSelection !== selection) {
      this.focus()
    }
  }

  load() {
    const property = this.getProperty(this.props)

    if (property) {
      const dataType = property.dataType

      if (dataType === 'CONTROLLEDVOCABULARY') {
        this.loadVocabularies()
      } else if (dataType === 'MATERIAL') {
        this.loadMaterialTypes()
      }
    }
  }

  loadVocabularies() {
    let criteria = new dto.VocabularySearchCriteria()
    let fo = new dto.VocabularyFetchOptions()

    return facade.searchVocabularies(criteria, fo).then(result => {
      this.setState(() => ({
        vocabularies: result.objects
      }))
    })
  }

  loadMaterialTypes() {
    let criteria = new dto.MaterialTypeSearchCriteria()
    let fo = new dto.MaterialTypeFetchOptions()

    return facade.searchMaterialTypes(criteria, fo).then(result => {
      this.setState(() => ({
        materialTypes: result.objects
      }))
    })
  }

  focus() {
    const property = this.getProperty(this.props)
    if (property) {
      const { part } = this.props.selection.params
      if (part) {
        const reference = this.references[part]
        if (reference) {
          reference.current.focus()
        }
      }
    }
  }

  handleChange(event) {
    const property = this.getProperty(this.props)

    const params = {
      id: property.id,
      field: event.target.name,
      value: event.target.value
    }

    this.props.onChange('property', params)
  }

  handleFocus(event) {
    const property = this.getProperty(this.props)

    let params = null

    if (_.has(event.target, 'checked')) {
      params = {
        id: property.id,
        part: event.target.value
      }
    } else {
      params = {
        id: property.id,
        part: event.target.name
      }
    }

    this.props.onSelectionChange('property', params)
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectTypeParametersProperty.render')

    const property = this.getProperty(this.props)
    if (!property) {
      return null
    }

    const { classes } = this.props

    return (
      <div className={classes.container}>
        <Typography variant='h6' className={classes.header}>
          Property
        </Typography>
        <form>
          {this.renderLabel(property)}
          {this.renderCode(property)}
          {this.renderDescription(property)}
          {this.renderDataType(property)}
          {this.renderVocabulary(property)}
          {this.renderMaterial(property)}
          {this.renderMandatory(property)}
          {this.renderVisible(property)}
        </form>
      </div>
    )
  }

  renderLabel(property) {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.label}
          label='Label'
          name='label'
          value={property.label}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
        />
      </div>
    )
  }

  renderCode(property) {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.code}
          label='Code'
          name='code'
          value={property.code}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
        />
      </div>
    )
  }

  renderDescription(property) {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.description}
          label='Description'
          name='description'
          value={property.description}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
        />
      </div>
    )
  }

  renderDataType(property) {
    const options = dto.DataType.values.sort().map(dataType => {
      return {
        label: dataType,
        value: dataType
      }
    })
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.dataType}
          label='Data Type'
          name='dataType'
          value={property.dataType}
          options={options}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
        />
      </div>
    )
  }

  renderVocabulary(property) {
    let { vocabularies } = this.state

    if (property.dataType === 'CONTROLLEDVOCABULARY' && vocabularies) {
      const options = vocabularies.map(vocabulary => {
        return {
          label: vocabulary.code,
          value: vocabulary.code
        }
      })
      const { classes } = this.props
      return (
        <div className={classes.field}>
          <SelectField
            label='Vocabulary'
            name='vocabulary'
            value={property.vocabulary ? property.vocabulary : ''}
            options={options}
            onChange={this.handleChange}
            onFocus={this.handleFocus}
          />
        </div>
      )
    } else {
      return null
    }
  }

  renderMaterial(property) {
    let { materialTypes } = this.state

    if (property.dataType === 'MATERIAL' && materialTypes) {
      const options = materialTypes.map(materialType => {
        return {
          label: materialType.code,
          value: materialType.code
        }
      })
      const { classes } = this.props
      return (
        <div className={classes.field}>
          <SelectField
            label='Material Type'
            name='materialType'
            value={property.materialType ? property.materialType : ''}
            options={options}
            onChange={this.handleChange}
            onFocus={this.handleFocus}
          />
        </div>
      )
    } else {
      return null
    }
  }

  renderMandatory(property) {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.mandatory}
          label='Mandatory'
          name='mandatory'
          value={property.mandatory}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
        />
      </div>
    )
  }

  renderVisible(property) {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          label='Visible'
          name='visible'
          value={property.visible}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
        />
      </div>
    )
  }

  getProperty(props) {
    let { properties, selection } = props

    if (selection && selection.type === 'property') {
      let [property] = properties.filter(
        property => property.id === selection.params.id
      )
      return property
    } else {
      return null
    }
  }
}

export default withStyles(styles)(ObjectTypeParametersProperty)
