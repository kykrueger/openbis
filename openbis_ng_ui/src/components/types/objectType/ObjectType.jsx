import _ from 'lodash'
import React from 'react'
import TextField from '@material-ui/core/TextField'
import Checkbox from '@material-ui/core/Checkbox'
import Button from '@material-ui/core/Button'
import logger from '../../../common/logger.js'
import {facade, dto} from '../../../services/openbis.js'

class ObjectType extends React.Component {

  constructor(props){
    super(props)
    this.state = {
      loaded: false,
    }
    this.handleSave = this.handleSave.bind(this)
  }

  componentDidMount(){
    facade.getSampleTypes([new dto.EntityTypePermId(this.props.objectId)], new dto.SampleTypeFetchOptions()).then(map => {
      let objectType = map[this.props.objectId]
      if(objectType){
        this.setState(() => {
          return {
            loaded: true,
            fields: {
              description: objectType.description || '',
              listable: objectType.listable
            }
          }
        })
      }
    })
  }

  handleChange(name){
    return event => {
      let value = _.has(event.target, 'checked') ? event.target.checked : event.target.value
      this.setState((prevState) => ({
        ...prevState,
        fields: {
          ...prevState.fields,
          [name]: value
        }
      }))
    }
  }

  handleSave(){
    let {description, listable} = this.state.fields
    let update = new dto.SampleTypeUpdate()
    update.setTypeId(new dto.EntityTypePermId(this.props.objectId))
    update.setDescription(description)
    update.setListable(listable)
    facade.updateSampleTypes([update])
  }

  render() {
    logger.log(logger.DEBUG, 'ObjectType.render')

    if(!this.state.loaded){
      return <div></div>
    }

    let { description, listable } = this.state.fields

    return (
      <div>
        <div>{this.props.objectId}</div>
        <form>
          <div>
            <TextField label='Description' value={description} onChange={this.handleChange('description')} />
          </div>
          <div>
            <Checkbox checked={listable} value='listable' onChange={this.handleChange('listable')} />
          </div>
          <div>
            <Button variant='contained' color='primary' onClick={this.handleSave}>Save</Button>
          </div>
        </form>
      </div>
    )
  }

}

export default ObjectType
