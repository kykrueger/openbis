import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import ObjectTypeParametersType from './ObjectTypeParametersType.jsx'
import ObjectTypeParametersProperty from './ObjectTypeParametersProperty.jsx'
import ObjectTypeParametersSection from './ObjectTypeParametersSection.jsx'
import logger from '../../../common/logger.js'

const styles = () => ({
  container: {
    minWidth: '400px'
  }
})

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
      classes
    } = this.props

    return (
      <div className={classes.container}>
        <ObjectTypeParametersType
          type={type}
          selection={selection}
          onChange={onChange}
        />
        <ObjectTypeParametersSection
          sections={sections}
          selection={selection}
          onChange={onChange}
        />
        <ObjectTypeParametersProperty
          properties={properties}
          selection={selection}
          onChange={onChange}
        />
      </div>
    )
  }
}

export default withStyles(styles)(ObjectTypeParameters)
