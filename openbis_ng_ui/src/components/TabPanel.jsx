import React from 'react'
import {connect} from 'react-redux'
import EntityDetails from './database/EntityDetails.jsx'
import TabContainer from './TabContainer.jsx'
import TabContent from './TabContent.jsx'
import actions from '../reducer/actions.js'


/**
 * This component at the moment only makes tabs for entities.
 * In the future, it should be extended for other kinds of tabs
 * (settings forms etc.).
 */

function mapDispatchToProps(dispatch) {
    return {
        selectEntity: (entityPermId) => dispatch(actions.selectEntity(entityPermId)),
        closeEntity: (e, entityPermId) => {
            e.stopPropagation()
            dispatch(actions.closeEntity(entityPermId))
        }
    }
}


function mapStateToProps(state) {
    const selectedEntity = state.openEntities.selectedEntity
    const spaces = state.database.spaces
    return {
        openEntities: state.openEntities.entities
            .filter(permId => permId in spaces)
            .map(permId => spaces[permId]),
        selectedEntity: selectedEntity,
        dirtyEntities: state.dirtyEntities,
    }
}


class TabPanel extends React.Component {

    render() {
        if (this.props.openEntities.length === 0) {
            return null
        }
        return (
            <TabContainer selectedKey={this.props.selectedEntity}>
                {
                    this.props.openEntities.map(entity => {
                        return (
                            <TabContent
                                key={entity.permId.permId}
                                name={entity.code}
                                dirty={this.props.dirtyEntities.indexOf(entity.permId.permId) > -1}
                                onSelect={() => {
                                    this.props.selectEntity(entity.permId.permId)
                                }}
                                onClose={(e) => {
                                    this.props.closeEntity(e, entity.permId.permId)
                                }}
                            >
                                <EntityDetails
                                    entity={entity}
                                />
                            </TabContent>
                        )
                    })
                }
            </TabContainer>
        )
    }
}

export default connect(mapStateToProps, mapDispatchToProps)(TabPanel)
