import React from 'react'
import {withStyles} from '@material-ui/core/styles'
import CircularProgress from '@material-ui/core/CircularProgress'

const styles = {
  loader: {
    position: 'absolute',
    paddingTop: '15%',
    width: '100%',
    height: '100%',
    zIndex: 1000,
    backgroundColor: '#FFFFFF',
    opacity: 0.5,
    textAlign: 'center',
  }
}

class Loading extends React.Component {

  render() {
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
