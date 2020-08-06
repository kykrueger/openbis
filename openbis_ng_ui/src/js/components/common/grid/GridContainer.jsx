import React from 'react'
import { withStyles } from '@material-ui/core/styles'

const styles = theme => ({
  container: {
    padding: theme.spacing(2),
    paddingBottom: 0
  }
})

class GridContainer extends React.Component {
  render() {
    const { classes, children } = this.props
    return <div className={classes.container}>{children}</div>
  }
}

export default withStyles(styles)(GridContainer)
