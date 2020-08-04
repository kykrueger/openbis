import React from 'react'
import PageWithTwoPanels from '@src/js/components/common/page/PageWithTwoPanels.jsx'
import PageButtons from '@src/js/components/common/page/PageButtons.jsx'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import ids from '@src/js/common/consts/ids.js'
import store from '@src/js/store/store.js'
import actions from '@src/js/store/actions/actions.js'
import openbis from '@src/js/services/openbis.js'
import logger from '@src/js/common/logger.js'

export default class VocabularyForm extends React.PureComponent {
  constructor(props) {
    super(props)

    this.state = {
      loading: true,
      loaded: false,
      mode: 'view'
    }
  }

  componentDidMount() {
    this.load().then(terms => {
      this.setState(() => ({
        terms,
        loading: false,
        loaded: true
      }))
    })
  }

  load() {
    const { id } = this.props.object

    const criteria = new openbis.VocabularyTermSearchCriteria()
    const fo = new openbis.VocabularyTermFetchOptions()

    criteria.withAndOperator()
    criteria.withVocabulary().withCode().thatEquals(id)

    return openbis
      .searchVocabularyTerms(criteria, fo)
      .then(result => {
        return result.objects.map(term => ({
          ...term,
          id: term.code
        }))
      })
      .catch(error => {
        store.dispatch(actions.errorChange(error))
      })
  }

  render() {
    logger.log(logger.DEBUG, 'VocabularyForm.render')

    const { loading, loaded, terms } = this.state

    return (
      <PageWithTwoPanels
        loading={loading}
        loaded={loaded}
        object={terms}
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
        data={terms}
      />
    )
  }

  renderAdditionalPanel() {
    return <div>Additional panel</div>
  }

  renderButtons() {
    const { mode } = this.state
    return (
      <PageButtons
        mode={mode}
        onEdit={() => {}}
        onSave={() => {}}
        onCancel={() => {}}
      />
    )
  }
}
