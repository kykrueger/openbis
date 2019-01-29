import React from 'react'
import {connect} from 'react-redux'
import {withStyles} from '@material-ui/core/styles'
import List from '@material-ui/core/List'
import ListItem from '@material-ui/core/ListItem'
import ListItemIcon from '@material-ui/core/ListItemIcon'
import ChevronRightIcon from '@material-ui/icons/ChevronRight'
import ExpandMoreIcon from '@material-ui/icons/ExpandMore'
import HourglassEmptyIcon from '@material-ui/icons/HourglassEmpty'
import Collapse from '@material-ui/core/Collapse';
import PropTypes from 'prop-types'


import actions from '../reducer/actions.js'


/*eslint-disable-next-line no-unused-vars*/
const styles = theme => ({
  noPadding: {
    paddingTop: '0',
    paddingBottom: '0',
  },
})


function mapDispatchToProps(dispatch) {
  return {
    expandNode: (e, node) => {
      e.stopPropagation()
      dispatch(actions.expandNode(node))
    },
    collapseNode: (e, node) => {
      e.stopPropagation()
      dispatch(actions.collapseNode(node))
    },
  }
}

class BrowserList extends React.Component {

  icon(node) {
    if (node.expanded) {
      return (<ListItemIcon><ExpandMoreIcon onClick={(e) => this.props.collapseNode(e, node)}/></ListItemIcon>)
    } else if (node.loaded === false || (node.children !== null && node.children.length > 0)) {
      return (<ListItemIcon><ChevronRightIcon onClick={(e) => this.props.expandNode(e, node)}/></ListItemIcon>)
    } else {
      return null
    }
  }

  render() {
    const classes = this.props.classes

    return (
      <List className={classes.noPadding}>
        {
          this.props.nodes.map(node =>
            (!node.filterable || node.filtered) &&
            <div key={node.id}>
              <ListItem
                button
                selected={this.props.selectedNodeId === node.id}
                key={node.id}
                onClick={() => this.props.onSelect(node)}
                style={{paddingLeft: '' + this.props.level * 20 + 'px'}}>
                {this.icon(node)}
                {node.loading &&
                <ListItemIcon>
                  <HourglassEmptyIcon/>
                </ListItemIcon>
                }
                {this.props.renderNode(node)}
              </ListItem>
              <Collapse key={node.id + '-collapse'} in={node.expanded}>
                <BrowserList {...this.props} nodes={node.children} level={this.props.level + 1}/>
              </Collapse>
            </div>
          )
        }
      </List>
    )
  }
}

BrowserList.propTypes = {
  renderNode: PropTypes.func.isRequired,
}

export default connect(null, mapDispatchToProps)(withStyles(styles)(BrowserList))
