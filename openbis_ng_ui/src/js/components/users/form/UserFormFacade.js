import openbis from '@src/js/services/openbis.js'

export default class UserFormFacade {
  async loadUser(userId) {
    const id = new openbis.PersonPermId(userId)
    const fo = new openbis.PersonFetchOptions()
    fo.withSpace()
    fo.withRoleAssignments()
    return openbis.getPersons([id], fo).then(map => {
      return map[userId]
    })
  }

  async loadUserGroups(userId) {
    const criteria = new openbis.AuthorizationGroupSearchCriteria()
    const fo = new openbis.AuthorizationGroupFetchOptions()
    fo.withUsers()
    fo.withRoleAssignments()
    return openbis.searchAuthorizationGroups(criteria, fo).then(result => {
      return result.getObjects().filter(group => {
        return group.getUsers().some(user => {
          return user.userId === userId
        })
      })
    })
  }
}
