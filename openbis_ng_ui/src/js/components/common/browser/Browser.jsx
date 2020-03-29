import _ from 'lodash'
import React from 'react'
import { connect } from 'react-redux'
import { Resizable } from 're-resizable'
import { withStyles } from '@material-ui/core/styles'
import Paper from '@material-ui/core/Paper'
import FilterField from '@src/js/components/common/form/FilterField.jsx'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import selectors from '@src/js/store/selectors/selectors.js'
import logger from '@src/js/common/logger.js'

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
  const getSelectedObject = selectors.createGetSelectedObject()
  return (state, ownProps) => {
    return {
      selectedObject: getSelectedObject(state, ownProps.controller.getPage())
    }
  }
}

class Browser extends React.PureComponent {
  constructor(props) {
    super(props)

    this.state = {}

    this.controller = props.controller
    this.controller.init(new ComponentContext(this))
  }

  componentDidMount() {
    this.controller.load()
  }

  componentDidUpdate(prevProps) {
    if (this.props.selectedObject !== prevProps.selectedObject) {
      this.controller.objectSelect(this.props.selectedObject)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'Browser.render')

    const { controller } = this

    if (!controller.getLoaded()) {
      return null
    }

    const { classes } = this.props

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
            filter={controller.getFilter()}
            filterChange={controller.filterChange}
          />
          <div className={classes.nodes}>
            <BrowserNodes
              controller={controller}
              nodes={controller.getNodes()}
              level={0}
            />
          </div>
        </Paper>
      </Resizable>
    )
  }
}

export default _.flow(connect(mapStateToProps), withStyles(styles))(Browser)
