import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Typography from '@material-ui/core/Typography'

const styles = theme => ({
  label: {
    fontSize: theme.typography.label.fontSize,
    color: theme.typography.label.color
  },
  value: {
    paddingBottom: theme.spacing(1) / 2,
    borderBottomStyle: 'dotted',
    borderBottomWidth: '1px',
    borderBottomColor: theme.typography.label.color,
    '&:after': {
      content: '"\\00a0"'
    }
  }
})

class FormFieldView extends React.PureComponent {
  render() {
    const { label, value, classes } = this.props
    return (
      <div>
        <Typography variant='body2' className={classes.label}>
          {label}
        </Typography>
        <Typography variant='body2' className={classes.value}>
          {value ? value : ''}
        </Typography>
      </div>
    )
  }
}

export default withStyles(styles)(FormFieldView)
