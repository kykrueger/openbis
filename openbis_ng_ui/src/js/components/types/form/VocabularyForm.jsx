import React from 'react'
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
      loaded: false
    }
  }

  componentDidMount() {
    this.load().then(terms => {
      this.setState(() => ({
        terms,
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

    if (!this.state.loaded) {
      return null
    }

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
        data={this.state.terms}
      />
    )
  }
}
