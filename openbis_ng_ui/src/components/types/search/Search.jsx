import _ from 'lodash'
import React from 'react'
import {connect} from 'react-redux'
import {withStyles} from '@material-ui/core/styles'
import Link from '@material-ui/core/Link'
import Table from '@material-ui/core/Table'
import TableBody from '@material-ui/core/TableBody'
import TableCell from '@material-ui/core/TableCell'
import TableHead from '@material-ui/core/TableHead'
import TableRow from '@material-ui/core/TableRow'
import TableSortLabel from '@material-ui/core/TableSortLabel'
import FilterField from '../../common/form/FilterField.jsx'
import ColumnConfig from '../../common/table/ColumnConfig.jsx'
import PageConfig from '../../common/table/PageConfig.jsx'
import * as pages from '../../../common/consts/pages.js'
import * as objectTypes from '../../../common/consts/objectType.js'
import * as actions from '../../../store/actions/actions.js'
import {facade, dto} from '../../../services/openbis.js'
import logger from '../../../common/logger.js'

const styles = (theme) => ({
  container: {
    display: 'flex',
    flexDirection: 'column',
    height: '100%'
  },
  headerContainer: {
    flexGrow: 0,
    padding: theme.spacing.unit * 2,
    paddingBottom: 0
  },
  footerContainer: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'flex-end'
  },
  tableContainer: {
    flexGrow: 1,
    overflow: 'auto',
    paddingLeft: theme.spacing.unit * 2,
    paddingRight: theme.spacing.unit * 2
  },
  table: {
    height: '100%'
  },
  tableHeader: {
    '& th': {
      position: 'sticky',
      top: 0,
      zIndex: 10,
      backgroundColor: theme.palette.background.paper
    }
  },
  tableSpacer: {
    height: '100%',
    '& td': {
      border: 0
    }
  },
  tableLink: {
    fontSize: 'inherit'
  }
})

const entityKindToObjecType = {
  'SAMPLE': objectTypes.OBJECT_TYPE,
  'EXPERIMENT': objectTypes.COLLECTION_TYPE,
  'DATA_SET': objectTypes.DATA_SET_TYPE,
  'MATERIAL': objectTypes.MATERIAL_TYPE
}

const allColumns = ['kind', 'code', 'description']

function mapDispatchToProps(dispatch){
  return {
    objectOpen: (objectType, objectId) => { dispatch(actions.objectOpen(pages.TYPES, objectType, objectId)) }
  }
}

class Search extends React.Component {

  constructor(props){
    super(props)
    this.state = {
      loaded: false,
      filter: '',
      visibleColumns: allColumns
    }
    this.handleFilterChange = this.handleFilterChange.bind(this)
    this.handleColumnsChange = this.handleColumnsChange.bind(this)
    this.handlePageChange = this.handlePageChange.bind(this)
    this.handlePageSizeChange = this.handlePageSizeChange.bind(this)
    this.handleLinkClick = this.handleLinkClick.bind(this)
  }

  componentDidMount(){
    this.load()
  }

  load(){
    this.setState({
      loaded: false,
      columnConfigEl: null
    })

    Promise.all([
      this.searchObjectTypes(),
      this.searchCollectionTypes(),
      this.searchDataSetTypes(),
      this.searchMaterialTypes()
    ]).then(([objectTypes, collectionTypes, dataSetTypes, materialTypes]) => {
      let allTypes = [].concat(objectTypes, collectionTypes, dataSetTypes, materialTypes)

      allTypes = allTypes.map(type => ({
        ...type,
        kind: type.permId.entityKind
      }))

      this.setState(() => ({
        loaded: true,
        page: 0,
        pageSize: 10,
        allTypes
      }))
    })
  }

  searchObjectTypes(){
    let criteria = new dto.SampleTypeSearchCriteria()
    let fo = new dto.SampleTypeFetchOptions()

    criteria.withCode().thatContains(this.props.objectId)

    return facade.searchSampleTypes(criteria, fo).then(result => {
      return result.objects
    })
  }

  searchCollectionTypes(){
    let criteria = new dto.ExperimentTypeSearchCriteria()
    let fo = new dto.ExperimentTypeFetchOptions()

    criteria.withCode().thatContains(this.props.objectId)

    return facade.searchExperimentTypes(criteria, fo).then(result => {
      return result.objects
    })
  }

  searchDataSetTypes(){
    let criteria = new dto.DataSetTypeSearchCriteria()
    let fo = new dto.DataSetTypeFetchOptions()

    criteria.withCode().thatContains(this.props.objectId)

    return facade.searchDataSetTypes(criteria, fo).then(result => {
      return result.objects
    })
  }

  searchMaterialTypes(){
    let criteria = new dto.MaterialTypeSearchCriteria()
    let fo = new dto.MaterialTypeFetchOptions()

    criteria.withCode().thatContains(this.props.objectId)

    return facade.searchMaterialTypes(criteria, fo).then(result => {
      return result.objects
    })
  }

