import React from 'react'
import {connect} from 'react-redux'
import ListItemText from '@material-ui/core/ListItemText'

import BrowserList from './BrowserList.jsx'
import actions from '../reducer/actions.js'


function mapDispatchToProps(dispatch) {
    return {
        selectNode: permId => dispatch(actions.selectEntity(permId)),
    }
}


function mapStateToProps(state) {
    // TODO stack tree nodes here when the final tree model is done
    if (state.mode === 'DATABASE') {
        return {
            nodes: state.databaseTreeNodes,
            selectedNodeId: state.openEntities.selectedEntity,
        }
    } else if (state.mode === 'USERS') {
        return {
            nodes: state.users.browser.nodes,
            selectedNodeId: state.users.browser.selectedNodeId
        }
    } else if (state.mode === 'TYPES') {
        return {
            nodes: state.types.browser.nodes,
            selectedNodeId: state.types.browser.selectedNodeId
        }
    } else {
        return {
            nodes: [],
            selectedNodeId: null
        }
    }
}


class Browser extends React.Component {

    render() {
        return (
            <BrowserList
                nodes={this.props.nodes}
                level={0}
                selectedNodeId={this.props.selectedNodeId}
                onSelect={node => {
                    if (node.type === 'as.dto.space.Space') this.props.selectNode(node.id)
                }}
                renderNode={node => {
                    return (<ListItemText inset secondary={node.id}/>)
                }}
            />
        )
    }
}

export default connect(mapStateToProps, mapDispatchToProps)(Browser)
