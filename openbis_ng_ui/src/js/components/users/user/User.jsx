import React from 'react'
import logger from '@src/js/common/logger.js'

class User extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'User.render')
    return <div>User</div>
  }
}

export default User
