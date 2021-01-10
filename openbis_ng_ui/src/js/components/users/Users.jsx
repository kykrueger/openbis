import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import logger from '@src/js/common/logger.js'
import pages from '@src/js/common/consts/pages.js'
import objectType from '@src/js/common/consts/objectType.js'

import Content from '@src/js/components/common/content/Content.jsx'
import ContentTab from '@src/js/components/common/content/ContentTab.jsx'

import UserBrowser from '@src/js/components/users/browser/UserBrowser.jsx'
import UserSearch from '@src/js/components/users/search/UserSearch.jsx'
import UserForm from '@src/js/components/users/form/UserForm.jsx'
import UserGroupForm from '@src/js/components/users/form/UserGroupForm.jsx'

const styles = () => ({
  container: {
    display: 'flex',
    width: '100%'
  }
})

class Users extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'Users.render')

    const classes = this.props.classes

    return (
      <div className={classes.container}>
        <UserBrowser />
        <Content
          page={pages.USERS}
          renderComponent={this.renderComponent}
          renderTab={this.renderTab}
        />
      </div>
    )
  }

  renderComponent(tab) {
    const { object } = tab
    if (
      object.type === objectType.USER ||
      object.type === objectType.NEW_USER
    ) {
      return <UserForm object={object} />
    } else if (
      object.type === objectType.USER_GROUP ||
      object.type === objectType.NEW_USER_GROUP
    ) {
      return <UserGroupForm object={object} />
    } else if (object.type === objectType.SEARCH) {
      return <UserSearch searchText={object.id} />
    } else if (object.type === objectType.OVERVIEW) {
      return <UserSearch objectType={object.id} />
    }
  }

  renderTab(tab) {
    const { object, changed } = tab

    let label = null

    if (object.type === objectType.OVERVIEW) {
      const labels = {
        [objectType.USER]: 'Users',
        [objectType.USER_GROUP]: 'Groups'
      }
      label = labels[object.id]
    } else {
      const prefixes = {
        [objectType.USER]: 'User: ',
        [objectType.USER_GROUP]: 'Group: ',
        [objectType.NEW_USER]: 'New User ',
        [objectType.NEW_USER_GROUP]: 'New Group ',
        [objectType.SEARCH]: 'Search: '
      }
      label = prefixes[object.type] + object.id
    }

    return <ContentTab label={label} changed={changed} />
  }
}

export default withStyles(styles)(Users)
