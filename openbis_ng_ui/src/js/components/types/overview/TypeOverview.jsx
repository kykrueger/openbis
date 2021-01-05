import React from 'react'
import logger from '@src/js/common/logger.js'

class TypeOverview extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'TypeOverview.render')

    const { objectType } = this.props

    return <div>Type overview {objectType}</div>
  }
}

export default TypeOverview
