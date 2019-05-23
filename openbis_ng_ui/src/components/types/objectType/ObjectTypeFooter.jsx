import React from 'react'
import Button from '@material-ui/core/Button'
import {withStyles} from '@material-ui/core/styles'
import logger from '../../../common/logger.js'

const styles = (theme) => ({
  button: {
    marginRight: theme.spacing.unit * 2
  }
})

class ObjectTypeFooter extends React.Component {

  render() {
    logger.log(logger.DEBUG, 'ObjectTypeFooter.render')

    const classes = this.props.classes

    return (
      <div>
        <Button classes={{ root: classes.button }} variant='contained' color='secondary' onClick={this.props.onAdd}>Add</Button>
        <Button classes={{ root: classes.button }} variant='contained' color='secondary' onClick={this.props.onRemove}>Remove</Button>
        <Button classes={{ root: classes.button }} variant='contained' color='primary' onClick={this.props.onSave}>Save</Button>
      </div>
    )
  }

}

export default withStyles(styles)(ObjectTypeFooter)
