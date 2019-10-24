import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Typography from '@material-ui/core/Typography'
import TextField from '@material-ui/core/TextField'
import logger from '../../../common/logger.js'

const styles = theme => ({
  container: {
    padding: theme.spacing(2)
  }
})

class ObjectTypeParametersSection extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleChange = this.handleChange.bind(this)
  }

  handleChange(event) {
    const params = {
      id: this.props.selection.params.id,
      field: event.target.name,
      value: event.target.value
    }

    this.props.onChange('section', params)
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectTypeParametersSection.render')

    let { classes, sections, selection } = this.props

    if (!selection || selection.type !== 'section') {
      return null
    }

    let [section] = sections.filter(
      section => section.id === selection.params.id
    )

    return (
      <div className={classes.container}>
        <Typography variant='h6'>Section</Typography>
        <form>
          <div>
            <TextField
              label='Name'
              name='name'
              value={section.name || ''}
              fullWidth={true}
              margin='normal'
              variant='filled'
              InputLabelProps={{
                shrink: true
              }}
              onChange={this.handleChange}
            />
          </div>
        </form>
      </div>
    )
  }
}

export default withStyles(styles)(ObjectTypeParametersSection)
