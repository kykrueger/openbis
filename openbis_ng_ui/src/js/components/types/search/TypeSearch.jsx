import _ from 'lodash'
import React from 'react'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import Link from '@material-ui/core/Link'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import GridContainer from '@src/js/components/common/grid/GridContainer.jsx'
import ids from '@src/js/common/consts/ids.js'
import pages from '@src/js/common/consts/pages.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import store from '@src/js/store/store.js'
import actions from '@src/js/store/actions/actions.js'
import openbis from '@src/js/services/openbis.js'
import logger from '@src/js/common/logger.js'

const styles = () => ({
  tableLink: {
    fontSize: 'inherit'
  }
})

function mapDispatchToProps(dispatch) {
  return {
    objectOpen: (objectType, objectId) => {
      dispatch(actions.objectOpen(pages.TYPES, objectType, objectId))
    }
  }
}

class TypeSearch extends React.Component {
  constructor(props) {
    super(props)

    this.state = {
      loaded: false
    }

    this.handleLinkClick = this.handleLinkClick.bind(this)
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
      this.searchMaterialTypes()
    ])
      .then(([objectTypes, collectionTypes, dataSetTypes, materialTypes]) => {
        let allTypes = [].concat(
          objectTypes,
          collectionTypes,
          dataSetTypes,
          materialTypes
        )

        let query = this.props.objectId.toUpperCase()

        return allTypes
          .filter(
            type =>
              (type.code && type.code.toUpperCase().includes(query)) ||
              (type.description &&
                type.description.toUpperCase().includes(query))
          )
          .map(type => ({
            ...type,
            id: type.permId.entityKind + '-' + type.permId.permId
          }))
      })
      .catch(error => {
        store.dispatch(actions.errorChange(error))
      })
  }

  searchObjectTypes() {
    let criteria = new openbis.SampleTypeSearchCriteria()
    let fo = new openbis.SampleTypeFetchOptions()
    return openbis.searchSampleTypes(criteria, fo).then(result => {
      return result.objects
    })
  }

  searchCollectionTypes() {
    let criteria = new openbis.ExperimentTypeSearchCriteria()
    let fo = new openbis.ExperimentTypeFetchOptions()
    return openbis.searchExperimentTypes(criteria, fo).then(result => {
      return result.objects
    })
  }

  searchDataSetTypes() {
    let criteria = new openbis.DataSetTypeSearchCriteria()
    let fo = new openbis.DataSetTypeFetchOptions()
    return openbis.searchDataSetTypes(criteria, fo).then(result => {
      return result.objects
    })
  }

  searchMaterialTypes() {
    let criteria = new openbis.MaterialTypeSearchCriteria()
    let fo = new openbis.MaterialTypeFetchOptions()
    return openbis.searchMaterialTypes(criteria, fo).then(result => {
      return result.objects
    })
  }

  handleLinkClick(permId) {
    const entityKindToObjecType = {
      SAMPLE: objectTypes.OBJECT_TYPE,
      EXPERIMENT: objectTypes.COLLECTION_TYPE,
      DATA_SET: objectTypes.DATA_SET_TYPE,
      MATERIAL: objectTypes.MATERIAL_TYPE
    }
    return () => {
      this.props.objectOpen(
        entityKindToObjecType[permId.entityKind],
        permId.permId
      )
    }
  }

  render() {
    logger.log(logger.DEBUG, 'Search.render')

    if (!this.state.loaded) {
      return null
    }

    const { classes } = this.props
    const { types } = this.state

    return (
      <GridContainer>
        <Grid
          id={ids.TYPES_GRID_ID}
          columns={[
            {
              field: 'code',
              render: row => (
                <Link
                  component='button'
                  classes={{ root: classes.tableLink }}
                  onClick={this.handleLinkClick(row.permId)}
                >
                  {row.code}
                </Link>
              ),
              sort: 'asc'
            },
            {
              field: 'permId.entityKind',
              label: 'Kind'
            },
            {
              field: 'description'
            }
          ]}
          rows={types}
        />
      </GridContainer>
    )
  }
}

export default _.flow(
  connect(null, mapDispatchToProps),
  withStyles(styles)
)(TypeSearch)
