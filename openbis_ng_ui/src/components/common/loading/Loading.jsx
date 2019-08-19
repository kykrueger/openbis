import React from 'react'
import {withStyles} from '@material-ui/core/styles'
import CircularProgress from '@material-ui/core/CircularProgress'
import logger from '../../../common/logger.js'

const styles = (theme) => ({
  loader: {
    position: 'absolute',
    width: '100%',
    height: '100%',
    zIndex: 1000,
    backgroundColor: theme.palette.background.paper,
    opacity: 0.8,
    textAlign: 'center',
  },
  progress: {
    position: 'absolute',
    top: '20%',
    left: 'calc(50% - 20px)'
  }
})

class Loading extends React.Component {

  render() {
    logger.log(logger.DEBUG, 'Loading.render')

    const classes = this.props.classes

    return (
      <div>
        {
          this.props.loading &&
          <div className={classes.loader}>
            <CircularProgress className={classes.progress}/>
          </div>
        }
        {this.props.children}
      </div>
    )
  }
}

export default withStyles(styles)(Loading)
