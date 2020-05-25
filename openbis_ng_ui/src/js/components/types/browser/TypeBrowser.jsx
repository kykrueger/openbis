import React from 'react'
import Browser from '@src/js/components/common/browser/Browser.jsx'
import logger from '@src/js/common/logger.js'

import TypeBrowserController from './TypeBrowserController'

class TypeBrowser extends React.Component {
  constructor(props) {
    super(props)
    this.controller = this.props.controller || new TypeBrowserController()
  }

  render() {
    logger.log(logger.DEBUG, 'TypeBrowser.render')

    return <Browser controller={this.controller} />
  }
}

export default TypeBrowser
