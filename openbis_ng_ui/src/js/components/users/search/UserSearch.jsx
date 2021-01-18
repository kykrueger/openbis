import _ from 'lodash'
import autoBind from 'auto-bind'
import React from 'react'
import GridContainer from '@src/js/components/common/grid/GridContainer.jsx'
import UsersGrid from '@src/js/components/users/common/UsersGrid.jsx'
import UserGroupsGrid from '@src/js/components/users/common/UserGroupsGrid.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import ids from '@src/js/common/consts/ids.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import store from '@src/js/store/store.js'
import actions from '@src/js/store/actions/actions.js'
import openbis from '@src/js/services/openbis.js'
import util from '@src/js/common/util.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

class UserSearch extends React.Component {
  constructor(props) {
    super(props)
    autoBind(this)

    this.state = {
      loaded: false,
      selection: null
    }
  }

  componentDidMount() {
    this.load()
  }

  async load() {
    try {
      await Promise.all([this.loadUsers(), this.loadUserGroups()])
      this.setState(() => ({
        loaded: true
      }))
    } catch (error) {
      store.dispatch(actions.errorChange(error))
    }
  }

  async loadUsers() {
    if (!this.shouldLoad(objectTypes.USER)) {
      return
    }

    const fo = new openbis.PersonFetchOptions()
    fo.withSpace()

    const result = await openbis.searchPersons(
      new openbis.PersonSearchCriteria(),
      fo
    )

    const users = util
      .filter(result.objects, this.props.searchText, [
        'userId',
        'firstName',
        'lastName'
      ])
      .map(object => ({
        id: _.get(object, 'userId'),
        userId: FormUtil.createField({ value: _.get(object, 'userId') }),
        firstName: FormUtil.createField({ value: _.get(object, 'firstName') }),
        lastName: FormUtil.createField({ value: _.get(object, 'lastName') }),
        email: FormUtil.createField({ value: _.get(object, 'email') }),
        space: FormUtil.createField({ value: _.get(object, 'space.code') }),
        active: FormUtil.createField({ value: _.get(object, 'active') })
      }))

    this.setState({
      users
    })
  }

  async loadUserGroups() {
    if (!this.shouldLoad(objectTypes.USER_GROUP)) {
      return
    }

    const result = await openbis.searchAuthorizationGroups(
      new openbis.AuthorizationGroupSearchCriteria(),
      new openbis.AuthorizationGroupFetchOptions()
    )

    const userGroups = util
      .filter(result.objects, this.props.searchText, ['code', 'description'])
      .map(object => ({
        id: _.get(object, 'code'),
        code: FormUtil.createField({ value: _.get(object, 'code') }),
        description: FormUtil.createField({
          value: _.get(object, 'description')
        })
      }))

    this.setState({
      userGroups
    })
  }

  shouldLoad(objectType) {
    return this.props.objectType === objectType || !this.props.objectType
  }

  handleClickContainer() {
    this.setState({
      selection: null
    })
  }

  handleSelectedRowChange(objectType) {
    return row => {
      if (row) {
        this.setState({
          selection: {
            type: objectType,
            id: row.id
          }
        })
      }
    }
  }

  getSelectedRowId(objectType) {
    const { selection } = this.state
    return selection && selection.type === objectType ? selection.id : null
  }

  render() {
    logger.log(logger.DEBUG, 'UserSearch.render')

    if (!this.state.loaded) {
      return null
    }

    return (
      <GridContainer onClick={this.handleClickContainer}>
        {this.renderNoResultsFoundMessage()}
        {this.renderUsers()}
        {this.renderUserGroups()}
      </GridContainer>
    )
  }

  renderNoResultsFoundMessage() {
    const { objectType } = this.props
    const { users = [], userGroups = [] } = this.state

    if (!objectType && users.length === 0 && userGroups.length === 0) {
      return (
        <Message type='info'>{messages.get(messages.NO_RESULTS_FOUND)}</Message>
      )
    } else {
      return null
    }
  }

  renderUsers() {
    if (this.shouldRender(objectTypes.USER, this.state.users)) {
      return (
        <UsersGrid
          id={ids.USERS_GRID_ID}
          rows={this.state.users}
          onSelectedRowChange={this.handleSelectedRowChange(objectTypes.USER)}
          selectedRowId={this.getSelectedRowId(objectTypes.USER)}
        />
      )
    } else {
      return null
    }
  }

  renderUserGroups() {
    if (this.shouldRender(objectTypes.USER_GROUP, this.state.userGroups)) {
      return (
        <UserGroupsGrid
          id={ids.USER_GROUPS_GRID_ID}
          rows={this.state.userGroups}
          onSelectedRowChange={this.handleSelectedRowChange(
            objectTypes.USER_GROUP
          )}
          selectedRowId={this.getSelectedRowId(objectTypes.USER_GROUP)}
        />
      )
    } else {
      return null
    }
  }

  shouldRender(objectType, types) {
    return this.props.objectType === objectType || (types && types.length > 0)
  }
}

export default UserSearch
