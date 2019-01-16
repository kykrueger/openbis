import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import { connect } from 'react-redux'
import CircularProgress from '@material-ui/core/CircularProgress'
import flow from 'lodash/flow'

const styles = {
  loader: { 
    position: 'absolute',
    paddingTop: '15%',
    width: '100%',
    height: '100%',
    zIndex: 1000,
    backgroundColor: '#000000',
    opacity: 0.5,
    textAlign: 'center',
  }
}

function mapStateToProps(state) {
  return {
    loading: state.loading,
    exception: state.exceptions.length > 0 ? state.exceptions[0] : null
  }
}

class WithLoader extends React.Component {

  render() {
    const classes = this.props.classes

    return (
      <div>
        {
          this.props.loading &&
          <div className={classes.loader}>
            <CircularProgress className={classes.progress} />
          </div>
        }
        {this.props.children}
      </div>
    )
  }
}

export default flow(
  connect(mapStateToProps),
  withStyles(styles),
)(WithLoader)
