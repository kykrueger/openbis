import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import ObjectTypeParametersType from './ObjectTypeParametersType.jsx'
import ObjectTypeParametersProperty from './ObjectTypeParametersProperty.jsx'
import ObjectTypeParametersSection from './ObjectTypeParametersSection.jsx'
import logger from '../../../common/logger.js'

const styles = () => ({})

class ObjectTypeParameters extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectTypeParameters.render')

    const {
      type,
      sections,
      properties,
      selection,
      onChange,
      onSelectionChange,
      onBlur
    } = this.props

    return (
      <div>
        <ObjectTypeParametersType
          type={type}
          selection={selection}
          onChange={onChange}
          onSelectionChange={onSelectionChange}
          onBlur={onBlur}
        />
        <ObjectTypeParametersSection
          sections={sections}
          selection={selection}
          onChange={onChange}
          onSelectionChange={onSelectionChange}
          onBlur={onBlur}
        />
        <ObjectTypeParametersProperty
          type={type}
          properties={properties}
          selection={selection}
          onChange={onChange}
          onSelectionChange={onSelectionChange}
          onBlur={onBlur}
        />
      </div>
    )
  }
}

export default withStyles(styles)(ObjectTypeParameters)
