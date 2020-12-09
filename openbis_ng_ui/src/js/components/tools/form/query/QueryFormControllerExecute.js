import openbis from '@src/js/services/openbis.js'
import actions from '@src/js/store/actions/actions.js'

export default class QueryFormControllerExecute {
  constructor(controller) {
    this.controller = controller
    this.context = controller.getContext()
    this.facade = controller.getFacade()
  }

  async execute() {
    try {
      const { query } = this.context.getState()

      this.context.setState(state => ({
        ...state,
        results: {
          ...state.results,
          loading: true
        }
      }))

      const tableModel = await this.executeSql(query)

      this.context.setState(state => ({
        ...state,
        results: {
          ...state.results,
          loaded: true,
          tableModel
        }
      }))
    } catch (error) {
      this.context.dispatch(actions.errorChange(error))
    } finally {
      this.context.setState(state => ({
        ...state,
        results: {
          ...state.results,
          loading: false
        }
      }))
    }
  }

  async executeSql(query) {
    const sql = query.sql.value
    const options = new openbis.SqlExecutionOptions()
    options.withDatabaseId(
      new openbis.QueryDatabaseName(query.databaseId.value)
    )
    return await this.facade.executeSql(sql, options)
  }
}
