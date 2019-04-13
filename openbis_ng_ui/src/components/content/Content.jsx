import React from 'react'
import ContentTabs from './ContentTabs.jsx'
import {connect} from 'react-redux'
import logger from '../../common/logger.js'
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

function mapStateToProps(state){
  let currentPage = selectors.getCurrentPage(state)
  return {
    currentPage: currentPage,
    openObjects: selectors.getOpenObjects(state, currentPage),
    selectedObject: selectors.getSelectedObject(state, currentPage)
  }
}

class Content extends React.Component {

  constructor(props){
    super(props)
    this.objectSelect = this.objectSelect.bind(this)
    this.objectClose = this.objectClose.bind(this)
  }

  objectSelect(type, id){
    this.props.dispatch(actions.objectOpen(this.props.currentPage, type, id))
  }

  objectClose(type, id){
    this.props.dispatch(actions.objectClose(this.props.currentPage, type, id))
  }

  render() {
    logger.log(logger.DEBUG, 'Content.render')

    let ObjectContent = this.props.selectedObject ? objectTypeToComponent[this.props.selectedObject.type] : null

    return (
      <div>
        <ContentTabs
          objects={this.props.openObjects}
          selectedObject={this.props.selectedObject}
          objectSelect={this.objectSelect}
          objectClose={this.objectClose} />
        {ObjectContent &&
          <ObjectContent objectId={this.props.selectedObject.id} />
        }
      </div>
    )
  }

}

export default connect(mapStateToProps, null)(Content)
