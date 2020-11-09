import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import DynamicPropertyPluginFormSelectionType from '@src/js/components/tools/form/dynamicproperty/DynamicPropertyPluginFormSelectionType.js'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class DynamicPropertyPluginFormScript extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {}
    this.references = {
      script: React.createRef()
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
    if (this.props.selection) {
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
    this.props.onChange(DynamicPropertyPluginFormSelectionType.PLUGIN, {
      field: event.target.name,
      value: event.target.value
    })
  }

  handleFocus(event) {
    this.props.onSelectionChange(
      DynamicPropertyPluginFormSelectionType.PLUGIN,
      {
        part: event.target.name
      }
    )
  }

  handleBlur() {
    this.props.onBlur()
  }

  render() {
    logger.log(logger.DEBUG, 'DynamicPropertyPluginFormScript.render')

    const { plugin } = this.props

    return (
      <Container>
        <Header>Script</Header>
        {this.renderScript(plugin)}
      </Container>
    )
  }

  renderScript(plugin) {
    const { visible, enabled, error, value } = { ...plugin.script }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.script}
          label='Script'
          name='script'
          mandatory={true}
          multiline={true}
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
}

export default withStyles(styles)(DynamicPropertyPluginFormScript)
