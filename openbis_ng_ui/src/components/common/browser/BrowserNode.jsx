import React from 'react'
import _ from 'lodash'
import {withStyles} from '@material-ui/core/styles'
import ListItem from '@material-ui/core/ListItem'
import ListItemIcon from '@material-ui/core/ListItemIcon'
import ListItemText from '@material-ui/core/ListItemText'
import Collapse from '@material-ui/core/Collapse'
import ChevronRightIcon from '@material-ui/icons/ChevronRight'
import ExpandMoreIcon from '@material-ui/icons/ExpandMore'
import BrowserNodes from './BrowserNodes.jsx'
import logger from '../../../common/logger.js'

const styles = {
  icon: {
    margin: '0px 8px'
  },
  text: {
    marginLeft: '-16px'
  }
}

class BrowserNode extends React.Component {

  render() {
    logger.log(logger.DEBUG, 'BrowserNode.render')

    const {node, level} = this.props

    return (<div>
      <ListItem
        button
        selected={node.selected}
        onClick={() => this.props.nodeSelect(node.id)}
        style={{paddingLeft: level * 20 + 'px'}}>
        {this.renderIcon(node)}
        {this.renderText(node)}
      </ListItem>
      {node.children && node.children.length > 0 &&
            <Collapse in={node.expanded} mountOnEnter={true} unmountOnExit={true}>
              <BrowserNodes
                nodes={node.children}
                nodeSelect={this.props.nodeSelect}
                nodeCollapse={this.props.nodeCollapse}
                nodeExpand={this.props.nodeExpand}
                level={level + 1} />
            </Collapse>}
    </div>)
  }

  renderIcon(node){
    logger.log(logger.DEBUG, 'BrowserNode.renderIcon')

    const classes = this.props.classes

    if(node.children && node.children.length > 0){
      let icon = null
      if(node.expanded){
        icon = <ExpandMoreIcon onClick={(e) => {
          e.stopPropagation()
          this.props.nodeCollapse(node.id)
        }}/>
      }else{
        icon = <ChevronRightIcon onClick={(e) => {
          e.stopPropagation()
          this.props.nodeExpand(node.id)
        }}/>
      }
      return <ListItemIcon classes={{
        root: classes.icon
      }}>{icon}</ListItemIcon>
    }else{
      return null
    }
  }

  renderText(node){
    logger.log(logger.DEBUG, 'BrowserNode.renderText "' + node.text + '"')

    const classes = this.props.classes

    return <ListItemText
      primary={node.text}
      inset={true}
      classes={{
        primary: classes.text
      }}
    />
  }

}

export default _.flow(
  withStyles(styles)
)(BrowserNode)
