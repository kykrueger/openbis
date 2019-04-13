import _ from 'lodash'
import React from 'react'
import TextField from '@material-ui/core/TextField'
import Checkbox from '@material-ui/core/Checkbox'
import Button from '@material-ui/core/Button'
import logger from '../../../common/logger.js'
import openbis from '../../../services/openbis.js'


class ObjectType extends React.Component {

  constructor(props){
    super(props)
    this.state = {
      description: '',
      listable: false
    }
  }

  componentDidMount(){
  }

  handleChange(name){
    return event => {
      let value = _.has(event.target, 'checked') ? event.target.checked : event.target.value
      this.setState({ [name]: value })
    }
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectType.render')
    return (
      <div>
        {this.props.objectId}
        <form>
          <div>
            <TextField
              label='Description'
              value={this.state.description}
              onChange={this.handleChange('description')}
            />
          </div>
          <div>
            <Checkbox
              checked={this.state.listable}
              value='listable'
              onChange={this.handleChange('listable')}
            />
          </div>
          <div>
            <Button variant='contained' color='primary'>
            Save
            </Button>
          </div>
        </form>
      </div>
    )
  }

}

export default ObjectType
