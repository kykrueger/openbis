export default class ObjectTypeHandlerLoad {
  constructor(objectId, getState, setState, facade) {
    this.objectId = objectId
    this.facade = facade
    this.getState = getState
    this.setState = setState
  }

  execute() {
    this.setState({
      loading: true,
      validate: false
    })

    return Promise.all([
      this.facade.loadType(this.objectId),
      this.facade.loadUsages(this.objectId)
    ])
      .then(([loadedType, loadedUsages]) => {
        const type = {
          code: loadedType.code,
          description: loadedType.description,
          listable: loadedType.listable,
          showContainer: loadedType.showContainer,
          showParents: loadedType.showParents,
          showParentMetadata: loadedType.showParentMetadata,
          autoGeneratedCode: loadedType.autoGeneratedCode,
          generatedCodePrefix: loadedType.generatedCodePrefix,
          subcodeUnique: loadedType.subcodeUnique,
          validationPlugin: loadedType.validationPlugin
            ? loadedType.validationPlugin.name
            : null,
          errors: {},
          usages: loadedUsages.type
        }

        const sections = []
        const properties = []
        let currentSection = null
        let currentProperty = null
        let sectionsCounter = 0
        let propertiesCounter = 0

        loadedType.propertyAssignments.forEach(assignment => {
          currentProperty = {
            id: 'property-' + propertiesCounter++,
            code: assignment.propertyType.code,
            label: assignment.propertyType.label,
            description: assignment.propertyType.description,
            dataType: assignment.propertyType.dataType,
            plugin: assignment.plugin ? assignment.plugin.name : null,
            vocabulary: assignment.propertyType.vocabulary
              ? assignment.propertyType.vocabulary.code
              : null,
            materialType: assignment.propertyType.materialType
              ? assignment.propertyType.materialType.code
              : null,
            schema: assignment.propertyType.schema,
            transformation: assignment.propertyType.transformation,
            mandatory: assignment.mandatory,
            showInEditView: assignment.showInEditView,
            showRawValueInForms: assignment.showRawValueInForms,
            errors: {},
            usages: loadedUsages.property[assignment.propertyType.code] || 0
          }

          if (currentSection && currentSection.name === assignment.section) {
            currentSection.properties.push(currentProperty.id)
          } else {
            currentSection = {
              id: 'section-' + sectionsCounter++,
              name: assignment.section,
              properties: [currentProperty.id]
            }
            sections.push(currentSection)
          }

          currentProperty.section = currentSection.id
          currentProperty.original = {
            ...currentProperty
          }

          properties.push(currentProperty)
        })

        type.original = {
          ...type,
          properties
        }

        this.setState(() => ({
          type,
          properties,
          propertiesCounter,
          sections,
          sectionsCounter,
          selection: null,
          removeSectionDialogOpen: false,
          removePropertyDialogOpen: false
        }))
      })
      .catch(error => {
        this.facade.catch(error)
      })
      .finally(() => {
        this.setState({
          loading: false
        })
      })
  }
}
