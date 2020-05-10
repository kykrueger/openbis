import TypeFormPreviewPropertyWrapper from './TypeFormPreviewPropertyWrapper.js'

export default class TypeFormPreviewSectionWrapper {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getName() {
    return this.wrapper.find('TypeFormHeader')
  }

  getProperties() {
    const properties = []
    this.wrapper.find('TypeFormPreviewProperty').forEach(propertyWrapper => {
      properties.push(new TypeFormPreviewPropertyWrapper(propertyWrapper))
    })
    return properties
  }

  toJSON() {
    return {
      name: this.getName().text(),
      properties: this.getProperties().map(property => property.toJSON())
    }
  }
}
