import React from 'react'
import {connect} from 'react-redux'
import ListItemText from '@material-ui/core/ListItemText'

import BrowserList from './BrowserList.jsx'
import actions from '../reducer/actions.js'
import {getTabState} from '../reducer/selectors.js'


function mapDispatchToProps(dispatch) {
  return {
    selectNode: node => {
      if (node.permId) {
        dispatch(actions.selectEntity(node.permId))
      }
    }
  }
}

function mapStateToProps(state) {
  let tabState = getTabState(state)
  return {
    nodes: tabState.browser.nodes,
    selectedNodeId: tabState.openEntities.selectedEntity
  }
}

class Browser extends React.Component {

  render() {
    return (
      <BrowserList
        nodes={this.props.nodes}
        level={0}
        selectedNodeId={this.props.selectedNodeId}
        onSelect={this.props.selectNode}
        renderNode={node => {
          return (<ListItemText inset secondary={node.id}/>)
        }}
      />
    )
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(Browser)
