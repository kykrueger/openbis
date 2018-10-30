import React from 'react'
import Hidden from '@material-ui/core/Hidden'
import { withStyles } from '@material-ui/core/styles'
import { connect } from 'react-redux'
import CircularProgress from '@material-ui/core/CircularProgress'
import HTML5Backend from 'react-dnd-html5-backend'
import { DragDropContext } from 'react-dnd'
import flow from 'lodash/flow'

import Browser from './Browser.jsx'
import BrowserFilter from './BrowserFilter.jsx'
import BrowserButtons from './BrowserButtons.jsx'
import ModeBar from './ModeBar.jsx'
import TabPanel from './TabPanel.jsx'
import TopBar from './TopBar.jsx'

const drawerWidth = 400

/* eslint-disable-next-line no-unused-vars */
const styles = theme => ({
  right: {
    width: `calc(100% - ${drawerWidth + 4 + 4 + 1}px)`,
    paddingLeft: 4,
    marginLeft: drawerWidth + 5,
  },
  
  left: {
    float: 'left',
    width: drawerWidth,
    paddingRight: 4,
    borderRight: '1px dotted',
    borderColor: '#e3e5ea',
    height: '100%',
    position: 'absolute',
  },

  browser: {
    height: 'calc(100% - 160px)',
    overflow: 'auto'
  },

  topMargin: {
    marginTop: 8
  },

  loader: { 
    position: 'absolute',
    paddingTop: '15%',      
    width: '100%',
    height: '100%',
    zIndex: 1000,
    backgroundColor: '#000000',
    opacity: 0.5,
    textAlign: 'center',
  },

})

function mapStateToProps(state) {
  return {
    loading: state.loading,
  }
}

class App extends React.Component {
  
  render() {
    const classes = this.props.classes
    
    return (
      <div>
        {
          this.props.loading &&
          <div className={classes.loader}>
            <CircularProgress className={classes.progress} />
          </div>
        }
        <Hidden mdUp>
          <TopBar/>
          <div className={classes.topMargin}>
            <ModeBar/>
          </div>
          <BrowserFilter/>
          <Browser/>
          <BrowserButtons />
          <div className={classes.topMargin}>
            <TabPanel />
          </div>
        </Hidden>

        <Hidden smDown>
          <div className={classes.left}>
            <ModeBar/>
            <BrowserFilter/>
            <div className={classes.browser}>
              <Browser />
            </div>
            <BrowserButtons />
          </div>
          <div className={classes.right}>
            <TopBar />
            <div className={classes.topMargin}>
              <TabPanel />
            </div>
          </div>
        </Hidden>
      </div>
    )
  }
}

export default flow(
  connect(mapStateToProps),
  withStyles(styles),
  DragDropContext(HTML5Backend)
)(App)
