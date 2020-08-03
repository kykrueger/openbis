import React from 'react'
import logger from '@src/js/common/logger.js'

class ContentObjectTab extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'ContentObjectTab.render')

    const { prefix, tab } = this.props

    return (prefix || '') + tab.object.id + (tab.changed ? '*' : '')
  }
}

export default ContentObjectTab
