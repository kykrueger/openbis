import React from 'react'
import logger from '../../../common/logger.js'

class ObjectType extends React.Component {

  render() {
    logger.log(logger.DEBUG, 'ObjectType.render')
    return <div>ObjectType</div>
  }

}

export default ObjectType
