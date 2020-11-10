import React from 'react'
import GroupsGrid from '@src/js/components/users/common/GroupsGrid.jsx'
import ids from '@src/js/common/consts/ids.js'
import logger from '@src/js/common/logger.js'

export default class UserFormGridGroups extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'UserFormGridGroups.render')

    return <GroupsGrid {...this.props} id={ids.USER_USER_GROUPS_GRID_ID} />
  }
}
