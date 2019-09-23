import React from 'react'
import {withStyles} from '@material-ui/core/styles'
import logger from '../../common/logger.js'
import * as pages from '../../common/consts/pages.js'
import * as objectType from '../../common/consts/objectType.js'

import Browser from '../common/browser/Browser.jsx'
import Content from '../common/content/Content.jsx'

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
