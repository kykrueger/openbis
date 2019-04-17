import React from 'react'
import {withStyles} from '@material-ui/core/styles'
import logger from '../../common/logger.js'
import * as pages from '../../store/consts/pages.js'
import * as objectType from '../../store/consts/objectType.js'

import Browser from '../common/browser/Browser.jsx'
import Content from '../common/content/Content.jsx'

import User from './user/User.jsx'
import Group from './group/Group.jsx'

const styles = () => ({
  pageContainer: {
    display: 'flex'
  },
})

const objectTypeToComponent = {
  [objectType.USER]: User,
  [objectType.GROUP]: Group
}

class Users extends React.Component {

  render() {
    logger.log(logger.DEBUG, 'Users.render')

    const classes = this.props.classes

    return (
      <div className={classes.pageContainer}>
        <Browser page={pages.USERS}/>
        <Content page={pages.USERS} objectTypeToComponent={objectTypeToComponent}/>
      </div>
    )
  }

}

export default withStyles(styles)(Users)
