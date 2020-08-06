export default class VocabularyFormControllerSave {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
    this.facade = controller.facade
    this.object = controller.object
  }

  async execute() {
    await this.context.setState({
      validate: true
    })

    await this.controller.validate(true)
  }
}
