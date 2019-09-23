import React from 'react'
import {withStyles} from '@material-ui/core/styles'
import List from '@material-ui/core/List'
import BrowserNode from './BrowserNode.jsx'
import logger from '../../../common/logger.js'
import * as util from '../../../common/util.js'

const styles = () => ({
  container: {
    flex: '1 1 100%',
    overflowY: 'overlay'
  },
  list: {
    paddingTop: '0',
    paddingBottom: '0'
  }
})

class BrowserNodes extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'BrowserNodes.render')

    const classes = this.props.classes

    return (
      <List
        className={util.classNames(
          classes.list,
          this.props.level === 0 ? classes.container : null
        )}
      >
        {this.props.nodes.map(node => {
          return (
            <BrowserNode
              key={node.id}
              node={node}
              nodeSelect={this.props.nodeSelect}
              nodeCollapse={this.props.nodeCollapse}
              nodeExpand={this.props.nodeExpand}
              level={this.props.level}
            />
          )
        })}
      </List>
    )
  }
}

export default withStyles(styles)(BrowserNodes)
