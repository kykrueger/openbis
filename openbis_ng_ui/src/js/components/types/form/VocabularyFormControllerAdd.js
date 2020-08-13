import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class VocabularyFormControllerAdd {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
    this.gridController = controller.gridController
  }

  async execute() {
    let { terms, termsCounter } = this.context.getState()

    const newTerm = {
      id: 'term-' + termsCounter++,
      code: FormUtil.createField({}),
      label: FormUtil.createField({}),
      description: FormUtil.createField({}),
      official: FormUtil.createField({
        value: true
      }),
      original: null
    }

    const newTerms = Array.from(terms)
    newTerms.push(newTerm)

    await this.context.setState(state => ({
      ...state,
      terms: newTerms,
      termsCounter,
      selection: {
        type: 'term',
        params: {
          id: newTerm.id,
          part: 'code'
        }
      }
    }))

    await this.controller.changed(true)
    await this.gridController.showSelectedRow()
  }
}
