import React from 'react'
import {withStyles} from '@material-ui/core/styles'
import List from '@material-ui/core/List'
import BrowserNode from './BrowserNode.jsx'
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
          return <BrowserNode {...this.props} key={node.id} node={node} level={this.props.level} />
        })
      }
    </List>)
  }

}

export default withStyles(styles)(BrowserNodes)
