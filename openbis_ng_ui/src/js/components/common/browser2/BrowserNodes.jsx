import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import List from '@material-ui/core/List'
import util from '@src/js/common/util.js'
import logger from '@src/js/common/logger.js'

import BrowserNode from './BrowserNode.jsx'

const styles = () => ({
  container: {
    flex: '1 1 100%'
  },
  list: {
    paddingTop: '0',
    paddingBottom: '0'
  }
})

class BrowserNodes extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'BrowserNodes.render')

    const { controller, classes } = this.props

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
              controller={controller}
              node={node}
              level={this.props.level}
            />
          )
        })}
      </List>
    )
  }
}

export default withStyles(styles)(BrowserNodes)
