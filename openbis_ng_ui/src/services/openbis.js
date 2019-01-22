import {store} from '../index.js'


let v3 = null
/* eslint-disable-next-line no-undef */
requirejs(['openbis'], openbis => {
    v3 = new openbis()
})

function login(user, password) {
    return new Promise((resolve, reject) => {
        v3.login(user, password).done(resolve).fail(() => {
            reject({message: 'Login failed'})
        })
    })
}

function logout() {
    return new Promise((resolve, reject) => {
        v3.logout().done(resolve).fail(reject)
    })
}

function getSpaces() {
    return new Promise((resolve, reject) => {
        /* eslint-disable-next-line no-undef */
        requirejs(
            ['as/dto/space/search/SpaceSearchCriteria', 'as/dto/space/fetchoptions/SpaceFetchOptions'],
            (SpaceSearchCriteria, SpaceFetchOptions) =>
                v3.searchSpaces(new SpaceSearchCriteria(), new SpaceFetchOptions()).done(resolve).fail(reject)
        )
    })
}

function getUsers() {
    return new Promise((resolve, reject) => {
        /* eslint-disable-next-line no-undef */
        requirejs(
            ['as/dto/person/search/PersonSearchCriteria', 'as/dto/person/fetchoptions/PersonFetchOptions'],
            (PersonSearchCriteria, PersonFetchOptions) => {
                v3.searchPersons(new PersonSearchCriteria(), new PersonFetchOptions()).done(resolve).fail(reject)
            })
    })
}

function getGroups() {
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

function getObjectTypes() {
    return new Promise((resolve, reject) => {
        /* eslint-disable-next-line no-undef */
        requirejs(
            ['as/dto/sample/search/SampleTypeSearchCriteria', 'as/dto/sample/fetchoptions/SampleTypeFetchOptions'],
            (SampleTypeSearchCriteria, SampleTypeFetchOptions) => {
                v3.searchSampleTypes(new SampleTypeSearchCriteria(), new SampleTypeFetchOptions()).done(resolve).fail(reject)
            })
    })
}

function getCollectionTypes() {
    return new Promise((resolve, reject) => {
        /* eslint-disable-next-line no-undef */
        requirejs(
            ['as/dto/experiment/search/ExperimentTypeSearchCriteria', 'as/dto/experiment/fetchoptions/ExperimentTypeFetchOptions'],
            (ExperimentTypeSearchCriteria, ExperimentTypeFetchOptions) => {
                v3.searchExperimentTypes(new ExperimentTypeSearchCriteria(), new ExperimentTypeFetchOptions()).done(resolve).fail(reject)
            })
    })
}

function getDataSetTypes() {
    return new Promise((resolve, reject) => {
        /* eslint-disable-next-line no-undef */
        requirejs(
            ['as/dto/dataset/search/DataSetTypeSearchCriteria', 'as/dto/dataset/fetchoptions/DataSetTypeFetchOptions'],
            (DataSetTypeSearchCriteria, DataSetTypeFetchOptions) => {
                v3.searchDataSetTypes(new DataSetTypeSearchCriteria(), new DataSetTypeFetchOptions()).done(resolve).fail(reject)
            })
    })
}

function getMaterialTypes() {
    return new Promise((resolve, reject) => {
        /* eslint-disable-next-line no-undef */
        requirejs(
            ['as/dto/material/search/MaterialTypeSearchCriteria', 'as/dto/material/fetchoptions/MaterialTypeFetchOptions'],
            (MaterialTypeSearchCriteria, MaterialTypeFetchOptions) => {
                v3.searchMaterialTypes(new MaterialTypeSearchCriteria(), new MaterialTypeFetchOptions()).done(resolve).fail(reject)
            })
    })
}

function updateSpace(permId, description) {
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

function searchProjects(spacePermId) {
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

export default {
    login: login,
    logout: logout,
    getSpaces: getSpaces,
    getUsers: getUsers,
    getGroups: getGroups,
    getObjectTypes: getObjectTypes,
    getCollectionTypes: getCollectionTypes,
    getDataSetTypes: getDataSetTypes,
    getMaterialTypes: getMaterialTypes,
    updateSpace: updateSpace,
    searchProjects: searchProjects,
}
