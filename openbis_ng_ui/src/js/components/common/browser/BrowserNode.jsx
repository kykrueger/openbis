import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import ListItem from '@material-ui/core/ListItem'
import ListItemIcon from '@material-ui/core/ListItemIcon'
import ListItemText from '@material-ui/core/ListItemText'
import Collapse from '@material-ui/core/Collapse'
import ChevronRightIcon from '@material-ui/icons/ChevronRight'
import ExpandMoreIcon from '@material-ui/icons/ExpandMore'
import logger from '@src/js/common/logger.js'

import BrowserNodes from './BrowserNodes.jsx'

const styles = theme => ({
  item: {
    padding: '4px 0px'
  },
  icon: {
    marginLeft: '8px',
    marginRight: '6px',
    minWidth: '24px'
  },
  text: {
    fontSize: theme.typography.body2.fontSize
  }
})

class BrowserNode extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'BrowserNode.render')

    const { controller, node, level, classes } = this.props

    return (
      <div>
        <ListItem
          button
          selected={node.selected}
          onClick={() => controller.nodeSelect(node.id)}
          style={{ paddingLeft: level * 24 + 'px' }}
          classes={{
            root: classes.item
          }}
        >
          {this.renderIcon(node)}
          {this.renderText(node)}
        </ListItem>
        {node.children && node.children.length > 0 && (
          <Collapse in={node.expanded} mountOnEnter={true} unmountOnExit={true}>
            <BrowserNodes
              controller={controller}
              nodes={node.children}
              level={level + 1}
            />
          </Collapse>
        )}
      </div>
    )
  }

  renderIcon(node) {
    logger.log(logger.DEBUG, 'BrowserNode.renderIcon')

    const { controller, classes } = this.props

    if (node.children && node.children.length > 0) {
      let icon = null
      if (node.expanded) {
        icon = (
          <ExpandMoreIcon
            onClick={e => {
              e.stopPropagation()
              controller.nodeCollapse(node.id)
            }}
          />
        )
      } else {
        icon = (
          <ChevronRightIcon
            onClick={e => {
              e.stopPropagation()
              controller.nodeExpand(node.id)
            }}
          />
        )
      }
      return (
        <ListItemIcon
          classes={{
            root: classes.icon
          }}
        >
          {icon}
        </ListItemIcon>
      )
    } else {
      return (
        <ListItemIcon
          classes={{
            root: classes.icon
          }}
        >
          <span></span>
        </ListItemIcon>
      )
    }
  }

  renderText(node) {
    logger.log(logger.DEBUG, 'BrowserNode.renderText "' + node.text + '"')

    const { classes } = this.props

    return (
      <ListItemText
        primary={node.text}
        classes={{
          primary: classes.text
        }}
      />
    )
  }
}

export default _.flow(withStyles(styles))(BrowserNode)
