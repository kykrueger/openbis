import Typography from '@material-ui/core/Typography'
import TypeFormPreviewProperty from '@src/js/components/types/form/TypeFormPreviewProperty.jsx'

import TypeFormPreviewPropertyWrapper from './TypeFormPreviewPropertyWrapper.js'

export default class TypeFormPreviewSectionWrapper {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getName() {
    return this.wrapper.find(Typography).filter({ 'data-part': 'name' })
  }

  getProperties() {
    const properties = []
    this.wrapper.find(TypeFormPreviewProperty).forEach(propertyWrapper => {
      properties.push(new TypeFormPreviewPropertyWrapper(propertyWrapper))
    })
    return properties
  }

  click() {
    this.wrapper.simulate('click')
  }

  toJSON() {
    const name = this.getName().text().trim()
    return {
      name: name.length > 0 ? name : null,
      properties: this.getProperties().map(property => property.toJSON())
    }
  }
}
