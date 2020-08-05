import React from 'react'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import PageWithTwoPanels from '@src/js/components/common/page/PageWithTwoPanels.jsx'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import ids from '@src/js/common/consts/ids.js'
import logger from '@src/js/common/logger.js'

import VocabularyFormController from './VocabularyFormController.js'
import VocabularyFormFacade from './VocabularyFormFacade.js'
import VocabularyFormButtons from './VocabularyFormButtons.jsx'

export default class VocabularyForm extends React.PureComponent {
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
    const { vocabulary } = this.state
    return (
      <Grid
        id={ids.VOCABULARY_TERMS_GRID_ID}
        columns={[
          {
            field: 'code'
          },
          {
            field: 'label'
          },
          {
            field: 'description'
          },
          {
            field: 'official'
          }
        ]}
        data={vocabulary.terms}
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
