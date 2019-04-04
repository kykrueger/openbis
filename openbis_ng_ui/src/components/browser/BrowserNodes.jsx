import React from 'react'
import {withStyles} from '@material-ui/core/styles'
import List from '@material-ui/core/List'
import ListItem from '@material-ui/core/ListItem'
import ListItemIcon from '@material-ui/core/ListItemIcon'
import ListItemText from '@material-ui/core/ListItemText'
import Collapse from '@material-ui/core/Collapse'
import ChevronRightIcon from '@material-ui/icons/ChevronRight'
import ExpandMoreIcon from '@material-ui/icons/ExpandMore'
import logger from '../../common/logger.js'

const styles = () => ({
  browserList: {
    paddingTop: '0',
    paddingBottom: '0',
  },
})

class BrowserNodes extends React.Component {

  render() {
    logger.log(logger.DEBUG, 'BrowserNodes.render')

    const classes = this.props.classes

    return (<List className={classes.browserList}>
      {
        this.props.nodes.map(node => {
          return (<div key={node.id + '-parent'}>
            <ListItem
              button
              key={node.id}
              selected={node.selected}
              style={{paddingLeft: this.props.level * 20 + 'px'}}>
              {this.renderIcon(node)}
              {this.renderText(node)}
            </ListItem>
            {node.children && node.children.length > 0 &&
            <Collapse key={node.id + '-collapse'} in={node.expanded} mountOnEnter={true} unmountOnExit={true}>
              <BrowserNodes {...this.props} nodes={node.children} level={this.props.level + 1} />
            </Collapse>}
          </div>)
        })
      }
    </List>)
  }

  renderIcon(node){
    if(node.children && node.children.length > 0){
      if(node.expanded){
        return (<ListItemIcon><ExpandMoreIcon onClick={() => this.props.nodeCollapsed(node.id)}/></ListItemIcon>)
      }else{
        return (<ListItemIcon><ChevronRightIcon onClick={() => this.props.nodeExpanded(node.id)}/></ListItemIcon>)
      }
    }else{
      return null
    }
  }

  renderText(node){
    logger.log(logger.DEBUG, 'BrowserNode.renderText "' + node.text + '"')
    return <ListItemText primary={node.text} inset={true} onClick={() => this.props.nodeSelected(node.id)} />
  }

}

export default withStyles(styles)(BrowserNodes)
