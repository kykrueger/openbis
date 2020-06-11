import React from 'react'
import Button from '@material-ui/core/Button'

export default class FormButton extends React.Component {
  render() {
    const { name, label, onClick, disabled, final, classes } = this.props

    return (
      <Button
        name={name}
        classes={classes}
        variant='contained'
        color={final ? 'primary' : 'secondary'}
        onClick={onClick}
        disabled={disabled}
        size='small'
      >
        {label}
      </Button>
    )
  }
}
