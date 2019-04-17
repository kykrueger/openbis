import React from 'react'
import {connect} from 'react-redux'
import logger from '../../../common/logger.js'
import * as selectors from '../../../store/selectors/selectors.js'
import * as actions from '../../../store/actions/actions.js'

import BrowserFilter from './BrowserFilter.jsx'
import BrowserNodes from './BrowserNodes.jsx'

function mapStateToProps(state, ownProps){
  return {
    filter: selectors.getBrowserFilter(state, ownProps.page),
    nodes: selectors.getBrowserNodes(state, ownProps.page)
  }
}

function mapDispatchToProps(dispatch, ownProps){
  return {
    init: () => { dispatch(actions.browserInit(ownProps.page)) },
    filterChange: (event) => { dispatch(actions.browserFilterChange(ownProps.page, event.currentTarget.value)) },
    nodeSelect: (id) => { dispatch(actions.browserNodeSelect(ownProps.page, id)) },
    nodeExpand: (id) => { dispatch(actions.browserNodeExpand(ownProps.page, id)) },
    nodeCollapse: (id) => { dispatch(actions.browserNodeCollapse(ownProps.page, id)) }
  }
}

class Browser extends React.PureComponent {

  componentDidMount(){
    this.props.init()
  }

  render() {
    logger.log(logger.DEBUG, 'Browser.render')

    return (
      <div>
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
      </div>
    )
  }

}

export default connect(mapStateToProps, mapDispatchToProps)(Browser)
