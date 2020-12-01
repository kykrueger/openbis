define(['require', 'stjs', 'as/dto/common/fetchoptions/SortOptions'], function (
  require,
  stjs,
  SortOptions
) {
  var QueryDatabaseSortOptions = function () {
    SortOptions.call(this)
  }
  stjs.extend(
    QueryDatabaseSortOptions,
    SortOptions,
    [SortOptions],
    function (constructor, prototype) {
      prototype['@type'] = 'as.dto.query.fetchoptions.QueryDatabaseSortOptions'
      constructor.serialVersionUID = 1
    },
    {}
  )
  return QueryDatabaseSortOptions
})
