import _ from 'lodash'

class FormUtil {
  createField(params = {}) {
    return {
      value: null,
      visible: true,
      enabled: true,
      ...params
    }
  }

  changeObjectField(state, stateKey, field, value, processFn) {
    const oldObject = state[stateKey]
    const newObject = {
      ...oldObject,
      [field]: {
        ...oldObject[field],
        value
      }
    }
    return {
      [stateKey]: processFn ? processFn(oldObject, newObject) : newObject
    }
  }

  changeCollectionItemField(state, stateKey, itemId, field, value, processFn) {
    const oldCollection = state[stateKey]
    const newCollection = Array.from(oldCollection)

    const index = oldCollection.findIndex(item => item.id === itemId)

    const oldItem = oldCollection[index]
    const newItem = {
      ...oldItem,
      [field]: {
        ...oldItem[field],
        value
      }
    }

    newCollection[index] = processFn ? processFn(oldItem, newItem) : newItem

    return {
      [stateKey]: newCollection
    }
  }

  hasFieldChanged(currentObject, originalObject, path) {
    const currentValue = _.get(currentObject, path)
    const originalValue = originalObject ? _.get(originalObject, path) : null
    return originalValue.value !== currentValue.value
  }

  haveFieldsChanged(currentObject, originalObject, paths) {
    return _.some(paths, path =>
      this.hasFieldChanged(currentObject, originalObject, path)
    )
  }

  trimFields(object) {
    const trimString = str => {
      const trimmed = str.trim()
      return trimmed.length > 0 ? trimmed : null
    }

    const trimField = field => {
      if (field) {
        if (_.isString(field)) {
          return trimString(field)
        } else if (_.isObject(field) && _.isString(field.value)) {
          return {
            ...field,
            value: trimString(field.value)
          }
        }
      }
      return field
    }

    return _.mapValues(
      {
        ...object
      },
      trimField
    )
  }
}

export default new FormUtil()
