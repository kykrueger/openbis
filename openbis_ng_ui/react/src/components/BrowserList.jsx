import React from 'react'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import List from '@material-ui/core/List'
import ListItem from '@material-ui/core/ListItem'
import ListItemIcon from '@material-ui/core/ListItemIcon'
import ChevronRightIcon from '@material-ui/icons/ChevronRight'
import ExpandMoreIcon from '@material-ui/icons/ExpandMore'
import HourglassEmptyIcon from '@material-ui/icons/HourglassEmpty'
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
      return (<ExpandMoreIcon  onClick={ (e) => this.props.collapseNode(e, node) }/>)
    } else {
      return (<ChevronRightIcon  onClick={ (e) => this.props.expandNode(e, node) }/>)
    }
  }

  render() {
    const classes = this.props.classes

    return (
      <List className={classes.noPadding}>
        {
          this.props.nodes.map(node => 
            <div key={node.id}>
              <ListItem 
                button
                selected={this.props.selectedNodeId ===  node.id}
                key={node.id}
                onClick = {() => this.props.onSelect(node)}
                style={{ paddingLeft: '' + this.props.level * 20 + 'px' }}>
                <ListItemIcon>
                  {this.icon(node)}
                </ListItemIcon>
                { node.loading &&
                <ListItemIcon>
                  <HourglassEmptyIcon/>
                </ListItemIcon>
                }
                { this.props.renderNode(node) }
              </ListItem>
              {
                node.expanded &&
                <BrowserList {...this.props} nodes={node.children} level={this.props.level+1}/>
              }
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
