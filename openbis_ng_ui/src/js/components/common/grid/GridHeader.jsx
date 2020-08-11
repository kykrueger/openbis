import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TableCell from '@material-ui/core/TableCell'
import TableHead from '@material-ui/core/TableHead'
import TableRow from '@material-ui/core/TableRow'
import TableSortLabel from '@material-ui/core/TableSortLabel'

import FilterField from '@src/js/components/common/form/FilterField.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  header: {
    '& th': {
      position: 'sticky',
      top: 0,
      zIndex: 10,
      fontWeight: 'bold',
      backgroundColor: theme.palette.background.primary
    }
  },
  cell: {
    padding: `${theme.spacing(1)}px ${theme.spacing(2)}px`,
    borderColor: theme.palette.border.secondary
  }
})

class GridHeader extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'GridHeader.render')

    const { columns, classes } = this.props

    return (
      <TableHead>
        <TableRow>
          {columns.map(column => this.renderFilterCell(column))}
        </TableRow>
        <TableRow classes={{ root: classes.header }}>
          {columns.map(column => this.renderHeaderCell(column))}
        </TableRow>
      </TableHead>
    )
  }

  renderHeaderCell(column) {
    const { sort, sortDirection, onSortChange, classes } = this.props

    if (column.visible) {
      if (column.sort) {
        return (
          <TableCell key={column.field} classes={{ root: classes.cell }}>
            <TableSortLabel
              active={sort === column.field}
              direction={sortDirection}
              onClick={() => onSortChange(column)}
            >
              {column.label}
            </TableSortLabel>
          </TableCell>
        )
      } else {
        return (
          <TableCell key={column.field} classes={{ root: classes.cell }}>
            {column.label}
          </TableCell>
        )
      }
    } else {
      return null
    }
  }

  renderFilterCell(column) {
    const { filters, onFilterChange, classes } = this.props

    if (column.visible) {
      return (
        <TableCell key={column.field} classes={{ root: classes.cell }}>
          <FilterField
            filter={filters[column.field] || ''}
            filterChange={filter => onFilterChange(column.field, filter)}
          />
        </TableCell>
      )
    } else {
      return null
    }
  }
}

export default withStyles(styles)(GridHeader)
