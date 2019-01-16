import { store } from '../index.js'


let v3 = null
/* eslint-disable-next-line no-undef */
requirejs([ 'openbis' ], openbis => {
  v3 = new openbis()
  store.dispatch({type: 'INIT'})
})

function login(user, password) {
  return new Promise( (resolve, reject) => {
    v3.login(user, password).done(resolve).fail(() => {
      reject({ message: 'Login failed' })
    })
  })
}

function logout() {
  return new Promise( (resolve, reject) => {
    v3.logout().done(resolve).fail(reject)
  })
}

function getSpaces() {
  return new Promise( (resolve, reject) => {
    /* eslint-disable-next-line no-undef */
    requirejs(
      ['as/dto/space/search/SpaceSearchCriteria', 'as/dto/space/fetchoptions/SpaceFetchOptions' ], 
      (SpaceSearchCriteria, SpaceFetchOptions) => 
        v3.searchSpaces(new SpaceSearchCriteria(), new SpaceFetchOptions()).done(resolve).fail(reject)
    )
  })
}

function updateSpace(permId, description) {
  return new Promise( (resolve, reject) => {
    /* eslint-disable-next-line no-undef */
    requirejs(
      ['as/dto/space/update/SpaceUpdate'], 
      (SpaceUpdate) => {
        let spaceUpdate = new SpaceUpdate()
        spaceUpdate.setSpaceId(permId)
        spaceUpdate.setDescription(description)
        v3.updateSpaces([spaceUpdate]).done(resolve).fail(reject)
      }
    )
  })
}

function searchProjects(spacePermId) {
  return new Promise( (resolve, reject) => {
    /* eslint-disable-next-line no-undef */
    requirejs(
      ['as/dto/project/search/ProjectSearchCriteria', 'as/dto/project/fetchoptions/ProjectFetchOptions'], 
      (ProjectSearchCriteria, ProjectFetchOptions) => {
        let searchCriteria = new ProjectSearchCriteria()
        searchCriteria.withSpace().withPermId().thatEquals(spacePermId)
        let fetchOptions = new ProjectFetchOptions()
        v3.searchProjects(searchCriteria, fetchOptions).done(resolve).fail(reject)
      }
    )
  })
}

export default {
  login: login,
  logout: logout,
  getSpaces: getSpaces,
  updateSpace: updateSpace,
  searchProjects: searchProjects,
}
