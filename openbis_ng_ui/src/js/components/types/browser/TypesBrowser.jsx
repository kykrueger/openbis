import React from 'react'
import Browser from '@src/js/components/common/browser/Browser.jsx'
import logger from '@src/js/common/logger.js'

import TypesBrowserController from './TypesBrowserController'

class TypesBrowser extends React.Component {
  constructor(props) {
    super(props)
    this.controller = this.props.controller || new TypesBrowserController()
  }

  render() {
    logger.log(logger.DEBUG, 'TypesBrowser.render')

    return <Browser controller={this.controller} />
  }
}

export default TypesBrowser
