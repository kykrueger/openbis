import React from 'react'
import _ from 'lodash'
import Paper from '@material-ui/core/Paper'
import { Resizable } from 're-resizable'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import logger from '../../../common/logger.js'
import * as selectors from '../../../store/selectors/selectors.js'
import * as actions from '../../../store/actions/actions.js'

import FilterField from './../form/FilterField.jsx'
import BrowserNodes from './BrowserNodes.jsx'

const styles = {
  resizable: {
    zIndex: 2000,
    position: 'relative'
  },
  paper: {
    height: '100%',
    display: 'flex',
    flexDirection: 'column'
  },
  nodes: {
    height: '100%',
    overflow: 'auto'
  }
}

function mapStateToProps() {
  const getBrowserNodes = selectors.createGetBrowserNodes()
  return (state, ownProps) => {
    return {
      filter: selectors.getBrowserFilter(state, ownProps.page),
      nodes: getBrowserNodes(state, ownProps.page)
    }
  }
}

function mapDispatchToProps(dispatch, ownProps) {
  return {
    init: () => {
      dispatch(actions.browserInit(ownProps.page))
    },
    filterChange: filter => {
      dispatch(actions.browserFilterChange(ownProps.page, filter))
    },
    nodeSelect: id => {
      dispatch(actions.browserNodeSelect(ownProps.page, id))
    },
    nodeExpand: id => {
      dispatch(actions.browserNodeExpand(ownProps.page, id))
    },
    nodeCollapse: id => {
      dispatch(actions.browserNodeCollapse(ownProps.page, id))
    }
  }
}

class Browser extends React.PureComponent {
  componentDidMount() {
    this.props.init()
  }

  render() {
    logger.log(logger.DEBUG, 'Browser.render')

    const classes = this.props.classes

    return (
      <Resizable
        defaultSize={{
          width: 300,
          height: 'auto'
        }}
        enable={{
          right: true,
          left: false,
          top: false,
          bottom: false,
          topRight: false,
          bottomRight: false,
          bottomLeft: false,
          topLeft: false
        }}
        className={classes.resizable}
      >
        <Paper square={true} elevation={3} classes={{ root: classes.paper }}>
          <FilterField
            filter={this.props.filter}
            filterChange={this.props.filterChange}
          />
          <div className={classes.nodes}>
            <BrowserNodes
              nodes={this.props.nodes}
              nodeSelect={this.props.nodeSelect}
              nodeExpand={this.props.nodeExpand}
              nodeCollapse={this.props.nodeCollapse}
              level={0}
            />
          </div>
        </Paper>
      </Resizable>
    )
  }
}

export default _.flow(
  connect(
    mapStateToProps,
    mapDispatchToProps
  ),
  withStyles(styles)
)(Browser)
