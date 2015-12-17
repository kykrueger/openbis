define([ "require", "stjs", "dto/common/fetchoptions/FetchOptions", "dto/service/fetchoptions/ServiceSortOptions" ], function(require, stjs, FetchOptions) {
  var ServiceFetchOptions = function() {
  };
  stjs.extend(ServiceFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
    prototype['@type'] = 'dto.service.fetchoptions.ServiceFetchOptions';
    constructor.serialVersionUID = 1;
    prototype.sort = null;
    prototype.sortBy = function() {
      if (this.sort == null) {
        var ServiceSortOptions = require("dto/service/fetchoptions/ServiceSortOptions");
        this.sort = new ServiceSortOptions();
      }
      return this.sort;
    };
    prototype.getSortBy = function() {
      return this.sort;
    };
  }, {
    sort : "ServiceSortOptions"
  });
  return ServiceFetchOptions;
})