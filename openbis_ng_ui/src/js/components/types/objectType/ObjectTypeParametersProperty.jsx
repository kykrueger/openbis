import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import { dto } from '../../../services/openbis.js'
import Typography from '@material-ui/core/Typography'
import FormControlLabel from '@material-ui/core/FormControlLabel'
import Checkbox from '@material-ui/core/Checkbox'
import TextField from '@material-ui/core/TextField'
import logger from '../../../common/logger.js'

const styles = theme => ({
  container: {
    padding: theme.spacing(2)
  }
})

class ObjectTypeParametersProperty extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleChange = this.handleChange.bind(this)
  }

  handleChange(event) {
    const id = this.props.selection.params.id

    let params = null

    if (_.has(event.target, 'checked')) {
      params = {
        id,
        field: event.target.value,
        value: event.target.checked
      }
    } else {
      params = {
        id,
        field: event.target.name,
        value: event.target.value
      }
    }

    this.props.onChange('property', params)
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectTypeParametersProperty.render')

    let { classes, properties, selection } = this.props

    if (!selection || selection.type !== 'property') {
      return null
    }

    let [property] = properties.filter(
      property => property.id === selection.params.id
    )

    return (
      <div className={classes.container}>
        <Typography variant='h6'>Property</Typography>
        <form>
          <div>
            <TextField
              label='Code'
              name='code'
              value={property.code}
              fullWidth={true}
              margin='normal'
              variant='filled'
              InputLabelProps={{
                shrink: true
              }}
              onChange={this.handleChange}
            />
          </div>
          <div>
            <TextField
              select
              label='Data Type'
              name='dataType'
              SelectProps={{
                native: true
              }}
              value={property.dataType}
              fullWidth={true}
              margin='normal'
              variant='filled'
              InputLabelProps={{
                shrink: true
              }}
              onChange={this.handleChange}
            >
              <option value=''></option>
              {dto.DataType.values.sort().map(dataType => {
                return (
                  <option key={dataType} value={dataType}>
                    {dataType}
                  </option>
                )
              })}
            </TextField>
          </div>
          <div>
            <TextField
              label='Label'
              name='label'
              value={property.label}
              fullWidth={true}
              margin='normal'
              variant='filled'
              InputLabelProps={{
                shrink: true
              }}
              onChange={this.handleChange}
            />
          </div>
          <div>
            <TextField
              multiline
              label='Description'
              name='description'
              value={property.description}
              fullWidth={true}
              margin='normal'
              variant='filled'
              InputLabelProps={{
                shrink: true
              }}
              onChange={this.handleChange}
            />
          </div>
          <div>
            <FormControlLabel
              control={
                <Checkbox
                  value='mandatory'
                  checked={property.mandatory}
                  onChange={this.handleChange}
                />
              }
              label='Mandatory'
            />
          </div>
          <div>
            <FormControlLabel
              control={
                <Checkbox
                  value='visible'
                  checked={property.visible}
                  onChange={this.handleChange}
                />
              }
              label='Visible'
            />
          </div>
        </form>
      </div>
    )
  }
}

export default withStyles(styles)(ObjectTypeParametersProperty)
