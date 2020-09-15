import dto from '@src/js/services/openbis/dto.js'
import api from '@src/js/services/openbis/api.js'

class Openbis {
  async init() {
    await Promise.all([dto._init(), api._init()])
    Object.assign(this, dto)
    Object.assign(this, api)
  }
}

const openbis = new Openbis()
export default openbis
