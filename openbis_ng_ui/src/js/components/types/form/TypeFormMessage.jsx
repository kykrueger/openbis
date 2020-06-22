import React from 'react'
import Typography from '@material-ui/core/Typography'
import InfoIcon from '@material-ui/icons/Info'
import WarningIcon from '@material-ui/icons/Warning'
import { withStyles } from '@material-ui/core/styles'
import util from '@src/js/common/util.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  message: {
    display: 'flex',
    alignItems: 'center',
    '& svg': {
      marginRight: theme.spacing(1)
    }
  },
  warning: {
    '& svg': {
      color: theme.palette.warning.main
    }
  },
  info: {
    '& svg': {
      color: theme.palette.info.main
    }
  }
})

class TypeFormMessage extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'TypeFormMessage.render')

    const { classes, children, type } = this.props

    return (
      <Typography
        variant='body2'
        className={util.classNames(classes.message, classes[type])}
      >
        {this.renderIcon(type)}
        {children}
      </Typography>
    )
  }

  renderIcon(type) {
    if (type === 'info') {
      return <InfoIcon fontSize='small' />
    } else if (type === 'warning') {
      return <WarningIcon fontSize='small' />
    } else {
      return null
    }
  }
}

export default withStyles(styles)(TypeFormMessage)
