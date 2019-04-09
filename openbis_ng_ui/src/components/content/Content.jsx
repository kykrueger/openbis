import React from 'react'
import ContentTabs from './ContentTabs.jsx'
import {connect} from 'react-redux'
import logger from '../../common/logger.js'
import store from '../../store/store.js'
import * as objectType from '../../store/consts/objectType.js'
import * as selectors from '../../store/selectors/selectors.js'
import * as actions from '../../store/actions/actions.js'

import ObjectType from './objectType/ObjectType.jsx'
import CollectionType from './collectionType/CollectionType.jsx'
import DataSetType from './dataSetType/DataSetType.jsx'
import MaterialType from './materialType/MaterialType.jsx'
import User from './user/User.jsx'
import Group from './group/Group.jsx'

const objectTypeToComponent = {
  [objectType.OBJECT_TYPE]: ObjectType,
  [objectType.COLLECTION_TYPE]: CollectionType,
  [objectType.DATA_SET_TYPE]: DataSetType,
  [objectType.MATERIAL_TYPE]: MaterialType,
  [objectType.USER]: User,
  [objectType.GROUP]: Group,
}

function getCurrentPage(){
  return selectors.getCurrentPage(store.getState())
}

function mapStateToProps(state){
  let currentPage = getCurrentPage()
  return {
    openObjects: selectors.getOpenObjects(state, currentPage),
    selectedObject: selectors.getSelectedObject(state, currentPage)
  }
}

function mapDispatchToProps(dispatch){
  return {
    objectSelect: (type, id) => { dispatch(actions.objectOpen(getCurrentPage(), type, id)) },
    objectClose: (type, id) => { dispatch(actions.objectClose(getCurrentPage(), type, id)) }
  }
}

class Content extends React.Component {

  render() {
    logger.log(logger.DEBUG, 'Content.render')

    let ObjectContent = this.props.selectedObject ? objectTypeToComponent[this.props.selectedObject.type] : null

    return (
      <div>
        <ContentTabs
          objects={this.props.openObjects}
          selectedObject={this.props.selectedObject}
          objectSelect={this.props.objectSelect}
          objectClose={this.props.objectClose} />
        {ObjectContent &&
          <ObjectContent />
        }
      </div>
    )
  }

}

export default connect(mapStateToProps, mapDispatchToProps)(Content)
