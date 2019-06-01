import _ from 'lodash'
import React from 'react'
import {withStyles} from '@material-ui/core/styles'
import Tabs from '@material-ui/core/Tabs'
import Tab from '@material-ui/core/Tab'
import CloseIcon from '@material-ui/icons/Close'
import logger from '../../../common/logger.js'

const styles = {
  tabsRoot: {
    height: '48px'
  },
  tabsScrollable: {
    overflow: 'auto',
    marginBottom: '0px !important'
  },
  tabsScrollButtons: {
    height: '48px'
  },
  tabRoot: {
    textTransform: 'none'
  },
  iconRoot: {
    marginLeft: '6px',
  },
  tabLabel: {
    display: 'inline-flex',
    alignItems: 'center',
    marginRight: '-16px'
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
        classes={{
          root: classes.tabsRoot,
          scrollable: classes.tabsScrollable,
          scrollButtons: classes.tabsScrollButtons
        }}
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
    let label = _.find(this.props.changedObjects, object) ? object.id + '*' : object.id
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
