import React from 'react'
import logger from '@src/js/common/logger.js'

import TypeFormWarning from './TypeFormWarning.jsx'

class TypeFormWarningGlobal extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'TypeFormWarningGlobal.render')

    return (
      <TypeFormWarning>
        This property is global (reusable among types).
      </TypeFormWarning>
    )
  }
}

export default TypeFormWarningGlobal
