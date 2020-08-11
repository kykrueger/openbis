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

import ComponentContext from '@src/js/components/common/ComponentContext.js'
import FilterField from '@src/js/components/common/form/FilterField.jsx'
import selectors from '@src/js/store/selectors/selectors.js'
import logger from '@src/js/common/logger.js'

import GridController from './GridController.js'
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
    }
  },
  tableRowSelectable: {
    cursor: 'pointer'
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

    this.state = {}

    if (this.props.controller) {
      this.controller = this.props.controller
    } else {
      this.controller = new GridController()
    }

    this.controller.init(new ComponentContext(this))
  }

  componentDidMount() {
    this.controller.load()
  }

  componentDidUpdate(prevProps) {
    if (this.props.rows !== prevProps.rows) {
      this.controller.updateAllRows(this.props.rows)
    }
    if (this.props.selectedRowId !== prevProps.selectedRowId) {
      this.controller.updateSelectedRowId(this.props.selectedRowId)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'Grid.render')

    if (!this.state.loaded) {
      return <React.Fragment />
    }

    const { classes, onSelectedRowChange } = this.props
    const { page, pageSize, columns, currentRows, allRows } = this.state

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
            {currentRows.map(row => {
              return (
                <TableRow
                  key={row.id}
                  onClick={() => this.controller.handleRowSelect(row)}
                  hover={true}
                  selected={row.id === this.props.selectedRowId}
                  classes={{
                    root: onSelectedRowChange
                      ? classes.tableRowSelectable
                      : null
                  }}
                >
                  {columns.map(column => this.renderRowCell(column, row))}
                </TableRow>
              )
            })}
          </TableBody>
        </Table>
        <div className={classes.tableFooter}>
          <PageConfig
            count={allRows.length}
            page={page}
            pageSize={pageSize}
            onPageChange={this.controller.handlePageChange}
            onPageSizeChange={this.controller.handlePageSizeChange}
          />
          <ColumnConfig
            columns={columns}
            onVisibleChange={this.controller.handleColumnVisibleChange}
            onOrderChange={this.controller.handleColumnOrderChange}
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
              onClick={this.controller.handleSortChange(column)}
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
        this.controller.handleFilterChange(column.field, filter)
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
