import _ from 'lodash'
import React from 'react'
import {connect} from 'react-redux'
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
import * as ids from '../../../common/consts/ids.js'
import * as selectors from '../../../store/selectors/selectors.js'
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

function mapStateToProps(state){
  return {
    session: selectors.getSession(state)
  }
}

class Grid extends React.Component {

  constructor(props){
    super(props)

    const sortDefault = _.isFunction(props.data) ? false : true

    this.columnsArray = props.columns.map(column => ({
      ...column,
      label: column.label || _.upperFirst(column.field),
      render: column.render || (row => _.get(row, column.field)),
      sort: column.sort === undefined ? sortDefault : Boolean(column.sort)
    }))
    this.columnsMap = _.keyBy(props.columns, 'field')

    this.state = {
      loaded: false,
      filter: '',
      page: 0,
      pageSize: 10,
      visibleColumns: Object.keys(this.columnsMap),
      columnConfigEl: null
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
    this.loadSettings().then(() => {
      this.loadData().then(() => {
        this.setState(() => ({
          loaded: true
        }))
      })
    })
  }

  loadData(){
    if(_.isFunction(this.props.data)){
      let loadConfig = {
        filter: this.state.filter,
        page: this.state.page,
        pageSize: this.state.pageSize,
        sort: this.state.sort,
        sortDirection: this.state.sortDirection
      }
      return this.props.data(loadConfig).then(({ objects, totalCount }) => {
        this.setState(() => ({
          objects,
          totalCount
        }))
      })
    }else if(!this.state.loaded){
      this.setState(() => ({
        objects: this.props.data
      }))
    }
    return Promise.resolve()
  }

  loadSettings(){
    let id = new dto.PersonPermId(this.props.session.userName)
    let fo = new dto.PersonFetchOptions()
    fo.withWebAppSettings(ids.WEB_APP_ID).withAllSettings()

    return facade.getPersons([id], fo).then(map => {
      let person = map[id]
      let webAppSettings = person.webAppSettings[ids.WEB_APP_ID]
      if(webAppSettings && webAppSettings.settings){
        let gridSettings = webAppSettings.settings[this.props.id]
        if(gridSettings){
          let settings = JSON.parse(gridSettings.value)
          if(settings){
            this.setState(() => ({
              ...settings
            }))
          }
        }
      }
    })
  }

  saveSettings(){
    let settings = {
      pageSize: this.state.pageSize,
      sort: this.state.sort,
      sortDirection: this.state.sortDirection,
      visibleColumns: this.state.visibleColumns
    }

    let gridSettings = new dto.WebAppSettingCreation()
    gridSettings.setName(this.props.id)
    gridSettings.setValue(JSON.stringify(settings))

    let update = new dto.PersonUpdate()
    update.setUserId(new dto.PersonPermId(this.props.session.userName))
    update.getWebAppSettings(ids.WEB_APP_ID).add(gridSettings)

    facade.updatePersons([update])
  }

  handleFilterChange(filter){
    this.setState(() => ({
      page: 0,
      filter
    }), () => {
      this.load()
    })
  }

  handleColumnsChange(visibleColumns){
    this.setState(() => ({
      visibleColumns
    }), () => {
      this.saveSettings()
    })
  }

  handleSortChange(column){
    if(!column.sort){
      return
    }
    return () => {
      this.setState((prevState) => ({
        sort: column.field,
        sortDirection: prevState.sortDirection === 'asc' ? 'desc' : 'asc'
      }), () => {
        this.saveSettings()
        this.load()
      })
    }
  }

  handlePageChange(page){
    this.setState(() => ({
      page
    }), () => {
      this.load()
    })
  }

  handlePageSizeChange(pageSize){
    this.setState(() => ({
      page: 0,
      pageSize
    }), () => {
      this.saveSettings()
      this.load()
    })
  }

  filter(objects){
    const filter = this.state.filter ? this.state.filter.trim().toUpperCase() : null

    function matches(value){
      if(filter){
        return value ? value.trim().toUpperCase().includes(filter) : false
      }else{
        return true
      }
    }

    return _.filter(objects, row => {
      return this.state.visibleColumns.some(visibleColumn => {
        let column = this.columnsMap[visibleColumn]
        let value = _.get(row, column.field)
        return matches(value)
      })
    })
  }

  sort(objects){
    const { sort, sortDirection } = this.state

    if(sort){
      return objects.sort((t1, t2)=>{
        let sign = sortDirection === 'asc' ? 1 : -1
        let column = this.columnsMap[sort]
        let v1 = _.get(t1, column.field) || ''
        let v2 = _.get(t2, column.field) || ''
        return sign * v1.localeCompare(v2)
      })
    }else{
      return objects
    }
  }

  page(objects){
    const { page, pageSize } = this.state
    return objects.slice(page*pageSize, Math.min(objects.length, (page+1)*pageSize))
  }

  render() {
    logger.log(logger.DEBUG, 'Grid.render')

    if(!this.state.loaded){
      return <React.Fragment />
    }

    const { classes } = this.props
    const { page, pageSize, filter, visibleColumns, sort, sortDirection } = this.state

    let pagedObjects = null
    let totalCount = null

    if(_.isFunction(this.props.data)){
      pagedObjects = this.state.objects
      totalCount = this.state.totalCount
    }else{
      const filteredObjects = this.filter([...this.state.objects])
      const sortedObjects = this.sort(filteredObjects)
      pagedObjects = this.page(sortedObjects)
      totalCount = filteredObjects.length
    }

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
                          active={column.sort && sort === column.field}
                          direction={sortDirection}
                          onClick={this.handleSortChange(column)}
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
              {pagedObjects.map(row => {
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
            count={totalCount}
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
  connect(mapStateToProps, null),
  withStyles(styles)
)(Grid)
