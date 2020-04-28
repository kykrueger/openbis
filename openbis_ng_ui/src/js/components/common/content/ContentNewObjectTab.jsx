import React from 'react'
import logger from '@src/js/common/logger.js'

class ContentObjectTab extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'ContentNewObjectTab.render')

    const { object, name } = this.props

    return name + ' ' + object.id
  }
}

export default ContentObjectTab
