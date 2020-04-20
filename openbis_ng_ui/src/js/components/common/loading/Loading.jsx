import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import CircularProgress from '@material-ui/core/CircularProgress'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    width: '100%',
    height: '100%',
    position: 'relative'
  },
  mask: {
    position: 'absolute',
    width: '100%',
    height: '100%',
    zIndex: 10000,
    backgroundColor: theme.palette.background.paper,
    opacity: 0.6,
    textAlign: 'center'
  },
  progress: {
    position: 'absolute',
    top: '20%',
    left: 'calc(50% - 20px)',
    zIndex: 10001
  }
})

class Loading extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'Loading.render')

    const classes = this.props.classes

    return (
      <div className={classes.container}>
        {this.props.loading && (
          <React.Fragment>
            <div className={classes.mask}></div>
            <CircularProgress className={classes.progress} />
          </React.Fragment>
        )}
        {this.props.children}
      </div>
    )
  }
}

export default withStyles(styles)(Loading)
