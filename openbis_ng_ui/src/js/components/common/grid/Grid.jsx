import _ from 'lodash'
import React from 'react'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import Table from '@material-ui/core/Table'
import TableBody from '@material-ui/core/TableBody'
import TableCell from '@material-ui/core/TableCell'
import TableHead from '@material-ui/core/TableHead'
import TableRow from '@material-ui/core/TableRow'
import TableSortLabel from '@material-ui/core/TableSortLabel'

import FilterField from '@src/js/components/common/form/FilterField.jsx'
import selectors from '@src/js/store/selectors/selectors.js'
import openbis from '@src/js/services/openbis.js'
import ids from '@src/js/common/consts/ids.js'
import logger from '@src/js/common/logger.js'

import ColumnConfig from './ColumnConfig.jsx'
import PageConfig from './PageConfig.jsx'

const styles = theme => ({
  table: {
    borderCollapse: 'unset'
  },
  tableHeader: {
    '& th': {
      position: 'sticky',
      top: 0,
      zIndex: 10,
      fontWeight: 'bold',
      backgroundColor: theme.palette.background.primary
    }
  },
  tableBody: {
    '& tr:last-child td': {
      border: 0
    },
    '& tr': {
      cursor: 'pointer'
    }
  },
  tableLink: {
    fontSize: 'inherit'
  },
  tableCell: {
    padding: `${theme.spacing(1)}px ${theme.spacing(2)}px`,
    borderColor: theme.palette.border.secondary
  },
  tableFooter: {
    position: 'sticky',
    bottom: 0,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'flex-end',
    borderTopWidth: '1px',
    borderTopStyle: 'solid',
    borderTopColor: theme.palette.border.secondary,
    backgroundColor: theme.palette.background.paper
  }
})

function mapStateToProps(state) {
  return {
    session: selectors.getSession(state)
  }
}

class Grid extends React.Component {
  constructor(props) {
    super(props)

    const sortDefault = _.isFunction(props.rows) ? false : true

    let columns = props.columns.map(column => ({
      ...column,
      label: column.label || _.upperFirst(column.field),
      render: column.render || (row => _.get(row, column.field)),
      sort: column.sort === undefined ? sortDefault : Boolean(column.sort),
      visible: true
    }))

    this.state = {
      loaded: false,
      filters: this.props.filters || {},
      page: 0,
      pageSize: 10,
      columns: columns,
      columnConfigEl: null
    }

    this.handleFilterChange = this.handleFilterChange.bind(this)
    this.handleColumnVisibleChange = this.handleColumnVisibleChange.bind(this)
    this.handleColumnOrderChange = this.handleColumnOrderChange.bind(this)
    this.handlePageChange = this.handlePageChange.bind(this)
    this.handlePageSizeChange = this.handlePageSizeChange.bind(this)
  }

  componentDidMount() {
    this.load()
  }

  load() {
    this.loadSettings().then(() => {
      this.loadRows().then(() => {
        this.setState(() => ({
          loaded: true
        }))
      })
    })
  }

  loadRows() {
    if (_.isFunction(this.props.rows)) {
      let loadConfig = {
        filters: this.state.filters,
        page: this.state.page,
        pageSize: this.state.pageSize,
        sort: this.state.sort,
        sortDirection: this.state.sortDirection
      }
      return this.props.rows(loadConfig).then(({ rows, totalCount }) => {
        this.setState(() => ({
          rows,
          totalCount
        }))
      })
    } else if (!this.state.loaded) {
      this.setState(() => ({
        rows: this.props.rows
      }))
    }
    return Promise.resolve()
  }

