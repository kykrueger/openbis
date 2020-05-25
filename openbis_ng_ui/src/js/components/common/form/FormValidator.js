class FormValidator {
  constructor() {
    this.errors = []
    this.objects = []
  }

  validateNotEmpty(object, name, label) {
    this._validateField(object, name, () => {
      const field = object[name]
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

  _validateField(object, name, fn) {
    const field = object[name]

    const oldError = field.error
    const newError = fn()

    if (!this.objects.includes(object)) {
      this.objects.push(object)
      object.errors = 0
    }

    if (newError !== oldError) {
      object[name] = {
        ...field,
        error: newError
      }
    }

    if (newError) {
      object.errors++
      this.errors.push({
        object,
        name,
        error: newError
      })
    }
  }

  getErrors() {
    return this.errors
  }
}

export default FormValidator
