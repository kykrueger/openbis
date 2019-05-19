import _ from 'lodash'
import React from 'react'
import Typography from '@material-ui/core/Typography'
import ObjectTypeTable from './ObjectTypeTable.jsx'
import {withStyles} from '@material-ui/core/styles'
import logger from '../../../common/logger.js'

const styles = () => ({
})

class ObjectTypeForm extends React.Component {

  render() {
    logger.log(logger.DEBUG, 'ObjectTypeForm.render')

    let { objectType, propertyTypes } = this.props
    let { code, properties } = objectType

    return (
      <div>
        <Typography variant="h6">
          {code}
        </Typography>
        <form>
          {properties && properties.length > 0 &&
            <ObjectTypeTable
              objectType={objectType}
              propertyTypes={propertyTypes}
              onSelect={this.props.onSelect}
              onReorder={this.props.onReorder}
              onChange={this.props.onChange}
            />
          }
        </form>
      </div>
    )
  }

}

export default _.flow(
  withStyles(styles),
)(ObjectTypeForm)
