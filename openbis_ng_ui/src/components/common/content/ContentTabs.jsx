import _ from 'lodash'
import React from 'react'
import {withStyles} from '@material-ui/core/styles'
import Tabs from '@material-ui/core/Tabs'
import Tab from '@material-ui/core/Tab'
import CloseIcon from '@material-ui/icons/Close'
import * as objectTypes from '../../../common/consts/objectType.js'
import logger from '../../../common/logger.js'

const styles = {
  tabRoot: {
    textTransform: 'none'
  },
  iconRoot: {
    marginLeft: '6px',
  },
  tabLabel: {
    display: 'inline-flex',
    alignItems: 'center',
  }
}

class ContentTabs extends React.Component {

  handleTabChange = (event, value) => {
    let object = this.props.objects[value]
    this.props.objectSelect(object.type, object.id)
  }

  handleTabClose = (event, object) => {
    this.props.objectClose(object.type, object.id)
    event.stopPropagation()
  }

  render() {
    logger.log(logger.DEBUG, 'ContentTabs.render')

    const { classes } = this.props

    return (
      <Tabs
        value={_.findIndex(this.props.objects, this.props.selectedObject)}
        variant="scrollable"
        scrollButtons="on"
        onChange={this.handleTabChange}
      >
        {this.props.objects.map(object =>
          <Tab key={`${object.type}/${object.id}`}
            label={this.renderLabel(object)}
            classes={{
              root: classes.tabRoot
            }}
          />
        )}
      </Tabs>
    )
  }

  renderLabel(object){
    let changed = _.find(this.props.changedObjects, object) ? '*' : ''
    let label = null

    switch(object.type){
      case objectTypes.SEARCH:
        label = 'search: ' + object.id
        break
      default:
        label = object.id + changed
        break
    }

    return <span className={this.props.classes.tabLabel}>{label}{this.renderIcon(object)}</span>
  }

  renderIcon(object){
    return <CloseIcon
      onClick={(event) => this.handleTabClose(event, object)}
      classes={{
        root: this.props.classes.iconRoot
      }}
    />
  }

}

export default withStyles(styles)(ContentTabs)
