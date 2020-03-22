const facade = {
  login: jest.fn(),
  logout: jest.fn(),
  getPropertyTypes: jest.fn(),
  getPersons: jest.fn(),
  updatePersons: jest.fn(),
  searchPropertyTypes: jest.fn(),
  searchPlugins: jest.fn(),
  searchMaterials: jest.fn(),
  searchVocabularies: jest.fn(),
  searchVocabularyTerms: jest.fn(),
  searchPersons: jest.fn(),
  searchAuthorizationGroups: jest.fn(),
  getSampleTypes: jest.fn(),
  searchSamples: jest.fn(),
  searchSampleTypes: jest.fn(),
  updateSampleTypes: jest.fn(),
  searchExperimentTypes: jest.fn(),
  searchDataSetTypes: jest.fn(),
  searchMaterialTypes: jest.fn(),
  executeOperations: jest.fn()
}
export default facade
