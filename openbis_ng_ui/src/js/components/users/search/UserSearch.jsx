import _ from 'lodash'
import autoBind from 'auto-bind'
import React from 'react'
import GridContainer from '@src/js/components/common/grid/GridContainer.jsx'
import UsersGrid from '@src/js/components/users/common/UsersGrid.jsx'
import UserGroupsGrid from '@src/js/components/users/common/UserGroupsGrid.jsx'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import ids from '@src/js/common/consts/ids.js'
import store from '@src/js/store/store.js'
import actions from '@src/js/store/actions/actions.js'
import openbis from '@src/js/services/openbis.js'
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
    Promise.all([this.loadUsers(), this.loadUserGroups()])
      .then(([users, groups]) => {
        this.setState(() => ({
          loaded: true,
          users,
          groups
        }))
      })
      .catch(error => {
        store.dispatch(actions.errorChange(error))
      })
  }

  loadUsers() {
    let query = this.props.objectId

    let criteria = new openbis.PersonSearchCriteria()
    let fo = new openbis.PersonFetchOptions()
    fo.withSpace()

    criteria.withOrOperator()
    criteria.withUserId().thatContains(query)
    criteria.withFirstName().thatContains(query)
    criteria.withLastName().thatContains(query)
    criteria.withEmail().thatContains(query)

    return openbis.searchPersons(criteria, fo).then(result => {
      return result.objects.map(user => ({
        id: _.get(user, 'userId'),
        userId: FormUtil.createField({ value: _.get(user, 'userId') }),
        firstName: FormUtil.createField({ value: _.get(user, 'firstName') }),
        lastName: FormUtil.createField({ value: _.get(user, 'lastName') }),
        email: FormUtil.createField({ value: _.get(user, 'email') }),
        space: FormUtil.createField({ value: _.get(user, 'space.code') }),
        active: FormUtil.createField({ value: _.get(user, 'active') })
      }))
    })
  }

  loadUserGroups() {
    let query = this.props.objectId

    let criteria = new openbis.AuthorizationGroupSearchCriteria()
    let fo = new openbis.AuthorizationGroupFetchOptions()

    return openbis.searchAuthorizationGroups(criteria, fo).then(result => {
      return result.objects
        .filter(group => {
          return (
            (group.code &&
              group.code.toUpperCase().includes(query.toUpperCase())) ||
            (group.description &&
              group.description.toUpperCase().includes(query.toUpperCase()))
          )
        })
        .map(group => ({
          id: _.get(group, 'code'),
          code: FormUtil.createField({ value: _.get(group, 'code') }),
          description: FormUtil.createField({
            value: _.get(group, 'description')
          })
        }))
    })
  }

  handleClickContainer() {
    this.setState({
      selection: null
    })
  }

  handleSelectedUserRowChange(row) {
    if (row) {
      this.setState({
        selection: {
          type: 'user',
          id: row.id
        }
      })
    }
  }

  handleSelectedGroupRowChange(row) {
    if (row) {
      this.setState({
        selection: {
          type: 'group',
          id: row.id
        }
      })
    }
  }

  render() {
    logger.log(logger.DEBUG, 'Search.render')

    if (!this.state.loaded) {
      return null
    }

    const { selection } = this.state

    return (
      <GridContainer onClick={this.handleClickContainer}>
        <UsersGrid
          id={ids.USERS_GRID_ID}
          rows={this.state.users}
          onSelectedRowChange={this.handleSelectedUserRowChange}
          selectedRowId={
            selection && selection.type === 'user' ? selection.id : null
          }
        />
        <UserGroupsGrid
          id={ids.USER_GROUPS_GRID_ID}
          rows={this.state.groups}
          onSelectedRowChange={this.handleSelectedGroupRowChange}
          selectedRowId={
            selection && selection.type === 'group' ? selection.id : null
          }
        />
      </GridContainer>
    )
  }
}

export default UserSearch
