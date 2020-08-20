const CODE_PATTERN = /^[A-Za-z0-9_\\-\\.:]+$/

class FormValidator {
  constructor(mode) {
    this.mode = mode
    this.objects = new Set()
    this.fields = new Set()
    this.errors = []
  }

  validateNotEmpty(object, name, label) {
    this._validateField(object, name, field => {
      if (this.mode !== 'full') {
        return null
      }
      if (
        field.value === null ||
        field.value === undefined ||
        field.value.trim() === ''
      ) {
        return label + ' cannot be empty'
      } else {
        return null
      }
    })
  }

  validatePattern(object, name, label, pattern) {
    this._validateField(object, name, field => {
      if (
        field.value === null ||
        field.value === undefined ||
        field.value.trim() === ''
      ) {
        return null
      } else {
        if (pattern.test(field.value)) {
          return null
        } else {
          return label
        }
      }
    })
  }

  validateCode(object, name, label) {
    this.validatePattern(
      object,
      name,
      label + ' can only contain A-Z, a-z, 0-9 and _, -, .',
      CODE_PATTERN
    )
  }

  _validateField(object, name, fn) {
    if (!this.objects.has(object)) {
      object.errors = 0
      this.objects.add(object)
    }

    if (!this.fields.has(object[name])) {
      object[name] = {
        ...object[name],
        error: null
      }
      this.fields.add(object[name])
    }

    const error = fn(object[name])

    if (error) {
      object.errors++
      object[name].error = error
      this.errors.push({ object, name, error })
    }
  }

  getErrors() {
    return this.errors
  }
}

export default FormValidator
