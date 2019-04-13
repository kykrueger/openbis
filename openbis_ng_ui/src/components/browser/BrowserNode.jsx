import React from 'react'
import ListItem from '@material-ui/core/ListItem'
import ListItemIcon from '@material-ui/core/ListItemIcon'
import ListItemText from '@material-ui/core/ListItemText'
import Collapse from '@material-ui/core/Collapse'
import ChevronRightIcon from '@material-ui/icons/ChevronRight'
import ExpandMoreIcon from '@material-ui/icons/ExpandMore'
import BrowserNodes from './BrowserNodes.jsx'
import logger from '../../common/logger.js'

class BrowserNode extends React.Component {

  render() {
    logger.log(logger.DEBUG, 'BrowserNode.render')

    const {node, level} = this.props

    return (<div>
      <ListItem
        button
        selected={node.selected}
        style={{paddingLeft: level * 20 + 'px'}}>
        {this.renderIcon(node)}
        {this.renderText(node)}
      </ListItem>
      {node.children && node.children.length > 0 &&
            <Collapse in={node.expanded} mountOnEnter={true} unmountOnExit={true}>
              <BrowserNodes {...this.props} nodes={node.children} level={level + 1} />
            </Collapse>}
    </div>)
  }

  renderIcon(node){
    if(node.children && node.children.length > 0){
      if(node.expanded){
        return (<ListItemIcon><ExpandMoreIcon onClick={() => this.props.nodeCollapse(node.id)}/></ListItemIcon>)
      }else{
        return (<ListItemIcon><ChevronRightIcon onClick={() => this.props.nodeExpand(node.id)}/></ListItemIcon>)
      }
    }else{
      return null
    }
  }

  renderText(node){
    logger.log(logger.DEBUG, 'BrowserNode.renderText "' + node.text + '"')
    return <ListItemText primary={node.text} inset={true} onClick={() => this.props.nodeSelect(node.id)} />
  }

}

export default BrowserNode
