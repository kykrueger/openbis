export default class TypeFormControllerLoadDictionaries {
  constructor(controller) {
    this.context = controller.context
    this.facade = controller.facade
    this.object = controller.object
  }

  async execute() {
    return Promise.all([
      this.facade.loadValidationPlugins(this.object.type),
      this.facade.loadDynamicPlugins(this.object.type),
      this.facade.loadVocabularies(),
      this.facade.loadMaterialTypes(),
      this.facade.loadGlobalPropertyTypes()
    ]).then(
      ([
        validationPlugins,
        dynamicPlugins,
        vocabularies,
        materialTypes,
        globalPropertyTypes
      ]) => {
        return this.context.setState(() => ({
          dictionaries: {
            validationPlugins,
            dynamicPlugins,
            vocabularies,
            materialTypes,
            globalPropertyTypes
          }
        }))
      }
    )
  }
}
