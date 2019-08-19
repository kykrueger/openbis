import React from 'react'
import Grid from '../../common/grid/Grid.jsx'
import * as ids from '../../../common/consts/ids.js'
import {facade, dto} from '../../../services/openbis.js'
import logger from '../../../common/logger.js'

class Search extends React.Component {

  constructor(props){
    super(props)

    this.load = this.load.bind(this)
  }

  load({ filter, page, pageSize }){
    let criteria = new dto.PersonSearchCriteria()
    let fo = new dto.PersonFetchOptions()

    criteria.withOrOperator()
    criteria.withUserId().thatContains(filter)
    criteria.withFirstName().thatContains(filter)
    criteria.withLastName().thatContains(filter)

    fo.count(pageSize)
    fo.from(page*pageSize)

    return facade.searchPersons(criteria, fo).then(result => {
      let objects = result.objects.map(user => ({
        ...user,
        id: user.userId
      }))
      return {
        objects,
        totalCount: result.totalCount
      }
    })
  }

  render() {
    logger.log(logger.DEBUG, 'Search.render')

    return (
      <Grid
        id={ids.USERS_GRID_ID}
        filter={this.props.objectId}
        columns={[
          {
            field: 'userId'
          },
          {
            field: 'firstName'
          },
          {
            field: 'lastName'
          }
        ]}
        data={this.load}
      />
    )
  }

}

export default Search
