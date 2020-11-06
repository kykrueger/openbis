import React from 'react'
import logger from '@src/js/common/logger.js'

export default class ToolBrowser extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'ToolBrowser.render')
    return 'ToolBrowser'
  }
}
