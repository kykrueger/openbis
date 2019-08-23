import _ from 'lodash'
import { matchPath } from 'react-router'
import pathToRegexp from 'path-to-regexp'
import * as pages from './pages.js'
import * as objectTypes from './objectType.js'

function doFormat(actualParams, pattern, requiredParams){
  if(_.isMatch(actualParams, requiredParams)){
    let toPath = pathToRegexp.compile(pattern)
    return {
      path: toPath(actualParams),
      match: Object.keys(requiredParams).length
    }
  }else{
    return null
  }
}

function doParse(path, pattern, extraParams){
  let match = matchPath(path, {
    path: pattern,
    exact: true,
    strict: false
  })
  if(match){
    return {
      path: match.url,
      ...match.params,
      ...extraParams
    }
  }else{
    return null
  }
}

const routes = {
  TYPES: {
    format: (params) => {
      return doFormat(params, '/types', {
        page: pages.TYPES
      })
    },
    parse: (path) => {
      return doParse(path, '/types', {
        page: pages.TYPES
      })
    }
  },
  TYPES_SEARCH: {
    format: (params) => {
      return doFormat(params, '/typesSearch/:id', {
        page: pages.TYPES,
        type: objectTypes.SEARCH
      })
    },
    parse: (path) => {
      return doParse(path, '/typesSearch/:id', {
        page: pages.TYPES,
        type: objectTypes.SEARCH
      })
    }
  },
  OBJECT_TYPE: {
    format: (params) => {
      return doFormat(params, '/objectType/:id', {
        page: pages.TYPES,
        type: objectTypes.OBJECT_TYPE
      })
    },
    parse: (path) => {
      return doParse(path, '/objectType/:id', {
        page: pages.TYPES,
        type: objectTypes.OBJECT_TYPE
      })
    }
  },
  COLLECTION_TYPE: {
    format: (params) => {
      return doFormat(params, '/collectionType/:id', {
        page: pages.TYPES,
        type: objectTypes.COLLECTION_TYPE
      })
    },
    parse: (path) => {
      return doParse(path, '/collectionType/:id', {
        page: pages.TYPES,
        type: objectTypes.COLLECTION_TYPE
      })
    }
  },
  DATA_SET_TYPE: {
    format: (params) => {
      return doFormat(params, '/datasetType/:id', {
        page: pages.TYPES,
        type: objectTypes.DATA_SET_TYPE
      })
    },
    parse: (path) => {
      return doParse(path, '/datasetType/:id', {
        page: pages.TYPES,
        type: objectTypes.DATA_SET_TYPE
      })
    }
  },
  MATERIAL_TYPE: {
    format: (params) => {
      return doFormat(params, '/materialType/:id', {
        page: pages.TYPES,
        type: objectTypes.MATERIAL_TYPE
      })
    },
    parse: (path) => {
      return doParse(path, '/materialType/:id', {
        page: pages.TYPES,
        type: objectTypes.MATERIAL_TYPE
      })
    }
  },
  USERS: {
    format: (params) => {
      return doFormat(params, '/users', {
        page: pages.USERS
      })
    },
    parse: (path) => {
      return doParse(path, '/users', {
        page: pages.USERS
      })
    }
  },
  USERS_SEARCH: {
    format: (params) => {
      return doFormat(params, '/usersSearch/:id', {
        page: pages.USERS,
        type: objectTypes.SEARCH
      })
    },
    parse: (path) => {
      return doParse(path, '/usersSearch/:id', {
        page: pages.USERS,
        type: objectTypes.SEARCH
      })
    }
  },
  USER: {
    format: (params) => {
      return doFormat(params, '/user/:id', {
        page: pages.USERS,
        type: objectTypes.USER
      })
    },
    parse: (path) => {
      return doParse(path, '/user/:id', {
        page: pages.USERS,
        type: objectTypes.USER
      })
    }
  },
  GROUP: {
    format: (params) => {
      return doFormat(params, '/group/:id', {
        page: pages.USERS,
        type: objectTypes.GROUP
      })
    },
    parse: (path) => {
      return doParse(path, '/group/:id', {
        page: pages.USERS,
        type: objectTypes.GROUP
      })
    }
  },
  DEFAULT: {
    format: (params) => {
      return {
        path: '/',
        match: 0
      }
    },
    parse: (path) => {
      return {
        path: '/',
        page: pages.TYPES
      }
    }
  }
}

function format(params){
  let keys = Object.keys(routes)
  let bestPath = null
  let bestMatch = 0
  for(let i = 0; i < keys.length; i++){
    let route = routes[keys[i]]
    let result = route.format(params)
    if(result && result.match > bestMatch){
      bestPath = result.path
    }
  }
  return bestPath
}

function parse(path){
  let keys = Object.keys(routes)
  for(let i = 0; i < keys.length; i++){
    let route = routes[keys[i]]
    let params = route.parse(path)
    if(params){
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