  handleFilterChange(filter){
    this.setState(() => ({
      page: 0,
      filter
    }))
  }

  handleColumnsChange(visibleColumns){
    this.setState(() => ({
      visibleColumns
    }))
  }

  handleSortChange(column){
    return () => {
      this.setState((prevState) => ({
        sort: column,
        sortDirection: prevState.sortDirection === 'asc' ? 'desc' : 'asc'
      }))
    }
  }

  handleLinkClick(permId){
    return () => {
      this.props.objectOpen(entityKindToObjecType[permId.entityKind], permId.permId)
    }
  }

  handlePageChange(page){
    this.setState(() => ({
      page
    }))
  }

  handlePageSizeChange(pageSize){
    this.setState(() => ({
      page: 0,
      pageSize
    }))
  }

  filter(types){
    const filter = this.state.filter ? this.state.filter.trim().toUpperCase() : null

    function matches(value){
      if(filter){
        return value ? value.trim().toUpperCase().includes(filter) : false
      }else{
        return true
      }
    }

    return _.filter(types, type => {
      return matches(type.kind) || matches(type.code) || matches(type.description)
    })
  }

  sort(types){
    const { sort, sortDirection } = this.state
    if(sort){
      return types.sort((t1, t2)=>{
        let sign = sortDirection === 'asc' ? 1 : -1
        let v1 = t1[sort] || ''
        let v2 = t2[sort] || ''
        return sign * v1.localeCompare(v2)
      })
    }else{
      return types
    }
  }

  page(types){
    const { page, pageSize } = this.state
    return types.slice(page*pageSize, Math.min(types.length, (page+1)*pageSize))
  }

  render() {
    logger.log(logger.DEBUG, 'Search.render')

    if(!this.state.loaded){
      return <React.Fragment />
    }

    const { classes } = this.props
    const { page, pageSize, filter, visibleColumns, sort, sortDirection, allTypes } = this.state

    const types = [...allTypes]
    const filteredTypes = this.filter(types)
    const sortedTypes = this.sort(filteredTypes)
    const pagedTypes = this.page(sortedTypes)

    return (
      <div className={classes.container}>
        <div className={classes.headerContainer}>
          <FilterField
            filter={filter}
            filterChange={this.handleFilterChange}
          />
        </div>
        <div className={classes.tableContainer}>
          <Table classes={{ root: classes.table }}>
            <TableHead classes={{ root: classes.tableHeader }}>
              <TableRow>
                {
                  visibleColumns.includes('kind') &&
                <TableCell>
                  <TableSortLabel
                    active={sort === 'kind'}
                    direction={sortDirection}
                    onClick={this.handleSortChange('kind')}
                  >
                Kind
                  </TableSortLabel>
                </TableCell>
                }
                {
                  visibleColumns.includes('code') &&
                  <TableCell>
                    <TableSortLabel
                      active={sort === 'code'}
                      direction={sortDirection}
                      onClick={this.handleSortChange('code')}
                    >
                  Code
                    </TableSortLabel>
                  </TableCell>
                }
                {
                  visibleColumns.includes('description') &&
                  <TableCell>
                    <TableSortLabel
                      active={sort === 'description'}
                      direction={sortDirection}
                      onClick={this.handleSortChange('description')}
                    >
                Description
                    </TableSortLabel>
                  </TableCell>
                }
              </TableRow>
            </TableHead>
            <TableBody>
              {pagedTypes.map(type => (
                <TableRow key={type.permId.entityKind + '-' + type.permId.permId} hover>
                  {
                    visibleColumns.includes('kind') &&
                  <TableCell>
                    {type.permId.entityKind}
                  </TableCell>
                  }
                  {
                    visibleColumns.includes('code') &&
                    <TableCell>
                      <Link
                        component="button"
                        classes={{ root: classes.tableLink }}
                        onClick={this.handleLinkClick(type.permId)}>{type.code}
                      </Link>
                    </TableCell>
                  }
                  {
                    visibleColumns.includes('description') &&
                    <TableCell>
                      {type.description}
                    </TableCell>
                  }
                </TableRow>
              ))}
              <TableRow classes={{ root: classes.tableSpacer }}>
                <TableCell></TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </div>
        <div className={classes.footerContainer}>
          <PageConfig
            count={filteredTypes.length}
            page={page}
            pageSize={pageSize}
            onPageChange={this.handlePageChange}
            onPageSizeChange={this.handlePageSizeChange}
          />
          <ColumnConfig
            allColumns={allColumns}
            visibleColumns={visibleColumns}
            onColumnsChange={this.handleColumnsChange}
          />
        </div>
      </div>
    )
  }

}

export default _.flow(
  connect(null, mapDispatchToProps),
  withStyles(styles)
)(Search)
