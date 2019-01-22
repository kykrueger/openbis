import React from 'react'
import {connect} from 'react-redux'
import {withStyles} from '@material-ui/core/styles'
import Button from '@material-ui/core/Button'
import Card from '@material-ui/core/Card'
import CardHeader from '@material-ui/core/CardHeader'
import CardContent from '@material-ui/core/CardContent'
import CardActions from '@material-ui/core/CardActions'
import Avatar from '@material-ui/core/Avatar'
import IconButton from '@material-ui/core/IconButton'
import FavoriteIcon from '@material-ui/icons/Favorite'
import ShareIcon from '@material-ui/icons/Share'
import ExpandMoreIcon from '@material-ui/icons/ExpandMore'
import TextField from '@material-ui/core/TextField'
import PropTypes from 'prop-types'
import _ from 'lodash'

import OpenBISTable from './OpenBISTable.jsx'
import actions from '../../reducer/actions.js'


/* eslint-disable-next-line no-unused-vars */
const styles = theme => ({
  textField: {
    width: '100%'
  }
})

function mapStateToProps(state) {
  return {
    dirtyEntities: state.dirtyEntities,
  }
}

function mapDispatchToProps(dispatch) {
  return {
    setDirty: (entity, dirty) => dispatch(actions.setDirty(entity.permId.permId, dirty)),
    saveEntity: (entity) => dispatch(actions.saveEntity(entity)),
  }
}

class EntityDetails extends React.Component {

  constructor(props) {
    super(props)
    this.state = {
      description: this.props.entity ? this.props.entity.description : '',
      dirty: false
    }
  }

  matches(value1, value2) {
    if (value1 === null || value1.length === 0) {
      return value2 === null || value2.length === 0
    }
    return value1 === value2
  }

  handleChange(name, e) {
    const currentlyDirty = !this.matches(e.target.value, this.props.entity.description)
    const stateDirty = this.props.dirtyEntities.indexOf(this.props.entity.permId.permId) > -1
    if (currentlyDirty !== stateDirty) {
      this.props.setDirty(this.props.entity, currentlyDirty)
    }
    this.setState({
      description: e.target.value,
      dirty: currentlyDirty
    })
  }

  handleSave(entity) {
    const entityCopy = _.cloneDeep(entity)
    entityCopy.description = this.state.description
    this.props.saveEntity(entityCopy)
  }

  render() {
    const {classes} = this.props
    if (this.props.entity === null) {
      return (<div/>)
    }
    const entity = this.props.entity

    const options = {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    }
    const created = new Date(entity.registrationDate).toLocaleDateString('en-US', options)

    return (
      <Card className={classes.card}>
        <CardHeader
          avatar={
            <Avatar aria-label="Recipe" className={classes.avatar}>
              S
            </Avatar>
          }
          action={
            <Button
              color="primary"
              disabled={!this.state.dirty}
              variant="contained"
              onClick={() => this.handleSave(this.props.entity)}>
              Save
            </Button>
          }
          title={'Space ' + entity.code}
          subheader={'Created on ' + created}
        />
        <CardContent>
          <TextField
            label='Description'
            className={classes.textField}
            value={this.state.description ? this.state.description : ''}
            onChange={(e) => {
              this.handleChange('description', e)
            }}
            margin='normal'
          />
          <OpenBISTable/>

        </CardContent>
        <CardActions disableActionSpacing>
          <IconButton aria-label="Add to favorites">
            <FavoriteIcon/>
          </IconButton>
          <IconButton aria-label="Share">
            <ShareIcon/>
          </IconButton>
          <IconButton>
            <ExpandMoreIcon/>
          </IconButton>
        </CardActions>
      </Card>
    )
  }
}

EntityDetails.propTypes = {
  entity: PropTypes.any.isRequired,
}

export default connect(mapStateToProps, mapDispatchToProps)(withStyles(styles)(EntityDetails))
