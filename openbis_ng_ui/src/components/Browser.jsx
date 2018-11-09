import React from 'react'
import { connect } from 'react-redux'
import ListItemText from '@material-ui/core/ListItemText'

import BrowserList from './BrowserList.jsx'
import actions from '../reducer/actions.js'


function mapDispatchToProps(dispatch) {
  return {
    selectEntity: permId => dispatch(actions.selectEntity(permId)),
  }
}


function mapStateToProps(state) {
  // TODO stack tree nodes here when the final tree model is done
  return {
    databaseTreeNodes: state.databaseTreeNodes,
    selectedEntity: state.openEntities.selectedEntity,
  }
}


class Browser extends React.Component {

  render() {
    return (
      <BrowserList 
        nodes={ this.props.databaseTreeNodes } 
        level={ 0 } 
        selectedNodeId={ this.props.selectedEntity } 
        onSelect={ node => { if (node.type === 'as.dto.space.Space') this.props.selectEntity(node.id) } }
        renderNode={ node => { return (<ListItemText secondary={node.id} />)} }
      />
    )
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(Browser)
