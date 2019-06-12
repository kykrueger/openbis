import React from 'react'
import Typography from '@material-ui/core/Typography'
import logger from '../../../common/logger.js'

class ObjectTypeTitle extends React.Component {

  render() {
    logger.log(logger.DEBUG, 'ObjectTypeHeader.render')

    const {objectType} = this.props

    return (
      <Typography variant="h6">
        {objectType.code}
      </Typography>
    )
  }

}

export default ObjectTypeTitle
