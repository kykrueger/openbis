import _ from 'lodash'
import React from 'react'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import Link from '@material-ui/core/Link'
import Grid from '../../common/grid/Grid.jsx'
import * as ids from '../../../common/consts/ids.js'
import * as pages from '../../../common/consts/pages.js'
import * as objectTypes from '../../../common/consts/objectType.js'
import * as actions from '../../../store/actions/actions.js'
import { facade, dto } from '../../../services/openbis.js'
import logger from '../../../common/logger.js'

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

class Search extends React.Component {
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
    ]).then(([objectTypes, collectionTypes, dataSetTypes, materialTypes]) => {
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
            (type.description && type.description.toUpperCase().includes(query))
        )
        .map(type => ({
          ...type,
          id: type.permId.entityKind + '-' + type.permId.permId
        }))
    })
  }

  searchObjectTypes() {
    let criteria = new dto.SampleTypeSearchCriteria()
    let fo = new dto.SampleTypeFetchOptions()
    return facade.searchSampleTypes(criteria, fo).then(result => {
      return result.objects
    })
  }

  searchCollectionTypes() {
    let criteria = new dto.ExperimentTypeSearchCriteria()
    let fo = new dto.ExperimentTypeFetchOptions()
    return facade.searchExperimentTypes(criteria, fo).then(result => {
      return result.objects
    })
  }

  searchDataSetTypes() {
    let criteria = new dto.DataSetTypeSearchCriteria()
    let fo = new dto.DataSetTypeFetchOptions()
    return facade.searchDataSetTypes(criteria, fo).then(result => {
      return result.objects
    })
  }

  searchMaterialTypes() {
    let criteria = new dto.MaterialTypeSearchCriteria()
    let fo = new dto.MaterialTypeFetchOptions()
    return facade.searchMaterialTypes(criteria, fo).then(result => {
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
      <Grid
        id={ids.TYPES_GRID_ID}
        columns={[
          {
            field: 'permId.entityKind',
            label: 'Kind'
          },
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
            )
          },
          {
            field: 'description'
          }
        ]}
        data={types}
      />
    )
  }
}

export default _.flow(
  connect(
    null,
    mapDispatchToProps
  ),
  withStyles(styles)
)(Search)
