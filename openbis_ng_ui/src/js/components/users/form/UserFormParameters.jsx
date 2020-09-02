import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import logger from '@src/js/common/logger.js'

import UserFormParametersUser from './UserFormParametersUser.jsx'
import UserFormParametersGroup from './UserFormParametersGroup.jsx'

const styles = () => ({})

class UserFormParameters extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'UserFormParameters.render')

    const {
      controller,
      user,
      groups,
      selection,
      selectedRow,
      mode,
      onChange,
      onSelectionChange,
      onBlur
    } = this.props

    return (
      <div>
        <UserFormParametersUser
          controller={controller}
          user={user}
          selection={selection}
          mode={mode}
          onChange={onChange}
          onSelectionChange={onSelectionChange}
          onBlur={onBlur}
        />
        <UserFormParametersGroup
          controller={controller}
          groups={groups}
          selection={selection}
          selectedRow={selectedRow}
          mode={mode}
          onChange={onChange}
          onSelectionChange={onSelectionChange}
          onBlur={onBlur}
        />
      </div>
    )
  }
}

export default withStyles(styles)(UserFormParameters)
