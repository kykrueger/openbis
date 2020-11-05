import React from 'react'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import GridContainer from '@src/js/components/common/grid/GridContainer.jsx'
import LinkToObject from '@src/js/components/common/form/LinkToObject.jsx'
import ids from '@src/js/common/consts/ids.js'
import store from '@src/js/store/store.js'
import actions from '@src/js/store/actions/actions.js'
import pages from '@src/js/common/consts/pages.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@src/js/services/openbis.js'
import logger from '@src/js/common/logger.js'

class UserSearch extends React.Component {
  constructor(props) {
    super(props)

    this.state = {
      loaded: false
    }

    this.load = this.load.bind(this)
  }

  componentDidMount() {
    this.load().then(users => {
      this.setState(() => ({
        users,
        loaded: true
      }))
    })
  }

  load() {
    let query = this.props.objectId

    let criteria = new openbis.PersonSearchCriteria()
    let fo = new openbis.PersonFetchOptions()

    criteria.withOrOperator()
    criteria.withUserId().thatContains(query)
    criteria.withFirstName().thatContains(query)
    criteria.withLastName().thatContains(query)

    return openbis
      .searchPersons(criteria, fo)
      .then(result => {
        return result.objects.map(user => ({
          ...user,
          id: user.userId
        }))
      })
      .catch(error => {
        store.dispatch(actions.errorChange(error))
      })
  }

  render() {
    logger.log(logger.DEBUG, 'Search.render')

    if (!this.state.loaded) {
      return null
    }

    return (
      <GridContainer>
        <Grid
          id={ids.USERS_GRID_ID}
          header={'Search: ' + this.props.objectId}
          columns={[
            {
              name: 'userId',
              label: 'UserId',
              sort: 'asc',
              getValue: ({ row }) => row.userId,
              renderValue: ({ row }) => (
                <LinkToObject
                  page={pages.USERS}
                  object={{ type: objectTypes.USER, id: row.userId }}
                >
                  {row.userId}
                </LinkToObject>
              )
            },
            {
              name: 'firstName',
              label: 'First Name',
              getValue: ({ row }) => row.firstName
            },
            {
              name: 'lastName',
              label: 'Last Name',
              getValue: ({ row }) => row.lastName
            }
          ]}
          rows={this.state.users}
        />
      </GridContainer>
    )
  }
}

export default UserSearch
