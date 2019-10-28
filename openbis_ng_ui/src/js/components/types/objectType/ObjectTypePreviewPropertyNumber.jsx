import React from 'react'
import TextField from '../../common/form/TextField.jsx'
import { withStyles } from '@material-ui/core/styles'
import ObjectTypePreviewPropertyMetadata from './ObjectTypePreviewPropertyMetadata.jsx'
import logger from '../../../common/logger.js'

const styles = () => ({})

class ObjectTypePreviewPropertyNumber extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'ObjectTypePreviewPropertyNumber.render')

    const { property } = this.props

    return (
      <TextField
        type='number'
        label={property.label}
        description={property.description}
        mandatory={property.mandatory}
        transparent={!property.visible}
        metadata={<ObjectTypePreviewPropertyMetadata property={property} />}
      />
    )
  }
}

export default withStyles(styles)(ObjectTypePreviewPropertyNumber)
