import React from 'react'
import CheckboxField from '../../common/form/CheckboxField.jsx'
import { withStyles } from '@material-ui/core/styles'
import ObjectTypePreviewPropertyMetadata from './ObjectTypePreviewPropertyMetadata.jsx'
import logger from '../../../common/logger.js'

const styles = () => ({})

class ObjectTypePreviewPropertyBoolean extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'ObjectTypePreviewPropertyBoolean.render')

    const { property } = this.props

    return (
      <div>
        <CheckboxField
          label={property.label}
          description={property.description}
          mandatory={property.mandatory}
          metadata={<ObjectTypePreviewPropertyMetadata property={property} />}
          transparent={!property.visible}
        />
      </div>
    )
  }
}

export default withStyles(styles)(ObjectTypePreviewPropertyBoolean)
