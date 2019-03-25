import React from 'react'
import {connect} from 'react-redux'
import store from '../../store/store.js'
import * as selectors from '../../store/selectors/selectors.js'
import * as actions from '../../store/actions/actions.js'

import Loading from '../loading/Loading.jsx'
import BrowserFilter from './BrowserFilter.jsx'
import BrowserNodes from './BrowserNodes.jsx'

function getCurrentPage(){
  return selectors.getCurrentPage(store.getState())
}

function mapStateToProps(state){
  let currentPage = getCurrentPage()
  return {
    currentPage: currentPage,
    initialized: selectors.getBrowserInitialized(state, currentPage),
    filter: selectors.getBrowserFilter(state, currentPage),
    nodes: selectors.getBrowserNodes(state, currentPage)
  }
}

function mapDispatchToProps(dispatch){
  return {
    init: (page) => { dispatch(actions.browserInit(page)) },
    release: (page) => { dispatch(actions.browserRelease(page)) },
    filterChanged: (event) => { dispatch(actions.browserFilterChanged(getCurrentPage(), event.currentTarget.value)) },
    nodeSelected: (id) => { dispatch(actions.browserNodeSelected(getCurrentPage(), id)) },
    nodeExpanded: (id) => { dispatch(actions.browserNodeExpanded(getCurrentPage(), id)) },
    nodeCollapsed: (id) => { dispatch(actions.browserNodeCollapsed(getCurrentPage(), id)) }
  }
}

class Browser extends React.PureComponent {

  componentDidMount(){
    this.props.init(this.props.currentPage)
  }

  componentDidUpdate(previousProps){
    if(this.props.currentPage !== previousProps.currentPage){
      this.props.release(previousProps.currentPage)
      this.props.init(this.props.currentPage)
    }
  }

  render() {
    return (
      <Loading loading={!this.props.initialized}>
        <BrowserFilter
          filter={this.props.filter}
          filterChanged={this.props.filterChanged}
        />
        <BrowserNodes
          nodes={this.props.nodes}
          nodeSelected={this.props.nodeSelected}
          nodeExpanded={this.props.nodeExpanded}
          nodeCollapsed={this.props.nodeCollapsed}
          level={0}
        />
      </Loading>)
  }

}

export default connect(mapStateToProps, mapDispatchToProps)(Browser)
