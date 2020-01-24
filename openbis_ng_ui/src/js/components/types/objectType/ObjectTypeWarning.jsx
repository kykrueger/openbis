import React from 'react'
import Typography from '@material-ui/core/Typography'
import WarningIcon from '@material-ui/icons/Warning'
import { withStyles } from '@material-ui/core/styles'
import logger from '../../../common/logger.js'

const styles = theme => ({
  warning: {
    display: 'flex',
    alignItems: 'center',
    '& svg': {
      marginRight: theme.spacing(1),
      color: theme.palette.warning.main
    }
  }
})

class ObjectTypeWarning extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'ObjectTypeWarning.render')

    const { classes, children } = this.props

    return (
      <Typography variant='body2' className={classes.warning}>
        <WarningIcon />
        {children}
      </Typography>
    )
  }
}

export default withStyles(styles)(ObjectTypeWarning)
