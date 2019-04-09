import React from 'react'
import {connect} from 'react-redux'
import logger from '../../common/logger.js'
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
    filter: selectors.getBrowserFilter(state, currentPage),
    nodes: selectors.getBrowserNodes(state, currentPage)
  }
}

function mapDispatchToProps(dispatch){
  return {
    init: (page) => { dispatch(actions.browserInit(page)) },
    release: (page) => { dispatch(actions.browserRelease(page)) },
    filterChange: (event) => { dispatch(actions.browserFilterChange(getCurrentPage(), event.currentTarget.value)) },
    nodeSelect: (id) => { dispatch(actions.browserNodeSelect(getCurrentPage(), id)) },
    nodeExpand: (id) => { dispatch(actions.browserNodeExpand(getCurrentPage(), id)) },
    nodeCollapse: (id) => { dispatch(actions.browserNodeCollapse(getCurrentPage(), id)) }
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
    logger.log(logger.DEBUG, 'Browser.render')

    return (
      <Loading loading={this.props.loading}>
        <BrowserFilter
          filter={this.props.filter}
          filterChange={this.props.filterChange}
        />
        <BrowserNodes
          nodes={this.props.nodes}
          nodeSelect={this.props.nodeSelect}
          nodeExpand={this.props.nodeExpand}
          nodeCollapse={this.props.nodeCollapse}
          level={0}
        />
      </Loading>)
  }

}

export default connect(mapStateToProps, mapDispatchToProps)(Browser)
