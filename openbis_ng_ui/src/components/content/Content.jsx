import React from 'react'
import ContentTabs from './ContentTabs.jsx'
import ContentTab from './ContentTab.jsx'
import {connect} from 'react-redux'
import logger from '../../common/logger.js'
import store from '../../store/store.js'
import * as selectors from '../../store/selectors/selectors.js'
import * as actions from '../../store/actions/actions.js'

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

    return (
      <div>
        <ContentTabs
          objects={this.props.openObjects}
          selectedObject={this.props.selectedObject}
          objectSelect={this.props.objectSelect}
          objectClose={this.props.objectClose} />
        {this.props.selectedObject &&
          <ContentTab
            object={this.props.selectedObject} />
        }
      </div>
    )
  }

}

export default connect(mapStateToProps, mapDispatchToProps)(Content)
