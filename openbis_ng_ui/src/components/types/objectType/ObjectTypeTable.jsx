import _ from 'lodash'
import React from 'react'
import Table from '@material-ui/core/Table'
import TableBody from '@material-ui/core/TableBody'
import TableHead from '@material-ui/core/TableHead'
import TableRow from '@material-ui/core/TableRow'
import ObjectTypePropertyCell from './ObjectTypePropertyCell.jsx'
import ObjectTypePropertyRow from './ObjectTypePropertyRow.jsx'
import { withStyles } from '@material-ui/core/styles'
import logger from '../../../common/logger.js'

const styles = () => ({
  table: {
    width: '100%'
  },
  propertyType: {
    minWidth: '30em'
  }
})

class ObjectTypeTable extends React.Component {
  constructor(props) {
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
            <ObjectTypePropertyCell></ObjectTypePropertyCell>
            <ObjectTypePropertyCell>Preview</ObjectTypePropertyCell>
            <ObjectTypePropertyCell>
              <div className={classes.propertyType}>Property Type*</div>
            </ObjectTypePropertyCell>
            <ObjectTypePropertyCell>Mandatory</ObjectTypePropertyCell>
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
      </Table>
    )
  }
}

export default _.flow(withStyles(styles))(ObjectTypeTable)
