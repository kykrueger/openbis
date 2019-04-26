import _ from 'lodash'
import React from 'react'
import {withStyles} from '@material-ui/core/styles'
import Tabs from '@material-ui/core/Tabs'
import Tab from '@material-ui/core/Tab'
import CloseIcon from '@material-ui/icons/Close'
import logger from '../../../common/logger.js'

const styles = {
  tabRoot: {
    textTransform: 'none'
  },
  tabWrapper: {
    flexDirection: 'row-reverse'
  },
  tabLabelContainer: {
    paddingRight: '0px'
  },
  tabLabelIcon: {
    minHeight: '48px'
  },
  iconRoot: {
    paddingLeft: '6px',
    paddingRight: '12px'
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

    const classes = this.props.classes

    return (
      <Tabs
        value={_.findIndex(this.props.objects, this.props.selectedObject)}
        scrollable
        scrollButtons="off"
        onChange={this.handleTabChange}
      >
        {this.props.objects.map(object =>
          <Tab key={`${object.type}/${object.id}`}
            label={object.id}
            icon={this.renderIcon(object)}
            classes={{
              root: classes.tabRoot,
              wrapper: classes.tabWrapper,
              labelContainer: classes.tabLabelContainer,
              labelIcon: classes.tabLabelIcon
            }}
          />
        )}
      </Tabs>
    )
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
