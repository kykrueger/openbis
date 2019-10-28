import React from 'react'
import logger from '../../../common/logger.js'

class ObjectTypePreviewPropertyMetadata extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'ObjectTypePreviewPropertyMetadata.render')

    const { property } = this.props
    const { code } = property

    return <React.Fragment>[{code}]</React.Fragment>
  }
}

export default ObjectTypePreviewPropertyMetadata
