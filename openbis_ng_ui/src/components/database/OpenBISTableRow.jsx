import React from 'react'
import ReactDOM from 'react-dom'
import {connect} from 'react-redux'
import TableCell from '@material-ui/core/TableCell'
import TableRow from '@material-ui/core/TableRow'
import {DragSource} from 'react-dnd'
import {DropTarget} from 'react-dnd'
import DragHandle from '@material-ui/icons/DragHandle'

import actions from '../../reducer/actions.js'


function targetCollect(connect, monitor) {
    return {
        connectDropTarget: connect.dropTarget()
    }
}

function sourceCollect(connect, monitor) {
    return {
        connectDragSource: connect.dragSource(),
        isDragging: monitor.isDragging(),
        connectDragPreview: connect.dragPreview()
    }
}

const source = {
    beginDrag(props, monitor, component) {
        return {
            id: props.row.Id
        }
    },
    endDrag(props, monitor, component) {
        monitor.getDropResult().fire()
    }
}

const target = {
    drop(props, monitor, component) {
        return {
            source: monitor.getItem(),
            target: {
                id: props.row.Id
            },
            fire: () => props.moveEntity(monitor.getItem().id, props.row.Id)
        }
    }
}

function mapStateToProps(state) {
    return {
        spaces: state.database.spaces,
    }
}

function mapDispatchToProps(dispatch) {
    return {
        selectEntity: e => dispatch(actions.selectEntity(e)),
        moveEntity: (source, target) => dispatch(actions.moveEntity(source, target))
    }
}

class OpenBISTableRow extends React.Component {

    constructor(props) {
        super(props)
    }

    render() {
        const row = this.props.row
        return (
            <TableRow
                key={row.Id}
                ref={instance => {
                    this.props.connectDragPreview(ReactDOM.findDOMNode(instance))
                    this.props.connectDropTarget(ReactDOM.findDOMNode(instance))
                }}>
                <TableCell>
                    <div
                        style={{cursor: 'pointer'}}
                        ref={instance => this.props.connectDragSource(ReactDOM.findDOMNode(instance))}>
                        <DragHandle/>
                    </div>
                </TableCell>
                {Object.entries(this.props.columns).map(([col, type]) =>
                    <TableCell key={col} numeric={type === 'int'}> {row[col]}</TableCell>
                )}
            </TableRow>
        )
    }
}

export default connect(mapStateToProps, mapDispatchToProps)(DropTarget('row', target, targetCollect)(DragSource('row', source, sourceCollect)(OpenBISTableRow)))
