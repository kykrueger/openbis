import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Typography from '@material-ui/core/Typography'
import TextField from '../../common/form/TextField.jsx'
import logger from '../../../common/logger.js'

const styles = theme => ({
  container: {
    padding: theme.spacing(2)
  },
  header: {
    paddingBottom: theme.spacing(2)
  },
  field: {
    paddingBottom: theme.spacing(2)
  }
})

class ObjectTypeParametersSection extends React.PureComponent {
  constructor(props) {
    super(props)
    this.reference = React.createRef()
    this.handleChange = this.handleChange.bind(this)
  }

  componentDidMount() {
    this.focus()
  }

  componentDidUpdate(prevProps) {
    const prevSelection = prevProps.selection
    const selection = this.props.selection

    if (prevSelection !== selection) {
      this.focus()
    }
  }

  focus() {
    const section = this.getSection(this.props)
    if (section) {
      this.reference.current.focus()
    }
  }

  handleChange(event) {
    const section = this.getSection(this.props)

    const params = {
      id: section.id,
      field: event.target.name,
      value: event.target.value
    }

    this.props.onChange('section', params)
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectTypeParametersSection.render')

    const section = this.getSection(this.props)
    if (!section) {
      return null
    }

    let { classes } = this.props

    return (
      <div className={classes.container}>
        <Typography variant='h6' className={classes.header}>
          Section
        </Typography>
        <div className={classes.field}>
          <TextField
            reference={this.reference}
            label='Name'
            name='name'
            value={section.name || ''}
            onChange={this.handleChange}
          />
        </div>
      </div>
    )
  }

  getSection(props) {
    let { sections, selection } = props

    if (selection && selection.type === 'section') {
      let [section] = sections.filter(
        section => section.id === selection.params.id
      )
      return section
    } else {
      return null
    }
  }
}

export default withStyles(styles)(ObjectTypeParametersSection)
