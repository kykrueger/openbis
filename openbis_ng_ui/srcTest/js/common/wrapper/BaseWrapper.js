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

  async expectJSON(json) {
    await new Promise((resolve, reject) => {
      setTimeout(() => {
        try {
          this.wrapper.update()
          expect(this.toJSON()).toMatchObject(json)
          resolve()
        } catch (e) {
          reject(e)
        }
      }, 0)
    })
  }
}
