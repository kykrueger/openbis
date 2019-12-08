import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Typography from '@material-ui/core/Typography'
import CheckboxField from '../../common/form/CheckboxField.jsx'
import TextField from '../../common/form/TextField.jsx'
import SelectField from '../../common/form/SelectField.jsx'
import Collapse from '@material-ui/core/Collapse'
import WarningIcon from '@material-ui/icons/Warning'
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
  },
  warning: {
    display: 'flex',
    alignItems: 'center',
    '& svg': {
      marginRight: theme.spacing(1),
      color: theme.palette.warning.main
    }
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
      vocabulary: React.createRef(),
      materialType: React.createRef(),
      schema: React.createRef(),
      transformation: React.createRef(),
      initialValueForExistingEntities: React.createRef(),
      mandatory: React.createRef(),
      showInEditView: React.createRef()
    }
    this.handleChange = this.handleChange.bind(this)
    this.handleFocus = this.handleFocus.bind(this)
    this.handleBlur = this.handleBlur.bind(this)
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

      if (dataType === dto.DataType.CONTROLLEDVOCABULARY) {
        this.loadVocabularies()
      } else if (dataType === dto.DataType.MATERIAL) {
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

  params(event) {
    const property = this.getProperty(this.props)

    return {
      id: property.id,
      field: event.target.name,
      part: event.target.name,
      value: event.target.value
    }
  }

  handleChange(event) {
    this.props.onChange('property', this.params(event))
  }

  handleFocus(event) {
    this.props.onSelectionChange('property', this.params(event))
  }

  handleBlur(event) {
    this.props.onBlur('property', this.params(event))
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
        {this.renderWarning(property)}
        {this.renderCode(property)}
        {this.renderDataType(property)}
        {this.renderVocabulary(property)}
        {this.renderMaterialType(property)}
        {this.renderSchema(property)}
        {this.renderTransformation(property)}
        {this.renderLabel(property)}
        {this.renderDescription(property)}
        {this.renderVisible(property)}
        {this.renderMandatory(property)}
        {this.renderInitialValue(property)}
      </div>
    )
  }

  renderWarning(property) {
    if (property.used) {
      const { classes } = this.props
      return (
        <div className={classes.field}>
          <Typography variant='body2' className={classes.warning}>
            <WarningIcon />
            This property is already used by some entities.
          </Typography>
        </div>
      )
    } else {
      return null
    }
  }

  renderLabel(property) {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.label}
          label='Label'
          name='label'
          mandatory={true}
          error={property.errors.label}
          value={property.label}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
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
          mandatory={true}
          error={property.errors.code}
          disabled={property.used}
          value={property.code}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
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
          mandatory={true}
          error={property.errors.description}
          value={property.description}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderDataType(property) {
    const { classes } = this.props

    const options = dto.DataType.values.sort().map(dataType => {
      return {
        label: dataType,
        value: dataType
      }
    })

    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.dataType}
          label='Data Type'
          name='dataType'
          mandatory={true}
          error={property.errors.dataType}
          disabled={property.used}
          value={property.dataType}
          options={options}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderVocabulary(property) {
    const { classes } = this.props
    const { vocabularies } = this.state

    let options = []

    if (vocabularies) {
      options = vocabularies.map(vocabulary => {
        return {
          label: vocabulary.code,
          value: vocabulary.code
        }
      })
      options.unshift({})
    }

    return (
      <Collapse in={property.dataType === dto.DataType.CONTROLLEDVOCABULARY}>
        <div className={classes.field}>
          <SelectField
            reference={this.references.vocabulary}
            label='Vocabulary'
            name='vocabulary'
            mandatory={true}
            error={property.errors.vocabulary}
            disabled={property.used}
            value={property.vocabulary}
            options={options}
            onChange={this.handleChange}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
          />
        </div>
      </Collapse>
    )
  }

  renderMaterialType(property) {
    const { classes } = this.props
    const { materialTypes } = this.state

    let options = []

    if (materialTypes) {
      options = materialTypes.map(materialType => {
        return {
          label: materialType.code,
          value: materialType.code
        }
      })
      options.unshift({})
    }

    return (
      <Collapse in={property.dataType === dto.DataType.MATERIAL}>
        <div className={classes.field}>
          <SelectField
            reference={this.references.materialType}
            label='Material Type'
            name='materialType'
            mandatory={true}
            error={property.errors.materialType}
            disabled={property.used}
            value={property.materialType}
            options={options}
            onChange={this.handleChange}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
          />
        </div>
      </Collapse>
    )
  }

  renderSchema(property) {
    const { classes } = this.props
    return (
      <Collapse in={property.dataType === dto.DataType.XML}>
        <div className={classes.field}>
          <TextField
            reference={this.references.schema}
            label='XML Schema'
            name='schema'
            error={property.errors.schema}
            value={property.schema}
            multiline={true}
            onChange={this.handleChange}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
          />
        </div>
      </Collapse>
    )
  }

  renderTransformation(property) {
    const { classes } = this.props
    return (
      <Collapse in={property.dataType === dto.DataType.XML}>
        <div className={classes.field}>
          <TextField
            reference={this.references.transformation}
            label='XSLT Script'
            name='transformation'
            error={property.errors.transformation}
            value={property.transformation}
            multiline={true}
            onChange={this.handleChange}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
          />
        </div>
      </Collapse>
    )
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
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderInitialValue(property) {
    const { type, classes } = this.props

    const wasMandatory = property.original ? property.original.mandatory : false
    const isMandatory = property.mandatory

    return (
      <Collapse
        in={type.used && !wasMandatory && isMandatory}
        mountOnEnter={true}
        unmountOnExit={true}
      >
        <div className={classes.field}>
          <TextField
            reference={this.references.initialValueForExistingEntities}
            label='Initial Value'
            name='initialValueForExistingEntities'
            mandatory={true}
            error={property.errors.initialValueForExistingEntities}
            value={property.initialValueForExistingEntities}
            onChange={this.handleChange}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
          />
        </div>
      </Collapse>
    )
  }

  renderVisible(property) {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.showInEditView}
          label='Visible'
          name='showInEditView'
          value={property.showInEditView}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  getType() {
    return this.props.type
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
