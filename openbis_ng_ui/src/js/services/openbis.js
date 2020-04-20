import dto from './openbis/dto.js'
import api from './openbis/api.js'

class Openbis {
  async init() {
    await Promise.all([dto._init(), api._init()])
    Object.assign(this, dto)
    Object.assign(this, api)
  }
}

const openbis = new Openbis()
export default openbis
