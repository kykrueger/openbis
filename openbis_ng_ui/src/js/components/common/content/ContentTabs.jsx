import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Tabs from '@material-ui/core/Tabs'
import Tab from '@material-ui/core/Tab'
import CloseIcon from '@material-ui/icons/Close'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  tabsRoot: {
    borderBottomStyle: 'solid',
    borderBottomWidth: '1px',
    borderBottomColor: theme.palette.background.secondary
  },
  tabRoot: {
    textTransform: 'none'
  },
  iconRoot: {
    marginLeft: '6px'
  },
  tabLabel: {
    display: 'inline-flex',
    alignItems: 'center'
  }
})

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

    const { objects, selectedObject, classes } = this.props

    let selectedIndex = selectedObject
      ? _.findIndex(objects, selectedObject)
      : -1

    return (
      <Tabs
        value={selectedIndex !== -1 ? selectedIndex : false}
        variant='scrollable'
        scrollButtons='on'
        onChange={this.handleTabChange}
        classes={{ root: classes.tabsRoot }}
      >
        {this.props.objects.map(object => (
          <Tab
            key={`${object.type}/${object.id}`}
            label={this.renderLabel(object)}
            classes={{
              root: classes.tabRoot
            }}
          />
        ))}
      </Tabs>
    )
  }

  renderLabel(object) {
    let changed = _.find(this.props.changedObjects, object)
    return (
      <span className={this.props.classes.tabLabel}>
        {this.props.renderTab(object, changed)}
        {this.renderIcon(object)}
      </span>
    )
  }

  renderIcon(object) {
    return (
      <CloseIcon
        onClick={event => this.handleTabClose(event, object)}
        classes={{
          root: this.props.classes.iconRoot
        }}
      />
    )
  }
}

export default withStyles(styles)(ContentTabs)
