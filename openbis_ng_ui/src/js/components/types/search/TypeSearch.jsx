import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import GridContainer from '@src/js/components/common/grid/GridContainer.jsx'
import LinkToObject from '@src/js/components/common/form/LinkToObject.jsx'
import ids from '@src/js/common/consts/ids.js'
import pages from '@src/js/common/consts/pages.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import store from '@src/js/store/store.js'
import actions from '@src/js/store/actions/actions.js'
import openbis from '@src/js/services/openbis.js'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class TypeSearch extends React.Component {
  constructor(props) {
    super(props)

    this.state = {
      loaded: false
    }
  }

  componentDidMount() {
    this.load().then(types => {
      this.setState(() => ({
        types,
        loaded: true
      }))
    })
  }

  load() {
    return Promise.all([
      this.searchObjectTypes(),
      this.searchCollectionTypes(),
      this.searchDataSetTypes(),
      this.searchMaterialTypes(),
      this.searchVocabularyTypes()
    ])
      .then(
        ([
          objectTypes,
          collectionTypes,
          dataSetTypes,
          materialTypes,
          vocabularyTypes
        ]) => {
          let allTypes = [].concat(
            objectTypes,
            collectionTypes,
            dataSetTypes,
            materialTypes,
            vocabularyTypes
          )

          let query = this.props.objectId.toUpperCase()

          return allTypes.filter(
            type =>
              (type.code && type.code.toUpperCase().includes(query)) ||
              (type.description &&
                type.description.toUpperCase().includes(query))
          )
        }
      )
      .catch(error => {
        store.dispatch(actions.errorChange(error))
      })
  }

  searchObjectTypes() {
    let criteria = new openbis.SampleTypeSearchCriteria()
    let fo = new openbis.SampleTypeFetchOptions()
    return openbis.searchSampleTypes(criteria, fo).then(result => {
      return result.objects.map(object => {
        object.id = 'objectType-' + object.code
        object.kind = 'Object Type'
        object.object = { id: object.code, type: objectTypes.OBJECT_TYPE }
        return object
      })
    })
  }

  searchCollectionTypes() {
    let criteria = new openbis.ExperimentTypeSearchCriteria()
    let fo = new openbis.ExperimentTypeFetchOptions()
    return openbis.searchExperimentTypes(criteria, fo).then(result => {
      return result.objects.map(object => {
        object.id = 'collectionType-' + object.code
        object.kind = 'Collection Type'
        object.object = { id: object.code, type: objectTypes.COLLECTION_TYPE }
        return object
      })
    })
  }

  searchDataSetTypes() {
    let criteria = new openbis.DataSetTypeSearchCriteria()
    let fo = new openbis.DataSetTypeFetchOptions()
    return openbis.searchDataSetTypes(criteria, fo).then(result => {
      return result.objects.map(object => {
        object.id = 'dataSetType-' + object.code
        object.kind = 'Data Set Type'
        object.object = { id: object.code, type: objectTypes.DATA_SET_TYPE }
        return object
      })
    })
  }

  searchMaterialTypes() {
    let criteria = new openbis.MaterialTypeSearchCriteria()
    let fo = new openbis.MaterialTypeFetchOptions()
    return openbis.searchMaterialTypes(criteria, fo).then(result => {
      return result.objects.map(object => {
        object.id = 'materialType-' + object.code
        object.kind = 'Material Type'
        object.object = { id: object.code, type: objectTypes.MATERIAL_TYPE }
        return object
      })
    })
  }

  searchVocabularyTypes() {
    let criteria = new openbis.VocabularySearchCriteria()
    let fo = new openbis.VocabularyFetchOptions()
    return openbis.searchVocabularies(criteria, fo).then(result => {
      return result.objects.map(object => {
        object.id = 'vocabularyType-' + object.code
        object.kind = 'Vocabulary Type'
        object.object = { id: object.code, type: objectTypes.VOCABULARY_TYPE }
        return object
      })
    })
  }

  render() {
    logger.log(logger.DEBUG, 'Search.render')

    if (!this.state.loaded) {
      return null
    }

    const { types } = this.state

    return (
      <GridContainer>
        <Grid
          id={ids.TYPES_GRID_ID}
          columns={[
            {
              name: 'code',
              label: 'Code',
              sort: 'asc',
              getValue: ({ row }) => row.code,
              renderValue: ({ row }) => (
                <LinkToObject page={pages.TYPES} object={row.object}>
                  {row.code}
                </LinkToObject>
              )
            },
            {
              name: 'kind',
              label: 'Kind',
              getValue: ({ row }) => row.kind
            },
            {
              name: 'description',
              label: 'Description',
              getValue: ({ row }) => row.description
            }
          ]}
          rows={types}
        />
      </GridContainer>
    )
  }
}

export default _.flow(withStyles(styles))(TypeSearch)
