import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import AutocompleterField from '@src/js/components/common/form/AutocompleterField.jsx'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import openbis from '@src/js/services/openbis.js'
import logger from '@src/js/common/logger.js'

import TypeFormWarningUsage from './TypeFormWarningUsage.jsx'
import TypeFormWarningLegacy from './TypeFormWarningLegacy.jsx'
import TypeFormHeader from './TypeFormHeader.jsx'

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

class TypeFormParametersProperty extends React.PureComponent {
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
      plugin: React.createRef(),
      showInEditView: React.createRef()
    }
    this.handleChange = this.handleChange.bind(this)
    this.handleFocus = this.handleFocus.bind(this)
    this.handleBlur = this.handleBlur.bind(this)
  }

  componentDidMount() {
    this.focus()
  }

  componentDidUpdate(prevProps) {
    const prevSelection = prevProps.selection
    const selection = this.props.selection

    if (prevSelection !== selection) {
      this.focus()
    }
  }

  focus() {
    const property = this.getProperty(this.props)
    if (property) {
      const { part } = this.props.selection.params
      if (part) {
        const reference = this.references[part]
        if (reference && reference.current) {
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
    logger.log(logger.DEBUG, 'TypeFormParametersProperty.render')

    const property = this.getProperty(this.props)
    if (!property) {
      return null
    }

    const { classes } = this.props

    return (
      <div className={classes.container}>
        <TypeFormHeader className={classes.header}>Property</TypeFormHeader>
        {this.renderWarningLegacy(property)}
        {this.renderWarningUsage(property)}
        {this.renderScope(property)}
        {this.renderCode(property)}
        {this.renderDataType(property)}
        {this.renderVocabulary(property)}
        {this.renderMaterialType(property)}
        {this.renderSchema(property)}
        {this.renderTransformation(property)}
        {this.renderLabel(property)}
        {this.renderDescription(property)}
        {this.renderDynamicPlugin(property)}
        {this.renderVisible(property)}
        {this.renderMandatory(property)}
        {this.renderInitialValue(property)}
      </div>
    )
  }

  renderWarningLegacy(property) {
    if (this.isLegacy(property)) {
      const { classes } = this.props
      return (
        <div className={classes.field}>
          <TypeFormWarningLegacy />
        </div>
      )
    } else {
      return null
    }
  }

  renderWarningUsage(property) {
    if (property.usages > 0) {
      const { classes } = this.props
      return (
        <div className={classes.field}>
          <TypeFormWarningUsage subject='property' usages={property.usages} />
        </div>
      )
    } else {
      return null
    }
  }

  renderScope(property) {
    const { classes } = this.props

    const options = [
      { label: 'Local', value: 'local' },
      { label: 'Global', value: 'global' }
    ]

    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.scope}
          label='Scope'
          name='scope'
          mandatory={true}
          error={property.errors.scope}
          disabled={!!property.original}
          value={property.scope.value}
          options={options}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
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
          mandatory={true}
          error={property.errors.label}
          disabled={this.isLegacy(property)}
          value={property.label.value}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderCode(property) {
    const { classes, controller } = this.props

    if (property.scope.value === 'local') {
      return (
        <div className={classes.field}>
          <TextField
            reference={this.references.code}
            label='Code'
            name='code'
            mandatory={true}
            error={property.errors.code}
            disabled={!!property.original}
            value={property.code.value}
            onChange={this.handleChange}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
          />
        </div>
      )
    } else if (property.scope.value === 'global') {
      const { globalPropertyTypes = [] } = controller.getDictionaries()

      const options = globalPropertyTypes.map(globalPropertyType => {
        return globalPropertyType.code
      })

      return (
        <div className={classes.field}>
          <AutocompleterField
            reference={this.references.code}
            label='Code'
            name='code'
            options={options}
            mandatory={true}
            error={property.errors.code}
            disabled={!!property.original}
            value={property.code.value}
            onChange={this.handleChange}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
          />
        </div>
      )
    }
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
          disabled={this.isLegacy(property)}
          value={property.description.value}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderDataType(property) {
    const { classes } = this.props

    const options = openbis.DataType.values.sort().map(dataType => {
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
          disabled={property.usages > 0 || this.isLegacy(property)}
          value={property.dataType.value}
          options={options}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderVocabulary(property) {
    if (property.dataType.value === openbis.DataType.CONTROLLEDVOCABULARY) {
      const { classes, controller } = this.props
      const { vocabularies = [] } = controller.getDictionaries()

      let options = []

      if (vocabularies.length > 0) {
        options = vocabularies.map(vocabulary => {
          return {
            label: vocabulary.code,
            value: vocabulary.code
          }
        })
        options.unshift({})
      }

      return (
        <div className={classes.field}>
          <SelectField
            reference={this.references.vocabulary}
            label='Vocabulary'
            name='vocabulary'
            mandatory={true}
            error={property.errors.vocabulary}
            disabled={property.usages > 0 || this.isLegacy(property)}
            value={property.vocabulary.value}
            options={options}
            onChange={this.handleChange}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
          />
        </div>
      )
    } else {
      return null
    }
  }

  renderMaterialType(property) {
    if (property.dataType.value === openbis.DataType.MATERIAL) {
      const { classes, controller } = this.props
      const { materialTypes = [] } = controller.getDictionaries()

      let options = []

      if (materialTypes.length > 0) {
        options = materialTypes.map(materialType => {
          return {
            label: materialType.code,
            value: materialType.code
          }
        })
        options.unshift({})
      }

      return (
        <div className={classes.field}>
          <SelectField
            reference={this.references.materialType}
            label='Material Type'
            name='materialType'
            mandatory={true}
            error={property.errors.materialType}
            disabled={property.usages > 0 || this.isLegacy(property)}
            value={property.materialType.value}
            options={options}
            onChange={this.handleChange}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
          />
        </div>
      )
    } else {
      return null
    }
  }

  renderSchema(property) {
    if (property.dataType.value === openbis.DataType.XML) {
      const { classes } = this.props
      return (
        <div className={classes.field}>
          <TextField
            reference={this.references.schema}
            label='XML Schema'
            name='schema'
            error={property.errors.schema}
            disabled={this.isLegacy(property)}
            value={property.schema.value}
            multiline={true}
            onChange={this.handleChange}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
          />
        </div>
      )
    } else {
      return null
    }
  }

  renderTransformation(property) {
    if (property.dataType.value === openbis.DataType.XML) {
      const { classes } = this.props
      return (
        <div className={classes.field}>
          <TextField
            reference={this.references.transformation}
            label='XSLT Script'
            name='transformation'
            error={property.errors.transformation}
            disabled={this.isLegacy(property)}
            value={property.transformation.value}
            multiline={true}
            onChange={this.handleChange}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
          />
        </div>
      )
    } else {
      return null
    }
  }

  renderDynamicPlugin(property) {
    const { classes, controller } = this.props
    const { dynamicPlugins = [] } = controller.getDictionaries()

    let options = []

    if (dynamicPlugins.length > 0) {
      options = dynamicPlugins.map(dynamicPlugin => {
        return {
          label: dynamicPlugin.name,
          value: dynamicPlugin.name
        }
      })
      options.unshift({})
    }

    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.plugin}
          label='Dynamic Plugin'
          name='plugin'
          error={property.errors.plugin}
          disabled={property.usages > 0}
          value={property.plugin.value}
          options={options}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
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
          value={property.mandatory.value}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderInitialValue(property) {
    const { classes, type } = this.props

    const typeIsUsed = type.usages > 0
    const propertyIsNew = !property.original
    const propertyIsMandatory = property.mandatory.value
    const propertyWasMandatory = property.original
      ? property.original.mandatory.value
      : false

    if (
      typeIsUsed &&
      propertyIsMandatory &&
      (propertyIsNew || !propertyWasMandatory)
    ) {
      return (
        <div className={classes.field}>
          <TextField
            reference={this.references.initialValueForExistingEntities}
            label='Initial Value'
            name='initialValueForExistingEntities'
            mandatory={true}
            error={property.errors.initialValueForExistingEntities}
            value={property.initialValueForExistingEntities.value}
            onChange={this.handleChange}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
          />
        </div>
      )
    } else {
      return null
    }
  }

  renderVisible(property) {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.showInEditView}
          label='Visible'
          name='showInEditView'
          value={property.showInEditView.value}
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

  isLegacy(property) {
    return (
      property.original &&
      !property.code.value.startsWith(this.props.type.code.value + '.')
    )
  }
}

export default withStyles(styles)(TypeFormParametersProperty)
