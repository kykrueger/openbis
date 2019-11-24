const autoBind = require('auto-bind')

import { dto } from './dto.js'

export class Facade {
  constructor() {
    autoBind(this)
  }

  init() {
    let _this = this
    return new Promise((resolve, reject) => {
      /* eslint-disable-next-line no-undef */
      requirejs(
        ['openbis'],
        openbis => {
          _this.v3 = new openbis()
          resolve()
        },
        error => {
          reject(error)
        }
      )
    })
  }

  login(user, password) {
    let v3 = this.v3
    return new Promise((resolve, reject) => {
      v3.login(user, password)
        .done(resolve)
        .fail(() => {
          reject({ message: 'Login failed' })
        })
    })
  }

  logout() {
    return this.v3.logout()
  }

  getPropertyTypes(ids, fo) {
    return this.v3.getPropertyTypes(ids, fo)
  }

  getPersons(ids, fo) {
    return this.v3.getPersons(ids, fo)
  }

  updatePersons(updates) {
    return this.v3.updatePersons(updates)
  }

  searchPropertyTypes(criteria, fo) {
    return this.v3.searchPropertyTypes(criteria, fo)
  }

  searchMaterials(criteria, fo) {
    return this.v3.searchMaterials(criteria, fo)
  }

  searchVocabularies(criteria, fo) {
    return this.v3.searchVocabularies(criteria, fo)
  }

  searchVocabularyTerms(criteria, fo) {
    return this.v3.searchVocabularyTerms(criteria, fo)
  }

  searchPersons(criteria, fo) {
    return this.v3.searchPersons(criteria, fo)
  }

  searchAuthorizationGroups(criteria, fo) {
    return this.v3.searchAuthorizationGroups(criteria, fo)
  }

  getSampleTypes(ids, fo) {
    return this.v3.getSampleTypes(ids, fo)
  }

  searchSamples(criteria, fo) {
    return this.v3.searchSamples(criteria, fo)
  }

  searchSampleTypes(criteria, fo) {
    return this.v3.searchSampleTypes(criteria, fo)
  }

  updateSampleTypes(updates) {
    return this.v3.updateSampleTypes(updates)
  }

  searchExperimentTypes(criteria, fo) {
    return this.v3.searchExperimentTypes(criteria, fo)
  }

  searchDataSetTypes(criteria, fo) {
    return this.v3.searchDataSetTypes(criteria, fo)
  }

  searchMaterialTypes(criteria, fo) {
    return this.v3.searchMaterialTypes(criteria, fo)
  }

  executeService() {
    let id = new dto.CustomASServiceCode('openbis-ng-ui-service')
    let options = new dto.CustomASServiceExecutionOptions()
    return this.v3.executeCustomASService(id, options)
  }

  executeOperations(operations, options) {
    return this.v3.executeOperations(operations, options)
  }
}

const facade = new Facade()
export { facade }