  loadSettings() {
    let id = new openbis.PersonPermId(this.props.session.userName)
    let fo = new openbis.PersonFetchOptions()
    fo.withWebAppSettings(ids.WEB_APP_ID).withAllSettings()

    return openbis.getPersons([id], fo).then(map => {
      let person = map[id]
      let webAppSettings = person.webAppSettings[ids.WEB_APP_ID]
      if (webAppSettings && webAppSettings.settings) {
        let gridSettings = webAppSettings.settings[this.props.id]
        if (gridSettings) {
          let settings = JSON.parse(gridSettings.value)
          if (settings) {
            let newColumns = [...this.state.columns]
            newColumns.sort((c1, c2) => {
              let index1 = _.findIndex(settings.columns, ['field', c1.field])
              let index2 = _.findIndex(settings.columns, ['field', c2.field])
              return index1 - index2
            })
            newColumns = newColumns.map(column => {
              let setting = _.find(settings.columns, ['field', column.field])
              if (setting) {
                return {
                  ...column,
                  visible: setting.visible
                }
              } else {
                return column
              }
            })
            this.setState(() => ({
              ...settings,
              columns: newColumns
            }))
          }
        }
      }
    })
  }

  saveSettings() {
    let columns = this.state.columns.map(column => ({
      field: column.field,
      visible: column.visible
    }))

    let settings = {
      pageSize: this.state.pageSize,
      sort: this.state.sort,
      sortDirection: this.state.sortDirection,
      columns
    }

    let gridSettings = new openbis.WebAppSettingCreation()
    gridSettings.setName(this.props.id)
    gridSettings.setValue(JSON.stringify(settings))

    let update = new openbis.PersonUpdate()
    update.setUserId(new openbis.PersonPermId(this.props.session.userName))
    update.getWebAppSettings(ids.WEB_APP_ID).add(gridSettings)

    openbis.updatePersons([update])
  }

  handleFilterChange(column, filter) {
    let filters = {
      ...this.state.filters,
      [column]: filter
    }

    this.setState(
      () => ({
        page: 0,
        filters
      }),
      () => {
        this.loadRows()
      }
    )
  }

  handleColumnVisibleChange(field) {
    let columns = this.state.columns.map(column => {
      if (column.field === field) {
        return {
          ...column,
          visible: !column.visible
        }
      } else {
        return column
      }
    })

    this.setState(
      () => ({
        columns
      }),
      () => {
        this.saveSettings()
      }
    )
  }

  handleColumnOrderChange(sourceIndex, destinationIndex) {
    let columns = [...this.state.columns]
    let source = columns[sourceIndex]
    columns.splice(sourceIndex, 1)
    columns.splice(destinationIndex, 0, source)

    this.setState(
      () => ({
        columns
      }),
      () => {
        this.saveSettings()
      }
    )
  }

  handleSortChange(column) {
    if (!column.sort) {
      return
    }
    return () => {
      this.setState(
        prevState => ({
          sort: column.field,
          sortDirection: prevState.sortDirection === 'asc' ? 'desc' : 'asc'
        }),
        () => {
          this.saveSettings()
          this.loadRows()
        }
      )
    }
  }

  handlePageChange(page) {
    this.setState(
      () => ({
        page
      }),
      () => {
        this.loadRows()
      }
    )
  }

  handlePageSizeChange(pageSize) {
    this.setState(
      () => ({
        page: 0,
        pageSize
      }),
      () => {
        this.saveSettings()
        this.loadRows()
      }
    )
  }

  handleRowSelect(row) {
    const { onRowSelect, selectedRowId } = this.props
    if (onRowSelect) {
      onRowSelect(selectedRowId === row.id ? null : row)
    }
  }

  filter(rows) {
    function matches(value, filter) {
      if (filter) {
        return value
          ? value.trim().toUpperCase().includes(filter.trim().toUpperCase())
          : false
      } else {
        return true
      }
    }

    return _.filter(rows, row => {
      let matchesAll = true
      this.state.columns.forEach(column => {
        let value = _.get(row, column.field)
        let filter = this.state.filters[column.field]
        matchesAll = matchesAll && matches(value, filter)
      })
      return matchesAll
    })
  }

