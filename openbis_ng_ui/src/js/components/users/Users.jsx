import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import logger from '@src/js/common/logger.js'
import pages from '@src/js/common/consts/pages.js'
import objectType from '@src/js/common/consts/objectType.js'

import Browser from '@src/js/components/common/browser/Browser.jsx'
import Content from '@src/js/components/common/content/Content.jsx'

import User from './user/User.jsx'
import Group from './group/Group.jsx'
import Search from './search/Search.jsx'

const styles = () => ({
  container: {
    display: 'flex',
    width: '100%'
  }
})

const objectTypeToComponent = {
  [objectType.USER]: User,
  [objectType.GROUP]: Group,
  [objectType.SEARCH]: Search
}

class Users extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'Users.render')

    const classes = this.props.classes

    return (
      <div className={classes.container}>
        <Browser page={pages.USERS} />
        <Content
          page={pages.USERS}
          objectTypeToComponent={objectTypeToComponent}
        />
      </div>
    )
  }
}

export default withStyles(styles)(Users)
