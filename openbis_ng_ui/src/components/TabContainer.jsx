import React from 'react'
import {withStyles} from '@material-ui/core/styles'
import AppBar from '@material-ui/core/AppBar'
import CloseIcon from '@material-ui/icons/Close'
import IconButton from '@material-ui/core/IconButton'
import Toolbar from '@material-ui/core/Toolbar'
import Tabs from '@material-ui/core/Tabs'
import Tab from '@material-ui/core/Tab'
import PropTypes from 'prop-types'

import TabContent from './TabContent.jsx'


/* eslint-disable-next-line no-unused-vars */
const styles = theme => ({
  entityTabs: {
    width: '100%'
  },
  inlineElement: {
    display: 'inline-block'
  },
  hidden: {
    display: 'none',
  }
})

class TabContainer extends React.Component {

  render() {
    const classes = this.props.classes
    const selectedKey = this.props.selectedKey
    const selectedTabIndex = this.props.children.findIndex(child => child.key === selectedKey)
    return (
      <div>
        <div>
          <AppBar position="static">
            <Toolbar>
              <Tabs
                value={selectedTabIndex}
                scrollable={React.Children.count(this.props.children) > 0}
                scrollButtons="auto"
                className={classes.entityTabs}>
                {
                  React.Children.map(this.props.children, child =>
                    <Tab
                      component="div"
                      label={
                        <div>
                          <div className={classes.inlineElement}>{child.props.name}</div>
                          {child.props.dirty &&
                          <div className={classes.inlineElement}>*</div>
                          }
                          <div className={classes.inlineElement}>
                            <IconButton onClick={(e) => child.props.onClose(e)}>
                              <CloseIcon/>
                            </IconButton>
                          </div>
                        </div>}
                      onClick={child.props.onSelect}/>
                  )
                }
              </Tabs>
            </Toolbar>
          </AppBar>
        </div>
        <div>
          {
            React.Children.map(this.props.children, child => {
              return (
                <div className={selectedKey === child.key ? {} : classes.hidden}>
                  {child}
                </div>
              )
            })
          }
        </div>
      </div>
    )
  }
}

TabContainer.propTypes = {
  selectedKey: PropTypes.string.isRequired,
  children: function (props, propName, componentName) {
    const prop = props[propName]
    let error = null
    React.Children.forEach(prop, function (child) {
      if (child.type !== TabContent) {
        error = new Error('`' + componentName + '` children should be of type `TabContent`.')
      }
    })
    return error
  }
}

export default withStyles(styles)(TabContainer)
