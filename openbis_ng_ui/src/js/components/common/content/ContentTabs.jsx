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
    borderBottomColor: theme.palette.background.secondary,
    minHeight: '36px'
  },
  tabRoot: {
    textTransform: 'none',
    minHeight: '36px'
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
    const tab = this.props.tabs[value]
    this.props.tabSelect(tab)
  }

  handleTabClose = (event, tab) => {
    this.props.tabClose(tab)
    event.stopPropagation()
  }

  render() {
    logger.log(logger.DEBUG, 'ContentTabs.render')

    const { tabs, selectedTab, classes } = this.props

    let value = false

    if (selectedTab) {
      const selectedIndex = _.findIndex(tabs, selectedTab)
      if (selectedIndex !== -1) {
        value = selectedIndex
      }
    }

    return (
      <Tabs
        value={value}
        variant='scrollable'
        scrollButtons='on'
        onChange={this.handleTabChange}
        classes={{ root: classes.tabsRoot }}
      >
        {this.props.tabs.map(tab => (
          <Tab
            key={tab.id}
            label={this.renderLabel(tab)}
            classes={{
              root: classes.tabRoot
            }}
          />
        ))}
      </Tabs>
    )
  }

  renderLabel(tab) {
    return (
      <span className={this.props.classes.tabLabel}>
        {this.props.renderTab(tab)}
        {this.renderIcon(tab)}
      </span>
    )
  }

  renderIcon(tab) {
    return (
      <CloseIcon
        onClick={event => this.handleTabClose(event, tab)}
        classes={{
          root: this.props.classes.iconRoot
        }}
        fontSize='small'
      />
    )
  }
}

export default withStyles(styles)(ContentTabs)
