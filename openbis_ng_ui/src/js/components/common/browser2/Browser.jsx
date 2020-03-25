import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import FilterField from '@src/js/components/common/form/FilterField.jsx'
import logger from '@src/js/common/logger.js'

import BrowserController from './BrowserController.js'
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

class Browser extends React.PureComponent {
  constructor(props) {
    super(props)

    this.state = {}

    if (props.controller) {
      this.controller = props.controller
    } else {
      this.controller = new BrowserController(() => {
        return this.state
      }, this.setState.bind(this))
    }
  }

  componentDidMount() {
    this.controller.init()
    this.controller.load()
  }

  render() {
    logger.log(logger.DEBUG, 'Browser2.render')

    const { controller } = this

    if (!controller.getLoaded()) {
      return null
    }

    return (
      <div>
        <FilterField
          filter={controller.getFilter()}
          filterChange={controller.filterChange}
        />
        <BrowserNodes
          controller={controller}
          nodes={controller.getNodes()}
          level={0}
        />
      </div>
    )
  }
}

export default withStyles(styles)(Browser)
