import React from 'react'
import TextField from '../../common/form/TextField.jsx'
import { withStyles } from '@material-ui/core/styles'
import logger from '../../../common/logger.js'

const styles = () => ({})

class ObjectTypePreviewPropertyVarchar extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'ObjectTypePreviewPropertyVarchar.render')

    const { property } = this.props

    return (
      <TextField
        label={property.label}
        description={property.description}
        mandatory={property.mandatory}
        transparent={!property.visible}
      />
    )
  }
}

export default withStyles(styles)(ObjectTypePreviewPropertyVarchar)
