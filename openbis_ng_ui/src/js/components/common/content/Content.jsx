import _ from 'lodash'
import React from 'react'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import ErrorBoundary from '@src/js/components/common/error/ErrorBoundary.jsx'
import ContentTabs from '@src/js/components/common/content/ContentTabs.jsx'
import selectors from '@src/js/store/selectors/selectors.js'
import actions from '@src/js/store/actions/actions.js'
import util from '@src/js/common/util.js'
import logger from '@src/js/common/logger.js'

const styles = {
  container: {
    display: 'flex',
    flexDirection: 'column',
    flex: 1,
    width: '100px',
    zIndex: 200,
    overflow: 'auto'
  },
  component: {
    height: 0,
    flex: '1 1 100%',
    overflow: 'auto'
  },
  visible: {
    display: 'block'
  },
  hidden: {
    display: 'none'
  }
}

function mapStateToProps() {
  return (state, ownProps) => {
    return {
      openTabs: selectors.getOpenTabs(state, ownProps.page),
      selectedTab: selectors.getSelectedTab(state, ownProps.page)
    }
  }
}

function mapDispatchToProps(dispatch, ownProps) {
  return {
    tabSelect: tab => {
      dispatch(
        actions.objectOpen(ownProps.page, tab.object.type, tab.object.id)
      )
    },
    tabClose: tab => {
      dispatch(
        actions.objectClose(ownProps.page, tab.object.type, tab.object.id)
      )
    }
  }
}

class Content extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'Content.render')

    const classes = this.props.classes

    return (
      <div className={classes.container}>
        <ContentTabs
          tabs={this.props.openTabs}
          selectedTab={this.props.selectedTab}
          tabSelect={this.props.tabSelect}
          tabClose={this.props.tabClose}
          renderTab={this.props.renderTab}
        />
        {this.props.openTabs.map(openTab => {
          let ObjectComponent = this.props.renderComponent(openTab)
          if (ObjectComponent) {
            let visible = _.isEqual(openTab, this.props.selectedTab)
            return (
              <div
                key={openTab.id}
                className={util.classNames(
                  classes.component,
                  visible ? classes.visible : classes.hidden
                )}
              >
                <ErrorBoundary>{ObjectComponent}</ErrorBoundary>
              </div>
            )
          }
        })}
      </div>
    )
  }
}

export default _.flow(
  connect(mapStateToProps, mapDispatchToProps),
  withStyles(styles)
)(Content)
