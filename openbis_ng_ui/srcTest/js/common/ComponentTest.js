import React from 'react'
import { Provider } from 'react-redux'
import { mount } from 'enzyme'
import { createStore } from '@src/js/store/store.js'
import ThemeProvider from '@src/js/components/common/theme/ThemeProvider.jsx'
import actions from '@src/js/store/actions/actions.js'

export default class ComponentTest {
  constructor(createComponentFn, createComponentWrapperFn) {
    this.store = null
    this.createComponentFn = createComponentFn
    this.createComponentWrapperFn = createComponentWrapperFn
  }

  beforeEach() {
    jest.resetAllMocks()
    this.store = createStore()
    this.store.dispatch(actions.init())
  }

  async mount() {
    document.body.innerHTML = '<div></div>'

    const reactWrapper = mount(
      <Provider store={this.getStore()}>
        <ThemeProvider>
          {this.createComponentFn.apply(null, arguments)}
        </ThemeProvider>
      </Provider>,
      {
        attachTo: document.getElementsByTagName('div')[0]
      }
    )

    const componentWrapper = this.createComponentWrapperFn(reactWrapper)
    return componentWrapper.update().then(() => componentWrapper)
  }

  getStore() {
    return this.store
  }
}
