export default class BaseWrapper {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getStringValue(value) {
    if (value === null || value === undefined || value === '') {
      return null
    } else {
      return value
    }
  }

  getBooleanValue(value) {
    return value === undefined || value === null || value === false
  }

  async update() {
    // update the wrapper after all queued async callbacks are finished
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        try {
          this.wrapper.update()
          resolve()
        } catch (e) {
          reject(e)
        }
      }, 0)
    })
  }

  expectJSON(json) {
    expect(this.toJSON()).toMatchObject(json)
  }
}
