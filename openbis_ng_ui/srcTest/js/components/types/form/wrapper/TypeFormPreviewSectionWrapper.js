import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import Typography from '@material-ui/core/Typography'
import TypeFormPreviewProperty from '@src/js/components/types/form/TypeFormPreviewProperty.jsx'
import TypeFormPreviewPropertyWrapper from './TypeFormPreviewPropertyWrapper.js'

export default class TypeFormPreviewSectionWrapper extends BaseWrapper {
  getName() {
    return this.findComponent(Typography).filter({ 'data-part': 'name' })
  }

  getProperties() {
    const properties = []
    this.findComponent(TypeFormPreviewProperty).forEach(propertyWrapper => {
      properties.push(new TypeFormPreviewPropertyWrapper(propertyWrapper))
    })
    return properties
  }

  click() {
    this.wrapper.instance().handleClick({
      stopPropagation: () => {}
    })
  }

  toJSON() {
    const name = this.getName().text().trim()
    return {
      name: name.length > 0 ? name : null,
      properties: this.getProperties().map(property => property.toJSON())
    }
  }
}
