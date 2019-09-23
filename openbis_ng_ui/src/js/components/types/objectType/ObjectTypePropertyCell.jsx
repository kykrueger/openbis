import _ from 'lodash'
import React from 'react'
import TableCell from '@material-ui/core/TableCell'
import {withStyles} from '@material-ui/core/styles'
import logger from '../../../common/logger.js'

const styles = () => ({
  cell: {
    paddingRight: '10px'
  }
})

class ObjectTypePropertyCell extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'ObjectTypePropertyCell.render')

    const { classes } = this.props

    return (
      <TableCell classes={{ root: classes.cell }}>
        {this.props.children}
      </TableCell>
    )
  }
}

export default _.flow(withStyles(styles))(ObjectTypePropertyCell)
