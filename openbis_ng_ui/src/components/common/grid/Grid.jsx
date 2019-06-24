import _ from 'lodash'
import React from 'react'
import {withStyles} from '@material-ui/core/styles'
import Table from '@material-ui/core/Table'
import TableBody from '@material-ui/core/TableBody'
import TableCell from '@material-ui/core/TableCell'
import TableHead from '@material-ui/core/TableHead'
import TableRow from '@material-ui/core/TableRow'
import TableSortLabel from '@material-ui/core/TableSortLabel'
import FilterField from '../../common/form/FilterField.jsx'
import ColumnConfig from '../../common/grid/ColumnConfig.jsx'
import PageConfig from '../../common/grid/PageConfig.jsx'
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

class Grid extends React.Component {

  constructor(props){
    super(props)

    this.columnsArray = props.columns.map(column => ({
      ...column,
      label: column.label || _.upperFirst(column.field),
      render: column.render || (row => _.get(row, column.field)),
    }))
    this.columnsMap = _.keyBy(props.columns, 'field')

    this.state = {
      loaded: false,
      filter: '',
      visibleColumns: Object.keys(this.columnsMap)
    }

    this.handleFilterChange = this.handleFilterChange.bind(this)
    this.handleColumnsChange = this.handleColumnsChange.bind(this)
    this.handlePageChange = this.handlePageChange.bind(this)
    this.handlePageSizeChange = this.handlePageSizeChange.bind(this)
  }

  componentDidMount(){
    this.load()
  }

  load(){
    this.setState({
      loaded: false,
      columnConfigEl: null
    })

    let loader = null

    if(_.isFunction(this.props.rows)){
      loader = this.props.rows
    }else{
      loader = function(){
        return new Promise(resolve => {
          resolve(this.props.rows)
        })
      }
    }

    loader().then(rows => {
      this.setState(() => ({
        loaded: true,
        page: 0,
        pageSize: 10,
        rows
      }))
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

  filter(rows){
    const filter = this.state.filter ? this.state.filter.trim().toUpperCase() : null

    function matches(value){
      if(filter){
        return value ? value.trim().toUpperCase().includes(filter) : false
      }else{
        return true
      }
    }

    return _.filter(rows, row => {
      return this.state.visibleColumns.some(visibleColumn => {
        let column = this.columnsMap[visibleColumn]
        let value = _.get(row, column.field)
        return matches(value)
      })
    })
  }

  sort(rows){
    const { sort, sortDirection } = this.state
    if(sort){
      return rows.sort((t1, t2)=>{
        let sign = sortDirection === 'asc' ? 1 : -1
        let column = this.columnsMap[sort]
        let v1 = _.get(t1, column.field) || ''
        let v2 = _.get(t2, column.field) || ''
        return sign * v1.localeCompare(v2)
      })
    }else{
      return rows
    }
  }

  page(rows){
    const { page, pageSize } = this.state
    return rows.slice(page*pageSize, Math.min(rows.length, (page+1)*pageSize))
  }

  render() {
    logger.log(logger.DEBUG, 'Grid.render')

    if(!this.state.loaded){
      return <React.Fragment />
    }

    const { classes } = this.props
    const { page, pageSize, filter, visibleColumns, sort, sortDirection, rows } = this.state

    const filteredRows = this.filter([...rows])
    const sortedRows = this.sort(filteredRows)
    const pagedRows = this.page(sortedRows)

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
                {this.columnsArray.map(column => {
                  if(visibleColumns.includes(column.field)){
                    return (
                      <TableCell key={column.field}>
                        <TableSortLabel
                          active={sort === column.field}
                          direction={sortDirection}
                          onClick={this.handleSortChange(column.field)}
                        >
                          {column.label}
                        </TableSortLabel>
                      </TableCell>
                    )
                  }else{
                    return null
                  }
                })}
              </TableRow>
            </TableHead>
            <TableBody>
              {pagedRows.map(row => {
                return (
                  <TableRow key={row.id} hover>
                    {this.columnsArray.map(column => {
                      if(visibleColumns.includes(column.field)){
                        return (
                          <TableCell key={column.field}>
                            {column.render(row)}
                          </TableCell>
                        )
                      }else{
                        return null
                      }
                    })}
                  </TableRow>
                )
              })}
              <TableRow classes={{ root: classes.tableSpacer }}>
                <TableCell></TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </div>
        <div className={classes.footerContainer}>
          <PageConfig
            count={filteredRows.length}
            page={page}
            pageSize={pageSize}
            onPageChange={this.handlePageChange}
            onPageSizeChange={this.handlePageSizeChange}
          />
          <ColumnConfig
            allColumns={this.columnsArray}
            visibleColumns={visibleColumns}
            onColumnsChange={this.handleColumnsChange}
          />
        </div>
      </div>
    )
  }

}

export default _.flow(
  withStyles(styles)
)(Grid)
