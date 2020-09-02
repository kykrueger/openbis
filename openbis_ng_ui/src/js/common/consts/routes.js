import _ from 'lodash'
import { compile, match } from 'path-to-regexp'
import pages from '@src/js/common/consts/pages.js'
import objectTypes from '@src/js/common/consts/objectType.js'

function doFormat(actualParams, pattern, requiredParams) {
  if (_.isMatch(actualParams, requiredParams)) {
    let toPath = compile(pattern, { encode: encodeURIComponent })
    return {
      path: toPath(actualParams),
      match: Object.keys(requiredParams).length
    }
  } else {
    return null
  }
}

function doParse(path, pattern, extraParams) {
  let toPathObject = match(pattern, { decode: decodeURIComponent })
  let pathObject = toPathObject(path)
  if (pathObject) {
    return {
      path: pathObject.path,
      ...pathObject.params,
      ...extraParams
    }
  } else {
    return null
  }
}

const routes = {
  TYPES: {
    format: params => {
      return doFormat(params, '/types', {
        page: pages.TYPES
      })
    },
    parse: path => {
      return doParse(path, '/types', {
        page: pages.TYPES
      })
    }
  },
  TYPES_SEARCH: {
    format: params => {
      return doFormat(params, '/types-search/:id', {
        page: pages.TYPES,
        type: objectTypes.SEARCH
      })
    },
    parse: path => {
      return doParse(path, '/types-search/:id', {
        page: pages.TYPES,
        type: objectTypes.SEARCH
      })
    }
  },
  NEW_OBJECT_TYPE: {
    format: params => {
      return doFormat(params, '/new-object-type/:id', {
        page: pages.TYPES,
        type: objectTypes.NEW_OBJECT_TYPE
      })
    },
    parse: path => {
      return doParse(path, '/new-object-type/:id', {
        page: pages.TYPES,
        type: objectTypes.NEW_OBJECT_TYPE
      })
    }
  },
  OBJECT_TYPE: {
    format: params => {
      return doFormat(params, '/object-type/:id', {
        page: pages.TYPES,
        type: objectTypes.OBJECT_TYPE
      })
    },
    parse: path => {
      return doParse(path, '/object-type/:id', {
        page: pages.TYPES,
        type: objectTypes.OBJECT_TYPE
      })
    }
  },
  NEW_COLLECTION_TYPE: {
    format: params => {
      return doFormat(params, '/new-collection-type/:id', {
        page: pages.TYPES,
        type: objectTypes.NEW_COLLECTION_TYPE
      })
    },
    parse: path => {
      return doParse(path, '/new-collection-type/:id', {
        page: pages.TYPES,
        type: objectTypes.NEW_COLLECTION_TYPE
      })
    }
  },
  COLLECTION_TYPE: {
    format: params => {
      return doFormat(params, '/collection-type/:id', {
        page: pages.TYPES,
        type: objectTypes.COLLECTION_TYPE
      })
    },
    parse: path => {
      return doParse(path, '/collection-type/:id', {
        page: pages.TYPES,
        type: objectTypes.COLLECTION_TYPE
      })
    }
  },
  NEW_DATA_SET_TYPE: {
    format: params => {
      return doFormat(params, '/new-dataset-type/:id', {
        page: pages.TYPES,
        type: objectTypes.NEW_DATA_SET_TYPE
      })
    },
    parse: path => {
      return doParse(path, '/new-dataset-type/:id', {
        page: pages.TYPES,
        type: objectTypes.NEW_DATA_SET_TYPE
      })
    }
  },
  DATA_SET_TYPE: {
    format: params => {
      return doFormat(params, '/dataset-type/:id', {
        page: pages.TYPES,
        type: objectTypes.DATA_SET_TYPE
      })
    },
    parse: path => {
      return doParse(path, '/dataset-type/:id', {
        page: pages.TYPES,
        type: objectTypes.DATA_SET_TYPE
      })
    }
  },
  NEW_MATERIAL_TYPE: {
    format: params => {
      return doFormat(params, '/new-material-type/:id', {
        page: pages.TYPES,
        type: objectTypes.NEW_MATERIAL_TYPE
      })
    },
    parse: path => {
      return doParse(path, '/new-material-type/:id', {
        page: pages.TYPES,
        type: objectTypes.NEW_MATERIAL_TYPE
      })
    }
  },
  MATERIAL_TYPE: {
    format: params => {
      return doFormat(params, '/material-type/:id', {
        page: pages.TYPES,
        type: objectTypes.MATERIAL_TYPE
      })
    },
    parse: path => {
      return doParse(path, '/material-type/:id', {
        page: pages.TYPES,
        type: objectTypes.MATERIAL_TYPE
      })
    }
  },
  NEW_VOCABULARY_TYPE: {
    format: params => {
      return doFormat(params, '/new-vocabulary-type/:id', {
        page: pages.TYPES,
        type: objectTypes.NEW_VOCABULARY_TYPE
      })
    },
    parse: path => {
      return doParse(path, '/new-vocabulary-type/:id', {
        page: pages.TYPES,
        type: objectTypes.NEW_VOCABULARY_TYPE
      })
    }
  },
  VOCABULARY_TYPE: {
    format: params => {
      return doFormat(params, '/vocabulary-type/:id', {
        page: pages.TYPES,
        type: objectTypes.VOCABULARY_TYPE
      })
    },
    parse: path => {
      return doParse(path, '/vocabulary-type/:id', {
        page: pages.TYPES,
        type: objectTypes.VOCABULARY_TYPE
      })
    }
  },
  USERS: {
    format: params => {
      return doFormat(params, '/users', {
        page: pages.USERS
      })
    },
    parse: path => {
      return doParse(path, '/users', {
        page: pages.USERS
      })
    }
  },
  USERS_SEARCH: {
    format: params => {
      return doFormat(params, '/users-search/:id', {
        page: pages.USERS,
        type: objectTypes.SEARCH
      })
    },
    parse: path => {
      return doParse(path, '/users-search/:id', {
        page: pages.USERS,
        type: objectTypes.SEARCH
      })
    }
  },
  NEW_USER: {
    format: params => {
      return doFormat(params, '/new-user/:id', {
        page: pages.USERS,
        type: objectTypes.NEW_USER
      })
    },
    parse: path => {
      return doParse(path, '/new-user/:id', {
        page: pages.USERS,
        type: objectTypes.NEW_USER
      })
    }
  },
  USER: {
    format: params => {
      return doFormat(params, '/user/:id', {
        page: pages.USERS,
        type: objectTypes.USER
      })
    },
    parse: path => {
      return doParse(path, '/user/:id', {
        page: pages.USERS,
        type: objectTypes.USER
      })
    }
  },
  NEW_USER_GROUP: {
    format: params => {
      return doFormat(params, '/new-user-group/:id', {
        page: pages.USERS,
        type: objectTypes.NEW_USER_GROUP
      })
    },
    parse: path => {
      return doParse(path, '/new-user-group/:id', {
        page: pages.USERS,
        type: objectTypes.NEW_USER_GROUP
      })
    }
  },
  USER_GROUP: {
    format: params => {
      return doFormat(params, '/user-group/:id', {
        page: pages.USERS,
        type: objectTypes.USER_GROUP
      })
    },
    parse: path => {
      return doParse(path, '/user-group/:id', {
        page: pages.USERS,
        type: objectTypes.USER_GROUP
      })
    }
  },
  DEFAULT: {
    format: () => {
      return {
        path: '/',
        match: 0
      }
    },
    parse: () => {
      return {
        path: '/',
        page: pages.TYPES
      }
    }
  }
}

function format(params) {
  let keys = Object.keys(routes)
  let best = { match: 0, path: null }

  for (let i = 0; i < keys.length; i++) {
    let route = routes[keys[i]]
    try {
      let result = route.format(params)
      if (result && result.match > best.match) {
        best = result
      }
    } catch (err) {
      // ignore problems with incorrect params
    }
  }

  return best.path
}

function parse(path) {
  let keys = Object.keys(routes)
  for (let i = 0; i < keys.length; i++) {
    let route = routes[keys[i]]
    let params = route.parse(path)
    if (params) {
      return params
    }
  }
  return null
}

export default {
  ...routes,
  format,
  parse
}
