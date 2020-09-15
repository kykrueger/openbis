import api from '@src/js/store/actions/api.js'
import app from '@src/js/store/actions/app.js'
import page from '@src/js/store/actions/page.js'

export default {
  ...api,
  ...app,
  ...page
}
