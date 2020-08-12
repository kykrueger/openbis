import _ from 'lodash'
import React from 'react'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import Table from '@material-ui/core/Table'
import TableBody from '@material-ui/core/TableBody'

import ComponentContext from '@src/js/components/common/ComponentContext.js'
import selectors from '@src/js/store/selectors/selectors.js'
import logger from '@src/js/common/logger.js'

import GridController from './GridController.js'
import GridHeader from './GridHeader.jsx'
import GridRow from './GridRow.jsx'
import ColumnConfig from './ColumnConfig.jsx'
import PageConfig from './PageConfig.jsx'

const styles = theme => ({
  table: {
    borderCollapse: 'unset',
    marginTop: -theme.spacing(1)
  },
  tableBody: {
    '& tr:last-child td': {
      border: 0
    }
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

class Grid extends React.PureComponent {
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
    const {
      filters,
      sort,
      sortDirection,
      page,
      pageSize,
      columns,
      currentRows,
      allRows
    } = this.state

    return (
      <div>
        <Table classes={{ root: classes.table }}>
          <GridHeader
            columns={columns}
            filters={filters}
            sort={sort}
            sortDirection={sortDirection}
            onSortChange={this.controller.handleSortChange}
            onFilterChange={this.controller.handleFilterChange}
          />
          <TableBody classes={{ root: classes.tableBody }}>
            {currentRows.map(row => {
              return (
                <GridRow
                  key={row.id}
                  columns={columns}
                  row={row}
                  selected={row.id === this.props.selectedRowId}
                  selectable={onSelectedRowChange}
                  onClick={this.controller.handleRowSelect}
                />
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
}

export default _.flow(connect(mapStateToProps, null), withStyles(styles))(Grid)
