import React from 'react'
import logger from '@src/js/common/logger.js'

import TypeFormWarning from './TypeFormWarning.jsx'

class TypeFormWarningLegacy extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'TypeFormWarningLegacy.render')

    return (
      <TypeFormWarning>
        This property is legacy (reusable among types).
      </TypeFormWarning>
    )
  }
}

export default TypeFormWarningLegacy
