import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import logger from '@src/js/common/logger.js'

import TypeFormHeader from './TypeFormHeader.jsx'

const styles = theme => ({
  header: {
    paddingBottom: theme.spacing(1)
  },
  field: {
    paddingBottom: theme.spacing(1)
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

  handleChange(event) {
    this.props.onChange('type', {
      field: event.target.name,
      value: event.target.value
    })
  }

  handleFocus(event) {
    this.props.onSelectionChange('type', {
      part: event.target.name
    })
  }

  handleBlur() {
    this.props.onBlur()
  }

  render() {
    logger.log(logger.DEBUG, 'TypeFormParametersType.render')

    const type = this.getType(this.props)
    if (!type) {
      return null
    }

    const { classes } = this.props

    return (
      <Container>
        <TypeFormHeader className={classes.header}>Type</TypeFormHeader>
        {this.renderMessageUsage(type)}
        {this.renderCode(type)}
        {this.renderDescription(type)}
        {this.renderValidationPlugin(type)}
        {this.renderGeneratedCodePrefix(type)}
        {this.renderAutoGeneratedCode(type)}
        {this.renderSubcodeUnique(type)}
        {this.renderShowParents(type)}
        {this.renderShowContainer(type)}
        {this.renderShowParentMetadata(type)}
        {this.renderListable(type)}
        {this.renderMainDataSetPattern(type)}
        {this.renderMainDataSetPath(type)}
        {this.renderDisallowDeletion(type)}
      </Container>
    )
  }

  renderMessageUsage(type) {
    const { classes } = this.props

    function entities(number) {
      return number === 0 || number > 1
        ? `${number} entities`
        : `${number} entity`
    }

    function message(type) {
      return `This type is already used by ${entities(type.usages)}.`
    }

    if (type.usages !== 0) {
      return (
        <div className={classes.field}>
          <Message type='info'>{message(type)}</Message>
        </div>
      )
    } else {
      return null
    }
  }

  renderCode(type) {
    const { visible, enabled, error, value } = { ...type.code }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.code}
          label='Code'
          name='code'
          mandatory={true}
          error={error}
          disabled={!enabled}
          value={value}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderDescription(type) {
    const { visible, enabled, error, value } = { ...type.description }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.description}
          label='Description'
          name='description'
          error={error}
          disabled={!enabled}
          value={value}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderValidationPlugin(type) {
    const { visible, enabled, error, value } = { ...type.validationPlugin }

    if (!visible) {
      return null
    }

    const { mode, classes, controller } = this.props
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
          error={error}
          disabled={!enabled}
          value={value}
          options={options}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderListable(type) {
    const { visible, enabled, error, value } = { ...type.listable }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.listable}
          label='Listable'
          name='listable'
          error={error}
          disabled={!enabled}
          value={value}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderShowContainer(type) {
    const { visible, enabled, error, value } = { ...type.showContainer }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.showContainer}
          label='Show Container'
          name='showContainer'
          error={error}
          disabled={!enabled}
          value={value}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderShowParents(type) {
    const { visible, enabled, error, value } = { ...type.showParents }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.showParents}
          label='Show Parents'
          name='showParents'
          error={error}
          disabled={!enabled}
          value={value}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderShowParentMetadata(type) {
    const { visible, enabled, error, value } = { ...type.showParentMetadata }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.showParentMetadata}
          label='Show Parent Metadata'
          name='showParentMetadata'
          error={error}
          disabled={!enabled}
          value={value}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderGeneratedCodePrefix(type) {
    const { visible, enabled, error, value } = { ...type.generatedCodePrefix }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.generatedCodePrefix}
          label='Generated code prefix'
          name='generatedCodePrefix'
          mandatory={true}
          error={error}
          disabled={!enabled}
          value={value}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderAutoGeneratedCode(type) {
    const { visible, enabled, error, value } = { ...type.autoGeneratedCode }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.autoGeneratedCode}
          label='Generate Codes'
          name='autoGeneratedCode'
          error={error}
          disabled={!enabled}
          value={value}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderSubcodeUnique(type) {
    const { visible, enabled, error, value } = { ...type.subcodeUnique }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.subcodeUnique}
          label='Unique Subcodes'
          name='subcodeUnique'
          error={error}
          disabled={!enabled}
          value={value}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderDisallowDeletion(type) {
    const { visible, enabled, error, value } = { ...type.disallowDeletion }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.disallowDeletion}
          label='Disallow Deletion'
          name='disallowDeletion'
          error={error}
          disabled={!enabled}
          value={value}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderMainDataSetPattern(type) {
    const { visible, enabled, error, value } = { ...type.mainDataSetPattern }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.mainDataSetPattern}
          label='Main Data Set Pattern'
          name='mainDataSetPattern'
          error={error}
          disabled={!enabled}
          value={value}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderMainDataSetPath(type) {
    const { visible, enabled, error, value } = { ...type.mainDataSetPath }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.mainDataSetPath}
          label='Main Data Set Path'
          name='mainDataSetPath'
          error={error}
          disabled={!enabled}
          value={value}
          mode={mode}
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
