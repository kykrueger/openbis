import openbis from '@src/js/services/openbis.js'

export default class VocabularyFormFacade {
  async loadVocabulary(code) {
    const id = new openbis.VocabularyPermId(code)
    const fo = new openbis.VocabularyFetchOptions()
    fo.withTerms()
    return openbis.getVocabularies([id], fo).then(map => {
      return map[code]
    })
  }
}
