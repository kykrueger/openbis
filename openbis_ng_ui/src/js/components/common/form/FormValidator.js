class FormValidator {
  validateNotEmpty(label, name, value, errors) {
    if (value === null || value === undefined || value.trim() === '') {
      errors.push({
        field: name,
        message: label + ' cannot be empty'
      })
    }
  }
}

export default new FormValidator()
