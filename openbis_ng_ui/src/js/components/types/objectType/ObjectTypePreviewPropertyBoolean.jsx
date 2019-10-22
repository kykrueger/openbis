import React from 'react'
import FormControl from '@material-ui/core/FormControl'
import FormControlLabel from '@material-ui/core/FormControlLabel'
import FormHelperText from '@material-ui/core/FormHelperText'
import Checkbox from '@material-ui/core/Checkbox'
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

class ObjectTypePreviewPropertyBoolean extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectTypePreviewPropertyBoolean.render')

    const { property, classes } = this.props

    return (
      <FormControl
        className={property.visible ? classes.visible : classes.hidden}
      >
        <FormControlLabel control={<Checkbox />} label={property.label} />
        <FormHelperText>{property.description}</FormHelperText>
      </FormControl>
    )
  }
}

export default withStyles(styles)(ObjectTypePreviewPropertyBoolean)
