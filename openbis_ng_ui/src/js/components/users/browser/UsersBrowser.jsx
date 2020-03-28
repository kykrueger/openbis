import React from 'react'
import Browser from '@src/js/components/common/browser/Browser.jsx'
import logger from '@src/js/common/logger.js'

import UsersBrowserController from './UsersBrowserController'

class UsersBrowser extends React.Component {
  constructor(props) {
    super(props)
    this.controller = this.props.controller || new UsersBrowserController()
  }

  render() {
    logger.log(logger.DEBUG, 'UsersBrowser.render')
    return <Browser controller={this.controller} />
  }
}

export default UsersBrowser
