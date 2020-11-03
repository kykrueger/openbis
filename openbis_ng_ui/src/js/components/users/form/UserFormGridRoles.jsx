import React from 'react'
import RolesGrid from '@src/js/components/users/form/common/RolesGrid.jsx'
import ids from '@src/js/common/consts/ids.js'
import logger from '@src/js/common/logger.js'

class UserFormGridRoles extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'UserFormGridRoles.render')

    return <RolesGrid {...this.props} id={ids.USER_ROLES_GRID_ID} />
  }
}

export default UserFormGridRoles
