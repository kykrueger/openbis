import React from 'react'
import logger from '@src/js/common/logger.js'

import TypeFormWarning from './TypeFormWarning.jsx'

class TypeFormWarningUsage extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'TypeFormWarningUsage.render')

    const { subject, usages } = this.props

    return (
      <TypeFormWarning>
        This {subject} is already used by {usages}
        {usages > 1 ? ' entities' : ' entity'}.
      </TypeFormWarning>
    )
  }
}

export default TypeFormWarningUsage
