import React from 'react'
import Typography from '@material-ui/core/Typography'

export default class TypeFormHeader extends React.PureComponent {
  render() {
    return (
      <Typography variant='h6' className={this.props.className}>
        {this.props.children}
      </Typography>
    )
  }
}
