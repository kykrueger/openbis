const autoBind = require('auto-bind')


export default class Openbis {

  constructor() {
    autoBind(this)
    let _this = this
    /* eslint-disable-next-line no-undef */
    requirejs(['openbis'], openbis => {
      _this.v3 = new openbis()
    })
  }

  login(user, password) {
    let v3 = this.v3
    return new Promise((resolve, reject) => {
      v3.login(user, password).done(resolve).fail(() => {
        reject({message: 'Login failed'})
      })
    })
  }

  logout() {
    let v3 = this.v3
    return new Promise((resolve, reject) => {
      v3.logout().done(resolve).fail(reject)
    })
  }

  getSpaces() {
    let v3 = this.v3
    return new Promise((resolve, reject) => {
      /* eslint-disable-next-line no-undef */
      requirejs(
        ['as/dto/space/search/SpaceSearchCriteria', 'as/dto/space/fetchoptions/SpaceFetchOptions'],
        (SpaceSearchCriteria, SpaceFetchOptions) =>
          v3.searchSpaces(new SpaceSearchCriteria(), new SpaceFetchOptions()).done(resolve).fail(reject)
      )
    })
  }

  updateSpace(permId, description) {
    let v3 = this.v3
    return new Promise((resolve, reject) => {
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

  searchProjects(spacePermId) {
    let v3 = this.v3
    return new Promise((resolve, reject) => {
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

  getUsers() {
    let v3 = this.v3
    return new Promise((resolve, reject) => {
      /* eslint-disable-next-line no-undef */
      requirejs(
        ['as/dto/person/search/PersonSearchCriteria', 'as/dto/person/fetchoptions/PersonFetchOptions'],
        (PersonSearchCriteria, PersonFetchOptions) => {
          v3.searchPersons(new PersonSearchCriteria(), new PersonFetchOptions()).done(resolve).fail(reject)
        })
    })
  }

  getGroups() {
    let v3 = this.v3
    return new Promise((resolve, reject) => {
      /* eslint-disable-next-line no-undef */
      requirejs(
        ['as/dto/authorizationgroup/search/AuthorizationGroupSearchCriteria', 'as/dto/authorizationgroup/fetchoptions/AuthorizationGroupFetchOptions'],
        (AuthorizationGroupSearchCriteria, AuthorizationGroupFetchOptions) => {
          let fo = new AuthorizationGroupFetchOptions()
          fo.withUsers()
          v3.searchAuthorizationGroups(new AuthorizationGroupSearchCriteria(), fo).done(resolve).fail(reject)
        })
    })
  }

  getObjectTypes() {
    let v3 = this.v3
    return new Promise((resolve, reject) => {
      /* eslint-disable-next-line no-undef */
      requirejs(
        ['as/dto/sample/search/SampleTypeSearchCriteria', 'as/dto/sample/fetchoptions/SampleTypeFetchOptions'],
        (SampleTypeSearchCriteria, SampleTypeFetchOptions) => {
          v3.searchSampleTypes(new SampleTypeSearchCriteria(), new SampleTypeFetchOptions()).done(resolve).fail(reject)
        })
    })
  }

  getCollectionTypes() {
    let v3 = this.v3
    return new Promise((resolve, reject) => {
      /* eslint-disable-next-line no-undef */
      requirejs(
        ['as/dto/experiment/search/ExperimentTypeSearchCriteria', 'as/dto/experiment/fetchoptions/ExperimentTypeFetchOptions'],
        (ExperimentTypeSearchCriteria, ExperimentTypeFetchOptions) => {
          v3.searchExperimentTypes(new ExperimentTypeSearchCriteria(), new ExperimentTypeFetchOptions()).done(resolve).fail(reject)
        })
    })
  }

  getDataSetTypes() {
    let v3 = this.v3
    return new Promise((resolve, reject) => {
      /* eslint-disable-next-line no-undef */
      requirejs(
        ['as/dto/dataset/search/DataSetTypeSearchCriteria', 'as/dto/dataset/fetchoptions/DataSetTypeFetchOptions'],
        (DataSetTypeSearchCriteria, DataSetTypeFetchOptions) => {
          v3.searchDataSetTypes(new DataSetTypeSearchCriteria(), new DataSetTypeFetchOptions()).done(resolve).fail(reject)
        })
    })
  }

  getMaterialTypes() {
    let v3 = this.v3
    return new Promise((resolve, reject) => {
      /* eslint-disable-next-line no-undef */
      requirejs(
        ['as/dto/material/search/MaterialTypeSearchCriteria', 'as/dto/material/fetchoptions/MaterialTypeFetchOptions'],
        (MaterialTypeSearchCriteria, MaterialTypeFetchOptions) => {
          v3.searchMaterialTypes(new MaterialTypeSearchCriteria(), new MaterialTypeFetchOptions()).done(resolve).fail(reject)
        })
    })
  }

}
