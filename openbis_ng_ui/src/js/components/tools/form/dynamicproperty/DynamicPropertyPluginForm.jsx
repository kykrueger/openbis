import React from 'react'
import logger from '@src/js/common/logger.js'

export default class DynamicPropertyPluginForm extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'DynamicPropertyPluginForm.render')
    return 'DynamicPropertyPluginForm'
  }
}
