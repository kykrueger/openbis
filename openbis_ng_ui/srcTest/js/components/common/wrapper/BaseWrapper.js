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

  getNumberValue(value) {
    if (value === null || value === undefined || value === '') {
      return null
    } else {
      let number = Number(value)
      if (Number.isNaN(number)) {
        return null
      } else {
        return number
      }
    }
  }

  getBooleanValue(value) {
    if (value === null || value === undefined || value === '') {
      return null
    } else {
      return value === true
    }
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
