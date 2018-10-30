import { store } from '../index.js'

let v3 = null
/* eslint-disable-next-line no-undef */
requirejs([ 'openbis' ], openbis => {
  v3 = new openbis()
  v3.login('admin', 'password').done(() => store.dispatch({type: 'INIT'}))
})

function getSpaces() {
  return new Promise( (resolve, reject) => {
    /* eslint-disable-next-line no-undef */
    requirejs(
      ['as/dto/space/search/SpaceSearchCriteria', 'as/dto/space/fetchoptions/SpaceFetchOptions' ], 
      (SpaceSearchCriteria, SpaceFetchOptions) => 
        v3.searchSpaces(new SpaceSearchCriteria(), new SpaceFetchOptions()).done(res => resolve(res.getObjects()))
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
        v3.updateSpaces([spaceUpdate]).done(res => resolve(res))
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
        v3.searchProjects(searchCriteria, fetchOptions).done(res => resolve(res.getObjects()))
      }
    )
  })
}

export default {
  getSpaces: getSpaces,
  updateSpace: updateSpace,
  searchProjects: searchProjects
}
