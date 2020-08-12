import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import logger from '@src/js/common/logger.js'
import pages from '@src/js/common/consts/pages.js'
import objectType from '@src/js/common/consts/objectType.js'

import Content from '@src/js/components/common/content/Content.jsx'
import ContentTab from '@src/js/components/common/content/ContentTab.jsx'

import UserBrowser from './browser/UserBrowser.jsx'
import UserSearch from './search/UserSearch.jsx'
import UserForm from './form/UserForm.jsx'
import UserGroupForm from './form/UserGroupForm.jsx'

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
    if (object.type === objectType.USER) {
      return <UserForm objectId={object.id} />
    } else if (object.type === objectType.GROUP) {
      return <UserGroupForm objectId={object.id} />
    } else if (object.type === objectType.SEARCH) {
      return <UserSearch objectId={object.id} />
    }
  }

  renderTab(tab) {
    const { object } = tab

    const prefixes = {
      [objectType.USER]: 'User: ',
      [objectType.GROUP]: 'Group: ',
      [objectType.SEARCH]: 'Search: '
    }

    return <ContentTab prefix={prefixes[object.type]} tab={tab} />
  }
}

export default withStyles(styles)(Users)
