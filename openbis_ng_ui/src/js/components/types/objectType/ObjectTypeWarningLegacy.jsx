import React from 'react'
import logger from '@src/js/common/logger.js'

import ObjectTypeWarning from './ObjectTypeWarning.jsx'

class ObjectTypeWarningLegacy extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'ObjectTypeWarningLegacy.render')

    return (
      <ObjectTypeWarning>
        This property is legacy (reusable among types).
      </ObjectTypeWarning>
    )
  }
}

export default ObjectTypeWarningLegacy
