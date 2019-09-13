import React from 'react'
import logger from '../../../common/logger.js'

class Group extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'Group.render')
    return <div>Group</div>
  }
}

export default Group
