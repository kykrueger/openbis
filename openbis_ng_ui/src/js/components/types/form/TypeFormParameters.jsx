import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import logger from '@src/js/common/logger.js'

import TypeFormParametersType from './TypeFormParametersType.jsx'
import TypeFormParametersProperty from './TypeFormParametersProperty.jsx'
import TypeFormParametersSection from './TypeFormParametersSection.jsx'

const styles = () => ({})

class TypeFormParameters extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'TypeFormParameters.render')

    const {
      controller,
      type,
      sections,
      properties,
      selection,
      mode,
      onChange,
      onSelectionChange,
      onBlur
    } = this.props

    return (
      <div>
        <TypeFormParametersType
          controller={controller}
          type={type}
          selection={selection}
          mode={mode}
          onChange={onChange}
          onSelectionChange={onSelectionChange}
          onBlur={onBlur}
        />
        <TypeFormParametersSection
          sections={sections}
          selection={selection}
          mode={mode}
          onChange={onChange}
          onSelectionChange={onSelectionChange}
          onBlur={onBlur}
        />
        <TypeFormParametersProperty
          controller={controller}
          type={type}
          properties={properties}
          selection={selection}
          mode={mode}
          onChange={onChange}
          onSelectionChange={onSelectionChange}
          onBlur={onBlur}
        />
      </div>
    )
  }
}

export default withStyles(styles)(TypeFormParameters)
