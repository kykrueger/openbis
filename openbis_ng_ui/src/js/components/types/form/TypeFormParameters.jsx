import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TypeFormParametersType from '@src/js/components/types/form/TypeFormParametersType.jsx'
import TypeFormParametersProperty from '@src/js/components/types/form/TypeFormParametersProperty.jsx'
import TypeFormParametersSection from '@src/js/components/types/form/TypeFormParametersSection.jsx'
import logger from '@src/js/common/logger.js'

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
