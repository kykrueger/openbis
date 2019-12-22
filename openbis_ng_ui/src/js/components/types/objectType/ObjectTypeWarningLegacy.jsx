import React from 'react'
import ObjectTypeWarning from './ObjectTypeWarning.jsx'
import logger from '../../../common/logger.js'

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
