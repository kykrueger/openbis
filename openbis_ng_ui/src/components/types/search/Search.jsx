import _ from 'lodash'
import React from 'react'
import {withStyles} from '@material-ui/core/styles'
import Table from '@material-ui/core/Table'
import TableBody from '@material-ui/core/TableBody'
import TableCell from '@material-ui/core/TableCell'
import TableHead from '@material-ui/core/TableHead'
import TableFooter from '@material-ui/core/TableFooter'
import TableRow from '@material-ui/core/TableRow'
import TableSortLabel from '@material-ui/core/TableSortLabel'
import Pagination from '../../common/table/Pagination.jsx'
import {facade, dto} from '../../../services/openbis.js'
import logger from '../../../common/logger.js'

const styles = () => ({
})

class Search extends React.Component {

  constructor(props){
    super(props)
    this.state = {
      loaded: false,
    }
    this.handlePageChange = this.handlePageChange.bind(this)
    this.handlePageSizeChange = this.handlePageSizeChange.bind(this)
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

  render() {
    logger.log(logger.DEBUG, 'Search.render')

    if(!this.state.loaded){
      return <React.Fragment />
    }

    const { page, pageSize, allTypes } = this.state

    const types = allTypes.slice(page*pageSize, Math.min(allTypes.length, (page+1)*pageSize))

    return (
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>
              <TableSortLabel
                active={true}
                direction="asc"
              >
                  Code
              </TableSortLabel>
            </TableCell>
            <TableCell>
              Description
            </TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {types.map(type => (
            <TableRow key={type.permId.entityKind + '-' + type.permId.permId} hover>
              <TableCell>
                {type.code}
              </TableCell>
              <TableCell>
                {type.description}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
        <TableFooter>
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
  withStyles(styles)
)(Search)
