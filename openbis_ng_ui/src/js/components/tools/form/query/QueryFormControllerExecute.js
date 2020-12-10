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
      const { query, executeParameters } = this.context.getState()

      this.context.setState(state => ({
        ...state,
        executeResults: {
          ...state.executeResults,
          loading: true
        }
      }))

      const tableModel = await this.executeSql(query, executeParameters.values)

      this.context.setState(state => ({
        ...state,
        executeResults: {
          ...state.executeResults,
          loaded: true,
          tableModel,
          timestamp: Date.now()
        }
      }))
    } catch (error) {
      this.context.dispatch(actions.errorChange(error))
    } finally {
      this.context.setState(state => ({
        ...state,
        executeResults: {
          ...state.executeResults,
          loading: false
        }
      }))
    }
  }

  async executeSql(query, parameters) {
    const sql = query.sql.value
    const options = new openbis.SqlExecutionOptions()
    options.withDatabaseId(
      new openbis.QueryDatabaseName(query.databaseId.value)
    )
    Object.entries(parameters).forEach(([key, value]) => {
      options.withParameter(key, value)
    })
    return await this.facade.executeSql(sql, options)
  }
}
