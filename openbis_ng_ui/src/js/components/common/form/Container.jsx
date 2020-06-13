import React from 'react'
import { withStyles } from '@material-ui/core/styles'

const styles = theme => ({
  container: {
    padding: `${theme.spacing(1)}px ${theme.spacing(2)}px`
  }
})

class Container extends React.Component {
  render() {
    const { children, onClick, className, classes } = this.props
    return (
      <div className={`${classes.container} ${className}`} onClick={onClick}>
        {children}
      </div>
    )
  }
}

export default withStyles(styles)(Container)
