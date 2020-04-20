import React from 'react'
import logger from '@src/js/common/logger.js'

class MaterialType extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'MaterialType.render')
    return <div>MaterialType</div>
  }
}

export default MaterialType
