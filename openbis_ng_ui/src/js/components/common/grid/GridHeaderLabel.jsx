import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TableCell from '@material-ui/core/TableCell'
import TableSortLabel from '@material-ui/core/TableSortLabel'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  cell: {
    padding: `${theme.spacing(1)}px ${theme.spacing(2)}px`,
    borderColor: theme.palette.border.secondary
  }
})

class GridHeaderLabel extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleSortChange = this.handleSortChange.bind(this)
  }

  handleSortChange() {
    const { column, onSortChange } = this.props
    if (onSortChange) {
      onSortChange(column)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'GridHeaderLabel.render')

    const { column, sort, sortDirection, classes } = this.props

    if (column.visible) {
      if (column.sort) {
        return (
          <TableCell classes={{ root: classes.cell }}>
            <TableSortLabel
              active={sort === column.field}
              direction={sortDirection}
              onClick={this.handleSortChange}
            >
              {column.label}
            </TableSortLabel>
          </TableCell>
        )
      } else {
        return (
          <TableCell classes={{ root: classes.cell }}>{column.label}</TableCell>
        )
      }
    } else {
      return null
    }
  }
}

export default withStyles(styles)(GridHeaderLabel)
