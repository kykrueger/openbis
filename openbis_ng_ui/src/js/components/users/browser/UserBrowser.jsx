import React from 'react'
import Browser from '@src/js/components/common/browser/Browser.jsx'
import logger from '@src/js/common/logger.js'

import UserBrowserController from './UserBrowserController'

class UserBrowser extends React.Component {
  constructor(props) {
    super(props)
    this.controller = this.props.controller || new UserBrowserController()
  }

  render() {
    logger.log(logger.DEBUG, 'UserBrowser.render')
    return <Browser controller={this.controller} />
  }
}

export default UserBrowser
