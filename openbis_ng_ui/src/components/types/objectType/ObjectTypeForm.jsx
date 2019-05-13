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

    let { code, properties } = this.props.object

    return (
      <div>
        <Typography variant="h6">
          {code}
        </Typography>
        <form>
          {properties && properties.length > 0 &&
            <ObjectTypeTable
              properties={properties}
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
