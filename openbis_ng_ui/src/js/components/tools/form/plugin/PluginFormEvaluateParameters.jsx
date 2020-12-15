import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import AutocompleterField from '@src/js/components/common/form/AutocompleterField.jsx'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import PluginFormSelectionType from '@src/js/components/tools/form/plugin/PluginFormSelectionType.js'
import openbis from '@src/js/services/openbis.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  parameter: {
    paddingBottom: theme.spacing(1)
  }
})

class PluginFormEvaluateParameters extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleChange = this.handleChange.bind(this)
  }

  handleChange(event) {
    this.props.onChange(PluginFormSelectionType.EVALUATE_PARAMETER, {
      field: event.target.name,
      value: event.target.value
    })
  }

  render() {
    logger.log(logger.DEBUG, 'PluginFormEvaluateParameters.render')

    return (
      <Container>
        <Header>Tester</Header>
        {this.renderEntityKind()}
        {this.renderEntityId()}
        {this.renderEntityIsNew()}
      </Container>
    )
  }

  renderEntityKind() {
    const { parameters, classes } = this.props

    const options = openbis.EntityKind.values.map(entityKind => {
      return {
        label: entityKind,
        value: entityKind
      }
    })

    return (
      <div className={classes.parameter}>
        <SelectField
          label='Entity Kind'
          name='entityKind'
          value={parameters.entityKind.value}
          options={options}
          onChange={this.handleChange}
        />
      </div>
    )
  }

  renderEntityId() {
    const { parameters, classes } = this.props

    const options = []

    return (
      <div className={classes.parameter}>
        <AutocompleterField
          label='Entity'
          name='entityId'
          options={options}
          value={parameters.entityId.value}
          onChange={this.handleChange}
        />
      </div>
    )
  }

  renderEntityIsNew() {
    const { plugin, parameters, classes } = this.props

    if (plugin.pluginType === openbis.PluginType.ENTITY_VALIDATION) {
      return (
        <div className={classes.parameter}>
          <CheckboxField
            label='Is New Entity'
            name='entityIsNew'
            value={parameters.entityIsNew.value}
            onChange={this.handleChange}
          />
        </div>
      )
    } else {
      return null
    }
  }
}

export default withStyles(styles)(PluginFormEvaluateParameters)
