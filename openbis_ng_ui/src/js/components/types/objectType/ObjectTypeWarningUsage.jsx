import React from 'react'
import logger from '@src/js/common/logger.js'

import ObjectTypeWarning from './ObjectTypeWarning.jsx'

class ObjectTypeWarningUsage extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'ObjectTypeWarningUsage.render')

    const { subject, usages } = this.props

    return (
      <ObjectTypeWarning>
        This {subject} is already used by {usages}
        {usages > 1 ? ' entities' : ' entity'}.
      </ObjectTypeWarning>
    )
  }
}

export default ObjectTypeWarningUsage
