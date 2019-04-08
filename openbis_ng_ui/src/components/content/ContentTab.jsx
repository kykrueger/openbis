import React from 'react'
import logger from '../../common/logger.js'

class ContentTab extends React.Component {

  render() {
    logger.log(logger.DEBUG, 'ContentTab.render')

    return <div>Content of {this.props.object.id}</div>
  }

}

export default ContentTab
