import dto from './dto.js'

const login = jest.fn()
const logout = jest.fn()
const getPropertyTypes = jest.fn()
const getPersons = jest.fn()
const updatePersons = jest.fn()
const searchPropertyTypes = jest.fn()
const searchPlugins = jest.fn()
const searchMaterials = jest.fn()
const searchVocabularies = jest.fn()
const searchVocabularyTerms = jest.fn()
const searchPersons = jest.fn()
const searchAuthorizationGroups = jest.fn()
const getSampleTypes = jest.fn()
const searchSamples = jest.fn()
const searchSampleTypes = jest.fn()
const updateSampleTypes = jest.fn()
const searchExperimentTypes = jest.fn()
const searchDataSetTypes = jest.fn()
const searchMaterialTypes = jest.fn()
const executeOperations = jest.fn()
const deleteSampleTypes = jest.fn()
const deleteExperimentTypes = jest.fn()
const deleteDataSetTypes = jest.fn()
const deleteMaterialTypes = jest.fn()

const mockSearchGroups = groups => {
  const searchGroupsResult = new dto.SearchResult()
  searchGroupsResult.setObjects(groups)

  searchAuthorizationGroups.mockReturnValue(Promise.resolve(searchGroupsResult))
}

const mockSearchPersons = persons => {
  const searchPersonResult = new dto.SearchResult()
  searchPersonResult.setObjects(persons)

  searchPersons.mockReturnValue(Promise.resolve(searchPersonResult))
}

const mockSearchSampleTypes = sampleTypes => {
  const searchResult = new dto.SearchResult()
  searchResult.setObjects(sampleTypes)
  searchSampleTypes.mockReturnValue(Promise.resolve(searchResult))
}

const mockSearchExperimentTypes = experimentTypes => {
  const searchResult = new dto.SearchResult()
  searchResult.setObjects(experimentTypes)
  searchExperimentTypes.mockReturnValue(Promise.resolve(searchResult))
}

const mockSearchDataSetTypes = dataSetTypes => {
  const searchResult = new dto.SearchResult()
  searchResult.setObjects(dataSetTypes)
  searchDataSetTypes.mockReturnValue(Promise.resolve(searchResult))
}

const mockSearchMaterialTypes = materialTypes => {
  const searchResult = new dto.SearchResult()
  searchResult.setObjects(materialTypes)
  searchMaterialTypes.mockReturnValue(Promise.resolve(searchResult))
}

export default {
  login,
  logout,
  getPropertyTypes,
  getPersons,
  updatePersons,
  searchPropertyTypes,
  searchPlugins,
  searchMaterials,
  searchVocabularies,
  searchVocabularyTerms,
  searchPersons,
  searchAuthorizationGroups,
  getSampleTypes,
  searchSamples,
  searchSampleTypes,
  updateSampleTypes,
  searchExperimentTypes,
  searchDataSetTypes,
  searchMaterialTypes,
  executeOperations,
  deleteSampleTypes,
  deleteExperimentTypes,
  deleteDataSetTypes,
  deleteMaterialTypes,
  mockSearchGroups,
  mockSearchPersons,
  mockSearchSampleTypes,
  mockSearchExperimentTypes,
  mockSearchDataSetTypes,
  mockSearchMaterialTypes
}