  sort(rows) {
    const { sort, sortDirection } = this.state

    if (sort) {
      let column = _.find(this.state.columns, ['field', sort])
      if (column) {
        return rows.sort((t1, t2) => {
          let sign = sortDirection === 'asc' ? 1 : -1
          let v1 = _.get(t1, column.field) || ''
          let v2 = _.get(t2, column.field) || ''
          return sign * v1.localeCompare(v2)
        })
      }
    }

    return rows
  }

  page(rows) {
    const { page, pageSize } = this.state
    return rows.slice(
      page * pageSize,
      Math.min(rows.length, (page + 1) * pageSize)
    )
  }

  render() {
    logger.log(logger.DEBUG, 'Grid.render')

    if (!this.state.loaded) {
      return <React.Fragment />
    }

    const { classes } = this.props
    const { page, pageSize, columns } = this.state

    let pagedRows = null
    let totalCount = null

    if (_.isFunction(this.props.rows)) {
      pagedRows = this.state.rows
      totalCount = this.state.totalCount
    } else {
      const filteredRows = this.filter([...this.state.rows])
      const sortedRows = this.sort(filteredRows)
      pagedRows = this.page(sortedRows)
      totalCount = filteredRows.length
    }

    return (
      <div>
        <Table classes={{ root: classes.table }}>
          <TableHead>
            <TableRow>
              {columns.map(column => this.renderFilterCell(column))}
            </TableRow>
            <TableRow classes={{ root: classes.tableHeader }}>
              {columns.map(column => this.renderHeaderCell(column))}
            </TableRow>
          </TableHead>
          <TableBody classes={{ root: classes.tableBody }}>
            {pagedRows.map(row => {
              return (
                <TableRow
                  key={row.id}
                  onClick={() => this.handleRowSelect(row)}
                  hover={true}
                  selected={row.id === this.props.selectedRowId}
                >
                  {columns.map(column => this.renderRowCell(column, row))}
                </TableRow>
              )
            })}
          </TableBody>
        </Table>
        <div className={classes.tableFooter}>
          <PageConfig
            count={totalCount}
            page={page}
            pageSize={pageSize}
            onPageChange={this.handlePageChange}
            onPageSizeChange={this.handlePageSizeChange}
          />
          <ColumnConfig
            columns={columns}
            onVisibleChange={this.handleColumnVisibleChange}
            onOrderChange={this.handleColumnOrderChange}
          />
        </div>
      </div>
    )
  }

  renderHeaderCell(column) {
    const { classes } = this.props
    const { sort, sortDirection } = this.state

    if (column.visible) {
      if (column.sort) {
        return (
          <TableCell key={column.field} classes={{ root: classes.tableCell }}>
            <TableSortLabel
              active={sort === column.field}
              direction={sortDirection}
              onClick={this.handleSortChange(column)}
            >
              {column.label}
            </TableSortLabel>
          </TableCell>
        )
      } else {
        return (
          <TableCell key={column.field} classes={{ root: classes.tableCell }}>
            {column.label}
          </TableCell>
        )
      }
    } else {
      return null
    }
  }

  renderFilterCell(column) {
    const { classes } = this.props
    const { filters } = this.state

    if (column.visible) {
      let filter = filters[column.field] || ''
      let filterChange = filter => {
        this.handleFilterChange(column.field, filter)
      }
      return (
        <TableCell key={column.field} classes={{ root: classes.tableCell }}>
          <FilterField filter={filter} filterChange={filterChange} />
        </TableCell>
      )
    } else {
      return null
    }
  }

  renderRowCell(column, row) {
    const { classes } = this.props

    if (column.visible) {
      let rendered = column.render(row)
      return (
        <TableCell key={column.field} classes={{ root: classes.tableCell }}>
          {rendered ? rendered : <span>&nbsp;</span>}
        </TableCell>
      )
    } else {
      return null
    }
  }
}

export default _.flow(connect(mapStateToProps, null), withStyles(styles))(Grid)
