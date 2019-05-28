import _ from 'lodash'
import React from 'react'
import Table from '@material-ui/core/Table'
import TableBody from '@material-ui/core/TableBody'
import TableHead from '@material-ui/core/TableHead'
import TableCell from '@material-ui/core/TableCell'
import TableRow from '@material-ui/core/TableRow'
import ObjectTypePropertyRow from './ObjectTypePropertyRow.jsx'
import {withStyles} from '@material-ui/core/styles'
import logger from '../../../common/logger.js'

const styles = () => ({
  table: {
    width: '100%'
  }
})

class ObjectTypeTable extends React.Component {

  constructor(props){
    super(props)
    this.state = {}
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectTypeTable.render')

    const { classes, objectType, propertyTypes } = this.props
    const { properties } = objectType

    return (
      <Table className={classes.table}>
        <TableHead>
          <TableRow>
            <TableCell></TableCell>
            <TableCell>Preview</TableCell>
            <TableCell>Property Type*</TableCell>
            <TableCell>Mandatory</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {properties.map((property, index) => (
            <React.Fragment key={property.id}>
              <ObjectTypePropertyRow
                index={index}
                property={property}
                propertyTypes={propertyTypes}
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
