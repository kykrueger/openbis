import React from 'react'
import logger from '@src/js/common/logger.js'

class ContentObjectTab extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'ContentObjectTab.render')

    const { object, changed } = this.props

    return object.id + (changed ? '*' : '')
  }
}

export default ContentObjectTab
