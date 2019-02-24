import React from 'react'
import {connect} from 'react-redux'
import ListItemText from '@material-ui/core/ListItemText'

import BrowserList from './BrowserList.jsx'
import {selectEntity} from '../store/actions/page.js'
import {getTabState} from '../store/selectors/selectors.js'

function mapDispatchToProps(dispatch) {
  return {
    selectNode: node => {
      if (node.selectable) {
        dispatch(selectEntity(node.permId, node.type))
      }
    }
  }
}

function mapStateToProps(state) {
  let tabState = getTabState(state)
  let selectedEntity = tabState.openEntities.selectedEntity
  return {
    nodes: tabState.browser.nodes,
    selectedNodeId: selectedEntity ? selectedEntity.type + '#' + selectedEntity.permId : null
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
          return (<ListItemText inset secondary={node.text}/>)
        }}
      />
    )
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(Browser)
