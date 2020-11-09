import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import PageWithTwoPanels from '@src/js/components/common/page/PageWithTwoPanels.jsx'
import DynamicPropertyPluginFormController from '@src/js/components/tools/form/dynamicproperty/DynamicPropertyPluginFormController.js'
import DynamicPropertyPluginFormFacade from '@src/js/components/tools/form/dynamicproperty/DynamicPropertyPluginFormFacade.js'
import DynamicPropertyPluginFormScript from '@src/js/components/tools/form/dynamicproperty/DynamicPropertyPluginFormScript.jsx'
import DynamicPropertyPluginFormParameters from '@src/js/components/tools/form/dynamicproperty/DynamicPropertyPluginFormParameters.jsx'
import DynamicPropertyPluginFormButtons from '@src/js/components/tools/form/dynamicproperty/DynamicPropertyPluginFormButtons.jsx'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class DynamicPropertyPluginForm extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)

    this.state = {}

    if (this.props.controller) {
      this.controller = this.props.controller
    } else {
      this.controller = new DynamicPropertyPluginFormController(
        new DynamicPropertyPluginFormFacade()
      )
    }

    this.controller.init(new ComponentContext(this))
  }

  componentDidMount() {
    this.controller.load()
  }

  render() {
    logger.log(logger.DEBUG, 'DynamicPropertyPluginForm.render')

    const { loading, loaded, plugin } = this.state

    return (
      <PageWithTwoPanels
        loading={loading}
        loaded={loaded}
        object={plugin}
        renderMainPanel={() => this.renderMainPanel()}
        renderAdditionalPanel={() => this.renderAdditionalPanel()}
        renderButtons={() => this.renderButtons()}
      />
    )
  }

  renderMainPanel() {
    const { controller } = this
    const { plugin, selection, mode } = this.state

    return (
      <DynamicPropertyPluginFormScript
        plugin={plugin}
        selection={selection}
        mode={mode}
        onChange={controller.handleChange}
        onSelectionChange={controller.handleSelectionChange}
        onBlur={controller.handleBlur}
      />
    )
  }

  renderAdditionalPanel() {
    const { controller } = this
    const { plugin, selection, mode } = this.state

    return (
      <DynamicPropertyPluginFormParameters
        plugin={plugin}
        selection={selection}
        mode={mode}
        onChange={controller.handleChange}
        onSelectionChange={controller.handleSelectionChange}
        onBlur={controller.handleBlur}
      />
    )
  }

  renderButtons() {
    const { controller } = this
    const { plugin, changed, mode } = this.state

    return (
      <DynamicPropertyPluginFormButtons
        onEdit={controller.handleEdit}
        onSave={controller.handleSave}
        onCancel={controller.handleCancel}
        plugin={plugin}
        changed={changed}
        mode={mode}
      />
    )
  }
}

export default _.flow(connect(), withStyles(styles))(DynamicPropertyPluginForm)
