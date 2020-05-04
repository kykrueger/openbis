export default class TypeFormControllerAddProperty {
  constructor(controller) {
    this.context = controller.context
  }

  execute() {
    let {
      sections,
      properties,
      propertiesCounter,
      selection
    } = this.context.getState()

    let sectionIndex = null
    let sectionPropertyIndex = null

    if (selection) {
      if (selection.type === 'section') {
        sectionIndex = sections.findIndex(
          section => section.id === selection.params.id
        )
        sectionPropertyIndex = sections[sectionIndex].properties.length
      } else if (selection.type === 'property') {
        sections.forEach((section, i) => {
          section.properties.forEach((property, j) => {
            if (property === selection.params.id) {
              sectionIndex = i
              sectionPropertyIndex = j + 1
            }
          })
        })
      }
    }

    let section = sections[sectionIndex]

    let newProperties = Array.from(properties)
    let newProperty = {
      id: 'property-' + propertiesCounter++,
      scope: this._createField({
        value: 'local'
      }),
      code: this._createField(),
      label: this._createField(),
      description: this._createField(),
      dataType: this._createField({
        value: 'VARCHAR'
      }),
      plugin: this._createField(),
      vocabulary: this._createField(),
      materialType: this._createField(),
      schema: this._createField(),
      transformation: this._createField(),
      mandatory: this._createField({
        value: false
      }),
      showInEditView: this._createField({
        value: true
      }),
      showRawValueInForms: this._createField({
        value: false
      }),
      initialValueForExistingEntities: this._createField(),
      section: section.id,
      usages: 0,
      errors: 0
    }
    newProperties.push(newProperty)

    let newSection = {
      ...section,
      properties: Array.from(section.properties)
    }
    newSection.properties.splice(sectionPropertyIndex, 0, newProperty.id)

    let newSections = Array.from(sections)
    newSections[sectionIndex] = newSection

    let newSelection = {
      type: 'property',
      params: {
        id: newProperty.id
      }
    }

    this.context.setState(state => ({
      ...state,
      sections: newSections,
      properties: newProperties,
      propertiesCounter,
      selection: newSelection
    }))
  }

  _createField(params = {}) {
    return {
      value: null,
      visible: true,
      ediable: true,
      ...params
    }
  }
}
