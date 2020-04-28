import React from 'react'
import logger from '@src/js/common/logger.js'

class ContentSearchTab extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'ContentSearchTab.render')

    const { object } = this.props

    return 'search: ' + object.id
  }
}

export default ContentSearchTab
