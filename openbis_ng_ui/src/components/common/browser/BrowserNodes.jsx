import React from 'react'
import {withStyles} from '@material-ui/core/styles'
import List from '@material-ui/core/List'
import BrowserNode from './BrowserNode.jsx'
import logger from '../../../common/logger.js'

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
          return <BrowserNode
            key={node.id}
            node={node}
            nodeSelect={this.props.nodeSelect}
            nodeCollapse={this.props.nodeCollapse}
            nodeExpand={this.props.nodeExpand}
            level={this.props.level}
          />
        })
      }
    </List>)
  }

}

export default withStyles(styles)(BrowserNodes)
