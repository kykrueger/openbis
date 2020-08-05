import _ from 'lodash'
import React from 'react'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import PageWithTwoPanels from '@src/js/components/common/page/PageWithTwoPanels.jsx'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import ids from '@src/js/common/consts/ids.js'
import logger from '@src/js/common/logger.js'

import VocabularyFormController from './VocabularyFormController.js'
import VocabularyFormFacade from './VocabularyFormFacade.js'
import VocabularyFormButtons from './VocabularyFormButtons.jsx'

const styles = () => ({})

class VocabularyForm extends React.PureComponent {
  constructor(props) {
    super(props)

    this.state = {}

    if (this.props.controller) {
      this.controller = this.props.controller
    } else {
      this.controller = new VocabularyFormController(new VocabularyFormFacade())
    }

    this.controller.init(new ComponentContext(this))
  }
  componentDidMount() {
    this.controller.load()
  }

  render() {
    logger.log(logger.DEBUG, 'VocabularyForm.render')

    const { loading, loaded, vocabulary } = this.state

    return (
      <PageWithTwoPanels
        loading={loading}
        loaded={loaded}
        object={vocabulary}
        renderMainPanel={() => this.renderMainPanel()}
        renderAdditionalPanel={() => this.renderAdditionalPanel()}
        renderButtons={() => this.renderButtons()}
      />
    )
  }

  renderMainPanel() {
    const { terms } = this.state
    return (
      <Grid
        id={ids.VOCABULARY_TERMS_GRID_ID}
        columns={[
          {
            field: 'code.value',
            label: 'Code'
          },
          {
            field: 'label.value',
            label: 'Label'
          },
          {
            field: 'description.value',
            label: 'Description'
          },
          {
            field: 'official.value',
            label: 'Official'
          }
        ]}
        data={terms}
      />
    )
  }

  renderAdditionalPanel() {
    return <div>Additional panel</div>
  }

  renderButtons() {
    const { controller } = this
    const { object } = this.props
    const { selection, changed, mode } = this.state

    return (
      <VocabularyFormButtons
        onEdit={controller.handleEdit}
        onSave={() => {}}
        onCancel={controller.handleCancel}
        object={object}
        selection={selection}
        changed={changed}
        mode={mode}
      />
    )
  }
}

export default _.flow(connect(), withStyles(styles))(VocabularyForm)
