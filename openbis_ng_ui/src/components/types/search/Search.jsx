import _ from 'lodash'
import React from 'react'
import {connect} from 'react-redux'
import {withStyles} from '@material-ui/core/styles'
import Link from '@material-ui/core/Link'
import Table from '@material-ui/core/Table'
import TableBody from '@material-ui/core/TableBody'
import TableCell from '@material-ui/core/TableCell'
import TableHead from '@material-ui/core/TableHead'
import TableFooter from '@material-ui/core/TableFooter'
import TableRow from '@material-ui/core/TableRow'
import TableSortLabel from '@material-ui/core/TableSortLabel'
import Pagination from '../../common/table/Pagination.jsx'
import * as pages from '../../../common/consts/pages.js'
import * as objectTypes from '../../../common/consts/objectType.js'
import * as actions from '../../../store/actions/actions.js'
import {facade, dto} from '../../../services/openbis.js'
import logger from '../../../common/logger.js'

const styles = (theme) => ({
  table: {
    height: '100%'
  },
  header: {
    '& th': {
      position: 'sticky',
      top: 0,
      backgroundColor: theme.palette.background.paper
    }
  },
  spacer: {
    height: '100%',
    '& td': {
      border: 0
    }
  },
  footer: {
    '& td': {
      position: 'sticky',
      bottom: 0,
      backgroundColor: theme.palette.background.paper
    }
  },
  link: {
    fontSize: 'inherit'
  }
})

const entityKindToObjecType = {
  'SAMPLE': objectTypes.OBJECT_TYPE,
  'EXPERIMENT': objectTypes.COLLECTION_TYPE,
  'DATA_SET': objectTypes.DATA_SET_TYPE,
  'MATERIAL': objectTypes.MATERIAL_TYPE
}

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
    }
    this.handlePageChange = this.handlePageChange.bind(this)
    this.handlePageSizeChange = this.handlePageSizeChange.bind(this)
    this.handleLinkClick = this.handleLinkClick.bind(this)
  }

  componentDidMount(){
    this.load()
  }

  load(){
    this.setState({
      loaded: false
    })

    Promise.all([
      this.searchObjectTypes(),
      this.searchCollectionTypes(),
      this.searchDataSetTypes(),
      this.searchMaterialTypes()
    ]).then(([objectTypes, collectionTypes, dataSetTypes, materialTypes]) => {
      this.setState(() => ({
        loaded: true,
        page: 0,
        pageSize: 10,
        allTypes: [].concat(objectTypes, collectionTypes, dataSetTypes, materialTypes)
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
    const { page, pageSize, sort, sortDirection, allTypes } = this.state

    let types = [...allTypes]
    types = this.sort(types)
    types = this.page(types)

    return (
      <Table classes={{ root: classes.table }}>
        <TableHead classes={{ root: classes.header }}>
          <TableRow>
            <TableCell>
              <TableSortLabel
                active={sort === 'code'}
                direction={sortDirection}
                onClick={this.handleSortChange('code')}
              >
                  Code
              </TableSortLabel>
            </TableCell>
            <TableCell>
              <TableSortLabel
                active={sort === 'description'}
                direction={sortDirection}
                onClick={this.handleSortChange('description')}
              >
                Description
              </TableSortLabel>
            </TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {types.map(type => (
            <TableRow key={type.permId.entityKind + '-' + type.permId.permId} hover>
              <TableCell>
                <Link
                  component="button"
                  classes={{ root: classes.link }}
                  onClick={this.handleLinkClick(type.permId)}>{type.code}
                </Link>
              </TableCell>
              <TableCell>
                {type.description}
              </TableCell>
            </TableRow>
          ))}
          <TableRow classes={{ root: classes.spacer }}>
            <TableCell></TableCell>
          </TableRow>
        </TableBody>
        <TableFooter classes={{ root: classes.footer }}>
          <TableRow>
            <Pagination
              count={allTypes.length}
              page={page}
              pageSize={pageSize}
              onPageChange={this.handlePageChange}
              onPageSizeChange={this.handlePageSizeChange}
            />
          </TableRow>
        </TableFooter>
      </Table>
    )
  }

}

export default _.flow(
  connect(null, mapDispatchToProps),
  withStyles(styles)
)(Search)
