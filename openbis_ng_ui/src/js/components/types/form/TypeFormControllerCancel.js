export default class TypeFormControllerEdit {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  executeCancel(confirmed = false) {
    const { changed } = this.context.getState()
    if (!changed || confirmed) {
      return this.context
        .setState({
          unsavedChangesDialogOpen: false
        })
        .then(() => {
          return this.controller.load()
        })
        .then(() => {
          return this.context.setState({
            mode: 'view'
          })
        })
    } else {
      this.context.setState({
        unsavedChangesDialogOpen: true
      })
    }
  }

  executeCancelCancel() {
    this.context.setState({
      unsavedChangesDialogOpen: false
    })
  }
}
