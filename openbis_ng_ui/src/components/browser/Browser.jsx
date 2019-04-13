import React from 'react'
import {connect} from 'react-redux'
import logger from '../../common/logger.js'
import * as selectors from '../../store/selectors/selectors.js'
import * as actions from '../../store/actions/actions.js'

import Loading from '../loading/Loading.jsx'
import BrowserFilter from './BrowserFilter.jsx'
import BrowserNodes from './BrowserNodes.jsx'

function mapStateToProps(state){
  let currentPage = selectors.getCurrentPage(state)
  return {
    currentPage: currentPage,
    filter: selectors.getBrowserFilter(state, currentPage),
    nodes: selectors.getBrowserNodes(state, currentPage)
  }
}

class Browser extends React.PureComponent {

  constructor(props){
    super(props)
    this.init = this.init.bind(this)
    this.release = this.release.bind(this)
    this.filterChange = this.filterChange.bind(this)
    this.nodeSelect = this.nodeSelect.bind(this)
    this.nodeExpand = this.nodeExpand.bind(this)
    this.nodeCollapse = this.nodeCollapse.bind(this)
  }

  componentDidMount(){
    this.init(this.props.currentPage)
  }

  componentDidUpdate(previousProps){
    if(this.props.currentPage !== previousProps.currentPage){
      this.release(previousProps.currentPage)
      this.init(this.props.currentPage)
    }
  }

  init(page){
    this.props.dispatch(actions.browserInit(page))
  }

  release(page){
    this.props.dispatch(actions.browserRelease(page))
  }

  filterChange(event){
    this.props.dispatch(actions.browserFilterChange(this.props.currentPage, event.currentTarget.value))
  }

  nodeSelect(id){
    this.props.dispatch(actions.browserNodeSelect(this.props.currentPage, id))
  }

  nodeExpand(id){
    this.props.dispatch(actions.browserNodeExpand(this.props.currentPage, id))
  }

  nodeCollapse(id){
    this.props.dispatch(actions.browserNodeCollapse(this.props.currentPage, id))
  }

  render() {
    logger.log(logger.DEBUG, 'Browser.render')

    return (
      <Loading loading={this.props.loading}>
        <BrowserFilter
          filter={this.props.filter}
          filterChange={this.filterChange}
        />
        <BrowserNodes
          nodes={this.props.nodes}
          nodeSelect={this.nodeSelect}
          nodeExpand={this.nodeExpand}
          nodeCollapse={this.nodeCollapse}
          level={0}
        />
      </Loading>)
  }

}

export default connect(mapStateToProps, null)(Browser)
