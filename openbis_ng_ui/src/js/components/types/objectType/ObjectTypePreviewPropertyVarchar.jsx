import React from 'react'
import TextField from '@material-ui/core/TextField'
import { withStyles } from '@material-ui/core/styles'
import logger from '../../../common/logger.js'

const styles = () => ({
  visible: {
    opacity: 1
  },
  hidden: {
    opacity: 0.5
  }
})

class ObjectTypePreviewPropertyVarchar extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectTypePreviewPropertyVarchar.render')

    const { property, classes } = this.props

    return (
      <TextField
        error={property.mandatory}
        multiline={property.dataType === 'MULTILINE_VARCHAR'}
        label={property.label}
        helperText={property.description}
        fullWidth={true}
        InputLabelProps={{
          shrink: true
        }}
        className={property.visible ? classes.visible : classes.hidden}
        variant='filled'
      />
    )
  }
}

export default withStyles(styles)(ObjectTypePreviewPropertyVarchar)
