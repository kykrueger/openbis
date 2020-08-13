import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import PageWithTwoPanels from '@src/js/components/common/page/PageWithTwoPanels.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import GridContainer from '@src/js/components/common/grid/GridContainer.jsx'
import ids from '@src/js/common/consts/ids.js'
import logger from '@src/js/common/logger.js'

import VocabularyFormController from './VocabularyFormController.js'
import VocabularyFormFacade from './VocabularyFormFacade.js'
import VocabularyFormParameters from './VocabularyFormParameters.jsx'
import VocabularyFormButtons from './VocabularyFormButtons.jsx'

const styles = () => ({})

class VocabularyForm extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)

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

  handleSelectedRowChange(row) {
    const { controller } = this
    if (row) {
      controller.handleSelectionChange('term', { id: row.id })
      this.setState({
        selectedRow: row
      })
    } else {
      controller.handleSelectionChange()
      this.setState({
        selectedRow: null
      })
    }
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
    const { controller } = this
    const { terms, selection } = this.state

    return (
      <GridContainer>
        <Header>Terms</Header>
        <Grid
          id={ids.VOCABULARY_TERMS_GRID_ID}
          controller={controller.gridController}
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
          rows={terms}
          selectedRowId={
            selection && selection.type === 'term' ? selection.params.id : null
          }
          onSelectedRowChange={this.handleSelectedRowChange}
        />
      </GridContainer>
    )
  }

  renderAdditionalPanel() {
    const { controller } = this
    const { vocabulary, terms, selection, selectedRow, mode } = this.state

    return (
      <VocabularyFormParameters
        controller={controller}
        vocabulary={vocabulary}
        terms={terms}
        selection={selection}
        selectedRow={selectedRow}
        mode={mode}
        onChange={controller.handleChange}
        onSelectionChange={controller.handleSelectionChange}
        onBlur={controller.handleBlur}
      />
    )
  }

  renderButtons() {
    const { controller } = this
    const { vocabulary, selection, changed, mode } = this.state

    return (
      <VocabularyFormButtons
        onEdit={controller.handleEdit}
        onSave={controller.handleSave}
        onCancel={controller.handleCancel}
        onAdd={controller.handleAdd}
        onRemove={controller.handleRemove}
        vocabulary={vocabulary}
        selection={selection}
        changed={changed}
        mode={mode}
      />
    )
  }
}

export default _.flow(connect(), withStyles(styles))(VocabularyForm)
