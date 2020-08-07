import _ from 'lodash'
import PageControllerSave from '@src/js/components/common/page/PageControllerSave.js'
import pages from '@src/js/common/consts/pages.js'
import actions from '@src/js/store/actions/actions.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@src/js/services/openbis.js'

export default class VocabularyFormControllerSave extends PageControllerSave {
  async save() {
    const state = this.context.getState()
    const vocabulary = this._prepareVocabulary(state.vocabulary)
    const terms = this._prepareTerms(state.terms)
    const operations = []

    if (vocabulary.original) {
      if (this._isVocabularyUpdateNeeded(vocabulary)) {
        operations.push(this._updateVocabularyOperation(vocabulary))
      }
    } else {
      operations.push(this._createVocabularyOperation(vocabulary))
    }

    state.original.terms.forEach(originalTerm => {
      const term = _.find(terms, ['id', originalTerm.id])
      if (!term) {
        operations.push(this._deleteTermOperation(vocabulary, originalTerm))
      }
    })

    terms.forEach(term => {
      if (term.original) {
        if (this._isTermUpdateNeeded(term)) {
          operations.push(this._updateTermOperation(vocabulary, term))
        }
      } else {
        operations.push(this._createTermOperation(vocabulary, term))
      }
    })

    const options = new openbis.SynchronousOperationExecutionOptions()
    options.setExecuteInOrder(true)
    await this.facade.executeOperations(operations, options)

    return vocabulary.code.value
  }

  _prepareVocabulary(vocabulary) {
    let code = vocabulary.code.value

    if (code) {
      code = code.toUpperCase()
    }

    return _.mapValues(
      {
        ...vocabulary,
        code: {
          value: code
        }
      },
      this._prepareValue
    )
  }

  _prepareTerms(terms) {
    return terms.map(term => {
      let code = term.code.value

      if (code) {
        code = code.toUpperCase()
      }

      return _.mapValues(
        {
          ...term,
          code: {
            value: code
          }
        },
        this._prepareValue
      )
    })
  }

  _prepareValue(field) {
    const trim = str => {
      const trimmed = str.trim()
      return trimmed.length > 0 ? trimmed : null
    }

    if (field) {
      if (_.isString(field)) {
        return trim(field)
      } else if (_.isObject(field) && _.isString(field.value)) {
        return {
          ...field,
          value: trim(field.value)
        }
      }
    }

    return field
  }

  _hasPropertyChanged(property, original, path) {
    const originalValue = original ? _.get(original, path) : null
    const currentValue = _.get(property, path)
    return originalValue.value !== currentValue.value
  }

  _isVocabularyUpdateNeeded(vocabulary) {
    return (
      this._hasPropertyChanged(vocabulary, vocabulary.original, 'code') ||
      this._hasPropertyChanged(
        vocabulary,
        vocabulary.original,
        'description'
      ) ||
      this._hasPropertyChanged(vocabulary, vocabulary.original, 'urlTemplate')
    )
  }

  _isTermUpdateNeeded(term) {
    return (
      this._hasPropertyChanged(term, term.original, 'code') ||
      this._hasPropertyChanged(term, term.original, 'label') ||
      this._hasPropertyChanged(term, term.original, 'description') ||
      this._hasPropertyChanged(term, term.original, 'official')
    )
  }

  _createVocabularyOperation(vocabulary) {
    const creation = new openbis.VocabularyCreation()
    creation.setCode(vocabulary.code.value)
    creation.setDescription(vocabulary.description.value)
    creation.setUrlTemplate(vocabulary.urlTemplate.value)
    return new openbis.CreateVocabulariesOperation([creation])
  }

  _updateVocabularyOperation(vocabulary) {
    const update = new openbis.VocabularyUpdate()
    update.setVocabularyId(new openbis.VocabularyPermId(vocabulary.code.value))
    update.setDescription(vocabulary.description.value)
    update.setUrlTemplate(vocabulary.urlTemplate.value)
    return new openbis.UpdateVocabulariesOperation([update])
  }

  _createTermOperation(vocabulary, term) {
    const creation = new openbis.VocabularyTermCreation()
    creation.setVocabularyId(
      new openbis.VocabularyPermId(vocabulary.code.value)
    )
    creation.setCode(term.code.value)
    creation.setLabel(term.label.value)
    creation.setDescription(term.description.value)
    creation.setOfficial(term.official.value)
    return new openbis.CreateVocabularyTermsOperation([creation])
  }

  _updateTermOperation(vocabulary, term) {
    const update = new openbis.VocabularyTermUpdate()
    update.setVocabularyTermId(
      new openbis.VocabularyTermPermId(term.code.value, vocabulary.code.value)
    )
    update.setLabel(term.label.value)
    update.setDescription(term.description.value)
    update.setOfficial(term.official.value)
    return new openbis.UpdateVocabularyTermsOperation([update])
  }

  _deleteTermOperation(vocabulary, term) {
    const termId = new openbis.VocabularyTermPermId(
      term.code.value,
      vocabulary.code.value
    )
    const options = new openbis.VocabularyTermDeletionOptions()
    options.setReason('deleted via ng_ui')
    return new openbis.DeleteVocabularyTermsOperation([termId], options)
  }

  _dispatchActions(oldObject, newObject) {
    if (oldObject.type === objectTypes.NEW_VOCABULARY_TYPE) {
      this.context.dispatch(
        actions.objectCreate(
          pages.TYPES,
          oldObject.type,
          oldObject.id,
          newObject.type,
          newObject.id
        )
      )
    } else if (oldObject.type === objectTypes.VOCABULARY_TYPE) {
      this.context.dispatch(
        actions.objectUpdate(pages.TYPES, oldObject.type, oldObject.id)
      )
    }
  }
}
