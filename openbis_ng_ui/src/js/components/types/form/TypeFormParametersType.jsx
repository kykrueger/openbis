import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import logger from '@src/js/common/logger.js'

import TypeFormControllerStrategies from './TypeFormControllerStrategies.js'
import TypeFormWarningUsage from './TypeFormWarningUsage.jsx'
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

class TypeFormParametersType extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {}
    this.references = {
      code: React.createRef(),
      description: React.createRef(),
      validationPlugin: React.createRef(),
      listable: React.createRef(),
      showContainer: React.createRef(),
      showParents: React.createRef(),
      showParentMetadata: React.createRef(),
      generatedCodePrefix: React.createRef(),
      autoGeneratedCode: React.createRef(),
      subcodeUnique: React.createRef()
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
    const type = this.getType(this.props)
    if (type && this.props.selection) {
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
    return {
      field: event.target.name,
      part: event.target.name,
      value: event.target.value
    }
  }

  handleChange(event) {
    this.props.onChange('type', this.params(event))
  }

  handleFocus(event) {
    this.props.onSelectionChange('type', this.params(event))
  }

  handleBlur(event) {
    this.props.onBlur('type', this.params(event))
  }

  render() {
    logger.log(logger.DEBUG, 'TypeFormParametersType.render')

    const type = this.getType(this.props)
    if (!type) {
      return null
    }

    const { classes } = this.props

    return (
      <div className={classes.container}>
        <TypeFormHeader className={classes.header}>Type</TypeFormHeader>
        {this.renderWarning(type)}
        {this.renderCode(type)}
        {this.renderDescription(type)}
        {this.renderValidationPlugin(type)}
        {this.renderObjectTypeAttributes(type)}
        {this.renderDataSetTypeAttributes(type)}
      </div>
    )
  }

  renderWarning(type) {
    if (type.usages > 0) {
      const { classes } = this.props
      return (
        <div className={classes.field}>
          <TypeFormWarningUsage subject='type' usages={type.usages} />
        </div>
      )
    } else {
      return null
    }
  }

  renderCode(type) {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.code}
          label='Code'
          name='code'
          mandatory={true}
          error={type.errors.code}
          disabled={!!type.original}
          value={type.code.value}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderDescription(type) {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.description}
          label='Description'
          name='description'
          error={type.errors.description}
          value={type.description.value}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderValidationPlugin(type) {
    const { classes, controller } = this.props
    const { validationPlugins = [] } = controller.getDictionaries()

    let options = []

    if (validationPlugins.length > 0) {
      options = validationPlugins.map(validationPlugin => {
        return {
          label: validationPlugin.name,
          value: validationPlugin.name
        }
      })
      options.unshift({})
    }

    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.validationPlugin}
          label='Validation Plugin'
          name='validationPlugin'
          error={type.errors.validationPlugin}
          value={type.validationPlugin.value}
          options={options}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderObjectTypeAttributes(type) {
    if (TypeFormControllerStrategies.isObjectType(type.objectType.value)) {
      return (
        <React.Fragment>
          {this.renderGeneratedCodePrefix(type)}
          {this.renderAutoGeneratedCode(type)}
          {this.renderSubcodeUnique(type)}
          {this.renderShowParents(type)}
          {this.renderShowContainer(type)}
          {this.renderShowParentMetadata(type)}
          {this.renderListable(type)}
        </React.Fragment>
      )
    } else {
      return null
    }
  }

  renderDataSetTypeAttributes(type) {
    if (TypeFormControllerStrategies.isDataSetType(type.objectType.value)) {
      return (
        <React.Fragment>
          {this.renderMainDataSetPattern(type)}
          {this.renderMainDataSetPath(type)}
          {this.renderDisallowDeletion(type)}
        </React.Fragment>
      )
    } else {
      return null
    }
  }

  renderListable(type) {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.listable}
          label='Listable'
          name='listable'
          error={type.errors.listable}
          value={type.listable.value}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderShowContainer(type) {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.showContainer}
          label='Show Container'
          name='showContainer'
          error={type.errors.showContainer}
          value={type.showContainer.value}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderShowParents(type) {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.showParents}
          label='Show Parents'
          name='showParents'
          error={type.errors.showParents}
          value={type.showParents.value}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderShowParentMetadata(type) {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.showParentMetadata}
          label='Show Parent Metadata'
          name='showParentMetadata'
          error={type.errors.showParentMetadata}
          value={type.showParentMetadata.value}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderGeneratedCodePrefix(type) {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.generatedCodePrefix}
          label='Generated code prefix'
          name='generatedCodePrefix'
          mandatory={true}
          error={type.errors.generatedCodePrefix}
          value={type.generatedCodePrefix.value}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderAutoGeneratedCode(type) {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.autoGeneratedCode}
          label='Generate Codes'
          name='autoGeneratedCode'
          error={type.errors.autoGeneratedCode}
          value={type.autoGeneratedCode.value}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderSubcodeUnique(type) {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.subcodeUnique}
          label='Unique Subcodes'
          name='subcodeUnique'
          error={type.errors.subcodeUnique}
          value={type.subcodeUnique.value}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderDisallowDeletion(type) {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.disallowDeletion}
          label='Disallow Deletion'
          name='disallowDeletion'
          error={type.errors.disallowDeletion}
          value={type.disallowDeletion.value}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderMainDataSetPattern(type) {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.mainDataSetPattern}
          label='Main Data Set Pattern'
          name='mainDataSetPattern'
          error={type.errors.mainDataSetPattern}
          value={type.mainDataSetPattern.value}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderMainDataSetPath(type) {
    const { classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.mainDataSetPath}
          label='Main Data Set Path'
          name='mainDataSetPath'
          error={type.errors.mainDataSetPath}
          value={type.mainDataSetPath.value}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  getType(props) {
    let { type, selection } = props

    if (!selection || selection.type === 'type') {
      return type
    } else {
      return null
    }
  }
}

export default withStyles(styles)(TypeFormParametersType)
