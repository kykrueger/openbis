import React from 'react'
import logger from '@src/js/common/logger.js'

class UserForm extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'UserForm.render')
    return <div>User</div>
  }
}

export default UserForm
