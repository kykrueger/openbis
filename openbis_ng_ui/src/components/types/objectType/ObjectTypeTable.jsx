import _ from 'lodash'
import React from 'react'
import Table from '@material-ui/core/Table'
import TableBody from '@material-ui/core/TableBody'
import TableHead from '@material-ui/core/TableHead'
import TableCell from '@material-ui/core/TableCell'
import TableRow from '@material-ui/core/TableRow'
import ObjectTypeTableRow from './ObjectTypeTableRow.jsx'
import {withStyles} from '@material-ui/core/styles'
import logger from '../../../common/logger.js'


const styles = () => ({
  table: {
    width: '100%'
  }
})

class ObjectTypeTable extends React.Component {

  render() {
    logger.log(logger.DEBUG, 'ObjectTypeTable.render')

    const { classes, properties } = this.props

    return (
      <Table className={classes.table}>
        <TableHead>
          <TableRow>
            <TableCell></TableCell>
            <TableCell>Code*</TableCell>
            <TableCell>Label*</TableCell>
            <TableCell>Description*</TableCell>
            <TableCell>Data Type*</TableCell>
            <TableCell>Mandatory</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {properties.map((property, index) => (
            <React.Fragment key={property.code}>
              <ObjectTypeTableRow
                index={index}
                property={property}
                onChange={this.props.onChange}
                onSelect={this.props.onSelect}
                onReorder={this.props.onReorder}
              />
            </React.Fragment>
          ))}
        </TableBody>
      </Table>)
  }

}

export default _.flow(
  withStyles(styles)
)(ObjectTypeTable)
