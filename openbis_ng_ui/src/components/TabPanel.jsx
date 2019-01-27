import React from 'react'
import {connect} from 'react-redux'
import EntityDetails from './database/EntityDetails.jsx'
import TabContainer from './TabContainer.jsx'
import TabContent from './TabContent.jsx'
import actions from '../reducer/actions.js'
import {getTabState, getTabEntity} from '../reducer/selectors.js'

/**
 * This component at the moment only makes tabs for entities.
 * In the future, it should be extended for other kinds of tabs
 * (settings forms etc.).
 */

function mapDispatchToProps(dispatch) {
  return {
    selectEntity: (entityPermId, entityTypeId) => dispatch(actions.selectEntity(entityPermId, entityTypeId)),
    closeEntity: (e, entityPermId, entityTypeId) => {
      e.stopPropagation()
      dispatch(actions.closeEntity(entityPermId, entityTypeId))
    }
  }
}


function mapStateToProps(state) {
  let tabState = getTabState(state)
  return {
    openEntities: tabState.openEntities.entities
      .map(entity => getTabEntity(state, entity))
      .filter(entity => entity),
    selectedEntity: tabState.openEntities.selectedEntity,
    dirtyEntities: tabState.dirtyEntities,
  }
}


class TabPanel extends React.Component {

  render() {
    if (this.props.openEntities.length === 0) {
      return null
    }

    return (
      <TabContainer selectedKey={this.props.selectedEntity.permId}>
        {
          this.props.openEntities.map(entity => {
            return (
              <TabContent
                key={entity.permId.permId}
                name={entity.code}
                dirty={this.props.dirtyEntities.indexOf(entity.permId.permId) > -1}
                onSelect={() => {
                  this.props.selectEntity(entity.permId.permId, entity['@type'])
                }}
                onClose={(e) => {
                  this.props.closeEntity(e, entity.permId.permId, entity['@type'])
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
