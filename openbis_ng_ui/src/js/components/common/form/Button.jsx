import React from 'react'
import Button from '@material-ui/core/Button'
import { withStyles } from '@material-ui/core/styles'

const styles = theme => ({
  risky: {
    backgroundColor: theme.palette.error.main,
    color: theme.palette.error.contrastText,
    '&:hover': {
      backgroundColor: theme.palette.error.dark
    },
    '&:disabled': {
      backgroundColor: theme.palette.error.light
    }
  }
})

class FormButton extends React.Component {
  render() {
    const {
      name,
      label,
      type,
      disabled,
      href,
      styles,
      classes,
      onClick
    } = this.props

    let theColor = 'secondary'
    let theClasses = { ...styles }

    if (type === 'final') {
      theColor = 'primary'
    } else if (type === 'risky') {
      theClasses = {
        ...styles,
        root: `${styles.root} ${classes.risky}`
      }
    }

    return (
      <Button
        name={name}
        classes={theClasses}
        variant='contained'
        color={theColor}
        href={href}
        onClick={onClick}
        disabled={disabled}
        size='small'
      >
        {label}
      </Button>
    )
  }
}

export default withStyles(styles)(FormButton)
